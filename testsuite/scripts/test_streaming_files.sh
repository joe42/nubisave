#!/bin/bash
if [ $# -lt 5 ]; then
   echo
   echo "Not enough arguments."
   echo
   echo
   echo "Create a directory test_directory/logs/streaming, where all logs are stored in a subdirectory with the name being the current date."
   echo "Creates a file info in the log directory for various information on the service and system under test, "
   echo "as well as a file diff to keep track of differences to the current revision."
   echo "Stop the storage service and start the storage service. "
   echo "Generate files to the directory samplefiles. Copy these files from the smallest to the biggest to the storage service."
   echo "Log the time for copying each group of files with the same size to write_time_log. Directly after copying all of such a group of files, log the time to read these files from the storage service to read_time_log." 
   echo "If the checksum of the read file differs from the original, this is noted as an unsuccessful operation."
   echo "After clearing the cache, log the time for reading the file from the storage service, which was copied in the step before, to previous_file_read_time_log and then delete this file. "
   echo "Stop the storage service."
   echo "The logs' format is as follows: "
   echo "single_file_size	total_size	time_for_operation_in_seconds	time_for_complete_operation_in_seconds	average_cpu_load	max_cpu_load	average_mem_load	max_mem_load	max_swap_load	average_net_load	total_net_load	success"
   echo "    single_file_size  - the size in MB of the files used for the operation"
   echo "    total_size  - the total size in MB of all files in the same group"
   echo "    time_for_operation_in_seconds  - how long it took to perform the operation on a group of files in seconds"
   echo "    average_cpu_load  - the average CPU load of the process measured during the operation"
   echo "    max_cpu_load  - the maximum CPU load of the process measured during the operation"
   echo "    average_mem_load  - the average proportional set size of memory allocated by the process during the operation"
   echo "    max_mem_load  - the maximum proportional set size of memory allocated by the process during the operation"
   echo "    max_swap_load  - the maximum swap allocated by the process during the operation"
   echo "    average_net_load  - the average network load during the operation"
   echo "    total_net_load  - the total network load during the operation"
   echo "    success  - number of successful copy operations"

   echo
   echo
   echo Usage: `basename $0` storage_path process_name test_directory test_file_sizes number_of_files username password [ stop_network_monitoring_after_file_operation ]
   echo "    storage_path  - a path to the directory to test"
   echo "    process_name  - the process name of the storage service, so that it can be found by pgrep"
   echo "    test_directory  - a path to the directory with start_service.sh and stop_service.sh, to start and stop the service, and info.sh to get information about the test (can be empty scripts)"
   echo "    test_file_sizes  - a list of numbers, which gives the size in MB of the test files, as well as their sequence "
   echo "    number_of_files  - a list of numbers, which gives the number of files to generate and test for each corresponding file size in test_file_sizes"
   echo "    username  - the username, which is given to the start_service.sh script as the first parameter (can be an arbitrary string if the service does not need authentication)"
   echo "    password  - the password, which is given to the start_service.sh script as the second parameter (can be an arbitrary string if the service does not need authentication)"
   echo "    stop_network_monitoring_after_file_operation  - Optional parameter. Can be one of yes or no. The default is no. Some file systems delay transfering files over the network, nevertheless claiming the file operation to be complete. Thus to get representational network statistics in this case, the network must be monitored even after the end of an file operation. Set this parameter to no, if the file system stops network transfers with the end of a file system operation. This may lead to more accureate network statistics."
   echo
   echo Example: `basename $0` ~/data Wuala . "1 10 100 1000 2000" "1000 100 10 1 1" joe 123456
   echo
   exit
fi  

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROCESS_NAME="$2"
TEST_DIRECTORY="$3"
FILE_SIZES="$4"
FILE_QUANTITYS="$5"
FILE_SIZES_ARR=(`echo "$4"`)
FILE_QUANTITYS_ARR=(`echo "$5"`)
LOG_DIR="$TEST_DIRECTORY/logs/streaming/`date`"
SAMPLE_FILES_DIR=samplefiles
TEMP_DIR=/tmp/storage_service`date +"%s"`
WRITE_TIME_LOG="$LOG_DIR/write_time_log"
READ_TIME_LOG="$LOG_DIR/read_time_log"
PREVIOUS_FILE_READ_TIME_LOG="$LOG_DIR/previous_file_read_time_log"
STORAGE_SERVICE_PATH="$1"
USERNAME="$6"
PASSWORD="$7"
STOP_NETWORK_MONITORING_AFTER_FILE_OPERATION="$8"
mkdir -p "$LOG_DIR" "$TEMP_DIR"

$TEST_DIRECTORY/info.sh > "$LOG_DIR/info"
git diff > "$LOG_DIR/diff"


source "$DIR"/functions.sh

function log_copy_operation {
    copy_source="$1" #copy source
    copy_destination="$2" #copy destination
    file_size="$3" #file size
	nr_of_files="$4"
    log_file="$5" #log file
    check="$6" #check copy destination with the file of the name "file_sizeMB" in $SAMPLE_FILES_DIR
    ../scripts/start_net_mem_cpu_logging.sh "$PROCESS_NAME" "$TEMP_DIR" &
	sleep 2 # wait until logging produces some results to process
    time_of_multiple_operations=0
	success=0
    time_before_operation=`date +"%s"`
	for nr in `seq 1 $nr_of_files`  # from 1 to file quantity
	do
		operation_succeeded=0
		until [ $operation_succeeded -eq 1 ] ; do
			time_of_operation=`/usr/bin/time -f "%e" cp "$copy_source"$nr "$copy_destination"$nr 2>&1`
			if [ "`echo $time_of_operation|grep 'Command exited with non-zero status'`" == "" ] ; then
				operation_succeeded=1
			fi
		done
		time_of_multiple_operations=`echo $time_of_multiple_operations+$time_of_operation | bc`
echo "Accumulation of previous operations' durations:" $time_of_multiple_operations
echo "Current operation's duration" $time_of_operation
		success=$(($success+1))
		if [ "$check" != "" ];
		then
			error=`checksum "$copy_destination"$nr "$SAMPLE_FILES_DIR/${file_size}MB_"$nr`
			if [ "$error" != "" ];
			then
				success=$(($success-1))
			fi
    		fi
	done

    time_after_operations=`echo $time_before_operation+$time_of_multiple_operations | bc` 
echo time_of_multiple_operations $time_of_multiple_operations
	if [ "$STOP_NETWORK_MONITORING_AFTER_FILE_OPERATION" != "yes" ];
	then
		time_of_network_stagnation=`wait_until_transfer_is_complete $(($file_size*$nr_of_files)) "$TEMP_DIR/netlog" $time_before_operation`
		time_after_network_transfer=$((`date +"%s"`-$time_of_network_stagnation)) #subtract the time waited for the network activity to stagnate
	else
		time_after_network_transfer=$time_after_operations
	fi
	sleep 2 # wait until logging produces some results to process
    ../scripts/stop_net_mem_cpu_logging.sh
	time_of_complete_operation=`echo $time_after_network_transfer-$time_before_operation | bc` 
echo time_before_operation $time_before_operation time_after_operation $time_after_operations time_after_network_transfer $time_after_network_transfer 

    #"file_size time_for_operation_in_seconds average_cpu_load average_cpu_load, max_cpu_load, average_mem_load, max_mem_load, max_swap_load, average_net_load, total_net_load success_in_num_of_successes"
    echo "$file_size	$(($file_size*$nr_of_files))	$time_of_complete_operation	`get_db_line $time_before_operation $time_after_operations $time_after_network_transfer`	$success" >> "$log_file"

}


echo "starting tests"
$TEST_DIRECTORY/stop_service.sh
echo "service stopped"
$TEST_DIRECTORY/start_service.sh "$USERNAME" "$PASSWORD"
echo "service started"

sleep 5
if [ ! -d "$SAMPLE_FILES_DIR" ]; then
    ../scripts/create_streaming_files.sh "$SAMPLE_FILES_DIR" "$FILE_SIZES" "$FILE_QUANTITYS"
fi

echo "files created"

previous_size=""
for i in ${!FILE_SIZES_ARR[*]}  # iterate over all indexes in array
do
	echo "Test writing file size ${FILE_SIZES_ARR[$i]}MB"
	log_copy_operation "samplefiles/${FILE_SIZES_ARR[$i]}MB_" "$STORAGE_SERVICE_PATH/${FILE_SIZES_ARR[$i]}MB_" "${FILE_SIZES_ARR[$i]}" "${FILE_QUANTITYS_ARR[$i]}" "$WRITE_TIME_LOG"

	echo "Test reading file size ${FILE_SIZES_ARR[$i]}MB"
	log_copy_operation "$STORAGE_SERVICE_PATH/${FILE_SIZES_ARR[$i]}MB_" "$TEMP_DIR/${FILE_SIZES_ARR[$i]}MB_" "${FILE_SIZES_ARR[$i]}" "${FILE_QUANTITYS_ARR[$i]}" "$READ_TIME_LOG" "check"

	for nr in `seq 1 ${FILE_QUANTITYS_ARR[$i]}`  # from 1 to file quantity
	do
		rm "$STORAGE_SERVICE_PATH/${FILE_SIZES_ARR[$i]}MB_$nr"
	done

done   

$TEST_DIRECTORY/stop_service.sh
rm -r "$TEMP_DIR"


