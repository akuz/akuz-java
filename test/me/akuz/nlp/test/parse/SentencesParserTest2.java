package me.akuz.nlp.test.parse;

import java.util.ArrayList;
import java.util.List;

import me.akuz.core.Hit;
import me.akuz.nlp.parse.SentencesParser;

import org.junit.Test;


public final class SentencesParserTest2 {
	
	@Test
	public void testSentencesParser1() {
		
		SentencesParser sp = new SentencesParser();
		
		String str;
		Hit hit;
		List<Hit> sentences = new ArrayList<Hit>();
		
		str = "Aaaa ssss ddd. Ssssaa aaaa! Oaaaa";
		hit = new Hit(0, str.length());
		sentences.clear();
		sp.parseSentences(str, hit, sentences);
		if (sentences.size() != 3) {
			throw new IllegalStateException("Incorrect sentence parsing 1: " + sentences.size());
		}
		
		str = "Aaaa ssss ddd. ssssaa aaaa! oaaaa";
		hit = new Hit(0, str.length());
		sentences.clear();
		sp.parseSentences(str, hit, sentences);
		if (sentences.size() != 1) {
			throw new IllegalStateException("Incorrect sentence parsing 2: " + sentences.size());
		}
		
		str = "Aaaa ssss ddd.Ssssaa aaaa! Oaaaa";
		hit = new Hit(0, str.length());
		sentences.clear();
		sp.parseSentences(str, hit, sentences);
		if (sentences.size() != 3) {
			throw new IllegalStateException("Incorrect sentence parsing 3: " + sentences.size());
		}
		
		str = "Aaaa ssss ddd. Ssssaa aaaa! Oaaaa sssl aalla;; alsll 27777 . ";
		hit = new Hit(0, str.length());
		sentences.clear();
		sp.parseSentences(str, hit, sentences);
		if (sentences.size() != 3) {
			throw new IllegalStateException("Incorrect sentence parsing 4: " + sentences.size());
		}
		
		str = "Aaaa ssss ddd. Ssssaa aaaa! Oaas768686s@@@aa sssl aalla;; alsll 27777 . (2)";
		hit = new Hit(0, str.length());
		sentences.clear();
		sp.parseSentences(str, hit, sentences);
		if (sentences.size() != 4) {
			throw new IllegalStateException("Incorrect sentence parsing 5: " + sentences.size());
		}
		
		str = "Aaaa ssss ddd. Ssssaa aaaa! Oaas768686s@@@aa sssl aalla;; alsll 27777 . (2)";
		hit = new Hit(0, /***********/ 20 /***********/);
		sentences.clear();
		sp.parseSentences(str, hit, sentences);
		if (sentences.size() != 2) {
			throw new IllegalStateException("Incorrect sentence parsing 6: " + sentences.size());
		}
		
	}

}
