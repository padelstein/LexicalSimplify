package simplify.readability;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * Readability and general measurements of English text.
 * </p>
 * 
 * <p>
 * Ported from perl's Lingua::EN::Fathom by Kim Ryan.
 * </p>
 * 
 * <p>
 * <b>COPYRIGHT</b><br>
 * Distributed under the same terms as Perl.
 * </p>
 * 
 * @author Kim Ryan
 * @author Larry Ogrodnek &lt;ogrodnek@gmail.com&gt;
 * 
 * @version $Revision: 1.2 $ $Date: 2007-10-22 01:28:51 $
 */
public final class Fathom {
	private static final String[] abbreviations = new String[] {
		// personal titles
		"Mr", "Mrs", "M", "Dr", "Prof", "Det", "Insp",
		// Commercial abbreviations
		"Pty", "PLC", "Ltd", "Inc",
		// Other abbreviations
		"etc", "vs", };

	public static ReadabilityStatsOld analyze(final File f) throws IOException {
		return analyze(new FileInputStream(f));
	}

	public static ReadabilityStatsOld analyze(final InputStream s) throws IOException {
		return analyze(new InputStreamReader(s));
	}

	public static ReadabilityStatsOld analyze(final Reader r) throws IOException {
		final BufferedReader br = new BufferedReader(r);

		try {
			final ReadabilityStatsOld ret = new ReadabilityStatsOld();

			String line;

			while ((line = br.readLine()) != null) {
				analyzeWords(ret, line);
			}

			return ret;
		} finally {
			br.close();
		}
	}

	public static ReadabilityStatsOld analyze(final String s) {
		return analyzeWords(new ReadabilityStatsOld(), s);
	}

	public static ReadabilityStatsOld analyze(final ReadabilityStatsOld ReadabilityStats, final String s) {
		return analyzeWords(ReadabilityStats, s);
	}

	// Word found, such as: twice, BOTH, a, I'd, non-plussed ..
	// Ignore words like K12, &, X.Y.Z ...

	private static final Pattern WORD_PAT = Pattern.compile("\\b([a-z][-'a-z]*)\\b");

	private static final Pattern VOWELS = Pattern.compile("[aeiouy]");

	private static final Pattern VALID_HYPHENS = Pattern.compile("[a-z]{2,}-[a-z]{2,}");

	private static final Pattern END_SENTENCE = Pattern.compile("\\b\\s*[.!?]\\s*\\b");

	private static final Pattern END_SENTENCE_END_LINE = Pattern.compile("\\b\\s*[.!?]\\s*$");

	private static ReadabilityStatsOld analyzeWords(final ReadabilityStatsOld _ReadabilityStats, final String _s) {
		final ReadabilityStatsOld stats = _ReadabilityStats == null ? new ReadabilityStatsOld() : _ReadabilityStats;

		String s = _s.toLowerCase().trim();

		Matcher m = WORD_PAT.matcher(s);

		while (m.find()) {
			final String word = m.group(1);

			// Try to filter out acronyms and abbreviations by accepting
			// words with a vowel sound. This won't work for GPO etc.
			if (!VOWELS.matcher(word).find()) {
				continue;
			}

			// Test for valid hyphenated word like be-bop
			if (word.indexOf('-') > 0 && (!VALID_HYPHENS.matcher(word).matches())) {
				continue;
			}

			// word frequency count
			stats.addWord(word);

			final int syl = Syllable.syllable(word);

			//stats.numSyllables += syl;
			stats.setNumSyllables(stats.getNumSyllables() + syl);
			
			// Required for Fog index, count non hyphenated words of 3 or more
			// syllables. Should add check for proper names in here as well
			if (syl > 2 && word.indexOf('-') < 0) {
				//stats.numComplexWords++;
				stats.setNumComplexWords(stats.getNumComplexWords() + 1);
			}
		}

		// replace common abbreviations to ease the search for end of sentence.
		s = replaceAbbr(s);

		// clean out quotes for same reason
		s.replaceAll("[\"']", "");

		// Search for '.', '?' or '!' to end a sentence.
		m = END_SENTENCE.matcher(s);

		while (m.find()) {
			//stats.numSentences++;
			stats.setNumSentences( stats.getNumSentences() + 1);
		}

		// Check for final sentence, with no following words.
		m = END_SENTENCE_END_LINE.matcher(s);

		if (m.find()) {
			//stats.numSentences++;
			stats.setNumSentences( stats.getNumSentences() + 1);
		}

		return stats;
	}

	private static final String replaceAbbr(final String s) {
		String ret = s;

		for (final String a : abbreviations) {
			ret = ret.replaceAll("\\s" + a + "\\.\\s", a);
		}

		return ret;
	}
}

