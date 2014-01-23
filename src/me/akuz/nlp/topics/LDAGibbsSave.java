package me.akuz.nlp.topics;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import me.akuz.core.FileUtils;
import me.akuz.core.Pair;
import me.akuz.core.Rounding;
import me.akuz.core.SelectK;
import me.akuz.core.SortOrder;
import me.akuz.core.StringUtils;
import me.akuz.core.logs.Monitor;
import me.akuz.core.logs.LocalMonitor;
import me.akuz.nlp.corpus.Corpus;
import Jama.Matrix;

public final class LDAGibbsSave {
	
	private final static int PROB_DECIMAL_PLACES = 8;

	public LDAGibbsSave(
			Monitor parentMonitor,
			Corpus corpus,
			List<LDAGibbsTopic> topics,
			Matrix mTopic, 
			Matrix mStemTopic,
			LDAGibbsUnstemmer unstemmer,
			int topicOutputStemsCount,
			String outputFileName) throws Exception {
		
		LocalMonitor monitor = parentMonitor == null ? null : new LocalMonitor(this.getClass().getSimpleName(), parentMonitor);
		
		if (monitor != null) {
			monitor.write("Sorting stem-by-topic probs...");
		}
		List<List<Pair<Integer, Double>>> topicsSortedStemsLists = new ArrayList<>();
		{
			// reusable SelectK algo for selecting top stems
			SelectK<Integer, Double> selectK = new SelectK<>(SortOrder.Desc, topicOutputStemsCount);
			for (int topicIndex=0; topicIndex<topics.size(); topicIndex++) {
	
				// select top stems
				for (int stemIndex=0; stemIndex<mStemTopic.getRowDimension(); stemIndex++) {
					double prob = mStemTopic.get(stemIndex, topicIndex);
					selectK.add(new Pair<Integer, Double>(stemIndex, prob));
				}
				List<Pair<Integer, Double>> sortedTopicStemsList = selectK.get();

				// add to results lists
				topicsSortedStemsLists.add(sortedTopicStemsList);
			}
		}

		if (monitor != null) {
			monitor.write("Init topic output string buffers...");
		}

		// total chars in one line:
		// 5 - probability, 1 - space, 10 - word = 16
		// 3 - spaces (between 4 columns)
		final int textWidth = 4 * 16 + 3;
		DecimalFormat fmtProb = new DecimalFormat("#.0000");
		DecimalFormat fmtInt = new DecimalFormat("0000");
		String separator1;
		{
			StringBuilder sb = new StringBuilder();
			for (int i=0; i<textWidth; i++) {
				sb.append("*");
			}
			separator1 = sb.toString();
		}
		String separator2;
		{
			StringBuilder sb = new StringBuilder();
			for (int i=0; i<textWidth; i++) {
				sb.append("-");
			}
			separator2 = sb.toString();
		}
		StringBuilder sb = new StringBuilder();
		
		sb.append(separator1 + "\n");
		sb.append("                       TOTAL TOPICS COUNT: " + topics.size() + "\n");
		sb.append(separator1 + "\n");

		if (monitor != null) {
			monitor.write("Generating topics file...");
		}

		// collect topics and groups
		for (int topicIndex=0; topicIndex<topics.size(); topicIndex++) {

			// get topic data and stems
			LDAGibbsTopic topic = topics.get(topicIndex);
			List<Pair<Integer, Double>> topicSortedStemsList = topicsSortedStemsLists.get(topicIndex);
			
			// create output topic object
			double topicProb = mTopic.get(topicIndex, 0);
			topicProb = Rounding.round(topicProb, PROB_DECIMAL_PLACES);
			
			sb.append("\n");
			sb.append("\n");
			sb.append(separator2 + "\n");
			sb.append("#: " + fmtInt.format(topicIndex+1) + "  P: " + fmtProb.format(topicProb));
			sb.append("    " + topic.getTopicId() + "\n");
			sb.append(separator2 + "\n");
			sb.append("\n");
			
			// output text
			int rowCount = Math.min(topicSortedStemsList.size(), topicOutputStemsCount) / 4 + 1;
			for (int row=0; row<rowCount; row++) {
				
				for (int column=0; column<4; column++) {
					
					int index = row * 4 + column;
					if (index < topicSortedStemsList.size()) {
						
						Pair<Integer, Double> pair = topicSortedStemsList.get(index);
						Integer stemIndex = pair.v1();
						
						double stemProb = pair.v2();
						stemProb = Rounding.round(stemProb, PROB_DECIMAL_PLACES);
		
						Pair<String, String> pair2 = getStemAndWord(stemIndex, topicIndex, corpus, unstemmer);
						String word = pair2.v2();
						
						sb.append(fmtProb.format(stemProb));
						sb.append(" ");
						sb.append(StringUtils.trimOrFillSpaces(word.toLowerCase(), 10));
						
						if (column < 3) {
							sb.append(" ");
						}
					}
				}
				sb.append("\n");
			}
		}
		sb.append("\n");

		if (monitor != null) {
			monitor.write("Writing topics text file...");
		}
		FileUtils.writeEntireFile(outputFileName, sb.toString());
		
		if (monitor != null) {
			monitor.write("Successfully saved.");
		}
	}
	
	private Pair<String, String> getStemAndWord(
			Integer stemIndex,
			Integer topicIndex,
			Corpus corpus,
			LDAGibbsUnstemmer unstemmer) {
		
		String word;
		String stem = corpus.getStemsIndex().getValue(stemIndex);
		Integer wordIndex = unstemmer.getWordIndex(topicIndex, stemIndex);
		if (wordIndex != null) {
			word = corpus.getWordsIndex().getValue(wordIndex);
		} else {
			word = stem;
		}
		return new Pair<String, String>(stem, word);
	}
}
