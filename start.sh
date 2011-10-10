#!/bin/bash

echo "Start des Splitter Modules"
mountpoint=`pwd`/mount
storages=`pwd`/storages

mkdir $mountpoint $storages
cd splitter
./mount.sh $mountpoint $storages &
cd ..

sleep 2;

echo "Start von nubisave"

cd bin/
java -jar Nubisave.jar $mountpoint


