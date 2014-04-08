package me.akuz.nlp.topics;

import java.util.Arrays;
import java.util.List;

import me.akuz.nlp.corpus.Corpus;
import me.akuz.nlp.corpus.CorpusDoc;

import Jama.Matrix;

/**
 * Dynamic, changeable via setTemperature(), alpha (document-topic) Dirichlet prior for LDA.
 *
 */
public final class LDAGibbsAlpha {
	
	private final List<LDAGibbsTopic> _topics;
	private final int[] _docLengths;
	private Matrix _mTopicDocAlpha;
	private double[] _mSumDocAlpha;
	
	public LDAGibbsAlpha(
			Corpus corpus,
			List<LDAGibbsTopic> topics) {
		
		_topics = topics;
		_docLengths = new int[corpus.getDocCount()];
		
		List<CorpusDoc> docs = corpus.getDocs();
		for (int docIndex=0; docIndex<docs.size(); docIndex++) {
			
			CorpusDoc doc = docs.get(docIndex);
			int docLength = doc.getPlaceCount();
			_docLengths[docIndex] = docLength;
		}
	}
	
	public double getTopicDocAlpha(int topicIndex, int docIndex) {
		if (_mTopicDocAlpha == null) {
			throw new IllegalStateException(this.getClass().getSimpleName() + " not initialized, call setTemperature() first");
		}
		return _mTopicDocAlpha.get(topicIndex, docIndex);
	}
	
	public double getSumDocAlpha(int docIndex) {
		if (_mSumDocAlpha == null) {
			throw new IllegalStateException(this.getClass().getSimpleName() + " not initialized, call setTemperature() first");
		}
		return _mSumDocAlpha[docIndex];
	}
	
	public void setTemperature(double temperature) {

		if (_mTopicDocAlpha == null) {
			_mTopicDocAlpha = new Matrix(_topics.size(), _docLengths.length);
			_mSumDocAlpha = new double[_docLengths.length];
		} else {
			Arrays.fill(_mSumDocAlpha, 0);
		}
		
		for (int docIndex=0; docIndex<_docLengths.length; docIndex++) {
			
			int docLength = _docLengths[docIndex];
			
			// calculate total prior mass to distribute between topics
			// based on the total posterior available * temperature,
			// and adjusted for the number of topics in this doc
			final double docPriorMass = docLength * temperature;
			
			// distribute prior mass to the topics
			// based on their expected corpus places fraction
			for (int topicIndex=0; topicIndex<_topics.size(); topicIndex++) {
				
				LDAGibbsTopic topic = _topics.get(topicIndex);
				double docTopicPriorMass = docPriorMass * topic.getProportion();
				_mTopicDocAlpha.set(topicIndex, docIndex, docTopicPriorMass);
				_mSumDocAlpha[docIndex] += docTopicPriorMass;
			}
		}
	}

}
