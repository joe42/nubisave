#!/bin/sh
#
# Updates Debian metadata about the repository

# FIXME: gzip -n for deterministic output?

cd packages
baseversdir=$PWD

for versdir in *; do
	if [ -d $versdir ]; then
		echo "Version: $versdir"
		cd $versdir
		basearchdir=$PWD

		for archdir in *; do
			if [ -d $archdir ]; then
				if [ $archdir != sources ] && [ $archdir != binary-all ]; then
					echo "- Architecture: $archdir"
					cd $archdir

					dpkg-scanpackages . > Packages
					if [ -d ../binary-all ]; then
						dpkg-scanpackages ../binary-all >> Packages
					fi
					gzip -9c Packages > Packages.gz

					cd $basearchdir
				fi
			fi
		done

		cd sources
		dpkg-scansources . > Sources
		gzip -9c Sources > Sources.gz
		#cd $basearchdir

		cd $baseversdir
	fi
done

topvers=current
toparch=binary-all

echo "Version: TRUNK [$topvers]"
echo "- Architecture: $toparch"
dpkg-scanpackages $topvers/$toparch > Packages
dpkg-scanpackages $topvers/binary-all >> Packages
gzip -9c Packages > Packages.gz
dpkg-scansources $topvers/sources > Sources
gzip -9c Sources > Sources.gz

cp Release.head Release
apt-ftparchive release . | grep -v " Release" >> Release

gpg --default-key CCF0E02E -abs -o Release.gpg Release

find -name Packages | xargs rm -f
find -name Sources | xargs rm -f

