#!/bin/bash

echo "Start des Splitter Modules"
mountpoint=$HOME/nubisavemount
storages=$HOME/.nubisave/storages

mkdir -p $mountpoint $storages
scriptpath=`readlink -f $0`
scriptloc=`dirname $scriptpath`
cd $scriptloc

cd splitter
./mount.sh $mountpoint $storages &
cd ..

if [ "$1" != "headless" ]
then
    # FIXME: This should be solved by some event detection
    sleep 2;

    echo "Start von NubiSave"

    cd bin/
    java -jar Nubisave.jar $mountpoint
fi





