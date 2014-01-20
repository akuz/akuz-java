package me.akuz.nlp.test.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.akuz.core.Hit;
import me.akuz.nlp.parse.RegexSentencesParser;
import me.akuz.nlp.parse.RegexWordsParser;
import me.akuz.nlp.porter.PorterStemmer;

import org.junit.Test;


public final class ParsingTest {
	
	@Test
	public void testParsing() {
		
		PorterStemmer porterStemmer = new PorterStemmer();
		
		String text = getText();
		
		RegexSentencesParser sp = new RegexSentencesParser();
		List<Hit> sentenceBounds = new ArrayList<Hit>();
		sp.parseSentences(text, new Hit(text), sentenceBounds);
		
		if (sentenceBounds.size() != 3) {
			throw new IllegalStateException("Incorrect sentence count");
		}
		
		RegexWordsParser wp = new RegexWordsParser(porterStemmer);
		for (int sentenceIndex=0; sentenceIndex<sentenceBounds.size(); sentenceIndex++) {
			
			Hit bounds = sentenceBounds.get(sentenceIndex);
			Map<String, List<Hit>> hitsByStem = wp.extractHitsByStem(text, bounds);
			
			if (sentenceIndex == 0) {
				if (hitsByStem.size() != 4) {
					throw new IllegalStateException("Incorrect stem count in sentence index " + sentenceIndex);
				}
			} else if (sentenceIndex == 1) {
				if (hitsByStem.size() != 2) {
					throw new IllegalStateException("Incorrect stem count in sentence index " + sentenceIndex);
				}
			} else if (sentenceIndex == 2) {
				if (hitsByStem.size() != 1) {
					throw new IllegalStateException("Incorrect stem count in sentence index " + sentenceIndex);
				}
			}
		}
	}

	public final static String getText() {
		
		return "We'd take abstract view. Minimum wage wages   Ha-ha.";
		
	}
}
