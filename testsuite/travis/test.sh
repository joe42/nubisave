#!/bin/bash

perl -pi -e "s/user = /user = ${USR}/g" splitter/mountscripts/Dropbox.ini
perl -pi -e "s/password = /password = ${PW}/g" splitter/mountscripts/Dropbox.ini
perl -pi -e "s/user = /user = ${USR}/g" splitter/mountscripts/Sugarsync.ini
perl -pi -e "s/password = /password = ${PW}/g" splitter/mountscripts/Sugarsync.ini
perl -pi -e "s/access_key_id =.*/access_key_id =${GS_ID}/g" splitter/mountscripts/Google_Storage.ini
perl -pi -e "s/secret_access_key =.*/secret_access_key =${GS_KEY}/g" splitter/mountscripts/Google_Storage.ini
perl -pi -e "s/access_key_id =.*/access_key_id =${S3_ID}/g" splitter/mountscripts/AmazonS3.ini
perl -pi -e "s/secret_access_key =.*/secret_access_key =${S3_KEY}/g" splitter/mountscripts/AmazonS3.ini
perl -pi -e "s/user =.*/user =${WEBDAV_USR}/g" splitter/mountscripts/Tonline.ini
perl -pi -e "s/password =.*/password =${WEBDAV_PWD}/g" splitter/mountscripts/Tonline.ini
perl -pi -e "s/user =.*/user =${WEBDAV2_USR}/g" splitter/mountscripts/GMXMediacenter.ini
perl -pi -e "s/password =.*/password =${WEBDAV2_PWD}/g" splitter/mountscripts/GMXMediacenter.ini
perl -pi -e "s/user =.*/user =${WEBDAV3_USR}/g" splitter/mountscripts/Box.ini
perl -pi -e "s/password =.*/password =${WEBDAV3_PWD}/g" splitter/mountscripts/Box.ini
perl -pi -e "s/user =.*/user =${WEBDAV4_USR}/g" splitter/mountscripts/Yandex.ini
perl -pi -e "s/password =.*/password =${WEBDAV4_PWD}/g" splitter/mountscripts/Yandex.ini


echo -e "\ncache_dir = cache" splitter/mountscripts/Dropbox.ini
echo -e "\ncache_dir = cache" splitter/mountscripts/Sugarsync.ini
echo -e "\ncache_dir = cache" splitter/mountscripts/Google_Storage.ini
echo -e "\ncache_dir = cache" splitter/mountscripts/AmazonS3.ini
echo -e "\ncache_dir = cache" splitter/mountscripts/Tonline.ini
echo -e "\ncache_dir = cache" splitter/mountscripts/GMXMediacenter.ini
echo -e "\ncache_dir = cache" splitter/mountscripts/Box.ini
echo -e "\ncache_dir = cache" splitter/mountscripts/Yandex.ini
mkdir cache


./start.sh headless &
sleep 10

cp splitter/mountscripts/Dropbox.ini ~/.nubisave/nubisavemount/config/
cp splitter/mountscripts/Sugarsync.ini ~/.nubisave/nubisavemount/config/
cp splitter/mountscripts/Google_Storage.ini ~/.nubisave/nubisavemount/config/
cp splitter/mountscripts/AmazonS3.ini ~/.nubisave/nubisavemount/config/
cp splitter/mountscripts/Tonline.ini ~/.nubisave/nubisavemount/config/
cp splitter/mountscripts/GMXMediacenter.ini ~/.nubisave/nubisavemount/config/
cp splitter/mountscripts/Box.ini ~/.nubisave/nubisavemount/config/
cp splitter/mountscripts/Yandex.ini ~/.nubisave/nubisavemount/config/

dd if=/dev/urandom of=testfile count=100 bs=1000000 #make 100MB testfile
cp testfile ~/.nubisave/nubisavemount/data/
sleep 600 #wait till it is uploaded




#now stop application, 
fusermount -zu ~/.nubisave/nubisavemount/
sleep 10; pkill -9 -f ".*splitter.Main.*" #just to be sure

#remove persistent cache
rm -rf cache/*

#restart nubisave
./start.sh headless &
sleep 10

cp splitter/mountscripts/Dropbox.ini ~/.nubisave/nubisavemount/config/
cp splitter/mountscripts/Sugarsync.ini ~/.nubisave/nubisavemount/config/
cp splitter/mountscripts/Google_Storage.ini ~/.nubisave/nubisavemount/config/
cp splitter/mountscripts/AmazonS3.ini ~/.nubisave/nubisavemount/config/
cp splitter/mountscripts/Tonline.ini ~/.nubisave/nubisavemount/config/
cp splitter/mountscripts/GMXMediacenter.ini ~/.nubisave/nubisavemount/config/
cp splitter/mountscripts/Box.ini ~/.nubisave/nubisavemount/config/
cp splitter/mountscripts/Yandex.ini ~/.nubisave/nubisavemount/config/


diff testfile ~/.nubisave/nubisavemount/data/testfile

# Revert changes to modified files.
git checkout HEAD -- splitter/mountscripts
rm ~/.nubisave/db/splitter_configuration/*


