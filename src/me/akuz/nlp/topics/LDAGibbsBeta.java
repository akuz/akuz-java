package me.akuz.nlp.topics;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.akuz.nlp.corpus.Corpus;

import Jama.Matrix;

/**
 * Builds beta priors for LDA; call setTemperature() to initialize.
 *
 */
public final class LDAGibbsBeta {
	
	private static final double EXCLUDED_STEM_MASS_FRACTION_MULTIPLIER = 0.0000001;
	
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
			double priorMass = _corpusPlaceCount * topic.getTargetCorpusFraction() * temperature;
			
			// calculate prior mass per "remaining" stem (not selected by topic)
			Map<Integer, Double> priorityStemsMassFractions = topic.getPriorityStemMassMap();
			Set<Integer> excludedStems = topic.getExcludedStems();
			int remainingStemsCount = _corpusStemCount - priorityStemsMassFractions.size();
			if (remainingStemsCount <= 0) {
				throw new IllegalStateException("Number of priority stems in topic must be < total number of stems in corpus");
			}
			double remainingStemMassFraction = (1.0 - topic.getSumPriorityStemMass()) / remainingStemsCount;

			// distribute prior mass to stems
			for (int stemIndex=0; stemIndex<_corpusStemCount; stemIndex++) {
				
				double stemPriorMass;
				Double priorityStemMassFraction = priorityStemsMassFractions.get(stemIndex);
				if (priorityStemMassFraction != null) {
					stemPriorMass = priorMass * priorityStemMassFraction;
				} else {
					if (excludedStems.contains(stemIndex)) {
						stemPriorMass = priorMass * remainingStemMassFraction * EXCLUDED_STEM_MASS_FRACTION_MULTIPLIER;
					} else {
						stemPriorMass = priorMass * remainingStemMassFraction;
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
