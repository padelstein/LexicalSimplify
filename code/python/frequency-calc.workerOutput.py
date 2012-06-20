
import sys

resultsFreq = {}
corpusFreq = {}
results = open(sys.argv[1])
corpus = open(sys.argv[2])

grossNumberOfWords = 0

#for line in results:
#	words_in_line = line.split(",")
#	for word in words_in_line:
#		word = word.strip().strip("]")
#		if resultsFreq.has_key(word):
#			resultsFreq[word] += 1
#		else:
#			resultsFreq[word] = 1

for line in corpus:
	(article, paragraph, text) = line.split("\t")
	words_in_line = text.lower().split()	
	for word in words_in_line:
		if corpusFreq.has_key(word):
			corpusFreq[word] += 1
		else:
			corpusFreq[word] = 1
	grossNumberOfWords += 1

#for key, value in sorted (resultsFreq.iteritems(), key = lambda (k,v): (v,k) ):
for line in results:
	(key, value) = line.split("\t")
	value = value.strip("\n")
	if not corpusFreq.has_key(key):
		corpusFreq[key] = 0	
	print value + "," + str(float(corpusFreq[key])/float(grossNumberOfWords)) 
