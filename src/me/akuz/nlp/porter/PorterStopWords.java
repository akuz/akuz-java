package me.akuz.nlp.porter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import me.akuz.core.FileUtils;

/**
 * Utilities for loading stop words and stemming them with PorterStemmer.
 *
 */
public final class PorterStopWords {

	/**
	 * Loads stop words from the file, stems them using PorterStemmer.
	 * 
	 */
	public static final Set<String> loadStopWordsAndStemThem(PorterStemmer ps, String fileName) throws IOException {
		return loadStopWordsAndStemThem(ps, fileName, null);
	}

	/**
	 * Loads stop words from the file, stems them using PorterStemmer.
	 * 
	 */
	public static final Set<String> loadStopWordsAndStemThem(PorterStemmer ps, String fileName, String encoding) throws IOException {
		List<String> stopWords = FileUtils.readLinesNoComments(fileName, encoding);
		Set<String> stopStems = ps.stemAll(stopWords);
		return stopStems;
	}
}
