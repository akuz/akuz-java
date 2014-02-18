package me.akuz.nlp.ontology;

import java.util.ArrayList;
import java.util.List;

import Jama.Matrix;

import com.google.gson.JsonArray;

import me.akuz.core.HashIndex;
import me.akuz.core.Index;
import me.akuz.core.math.MatrixUtils;

/**
 * Topic model that can be used for {@link Topic} detection.
 * Contains a collection of topics to be detected together.
 * Includes p(topic) and p(stem|topic) probabilities.
 *
 */
public final class TopicModel {
	
	private static final double VALIDATION_EPSILON = 0.0000001;
	private static final double MISSING_WORD_MIN_PROB_TIMES = 0.001;
	
	private final List<Topic> _topics;
	private final Index<String> _stemsIndex;
	private final Matrix _mTopicProb;
	private final Matrix _mStemTopicProb;

	public TopicModel(JsonArray topicList) {
		
		_topics = new ArrayList<>();
		_stemsIndex = new HashIndex<>();

		double minWordProb = Double.MAX_VALUE;
		if (topicList.size() == 0) {
			throw new IllegalArgumentException("Topic list contains no topics");
		}
		
		for (int topicIndex=0; topicIndex<topicList.size(); topicIndex++) {
			
			Topic topic = new Topic(topicList.get(topicIndex).getAsJsonObject());
			_topics.add(topic);
			
			JsonArray words = topic.getWords();
			if (words.size() == 0) {
				throw new IllegalArgumentException("Topic " + topic.getStem() + " contains no words");
			}

			for (int w=0; w<words.size(); w++) {
			
				TopicWord word = new TopicWord(words.get(w).getAsJsonObject());
				_stemsIndex.ensure(word.getStem());
				
				if (minWordProb > word.getProb()) {
					minWordProb = word.getProb();
				}
			}
		}
		
		_mTopicProb = new Matrix(topicList.size(), 1);
		_mStemTopicProb = new Matrix(_stemsIndex.size(), topicList.size(), MISSING_WORD_MIN_PROB_TIMES * minWordProb);
		
		for (int topicIndex=0; topicIndex<topicList.size(); topicIndex++) {
			
			Topic topic = new Topic(topicList.get(topicIndex).getAsJsonObject());
			_mTopicProb.set(topicIndex, 0, topic.getProb());
			
			JsonArray words = topic.getWords();
			for (int w=0; w<words.size(); w++) {
			
				TopicWord word = new TopicWord(words.get(w).getAsJsonObject());
				Integer stemIndex = _stemsIndex.getIndex(word.getStem());
				_mStemTopicProb.set(stemIndex, topicIndex, word.getProb());
			}
		}
		
		MatrixUtils.normalizeColumns(_mTopicProb);
		MatrixUtils.normalizeColumns(_mStemTopicProb);
	}

	public void validate() {
		
		Matrix mSumTopicProb = MatrixUtils.sumRows(_mTopicProb);
		if (Math.abs(mSumTopicProb.get(0, 0) - 1.0) > VALIDATION_EPSILON) {
			throw new IllegalStateException("Topic probabilities p(topic) do not sum up to one");
		}
		
		Matrix mSumStemTopicProb = MatrixUtils.sumRows(_mStemTopicProb);
		for (int j=0; j<mSumStemTopicProb.getColumnDimension(); j++) {
			if (Math.abs(mSumStemTopicProb.get(0, j) - 1.0) > VALIDATION_EPSILON) {
				throw new IllegalStateException("Topic (index " + j + ") stem probabilities p(stem|topic) do not sum up to one");
			}
		}
	}
	
	public int getTopicCount() {
		return _topics.size();
	}
	
	public List<Topic> getTopics() {
		return _topics;
	}
	
	public int getStemCount() {
		return _stemsIndex.size();
	}
	
	public Index<String> getStemsIndex() {
		return _stemsIndex;
	}
	
	public Matrix getTopicProb() {
		return _mTopicProb;
	}
	
	public Matrix getStemTopicProb() {
		return _mStemTopicProb;
	}
	
}
