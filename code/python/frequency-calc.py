# frequency-calc.py
# a word frequency calculator
# Patrick Adelstein, Middlebury College Summer 2012, Prof. David Kauchak

import sys

wordFreq = {}
corpus = open(sys.argv[1])
normalToSimple = open(sys.argv[2])
simpleWords = open(sys.argv[3])
simpleWordsHash = {}
cnt = 0
normFreq = 0
simpFreq = 0
numSent = 0


for line in simpleWords:
	line = line.strip()
	simpleWordsHash[line] = 0

for line in corpus:
	(article, paragraph, text) = line.split("\t")
	words_in_line = text.lower().split()	
	for word in words_in_line:
		if wordFreq.has_key(word):
			wordFreq[word] += 1
		else:
			wordFreq[word] = 1

for line in normalToSimple:
	(context, index, normal, simple) = line.split("\t")
	simple = simple.strip('\n').strip()
	normal = normal.lower()
	simple = simple.lower()
	if wordFreq.has_key(simple) and not simpleWordsHash.has_key(normal):
		if not wordFreq.has_key(normal):
			wordFreq[normal] = 0		
#		numSent += 1
		print context + "\t" + index + "\t" + normal + "\t" + simple		
		#if wordFreq[simple] > wordFreq[normal]:
		#	cnt += 1
		#normFreq += wordFreq[normal]
		#simpFreq += wordFreq[simple]
	#else:
	#	print normal + ' ' + simple

#normFreq = float(normFreq)/numSent
#simpFreq = float(simpFreq)/numSent

# print 'The number of good substitutions is ' + str(cnt) + ' and the frequency of normal words is ' + str(normFreq) + ' and the frequency of simple words is ' + str(simpFreq) + ' out of ' + str(numSent)
