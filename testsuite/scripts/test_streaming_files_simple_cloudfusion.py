#!/usr/bin/python
import sys, os
import argparse
import datetime
from time import sleep
import shutil
from sh import ErrorReturnCode
try:
    from sh import dd
    from sh import git
except:
    import traceback
    traceback.print_exc()
    print 'Please install the python sh module first : pip install sh'
    exit(1)
    
# CONSTANTS
HOME = os.environ['HOME']
MOUNT_DIRECTORY = None
CLOUDFUSION_NOT_UPLOADED_PATH = None  
CLOUDFUSION_STATS_DIR = None  
CLOUDFUSION_CONFIG_PATH = None  
DATA_DIRECTORY = None
TEST_DIRECTORY = None
SAMPLE_FILES_DIR = '/home/joe/shared/samplefiles'
now = datetime.datetime.today().strftime('%Y.%m.%d_%H.%M')
TEMP_DIR = '/home/joe/shared/tmp'+now 
CONFIGFILE = HOME+'/Sugarsync.ini'
    
class MyParser(argparse.ArgumentParser):
    def error(self, message):
        print_help()
        sys.exit(1)

def print_help():
    print
    print "Not enough arguments.\n"
    print 'Example: %s ~/mnt . "1 10 100 1000 2000" 4000 ' % sys.argv[0]
    print
    print 'Usage: %s mount_path log_directory test_file_sizes data_per_file_size [size_unit]' % sys.argv[0]
    print "    mount_path:         mount directory of Cloudfusion"
    print "    log_directory:      a path to the directory to store log files"
    print "    test_file_sizes:    a list of numbers, which gives the size in MB of the test files, as well as their sequence "
    print "    data_per_file_size: determines how many files of each file size are written - i.e. choosing a value of 4000 could mean 4000 1MB files, 400 10 MB files, 2 2000MB, and 1 3000MB file. This should be bigger than the largest filesize."
    print '    size_unit:          the unit of the filesizes i.e. B, KB, MB, GB which are powers of 1000 Bytes (default is MB)'
    print
    print "Simple version of test_streaming_files.sh. It does not clear the cache of the tested file system before starting to read from it."
    print "It does not ever start or stop the storage service."
    print "It does not log memory, network, or cpu load."
    print "It does not wait until the network transfer is over, but assumes the operation is over as soon as the file operation is done."
    print "Create a directory log_directory/logs/streaming, where all logs are stored in a subdirectory with the name being the current date."
    print "Creates a file diff in the log directory to keep track of differences to the current revision."
    print "Generate a number of files for each file size, so that all files of one size are about the size of specified through the data_per_file_size parameter."
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

def wait_for_completed_upload():
    print "waiting for completed upload"
    while os.path.getsize(CLOUDFUSION_NOT_UPLOADED_PATH) > 0:
        sleep(10)
        
def restart_cloudfusion():
    os.system("pkill -9 -f  \".*cloudfusion .*\"")
    os.system("fusermount -zu "+MOUNT_DIRECTORY)
    os.system("rm /tmp/cloudfusion/cachingstore* -r")
    os.system("rm .cloudfusion/logs/*;")
    while CLOUDFUSION_CONFIG_PATH and os.path.exists(CLOUDFUSION_CONFIG_PATH):
        sleep(10)
        print "still mounted!"
    print "starting file system"    
    os.system("/usr/local/bin/cloudfusion --config "+CONFIGFILE+" "+MOUNT_DIRECTORY+" foreground log &")
    sleep(10)
    
def copy_stats(log_directory):
    os.system("cp -r "+CLOUDFUSION_STATS_DIR+" "+log_directory)
    os.system("cp -r .cloudfusion/logs "+log_directory)
    
def log_copy_operation(copy_source, copy_destination, file_size, nr_of_files, log_file, sample_files_dir, unit, check=False):
    #check="$6" #check copy destination with the file of the name "file_sizeMB" in $SAMPLE_FILES_DIR
    success = 0

    time_before_operation = datetime.datetime.now() - datetime.timedelta(0)
    for nr in range(1, nr_of_files+1):  # from 1 to file quantity
        operation_succeeded=0
        while True:
            try:
                dd('if='+copy_source+str(nr), 'of='+copy_destination+str(nr), 'bs=131072')
            except ErrorReturnCode:
                import traceback
                sys.stderr.write("Error occured during copying - retrying:")
                traceback.print_exc()
                continue
            break # stop loop if command succeeded
        success += 1
    wait_for_completed_upload()
    time_after_operations = datetime.datetime.now() - datetime.timedelta(0)
    time_of_multiple_operations = time_after_operations - time_before_operation
    print "time_before_operation %s - time_after_operation %s" % (time_before_operation, time_after_operations)
    for nr in range(1, nr_of_files+1):  # from 1 to file quantity
        if check:
            if is_equal(copy_destination+str(nr), sample_files_dir+'/'+str(file_size)+unit+'_'+str(nr)):
                success -= 1
    average_transfer_rate = (1.0*int(file_size) * nr_of_files)*(1/time_of_multiple_operations.total_seconds())
    #"single_file_size    total_size    time_for_operation_in_seconds average_transfer_rate nr_of_files success"
    if not os.path.exists(log_file):
        with open(log_file, 'w') as f:
            f.write("single file size\ttotal size\ttime for operation [s]\taverage transfer rate [%s/s]\tnumber of files\tsuccess\n" % unit)
    with open(log_file, 'a') as f:
        total_size = int(file_size)*nr_of_files
        f.write("%s\t%s\t%s\t%s\t%s\t%s\n" % (file_size, total_size, time_of_multiple_operations.total_seconds(), average_transfer_rate, nr_of_files, success))
    copy_stats(os.path.dirname(log_file)+"stats_"+str(file_size))


