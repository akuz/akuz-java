package me.akuz.nlp.test.parse;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.akuz.core.Hit;
import me.akuz.core.StringUtils;
import me.akuz.nlp.parse.WordsParser;
import me.akuz.nlp.porter.PorterStemmer;

import org.junit.Test;

public final class WordsParserTest {

	@Test
	public void testWordsParser() {
		
		PorterStemmer porterStemmer = new PorterStemmer();

		Set<String> stopStems = new HashSet<String>();
		stopStems.add("gag");
		WordsParser wp = new WordsParser(porterStemmer, stopStems);
		
		String str;
		Hit bounds;
		Map<String, List<Hit>> out;
		
		str = "Wow 1 10 7.3 3,500";
		bounds = new Hit(0, str.length());
		out = wp.extractHitsByStem(str, bounds);
		if (out.size() != 1) {
			throw new IllegalStateException("Invalid words parsing 0 (" + out.size() + ")");
		}
		
		str = "Wow wowed 4Quants Q3";
		bounds = new Hit(0, str.length());
		out = wp.extractHitsByStem(str, bounds);
		if (out.size() != 3) {
			throw new IllegalStateException("Invalid words parsing 1 (" + out.size() + ")");
		}
		
		str = "Wow wowed gag gagged";
		bounds = new Hit(0, str.length());
		out = wp.extractHitsByStem(str, bounds);
		if (out.size() != 1) {
			throw new IllegalStateException("Invalid words parsing 2 (" + out.size() + ")");
		}
		
		str = "Wow wowed self-made";
		bounds = new Hit(0, str.length());
		out = wp.extractHitsByStem(str, bounds);
		if (out.size() != 3) {
			throw new IllegalStateException("Invalid words parsing 3 (" + out.size() + "): " + StringUtils.collectionToString(out.keySet(), ", "));
		}
		
		str = "Wow wowed self-made web2.0";
		bounds = new Hit(0, str.length());
		out = wp.extractHitsByStem(str, bounds);
		if (out.size() != 4) {
			throw new IllegalStateException("Invalid words parsing 4 (" + out.size() + "): " + StringUtils.collectionToString(out.keySet(), ", "));
		}
		
		str = "Wow wowed self-made web2.0 www..2";
		bounds = new Hit(0, str.length());
		out = wp.extractHitsByStem(str, bounds);
		if (out.size() != 4) {
			throw new IllegalStateException("Invalid words parsing 5 (" + out.size() + "): " + StringUtils.collectionToString(out.keySet(), ", "));
		}
		
		str = "Wow wowed self-made web2.0 www..2 127,199.00";
		bounds = new Hit(0, str.length());
		out = wp.extractHitsByStem(str, bounds);
		if (out.size() != 4) {
			throw new IllegalStateException("Invalid words parsing 6 (" + out.size() + "): " + StringUtils.collectionToString(out.keySet(), ", "));
		}
		
		str = "Wow wowed self-made web2.0 www..2 127,199.00 www.readrz.com";
		bounds = new Hit(0, str.length());
		out = wp.extractHitsByStem(str, bounds);
		if (out.size() != 5) {
			throw new IllegalStateException("Invalid words parsing 7 (" + out.size() + "): " + StringUtils.collectionToString(out.keySet(), ", "));
		}
		
		str = "Wow wowed self-made web2.0 www..2 127,199.00 www.readrz.com 2-go";
		bounds = new Hit(0, str.length());
		out = wp.extractHitsByStem(str, bounds);
		if (out.size() != 5) {
			throw new IllegalStateException("Invalid words parsing 8 (" + out.size() + "): " + StringUtils.collectionToString(out.keySet(), ", "));
		}
		
		str = "Wow wowed self-made web2.0 www..2 127,199.00 www.readrz.com 2x-go #whatever_tag u.s.";
		bounds = new Hit(0, str.length());
		out = wp.extractHitsByStem(str, bounds);
		if (out.size() != 9) {
			throw new IllegalStateException("Invalid words parsing 9 (" + out.size() + "): " + StringUtils.collectionToString(out.keySet(), ", "));
		}
		
		str = "Wow wowed self-made web2.0 www..2 127,199.00 www.readrz.com 2x-go #whatever_tag u.s. @to-them2 ((aa9--a0!!";
		bounds = new Hit(0, str.length());
		out = wp.extractHitsByStem(str, bounds);
		if (out.size() != 11) {
			throw new IllegalStateException("Invalid words parsing 10 (" + out.size() + "): " + StringUtils.collectionToString(out.keySet(), ", "));
		}
		
		str = "(Wow)";
		bounds = new Hit(0, str.length());
		out = wp.extractHitsByStem(str, bounds);
		if (out.size() != 1) {
			throw new IllegalStateException("Invalid words parsing 11 (" + out.size() + "): " + StringUtils.collectionToString(out.keySet(), ", "));
		}
		
		str = "-:Wow";
		bounds = new Hit(0, str.length());
		out = wp.extractHitsByStem(str, bounds);
		if (out.size() != 1) {
			throw new IllegalStateException("Invalid words parsing 12 (" + out.size() + "): " + StringUtils.collectionToString(out.keySet(), ", "));
		}
	}
}
