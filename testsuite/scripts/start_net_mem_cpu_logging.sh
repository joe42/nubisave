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
NOT_IN_PROCESS_NAME='start_net_mem_cpu_logging.sh|test_normal_files|/usr/bin/time|test_sparse_files|cp '
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
   smem -c "command pss swap" | grep "^`pgrep -lf $PROCESS_NAME| grep -Ev $NOT_IN_PROCESS_NAME | cut -d' ' -f1`" | gawk '{print systime()" "$0 }'|tail -n1 >> "$MEMORY_LOG"
   ps aux| grep `pgrep -lf "$PROCESS_NAME"| grep -Ev "$NOT_IN_PROCESS_NAME" | cut -d" " -f1` | grep -v grep | gawk '{print systime()" "$3}' >>"$CPU_LOG"
   sleep 1
done 
