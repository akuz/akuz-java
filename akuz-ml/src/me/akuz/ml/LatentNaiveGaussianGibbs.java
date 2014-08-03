package me.akuz.ml;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Random;

import me.akuz.core.Pair;
import me.akuz.core.math.GammaApprox;
import me.akuz.core.math.StatsUtils;
import Jama.Matrix;

import me.akuz.core.math.MatrixVar;

public class LatentNaiveGaussianGibbs {

	public enum PosteriorSource {
		CurrentSample,
		AverageSamples
	}

	private final Random _rnd;

	private final double _dirichletAlpha;
	private final int _topicCount;
	private final Matrix _data;
	private final MatrixVar[] _vars;
	private final int[] _z; // topic allocations
	
	private final Matrix _topicLyamdaPriors;
	private final Matrix _topicVegaPriors;
	private final Matrix _topicAlphaPriors;
	private final Matrix _topicBetaPriors;
	
	private final double[] _tempTopicPDF; // reused topic allocation CDF
	private final double[] _tempTopicLogLike; // reused topic allocation CDF
	private final double[] _tempTopicCDF; // reused topic allocation CDF
	
	private final int[] _topicAllocCounts; // counters of topic allocations
	private final Matrix _topicVarOm; // sufficient statistic: average of omegas
	private final Matrix _topicVarOmSq; // sufficient statistic: average of squared omegas
	
	private int _samplesTakenCount; // number of samples taken
	private final double[] _samplesAvgTopicAllocCounts; // counters of topic allocations
	private final Matrix _samplesAvgTopicVarOm; // sufficient statistic: average of omegas
	private final Matrix _samplesAvgTopicVarOmSq; // sufficient statistic: average of squared omegas
	
	public LatentNaiveGaussianGibbs(
			double dirichletAlpha,
			int topicCount, 
			Matrix data,
			MatrixVar[] vars,
			Matrix topicLyamdaPriors, 
			Matrix topicVegaPriors, 
			Matrix topicAlphaPriors, 
			Matrix topicBetaPriors) {
		
		if (data == null) {
			throw new InvalidParameterException("Input data must not be null");
		}
		if (vars == null || vars.length < 1) {
			throw new InvalidParameterException("At least one variable must be specified");
		}
		
		_rnd = new Random(System.currentTimeMillis());
		
		_dirichletAlpha = dirichletAlpha;
		_topicCount = topicCount;
		
		_data = data;
		_vars = vars;

		_topicLyamdaPriors = checkTopicPriors("Lyamda", topicLyamdaPriors, _vars.length, _topicCount);
		_topicVegaPriors = checkTopicPriors("Vega", topicVegaPriors, _vars.length, _topicCount);
		_topicAlphaPriors = checkTopicPriors("Alpha", topicAlphaPriors, _vars.length, _topicCount);
		_topicBetaPriors = checkTopicPriors("Beta", topicBetaPriors, _vars.length, _topicCount);
		
		// no topic allocations at start: fill with -1
		_z = new int[_data.getRowDimension()];
		Arrays.fill(_z, -1);
		
		_tempTopicPDF = new double[_topicCount];
		_tempTopicLogLike = new double[_topicCount];
		_tempTopicCDF = new double[_topicCount];
		
		_topicAllocCounts = new int[_topicCount];
		_topicVarOm = new Matrix(_topicCount, _vars.length);
		_topicVarOmSq = new Matrix(_topicCount, _vars.length);
		
		_samplesTakenCount = 0;
		_samplesAvgTopicAllocCounts = new double[_topicCount];
		_samplesAvgTopicVarOm = new Matrix(_topicCount, _vars.length);
		_samplesAvgTopicVarOmSq = new Matrix(_topicCount, _vars.length);
	}
	
	private final static Matrix checkTopicPriors(String name, Matrix topicPriors, int varCount, int topicCount) {
		
		if (topicPriors.getRowDimension() != varCount) {
			throw new InvalidParameterException("Topic priors for " + name + " should have number of rows equal to input var count (" + varCount + ")");
		}
		if (topicPriors.getColumnDimension() != topicCount) {
			throw new InvalidParameterException("Topic priors for " + name + " should have number of columns equal to number of topics (" + topicCount + ")");
		}
		
		return topicPriors;
	}
	
