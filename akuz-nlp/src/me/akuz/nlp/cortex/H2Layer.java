package me.akuz.nlp.cortex;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import me.akuz.core.logs.LocalMonitor;
import me.akuz.core.logs.Monitor;
import me.akuz.core.math.StatsUtils;

public final class H2Layer {
	
	private final double HDP_ALPHA = 1.0;
	private final double LOG_INSURANCE = 1e-10;
	private final double FEATURE_LEG_ALPHA = 0.1;
	private final double FEATURE_LEG_ALPHA_INIT_NOISE = 0.02;
	
	private final int _featureDim;
	private final List<PWord> _data;
	private final int _dataDim;
	
	// input data expressed through the
	// probabilities of inferred features
	// at each input document and position
	private List<PWord> _dataFeatures;
	
	// probabilities of inferred features
	private double[] _featureProbs;
	
	// inferred features infos
	private H2Feature[] _features;
	
	// parent layer, if any
	private H2Layer _parent;
	
	public H2Layer(
			final int featureDim,
			final List<PWord> data,
			final int dataDim) {
		
		_featureDim = featureDim;
		_data = data;
		_dataDim = dataDim;
		
		_dataFeatures = new ArrayList<>(data.size());
		for (int i=0; i<data.size(); i++) {
			final PWord item = data.get(i);
			final PWord itemFeatures;
			if (item.size() > 1) {
				itemFeatures = new PWord(_featureDim, item.size() - 1);
			} else {
				itemFeatures = new PWord(_featureDim, 0);
			}
			_dataFeatures.add(itemFeatures);
		}
	}
	
	public List<PWord> getData() {
		return _data;
	}
	
	public List<PWord> getDataFeatures() {
		return _dataFeatures;
	}
	
	public int getDataDim() {
		return _dataDim;
	}
	
	public int getFeatureDim() {
		return _featureDim;
	}
	
	public double[] getFeatureProbs() {
		return _featureProbs;
	}
	
	public H2Feature[] getFeatures() {
		return _features;
	}

	/**
	 * Set parent layer, which improves inference of the 
	 * features of this layer by taking into account 
	 * the features inferred at the parent level...
	 * 
	 */
	public void setParent(final H2Layer parent) {
		if (_parent != null) {
			throw new IllegalStateException("Parent already set");
		}
		if (parent.getData().size() != _data.size()) {
			throw new IllegalArgumentException("Parent and child data sizes don't match");
		}
		if (parent.getDataDim() != _featureDim) {
			throw new IllegalArgumentException("Parent data dim doesn't match child feature dim");
		}
		_parent = parent;
	}

