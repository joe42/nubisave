#!/bin/bash
#
# Installations-Script für NubiSave auf Debian bzw. Derivaten (inkl. Ubuntu)
#
# DEPRECATED - use Debian package if possible!

if [ ! -x /usr/bin/sudo ]; then
	echo "Fehler: sudo muss manuell installiert und konfiguriert werden." >&2
	exit 1
fi

read -p "SPACE-Plattformdienste installieren? (j/N) " antwort

if [ "$antwort" == "j" ]
then
	sudo bash -c "echo 'deb http://serviceplatform.org/packages/ ./' >> /etc/apt/sources.list.d/space.list"
	sudo apt-get update
	#sudo apt-get install space
	sudo apt-get install mysql-server mysql-client conqo conqotool
else
	echo "Ohne SPACE weiter - ConQo + Contract Wizard müssen vorhanden sein oder von extern genutzt werden."
fi

read -p "SPACE-Plattformdienste konfigurieren? (j/N) " antwort

if [ "$antwort" == "j" ]
then
	echo "Registrierung der Speicherdienstanbieter..."

	for service in services/CloudServices/*.wsml; do
		servicename=$(basename "$service")
		echo "-------------------------------------------"
		echo "Registriere Service:    $servicename"
		conqotool -u admin register "$service"
	done

	#ontologiepath=`find /var/lib 2>/dev/null | grep Matchmaker/ontologies | head -n 1 `cloud
	ontologiepath=/var/lib/tomcat6/webapps/Matchmaker/ontologies/

	echo
	echo
	echo "Kopiere CloudStorage-Ontologie"
	sudo cp services/CloudStorage.wsml $ontologiepath
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

