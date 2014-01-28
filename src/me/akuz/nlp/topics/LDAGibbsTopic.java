package me.akuz.nlp.topics;

import java.util.HashSet;
import java.util.Set;

/**
 * Specifies a topic to be inferred by LDA.
 * 
 * Topic proportion is used to distribute more mass 
 * of the Dirichlet priors to the topics with higher
 * proportion. This essentially allows specifying 
 * asymmetric priors on p(t | d), and scaling
 * betas for priors on p(w | t) proportionally.
 *
 */
public final class LDAGibbsTopic {
	
	private final String _topicId;
	private double _proportion;
	private double _priorityWordsProportion = 0.5; // default value
	private final Set<Integer> _priorityStems;
	private final Set<Integer> _excludedStems;
	
	public LDAGibbsTopic(
			String topicId, 
			double proportion) {
		
		_topicId = topicId;
		setProportion(proportion);
		_priorityStems = new HashSet<>();
		_excludedStems = new HashSet<>();
	}
	
	public String getTopicId() {
		return _topicId;
	}
	
	public double getProportion() {
		return _proportion;
	}
	public void setProportion(double proportion) {
		if (proportion <= 0 || proportion >= 1) {
			throw new IllegalArgumentException("Topic proportion must be within interval (0, 1), but " + proportion + " is specified");
		}
		_proportion = proportion;
	}
	
	public double getPriorityWordsProportion() {
		return _priorityWordsProportion;
	}
	public void setPriorityWordsProportion(double proportion) {
		if (proportion <= 0 || proportion >= 1) {
			throw new IllegalArgumentException("Priority words proportion must be within interval (0, 1), but " + proportion + " is specified");
		}
		_priorityWordsProportion = proportion;
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
