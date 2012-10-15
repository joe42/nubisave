#!/bin/sh
#usage: sh mount_lessfs.sh mountpoint databasedir backendservice1 COMPRESSIONTYPE ENCRYPT_DATA_ON_OFF CACHE_SIZE_IN_MB PASSWORD
#replace parameters in template configuration file

echo sed -e "s#DATABASEPATH#$2#g" -e "s#MOUNTPOINT#$3#g" -e "s/COMPRESSIONTYPE/$4/g" -e "s/ENCRYPT/$5/g" -e "s/CACHE_SIZE_IN_MB/$6/g" "mountscripts/lessfs.cfg.template" >param 
sed -e "s#DATABASEPATH#$2#g" -e "s#MOUNTPOINT#$3#g" -e "s/COMPRESSIONTYPE/$4/g" -e "s/ENCRYPT_DATA_ON_OFF/$5/g" -e "s/CACHE_SIZE_IN_MB/$6/g" "mountscripts/lessfs.cfg.template" > "mountscripts/lessfs.cfg"
#export passord
export PASSWORD="$7"
#make data directory at mountpoint if it does not yet exist
mkdir -p "$1/data"
#make database directory for meta data
mkdir "$2"
#copy file for berkeley db
cp mountscripts/DB_CONFIG "$2"
#mount directory lessfs 
export SUDO_ASKPASS=/usr/bin/ssh-askpass
sudo -A mklessfs -c "mountscripts/lessfs.cfg"
sudo -A lessfs "mountscripts/lessfs.cfg" "$1/data"
sudo -A chown `id -un`:`id -un` "$1/data"
#create a config file after a succesfull connection to let the splitter know everything went well
mkdir -p "$1/config"
#ln -s mnt/.lessfs/lessfs_stats "$1/config/config"
touch "$1/config/config"
