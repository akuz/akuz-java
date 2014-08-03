package me.akuz.ml;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Random;

import me.akuz.core.math.NIGDist;
import me.akuz.core.math.StatsUtils;
import Jama.Matrix;

import me.akuz.core.math.MatrixVar;

public class LatentNaiveGaussianGibbs2 {

	private final Random _rnd;

	private final Matrix _data;
	private final MatrixVar[] _vars;
	private final NIGDist[][] _topicVarDist;

	private final int _topicCount;
	private final double _dirichletAlpha;
	private final int[] _topicAllocCount; // counters of topic allocations
	private final int[] _dataTopicIndex; // topic allocations
	
	private final double[] _tempTopicPDF; // reused topic allocation CDF
	private final double[] _tempTopicLogLike; // reused topic allocation CDF
	private final double[] _tempTopicCDF; // reused topic allocation CDF
	
	public LatentNaiveGaussianGibbs2(
			double dirichletAlpha,
			int topicCount, 
			Matrix data,
			MatrixVar[] vars,
			NIGDist[] varPriors) {
		
		if (data == null) {
			throw new InvalidParameterException("Input data must not be null");
		}
		if (vars == null || vars.length < 1) {
			throw new InvalidParameterException("At least one variable must be specified");
		}
		
		_rnd = new Random(System.currentTimeMillis());
		
		_topicCount = topicCount;
		_dirichletAlpha = dirichletAlpha;
		_topicAllocCount = new int[topicCount];

		// no topic allocations at start: fill with -1
		_dataTopicIndex = new int[data.getRowDimension()];
		Arrays.fill(_dataTopicIndex, -1);
		
		_data = data;
		_vars = vars;
		_topicVarDist = new NIGDist[topicCount][];
		for (int topicIndex = 0; topicIndex < topicCount; topicIndex++) {
			NIGDist[] topicVarDists = new NIGDist[vars.length];
			for (int varIdx=0; varIdx<vars.length; varIdx++) {
				topicVarDists[varIdx] = varPriors[varIdx].clone();
			}
			_topicVarDist[topicIndex] = topicVarDists;
		}

		_tempTopicPDF = new double[_topicCount];
		_tempTopicLogLike = new double[_topicCount];
		_tempTopicCDF = new double[_topicCount];
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
				int prevTopicIndex = _dataTopicIndex[docIndex];
				
				// generate next topic index and update stats
				int nextTopicIndex = generateNextTopicIndexAndUpdateStats(docIndex, prevTopicIndex);
				
				// remember new topic index
				_dataTopicIndex[docIndex] = nextTopicIndex;
			}
			
			iterationIndex += 1;
		}
		
		return iterationIndex;
	}
	
	public double[] calcTopicProbs() {
		return calcTopicPDF(null, -1, -1);
	}
	
	public double[] calcTopicPDF(double[] newDocRow, int docIndex, int prevTopicIndex) {

		// topic "prior" distribution
		for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
			_tempTopicPDF[topicIndex] = _dirichletAlpha + _topicAllocCount[topicIndex];
			if (prevTopicIndex >= 0 && prevTopicIndex == topicIndex) {
				_tempTopicPDF[topicIndex] -= 1;
			}
		}

		// initialize topic log likes
		StatsUtils.normalize(_tempTopicPDF);
		for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
			_tempTopicLogLike[topicIndex] = Math.log(_tempTopicPDF[topicIndex]);
		}
		
		// add log likes for each variable
		if (newDocRow != null || docIndex >= 0) {
			
			for (int varIndex=0; varIndex<_vars.length; varIndex++) {
				
				// get the variable
				MatrixVar var = _vars[varIndex];
				
				// get value
				double value = Double.NaN;
				if (newDocRow != null) {
					value = newDocRow[var.getColumnIndex()];
				} else if (docIndex >= 0) {
					value = _data.get(docIndex, var.getColumnIndex());
				}
				
				// value can be NaN when inferring
				// topic with incomplete information
				if (Double.isNaN(value) == false) {
					
					// calculate log likelihood of document in topic
					for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
						
						NIGDist topicVarDist = _topicVarDist[topicIndex][varIndex];
						
						// remove document observation, if necessary
						if (prevTopicIndex >= 0 && prevTopicIndex == topicIndex) {
							topicVarDist.removeObservation(value, var.getColumnWeight());
						}
						
						_tempTopicLogLike[topicIndex] += Math.log(topicVarDist.getProb(value));

						// add back document observation, if necessary
						if (prevTopicIndex >= 0 && prevTopicIndex == topicIndex) {
							topicVarDist.addObservation(value, var.getColumnWeight());
						}

						if (Double.isNaN(_tempTopicLogLike[topicIndex])) {
							throw new IllegalStateException("Internal error");
						}
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
	
	private int generateNextTopicIndexAndUpdateStats(int docIndex, int prevTopicIndex) {

		// calculate topic PDF
		calcTopicPDF(null, docIndex, prevTopicIndex);
		
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
				int topicAllocCount = _topicAllocCount[prevTopicIndex] - 1;
				if (topicAllocCount < 0) {
					throw new IllegalStateException("Internal error");
				}
				_topicAllocCount[prevTopicIndex] = topicAllocCount;

				// topic-var statistics
				for (int varIndex=0; varIndex<_vars.length; varIndex++) {
					
					// get variable
					MatrixVar var = _vars[varIndex];
					
					// get variable value
					double value = _data.get(docIndex, var.getColumnIndex());
					
					// remove observation
					_topicVarDist[prevTopicIndex][varIndex].removeObservation(value, var.getColumnWeight());
				}
			}
			
			// remember new topic allocation
			int topicAllocCount = _topicAllocCount[nextTopicIndex] + 1;
			_topicAllocCount[nextTopicIndex] = topicAllocCount;
			
			// topic-var statistics
			for (int varIndex=0; varIndex<_vars.length; varIndex++) {
				
				// get variable
				MatrixVar var = _vars[varIndex];

				// get variable value
				double value = _data.get(docIndex, var.getColumnIndex());
				
				// add observation
				_topicVarDist[nextTopicIndex][varIndex].addObservation(value, var.getColumnWeight());
			}
		}
		
		return nextTopicIndex;
	}
	
	public final NIGDist getTopicVarDist(int topicIndex, int varIndex) {
		return _topicVarDist[topicIndex][varIndex];
	}
	
	public final double[] inferTopicProbs(double[] newDocW) {
		return calcTopicPDF(newDocW, -1, -1);
	}
	
	public final double[] inferAverageMeanVarModes(int varIndex, double[] topicProbs) {
		
		double mean = 0;
		double variance = 0;
		for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {

			mean += topicProbs[topicIndex] * _topicVarDist[topicIndex][varIndex].getMeanMode();
			variance += topicProbs[topicIndex] * _topicVarDist[topicIndex][varIndex].getVarianceMode();
		}
		
		return new double[]{ mean, variance };
	}
	
}
