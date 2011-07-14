#!/bin/bash

mntpoint="$HOME/nubisave"

function shouldexist {
if [ -e "$mntpoint/$1" ]
then
	echo "	OK" 
else
	echo "	ERROR"
fi
}

function shouldntexist {
if [ -e "$mntpoint/$1" ]
then
	echo "	ERROR" 
else
	echo "	OK"
fi
}

function checksum {
	csum=`grep $1 samplefiles/checksums | head -n1 | cut -d " "  -f 1`	
	sum=`md5sum -b "$mntpoint/$1" | cut -d " "  -f 1`
	if [ $csum == $sum ]
	then
		echo "	OK checksum" 
	else
		echo "	ERROR checksum"
	fi
}




if [ -d "samplefiles" ] 
then
while [ true ]
do
	echo -n "Create new sample files? [y/N]:"
	read answer
	if [ "$answer" == "" ]
	then
		answer="n"
	fi

	if [ "$answer" == "y" -o "$answer" == "Y" ]
	then
		./createsamplefiles.sh
    break
	else if [ "$answer" == "n" -o "$answer" == "N" ]
		then
			break
		fi
	fi
done
else
	./createsamplefiles.sh
fi

answer=""

while [ true ]
do
	echo -n "Enter test directory [$mntpoint]:"
	read answer 
	if [ "$answer" == "" ]
	then
		answer=$mntpoint	
	fi

	if [ ! -d "$answer" ]
	then
		echo "ERROR: $answer doesn't exists!"
	else
		break;
	fi
done

mntpoint=$answer

echo "create 0 byte file"
touch "$mntpoint/0bytefile"
shouldexist "/0bytefile"

echo "remove 0 byte file"
rm "$mntpoint/0bytefile"
shouldntexist "0bytefile"

echo "create 1st 0 byte file"
touch "$mntpoint/0bytefile1"
shouldexist "0bytefile1"

echo "create 2nd 0 byte file"
touch "$mntpoint/0bytefile2"
shouldexist "0bytefile2"

echo "create 3rd 0 byte file"
touch "$mntpoint/0bytefile3"
shouldexist "0bytefile3"

echo "remove 1st 0 byte file"
rm "$mntpoint/0bytefile1"
shouldntexist "0bytefile1"

echo "remove 3rd 0 byte file"
rm "$mntpoint/0bytefile3"
shouldntexist "0bytefile3"

echo "remove 2nd 0 byte file"
rm "$mntpoint/0bytefile2"
shouldntexist "0bytefile2"

echo ""
echo "Speedtest - write:"
echo "#size total user sys" > times.dat

file="500kb.0"
echo "create $file"
/usr/bin/time -a -f "`stat -c%s samplefiles/$file` %e %U %S" -o times.dat cp samplefiles/$file "$mntpoint/"
shouldexist $file
checksum $file

file="500kb.1"
echo "create $file"
/usr/bin/time -a -f "`stat -c%s samplefiles/$file` %e %U %S" -o times.dat cp samplefiles/$file "$mntpoint/"
shouldexist $file
checksum $file

file="500kb.2"
echo "create $file"
/usr/bin/time -a -f "`stat -c%s samplefiles/$file` %e %U %S" -o times.dat cp samplefiles/$file "$mntpoint/"
shouldexist $file
checksum $file

file="1mb.0"
echo "create $file"
/usr/bin/time -a -f "`stat -c%s samplefiles/$file` %e %U %S" -o times.dat cp samplefiles/$file "$mntpoint/"
shouldexist $file
checksum $file

file="1mb.1"
echo "create $file"
/usr/bin/time -a -f "`stat -c%s samplefiles/$file` %e %U %S" -o times.dat cp samplefiles/$file "$mntpoint/"
shouldexist $file
checksum $file

file="1mb.2"
echo "create $file"
/usr/bin/time -a -f "`stat -c%s samplefiles/$file` %e %U %S" -o times.dat cp samplefiles/$file "$mntpoint/"
shouldexist $file
checksum $file

file="10mb.0"
echo "create $file"
/usr/bin/time -a -f "`stat -c%s samplefiles/$file` %e %U %S" -o times.dat cp samplefiles/$file "$mntpoint/"
shouldexist $file
checksum $file

file="10mb.1"
echo "create $file"
/usr/bin/time -a -f "`stat -c%s samplefiles/$file` %e %U %S" -o times.dat cp samplefiles/$file "$mntpoint/"
shouldexist $file
checksum $file

file="10mb.2"
echo "create $file"
/usr/bin/time -a -f "`stat -c%s samplefiles/$file` %e %U %S" -o times.dat cp samplefiles/$file "$mntpoint/"
shouldexist $file
checksum $file

file="50mb.0"
echo "create $file"
/usr/bin/time -a -f "`stat -c%s samplefiles/$file` %e %U %S" -o times.dat cp samplefiles/$file "$mntpoint/"
shouldexist $file
checksum $file

file="50mb.1"
echo "create $file"
/usr/bin/time -a -f "`stat -c%s samplefiles/$file` %e %U %S" -o times.dat cp samplefiles/$file "$mntpoint/"
shouldexist $file
checksum $file

file="50mb.2"
echo "create $file"
/usr/bin/time -a -f "`stat -c%s samplefiles/$file` %e %U %S" -o times.dat cp samplefiles/$file "$mntpoint/"
shouldexist $file
checksum $file

echo "times written to times.dat"

echo "remove testfiles"
rm "$mntpoint/500kb.0"
rm "$mntpoint/500kb.1"
rm "$mntpoint/500kb.2"
rm "$mntpoint/1mb.0"
rm "$mntpoint/1mb.1"
rm "$mntpoint/1mb.2"
rm "$mntpoint/10mb.0"
rm "$mntpoint/10mb.1"
rm "$mntpoint/10mb.2"
rm "$mntpoint/50mb.0"
rm "$mntpoint/50mb.1"
rm "$mntpoint/50mb.2"
