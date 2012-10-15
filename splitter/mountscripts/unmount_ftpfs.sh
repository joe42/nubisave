#!/bin/sh
#usage: sh unmount_ftpfs.sh mountpoint
#unmount ftpfs
fusermount -zu "$1/data"
#remove data directory
rmdir "$1/data"
#remove config file to let the splitter know that the unmounting went well
rm "$1/config/config"
rmdir "$1/config"
#remove directory at mountpoint
rmdir "$1"

