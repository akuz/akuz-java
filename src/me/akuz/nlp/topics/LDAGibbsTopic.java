package me.akuz.nlp.topics;

import java.util.HashSet;
import java.util.Set;

public final class LDAGibbsTopic {
	
	private final String _topicId;
	private final double _corpusFraction;
	private final Set<Integer> _priorityStems;
	private final Set<Integer> _excludedStems;
	private final boolean _isTransient;
	
	public LDAGibbsTopic(
			String topicId,
			double corpusFraction) {
		
		this(topicId, corpusFraction, false);
	}
	
	public LDAGibbsTopic(
			String topicId, 
			double corpusFraction, 
			boolean isTransient) {
		
		_topicId = topicId;
		_corpusFraction = corpusFraction;
		_priorityStems = new HashSet<>();
		_excludedStems = new HashSet<>();
		_isTransient = isTransient;
	}
	
	public String getTopicId() {
		return _topicId;
	}
	
	public boolean isTransient() {
		return _isTransient;
	}
	
	public double getCorpusFraction() {
		return _corpusFraction;
	}
	
	public int getPriorityStemCount() {
		return _priorityStems.size();
	}
	public Set<Integer> getPriorityStems() {
		return _priorityStems;
	}
	public void addPriorityStem(Integer stemIndex) {
		_priorityStems.add(stemIndex);
	}
	public boolean isPriorityStem(Integer stemIndex) {
		return _priorityStems.contains(stemIndex);
	}
	
	public int getExcludedStemCount() {
		return _excludedStems.size();
	}
	public Set<Integer> getExcludedStems() {
		return _excludedStems;
	}
	public void addExcludedStem(Integer stemIndex) {
		_excludedStems.add(stemIndex);
	}
	public boolean isExcludedStem(Integer stemIndex) {
		return _excludedStems.contains(stemIndex);
	}

}
