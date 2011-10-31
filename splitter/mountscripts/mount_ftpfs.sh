#!/bin/sh
#usage: sh mount_ftpfs.sh mountpoint user password server remotedir

#make data directory at mountpoint if it does not yet exist
mkdir -p $1/data
#mount directory via ftp
curlftpfs $2:$3@$4$5 $1/data
#create a config file after a succesfull connection to let the splitter know everything went well
mkdir -p $1/config
touch $1/config/config
