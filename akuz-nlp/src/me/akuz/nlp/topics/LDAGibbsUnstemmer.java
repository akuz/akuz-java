package me.akuz.nlp.topics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import me.akuz.core.Pair;
import me.akuz.nlp.corpus.Corpus;
import me.akuz.nlp.corpus.CorpusDoc;
import me.akuz.nlp.corpus.CorpusPlace;

/**
 * Unstemmer for the LDA algo, for collecting topic-specific
 * frequencies of words-by-stem during sampling process.
 *
 */
public final class LDAGibbsUnstemmer {
	
	private final Corpus _corpus;
	private final Map<Integer, Map<Integer, List<Pair<Integer, Integer>>>> _topicStemWordCounts;
	private Map<Integer, Map<Integer, Integer>> _optimizedTopicStemWord;
	
	public LDAGibbsUnstemmer(Corpus corpus) {
		_corpus = corpus;
		_topicStemWordCounts = new HashMap<Integer, Map<Integer,List<Pair<Integer,Integer>>>>();
	}
	
	public void sample() {

		List<CorpusDoc> docs = _corpus.getDocs();
		
		// gather original word counts by topic
		for (int docIndex=0; docIndex<docs.size(); docIndex++) {
			
			CorpusDoc doc = docs.get(docIndex);
			List<CorpusPlace> places = doc.getPlaces();
			
			for (int placeIndex=0; placeIndex<places.size(); placeIndex++) {
				
				CorpusPlace place = places.get(placeIndex);
				
				int stemIndex = place.getStemIndex();
				int wordIndex = place.getWordIndex();
				
				int[] tag = (int[])place.getTag();
				if (tag == null) {
					throw new IllegalStateException("No topic assigned to the corpus place, please iterate first");
				}
				
				int topicIndex = tag[0];
				
				Map<Integer, List<Pair<Integer, Integer>>> stemWordCountsMap = 
					_topicStemWordCounts.get(topicIndex);
				
				if (stemWordCountsMap == null) {
					stemWordCountsMap = new HashMap<Integer, List<Pair<Integer,Integer>>>();
					_topicStemWordCounts.put(topicIndex, stemWordCountsMap);
				}
				
				List<Pair<Integer, Integer>> stemWordCounts = 
					stemWordCountsMap.get(stemIndex);
				
				if (stemWordCounts == null) {
					stemWordCounts = new ArrayList<Pair<Integer,Integer>>(2);
					stemWordCountsMap.put(stemIndex, stemWordCounts);
				}
				
				boolean found = false;
				for (int i=0; i<stemWordCounts.size(); i++) {
					Pair<Integer, Integer> pair = stemWordCounts.get(i);
					if (pair.v1() == wordIndex) {
						stemWordCounts.set(i, new Pair<Integer, Integer>(wordIndex, pair.v2() + 1));
						found = true;
						break;
					}
				}
				
				if (!found) {
					stemWordCounts.add(new Pair<Integer, Integer>(wordIndex, 1));
				}
			}
		}		
	}
	
	public void optimize() {
		if (_optimizedTopicStemWord != null) {
			throw new IllegalStateException("Already optimized");
		}
		// select most frequent original words
		_optimizedTopicStemWord = new HashMap<Integer, Map<Integer,Integer>>();
		Iterator<Integer> topicIndex_i = _topicStemWordCounts.keySet().iterator();
		while (topicIndex_i.hasNext()) {
			Integer topicIndex = topicIndex_i.next();
			Map<Integer, List<Pair<Integer, Integer>>> stemWordCountsMap = _topicStemWordCounts.get(topicIndex);
			Map<Integer, Integer> stemWordMap = new HashMap<Integer, Integer>();
			_optimizedTopicStemWord.put(topicIndex, stemWordMap);
			
			Iterator<Integer> stemIndexIterator = stemWordCountsMap.keySet().iterator();
			while (stemIndexIterator.hasNext()) {
				
				Integer stemIndex = stemIndexIterator.next();
				List<Pair<Integer, Integer>> stemWordCounts = stemWordCountsMap.get(stemIndex);
				
				Integer maxCount = null;
				Integer wordIndex = null;
				for (int i=0; i<stemWordCounts.size(); i++) {
					Pair<Integer, Integer> pair = stemWordCounts.get(i);
					if (maxCount == null || maxCount < pair.v2()) {
						maxCount = pair.v2();
						wordIndex = pair.v1();
					}
				}
				
				stemWordMap.put(stemIndex, wordIndex);
			}
		}		
	}
	
	public Integer getWordIndex(int topicIndex, int stemIndex) {
		if (_optimizedTopicStemWord == null) {
			throw new IllegalStateException("Not optimized yet");
		}
		Map<Integer, Integer> stemWordMap = _optimizedTopicStemWord.get(topicIndex);
		if (stemWordMap != null) {
			return stemWordMap.get(stemIndex);
		}
		return null;
	}
}