	/**
	 * Execute EM inference until the max number of
	 * iterations is reached or log-likelihood has 
	 * converged as specified by the parameters.
	 * 
	 */
	public void execute(
			final Monitor parentMonitor,
			final int maxIterationCount,
			final double logLikeChangeThreshold) {
		
		if (maxIterationCount <= 0) {
			throw new IllegalArgumentException("Max iteration count must be positive");
		}
		if (logLikeChangeThreshold <= 0) {
			throw new IllegalArgumentException("LogLike change threshold must be positive");
		}

		final Monitor monitor = parentMonitor == null ? null : new LocalMonitor(this.getClass().getSimpleName(), parentMonitor);
		final DecimalFormat fmt = new DecimalFormat("' '0.00000000;'-'0.00000000");
		final Random rnd = ThreadLocalRandom.current();
		
		double[] currFeatureProbs = new double[_featureDim];
		Arrays.fill(currFeatureProbs, 1.0 / _featureDim);
		
		double[] nextFeatureProbs = new double[_featureDim];
		Arrays.fill(nextFeatureProbs, 0);
		
		H2Feature[] currFeatures = new H2Feature[_featureDim];
		for (int k=0; k<_featureDim; k++) {
			final H2Feature feature = new H2Feature(_dataDim, FEATURE_LEG_ALPHA);
			currFeatures[k] = feature;
			for (int d=0; d<_dataDim; d++) {
				feature.getLeft().addObservation(d, FEATURE_LEG_ALPHA_INIT_NOISE * rnd.nextDouble());
				feature.getRight().addObservation(d, FEATURE_LEG_ALPHA_INIT_NOISE * rnd.nextDouble());
			}
			feature.normalize();
		}

		H2Feature[] nextFeatures = new H2Feature[_featureDim];
		for (int k=0; k<_featureDim; k++) {
			final H2Feature feature = new H2Feature(_dataDim, FEATURE_LEG_ALPHA);
			nextFeatures[k] = feature;
		}
		
		int iter = 0;
		double prevLogLike = Double.NaN;
		double[] featureLogLikes = new double[_featureDim];
		
		if (monitor != null) {
			monitor.write("Total " + _data.size() + " data items");
			monitor.write("Starting with:");
			for (int k=0; k<_featureDim; k++) {
				monitor.write("Feature #" + (k+1));
				monitor.write("  Prob : " + fmt.format(currFeatureProbs[k]));
				monitor.write("  Left : " + currFeatures[k].getLeft());
				monitor.write("  Right: " + currFeatures[k].getRight());
			}
		}
		
		while (true) {

			iter += 1;
			
			if (monitor != null) {
				monitor.write("Iteration " + iter);
			}
			
			double currLogLike = 0;
			
			for (int dataIndex=0; dataIndex<_data.size(); dataIndex++) {
				
				if (monitor != null) {
					if ((dataIndex+1) % 100 == 0) {
						monitor.write((dataIndex+1) + " data items processed");
					}
				}
				
				final PWord word = _data.get(dataIndex);
				final PWord wordFeatures = _dataFeatures.get(dataIndex);
				final PWord parentWordFeatures = _parent != null ? _parent.getDataFeatures().get(dataIndex) : null;
				final H2Feature[] parentFeatures = _parent != null ? _parent.getFeatures() : null;
				
				for (int i=0; i<wordFeatures.size(); i++) {
					
					int parentCount = 0;
					int iLeftParent = -1;
					int iRightParent = -1;
					if (i > 0) {
						++parentCount;
						iLeftParent = i - 1;
					}
					if (i < wordFeatures.size() - 1) {
						++parentCount;
						iRightParent = i;
					}
					
					// init log likes based on parents
					if (parentCount > 0 && parentWordFeatures != null) {
						
						Arrays.fill(featureLogLikes, 0);
						
						// for now, in this limited model,
						// assume that the symbol could have
						// been generated from any of the 
						// parents with equal probability,
						// which is not very good modeling...
						final double parentProb = 1.0 / parentCount;
						
						if (iLeftParent >= 0) {
							
							final PChar leftParentFeatureProbs = parentWordFeatures.getChar(iLeftParent);
							
							for (int parentFeatureIdx = 0;
								parentFeatureIdx<leftParentFeatureProbs.getDim();
								parentFeatureIdx++) {
								
								final H2Feature parentFeature = parentFeatures[parentFeatureIdx];
								final double parentFeatureProb = leftParentFeatureProbs.getProb(parentFeatureIdx);
								
								// IMPORTANT: take *right* leg from *left* parent
								final double[] childPosteriorMean = parentFeature.getRight().getPosteriorMean();
								
								for (int f=0; f<_featureDim; f++) {
									featureLogLikes[f] += parentProb * parentFeatureProb * childPosteriorMean[f];
								}
							}
						}
							
						if (iRightParent >= 0) {
							
							final PChar rightParentFeatureProbs = parentWordFeatures.getChar(iRightParent);
							
							for (int parentFeatureIdx = 0;
								parentFeatureIdx<rightParentFeatureProbs.getDim();
								parentFeatureIdx++) {
								
								final H2Feature parentFeature = parentFeatures[parentFeatureIdx];
								final double parentFeatureProb = rightParentFeatureProbs.getProb(parentFeatureIdx);
								
								// IMPORTANT: take *left* leg from *right* parent
								final double[] childPosteriorMean = parentFeature.getLeft().getPosteriorMean();
								
								for (int f=0; f<_featureDim; f++) {
									featureLogLikes[f] += parentProb * parentFeatureProb * childPosteriorMean[f];
								}
							}
						}

						// log parent induced priors
						for (int f=0; f<_featureDim; f++) {
							featureLogLikes[f] = Math.log(featureLogLikes[f]);
						}
						
					} else {
						
						// feature frequency based priors
						for (int k=0; k<_featureDim; k++) {
							featureLogLikes[k] = Math.log(currFeatureProbs[k]);
						}
					}
					
					// add data log likes
					for (int k=0; k<_featureDim; k++) {
						{
							final double[] leftLegFeatureProbs = currFeatures[k].getLeft().getPosteriorMean();
							final PChar leftLegObservation = word.getChar(i);
							for (int d=0; d<leftLegObservation.getDim(); d++) {
								featureLogLikes[k] 
										+= (leftLegFeatureProbs[d]*HDP_ALPHA - 1) 
										* Math.log(LOG_INSURANCE + leftLegObservation.getProb(d));
							}
						}
						{
							final double[] rightLegFeatureProbs = currFeatures[k].getRight().getPosteriorMean();
							final PChar rightLegObservation = word.getChar(i + 1);
							for (int d=0; d<rightLegObservation.getDim(); d++) {
								featureLogLikes[k] 
										+= (rightLegFeatureProbs[d]*HDP_ALPHA - 1) 
										* Math.log(LOG_INSURANCE + rightLegObservation.getProb(d));
							}
						}
					}
					
					// add to current log likelihood
					currLogLike += StatsUtils.logSumExp(featureLogLikes);

					// normalize probabilities of features
					StatsUtils.logLikesToProbsReplace(featureLogLikes);
					
					// save feature probs to feature image
					PChar featureProbs = wordFeatures.getChar(i);
					featureProbs.setProbsFrom(featureLogLikes);

					// add to next probs
					for (int k=0; k<_featureDim; k++) {
						nextFeatureProbs[k] += featureLogLikes[k];
						if (featureLogLikes[k] > 0) {
							{
								// update features *left* legs
								final PChar probs = word.getChar(i);
								for (int d=0; d<probs.getDim(); d++) {
									nextFeatures[k].getLeft().addObservation(
											d, probs.getProb(d), featureLogLikes[k]);
								}
							}
							{
								// update features *right* legs
								final PChar probs = word.getChar(i + 1);
								for (int d=0; d<probs.getDim(); d++) {
									nextFeatures[k].getRight().addObservation(
											d, probs.getProb(d), featureLogLikes[k]);
								}
							}
						}
					}
				}
			}
			
			// normalize next probs
			StatsUtils.normalize(nextFeatureProbs);
			for (int k=0; k<nextFeatures.length; k++) {
				for (int l=0; l<4; l++) {
					nextFeatures[k].normalize();
				}
			}
			
			// update current probs
			{
				double[] tmp = currFeatureProbs;
				currFeatureProbs = nextFeatureProbs;
				nextFeatureProbs = tmp;
				Arrays.fill(nextFeatureProbs, 0);
			}
			{
				H2Feature[] tmp = currFeatures;
				currFeatures = nextFeatures;
				nextFeatures = tmp;
				for (int k=0; k<_featureDim; k++) {
					final H2Feature feature = new H2Feature(_dataDim, FEATURE_LEG_ALPHA);
					nextFeatures[k] = feature;
				}
			}

			if (monitor != null) {
				for (int k=0; k<_featureDim; k++) {
					monitor.write("Feature #" + (k+1));
					monitor.write("  Prob : " + fmt.format(currFeatureProbs[k]));
					monitor.write("  Left : " + currFeatures[k].getLeft());
					monitor.write("  Right: " + currFeatures[k].getRight());
				}
				monitor.write("LogLike: " + currLogLike + " (" + prevLogLike + ")");
			}
			
			// check log like error
			if (Double.isNaN(currLogLike)) {
				if (monitor != null) {
					monitor.write("Log likelihood error.");
				}
				break;
			}
			
			// check log like
			if (currLogLike < prevLogLike) {
				if (monitor != null) {
					monitor.write("Log likelihood fell, but we don't stop");
				}
				
			} else {
			
				// check if converged
				if (Double.isNaN(prevLogLike) == false &&
					Math.abs(prevLogLike - currLogLike) < logLikeChangeThreshold) {
					if (monitor != null) {
						monitor.write("Log likelihood converged.");
					}
					break;
				}
			}

			// check if max iterations
			if (iter >= maxIterationCount) {
				if (monitor != null) {
					monitor.write("Done max iterations (" + iter + ").");
				}
				break;
			}
			
			prevLogLike = currLogLike;
		}
		
		_featureProbs = currFeatureProbs;
		_features = currFeatures;
	}
	
