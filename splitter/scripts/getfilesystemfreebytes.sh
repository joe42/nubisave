#!/bin/bash
#Output the file system's free bytes. The file system is given in the form of a path as the first parameter.
echo `df $1 --block-size 1|tail -n1|awk '{print $4}'`