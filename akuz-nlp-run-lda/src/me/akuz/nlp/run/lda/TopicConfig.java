package me.akuz.nlp.run.lda;

/**
 * Topic configuration object deserialized from JSON.
 *
 */
public final class TopicConfig {

	private String id; // topic id, required
	private Double proportion; // topic proportion, required
	private Integer count; // number of topics to generate, optional
	private Double priorityWordsProportion; // priority words proportion, optional
	private String[] priorityWords; // priority words, optional
	private String[] excludedWords; // excluded words, optional
	
	public TopicConfig() {
		// for deserialization
	}
	
	public String getId() {
		return id;
	}
	
	public Integer getCount() {
		return count;
	}
	
	public Double getProportion() {
		return proportion;
	}
	
	public Double getPrioriryWordsProportion() {
		return priorityWordsProportion;
	}
	
	public String[] getPriorityWords() {
		return priorityWords;
	}
	
	public String[] getExcludedWords() {
		return excludedWords;
	}

}
