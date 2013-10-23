#!/bin/sh
#usage: sh mount_sshfs.sh mountpoint user server remotedir

#make data directory at mountpoint if it does not yet exist
mkdir -p "$1/data"
#mount directory via ssh
if [ -z $DISPLAY ]
then
	sshfs "$2@$3:$4" "$1/data"
else
	export SSH_ASKPASS=ssh-askpass
	setsid sshfs "$2@$3:$4" "$1/data"
fi

#create a config file after a succesful connection to let the splitter know everything went well
for x in `seq 0 20`
do
	mount | grep -q "$1/data"
	if [ $? = 0 ]
	then
		mkdir -p "$1/config"
		touch "$1/config/config"
		exit
	fi
	sleep 1
done
