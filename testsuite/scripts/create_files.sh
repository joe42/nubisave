#!/bin/bash
if [ $# -lt 1 ]; then
   echo
   echo "Not enough arguments."
   echo
   echo
   echo Creates the files 1MB 10MB 100MB 1000MB 2000MB with the size suggested by the name. The content is random. 
   echo Additionally, a 1GB sparse file with the name sparse is created. The sparse file has about 1MB of sequential data.
   echo The files are stored to the specified directory. All files in directory are deleted, when starting the script.
   echo Usage: `basename $0` directory
   echo Example: `basename $0` generated_files
   echo
   exit
fi  
#create files
FOLDER=$1
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
ONE_MB=$((1024*1024))
TEN_MB=$(($ONE_MB*10))
mkdir -p "$1"
rm -f $FOLDER/*

for size in 1 10 100 1000 2000
do
    dd if=/dev/urandom of=$FOLDER/${size}MB bs=${size}MB count=1
done

dd if=/dev/zero of=$FOLDER/sparse bs=1 count=1 seek=1000MB

python ../scripts/write.py $FOLDER/sparse $ONE_MB $TEN_MB

