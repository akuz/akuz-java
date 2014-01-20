package me.akuz.nlp.topics;

import java.security.InvalidParameterException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.akuz.nlp.corpus.Corpus;
import me.akuz.nlp.corpus.CorpusDoc;
import me.akuz.nlp.corpus.CorpusPlace;

import org.apache.commons.lang3.time.StopWatch;

import Jama.Matrix;

public final class LDAGibbs {
	
	private static final DecimalFormat _fmtMs = new DecimalFormat("0");

	private final Logger _log;
	private int _logIterationFrequency = 5;
	
	private final int _threadCount;
	private final List<TopicAllocationBatch> _topicAllocationBatches;
	private final List<TopicDocProbsBatch> _topicDocProbsBatches;
	private final List<StemTopicProbsBatch> _stemTopicProbsBatches;
	private final List<Future<Boolean>> _batchFutures;
	private final ExecutorService _executorService;
	
	private int    _iterCount;
	private final StopWatch _stopWatch = new StopWatch();
	private double _iterAllocateAvgMs;
	private double _iterAcceptAvgMs;
	private double _iterTotalAvgMs;
	private int    _calcTopicProbsCount;
	private int    _calcStemTopicCount;
	private double _calcStemTopicAvgMs;
	private int    _calcTopicDocCount;
	private double _calcTopicDocAvgMs;
	
	private final List<CorpusDoc> _docs;
	private final int _placeCount;
	private final int _topicCount;
	private final int _stemCount;
	
	private final LDAGibbsAlpha _buildAlpha;
	private final LDAGibbsBeta _buildBeta;
	
	private boolean _isInitialized;
	private Matrix _topicDocAlpha;
	private double[] _sumDocAlpha;
	private Matrix _stemTopicBeta;
	private double[] _sumTopicBeta;
	
	private final Matrix _countDocTopic;
	private final Matrix _countTopicStem;
	private final double[] _countTopic;
	
