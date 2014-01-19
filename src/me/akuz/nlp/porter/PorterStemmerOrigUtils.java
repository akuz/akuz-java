package me.akuz.nlp.porter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Utilities for working with Porter stemmer (I didn't want to 
 * modify the original PorterStemmer class shared by the author).
 *
 */
public final class PorterStemmerOrigUtils {
	
	public final static String stem(PorterStemmerOrig porterStemmerOrig, String word) {
		return stem(porterStemmerOrig, word, null);
	}
	
	public final static String stem(PorterStemmerOrig porterStemmerOrig, String word, String suffix) {
		word = word.toLowerCase();
		for (int i=0; i<word.length(); i++) {
			porterStemmerOrig.add(word.charAt(i));
		}
		porterStemmerOrig.stem();
		if (suffix != null) {
			return String.format("%s%s", porterStemmerOrig.toString(), suffix);
		} else {
			return porterStemmerOrig.toString();
		}
	}
	
	public final static Set<String> stemAll(PorterStemmerOrig porterStemmerOrig, Collection<String> words) {
		return stemAll(porterStemmerOrig, words, null);
	}

	public final static Set<String> stemAll(PorterStemmerOrig porterStemmerOrig, Collection<String> words, String suffix) {
		Set<String> stems = new HashSet<String>(words.size());
		for (String word : words) {
			stems.add(stem(porterStemmerOrig, word, suffix));
		}
		return stems;
	}

}
