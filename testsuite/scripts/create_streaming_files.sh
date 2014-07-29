#!/bin/bash
if [ $# -lt 1 ]; then
   echo
   echo "Not enough arguments."
   echo
   echo
   echo Creates n files {$size}MB_#NR for all sizes in test_file_sizes with the size suggested by the name. The content is random. 
   echo The number of files for each size is specified by the parameter number_of_files.
   echo The files are stored to the specified directory. All files in directory are deleted, when starting the script.
   echo Usage: `basename $0` directory  test_file_sizes number_of_files
   echo Example: 
   echo Generate 10 1MB files, 10 10 MB files, 10 100MB files, 2 1000MB files and one 2000MB file.
   echo `basename $0` generated_files \"1 10 100 1000 2000\" \"10 10 10 2 1\" MB
   echo
   exit
fi  
#create files
FOLDER=$1
FILE_SIZES=(`echo "$2"`)
FILE_QUANTITY=(`echo "$3"`)
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
mkdir -p "$1"
rm -f $FOLDER/*

for i in ${!FILE_SIZES[*]}  # iterate over all indexes in array
do
	for nr in `seq 1 ${FILE_QUANTITY[$i]}`  # from 1 to file quantity
	do
    	dd if=/dev/urandom of=$FOLDER/${FILE_SIZES[$i]}MB_$nr bs=1MB count=${FILE_SIZES[$i]}
	done
done
