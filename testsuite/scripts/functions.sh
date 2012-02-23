function checksum {
        csum=`md5sum -b "$1" | cut -d " "  -f 1`
        sum=`md5sum -b "$2" | cut -d " "  -f 1`
        if [ "$csum" != "$sum" ]
        then
                echo "ERROR checksum"
        fi
}

function round {
	echo "$1" | python -c "print int(round(float(raw_input())))"
}

function get_db_line { 
    # get "average_cpu_load, max_cpu_load, average_mem_load, max_mem_load, max_swap_load, average_net_load, total_net_load" 
    # from prior execution of../scripts/start_net_mem_cpu_logging.sh, between start and end point given in seconds from the epoch
    # @param 1: start of file operation in seconds from the epoch
    # @param 2: stop of file operation in seconds from the epoch
    # @param 3: stop of network transfer in seconds from the epoch
    start=$1
    end=$2
    net_end=$3
    net_total=`gawk -v end=$net_end -v start=$start '(($1 >= start && $1 <= end) || ($1 >= start && first_line == 0)) {total+=$2+$3; first_line=1} END {print total}' "$TEMP_DIR"/netlog`
    net_avg=`gawk -v end=$net_end -v start=$start '(($1 >= start && $1 <= end) || ($1 >= start && n == 0)) {n++; total+=$2+$3} END {print total/n}' "$TEMP_DIR"/netlog`
    mem_max=`gawk -v end=$end -v start=$start 'BEGIN {max=0} (($1 >= start && $1 <= end && max < $2) || ($1 >= start && max == 0)) {max=$2} END {print max}' "$TEMP_DIR"/memlog`
    swap_max=`gawk -v end=$end -v start=$start 'BEGIN {max=0} (($1 >= start && $1 <= end && max < $3) || ($1 >= start && first_line == 0)) {max=$3; first_line=1} END {print max}' "$TEMP_DIR"/memlog`
    mem_avg=`gawk -v end=$end -v start=$start '(($1 >= start && $1 <= end) || ($1 >= start && n == 0)) {n++; total+=$2} END {print total/n}' "$TEMP_DIR"/memlog`
    cpu_max=`gawk -v end=$end -v start=$start 'BEGIN {max=0} (($1 >= start && $1 <= end && max < $2) || ($1 >= start && first_line == 0)) {max=$2; first_line=1} END {print max}' "$TEMP_DIR"/cpulog`
    cpu_avg=`gawk -v end=$end -v start=$start '(($1 >= start && $1 <= end) || ($1 >= start && n == 0)) {n++; total+=$2} END {print total/n}' "$TEMP_DIR"/cpulog`
    echo -n "$cpu_avg	$cpu_max	$mem_avg	$mem_max	$swap_max	$net_avg	$net_total"
}

function wait_until_transfer_is_complete {
    # Wait until a transfer of size $1 is complete and the network activity stagnates. Return the seconds needed to recognize the end of the network transfer, after it had already ended.
    # @param 1: size of transfer in MB
    # @param 2: log file with ifstat output, where the first column is the time in seconds from the epoch of when the line was output by ifstat
    # @param 3: the time in seconds from the epoch of when the file operation started
	transfered_KB=`gawk -v start=$3 'BEGIN {total=0} ($1 >= start) {total+=$2+$3} END {print total}' $2`
	transfered_KB=`round $transfered_KB`
	previous_transfered_KB=$transfered_KB
	stagnated=0
	while [[ ($transfered_KB -lt $(($1*1000)) || $stagnated -lt 10) && $stagnated -lt 20 ]];
	do
		if [ $(($previous_transfered_KB+5)) -gt $transfered_KB ];
		then
			let stagnated=stagnated+1
		else		
			stagnated=0
			previous_transfered_KB=$transfered_KB
		fi
		transfered_KB=`gawk -v start=$3 '($1 >= start) {total+=$2+$3} END {print total}' $2`
		transfered_KB=`round $transfered_KB`
		#echo $transfered_KB -lt $(($size*1000));
		#echo "waiting for network transfer to complete"
		sleep 1
	done
	echo $stagnated
}

