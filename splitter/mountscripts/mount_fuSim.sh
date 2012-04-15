#!/bin/sh
#usage: sh mount_fusim.sh root_directory mountpoint read_bandwidth_in_KB_per_second write_bandwidth_in_KB_per_second write_errors_in_bits_per_GB  

#make root directory if it does not yet exist
mkdir -p "$1" 
#make data directory at mounpoint if it does not yet exist
mkdir -p "$2/data"
#mount fuSim module
python ../testsuite/FuSim/fusim.py "$1" "$2/data" "$3" "$4" "$5"
#create a config file after a succesfull setup to let the splitter know everything went well
mkdir -p "$2/config"
touch "$2/config/config"

