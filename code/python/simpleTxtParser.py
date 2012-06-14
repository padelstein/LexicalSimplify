import sys

doc1 = open(sys.argv[1])
doc2 = open(sys.argv[2])


words = {}

ogden = doc1.readline().split(",")

for word in ogden:
	word = word.strip()	
	words[word] = 0

for line in doc2:
	line = line.strip()
	words[line] = 0

for i in sorted(words.iterkeys()):
	print i

	
