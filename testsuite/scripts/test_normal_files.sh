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
   echo "file_size	time_for_operation_in_seconds	time_for_complete_operation_in_seconds	average_cpu_load	max_cpu_load	average_mem_load	max_mem_load	max_swap_load	average_net_load	total_net_load	success"
   echo "    file_size  - the size in MB of the file used for the operation"
   echo "    time_for_operation_in_seconds  - how long it took to perform the operation in seconds"
   echo "    time_for_complete_operation_in_seconds  - how long it took to perform the operation in seconds including the time for network transfer"
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
   echo Usage: `basename $0` storage_path process_name test_directory test_file_sizes username password [ stop_network_monitoring_after_file_operation ]
   echo "    storage_path  - a path to the directory to test"
   echo "    process_name  - the process name of the storage service, so that it can be found by pgrep"
   echo "    test_directory  - a path to the directory with start_service.sh and stop_service.sh, to start and stop the service, and info.sh to get information about the test (can be empty scripts)"
   echo "    test_file_sizes  - a list of numbers, which gives the size in MB of the test files, as well as their sequence "
   echo "    username  - the username, which is given to the start_service.sh script as the first parameter (can be an arbitrary string if the service does not need authentication)"
   echo "    password  - the password, which is given to the start_service.sh script as the second parameter (can be an arbitrary string if the service does not need authentication)"
   echo "    stop_network_monitoring_after_file_operation  - Optional parameter. Can be one of yes or no. The default is no. Some file systems delay transfering files over the network, nevertheless claiming the file operation to be complete. Thus to get representational network statistics in this case, the network must be monitored even after the end of an file operation. Set this parameter to no, if the file system stops network transfers with the end of a file system operation. This may lead to more accureate network statistics."
   echo
   echo Example: `basename $0` ~/data Wuala . "1 10 100 1000 2000" joe 123456
   echo
   exit
fi  

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROCESS_NAME="$2"
TEST_DIRECTORY="$3"
FILE_SIZES="$4"
LOG_DIR="$TEST_DIRECTORY/logs/normal/`date +%Y.%m.%d_%H:%M`"
SAMPLE_FILES_DIR=samplefiles
TEMP_DIR=/tmp/storage_service`date +"%s"`
WRITE_TIME_LOG="$LOG_DIR/write_time_log"
READ_TIME_LOG="$LOG_DIR/read_time_log"
PREVIOUS_FILE_READ_TIME_LOG="$LOG_DIR/previous_file_read_time_log"
STORAGE_SERVICE_PATH="$1"
USERNAME="$5"
PASSWORD="$6"
STOP_NETWORK_MONITORING_AFTER_FILE_OPERATION="$7"
mkdir -p "$LOG_DIR" "$TEMP_DIR"

$TEST_DIRECTORY/info.sh > "$LOG_DIR/info"
git diff > "$LOG_DIR/diff"


source "$DIR"/functions.sh

function log_copy_operation {
    copy_source="$1" #copy source
    copy_destination="$2" #copy destination
    file_size="$3" #file size
    log_file="$4" #log file
    check="$5" #check copy destination with the file of the name "file_sizeMB" in $SAMPLE_FILES_DIR
    ../scripts/start_net_mem_cpu_logging.sh "$PROCESS_NAME" "$TEMP_DIR" &
	sleep 2 # wait until logging produces some results to process
    time_before_operation=`date +"%s"`
    time_of_operation=`/usr/bin/time -f "%e" cp "$copy_source" "$copy_destination" 2>&1`
    time_after_operation=$(($time_before_operation+$time_of_operation))	
echo time_of_operation $time_of_operation
	if [ "$STOP_NETWORK_MONITORING_AFTER_FILE_OPERATION" != "yes" ];
	then
		time_of_network_stagnation=`wait_until_transfer_is_complete $file_size "$TEMP_DIR/netlog" $time_before_operation`
		time_after_network_transfer=$((`date +"%s"`-$time_of_network_stagnation)) #subtract the time waited for the network activity to stagnate
	else
		time_after_network_transfer=$time_after_operation
	fi
	sleep 2 # wait until logging produces some results to process
    ../scripts/stop_net_mem_cpu_logging.sh
	time_of_complete_operation=$(($time_after_network_transfer-$time_before_operation))	 
    success=yes
    if [ "$check" != "" ];
    then
		error=`checksum "$copy_destination" "$SAMPLE_FILES_DIR/${file_size}MB"`
		if [ "$error" != "" ];
		then
		    success=no
		fi
    fi

    #"file_size time_for_operation_in_seconds average_cpu_load max_cpu_load average_mem_load max_mem_load max_swap_load average_net_load total_net_load success_in_yes_no"
    echo "$file_size	$time_of_operation	$time_of_complete_operation	`get_db_line $time_before_operation $time_after_operation $time_after_network_transfer`	$success" >> "$log_file"
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


    log_copy_operation "samplefiles/${size}MB" "$STORAGE_SERVICE_PATH/${size}MB" $size "$WRITE_TIME_LOG"
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


