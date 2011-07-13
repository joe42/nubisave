#!/bin/bash

echo "Start von nubisave"
echo "Files werden auf 3 Ordner gesplittet"
echo "~/.cache/nubisave/storages/storage01"
echo "~/.cache/nubisave/storages/storage02"
echo "~/.cache/nubisave/storages/storage03"

echo -n "Cloud-Services in Mountpoints hängen?j/N"

read antwort
if [ $antwort == "j" ] 
  then
   for mount in mount_script/mount_*.sh ; do 
      mountname=$( basename "$mount")
      echo "Mounte CloudStorage :    $mountname"
      echo "-------------------------------------------"
      bash ./$mount
     done  
else
echo "Ohne Sample-Mounts weiter"
fi



