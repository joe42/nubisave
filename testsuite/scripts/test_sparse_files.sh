#!/bin/bash
if [ $# -lt 5 ]; then
   echo
   echo "Not enough arguments."
   echo
   echo
   echo "Create a directory test_directory/logs/sparse, where all logs are stored in a subdirectory with the name being the current date."
   echo "Creates a file info in the log directory for various information on the service and system under test, "
   echo "as well as a file diff to keep track of differences to the current revision."
   echo "Stop the storage service and start the storage service. "
   echo "Generate files to the directory samplefiles. Write one megabyte of data to the sparse file. Copy the sparse file to the storage service."
   echo "Log the time for the copy operation to sparse_time_log. Log the time for reading the sparse file to sparse_time_log and check if it was read correctly." 
   echo "If the checksum of the read file differs from the original, this is noted as an unsuccessful operation."
   echo "Log the time for writing one megabyte of data to the sparse file on the storage service. "
   echo "Log the time for reading one megabyte of data from the sparse file on the storage service. "
   echo "Log the time for appending 15 characters to the sparse file on the storage service. "
   echo "Stop the storage service."
   echo "The log's format is as follows: "
   echo "operation, size, time_for_operation_in_seconds, average_cpu_load, max_cpu_load, average_mem_load, max_mem_load, max_swap_load, average_net_load, total_net_load, success"
   echo "    size  - the size in MB read or written during the operation"
   echo "    time_for_operation_in_seconds  - how long it took to perform the operation in seconds"
   echo "    average_cpu_load  - the average CPU load of the process measured during the operation"
   echo "    max_cpu_load  - the maximum CPU load of the process measured during the operation"
   echo "    average_mem_load  - the average proportional set size of memory allocated by the process during the operation"
   echo "    max_mem_load  - the maximum proportional set size of memory allocated by the process during the operation"
   echo "    max_swap_load  - the maximum swap allocated by the process during the operation"
   echo "    average_net_load  - the average network load during the operation"
   echo "    total_net_load  - the total network load during the operation"
   echo
   echo
   echo Usage: `basename $0` storage_path process_name test_directory username password [ stop_network_monitoring_after_file_operation ]
   echo "    storage_path  - a path to the directory to test"
   echo "    process_name  - the process name of the storage service, so that it can be found by pgrep"
   echo "    test_directory  - a path to the directory with start_service.sh and stop_service.sh, to start and stop the service, and info.sh to get information about the test (can be empty scripts)"
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
LOG_DIR="$TEST_DIRECTORY/logs/sparse/`date`"
SAMPLE_FILES_DIR=samplefiles
TEMP_DIR="/tmp/storage_service`date +"%s"`"
TIME_LOG_SPARSE_1GB="$LOG_DIR/sparse_time_log_1gb"
TIME_LOG_SPARSE_2GB="$LOG_DIR/sparse_time_log_2gb"
STORAGE_SERVICE_PATH="$1"
USERNAME="$4"
PASSWORD="$5"
STOP_NETWORK_MONITORING_AFTER_FILE_OPERATION="$6"
mkdir -p "$LOG_DIR" "$TEMP_DIR"
ONE_MB=$((1024*1024))
TEN_MB=$(($ONE_MB*10))

$TEST_DIRECTORY/info.sh > "$LOG_DIR/info"
git diff > "$LOG_DIR/diff"

source "$DIR"/functions.sh

function log_operation {
    operation="$1" #operation returning the time for the operation
    operation_description="$2" #description of the operation
    file_size="$3" #a file size in MB related to the operation
	log_file="$4"
    check="$5" #optional operation to check the success of the operation
    ../scripts/start_net_mem_cpu_logging.sh "$PROCESS_NAME" "$TEMP_DIR" &
	sleep 2 # wait until logging produces some results to process
    time_before_operation=`date +"%s"`
    time_of_operation=`eval $operation`
echo time_of_operation $time_of_operation
    time_after_operation=`date +"%s"`	
	if [ "$STOP_NETWORK_MONITORING_AFTER_FILE_OPERATION" != "yes" ];
	then
		time_of_network_stagnation=`wait_until_transfer_is_complete $file_size "$TEMP_DIR/netlog" $time_before_operation`
		time_after_network_transfer=$((`date +"%s"`-$time_of_network_stagnation)) #subtract the time waited for the network activity to stagnate
	else
		time_after_network_transfer=$time_after_operation
	fi
	sleep 2 # wait until logging produces some results to process
    ../scripts/stop_net_mem_cpu_logging.sh

    success=yes
    if [ "$check" != "" ];
    then
		error=`$check`
		if [ "$error" != "" ];
		then
		    success=no
		fi
    fi

    #"file_size time_for_operation_in_seconds average_cpu_load max_cpu_load average_mem_load max_mem_load max_swap_load average_net_load total_net_load success_in_yes_no"
    echo "$operation_description, $file_size, $time_of_operation, `get_db_line $time_before_operation $time_after_operation $time_after_network_transfer`, $success" >> "$log_file"
}


echo "starting tests"
$TEST_DIRECTORY/stop_service.sh
echo "service stopped"
$TEST_DIRECTORY/start_service.sh "$USERNAME" "$PASSWORD"
echo "service started"

sleep 5
if [ ! -d "$SAMPLE_FILES_DIR" ]; then
    ../scripts/create_files.sh "$SAMPLE_FILES_DIR"
fi

################	Test no. 1.1: copy sparse file with size 1000MB to storage service	################
operation='/usr/bin/time --quiet -f "%e" cp --sparse=always samplefiles/sparse "'$STORAGE_SERVICE_PATH'"/ 2>&1 |tail -n1'
operation_description='write'
file_size='1000'
log_file="$TIME_LOG_SPARSE_1GB"
check=""
log_operation "$operation" "$operation_description" "$file_size" "$log_file" "$check"

################	Test no. 1.2: copy sparse file with size 2000MB to storage service	################
operation='/usr/bin/time --quiet -f "%e" cp --sparse=always samplefiles/sparse2 "'$STORAGE_SERVICE_PATH'"/ 2>&1 |tail -n1'
operation_description='write'
file_size='2000'
log_file="$TIME_LOG_SPARSE_2GB"
check=""
log_operation "$operation" "$operation_description" "$file_size" "$log_file" "$check"


################	Test no. 2.1: read sparse file with size 1000MB from storage service	################
operation='/usr/bin/time --quiet -f "%e" cp --sparse=always "'$STORAGE_SERVICE_PATH/sparse'" "'$TEMP_DIR'/sparse" 2>&1 |tail -n1'
operation_description='read'
file_size='1000'
log_file="$TIME_LOG_SPARSE_1GB"
check='checksum '$TEMP_DIR'/sparse samplefiles/sparse'
log_operation "$operation" "$operation_description" "$file_size" "$log_file" "$check"

################	Test no. 2.2: read sparse file with size 2000MB from storage service	################
operation='/usr/bin/time --quiet -f "%e" cp --sparse=always "'$STORAGE_SERVICE_PATH/sparse2'" "'$TEMP_DIR'/sparse2" 2>&1 |tail -n1'
operation_description='read'
file_size='2000'
log_file="$TIME_LOG_SPARSE_2GB"
check='checksum '$TEMP_DIR'/sparse2 samplefiles/sparse2'
log_operation "$operation" "$operation_description" "$file_size" "$log_file" "$check"

################	Test no. 3.1: write to sparse file with size 1000MB	################
operation='/usr/bin/time --quiet -f "%e" python ../scripts/write.py "'$STORAGE_SERVICE_PATH'/sparse" '$TEN_MB' '$ONE_MB' 2>&1 |tail -n1'
operation_description='write'
file_size='1'
log_file="$TIME_LOG_SPARSE_1GB"
check=''
log_operation "$operation" "$operation_description" "$file_size" "$log_file" "$check"

################	Test no. 3.2: write to sparse file with size 2000MB	################
operation='/usr/bin/time --quiet -f "%e" python ../scripts/write.py "'$STORAGE_SERVICE_PATH'/sparse2" '$TEN_MB' '$ONE_MB' 2>&1 |tail -n1'
operation_description='write'
file_size='1'
log_file="$TIME_LOG_SPARSE_2GB"
check=''
log_operation "$operation" "$operation_description" "$file_size" "$log_file" "$check"

################	Test no. 4.1: read from sparse file with size 1000MB	################
operation='/usr/bin/time --quiet -f "%e" python ../scripts/read.py "'$STORAGE_SERVICE_PATH'/sparse" '$TEN_MB' '$ONE_MB' 2>&1 |tail -n1'
operation_description='read'
file_size='1'
log_file="$TIME_LOG_SPARSE_1GB"
check=''
log_operation "$operation" "$operation_description" "$file_size" "$log_file" "$check"

################	Test no. 4.2: read from sparse file with size 2000MB	################
operation='/usr/bin/time --quiet -f "%e" python ../scripts/read.py "'$STORAGE_SERVICE_PATH'/sparse2" '$TEN_MB' '$ONE_MB' 2>&1 |tail -n1'
operation_description='read'
file_size='1'
log_file="$TIME_LOG_SPARSE_2GB"
check=''
log_operation "$operation" "$operation_description" "$file_size" "$log_file" "$check"


################	Test no. 5.1: append to sparse file with size 1000MB	################
operation='/usr/bin/time --output "'$TEMP_DIR'/append" --quiet -f "%e" echo "this is the end" >> "'$STORAGE_SERVICE_PATH'/sparse" ; tail -n1 "'$TEMP_DIR'/append"'
operation_description='append 15 characters'
file_size='0'
log_file="$TIME_LOG_SPARSE_1GB"
check=''
log_operation "$operation" "$operation_description" "$file_size" "$log_file" "$check"

################	Test no. 5.2: append to sparse file with size 2000MB	################
operation='/usr/bin/time --output "'$TEMP_DIR'/append" --quiet -f "%e" echo "this is the end" >> "'$STORAGE_SERVICE_PATH'/sparse2" ; tail -n1 "'$TEMP_DIR'/append"'
operation_description='append 15 characters'
file_size='0'
log_file="$TIME_LOG_SPARSE_2GB"
check=''
log_operation "$operation" "$operation_description" "$file_size" "$log_file" "$check"

rm "$TEMP_DIR/sparse"
rm "$STORAGE_SERVICE_PATH/sparse"
rm "$TEMP_DIR/sparse2"
rm "$STORAGE_SERVICE_PATH/sparse2"

$TEST_DIRECTORY/stop_service.sh
