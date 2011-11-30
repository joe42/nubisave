#!/bin/sh
#make data directory at mountpoint if it does not yet exist
mkdir -p $2/data $1/data
#mount directory
mount_point=$2/data
root=$1/data
encfs --extpass=ssh-askpass --standard $root $mount_point
#create a config file after a succesfull connection to let the splitter know everything went well
mkdir -p $2/config
touch $2/config/config


