#!/bin/bash
if [ $# -lt 5 ]; then
   echo
   echo "Not enough arguments."
   echo
   echo
   echo "Create a directory test_directory/logs/normal, where all logs are stored in a subdirectory with the name being the current date."
   echo "Creates a file info in the log directory for various information on the service and system under test, "
   echo "as well as a file diff to keep track of differences to the current revision."
   echo "Stop the storage service and start the storage service. "
   echo "Generate files to the directory samplefiles. Copy these files from the smallest to the biggest to the storage service, excluding the sparse file."
   echo "Log the time for copying a file to write_time_log. Directly after copying a file, log the time to read it from the storage service to read_time_log." 
   echo "If the checksum of the read file differs from the original, this is noted as an unsuccessful operation."
   echo "After copying a file, log the time for reading the file from the storage service, which was copied in the step before, to previous_file_read_time_log and then delete this file. "
   echo "Stop the storage service."
   echo "The logs' format is as follows: "
   echo "file_size, time_for_operation_in_seconds, average_cpu_load, max_cpu_load, average_mem_load, max_mem_load, max_swap_load, average_net_load, total_net_load, success"
   echo "    file_size  - the size in MB of the file used for the operation"
   echo "    time_for_operation_in_seconds  - how long it took to perform the operation in seconds"
   echo "    average_cpu_load  - the average CPU load of the process measured during the operation"
   echo "    max_cpu_load  - the maximum CPU load of the process measured during the operation"
   echo "    average_mem_load  - the average proportional set size of memory allocated by the process during the operation"
   echo "    max_mem_load  - the maximum proportional set size of memory allocated by the process during the operation"
   echo "    max_swap_load  - the maximum swap allocated by the process during the operation"
   echo "    average_net_load  - the average network load during the operation"
   echo "    total_net_load  - the total network load during the operation"
   echo "    success  - yes or no, depending on whether the operation was successful or not"
   echo
   echo
   echo Usage: `basename $0` storage_path process_name test_directory test_file_sizes username password
   echo "    storage_path  - a path to the directory to test"
   echo "    process_name  - the process name of the storage service, so that it can be found by pgrep"
   echo "    test_directory  - a path to the directory with start_service.sh and stop_service.sh, to start and stop the service, and info.sh to get information about the test (can be empty scripts)"
   echo "    test_file_sizes  - a list of numbers, which gives the size in MB of the test files, as well as their sequence "
   echo "    username  - the username, which is given to the start_service.sh script as the first parameter (can be an arbitrary string if the service does not need authentication)"
   echo "    password  - the password, which is given to the start_service.sh script as the second parameter (can be an arbitrary string if the service does not need authentication)"
   echo
   echo Example: `basename $0` ~/data Wuala . "1 10 100 1000 2000" joe 123456
   echo
   exit
fi  

PROCESS_NAME="$2"
TEST_DIRECTORY="$3"
FILE_SIZES="$4"
LOG_DIR="$TEST_DIRECTORY/logs/normal/`date`"
SAMPLE_FILES_DIR=samplefiles
TEMP_DIR=/tmp/storage_service`date +"%s"`
WRITE_TIME_LOG="$LOG_DIR/write_time_log"
READ_TIME_LOG="$LOG_DIR/read_time_log"
PREVIOUS_FILE_READ_TIME_LOG="$LOG_DIR/previous_file_read_time_log"
STORAGE_SERVICE_PATH="$1"
USERNAME="$5"
PASSWORD="$6"
mkdir -p "$LOG_DIR" "$TEMP_DIR"

$TEST_DIRECTORY/info.sh > "$LOG_DIR/info"
git diff > "$LOG_DIR/diff"

function checksum {
        csum=`md5sum -b "$1" | cut -d " "  -f 1`
        sum=`md5sum -b "$2" | cut -d " "  -f 1`
        if [ "$csum" != "$sum" ]
        then
                echo "ERROR checksum"
        fi
}

function round {
	echo "$1" | python -c "print int(round(float(raw_input())))"
}

function get_db_line { 
    # get "average_cpu_load, max_cpu_load, average_mem_load, max_mem_load, max_swap_load, average_net_load, total_net_load" 
    # from prior execution of../scripts/start_net_mem_cpu_logging.sh, between start and end point given in seconds from the epoch
    # @param 1: start in seconds form the epoch
    # @param 2: stop in seconds form the epoch
    start=$1
    end=$2
    net_total=`gawk -v end=$end -v start=$start '($1 >= start && $1 <= end) {total+=$2+$3} END {print total}' "$TEMP_DIR"/netlog`
    net_avg=`gawk -v end=$end -v start=$start '($1 >= start && $1 <= end) {n++; total+=$2+$3} END {print total/(n+1)}' "$TEMP_DIR"/netlog`
    mem_max=`gawk -v end=$end -v start=$start 'BEGIN {max=0} ($1 >= start && $1 <= end && max < $2) {max=$2} END {print max}' "$TEMP_DIR"/memlog`
    swap_max=`gawk -v end=$end -v start=$start 'BEGIN {max=0} ($1 >= start && $1 <= end && max < $3) {max=$3} END {print max}' "$TEMP_DIR"/memlog`
    mem_avg=`gawk -v end=$end -v start=$start '($1 >= start && $1 <= end) {n++; total+=$2} END {print total/(n+1)}' "$TEMP_DIR"/memlog`
    cpu_max=`gawk -v end=$end -v start=$start 'BEGIN {max=0} ($1 >= start && $1 <= end && max < $2) {max=$2} END {print max}' "$TEMP_DIR"/cpulog`
    cpu_avg=`gawk -v end=$end -v start=$start '($1 >= start && $1 <= end) {n++; total+=$2} END {print total/(n+1)}' "$TEMP_DIR"/cpulog`
    echo -n "$cpu_avg, $cpu_max, $mem_avg, $mem_max, $swap_max, $net_avg, $net_total"
}

function wait_until_transfer_is_complete {
    # Wait until a transfer of size $1 is complete and the network activity stagnated
    # @param 1: size of transfer in MB
    # @param 2: log file with ifstat output, where the first column is the time in seconds from the epoch of when the line was output by ifstat
    # @param 3: the time in seconds from the epoch of when the download started
	sleep 10
	outgoing=`gawk -v start=$3 '($1 >= start) {total+=$2+$3} END {print total}' $2`
	outgoing=`round $outgoing`
	previous_outgoing=$outgoing
	stagnated=0
	while [[ ($outgoing -lt $(($1*1000)) && $stagnated -lt 10) || $stagnated -lt 20 ]];
	do
		if [ $previous_outgoing -eq $outgoing ];
		then
			let stagnated=stagnated+1
		else		
			stagnated=0
			previous_outgoing=$outgoing
		fi
		outgoing=`gawk -v start=$3 '($1 >= start) {total+=$2+$3} END {print total}' $2`
		outgoing=`round $outgoing`
		#echo $outgoing -lt $(($size*1000));
		#echo "waiting for network transfer to complete"
		sleep 1
	done
#echo "enough kb sent"
}

function log_copy_operation {
    copy_source="$1" #copy source
    copy_destination="$2" #copy destination
    file_size="$3" #file size
    log_file="$4" #log file
    check="$5" #check copy destination with the file of the name "file_sizeMB" in $SAMPLE_FILES_DIR
    ../scripts/start_net_mem_cpu_logging.sh "$PROCESS_NAME" "$TEMP_DIR" &
    time_before_operation=`date +"%s"`
    time_of_operation=`/usr/bin/time -f "%e" cp "$copy_source" "$copy_destination" 2>&1`
echo time_of_operation $time_of_operation
	sleep 1
    time_after_operation=`date +"%s"`
	wait_until_transfer_is_complete $file_size "$TEMP_DIR/netlog" $time_before_operation
    ../scripts/stop_net_mem_cpu_logging.sh

    success=yes
    if [ "$check" = "check" ];
    then
		error=`checksum "$copy_destination" "$SAMPLE_FILES_DIR/${file_size}MB"`
		if [ "$error" != "" ];
		then
		    success=no
		fi
    fi

    #"file_size time_for_operation_in_seconds average_cpu_load max_cpu_load average_mem_load max_mem_load max_swap_load average_net_load total_net_load success_in_yes_no"
    echo "$file_size, $time_of_operation, `get_db_line $time_before_operation $time_after_operation`, $success" >> "$log_file"
}


echo "starting tests"
$TEST_DIRECTORY/stop_service.sh
echo "service stopped"
$TEST_DIRECTORY/start_service.sh "$USERNAME" "$PASSWORD"
echo "service started"

sleep 5
if [ ! -d "$SAMPLE_FILES_DIR" ]; then
    ../scripts/create_files.sh "$SAMPLE_FILES_DIR" "$FILE_SIZES"
fi

echo "files created"

previous_size=""
for size in `echo $FILE_SIZES`
do
    echo "Testing file size ${size}MB"


    log_copy_operation "samplefiles/${size}MB" "$STORAGE_SERVICE_PATH/${size}MB" $size "$WRITE_TIME_LOG" "nocheck"
    log_copy_operation "$STORAGE_SERVICE_PATH/${size}MB" "$TEMP_DIR/${size}MB" $size "$READ_TIME_LOG" "check"

    rm "$TEMP_DIR/${size}MB"
    
    if [ "$previous_size" != "" ];
    then
		echo "handling previous file"
		
		log_copy_operation "$STORAGE_SERVICE_PATH/${previous_size}MB" "$TEMP_DIR/${previous_size}MB" $previous_size "$PREVIOUS_FILE_READ_TIME_LOG" "check"

        rm "$STORAGE_SERVICE_PATH/${previous_size}MB"

    fi 
    previous_size=$size
done   

if [ "$previous_size"!="" ];
then
	log_copy_operation "$STORAGE_SERVICE_PATH/${previous_size}MB" "$TEMP_DIR/${previous_size}MB" $previous_size "$PREVIOUS_FILE_READ_TIME_LOG" "check"
    rm "$STORAGE_SERVICE_PATH/${previous_size}MB"
fi 

$TEST_DIRECTORY/stop_service.sh
rm -r "$TEMP_DIR"