	public LDAGibbs(
			Logger log,
			List<LDAGibbsTopic> buildTopics,
			LDAGibbsAlpha buildAlpha,
			LDAGibbsBeta buildBeta,
			Corpus corpus,
			int threadCount) {
		
		_log = log;
		
		if (buildTopics.size() < 2) {
			throw new IllegalArgumentException("Topic count should be >= 2");
		}
		
		if (threadCount < 1) {
			throw new IllegalArgumentException("Thread count must be positive (requested " + threadCount + ")");
		} else if (threadCount > 32) {
			throw new IllegalArgumentException("Thread count cannot be > 32 (requested " + threadCount + ")");
		}
		_threadCount = threadCount;
		
		_docs = corpus.getDocs();
		_placeCount = corpus.getPlaceCount();
		_topicCount = buildTopics.size();
		_stemCount = corpus.getStemsIndex().size();
		
		_buildAlpha = buildAlpha;
		_buildBeta = buildBeta;

		_countDocTopic = new Matrix(_docs.size(), _topicCount);
		_countTopicStem = new Matrix(_topicCount, _stemCount);
		_countTopic = new double[_topicCount];
		
		// initialize threads
		if (_docs.size() < _threadCount) {
			throw new IllegalStateException("Document count (now " + _docs.size() + ") must be > thread count (now " + _threadCount + ")");
		}
		_topicAllocationBatches = new ArrayList<>(_threadCount);
		_topicDocProbsBatches = new ArrayList<>(_threadCount);
		_stemTopicProbsBatches = new ArrayList<>(_threadCount);
		_batchFutures = new ArrayList<>();
		final int threadDocCount = _docs.size() / _threadCount;
		final int threadStemCount = _stemCount / _threadCount;
		if (_log != null) {
			_log.finest("LDA: batches...");
		}
		for (int i=0; i<_threadCount; i++) {

			{ // create topic allocation batch
				
				int threadDocIndexStart = threadDocCount * i;
				int threadDocIndexEnd;
				
				// last thread
				if (i<_threadCount-1) {
					// not last thread
					threadDocIndexEnd = threadDocIndexStart + threadDocCount;
				} else {
					// last thread
					threadDocIndexEnd = _docs.size();
				}
				
				TopicAllocationBatch batch = new TopicAllocationBatch(_topicCount, threadDocIndexStart, threadDocIndexEnd);
				_topicAllocationBatches.add(batch);
				
				if (_log != null) {
					_log.finest("Thread #" + (i+1) + ": " + batch.getClass().getSimpleName() + ": " + batch.getDocCount() + " docs [" + batch._docIndexStart + ", " + batch._docIndexEnd + ")");
				}
			}

			{ // create topic | doc probability calc batch
				
				int threadDocIndexStart = threadDocCount * i;
				int threadDocIndexEnd;
				
				// last thread
				if (i<_threadCount-1) {
					// not last thread
					threadDocIndexEnd = threadDocIndexStart + threadDocCount;
				} else {
					// last thread
					threadDocIndexEnd = _docs.size();
				}
				
				TopicDocProbsBatch batch = new TopicDocProbsBatch(threadDocIndexStart, threadDocIndexEnd);
				_topicDocProbsBatches.add(batch);
				if (_log != null) {
					_log.finest("Thread #" + (i+1) + ": " + batch.getClass().getSimpleName() + ": " + batch.getDocCount() + " docs [" + batch._docIndexStart + ", " + batch._docIndexEnd + ")");
				}
			}
			
			{ // create stem | topic probability calc batch
				
				int threadStemIndexStart = threadStemCount * i;
				int threadStemIndexEnd;
				
				// last thread
				if (i<_threadCount-1) {
					// not last thread
					threadStemIndexEnd = threadStemIndexStart + threadStemCount;
				} else {
					// last thread
					threadStemIndexEnd = _stemCount;
				}
				
				StemTopicProbsBatch batch = new StemTopicProbsBatch(threadStemIndexStart, threadStemIndexEnd);
				_stemTopicProbsBatches.add(batch);

				if (_log != null) {
					_log.finest("Thread #" + (i+1) + ": " + batch.getClass().getSimpleName() + ": " + batch.getStemCount() + " stems [" + batch._stemIndexStart + ", " + batch._stemIndexEnd + ")");
				}
			}
		}
		
		if (_threadCount > 1) {
			
			// only create thread pool for multi-threading
			_executorService = Executors.newFixedThreadPool(_threadCount);
		} else {
			
			// will execute directly on calling thread
			_executorService = null;
		}
	}
	
	public void terminate() {
		if (_executorService != null) {
			if (_log != null) {
				_log.finest("LDA: Terminating executors...");
			}
			_executorService.shutdownNow();
			try {
				_executorService.awaitTermination(1, TimeUnit.MINUTES);
				if (_log != null) {
					_log.finest("LDA: Executors terminated.");
				}
			} catch (InterruptedException e) {
				if (_log != null) {
					_log.log(Level.FINEST, "LDA: Interrupted while waiting for termination.", e);
				}
			}
		}
	}
	
	public final int getTopicCount() {
		return _topicCount;
	}
	
	public int getLogIterationFrequency() {
		return _logIterationFrequency;
	}
	
	public void setLogIterationFrequency(int frequency) {
		if (frequency < 1) {
			throw new IllegalArgumentException("Frequency must be positive");
		}
		_logIterationFrequency = frequency;
	}
	
	public void setTemperature(double temperature) {
		
		_buildAlpha.setTemperature(temperature);
		_buildBeta.setTemperature(temperature);
		
		_topicDocAlpha = _buildAlpha.getTopicDocAlpha();
		_sumDocAlpha = _buildAlpha.getSumDocAlpha();
		_stemTopicBeta = _buildBeta.getStemTopicBeta();
		_sumTopicBeta = _buildBeta.getSumTopicBeta();
		
		_isInitialized = true;
	}

