#!/bin/bash
#usage: sh mount_fusedav.sh user password davurl mountpoint
#make data directory at mountpoint if it does not yet exist
mkdir -p "$4/data"
#mount directory fusedav 
fusedav -u "$1" -p "$2" "$3" "$4/data"&
#create a config file after a succesfull connection to let the splitter know everything went well
mkdir -p "$4/config"
touch "$4/config/config"

