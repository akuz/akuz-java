package me.akuz.nlp.topics;

import java.util.Arrays;
import java.util.List;

import me.akuz.nlp.corpus.Corpus;
import me.akuz.nlp.corpus.CorpusDoc;

import Jama.Matrix;

/**
 * Dynamic (changeable via setTemperature()) 
 * Alpha (document-topic) Dirichlet prior for LDA.
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
	
	public Matrix getTopicDocAlpha() {
		if (_mTopicDocAlpha == null) {
			throw new IllegalStateException("Prior not initialized, call setTemperature() first");
		}
		return _mTopicDocAlpha;
	}
	
	public double[] getSumDocAlpha() {
		if (_mSumDocAlpha == null) {
			throw new IllegalStateException("Prior not initialized, call setTemperature() first");
		}
		return _mSumDocAlpha;
	}
	
	public void setTemperature(double temperature) {

		Matrix mTopicDocAlpha = _mTopicDocAlpha;
		double[] mSumDocAlpha = _mSumDocAlpha;
		if (mTopicDocAlpha == null) {
			mTopicDocAlpha = new Matrix(_topics.size(), _docLengths.length);
			mSumDocAlpha = new double[_docLengths.length];
		} else {
			Arrays.fill(mSumDocAlpha, 0);
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
				double docTopicPriorMass = docPriorMass * topic.getCorpusFraction();
				mTopicDocAlpha.set(topicIndex, docIndex, docTopicPriorMass);
				mSumDocAlpha[docIndex] += docTopicPriorMass;
			}
		}
		
		_mTopicDocAlpha = mTopicDocAlpha;
		_mSumDocAlpha = mSumDocAlpha;
	}

}
