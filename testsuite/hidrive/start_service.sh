#!/bin/bash
if [ $# -lt 2 ]; then
   echo
   echo "Not enough arguments."
   echo
   echo
   echo Start Service.
   echo Usage: `basename $0` username password
   echo Example: `basename $0` joe 123456
   echo
   exit
fi  

USERNAME=$1
PASSWORD=$2

mkdir -p ~/hidrive
echo -e "$USERNAME\n$PASSWORD\n"|mount ~/hidrive
sleep 10
