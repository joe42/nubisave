#!/bin/sh
#usage: sh unmount_nubisave.sh mountpoint

fusermount -u $1
