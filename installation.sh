#!/bin/bash
#
# Installations-Script für NubiSave auf Debian bzw. Derivaten (inkl. Ubuntu)

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
echo "Kopiere CloudQoS-Ontologie"
sudo cp services/CloudQoS.wsml $ontologiepath

echo
echo
echo "Installiere Entwicklerpakete (openJDK, FUSE, ...)"

sudo apt-get install openjdk-6-jdk libfuse-dev python-simplejson python-setuptools python-oauth python-httplib2

sudo easy_install 'http://pypi.python.org/packages/2.6/p/poster/poster-0.4-py2.6.egg#md5=f69a6be30737ad5d652a602f3af005ac'

echo
echo
echo "Kompilieren des Core-Moduls"

cd splitter/
make
cd ..

echo
echo
echo "Installation von CloudFusion"

cd cloudfusion/
python setup.py build
sudo python setup.py install
cd ..

echo "Installation abgeschlossen"

