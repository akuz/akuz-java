package me.akuz.nlp.porter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.akuz.core.FileUtils;
import me.akuz.core.math.SampleAverage;

public final class PorterWordsSentiment {
	
	private final Map<String, Integer> _wordSentimentMap;
	private final Map<String, SampleAverage> _stemSentimentMap;
	
	public PorterWordsSentiment() {
		_wordSentimentMap = new HashMap<>();
		_stemSentimentMap = new HashMap<>();
	}
	
	public void load(String fileName) throws IOException {
		PorterStemmerOrig ps = new PorterStemmerOrig();
		List<String> lines = FileUtils.readLinesNoComments(fileName);
		for (int i=0; i<lines.size(); i++) {
			String line = lines.get(i).trim();
			if (line.length() > 0) {
				String[] parts = line.split(",");
				if (parts.length != 2) {
					throw new IOException("Incorrect format in line " + (i+1) + ": " + line);
				}
				String word = parts[0].trim();
				int sentiment = Integer.parseInt(parts[1]);
				String stem = PorterStemmerOrigUtils.stem(ps, word);
				add(stem, word, sentiment);
			}
		}
	}
	
	public void add(String stem, String word, int sentiment) {
		_wordSentimentMap.put(word, sentiment);
		SampleAverage avg = _stemSentimentMap.get(stem);
		if (avg == null) {
			avg = new SampleAverage();
			_stemSentimentMap.put(stem, avg);
		}
		avg.add(sentiment);
	}
	
	public Double getAverageByStem(String stem) {
		SampleAverage avg = _stemSentimentMap.get(stem);
		return avg == null ? null : avg.getMean();
	}
	
	public Integer getByWord(String word) {
		return _wordSentimentMap.get(word);
	}
	
	public Map<String, SampleAverage> getStemSentimentMap() {
		return _stemSentimentMap;
	}
	
}
