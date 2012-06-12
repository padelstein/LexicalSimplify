# frequency-calc.py
# a word frequency calculator
# Patrick Adelstein, Middlebury College Summer 2012, Prof. David Kauchak

import sys

wordFreq = {}
doc = open(sys.argv[1])

for line in doc:
	(article, paragraph, text) = line.split("\t")
	words_in_line = text.lower().split()	
	for word in words_in_line:
		if wordFreq.has_key(word):
			wordFreq[word] += 1
		else:
			wordFreq[word] = 1

for word in wordFreq:
	print word + "\t" + str(wordFreq[word])
