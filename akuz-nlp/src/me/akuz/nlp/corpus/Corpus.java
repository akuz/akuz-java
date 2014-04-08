package me.akuz.nlp.corpus;

import java.util.ArrayList;
import java.util.List;

import me.akuz.core.Index;

/**
 * A corpus containing documents, prepared for use 
 * by a text analysis algorithm (such as LDA).
 *
 */
public final class Corpus {
	
	private final Index<String> _stemsIndex;
	private final Index<String> _wordsIndex;
	private final List<CorpusDoc> _docs;
	private int _placeCount;
	
	public Corpus(Index<String> stemsIndex, Index<String> wordsIndex) {
		_stemsIndex = stemsIndex;
		_wordsIndex = wordsIndex;
		_docs = new ArrayList<>();
	}
	
	public Index<String> getStemsIndex() {
		return _stemsIndex;
	}
	
	public Index<String> getWordsIndex() {
		return _wordsIndex;
	}
	
	public int getDocCount() {
		return _docs.size();
	}
	
	public int getPlaceCount() {
		return _placeCount;
	}
	
	public List<CorpusDoc> getDocs() {
		return _docs;
	}
	
	public void addDoc(CorpusDoc doc) {
		_placeCount += doc.getPlaceCount();
		_docs.add(doc);
	}

}
