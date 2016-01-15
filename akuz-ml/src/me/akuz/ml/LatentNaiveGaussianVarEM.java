 package me.akuz.ml;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.akuz.core.math.NIGDist;
import me.akuz.core.math.StatsUtils;
import Jama.Matrix;

import me.akuz.core.math.SparseVector;

/**
 * NBG (Naive Bayes Gaussian) inference 
 * using variational EM (Expectation Maximization).
 *
 */
public final class LatentNaiveGaussianVarEM {

	// input data
	private final int _topicCount;
	private final NIGDist[] _valueByTopicPriorDist;
	private final List<SparseVector<Integer, Double>> _dataDocs;
	private final List<Double> _dataValues;

	// posterior parameters
	private NIGDist[] _valueByTopicDist;
	private final Matrix _topicByDocProbs;
	private Matrix _wordByTopicProbs;
	private Matrix _topicProbs;

	public LatentNaiveGaussianVarEM(
			int wordCount,
			NIGDist[] valueByTopicPriorDist,
			double wordByTopicDirichletPrior,
			List<SparseVector<Integer, Double>> dataDocs,
			List<Double> dataValues,
			int[] trainDocIdxs,
			int[] validDocIdxs,
			double maxIter) {
		
		DecimalFormat fmtLogLike = new DecimalFormat("0.00");
		
		_topicCount = valueByTopicPriorDist.length;
		_valueByTopicPriorDist = valueByTopicPriorDist;
		
		_dataDocs = dataDocs;
		_dataValues = dataValues;
		
		Matrix prevTopicProbs = new Matrix(_topicCount, 1);
		Matrix prevWordByTopicProbs = new Matrix(wordCount, _topicCount);
		NIGDist[] prevValueByTopicDist = new NIGDist[_topicCount];
		Matrix prevTopicByDocProbs = new Matrix(_topicCount, dataDocs.size());
		
		Matrix currTopicProbs = new Matrix(_topicCount, 1);
		Matrix currWordByTopicProbs = new Matrix(wordCount, _topicCount);
		NIGDist[] currValueByTopicDist = new NIGDist[_topicCount];
		Matrix currTopicByDocProbs = new Matrix(_topicCount, dataDocs.size());

		for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
			prevValueByTopicDist[topicIndex] = (NIGDist)_valueByTopicPriorDist[topicIndex].clone();
			currValueByTopicDist[topicIndex] = (NIGDist)_valueByTopicPriorDist[topicIndex].clone();
		}

		// set initial topic-by-doc probs
		Random rnd = new Random(System.currentTimeMillis());
		currTopicByDocProbs = new Matrix(_topicCount, dataDocs.size());
		for (int j=0; j<dataDocs.size(); j++) {
			double sum = 0;
			for (int i=0; i<_topicCount; i++) {
				double value = 0.25 + 0.5 * rnd.nextDouble();
				currTopicByDocProbs.set(i, j, value);
				sum += value;
			}
			for (int i=0; i<_topicCount; i++) {
				currTopicByDocProbs.set(i, j, currTopicByDocProbs.get(i, j) / sum);
			}
		}

		// perform EM iterations
		List<Double> trainLogLikeList = new ArrayList<Double>();
		List<Double> validLogLikeList = new ArrayList<Double>();
		for (int iter=1; iter<=maxIter; iter++) {
			
			System.out.println("NBG: Running iteration " + iter + "...");

			// cache old params data
			Matrix topicProbs_ = prevTopicProbs;
			Matrix wordByTopicProbs_ = prevWordByTopicProbs;
			NIGDist[] valueByTopicDist_ = prevValueByTopicDist;
			Matrix topicByDocProbs_ = prevTopicByDocProbs;

			// prev params = curr params
			prevTopicProbs = currTopicProbs;
			prevWordByTopicProbs = currWordByTopicProbs;
			prevValueByTopicDist = currValueByTopicDist;
			prevTopicByDocProbs = currTopicByDocProbs;
			
			// reuse old params data
			currTopicProbs = topicProbs_;
			currWordByTopicProbs = wordByTopicProbs_;
			currValueByTopicDist = valueByTopicDist_;
			currTopicByDocProbs = topicByDocProbs_;

			// -------------- //
			// *** M-STEP *** //
			// -------------- //

			System.out.println("NBG: M-Step...");

			// *** estimate topic probs
			for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
				double sum = 0;
				// for all training docs
				for (int i=0; i<trainDocIdxs.length; i++) {
					int docIndex = trainDocIdxs[i];
					sum += prevTopicByDocProbs.get(topicIndex, docIndex);
				}
				currTopicProbs.set(topicIndex, 0, sum / trainDocIdxs.length);
			}
			