	public int run(int startIterationNumber, int iterationCount) {
		
		if (!_isInitialized) {
			throw new IllegalStateException("Not initialized, call setTemperature() first");
		}

		int iteration;
		for (iteration=startIterationNumber; iteration<startIterationNumber+iterationCount; iteration++) {
			
			if (_log != null && iteration % _logIterationFrequency == 0) {
				_log.finest("LDA Iteration: " + iteration + "...");
			}

			_stopWatch.reset();
			_stopWatch.start();
			if (_topicAllocationBatches.size() == 1) {

				// single thread
				_topicAllocationBatches.get(0).call();
			
			} else if (_topicAllocationBatches.size() > 0) {
			
				// clear futures
				_batchFutures.clear();

				// execute on multiple threads
				for (int i=0; i<_topicAllocationBatches.size(); i++) {
					TopicAllocationBatch batch = _topicAllocationBatches.get(i);
					_batchFutures.add(_executorService.submit(batch));
				}
				
				// wait for completion of all batches
				try {
					for (int i=0; i<_batchFutures.size(); i++) {
						_batchFutures.get(i).get();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					throw new IllegalStateException("Interrupted while waiting for batch completion");
				} catch (ExecutionException e) {
					e.printStackTrace();
					throw new IllegalStateException("Could not complete batch execution: " + e.getMessage());
				}
				
			} else {
				
				throw new IllegalStateException("No batches to execute topic allocation with");
			}
			_stopWatch.stop();
			long iterationAllocateMs = _stopWatch.getTime();
			
			_stopWatch.reset();
			_stopWatch.start();
			acceptNextTopicAllocations();
			_stopWatch.stop();
			long iterationAcceptMs = _stopWatch.getTime();
			
			long iterationTotalMs = iterationAllocateMs + iterationAcceptMs;
			
			_iterCount += 1;
			_iterAllocateAvgMs = (double)_iterAllocateAvgMs / (_iterCount) * (_iterCount-1) + (double)iterationAllocateMs / (_iterCount);
			_iterAcceptAvgMs   = (double)_iterAcceptAvgMs   / (_iterCount) * (_iterCount-1) + (double)iterationAcceptMs   / (_iterCount);
			_iterTotalAvgMs    = (double)_iterTotalAvgMs    / (_iterCount) * (_iterCount-1) + (double)iterationTotalMs    / (_iterCount);

			if (_log != null && iteration % _logIterationFrequency == 0) {
				_log.finest("LDA Stats: iteration " + iteration + "...");
				_log.finest("LDA Stats: iterAllocate ms: " + _fmtMs.format(_iterAllocateAvgMs));
				_log.finest("LDA Stats: iterAccept   ms: " + _fmtMs.format(_iterAcceptAvgMs));
				_log.finest("LDA Stats: iterTotal    ms: " + _fmtMs.format(_iterTotalAvgMs));
			}
		}
		return iteration;
	}
	
	private final void acceptNextTopicAllocations() {
		
		for (int docIndex=0; docIndex<_docs.size(); docIndex++) {
			
			CorpusDoc doc = _docs.get(docIndex);
			List<CorpusPlace> places = doc.getPlaces();
			
			// for every word place in a document
			for (int placeIndex=0; placeIndex<places.size(); placeIndex++) {
				
				// get word place
				CorpusPlace place = places.get(placeIndex);
				int stemIndex = place.getStemIndex();
				
				// get current topic allocations
				int[] topicAlloc = (int[])place.getTag();
				if (topicAlloc == null) {
					throw new IllegalStateException("Topic allocations should have already been initialized");
				}
				int currTopicIndex = topicAlloc[0];
				int nextTopicIndex = topicAlloc[1];
				
				// update statistics
				if (currTopicIndex != nextTopicIndex) {
					
					// set next topic as current
					topicAlloc[0] = nextTopicIndex;
					
					// forget the old topic allocation
					// if not the first iteration 
					if (currTopicIndex >= 0) {
						double count;
						
						count = getCountDocTopic(docIndex, currTopicIndex, currTopicIndex);
						_countDocTopic.set(docIndex, currTopicIndex, count);
						
						count = getCountTopicStem(currTopicIndex, stemIndex, currTopicIndex);
						_countTopicStem.set(currTopicIndex, stemIndex, count);
						
						count = getCountTopic(currTopicIndex, currTopicIndex);
						_countTopic[currTopicIndex] = count;
					}
					
					// remember new topic
					_countDocTopic.set(docIndex, nextTopicIndex, _countDocTopic.get(docIndex, nextTopicIndex) + 1);
					_countTopicStem.set(nextTopicIndex, stemIndex, _countTopicStem.get(nextTopicIndex, stemIndex) + 1);
					_countTopic[nextTopicIndex] = _countTopic[nextTopicIndex] + 1;
				}
			}
		}
	}
	
	private final int generateNextTopicIndex(ThreadLocalRandom rnd, double[] reusedTopicCDF, int docIndex, int stemIndex, int prevTopicIndex) {
		
		int nextTopicIndex = -1;
		
		double cdf = 0.0;
		for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
			cdf = cdf 
				+ (getCountDocTopic(docIndex, topicIndex, prevTopicIndex) + _topicDocAlpha.get(topicIndex, docIndex))
				* (getCountTopicStem(topicIndex, stemIndex, prevTopicIndex) + _stemTopicBeta.get(stemIndex, topicIndex))
				/ (getCountTopic(topicIndex, prevTopicIndex) + _sumTopicBeta[topicIndex]);
			reusedTopicCDF[topicIndex] = cdf;
		}
		
		double u = rnd.nextDouble();
		for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
			if (u < reusedTopicCDF[topicIndex]/cdf) {
				nextTopicIndex = topicIndex;
				break;
			}
		}
		
