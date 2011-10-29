#!/bin/bash

scriptpath=`readlink -f $0`
scriptloc=`dirname $scriptpath`
cd $scriptloc

echo "Start des Splitter Modules"
mountpoint=`pwd`/mount
storages=`pwd`/storages

mkdir $mountpoint $storages
cd splitter
./mount.sh $mountpoint $storages &
cd ..

# FIXME: This should be solved by some event detection
sleep 2;

echo "Start von NubiSave"

cd bin/
java -jar Nubisave.jar $mountpoint

