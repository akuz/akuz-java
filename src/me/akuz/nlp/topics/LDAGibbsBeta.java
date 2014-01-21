package me.akuz.nlp.topics;

import java.util.Arrays;
import java.util.List;

import me.akuz.nlp.corpus.Corpus;

import Jama.Matrix;

/**
 * Dynamic (changeable via setTemperature()) 
 * Beta (topic-word) Dirichlet prior for LDA.
 *
 */
public final class LDAGibbsBeta {
	
	private double _priorityMassFraction = 0.5;
	private double _excludedMassMultiplier = 0.00000001;
	private final List<LDAGibbsTopic> _topics;
	private final int _corpusStemCount;
	private final int _corpusPlaceCount;
	private Matrix _mStemTopicBeta;
	private double[] _mSumTopicBeta;

	public LDAGibbsBeta(
			Corpus corpus,
			List<LDAGibbsTopic> topics) {
		
		_topics = topics;
		_corpusStemCount = corpus.getStemsIndex().size();
		_corpusPlaceCount = corpus.getPlaceCount();
	}
	
	public double getPriorityMassFraction() {
		return _priorityMassFraction;
	}

	public void setPriorityMassFraction(double fraction) {
		if (fraction <= 0) {
			throw new IllegalArgumentException("Priority mass fraction must be positive");
		}
		if (fraction >= 1) {
			throw new IllegalArgumentException("Priority mass fraction must be less than one");
		}
		_priorityMassFraction = fraction;
	}
	
	public double getExcludedMassMultiplier() {
		return _excludedMassMultiplier;
	}

	public void setExcludedMassMultiplier(double multiplier) {
		if (multiplier <= 0) {
			throw new IllegalArgumentException("Excluded mass multiplier must be positive");
		}
		if (multiplier >= 1) {
			throw new IllegalArgumentException("Excluded mass multiplier must be less than one");
		}
		_excludedMassMultiplier = multiplier;
	}

	public Matrix getStemTopicBeta() {
		if (_mStemTopicBeta == null) {
			throw new IllegalStateException("Prior not initialized, call setTemperature() first");
		}
		return _mStemTopicBeta;
	}

	public double[] getSumTopicBeta() {
		if (_mSumTopicBeta == null) {
			throw new IllegalStateException("Prior not initialized, call setTemperature() first");
		}
		return _mSumTopicBeta;
	}

	public void setTemperature(double temperature) {
		
		Matrix mStemTopicBeta = _mStemTopicBeta;
		double[] mSumTopicBeta = _mSumTopicBeta;
		
		if (mStemTopicBeta == null) {
			mStemTopicBeta = new Matrix(_corpusStemCount, _topics.size());
			mSumTopicBeta = new double[_topics.size()];
		} else {
			Arrays.fill(mSumTopicBeta, 0);
		}
		
		for (int topicIndex=0; topicIndex<_topics.size(); topicIndex++) {
			
			LDAGibbsTopic topic = _topics.get(topicIndex);
			
			// calculate topic prior mass based on expected posterior mass * temperature
			double priorMass = _corpusPlaceCount * topic.getCorpusFraction() * temperature;
			
			// calculate prior mass per "remaining" stem (not selected by topic)
			double priorityStemMass;
			double remainingStemMass;
			if (topic.getPriorityStemCount() > 0) {
				
				final double priorityMass = priorMass * _priorityMassFraction;
				priorityStemMass = priorityMass / topic.getPriorityStemCount();

				final double remainingMass = priorMass * (1.0 - _priorityMassFraction);
				int remainingStemCount = _corpusStemCount - topic.getPriorityStemCount();
				if (remainingStemCount > 0) {
					remainingStemMass = remainingMass / remainingStemCount;
				} else {
					throw new IllegalStateException("Number of priority stems in topic must be less than total number of stems in corpus");
				}
				
			} else {
				
				priorityStemMass = 0;
				remainingStemMass = priorMass / _corpusStemCount;
			}
			
			// distribute prior mass to stems
			for (Integer stemIndex=0; stemIndex<_corpusStemCount; stemIndex++) {
				
				double stemPriorMass;

				if (priorityStemMass > 0 && 
					topic.isPriorityStem(stemIndex)) {
					stemPriorMass = priorityStemMass;
				} else {
					stemPriorMass = remainingStemMass;
					if (topic.isExcludedStem(stemIndex)) {
						stemPriorMass /= _excludedMassMultiplier;
					}
				}
				mStemTopicBeta.set(stemIndex, topicIndex, stemPriorMass);
				mSumTopicBeta[topicIndex] += stemPriorMass;
			}
		}
		
		_mStemTopicBeta = mStemTopicBeta;
		_mSumTopicBeta = mSumTopicBeta;
	}

}