	public int iterate(int docIndexStart, int docIndexEnd, int iterationIndex, int iterationCount) {
		
		int afterLastIterationIndex = iterationIndex + iterationCount;
		while (iterationIndex < afterLastIterationIndex) {
			
			if (iterationIndex % 5 == 0) {
				System.out.println("LDF: Iteration " + iterationIndex + "...");
			}
			
			// iterate topic allocations
			for (int docIndex=docIndexStart; docIndex<docIndexEnd; docIndex++) {
				
				// get current topic
				int prevTopicIndex = _z[docIndex];
				
				// generate next topic index
				int nextTopicIndex = generateNextTopicIndex(docIndex, prevTopicIndex);
				
				// remember new topic index
				_z[docIndex] = nextTopicIndex;
			}
			
			iterationIndex += 1;
		}
		
		return iterationIndex;
	}
	
	public void sample() {
		
		_samplesTakenCount += 1;
		
		for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {

			_samplesAvgTopicAllocCounts[topicIndex] 
			            			    = _samplesAvgTopicAllocCounts[topicIndex] / (double)_samplesTakenCount * (double)(_samplesTakenCount-1) 
			            				+ _topicAllocCounts[topicIndex] / (double)_samplesTakenCount;
			
			for (int i=0; i<_samplesAvgTopicVarOm.getRowDimension(); i++) {
				for (int j=0; j<_samplesAvgTopicVarOm.getColumnDimension(); j++) {
					
					double average
	    			    = _samplesAvgTopicVarOm.get(i, j) / (double)_samplesTakenCount * (double)(_samplesTakenCount-1) 
	    				+ _topicVarOm.get(i, j) / (double)_samplesTakenCount;
					
					_samplesAvgTopicVarOm.set(i, j, average);
					
					double averageSq
	    			    = _samplesAvgTopicVarOmSq.get(i, j) / (double)_samplesTakenCount * (double)(_samplesTakenCount-1) 
	    				+ _topicVarOmSq.get(i, j) / (double)_samplesTakenCount;
					
					_samplesAvgTopicVarOmSq.set(i, j, averageSq);
				}
			}
		}
	}
	
