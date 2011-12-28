#!/bin/bash
if [ $# -lt 1 ]; then
   echo
   echo "Not enough arguments."
   echo
   echo
   echo Be careful. Executing this script will delete all your files.
   echo Usage: `basename $0` log_directory [iozone_directory]
   echo Example: `basename $0` /home/joe/nubisavelogs ~/iozone3_398/src/current/iozone
   echo
   exit
fi  
LOG_DIR="$1/`git rev-parse HEAD`"
CURRENT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
NUBISAVE_DIR="$CURRENT_DIR/.."
MOUNTPOINT=~/nubisavemount
IOZONE_DIR="$2"
STORAGES_DIR=~/.nubisave/storages
echo logs are written to "$LOG_DIR"

cd systemtest
mkdir -p "$LOG_DIR"
git diff > "$LOG_DIR"/diff
"$NUBISAVE_DIR"/start.sh  &> "$LOG_DIR"/splitter_log &
javac -classpath lib/sikuli-script.jar src/GuiTest.java
java -classpath "lib/sikuli-script.jar:src/" GuiTest
cd ..

#function tests:
"$CURRENT_DIR"/fuse_tests.sh "$MOUNTPOINT"/data/ mytestfile | tee "$LOG_DIR"/fuse_tests_results
#performance tests for memory and write/read:
rm -f /tmp/nubi_mem_trace 
touch /tmp/nubi_mem_trace 
./mem.sh /tmp/nubi_mem_trace & #-q 64 -g 132000
"$IOZONE_DIR"/iozone -acMe -y 4 -g 14000 -f "$MOUNTPOINT"/data/.iozone |tee "$LOG_DIR"/iozone_results
pkill mem.sh
cat /tmp/nubi_mem_trace > "$LOG_DIR"/nubi_mem_trace
cat /tmp/nubi_mem_trace | grep java > /tmp/nubi_mem_trace2
cat /tmp/nubi_mem_trace2 | gawk 'BEGIN { getline; time=$4 } {print $4-time " " $2 " " $3 }' > /tmp/nubi_mem_trace3
gnuplot plot_mem.gnp
mv memory_usage.svg "$LOG_DIR"
rm -f /tmp/nubi_mem_trace2 /tmp/nubi_mem_trace3 /tmp/nubi_mem_trace

cd "$IOZONE_DIR"
./Generate_Graphs "$LOG_DIR"/iozone_results
mv bkwdread "$LOG_DIR"
mv fread "$LOG_DIR"
mv freread "$LOG_DIR"
mv frewrite "$LOG_DIR"
mv fwrite "$LOG_DIR"
mv randread "$LOG_DIR"
mv randwrite "$LOG_DIR"
mv read "$LOG_DIR"
mv recrewrite "$LOG_DIR"
mv reread "$LOG_DIR"
mv rewrite "$LOG_DIR"
mv strideread "$LOG_DIR"
mv write "$LOG_DIR"

mv write_telemetry "$LOG_DIR"
mv read_telemetry "$LOG_DIR"


fusermount -zu "$MOUNTPOINT"
find "$STORAGES_DIR" -type d -exec fusermount -u {} -z \\;
rm -r "$STORAGES_DIR"/*
rm "$NUBISAVE_DIR"/splitter/database_of_splitter.*