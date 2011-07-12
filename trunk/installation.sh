#!/bin/bash
#Installations-Script-Nubisave

echo "Möchten Sie Space installieren? j/N "

read antwort 

if [ $antwort == "j" ] 
  then
   
   echo 'deb http://serviceplatform.org/packages/ ./' >> /etc/apt/sources.list.d/space.list
   apt-get update
   apt-get install space

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
echo "Kopiere CloudQoS-Ontologie"
sudo cp services/CloudQoS.wsml $ontologiepath

echo -n "Möchten Sie Beispiel-Services mounten? j/n "
read antwort 

if [ $antwort == "j" ] 
  then

done
fi