	public double[] calcTopicPDF(PosteriorSource posteriorSource, double[] newDocW, int docIndex, int prevTopicIndex) {

		// initialize topic log likes
		Arrays.fill(_tempTopicLogLike, 0);
		
		// topic "prior" distribution
		for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
			double count;
			if (posteriorSource == PosteriorSource.AverageSamples) {
				count = _samplesAvgTopicAllocCounts[topicIndex] + _dirichletAlpha;
			} else if (posteriorSource ==  PosteriorSource.CurrentSample) {
				count = _topicAllocCounts[topicIndex] + _dirichletAlpha;
			} else {
				throw new IllegalStateException("Internal error");
			}
			_tempTopicPDF[topicIndex] = count;
		}
		StatsUtils.normalize(_tempTopicPDF);
		for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
			_tempTopicLogLike[topicIndex] += Math.log(_tempTopicPDF[topicIndex]);
		}
		
		// calculate log likes of each variable
		// FIXME
		for (int varIndex=0; varIndex<_vars.length; varIndex++) {
			
			MatrixVar var = _vars[varIndex];
			
			// get value
			double value = Double.NaN;
			if (newDocW != null) {
				value = newDocW[var.getColumnIndex()];
			} else if (docIndex >= 0) {
				value = _data.get(docIndex, var.getColumnIndex());
			}
			
			// value can be NaN when inferring
			// topic with incomplete information
			if (Double.isNaN(value) == false) {
				
				// calculate log likelihood of document in topic
				for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
					
					double varTopicLogLike = Math.log(getProbTopicVar(posteriorSource, topicIndex, varIndex, value, prevTopicIndex));
					_tempTopicLogLike[topicIndex] += varTopicLogLike;

					if (Double.isNaN(varTopicLogLike)) {
						throw new IllegalStateException("Internal error");
					}
				}
			}
		}
		
		// normalize log likelihood into the PDF
		double maxLogLike = 0;
		for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
			double logLike = _tempTopicLogLike[topicIndex];
			if (topicIndex == 0) {
				maxLogLike = logLike;
			} else if (maxLogLike < logLike) {
				maxLogLike = logLike;
			}
		}
		for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
			_tempTopicPDF[topicIndex] = Math.exp(_tempTopicLogLike[topicIndex] - maxLogLike);
		}
		StatsUtils.normalize(_tempTopicPDF);
		
		return _tempTopicPDF;
	}
	
	private int generateNextTopicIndex(int docIndex, int prevTopicIndex) {

		// calculate topic PDF
		calcTopicPDF(PosteriorSource.CurrentSample, null, docIndex, prevTopicIndex);
		
		// calculate topic CDF
		double cdf = 0.0;
		for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
			cdf = cdf + _tempTopicPDF[topicIndex];
			_tempTopicCDF[topicIndex] = cdf;
		}

		// initialize next topic index
		int nextTopicIndex = -1;
		
		// select next topic by CDF
		double u = _rnd.nextDouble();
		for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
			if (u < _tempTopicCDF[topicIndex]/cdf) {
				nextTopicIndex = topicIndex;
				break;
			}
		}
		
		if (nextTopicIndex < 0) {
			throw new IllegalStateException("Internal error");
		}
		
		// update statistics
		if (docIndex >= 0 && prevTopicIndex != nextTopicIndex) {
			
			// forget old topic allocation, if not the first iteration
			if (prevTopicIndex >= 0) {
				
				// topic counts
				int topicAllocCount = _topicAllocCounts[prevTopicIndex] - 1;
				if (topicAllocCount < 0) {
					throw new IllegalStateException("Internal error");
				}
				_topicAllocCounts[prevTopicIndex] = topicAllocCount;

				// topic-var statistics
				for (int varIndex=0; varIndex<_vars.length; varIndex++) {
					
					MatrixVar var = _vars[varIndex];
					
					// get value
					double value = _data.get(docIndex, var.getColumnIndex());
					
					// sufficient statistics
					if (topicAllocCount == 0) {
						
						// reset sufficient statistics
						_topicVarOm.set(prevTopicIndex, varIndex, 0.0);
						_topicVarOmSq.set(prevTopicIndex, varIndex, 0.0);
						
					} else {
						
						// update sufficient statistic: om
						double oldOm = _topicVarOm.get(prevTopicIndex, varIndex);
						double newOm = (oldOm / (double)topicAllocCount * (double)(topicAllocCount + 1)) - (value / (double)topicAllocCount);
						_topicVarOm.set(prevTopicIndex, varIndex, newOm);
						
						// update sufficient statistic: omSq
						double oldOmSq = _topicVarOmSq.get(prevTopicIndex, varIndex);
						double newOmSq = (oldOmSq / (double)topicAllocCount * (double)(topicAllocCount + 1)) - (value*value / (double)topicAllocCount);
						_topicVarOmSq.set(prevTopicIndex, varIndex, newOmSq);
					}					
				}
			}
			
			// remember new topic allocation
			int topicAllocCount = _topicAllocCounts[nextTopicIndex] + 1;
			_topicAllocCounts[nextTopicIndex] = topicAllocCount;
			
			// topic-var statistics
			for (int varIndex=0; varIndex<_vars.length; varIndex++) {
				
				MatrixVar var = _vars[varIndex];

				// get value
				double value = _data.get(docIndex, var.getColumnIndex());
								
				// update sufficient statistic: om
				double oldOm = _topicVarOm.get(nextTopicIndex, varIndex);
				double newOm = (oldOm / (double)topicAllocCount * (double)(topicAllocCount - 1)) + (value / (double)topicAllocCount);
				_topicVarOm.set(nextTopicIndex, varIndex, newOm);
				
				// update sufficient statistic: omSq
				double oldOmSq = _topicVarOmSq.get(nextTopicIndex, varIndex);
				double newOmSq = (oldOmSq / (double)topicAllocCount * (double)(topicAllocCount - 1)) + (value*value / (double)topicAllocCount);
				_topicVarOmSq.set(nextTopicIndex, varIndex, newOmSq);
			}
		}
		
		return nextTopicIndex;
	}
	
	public final double getProbTopicVar(PosteriorSource posteriorSource, int topicIndex, int varIndex, double value, int prevTopicIndex) {

		// get sufficient statistics
		double om;
		double omSq;
		double n;
		
		// get sufficient statistics from the current sample
		if (posteriorSource == PosteriorSource.CurrentSample) {
		
			om = _topicVarOm.get(topicIndex, varIndex);
			omSq = _topicVarOmSq.get(topicIndex, varIndex);
			n = _topicAllocCounts[topicIndex];
		
			// get sufficient statistics from the averaged samples
		} else if (posteriorSource == PosteriorSource.AverageSamples) {
			
			om = _samplesAvgTopicVarOm.get(topicIndex, varIndex);
			omSq = _samplesAvgTopicVarOmSq.get(topicIndex, varIndex);
			n = _samplesAvgTopicAllocCounts[topicIndex];
		
		} else {
			
			throw new IllegalStateException("Internal error");
		}
		
		if (topicIndex == prevTopicIndex) {
			n -= 1;
			if (n < 0) {
				throw new IllegalStateException("Incorrect counting");
			}
			if (n == 0) {
				om = 0.0;
				omSq = 0.0;
			} else {
				om = (om / (double)n * (double)(n + 1)) - (value / (double)n);
				omSq = (omSq / (double)n * (double)(n + 1)) - (value*value / (double)n);
			}
		}

		// get prior parameters
		double lyamdaPrior = _topicLyamdaPriors.get(varIndex, topicIndex);
		double vegaPrior = _topicVegaPriors.get(varIndex, topicIndex);
		double alphaPrior = _topicAlphaPriors.get(varIndex, topicIndex);
		double betaPrior = _topicBetaPriors.get(varIndex, topicIndex);
		
		// calculate posterior parameters
		double lyamda = (vegaPrior*lyamdaPrior + n*om) / (vegaPrior + n);
		double vega = vegaPrior + n;
		double alpha = alphaPrior + n / 2.0;
		double beta = betaPrior + n / 2.0 * (omSq - om*om) + n*vegaPrior / (n + vegaPrior) / 2.0 * (om - lyamdaPrior)*(om - lyamdaPrior);
		
		// we now have posterior over myu and sigma
		// we need to calculate probability of sample value
		// we integrated out myu and sigma to get the below

		double help1 = (vega*lyamda*lyamda + value*value)/(1 + vega);
		double help2 = (vega*lyamda + value)/(1 + vega);
		double betaStar = beta + (vega + 1) / 2.0 * (help1 - help2*help2);
		
		double gammaFraction = GammaApprox.approxGammaPlusHalfByGammaFraction(alpha);
		
		double powerFraction = Math.pow(Math.pow(beta, -2.0*alpha/(2.0*alpha + 1)) * betaStar, -alpha - 0.5);
		
		double prob = Math.sqrt(vega/(1 + vega)) / Math.sqrt(2*Math.PI) * powerFraction * gammaFraction;
		
		if (Double.isNaN(prob)) {
			throw new IllegalStateException("Internal error");
		}
		return prob;
	}
	
	public final double[] getTopicVarMeanVarianceModes(PosteriorSource posteriorSource, int topicIndex, int varIndex) {

		// get sufficient statistics
		double om;
		double omSq;
		double n;
		
		// get sufficient statistics from the current sample
		if (posteriorSource == PosteriorSource.CurrentSample) {
		
			om = _topicVarOm.get(topicIndex, varIndex);
			omSq = _topicVarOmSq.get(topicIndex, varIndex);
			n = _topicAllocCounts[topicIndex];
		
			// get sufficient statistics from the averaged samples
		} else if (posteriorSource == PosteriorSource.AverageSamples) {
			
			om = _samplesAvgTopicVarOm.get(topicIndex, varIndex);
			omSq = _samplesAvgTopicVarOmSq.get(topicIndex, varIndex);
			n = _samplesAvgTopicAllocCounts[topicIndex];
		
		} else {
			
			throw new IllegalStateException("Internal error");
		}
		
		// get prior parameters
		double lyamdaPrior = _topicLyamdaPriors.get(varIndex, topicIndex);
		double vegaPrior = _topicVegaPriors.get(varIndex, topicIndex);
		double alphaPrior = _topicAlphaPriors.get(varIndex, topicIndex);
		double betaPrior = _topicBetaPriors.get(varIndex, topicIndex);
		
		// calculate posterior parameters
		double lyamda = (vegaPrior*lyamdaPrior + n*om) / (vegaPrior + n);
		//double vega = vegaPrior + n;
		double alpha = alphaPrior + n / 2.0;
		double beta = betaPrior + n / 2.0 * (omSq - om*om) + n*vegaPrior / (n + vegaPrior) / 2.0 * (om - lyamdaPrior)*(om - lyamdaPrior);
		
		// calculate posterior modes
		double meanMode = lyamda;
		double varianceMode = beta / (alpha + 1.0);

		// collect result
		double[] result = new double[2];
		result[0] = meanMode;
		result[1] = varianceMode;
		
		if (Double.isNaN(meanMode) || Double.isNaN(varianceMode)) {
			throw new IllegalStateException("Internal error");
		}

		return result;
	}

	public final double getTopicCount(int topicIndex) {
		return getTopicCount(topicIndex, -1);
	}

	private final double getTopicCount(int topicIndex, int prevTopicIndex) {
		
		double result = _topicAllocCounts[topicIndex];

		if (prevTopicIndex == topicIndex) {
			result -= 1;
			if (result < 0) {
				throw new IllegalStateException("Internal error");
			}
		}
		return result;
	}
	
	public final double[] inferTopicProbs(double[] newDocW) {
		return calcTopicPDF(PosteriorSource.AverageSamples, newDocW, -1, -1);
	}
	
	public final double[] inferVarMeanAndVariance(int varIndex, double[] topicProbs) {
		
		double mean = 0;
		double variance = 0;
		for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {

			double[] pars = getTopicVarMeanVarianceModes(PosteriorSource.AverageSamples, topicIndex, varIndex);
			mean += topicProbs[topicIndex] * pars[0];
			variance += topicProbs[topicIndex] * pars[1];
		}
		
		return new double[]{ mean, variance };
	}
	
	public final Pair<double[],double[]> inferVarDistribution(PosteriorSource posteriorSource, int varIndex, double[] topicProbs, int intervalsCount) {
		
		// determine max and min values of the variable
		double minValue = 0;
		double maxValue = 0;
		for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
			
			double[] pars = getTopicVarMeanVarianceModes(posteriorSource, topicIndex, varIndex);
			double meanMode = pars[0];
			double sigmaMode = Math.sqrt(pars[1]);
			
			double min = meanMode - 4.0*sigmaMode;
			double max = meanMode + 4.0*sigmaMode;
			
			if (topicIndex == 0) {
				minValue = min;
				maxValue = max;
			} else {
				if (minValue > min) {
					minValue = min;
				}
				if (maxValue < max) {
					maxValue = max;
				}
			}
		}
		
		double[] values = new double[intervalsCount+1];
		double[] probs = new double[intervalsCount+1];

		double stepSize = (maxValue - minValue)/(double)intervalsCount;
		for (int i=0; i<=intervalsCount; i++) {

			// get point value
			double value = minValue + (double)i*stepSize;
			values[i] = value;
			
			// calculate probability of variable value
			for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
				
				// probability of value in this topic
				double topicVarValueProb = getProbTopicVar(PosteriorSource.AverageSamples, topicIndex, varIndex, value, -1);
				
				// average of probabilities under topics weighted by topic probs
				probs[i] = probs[i] + topicVarValueProb * topicProbs[topicIndex];
			}
		}
		
		return new Pair<double[],double[]>(values, probs);
	}
	
}
