package me.akuz.nlp.run.lda;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import me.akuz.core.FileUtils;
import me.akuz.core.Index;
import me.akuz.nlp.porter.PorterStemmer;
import me.akuz.nlp.topics.LDAGibbsTopic;

/**
 * Loads topics configuration from JSON file.
 *
 */
public final class TopicConfigLoader {

	public static final List<LDAGibbsTopic> load(
			String fileName, 
			PorterStemmer stemmer,
			Index<String> stemsIndex) throws IOException {
		
		List<LDAGibbsTopic> topics = new ArrayList<>();
		
		// load JSON from file
		String json = FileUtils.readEntireFile(fileName);
		
		// parse JSON
		Gson gson = new Gson();
		Type collectionType = new TypeToken<List<TopicConfig>>(){}.getType();
		@SuppressWarnings("unchecked")
		List<TopicConfig> topicConfigs = (List<TopicConfig>)gson.fromJson(json, collectionType);
		
		// check configs
		if (topicConfigs == null || topicConfigs.size() == 0) {
			throw new IOException("Topic configuration file does not contain a valid topic configs list: " + fileName);
		}
		
		// process configs
		double sumTopicProportion = 0;
		Set<String> createdTopicIdsLowercase = new HashSet<>();
		for (int i=1; i<=topicConfigs.size(); i++) {
			
			TopicConfig config = topicConfigs.get(i-1);

			Integer count = config.getCount();
			if (count == null) {
				count = 1;
			} else if (count < 1) {
				throw new IOException("Topic count must be positive, but instead a count of " + count + " specified for config entry #" + i);
			}
			
			for (int j=1; j<=count; j++) {
				
				String baseId = config.getId();
				if (baseId == null || baseId.trim().length() == 0) {
					baseId = "topic_" + i;
				}
				
				String id = baseId;
				if (count > 1) {
					id = baseId + "_" + j;
				}
				
				if (createdTopicIdsLowercase.contains(id.toLowerCase())) {
					throw new IOException("Duplicate topic id: " + id + ", please check your configuration");
				}
				
				Double proportion = config.getProportion();
				if (proportion == null) {
					throw new IOException("Topic proportion not specified for topic: " + id);
				} else if (proportion <= 0 || proportion >= 1) {
					throw new IOException("Topic proportion must be within interval (0, 1), but " + proportion + " is specified for topic: " + id);
				}
				sumTopicProportion += proportion;

				Double priorityWordsProportion = config.getPrioriryWordsProportion();
				if (proportion != null) {
					if (proportion <= 0 || proportion >= 1) {
						throw new IOException("Priority words proportion must be within interval (0, 1), but " + priorityWordsProportion + " is specified for topic: " + id);
					}
				}
				
				LDAGibbsTopic topic = new LDAGibbsTopic(id, proportion);
				if (priorityWordsProportion != null) {
					topic.setPriorityWordsProportion(priorityWordsProportion);
				}
				
				{
					String[] priorityWords = config.getPriorityWords();
					if (priorityWords != null) {
						for (int k=0; k<priorityWords.length; k++) {
							String word = priorityWords[k];
							if (word != null) {
								word = word.trim();
								if (word.length() > 0) {
									
									String stem = stemmer.stem(word);
									Integer stemIndex = stemsIndex.ensure(stem);
									topic.addPriorityStem(stemIndex);
								}
							}
						}
					}
				}
				{
					String[] excludedWords = config.getExcludedWords();
					if (excludedWords != null) {
						for (int k=0; k<excludedWords.length; k++) {
							String word = excludedWords[k];
							if (word != null) {
								word = word.trim();
								if (word.length() > 0) {
									
									String stem = stemmer.stem(word);
									Integer stemIndex = stemsIndex.ensure(stem);
									topic.addExcludedStem(stemIndex);
								}
							}
						}
					}
				}
				
				topics.add(topic);
				createdTopicIdsLowercase.add(id.toLowerCase());
			}
		}
		
		// normalize topic proportions
		if (sumTopicProportion <= 0) {
			throw new IOException("Sum of all topic proportions must be positive");
		}
		for (int i=0; i<topics.size(); i++) {
			LDAGibbsTopic topic = topics.get(i);
			topic.setProportion(topic.getProportion() / sumTopicProportion);
		}
		
		return topics;
	}
}
