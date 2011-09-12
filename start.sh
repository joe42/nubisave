#!/bin/bash

echo "Start von nubisave"
echo "Files werden auf 3 Ordner gesplittet"
echo "~/.cache/nubisave/storages/storage01"
echo "~/.cache/nubisave/storages/storage02"
echo "~/.cache/nubisave/storages/storage03"

# FIXME: find out why this is needed here
mkdir -p .cloudfusion/logs

read -p "Cloud-Services in Mountpoints hängen? (j/N) " antwort

if [ $antwort == "j" ]
then
	for mount in mount_script/mount_*.sh
	do
		mountname=$(basename "$mount")
		echo "-------------------------------------------"
		echo "Mounte CloudStorage :    $mountname"
		bash ./$mount
	done
else
	echo "Ohne Sample-Mounts weiter"
fi

echo "Starten des Core-Moduls"

sudo umount ~/nubisave

./splitter_mount.sh >> /dev/null 2>/dev/null &

read -p "CloudStorage Anbieter konfigurieren? (j/N) " antwort

if [ $antwort == "j" ]
then
	cd bin/
	java -jar Nubisave.jar
	cd ..
fi

echo "Nubisave erfolgreich gestartet"

