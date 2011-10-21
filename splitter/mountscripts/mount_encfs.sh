#!/bin/sh
#make data directory at mountpoint if it does not yet exist
mkdir -p $2/data
#mount directory 
encfs --extpass=ssh-askpass --standard $1 $2 
#create a config file after a succesfull connection to let the splitter know everything went well
mkdir -p $2/config
touch $2/config/config


