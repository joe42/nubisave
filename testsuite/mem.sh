while :
do
   sleep 1
   smem -M `pgrep -f com.github.joe42.splitter.Main` -c "command pss swap" | gawk -v time=`date +"%s"` '{print $0 time }' >> "$1"
done 