		if (nextTopicIndex < 0) {
			throw new IllegalStateException("Internal error: Something's wrong with the CDF calculation");
		}
		
		return nextTopicIndex;
	}
	
	private final double getCountDocTopic(int docIndex, int topicIndex, int prevTopicIndex) {
		double result = _countDocTopic.get(docIndex, topicIndex);
		if (prevTopicIndex == topicIndex) {
			result -= 1;
		}
		return result;
	}
	
	private final double getCountTopicStem(int topicIndex, int stemIndex, int prevTopicIndex) {
		double result = _countTopicStem.get(topicIndex, stemIndex);
		if (prevTopicIndex == topicIndex) {
			result -= 1;
		}
		return result;
	}
	
	private final double getCountTopic(int topicIndex, int prevTopicIndex) {
		double result = _countTopic[topicIndex];
		if (prevTopicIndex == topicIndex) {
			result -= 1;
			if (Math.abs(result) < 0.0000001) {
				result = 0.0;
			}
			if (result < 0) {
				throw new IllegalStateException("Invalid count: " + result);
			}
		}
		return result;
	}

	public final void calcTopicDoc(Matrix mTopicDoc) {

		_calcTopicDocCount += 1;
		_stopWatch.reset();
		_stopWatch.start();
		if (_topicDocProbsBatches.size() == 1) {

			// single thread
			TopicDocProbsBatch batch = _topicDocProbsBatches.get(0);
			batch.prepare(mTopicDoc);
			batch.call();
		
		} else if (_topicDocProbsBatches.size() > 0) {
		
			// clear futures
			_batchFutures.clear();

			// execute on multiple threads
			for (int i=0; i<_topicDocProbsBatches.size(); i++) {
				TopicDocProbsBatch batch = _topicDocProbsBatches.get(i);
				batch.prepare(mTopicDoc);
				_batchFutures.add(_executorService.submit(batch));
			}
			
			// wait for completion of all batches
			try {
				for (int i=0; i<_batchFutures.size(); i++) {
					_batchFutures.get(i).get();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new IllegalStateException("Interrupted while waiting for batch completion");
			} catch (ExecutionException e) {
				e.printStackTrace();
				throw new IllegalStateException("Could not complete batch execution: " + e.getMessage());
			}
			
		} else {
			
			throw new IllegalStateException("No batches to execute calculation with");
		}
		_stopWatch.stop();
		
		_calcTopicDocAvgMs = (double)_calcTopicDocAvgMs / (_calcTopicDocCount) * (_calcTopicDocCount-1) + (double)_stopWatch.getTime() / (_calcTopicDocCount);
		
		if (_log != null && _calcTopicDocCount % _logIterationFrequency == 0) {
			_log.finest("LDA Stats: calcTopicDoc ms: " + _fmtMs.format(_calcTopicDocAvgMs));
		}
	}

	public final void calcStemTopic(Matrix mStemTopic) {

		_calcStemTopicCount += 1;
		_stopWatch.reset();
		_stopWatch.start();
		if (_stemTopicProbsBatches.size() == 1) {

			// single thread
			StemTopicProbsBatch batch = _stemTopicProbsBatches.get(0);
			batch.prepare(mStemTopic);
			batch.call();
		
		} else if (_stemTopicProbsBatches.size() > 0) {
		
			// clear futures
			_batchFutures.clear();

			// execute on multiple threads
			for (int i=0; i<_stemTopicProbsBatches.size(); i++) {
				StemTopicProbsBatch batch = _stemTopicProbsBatches.get(i);
				batch.prepare(mStemTopic);
				_batchFutures.add(_executorService.submit(batch));
			}
			
			// wait for completion of all batches
			try {
				for (int i=0; i<_batchFutures.size(); i++) {
					_batchFutures.get(i).get();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				throw new IllegalStateException("Interrupted while waiting for batch completion");
			} catch (ExecutionException e) {
				e.printStackTrace();
				throw new IllegalStateException("Could not complete batch execution: " + e.getMessage());
			}
			
		} else {
			
			throw new IllegalStateException("No batches to execute calculation with");
		}
		_stopWatch.stop();
		
		_calcStemTopicAvgMs = (double)_calcStemTopicAvgMs / (_calcStemTopicCount) * (_calcStemTopicCount-1) + (double)_stopWatch.getTime() / (_calcStemTopicCount);
		
		if (_log != null && _calcStemTopicCount % _logIterationFrequency == 0) {
			_log.finest("LDA Stats: calcStemTopic ms: " + _fmtMs.format(_calcStemTopicAvgMs));
		}
	}

	public final void calcTopicProbs(Matrix mTopicProbs) {

		if (mTopicProbs == null) {
			throw new NullPointerException("mTopicProbs");
		}
		if (mTopicProbs.getRowDimension() != _topicCount) {
			throw new InvalidParameterException("Out matrix row count should be " + _topicCount);
		}
		if (mTopicProbs.getColumnDimension() != 1) {
			throw new InvalidParameterException("Out matrix col count should be " + 1);
		}
		
		_calcTopicProbsCount += 1;
		for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
			
			double topicAllocCount = getCountTopic(topicIndex, -1);
			double value = (double)topicAllocCount / _placeCount;

			double avgValue
				= mTopicProbs.get(topicIndex, 0) / (_calcTopicProbsCount) * (_calcTopicProbsCount-1)
				+ value / (_calcTopicProbsCount);
			
			mTopicProbs.set(topicIndex, 0, avgValue);
		}
	}
	
	// =============================================================
	//   MULTITHREADING
	// =============================================================
	
	/**
	 * Topic allocation batch call over specific document indices.
	 *
	 */
	private final class TopicAllocationBatch implements Callable<Boolean> {
		
		private final ThreadLocalRandom _rnd;
		private final double[] _reusedTopicCDF;
		private final int _docIndexStart;
		private final int _docIndexEnd;
		
		public TopicAllocationBatch(int topicCount, int docIndexStart, int docIndexEnd) {
			_rnd = ThreadLocalRandom.current();
			_reusedTopicCDF = new double[topicCount];
			_docIndexStart = docIndexStart;
			_docIndexEnd = docIndexEnd;
		}
		
		public int getDocCount() {
			return _docIndexEnd - _docIndexStart;
		}

		@Override
		public Boolean call() {
			
			for (int docIndex=_docIndexStart; docIndex<_docIndexEnd; docIndex++) {
				
				CorpusDoc doc = _docs.get(docIndex);
				List<CorpusPlace> places = doc.getPlaces();
				
				// for every word place in a document
				for (int placeIndex=0; placeIndex<places.size(); placeIndex++) {
					
					// get word place
					CorpusPlace place = places.get(placeIndex);
					
					// get word index (doesn't change)
					int stemIndex = place.getStemIndex();
					
					// get current topic allocations
					int[] topicAlloc = (int[])place.getTag();
					if (topicAlloc == null) {
						topicAlloc = new int[2];
						topicAlloc[0] = -1; // current topic index
						place.setTag(topicAlloc);
					}
					
					// generate next topic allocation
					topicAlloc[1] = generateNextTopicIndex(_rnd, _reusedTopicCDF, docIndex, stemIndex, topicAlloc[0]);
				}
			}
			return null;
		}
	}
	
	/**
	 * Stem | topic probability calculation batch call over specific stem indices.
	 *
	 */
	private final class StemTopicProbsBatch implements Callable<Boolean> {
		
		private final int _stemIndexStart;
		private final int _stemIndexEnd;
		private Matrix _mStemTopic;
		
		public StemTopicProbsBatch(int stemIndexStart, int stemIndexEnd) {
			_stemIndexStart = stemIndexStart;
			_stemIndexEnd = stemIndexEnd;
		}
		
		public int getStemCount() {
			return _stemIndexEnd - _stemIndexStart;
		}
		
		public void prepare(Matrix mStemTopic) {
			_mStemTopic = mStemTopic;
		}

		@Override
		public Boolean call() {
			
			if (_mStemTopic == null) {
				throw new NullPointerException("mStemTopic");
			}
			if (_mStemTopic.getRowDimension() != _stemCount) {
				throw new InvalidParameterException("Out matrix row count should be " + _stemCount);
			}
			if (_mStemTopic.getColumnDimension() != _topicCount) {
				throw new InvalidParameterException("Out matrix col count should be " + _topicCount);
			}
			
			for (int stemIndex=_stemIndexStart; stemIndex<_stemIndexEnd; stemIndex++) {
				
				for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
					
					double value 
						= (getCountTopicStem(topicIndex, stemIndex, -1) + _stemTopicBeta.get(stemIndex, topicIndex))
						/ (getCountTopic(topicIndex, -1) + _sumTopicBeta[topicIndex]);
					
					double avgValue 
						= _mStemTopic.get(stemIndex, topicIndex) / (_calcStemTopicCount) * (_calcStemTopicCount-1)
						+ value / (_calcStemTopicCount);
					
					_mStemTopic.set(stemIndex, topicIndex, avgValue);
				}
			}
			return null;
		}
	}
	
	/**
	 * Topic | doc probability calculation batch call over specific doc indices.
	 *
	 */
	private final class TopicDocProbsBatch implements Callable<Boolean> {
		
		private final int _docIndexStart;
		private final int _docIndexEnd;
		private Matrix _mTopicDoc;
		
		public TopicDocProbsBatch(int docIndexStart, int docIndexEnd) {
			_docIndexStart = docIndexStart;
			_docIndexEnd = docIndexEnd;
		}
		
		public int getDocCount() {
			return _docIndexEnd - _docIndexStart;
		}
		
		public void prepare(Matrix mTopicDoc) {
			_mTopicDoc = mTopicDoc;
		}

		@Override
		public Boolean call() {
			
			if (_mTopicDoc == null) {
				throw new NullPointerException("mTopicDoc");
			}
			if (_mTopicDoc.getRowDimension() != _topicCount) {
				throw new InvalidParameterException("Out matrix row count should be " + _topicCount);
			}
			if (_mTopicDoc.getColumnDimension() != _docs.size()) {
				throw new InvalidParameterException("Out matrix col count should be " + _docs.size());
			}
			
			for (int docIndex=_docIndexStart; docIndex<_docIndexEnd; docIndex++) {
				
				CorpusDoc doc = _docs.get(docIndex);
				int docPlaceCount = doc.getPlaceCount();

				for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
					
					double value 
						= (getCountDocTopic(docIndex, topicIndex, -1) + _topicDocAlpha.get(topicIndex, docIndex))
						/ (docPlaceCount + _sumDocAlpha[docIndex]);
					
					double avgValue 
						= _mTopicDoc.get(topicIndex, docIndex) / (_calcTopicDocCount) * (_calcTopicDocCount-1)
						+ value / (_calcTopicDocCount);
					
					_mTopicDoc.set(topicIndex, docIndex, avgValue);
				}
			}
			return null;
		}
	}

}
