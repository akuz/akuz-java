package me.akuz.nlp.corpus;

import java.util.ArrayList;
import java.util.List;

/**
 * A corpus document, containing a list of word-places 
 * corresponding to all word occurrences in the document
 * and a tag, which can be used by an algorithm to attach
 * extra information to this document.
 *
 */
public final class CorpusDoc {
	
	private final List<CorpusPlace> _places;
	private Object _tag;
	
	public CorpusDoc() {
		_places = new ArrayList<>();
	}
	
	public int getPlaceCount() {
		return _places.size();
	}
	
	public List<CorpusPlace> getPlaces() {
		return _places;
	}
	
	public void addPlace(CorpusPlace place) {
		_places.add(place);
	}

	public Object getTag() {
		return _tag;
	}
	
	public void setTag(Object tag) {
		_tag = tag;
	}
}
