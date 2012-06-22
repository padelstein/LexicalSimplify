# frequency-calc.py
# a word frequency calculator
# Patrick Adelstein, Middlebury College Summer 2012, Prof. David Kauchak

import sys

wordFreq = {}
corpus = open(sys.argv[1])
normalToTopSub = open(sys.argv[2])
simpleWordsHash = {}
cnt = 0
normFreq = 0
simpFreq = 0
numSent = 0


for line in corpus:
	(article, paragraph, text) = line.split("\t")
	words_in_line = text.lower().split()	
	for word in words_in_line:
		if wordFreq.has_key(word):
			wordFreq[word] += 1
		else:
			wordFreq[word] = 1

for line in normalToTopSub:
	(normal, topSub) = line.split("\t")
	topSub = topSub.strip();
	if wordFreq.has_key(topSub):
		topSubFreq = wordFreq[topSub]
	else:
		topSubFreq = 0
	print normal + "-" + topSub + ", " + str(wordFreq[normal]) + ", " + str(topSubFreq) + ", " + str(topSubFreq - wordFreq[normal])