	public PWord fromFeaturesToData(final PWord featuresWord) {
		
		final List<PChar> dataChars = new ArrayList<>();
		final int dataCharsCount = featuresWord.size() + 1;
		
		for (int d=0; d<dataCharsCount; d++) {
			
			int parentCount = 0;
			int leftParentIndex = -1;
			int rightParentIndex = -1;

			if (d > 0) {
				leftParentIndex = d - 1;
				parentCount++;
			}
			
			if (d < featuresWord.size()) {
				rightParentIndex = d;
				parentCount++;
			}
			
			if (parentCount > 0) {
				
				double[] dataCharProbs = new double[_dataDim];
				
				if (leftParentIndex >= 0) {
					PChar featuresChar = featuresWord.getChar(leftParentIndex);
					for (int f=0; f<_featureDim; f++) {
						final H2Feature feature = _features[f];
						double[] dataProbs = feature.getRight().getPosteriorMean();
						for (int l=0; l<dataProbs.length; l++) {
							dataCharProbs[l] += featuresChar.getProb(f) / parentCount * dataProbs[l];
						}
					}
				}
				if (rightParentIndex >= 0) {
					PChar featuresChar = featuresWord.getChar(rightParentIndex);
					for (int f=0; f<_featureDim; f++) {
						final H2Feature feature = _features[f];
						double[] dataProbs = feature.getLeft().getPosteriorMean();
						for (int l=0; l<dataProbs.length; l++) {
							dataCharProbs[l] += featuresChar.getProb(f) / parentCount * dataProbs[l];
						}
					}
				}
				
				dataChars.add(new PChar(_dataDim, dataCharProbs));
			}
		}
		
		final PChar[] dataCharsArr = new PChar[dataChars.size()];
		dataChars.toArray(dataCharsArr);
		
		final PWord dataWord = new PWord(_dataDim, dataCharsArr);
		return dataWord;
	}
		
}
