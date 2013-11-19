#!/usr/bin/python
import sys, os
import argparse
from  datetime import datetime
from time import sleep
import shutil
from 
from sh import ErrorReturnCode
try:
    import sh
    from sh import dd
    from sh import git
except:
    print 'Please install the python sh module first : pip install sh'
    exit(1)

class MyParser(argparse.ArgumentParser):
    def error(self, message):
        print_help()
        sys.exit(1)

def print_help():
    print
    print "Not enough arguments.\n"
    print 'Example: %s ~/data . "1 10 100 1000 2000" "1000 100 10 1 1" ' % sys.args[0]
    print
    print 'Usage: %s storage_path log_directory test_file_sizes number_of_files [size_unit]' % sys.args[0]
    print "    storage_path:       a path to the directory to test"
    print "    log_directory:      a path to the directory to store log files"
    print "    test_file_sizes:    a list of numbers, which gives the size in MB of the test files, as well as their sequence "
    print "    number_of_files:    a list of numbers, which gives the number of files to generate and test for each corresponding file size in test_file_sizes"
    print '    size_unit:          the unit of the filesizes i.e. B, KB, MB, GB which are powers of 1000 Bytes (default is MB)'
    print
    print "Simple version of test_streaming_files.sh. It does not clear the cache of the tested file system before starting to read from it."
    print "It does not ever start or stop the storage service."
    print "It does not log memory, network, or cpu load."
    print "It does not wait until the network transfer is over, but assumes the operation is over as soon as the file operation is done."
    print "Create a directory log_directory/logs/streaming, where all logs are stored in a subdirectory with the name being the current date."
    print "Creates a file info in the log directory for various information on the service and system under test, "
    print "as well as a file diff to keep track of differences to the current revision."
    print "Generate files to the directory samplefiles. Copy these files from the smallest to the biggest to the storage service."
    print "Log the time for copying each group of files with the same size to write_time_log. Directly after copying all of such a group of files, log the time to read these files from the storage service to read_time_log." 
    print "If the checksum of the read file differs from the original, this is noted as an unsuccessful operation."
    print "Log the time for reading the file from the storage service, which was copied in the step before, to previous_file_read_time_log and then delete this file. "
    print "The logs' format is as follows: "
    print "single_file_size    total_size    time_for_operation_in_seconds average_transfer_rate nr_of_files success"
    print "    single_file_size  - the size in MB of the files used for the operation"
    print "    total_size  - the total size in MB of all files in the same group"
    print "    time_for_operation_in_seconds  - how long it took to perform the operation on a group of files in seconds"
    print "    average_transfer_rate  - the averagetransfer rate in MB/s"
    print "    nr_of_files  - the number of files for this operation"
    print "    success  - number of successful copy operations"
    print
    
def is_equal(file1, file2):
    import hashlib 
    with open(file1) as f:
        data = f.read()    
    file1_md5 = hashlib.md5(data).hexdigest()
    with open(file2) as f:
        data = f.read()    
    file2_md5 = hashlib.md5(data).hexdigest()
    return file1_md5 == file2_md5

def log_copy_operation(copy_source, copy_destination, file_size, nr_of_files, log_file, check=False):
    #check="$6" #check copy destination with the file of the name "file_sizeMB" in $SAMPLE_FILES_DIR
    now = datetime.now()
    time_of_multiple_operations = now - now
    success = 0

    time_before_operation = datetime.now() - datetime.timedelta(0)
    for nr in range(1, nr_of_files+1):  # from 1 to file quantity
        operation_succeeded=0
        while True:
            try:
                start = datetime.now()
                dd('if='+copy_source+str(nr), 'of='+copy_destination+str(nr), 'bs=131072')
                end = datetime.now()
            except ErrorReturnCode:
                break
            time_of_operation = end - start 
        time_of_multiple_operations += time_of_operation
        print "time gone until now: "+str(time_of_multiple_operations)
        success += 1
        if check:
            if is_equal(copy_destination+str(nr), sample_files_dir+'/'+str(file_size)+unit+'_'+str(nr)):
                success -= 1
    time_after_operations = time_before_operation + time_of_multiple_operations

    time_after_operations=`echo "scale = 10;($time_before_operation+$time_of_multiple_operations) / 1000000000" | bc` 

    print "time_before_operation %s - time_after_operation %s" % (time_before_operation, time_after_operation)

    average_transfer_rate = (1.0*file_size * nr_of_files)*(1/time_of_multiple_operations.total_seconds())"
    #"single_file_size    total_size    time_for_operation_in_seconds average_transfer_rate nr_of_files success"
    with open(log_file, 'w') as f:
        f.write(file_size+"\t"+file_size*nr_of_files+"\t"+time_of_multiple_operations.total_seconds()+"\t"+average_transfer_rate+"\t"+nr_of_files+"\t"+success)


def main():
    parser = MyParser()
    parser.add_argument('storage_path')
    parser.add_argument('log_directory')
    parser.add_argument('filesize')
    parser.add_argument('filequantity')
    parser.add_argument('args', nargs=argparse.REMAINDER) #collect all arguments positioned after positional and optional parameters 
    args = parser.parse_args()
    
    if len(args.args) != 1:
        unit = 'MB'
    else:
        unit = args.args[0]
    storage_path = args.storage_path
    log_directory = args.log_directory
    now = datetime.date.today().strftime('%Y.%m.%d_%H:%M')
    log_directory = log_directory+'/logs/streaming/'+now
    filesize_arr = args.filesize.split() #maps the list  ['1','2','3'] to [1,2,3]
    filequantity_arr = map(int, args.filequantity.split()) #maps the list  ['1','2','3'] to [1,2,3]
    sample_files_dir = 'samplefiles'
    temp_dir = '/tmp/'+now 
    write_time_log = log_directory + '/write_time_log'
    read_time_log = log_directory + '/read_time_log'
    previous_file_read_time_log = log_directory + '/previous_file_read_time_log'
    
    os.makedirs(log_directory)
    os.makedirs(temp_dir)
    #git.diff('') > "$LOG_DIR/diff"
    with open(log_directory+'/diff', 'w') as f:
        f.write(git.diff(''))
    shutil.copyfile('~/.nubisave/nubisavemount/config/config', log_directory)
    

    print "starting tests"

    if not os.path.exists(sample_files_dir):
        os.system(os.path.dirname(__file__)"/create_streaming_files.py %s '%s' '%s' %s"%(sample_files_dir, args.filesize, args.filequantity, unit))
    print "files created"


    idx = 0 
    for size in filesize_arr:
        for nr in range(1,filequantity_arr[idx]+1):  # from 1 to file quantity
            if os.path.exists('/tmp/stop'):
                print "waiting as long as /tmp/stop exists"
                while os.path.exists('/tmp/stop'):
                    sleep(1)
                print "continuing test"
            print "Test writing file size %s %s"%(size,unit)
            log_copy_operation('samplefiles/'+size+'MB_', temp_dir
            filename = os.getcwd()+'/'+directory+'/'+size+display_unit+'_'+str(nr)
            print "writing "+directory+'/'+size+display_unit+'_'+str(nr)
            dd('if=/dev/urandom', 'of='+filename, 'bs=1'+unit, 'count='+size)
        idx += 1
    previous_size=""
    """
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
"""

if __name__ == '__main__':
    main()

