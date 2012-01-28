#!/bin/bash
if [ $# -lt 2 ]; then
   echo
   echo "Not enough arguments."
   echo
   echo
   echo Log overall network usage, as well as process specific memory and cpu usage to one of log_directory/{memlog,netlog,cpulog}
   echo Usage: `basename $0` process_name log_directory
   echo Example: `basename $0` Wuala ~/logs
   echo
   exit
fi  

PROCESS_NAME="$1"
NOT_IN_PROCESS_NAME='start_net_mem_cpu_logging.sh|../scripts|/usr/bin/time|cp '
MEMORY_LOG="$2"/memlog
NETWORK_LOG="$2"/netlog
CPU_LOG="$2"/cpulog
mkdir -p "$2"


ifstat -z 1/1 | gawk '( !/KB|eth/ ) {print systime()" "$1" "$2; fflush()}' >> "$NETWORK_LOG"&

while :
do

#echo processes: 
#pgrep -lf "$PROCESS_NAME"| grep -Ev $NOT_IN_PROCESS_NAME
#echo end
   pid=`pgrep -lf $PROCESS_NAME| grep -Ev $NOT_IN_PROCESS_NAME | cut -d' ' -f1`
   smem -c "pid pss swap" | gawk -v pid=$pid '(pid == $1) {print systime()" "$2" "$3 }' >> "$MEMORY_LOG"
   ps aux| gawk -v pid=$pid '(pid == $2) {print systime()" "$3}' >>"$CPU_LOG"
done 
