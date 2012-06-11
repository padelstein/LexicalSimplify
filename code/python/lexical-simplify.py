# lexical-simplify.py
# substitutes words in input sentences
# Patrick Adelstein, Middlebury College Summer 2012, Prof. David Kauchak
# adapted from Sujay Kumar Jauhar

import sys

class Usage(Exception):
	def __init__(self, msg):
		self.msg = msg


def createFreqHash(file):
	wordFreq = {}

	for line in file:
		words_in_line = line.lower().split()	
		# should add code to remove titles in particular file(simple.txt)
		for word in words_in_line:
			if wordFreq.has_key(word):
				wordFreq[word] += 1
			else:
				wordFreq[word] = 1

	return wordFreq


#function to read system produced ranking file
def getSystemRankings(file):
	#pattern to recognize rankings in output file
	pattern = re.compile('.*?\{(.*?)\}(.*)')
	
	for line in file:
		rest = line
		counter = 0
		while rest:
			match = pattern.search(rest)
			currentRank = match.group(1)
			individualWords = currentRank.split(', ')
			for word in individualWords:		
				word = re.sub('\s$','',word)
				currentContextRanking[word] = counter
			rest = match.group(2)
			counter += 1
		
		allContextRankings.append(currentContextRanking)
		
	return allContextRankings

if __name__ == "__main__":

