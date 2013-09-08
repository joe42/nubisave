#!/bin/bash
#
# Launches NubiSave. Either the configured instances of the splitter filesystem, or
# the central configuration GUI, or all of these together.
# Syntax: nubisave [headless|gui|<instance>|stop|--help]

if [ "$1" == "-h" ] || [ "$1" == "--help" ]
then
	echo "Syntax: nubisave [headless|gui|<instance>|stop|--help]"
	exit
fi

userdir=$HOME/nubisave
instance=$HOME/.nubisave
storages=$HOME/.storages

if [ "$1" != "headless" ] && [ "$1" != "gui" ] && [ "$1" != "stop" ]
then
	instance=$1
fi
mountpoint=$instance/nubisavemount

if [ "$1" == "stop" ]
then
	echo "Stopping NubiSave..."
	if [ -d $mountpoint ] && [ -d $mountpoint/data ]
	then
		fusermount -u $mountpoint -z
		exit
	else
		echo "Error: The splitter file system is not mounted." >&2
		exit 1
	fi
fi

echo "Starting NubiSave..."

mkdir -p "$mountpoint" "$storages"
scriptpath=`readlink -f $0`
scriptloc=`dirname $scriptpath`
cd $scriptloc

#groups | grep -q fuse
fusermount 2>/dev/null
if [ $? == 126 ]
then
	echo "Error: The current system user ($USER) must be added to the 'fuse' group." >&2
	exit 1
fi

if [ "$1" == "gui" ]
then
	if [ ! -d $mountpoint/data ]; then
		echo "Error: The splitter file system is not mounted yet." >&2
		exit 1
	fi
else
	echo "- Start of the file splitter/dispersion module"
	cd splitter
	./mount.sh "$mountpoint" "$storages" &
	cd ..
fi

if [ ! -h "$userdir" ]
then
	ln -sf "$mountpoint/data" "$userdir"
fi

headless=0
if [ "$1" == "headless" ]
then
	headless=1
elif [ -z $DISPLAY ]
then
	echo "- Forcing headless mode due to missing display server!"
	headless=1
fi

if [ "$headless" == 0 ]
then
	echo "- Start of the storage flow configuration editor"
	cd bin/
	java -Djava.library.path=lib -jar Nubisave.jar "$mountpoint"
fi