			// *** estimate word-by-topic probs; set priors
			for (int wordIndex=0; wordIndex<wordCount; wordIndex++) {
				for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
					currWordByTopicProbs.set(wordIndex, topicIndex, wordByTopicDirichletPrior);
				}
			}
			// for all training docs
			for (int i=0; i<trainDocIdxs.length; i++) {
				
				// get doc
				int docIndex = trainDocIdxs[i];
				SparseVector<Integer, Double> doc = dataDocs.get(docIndex);
				
				// for all words in doc
				for (int wordLoc=0; wordLoc<doc.size(); wordLoc++) {
					
					int wordIndex = doc.getKeyByIndex(wordLoc);
					Double wordWeight = doc.getValueByIndex(wordLoc);
					
					// if word is present
					if (wordWeight > 0) {
						
						// update soft counts based on topic soft assignments
						for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
							double topicResponsibility = prevTopicByDocProbs.get(topicIndex, docIndex);
							currWordByTopicProbs.set(wordIndex, topicIndex, 
									currWordByTopicProbs.get(wordIndex, topicIndex) 
									+ 
									wordWeight * 
									topicResponsibility);
						}
					}
				}
			}
			// normalize word-by-topic probs
			for (int j=0; j<currWordByTopicProbs.getColumnDimension(); j++) {
				double sum = 0;
				for (int i=0; i<currWordByTopicProbs.getRowDimension(); i++) {
					sum += currWordByTopicProbs.get(i, j);
				}
				for (int i=0; i<currWordByTopicProbs.getRowDimension(); i++) {
					currWordByTopicProbs.set(i, j, currWordByTopicProbs.get(i, j) / sum);
				}
			}
			
			// *** estimate value-by-topic distributions
			for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
				
				// get distribution
				NIGDist valueDist = currValueByTopicDist[topicIndex];
				
				// reset distribution
				valueDist.reset();
				
				// for all training docs
				for (int i=0; i<trainDocIdxs.length; i++) {
					
					int docIndex = trainDocIdxs[i];
					
					double docValue = _dataValues.get(docIndex);
					
					double topicResponsibility = prevTopicByDocProbs.get(topicIndex, docIndex);
					
					valueDist.addObservation(docValue, topicResponsibility);
				}
			}
			
			// -------------- //
			// *** E-STEP *** //
			// -------------- //
			
			System.out.println("NBG: E-Step...");

			double trainLogLike = 0;
			for (int i=0; i<trainDocIdxs.length; i++) {
				int docIndex = trainDocIdxs[i];
				double docLogLike = calcTopicByDocProbsAndLogLike(currTopicByDocProbs, docIndex, currTopicProbs, currValueByTopicDist, currWordByTopicProbs);
				trainLogLike += docLogLike;
			}
			double validLogLike = 0;
			if (validDocIdxs != null && validDocIdxs.length > 0) {
				for (int i=0; i<validDocIdxs.length; i++) {
					int docIndex = validDocIdxs[i];
					double docLogLike = calcTopicByDocProbsAndLogLike(currTopicByDocProbs, docIndex, currTopicProbs, currValueByTopicDist, currWordByTopicProbs);
					validLogLike += docLogLike;
				}
			}
			trainLogLikeList.add(trainLogLike);
			validLogLikeList.add(validLogLike);
			System.out.println("TrainLogLike: " + fmtLogLike.format(trainLogLike));
			System.out.println("ValidLogLike: " + fmtLogLike.format(validLogLike));
			
			if (validLogLikeList.size() > 1) {
				if (validLogLikeList.get(validLogLikeList.size() - 1) <
					validLogLikeList.get(validLogLikeList.size() - 2)) {
					
					System.out.println("Validation log likelihood decreased, stopped at " + iter + " iteration.");
					_topicProbs = prevTopicProbs;
					_wordByTopicProbs = prevWordByTopicProbs;
					_valueByTopicDist = prevValueByTopicDist;
					_topicByDocProbs = prevTopicByDocProbs;
					
					return;
				}
			}
		}
		
		System.out.println("Performed max number of iterations (" + maxIter + "), stoped.");
		_topicProbs = currTopicProbs;
		_wordByTopicProbs = currWordByTopicProbs;
		_valueByTopicDist = currValueByTopicDist;
		_topicByDocProbs = currTopicByDocProbs;
	}

	private final double calcTopicByDocProbsAndLogLike(
			Matrix outTopicByDocProbs, 
			int docIndex, 
			Matrix topicProbs, 
			NIGDist[] valueByTopicDist, 
			Matrix wordByTopicProbs) {

		SparseVector<Integer, Double> doc = _dataDocs.get(docIndex);

		double[] topicLogLikes = new double[_topicCount];
		
		// calculate per-topic log likelihoods
		for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {

			// init topic log like
			double topicLogLike = 0;

			// add topic prior to likelihood
			topicLogLike +=  Math.log(topicProbs.get(topicIndex, 0));
			
			// add value likelihood
			topicLogLike += Math.log(valueByTopicDist[topicIndex].getProb(_dataValues.get(docIndex)));
			
			// add likelihood of doc words
			for (int wordLoc=0; wordLoc<doc.size(); wordLoc++) {
				
				int wordIndex = doc.getKeyByIndex(wordLoc);
				Double wordWeight = doc.getValueByIndex(wordLoc);
				
				if (wordWeight > 0) {
					
					double wordLogLike = Math.log(
							wordByTopicProbs.get(wordIndex, topicIndex)
							* wordWeight
							);
					
					topicLogLike += wordLogLike;
				}
			}
			
			// remember per-topic log like
			topicLogLikes[topicIndex] = topicLogLike;
		}
		
		// sum up per-topic log likes
		double docLogLike = StatsUtils.logSumExp(topicLogLikes);
		
		// normalize and output topic probs
		StatsUtils.logLikesToProbsInPlace(topicLogLikes);
		for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
			double topicProb = topicLogLikes[topicIndex];
			outTopicByDocProbs.set(topicIndex, docIndex, topicProb);
		}

		return docLogLike;
	}

	public Matrix getTopicProbs() {
		return _topicProbs;
	}
	
	public Matrix getWordByTopicProbs() {
		return _wordByTopicProbs;
	}
	
	public NIGDist[] getValueByTopicDist() {
		return _valueByTopicDist;
	}
	
	public Matrix getTopicByDocProbs() {
		return _topicByDocProbs;
	}

}
