package me.akuz.nlp.topics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class LDAGibbsTopic {
	
	private final String _topicId;
	private final double _targetCorpusFraction;
	private final Map<Integer, Double> _priorityStemsMasses;
	private double _sumPriorityStemsMass;
	private final Set<Integer> _excludedStems;
	private final boolean _isTransient;
	private final boolean _isGroup;
	
	public LDAGibbsTopic(
			String topicId,
			double targetCorpusFraction) {
		
		this(topicId, targetCorpusFraction, false, false);
	}
	
	public LDAGibbsTopic(
			String topicId, 
			double targetCorpusFraction, 
			boolean isTransient,
			boolean isGroup) {
		
		_topicId = topicId;
		_targetCorpusFraction = targetCorpusFraction;
		_priorityStemsMasses = new HashMap<>();
		_excludedStems = new HashSet<>();
		_isTransient = isTransient;
		_isGroup = isGroup;
	}
	
	public String getTopicId() {
		return _topicId;
	}
	
	public boolean isTransient() {
		return _isTransient;
	}
	
	public boolean isGroup() {
		return _isGroup;
	}
	
	public double getTargetCorpusFraction() {
		return _targetCorpusFraction;
	}
	
	public void addPriorityStemMass(Integer stemIndex, double mass) {
		double sumPriorityStemsMass = _sumPriorityStemsMass + mass;
		if (sumPriorityStemsMass >= 1) {
			throw new IllegalStateException("Total mass for all priority stems must be < 1");
		}
		_priorityStemsMasses.put(stemIndex, mass);
		_sumPriorityStemsMass = sumPriorityStemsMass;
	}
	
	public Map<Integer, Double> getPriorityStemMassMap() {
		return _priorityStemsMasses;
	}
	
	public double getSumPriorityStemMass() {
		return _sumPriorityStemsMass;
	}
	
	public Set<Integer> getExcludedStems() {
		return _excludedStems;
	}

	public void addExcludedStem(Integer stemIndex) {
		_excludedStems.add(stemIndex);
	}
}
