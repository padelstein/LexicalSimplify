import sys

corpus = open(sys.argv[1])

for line in corpus:
	line = line.strip()
	print "\"" + line + "\"" + ",",
