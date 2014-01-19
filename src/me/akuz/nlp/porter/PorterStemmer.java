package me.akuz.nlp.porter;

import java.util.Collection;
import java.util.Set;

/**
 * Porter stemmer wrapper around the original implementation.
 * Allows appending a specified suffix to all stems, if needed.
 *
 */
public final class PorterStemmer {
	
	private final String _suffix;
	private final PorterStemmerOrig _ps;
	
	public PorterStemmer() {
		this(null);
	}

	public PorterStemmer(String suffix) {
		_suffix = suffix;
		_ps = new PorterStemmerOrig();
	}
	
	/**
	 * Stems one word.
	 * 
	 */
	public String stem(String word) {
		return PorterStemmerOrigUtils.stem(_ps, word, _suffix);
	}
	
	/**
	 * Stems many words at once.
	 * 
	 */
	public Set<String> stemAll(Collection<String> words) {
		return PorterStemmerOrigUtils.stemAll(_ps, words, _suffix);
	}

}
