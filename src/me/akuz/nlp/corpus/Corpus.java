package me.akuz.nlp.corpus;

import java.util.ArrayList;
import java.util.List;

/**
 * A corpus containing documents, prepared for use 
 * by a text analysis algorithm (such as LDA).
 *
 */
public final class Corpus {
	
	private final List<CorpusDoc> _docs;
	private int _placeCount;
	
	public Corpus() {
		_docs = new ArrayList<>();
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