def main():
    global CLOUDFUSION_NOT_UPLOADED_PATH
    global MOUNT_DIRECTORY
    global DATA_DIRECTORY
    global CLOUDFUSION_STATS_DIR
    global CLOUDFUSION_CONFIG_PATH
    global TEST_DIRECTORY
    parser = MyParser()
    parser.add_argument('mount_path')
    parser.add_argument('log_directory')
    parser.add_argument('filesize')
    parser.add_argument('filegroup_size')
    parser.add_argument('args', nargs=argparse.REMAINDER) #collect all arguments positioned after positional and optional parameters 
    args = parser.parse_args()
    if len(args.args) != 1:
        unit = 'MB'
    else:
        unit = args.args[0]
    MOUNT_DIRECTORY = args.mount_path
    CLOUDFUSION_NOT_UPLOADED_PATH = MOUNT_DIRECTORY+"/stats/notuploaded"
    DATA_DIRECTORY = MOUNT_DIRECTORY+"/data"
    TEST_DIRECTORY = DATA_DIRECTORY+'/Magic\ Briefcase/cftest'
    CLOUDFUSION_STATS_DIR = MOUNT_DIRECTORY+"/stats"
    CLOUDFUSION_CONFIG_PATH = MOUNT_DIRECTORY+"/config/config"
    log_directory = args.log_directory
    log_directory = log_directory+'/logs/streaming/'+now
    filesize_arr = args.filesize.split() 
    filegroup_size = int(args.filegroup_size)
    filequantity_arr =[]
    for size in map(int,filesize_arr):
        print "size: %s number of files in group: %s"%(size, int(round(1.0*filegroup_size/size)))
        filequantity_arr.append( int(round(1.0*filegroup_size/size)) )
    
        
    
    write_time_log = log_directory + '/write_time_log'
    read_time_log = log_directory + '/read_time_log'
    previous_file_read_time_log = log_directory + '/previous_file_read_time_log'
    if not os.path.exists(log_directory):
        os.makedirs(log_directory)
    if not os.path.exists(SAMPLE_FILES_DIR):
        os.makedirs(SAMPLE_FILES_DIR)
    os.makedirs(TEMP_DIR)
    with open(log_directory+'/diff', 'w') as f:
        f.write(git('--no-pager', 'diff').stdout)
        
    print "starting tests"

    os.system(os.path.dirname(__file__)+"/create_streaming_files.py %s '%s' '%s' %s"%(SAMPLE_FILES_DIR, args.filesize, ' '.join(map(str,filequantity_arr)), unit))
    print "files created"
    idx = 0 
    for size in filesize_arr:
        if os.path.exists('/tmp/stop'):
            print "waiting as long as /tmp/stop exists"
            while os.path.exists('/tmp/stop'):
                sleep(1)
            print "continuing test"
        restart_cloudfusion()
        if not os.path.exists(TEST_DIRECTORY):
            os.makedirs(TEST_DIRECTORY)
        print "Test writing file size %s %s"%(size,unit)
        log_copy_operation(SAMPLE_FILES_DIR+'/'+size+unit+'_', TEST_DIRECTORY+'/'+size+unit+'_', size, filequantity_arr[idx], write_time_log, SAMPLE_FILES_DIR, unit)
        print "Test reading file size %s %s"%(size,unit)
        log_copy_operation(TEST_DIRECTORY+'/'+size+unit+'_', TEMP_DIR+'/'+size+unit+'_', size, filequantity_arr[idx], read_time_log, SAMPLE_FILES_DIR, unit, check=True)
        for nr in range(1,filequantity_arr[idx]+1):  # from 1 to file quantity
            os.remove(TEST_DIRECTORY+'/'+size+unit+'_'+str(nr))
            os.remove(TEMP_DIR+'/'+size+unit+'_'+str(nr))
        idx += 1
    shutil.rmtree(TEMP_DIR)
    print "Created statistic files in "+log_directory

if __name__ == '__main__':
    main()
