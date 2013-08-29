#read $3 bytes at position $2 in file $1
#call read filename position bytes
import sys
if len(sys.argv) != 4:
	print "Read a number bytes at a position from a file."
	print "Usage: %s filename position_in_bytes nr_of_bytes_to_read" % (sys.argv[0])
with open(sys.argv[1],"rb") as f:
    f.seek(int(sys.argv[2]))
    for i in range(1,int(sys.argv[3])):
        f.read(1),
