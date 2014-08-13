package me.akuz.nlp.topics;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import me.akuz.core.Rounding;
import me.akuz.core.logs.LocalMonitor;
import me.akuz.core.logs.Monitor;
import me.akuz.nlp.corpus.Corpus;
import me.akuz.nlp.corpus.CorpusDoc;
import me.akuz.nlp.corpus.CorpusPlace;

import org.apache.commons.lang3.time.StopWatch;

import Jama.Matrix;

/**
 * Gibbs sampling inference algorithm for Latent Dirichlet Allocation (LDA).
 * This implementation also includes the following optimizations: 
 * 1) Multi-threading for faster inference on multi-processor machines
 * 2) Simulated annealing for faster convergence of Gibbs sampling
 *
 */
public final class LDAGibbs {
	
	private final Monitor _monitor;
	private int _logIterationFrequency = 5;
	private static final int _msLogDigits = 2;
	
	private final int _threadCount;
	private final List<TopicAllocationBatch> _topicAllocationBatches;
	private final List<TopicDocProbsBatch> _topicDocProbsBatches;
	private final List<StemTopicProbsBatch> _stemTopicProbsBatches;
	private final List<Future<Boolean>> _batchFutures;
	private final ExecutorService _executorService;
	
	private int _iterCount;
	private final StopWatch _stopWatch = new StopWatch();
	private double _iterAllocateAvgMs;
	private double _iterAcceptAvgMs;
	private double _iterTotalAvgMs;
	
	private int    _sampleTopicCount;
	private int    _sampleStemTopicCount;
	private double _sampleStemTopicAvgMs;
	private int    _sampleTopicDocCount;
	private double _sampleTopicDocAvgMs;
	
	private final List<CorpusDoc> _docs;
	private final int _placeCount;
	private final int _topicCount;
	private final int _stemCount;
	
	private final LDAGibbsAlpha _alpha;
	private final LDAGibbsBeta _beta;
	
	private final Matrix _countDocTopic;
	private final Matrix _countTopicStem;
	private final double[] _countTopic;
	
