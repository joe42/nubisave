#!/bin/bash

mntpoint="splitter"
statsfile=nubisave.test.out

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
	csum=`md5sum -b "samplefiles/$1" | cut -d " "  -f 1`
	sum=`md5sum -b "copies/$1" | cut -d " "  -f 1`
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
rm $mntpoint/*

echo "size	run	write	rewrite	read	reread" > $statsfile

for N in 4 8 16 32 64 128 256 512 1024 2048 4096 8192 16384 32768 65536 131072
do
	for C in 1 2 3 4 5 6 7 8
	do
		rm "$mntpoint/${N}kb"
		
		beginwrite=$(date +%s.%N)
		cp samplefiles/${N}kb "$mntpoint/"
		endwrite=$(date +%s.%N)
	
		beginrewrite=$(date +%s.%N)
		cp samplefiles/${N}kb "$mntpoint/"
		endrewrite=$(date +%s.%N)
	
		beginread=$(date +%s.%N)
		cp "$mntpoint/${N}kb" copies
		endread=$(date +%s.%N)
	
		beginreread=$(date +%s.%N)
		cp "$mntpoint/${N}kb" copies
		endreread=$(date +%s.%N)
	
		shouldexist ${N}kb
		checksum ${N}kb

		rm "$mntpoint/${N}kb"
	
		echo "$N	$C	`echo "scale=0; $N / ($endwrite - $beginwrite)" | bc`	`echo "scale=0; $N / ($endrewrite - $beginrewrite)" | bc`	`echo "scale=0; $N / ($endread - $beginread)" | bc`	`echo "scale=0; $N / ($endreread - $beginreread)" | bc`" | tee -a $statsfile

		if [ -e "stop" ]
		then
			echo "	stopping" 
			read x
		fi

	done
done

echo "times written to $statsfile"

./gengnuplot.sh $statsfile write
./gengnuplot.sh $statsfile rewrite
./gengnuplot.sh $statsfile read
./gengnuplot.sh $statsfile reread

gnuplot nubi3d.dem
