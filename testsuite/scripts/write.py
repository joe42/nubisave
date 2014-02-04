#write $3 bytes at position $2 in file $1
#call write filename position bytes
import sys
if len(sys.argv) != 4:
	print "Write a number of \"\\x61\" bytes at a position to a file."
	print "Usage: %s filename position_in_bytes nr_of_bytes_to_write" % (sys.argv[0])
with open(sys.argv[1],"r+b") as f:
    f.seek(int(sys.argv[2]))
    f.write("\x61"*int(sys.argv[3]));
