#!/bin/bash
#
# Installations-Script für NubiSave auf Debian bzw. Derivaten (inkl. Ubuntu)
#
# DEPRECATED - use Debian package if possible!

if [ ! -x /usr/bin/sudo ]; then
	echo "Fehler: sudo muss manuell installiert und konfiguriert werden." >&2
	exit 1
fi

echo
echo
echo "Installiere Entwicklerpakete (openJDK, FUSE, ...)"

sudo apt-get install openjdk-7-jdk
sudo apt-get install python-setuptools gcc libssl-dev python-dev
sudo apt-get install fuse-utils || sudo apt-get install fuse #fuse-utils was renamed to fuse

sudo usermod -a -G fuse "$USER"
newgrp fuse

sudo chgrp fuse /dev/fuse   #On Debian: change permissions for fuse interface
sudo chmod g+wr /dev/fuse   

echo
echo
echo "Kompilieren des Core-Moduls"

cd splitter/
make
cd ..

echo
echo
echo "Installation von CloudFusion"

git submodule init
git submodule update

cd CloudFusion
sudo python setup.py install
cd ..

echo "Installation abgeschlossen"