	public LDAGibbs(
			Monitor parentMonitor,
			Corpus corpus,
			List<LDAGibbsTopic> topics,
			LDAGibbsAlpha alpha,
			LDAGibbsBeta beta,
			int threadCount) {
		
		_monitor = parentMonitor == null ? null : new LocalMonitor(this.getClass().getSimpleName(), parentMonitor);
		
		if (topics.size() < 2) {
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
		_topicCount = topics.size();
		_stemCount = corpus.getStemsIndex().size();
		
		if (_monitor != null) {
			_monitor.write("Allocating matrices...");
		}
		_countDocTopic = new Matrix(_docs.size(), _topicCount);
		_countTopicStem = new Matrix(_topicCount, _stemCount);
		_countTopic = new double[_topicCount];
		
		if (_monitor != null) {
			_monitor.write("Initializing counts...");
		}
		for (int docIndex=0; docIndex<_docs.size(); docIndex++) {
			final CorpusDoc doc = _docs.get(docIndex);
			final List<CorpusPlace> places = doc.getPlaces();
			for (int j=0; j<places.size(); j++) {
				final CorpusPlace place = places.get(j);
				final int stemIndex = place.getStemIndex();
				if (stemIndex < 0) {
					continue;
				}
				if (stemIndex >= _stemCount) {
					throw new IllegalStateException(
							"Corpus word place is invalid " + 
							"(stem index " + stemIndex + " is outside " + 
							"of stems index length " + _stemCount + ")");
				}
				final int[] topicAlloc;
				try {
					topicAlloc = (int[])place.getTag();
				} catch (Exception ex) {
					throw new IllegalStateException(
							"Corpus word place contains invalid tag of type: " + 
							place.getTag().getClass().getSimpleName());
				}
				if (topicAlloc != null) {
					if (topicAlloc.length != 2) {
						throw new IllegalStateException(
								"Corpus word place contains invalid tag " + 
								"(array of wrong length " + topicAlloc.length + ")");
					}
					final int topicIndex = topicAlloc[0];
					if (topicIndex >= 0) {
						if (topicIndex >= _topicCount) {
							throw new IllegalStateException(
									"Corpus word place contains invalid tag " + 
									"(topic index " + topicIndex + " is outside " + 
									"of topics list length " + _topicCount + ")");
						}
						_countDocTopic.set(docIndex, topicIndex, _countDocTopic.get(docIndex, topicIndex) + 1);
						_countTopicStem.set(topicIndex, stemIndex, _countTopicStem.get(topicIndex, stemIndex) + 1);
						_countTopic[topicIndex] += 1;
					}
				}
			}
		}
		
		_alpha = alpha;
		_beta = beta;

		// initialize threads
		if (_docs.size() < _threadCount) {
			throw new IllegalStateException("Document count (" + _docs.size() + ") must be > thread count (" + _threadCount + ")");
		}
		_topicAllocationBatches = new ArrayList<>(_threadCount);
		_topicDocProbsBatches = new ArrayList<>(_threadCount);
		_stemTopicProbsBatches = new ArrayList<>(_threadCount);
		_batchFutures = new ArrayList<>();
		final int threadDocCount = _docs.size() / _threadCount;
		final int threadStemCount = _stemCount / _threadCount;
		if (_monitor != null) {
			_monitor.write("Creating batches...");
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
				
				if (_monitor != null) {
					_monitor.write("Thread #" + (i+1) + ": " + batch.getClass().getSimpleName() + ": " + batch.getDocCount() + " docs [" + batch._docIndexStart + ", " + batch._docIndexEnd + ")");
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
				if (_monitor != null) {
					_monitor.write("Thread #" + (i+1) + ": " + batch.getClass().getSimpleName() + ": " + batch.getDocCount() + " docs [" + batch._docIndexStart + ", " + batch._docIndexEnd + ")");
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

				if (_monitor != null) {
					_monitor.write("Thread #" + (i+1) + ": " + batch.getClass().getSimpleName() + ": " + batch.getStemCount() + " stems [" + batch._stemIndexStart + ", " + batch._stemIndexEnd + ")");
				}
			}
		}
		
		if (_threadCount > 1) {
			if (_monitor != null) {
				_monitor.write("Creating fixed thread pool of " + _threadCount + " threads...");
			}
			_executorService = Executors.newFixedThreadPool(_threadCount);
		} else {
			if (_monitor != null) {
				_monitor.write("Not creating thread pool, will execute on caller thread...");
			}
			_executorService = null;
		}
	}
	
	public void terminate() {
		if (!_executorService.isShutdown()) {
			if (_monitor != null) {
				_monitor.write("Terminating executors...");
			}
			_executorService.shutdownNow();
			try {
				_executorService.awaitTermination(1, TimeUnit.MINUTES);
				if (_monitor != null) {
					_monitor.write("Executors terminated.");
				}
			} catch (InterruptedException ex) {
				if (_monitor != null) {
					_monitor.write("Interrupted while waiting for termination.", ex);
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

	public int run(int startIterationNumber, int iterationCount) {

		int iteration;
		for (iteration=startIterationNumber; iteration<startIterationNumber+iterationCount; iteration++) {
			
			if (_monitor != null && iteration % _logIterationFrequency == 0) {
				_monitor.write("Iteration: " + iteration + "...");
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

			if (_monitor != null && iteration % _logIterationFrequency == 0) {
				_monitor.write("Stats: iteration " + iteration + "...");
				_monitor.write("Stats: iterAllocate ms: " + Rounding.round(_iterAllocateAvgMs, _msLogDigits));
				_monitor.write("Stats: iterAccept   ms: " + Rounding.round(_iterAcceptAvgMs, _msLogDigits));
				_monitor.write("Stats: iterTotal    ms: " + Rounding.round(_iterTotalAvgMs, _msLogDigits));
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
				final CorpusPlace place = places.get(placeIndex);
				final int stemIndex = place.getStemIndex();
				if (stemIndex < 0) {
					continue;
				}
				
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
				+ (getCountDocTopic(docIndex, topicIndex, prevTopicIndex) + _alpha.getTopicDocAlpha(topicIndex, docIndex))
				* (getCountTopicStem(topicIndex, stemIndex, prevTopicIndex) + _beta.getStemTopicBeta(stemIndex, topicIndex))
				/ (getCountTopic(topicIndex, prevTopicIndex) + _beta.getSumTopicBeta(topicIndex));
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

	public final void sampleTopicDoc(Matrix mTopicDoc) {

		_sampleTopicDocCount += 1;
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
		
		_sampleTopicDocAvgMs = (double)_sampleTopicDocAvgMs / (_sampleTopicDocCount) * (_sampleTopicDocCount-1) + (double)_stopWatch.getTime() / (_sampleTopicDocCount);
		
		if (_monitor != null && _sampleTopicDocCount % _logIterationFrequency == 0) {
			_monitor.write("Stats: calcTopicDoc ms: " + Rounding.round(_sampleTopicDocAvgMs, _msLogDigits));
		}
	}

	public final void sampleStemTopic(Matrix mStemTopic) {

		_sampleStemTopicCount += 1;
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
		
		_sampleStemTopicAvgMs = (double)_sampleStemTopicAvgMs / (_sampleStemTopicCount) * (_sampleStemTopicCount-1) + (double)_stopWatch.getTime() / (_sampleStemTopicCount);
		
		if (_monitor != null && _sampleStemTopicCount % _logIterationFrequency == 0) {
			_monitor.write("Stats: calcStemTopic ms: " + Rounding.round(_sampleStemTopicAvgMs, _msLogDigits));
		}
	}

	public final void sampleTopic(Matrix mTopic) {

		if (mTopic == null) {
			throw new NullPointerException("mTopicProbs");
		}
		if (mTopic.getRowDimension() != _topicCount) {
			throw new InvalidParameterException("Out matrix row count should be " + _topicCount);
		}
		if (mTopic.getColumnDimension() != 1) {
			throw new InvalidParameterException("Out matrix col count should be " + 1);
		}
		
		_sampleTopicCount += 1;
		for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
			
			double topicAllocCount = getCountTopic(topicIndex, -1);
			double value = (double)topicAllocCount / _placeCount;

			double avgValue
				= mTopic.get(topicIndex, 0) / (_sampleTopicCount) * (_sampleTopicCount-1)
				+ value / (_sampleTopicCount);
			
			mTopic.set(topicIndex, 0, avgValue);
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
					final CorpusPlace place = places.get(placeIndex);
					
					// get word index (doesn't change)
					final int stemIndex = place.getStemIndex();
					if (stemIndex < 0) {
						continue;
					}
					
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
						= (getCountTopicStem(topicIndex, stemIndex, -1) + _beta.getStemTopicBeta(stemIndex, topicIndex))
						/ (getCountTopic(topicIndex, -1) + _beta.getSumTopicBeta(topicIndex));
					
					double avgValue 
						= _mStemTopic.get(stemIndex, topicIndex) / (_sampleStemTopicCount) * (_sampleStemTopicCount-1)
						+ value / (_sampleStemTopicCount);
					
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
						= (getCountDocTopic(docIndex, topicIndex, -1) + _alpha.getTopicDocAlpha(topicIndex, docIndex))
						/ (docPlaceCount + _alpha.getSumDocAlpha(docIndex));
					
					double avgValue 
						= _mTopicDoc.get(topicIndex, docIndex) / (_sampleTopicDocCount) * (_sampleTopicDocCount-1)
						+ value / (_sampleTopicDocCount);
					
					_mTopicDoc.set(topicIndex, docIndex, avgValue);
				}
			}
			return null;
		}
	}

}
