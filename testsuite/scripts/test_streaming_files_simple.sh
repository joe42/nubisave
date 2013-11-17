#!/bin/bash
if [ $# -lt 4 ]; then
   echo
   echo "Not enough arguments."
   echo
   echo "Simple version of test_streaming_files.sh. It does not clear the cache of the testet file system before starting to read from it."
   echo "It does not ever start or stop the storage service."
   echo "It does not log memory, network, or cpu load."
   echo "It does not wait until the network transfer is over, but assumes the operation is over as soon as the file operation is done."
   echo "Create a directory test_directory/logs/streaming, where all logs are stored in a subdirectory with the name being the current date."
   echo "Creates a file info in the log directory for various information on the service and system under test, "
   echo "as well as a file diff to keep track of differences to the current revision."
   echo "Generate files to the directory samplefiles. Copy these files from the smallest to the biggest to the storage service."
   echo "Log the time for copying each group of files with the same size to write_time_log. Directly after copying all of such a group of files, log the time to read these files from the storage service to read_time_log." 
   echo "If the checksum of the read file differs from the original, this is noted as an unsuccessful operation."
   echo "Log the time for reading the file from the storage service, which was copied in the step before, to previous_file_read_time_log and then delete this file. "
   echo "The logs' format is as follows: "
   echo "single_file_size	total_size	time_for_operation_in_seconds average_transfer_rate nr_of_files success"
   echo "    single_file_size  - the size in MB of the files used for the operation"
   echo "    total_size  - the total size in MB of all files in the same group"
   echo "    time_for_operation_in_seconds  - how long it took to perform the operation on a group of files in seconds"
   echo "    average_transfer_rate  - the averagetransfer rate in MB/s"
   echo "    nr_of_files  - the number of files for this operation"
   echo "    success  - number of successful copy operations"

   echo
   echo
   echo Usage: `basename $0` storage_path test_directory test_file_sizes number_of_files
   echo "    storage_path  - a path to the directory to test"
   echo "    test_file_sizes  - a list of numbers, which gives the size in MB of the test files, as well as their sequence "
   echo "    number_of_files  - a list of numbers, which gives the number of files to generate and test for each corresponding file size in test_file_sizes"
   echo
   echo Example: `basename $0` ~/data . \"1 10 100 1000 2000\" \"1000 100 10 1 1\" 
   echo
   exit
fi  

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
TEST_DIRECTORY="$2"
FILE_SIZES="$3"
FILE_QUANTITYS="$4"
FILE_SIZES_ARR=(`echo "$3"`)
FILE_QUANTITYS_ARR=(`echo "$4"`)
LOG_DIR="$TEST_DIRECTORY/logs/streaming/`date +%Y.%m.%d_%H:%M`"
SAMPLE_FILES_DIR=samplefiles
TEMP_DIR=/tmp/storage_service`date +"%s"`
WRITE_TIME_LOG="$LOG_DIR/write_time_log"
READ_TIME_LOG="$LOG_DIR/read_time_log"
PREVIOUS_FILE_READ_TIME_LOG="$LOG_DIR/previous_file_read_time_log"
STORAGE_SERVICE_PATH="$1"
mkdir -p "$LOG_DIR" "$TEMP_DIR"

$TEST_DIRECTORY/info.sh > "$LOG_DIR/info"
git diff > "$LOG_DIR/diff"
cat ~/.nubisave/nubisavemount/config/config > "$LOG_DIR/config"


source "$DIR"/functions.sh



function log_copy_operation {
    copy_source="$1" #copy source
    copy_destination="$2" #copy destination
    file_size="$3" #file size
	nr_of_files="$4"
    log_file="$5" #log file
    check="$6" #check copy destination with the file of the name "file_sizeMB" in $SAMPLE_FILES_DIR
    time_of_multiple_operations=0
	success=0

    time_before_operation=`date +%s%N` #in nanoseconds
	for nr in `seq 1 $nr_of_files`  # from 1 to file quantity
	do
		operation_succeeded=0
		until [ $operation_succeeded -eq 1 ] ; do
            start=`date +%s%N`
			dd if="$copy_source"$nr of="$copy_destination"$nr bs=131072 &>/dev/null
            success_if_zero=$?
            end=`date +%s%N`
            time_of_operation=$(($end-$start))
            if [ $success_if_zero -eq 0 ] ; then  
				operation_succeeded=1
			fi
		done
		time_of_multiple_operations=`echo $time_of_multiple_operations+$time_of_operation | bc`
echo "Accumulation of previous operations' durations:" $(($time_of_multiple_operations/1000000000)) s 
echo "Current operation's duration" `echo "scale = 10;$time_of_operation / 1000000000" | bc`  s
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

    time_after_operations=`echo "scale = 10;($time_before_operation+$time_of_multiple_operations) / 1000000000" | bc` 

echo time_before_operation `echo "scale = 10;($time_before_operation) / 1000000000" | bc`  time_after_operation $time_after_operations

    average_transfer_rate=`echo "scale=10;$(($file_size*$nr_of_files))/($time_of_multiple_operations/1000000000)" | bc`
    #"single_file_size	total_size	time_for_operation_in_seconds average_transfer_rate nr_of_files success"
    echo "$file_size	$(($file_size*$nr_of_files))    $time_of_multiple_operations    $average_transfer_rate   $nr_of_files  $success" >> "$log_file"

}


echo "starting tests"

if [ ! -d "$SAMPLE_FILES_DIR" ]; then
    ../scripts/create_streaming_files.sh "$SAMPLE_FILES_DIR" "$FILE_SIZES" "$FILE_QUANTITYS"
fi

echo "files created"

previous_size=""
for i in ${!FILE_SIZES_ARR[*]}  # iterate over all indexes in array
do
    echo "waiting as long as /tmp/stop exists"
    while [ -e "/tmp/stop" ]; do
        sleep 1
    done
	echo "Test writing file size ${FILE_SIZES_ARR[$i]}MB"
	log_copy_operation "samplefiles/${FILE_SIZES_ARR[$i]}MB_" "$STORAGE_SERVICE_PATH/${FILE_SIZES_ARR[$i]}MB_" "${FILE_SIZES_ARR[$i]}" "${FILE_QUANTITYS_ARR[$i]}" "$WRITE_TIME_LOG"

	echo "Test reading file size ${FILE_SIZES_ARR[$i]}MB"
	log_copy_operation "$STORAGE_SERVICE_PATH/${FILE_SIZES_ARR[$i]}MB_" "$TEMP_DIR/${FILE_SIZES_ARR[$i]}MB_" "${FILE_SIZES_ARR[$i]}" "${FILE_QUANTITYS_ARR[$i]}" "$READ_TIME_LOG" "check"

	for nr in `seq 1 ${FILE_QUANTITYS_ARR[$i]}`  # from 1 to file quantity
	do
		rm "$STORAGE_SERVICE_PATH/${FILE_SIZES_ARR[$i]}MB_$nr"
		rm "$TEMP_DIR/${FILE_SIZES_ARR[$i]}MB_$nr"
	done

done   

rm -r "$TEMP_DIR"


