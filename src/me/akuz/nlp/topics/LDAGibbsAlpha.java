package me.akuz.nlp.topics;

import java.util.Arrays;
import java.util.List;

import me.akuz.core.math.StatsUtils;
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
	private final int[] _docPlaceCounts;
	private final double[] _docTargetTopicCounts;
	private Matrix _mTopicDocAlpha;
	private double[] _mSumDocAlpha;
	
	public LDAGibbsAlpha(
			Corpus corpus,
			List<LDAGibbsTopic> topics,
			double docMinTopicCount,
			double docExtraTopicPlaceCount) {
		
		_topics = topics;
		
		_docPlaceCounts = new int[corpus.getDocCount()];
		_docTargetTopicCounts = new double[corpus.getDocCount()];
		
		List<CorpusDoc> docs = corpus.getDocs();
		for (int docIndex=0; docIndex<docs.size(); docIndex++) {
			
			CorpusDoc doc = docs.get(docIndex);
			int docLength = doc.getPlaceCount();
			
			_docPlaceCounts[docIndex] = docLength;
			
			// calculate number of topics we expect encounter
			// in this document based on the document length
			double docTargetTopicCount = docMinTopicCount + 
					StatsUtils.log2(
							1.0 + 
							(double)docLength/
							(double)docExtraTopicPlaceCount);
			
			_docTargetTopicCounts[docIndex] = docTargetTopicCount;
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
			mTopicDocAlpha = new Matrix(_topics.size(), _docPlaceCounts.length);
			mSumDocAlpha = new double[_docPlaceCounts.length];
		} else {
			Arrays.fill(mSumDocAlpha, 0);
		}
		
		for (int docIndex=0; docIndex<_docPlaceCounts.length; docIndex++) {
			
			int docPlaceCount = _docPlaceCounts[docIndex];
			double docTargetTopicCount = _docTargetTopicCounts[docIndex];
			
			// calculate total prior mass to distribute between topics
			// based on the total posterior available * temperature,
			// and adjusted for the number of topics in this doc
			double priorMass = docPlaceCount * temperature * docTargetTopicCount;
			
			// distribute prior mass to the topics
			// based on their expected corpus places fraction
			for (int topicIndex=0; topicIndex<_topics.size(); topicIndex++) {
				
				LDAGibbsTopic topic = _topics.get(topicIndex);
				double topicPriorMass = priorMass * topic.getCorpusFraction();
				mTopicDocAlpha.set(topicIndex, docIndex, topicPriorMass);
				mSumDocAlpha[docIndex] += topicPriorMass;
			}
		}
		
		_mTopicDocAlpha = mTopicDocAlpha;
		_mSumDocAlpha = mSumDocAlpha;
	}

}
