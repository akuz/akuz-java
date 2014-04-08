package me.akuz.nlp.corpus;

/**
 * A word-place occurrence in a list-corpus document,
 * containing stem index, word index, and a tag,
 * which can be used by an algorithm to attach
 * extra information to this word-place.
 *
 */
public final class CorpusPlace {
	
	private final int _stemIndex;
	private final int _wordIndex;
	private Object _tag;
	
	public CorpusPlace(int stemIndex, int wordIndex) {
		_stemIndex = stemIndex;
		_wordIndex = wordIndex;
	}
	
	public int getStemIndex() {
		return _stemIndex;
	}
	
	public int getWordIndex() {
		return _wordIndex;
	}

	public Object getTag() {
		return _tag;
	}
	
	public void setTag(Object tag) {
		_tag = tag;
	}
}
