package me.akuz.nlp.detect;

import java.util.Arrays;
import java.util.List;

import me.akuz.core.Index;
import me.akuz.core.Pair;
import me.akuz.core.SortOrder;
import me.akuz.core.logs.LocalMonitor;
import me.akuz.core.logs.Monitor;
import me.akuz.core.math.MatrixUtils;
import me.akuz.core.math.StatsUtils;
import me.akuz.core.sort.SelectK;
import me.akuz.nlp.corpus.CorpusDoc;
import me.akuz.nlp.corpus.CorpusPlace;
import me.akuz.nlp.ontology.TopicModel;
import Jama.Matrix;

/**
 * Provides functionality to calculate topic probabilities
 * for a document (and each word-place within the document).
 * 
 */
public final class TopicsDetector {

	private final Monitor _monitor;
	private final Index<String> _docsStemsIndex;
	private final TopicModel _topicModel;
	private final double _alpha;
	private final double _alphaMinus1;
	private final int _maxIterations;
	private final double _stopDeltaLogLike;
	private final int _topProbsCount;

	public TopicsDetector(
			Monitor parentMonitor,
			Index<String> docsStemsIndex,
			TopicModel topicModel,
			double alpha,
			int maxIterations,
			double stopDeltaLogLike,
			int topProbsCount) {
		
		if (alpha <= 0) {
			throw new IllegalArgumentException("Argument alpha must be positive");
		}
		if (maxIterations <= 0) {
			throw new IllegalArgumentException("Argument maxIterations must be positive");
		}
		if (stopDeltaLogLike <= 0) {
			throw new IllegalArgumentException("Argument stopDeltaLogLike must be positive");
		}

		_monitor = parentMonitor != null ? new LocalMonitor(this.getClass().getSimpleName(), parentMonitor) : null;
		_docsStemsIndex = docsStemsIndex;
		_topicModel = topicModel;
		_alpha = alpha;
		_alphaMinus1 = alpha - 1.0;
		_maxIterations = maxIterations;
		_stopDeltaLogLike = stopDeltaLogLike;
		_topProbsCount = topProbsCount;
	}
	
	public TopicModel getTopicModel() {
		return _topicModel;
	}
	
	/**
	 * Calculate topic probs for each word-place, as well as for the whole document; 
	 * returns a corpus doc object which contains the output, with probs in tags:
	 * tags contain List<Pair<Integer, Double>> of top topic indices with probs.
	 */
	public void step1_calcTopicProbs(final CorpusDoc doc) {
		
		// init expectation optimization
		double prevLogLike = Double.NaN;
		final Matrix mStemTopicProb = _topicModel.getStemTopicProb();
		double[] mTopicProb = MatrixUtils.columnToArray(_topicModel.getTopicProb(), 0);
		doc.setTag(mTopicProb);
		
		// initialize log likelihood
		double currLogLike = Double.NaN;

		// prepare doc
		doc.clearTags();

		// get doc places to iterate over
		List<CorpusPlace> places = doc.getPlaces();
		
		int iter = 0;
		while (true) {

			iter += 1;

			// expectation
			currLogLike = 0;
			for (int topicIndex=0; topicIndex<mTopicProb.length; topicIndex++) {
				currLogLike += _alphaMinus1 * Math.log(mTopicProb[topicIndex]);
			}
			
			for (int j=0; j<places.size(); j++) {
				
				CorpusPlace place = places.get(j);
				int docStemIndex = place.getStemIndex();
				if (docStemIndex < 0) {
					continue;
				}

				String stem = _docsStemsIndex.getValue(docStemIndex);
				Integer modelStemIndex = _topicModel.getStemsIndex().getIndex(stem);
				if (modelStemIndex == null) {
					continue;
				}

				double[] placeTopicProb = (double[])place.getTag();
				if (placeTopicProb == null) {
					placeTopicProb = new double[_topicModel.getTopicCount()];
					place.setTag(placeTopicProb);
				}
				
				for (int topicIndex=0; topicIndex<placeTopicProb.length; topicIndex++) {
					double topicProb = mTopicProb[topicIndex];
					double stemTopicProb = mStemTopicProb.get(modelStemIndex, topicIndex);
					placeTopicProb[topicIndex] = Math.log(topicProb) + Math.log(stemTopicProb);
				}
				
				currLogLike += StatsUtils.logSumExp(placeTopicProb);
				StatsUtils.logLikesToProbsReplace(placeTopicProb);
			}
			
			if (_monitor != null) {
				_monitor.write("LogLike: " + currLogLike);
			}
			
			if (Double.isInfinite(currLogLike)) {
				if (_monitor != null) {
					_monitor.write("Stop: infinite log likelihood");
				}
				break;
			}
			
			if (!Double.isNaN(prevLogLike) && 
				(currLogLike < prevLogLike ||
				 Math.abs(currLogLike - prevLogLike) < _stopDeltaLogLike)) {
				if (_monitor != null) {
					_monitor.write("Stop: log like converged");
				}
				break;
			}
			
			if (iter >= _maxIterations) {
				if (_monitor != null) {
					_monitor.write("Stop: max iterations (" + _maxIterations + ")");
				}
				break;
			}
			
			// maximization
			Arrays.fill(mTopicProb, _alpha);
			for (int i=0; i<places.size(); i++) {
				
				CorpusPlace place = places.get(i);
				double[] placeTopicProb = (double[])place.getTag();

				if (placeTopicProb != null) {
					
					for (int topicIndex=0; topicIndex<placeTopicProb.length; topicIndex++) {
						mTopicProb[topicIndex] += placeTopicProb[topicIndex];
					}
				}
			}
			StatsUtils.normalize(mTopicProb);
			
			prevLogLike = currLogLike;
		}
		
		// collect doc top topic probs
		SelectK<Integer, Double> selectDocTopTopics = new SelectK<>(SortOrder.Desc, _topProbsCount);
		for (int topicIndex=0; topicIndex<mTopicProb.length; topicIndex++) {
			selectDocTopTopics.add(new Pair<Integer, Double>(topicIndex, mTopicProb[topicIndex]));
		}
		List<Pair<Integer, Double>> topicProbs = selectDocTopTopics.get();
		doc.setTag(topicProbs);
		
		// collect places top topic probs
		for (int i=0; i<places.size(); i++) {
			
			CorpusPlace place = places.get(i);
			double[] placeTopicProb = (double[])place.getTag();

			if (placeTopicProb != null) {

				// collect place top topic probs
				SelectK<Integer, Double> selectPlaceTopTopics = new SelectK<>(SortOrder.Desc, _topProbsCount);
				for (int topicIndex=0; topicIndex<placeTopicProb.length; topicIndex++) {
					selectPlaceTopTopics.add(new Pair<Integer, Double>(topicIndex, placeTopicProb[topicIndex]));
				}
				
				List<Pair<Integer, Double>> placeTopicProbs = selectPlaceTopTopics.get();
				place.setTag(placeTopicProbs);
			}
		}
	}
	
}
