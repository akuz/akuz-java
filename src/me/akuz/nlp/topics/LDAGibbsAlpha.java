package me.akuz.nlp.topics;

import java.util.Arrays;
import java.util.List;

import me.akuz.core.math.StatsUtils;
import me.akuz.nlp.corpus.Corpus;
import me.akuz.nlp.corpus.CorpusDoc;

import Jama.Matrix;

/**
 * Builds alpha priors for LDA; call setTemperature() to initialize.
 *
 */
public final class LDAGibbsAlpha {
	
	private final List<LDAGibbsTopic> _topics;
	private final int[] _docLengths;
	private final double[] _docExpectedTopicCounts;
	private Matrix _mTopicDocAlpha;
	private double[] _mSumDocAlpha;
	
	public LDAGibbsAlpha(
			Corpus corpus,
			List<LDAGibbsTopic> topics,
			double docMinTopicCount,
			double docLengthForFirstExtraTopic) {
		
		_topics = topics;
		
		_docLengths = new int[corpus.getDocCount()];
		_docExpectedTopicCounts = new double[corpus.getDocCount()];
		
		List<CorpusDoc> docs = corpus.getDocs();
		for (int docIndex=0; docIndex<docs.size(); docIndex++) {
			
			CorpusDoc doc = docs.get(docIndex);
			int docLength = doc.getPlaceCount();
			
			_docLengths[docIndex] = docLength;
			
			// calculate number of topics we expect encounter
			// in this document based on the document length
			double docExpectedTopicCount = docMinTopicCount + 
					StatsUtils.log2(
							1.0 + 
							(double)docLength/
							(double)docLengthForFirstExtraTopic);
			
			_docExpectedTopicCounts[docIndex] = docExpectedTopicCount;
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
			double docExpectedTopicCount = _docExpectedTopicCounts[docIndex];
			
			// calculate total prior mass to distribute between topics
			// based on the total posterior available * temperature,
			// and adjusted for the number of topics in this doc
			double priorMass = docLength * temperature * docExpectedTopicCount;
			
			// distribute prior mass to the topics
			// based on their expected corpus places fraction
			for (int topicIndex=0; topicIndex<_topics.size(); topicIndex++) {
				
				LDAGibbsTopic topic = _topics.get(topicIndex);
				double topicPriorMass = priorMass * topic.getTargetCorpusFraction();
				mTopicDocAlpha.set(topicIndex, docIndex, topicPriorMass);
				mSumDocAlpha[docIndex] += topicPriorMass;
			}
		}
		
		_mTopicDocAlpha = mTopicDocAlpha;
		_mSumDocAlpha = mSumDocAlpha;
	}

}
