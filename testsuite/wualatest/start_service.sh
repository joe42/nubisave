#!/bin/bash
if [ $# -lt 2 ]; then
   echo
   echo "Not enough arguments."
   echo
   echo
   echo Start Wuala.
   echo Usage: `basename $0` username password
   echo Example: `basename $0` joe 123456
   echo
   exit
fi  

WUALA_USERNAME=$1
WUALA_PASSWORD=$2

wuala login  > /dev/null &
sleep 10
