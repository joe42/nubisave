#!/bin/sh
#usage: sh unmount_fuSim.sh mountpoint

#unmount fuSim 
fusermount -zu "$1/data"

#remove data directory
rm "$1/data"
#remove config file to let the splitter know that the unmounting went well
rm "$1/config/config"
rmdir "$1/config"
#remove directory at mountpoint
rmdir "$1"

