package me.akuz.nlp.topics;

import java.util.Arrays;
import java.util.List;

import me.akuz.nlp.corpus.Corpus;

import Jama.Matrix;

/**
 * Dynamic, changeable via setTemperature(), beta (topic-word) Dirichlet prior for LDA.
 *
 */
public final class LDAGibbsBeta {
	
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

	public double getStemTopicBeta(int stemIndex, int topicIndex) {
		if (_mStemTopicBeta == null) {
			throw new IllegalStateException(this.getClass().getSimpleName() + " not initialized, call setTemperature() first");
		}
		return _mStemTopicBeta.get(stemIndex, topicIndex);
	}

	public double getSumTopicBeta(int topicIndex) {
		if (_mSumTopicBeta == null) {
			throw new IllegalStateException(this.getClass().getSimpleName() + " not initialized, call setTemperature() first");
		}
		return _mSumTopicBeta[topicIndex];
	}

	public void setTemperature(double temperature) {
		
		if (_mStemTopicBeta == null) {
			_mStemTopicBeta = new Matrix(_corpusStemCount, _topics.size());
			_mSumTopicBeta = new double[_topics.size()];
		} else {
			Arrays.fill(_mSumTopicBeta, 0);
		}
		
		for (int topicIndex=0; topicIndex<_topics.size(); topicIndex++) {
			
			LDAGibbsTopic topic = _topics.get(topicIndex);
			
			// calculate topic prior mass based on expected posterior mass * temperature
			final double topicPriorMass = _corpusPlaceCount * topic.getProportion() * temperature;
			
			// calculate prior mass per "remaining" stem (not selected by topic)
			double priorityStemMass;
			double remainingStemMass;
			if (topic.getPriorityStemCount() > 0) {
				
				final double priorityMass = topicPriorMass * topic.getPriorityWordsProportion();
				priorityStemMass = priorityMass / topic.getPriorityStemCount();

				final double remainingMass = topicPriorMass * (1.0 - topic.getPriorityWordsProportion());
				int remainingStemCount = _corpusStemCount - topic.getPriorityStemCount();
				if (remainingStemCount > 0) {
					remainingStemMass = remainingMass / remainingStemCount;
				} else {
					throw new IllegalStateException("Number of priority stems in topic must be less than total number of stems in corpus");
				}
				
			} else {
				
				priorityStemMass = 0;
				remainingStemMass = topicPriorMass / _corpusStemCount;
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
				_mStemTopicBeta.set(stemIndex, topicIndex, stemPriorMass);
				_mSumTopicBeta[topicIndex] += stemPriorMass;
			}
		}
	}

}
