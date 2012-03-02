#!/bin/bash
if [ $# -lt 1 ]; then
   echo
   echo "Not enough arguments."
   echo
   echo
   echo Creates the files {$size}MB for all sizes in test_file_sizes with the size suggested by the name. The content is random. 
   echo Additionally, a 1GB sparse file with the name sparse and a 2GB sparse file with the name sparse2 is created. The sparse file has about 1MB of sequential data.
   echo The files are stored to the specified directory. All files in directory are deleted, when starting the script.
   echo Usage: `basename $0` directory  test_file_sizes 
   echo Example: `basename $0` generated_files "1 10 100 1000 2000"
   echo
   exit
fi  
#create files
FOLDER=$1
FILE_SIZES="$2"
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ONE_MB=$((1024*1024))
TEN_MB=$(($ONE_MB*10))
mkdir -p "$1"
rm -f $FOLDER/*

for size in `echo $FILE_SIZES`
do
    dd if=/dev/urandom of=$FOLDER/${size}MB bs=1MB count=$size
done

dd if=/dev/zero of=$FOLDER/sparse bs=1 count=1 seek=1000MB
dd if=/dev/zero of=$FOLDER/sparse2 bs=1 count=1 seek=2000MB

python ../scripts/write.py $FOLDER/sparse $ONE_MB $TEN_MB

