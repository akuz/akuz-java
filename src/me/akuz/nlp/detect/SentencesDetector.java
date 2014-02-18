package me.akuz.nlp.detect;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.akuz.core.Hit;

/**
 * Finds locations of sentence breaks in the document.
 * This is a pattern-matching solution, not of a good 
 * quality; works bad for: "When Mr. Porter looked."
 *
 */
public final class SentencesDetector {
	
	private final static Pattern _patternSentenceBreak = Pattern.compile(
			
			"((?<=[a-z0-9]{2}[^\\w\\s]?)[.;!?]([’”)\\]]|[^\\w\\s]\\s)?\\s*(?=(\\s+[^\\w\\s]|\\s*[A-Z“‘(\\[])))" + 
			//      ^ must start with two word chars
			//                ^ maybe a special symbol (%, ", etc)
			//                           ^ sentence delimiter
			//                                  ^ maybe closing quote or
			//                                           ^ special symbol with a space
			//                                                        ^ include extra spaces into previous sentence
			//                                                             ^ must be followed by
			//                                                                ^ required space & non word char, or
			//                                                                             ^ maybe spaces & uppercase or opening quote
			
			"|" +
			
			"((?<=\\s)[.;!?]\\s+(?=([^\\w\\s]|[A-Z])))" + 
			//      ^ must start with a space
			//         ^ sentence delimiter
			//                ^ another space
			//                    ^ followed by
			//                       ^ non word char, or
			//                                 ^ uppercase
			
			"|" +
			
			"(\\s{3,}(?!(\\s|[a-z]|$)))"); 
			//    ^ must start with three spaces
			//        ^ not followed by
			//           ^ space or
			//                ^ lowercase or
			//                     ^ end

	public SentencesDetector() {
		
	}

	public void parseSentences(String str, List<Hit> fillSentencesBoundsList) {
		parseSentences(str, null, fillSentencesBoundsList);
	}
	
	public void parseSentences(String str, Hit bounds, List<Hit> fillSentencesBoundsList) {
	
		if (str == null) {
			return;
		}
		
		// start parsing
		int prevEnd = bounds != null ? bounds.start() : 0;
		Matcher matcher = _patternSentenceBreak.matcher(str);
		if (bounds != null) {
			matcher.region(bounds.start(), bounds.end());
		}
		while (matcher.find()) {
			
			// add sentence to results
			Hit sentenceBounds = new Hit(prevEnd, matcher.end());
			fillSentencesBoundsList.add(sentenceBounds);
			
			// remember last sentence end
			prevEnd = matcher.end();
		}
		
		// add last sentence (maybe whole string, if no breaks found)
		Hit sentenceBounds = new Hit(prevEnd, bounds != null ? bounds.end() : str.length());
		fillSentencesBoundsList.add(sentenceBounds);
	}

}
