#!/bin/bash
#Installations-Script-Nubisave


echo "Möchten Sie Space installieren? j/N "

read antwort

if [ $antwort == "j" ]
  then

echo 'deb http://serviceplatform.org/packages/ ./' >> sudo /etc/apt/sources.list.d/space.list
 sudo  apt-get update
 sudo  apt-get install space

  else
  echo "Ohne Space weiter - muss vorhanden sein"
fi

echo "Registrieren der Dienste" 

for service in services/CloudServices/*.wsml ; do 
    servicename=$( basename "$service")
    echo "Registriere Service:    $servicename"
    echo "-------------------------------------------"
    conqotool -u admin register "$service"
done

ontologiepath=`find /var/lib 2>/dev/null | grep Matchmaker/ontologies | head -n 1 `cloud

echo
echo
echo "Kopiere CloudQoS-Ontologie"
sudo cp services/CloudQoS.wsml $ontologiepath

echo
echo
echo "Installiere openJDK"

apt-get install openjdk-6-jdk

echo
echo
echo "Kompilieren des Core-Moduls"

cd splitter/
sudo make 
cd ..

mkdir -p $HOME/.config/nubisave
mkdir -p $HOME/.config/nubisave/fileparts
mkdir -p $HOME/.config/nubisave/files
mkdir -p $HOME/.config/nubisave/storages

echo
echo
echo -n "Möchten Sie Beispiel-Services mounten und Nubisave starten? [start.sh] j/n "
read antwort 

if [ $antwort == "j" ] 
  then
   bash ./start.sh
fi



