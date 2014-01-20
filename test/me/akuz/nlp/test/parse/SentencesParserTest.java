package me.akuz.nlp.test.parse;

import java.util.ArrayList;
import java.util.List;

import me.akuz.core.Hit;
import me.akuz.nlp.parse.RegexSentencesParser;

import org.junit.Test;


public final class SentencesParserTest {
	
	private static final List<Hit> parse(String text) {
		RegexSentencesParser sp = new RegexSentencesParser();
		List<Hit> sentenceBounds = new ArrayList<Hit>();
		sp.parseSentences(text, new Hit(text), sentenceBounds);
		return sentenceBounds;
	}
	
	private static final void checkSentenceBounds(String testId, int mustStart, int mustEnd, Hit actualHit) {
		if (actualHit.start() != mustStart) {
			throw new IllegalStateException(testId + ": Wrong sentence start: " + actualHit.start() + " (but must be " + mustStart + ")");
		}
		if (actualHit.end() != mustEnd) {
			throw new IllegalStateException(testId + ": Wrong sentence end: " + actualHit.end() + " (but must be " + mustEnd + ")");
		}
	}
	
	private static final void outputSentences(String text, List<Hit> hits) {
		for (int i=0; i<hits.size(); i++) {
			Hit hit = hits.get(i);
			System.out.println(text.substring(hit.start(), hit.end()));
		}
	}
	
	@Test
	public void test1() {
		
		String text = "Whatever he did.Today we don't know.     ";
		List<Hit> sentenceBounds = parse(text);
		outputSentences(text, sentenceBounds);
		
		if (sentenceBounds.size() != 2) {
			throw new IllegalStateException("1.A: Wrong number of sentences: " + sentenceBounds.size());
		}
		checkSentenceBounds("1.B", 0, 16, sentenceBounds.get(0));
		checkSentenceBounds("1.C", 16, text.length(), sentenceBounds.get(1));
	}
	
	@Test
	public void test2() {
		
		String text = "I have been told by E.T. Jaynes that it's cool.     ";
		List<Hit> sentenceBounds = parse(text);
		outputSentences(text, sentenceBounds);
		
		if (sentenceBounds.size() != 1) {
			throw new IllegalStateException("2.A: Wrong number of sentences: " + sentenceBounds.size());
		}
		checkSentenceBounds("2.B", 0, text.length(), sentenceBounds.get(0));
	}
	
	@Test
	public void test3() {
		
		String text = "I have been told by E. T. Jaynes that it's cool.     ";
		List<Hit> sentenceBounds = parse(text);
		outputSentences(text, sentenceBounds);
		
		if (sentenceBounds.size() != 1) {
			throw new IllegalStateException("3.A: Wrong number of sentences: " + sentenceBounds.size());
		}
		checkSentenceBounds("3.B", 0, text.length(), sentenceBounds.get(0));
	}

	@Test
	public void test4() {
		
		String text = "Mirracles happen    asserted Oppy.    ";
		List<Hit> sentenceBounds = parse(text);
		outputSentences(text, sentenceBounds);
		
		if (sentenceBounds.size() != 1) {
			throw new IllegalStateException("4.A: Wrong number of sentences: " + sentenceBounds.size());
		}
		checkSentenceBounds("4.B", 0, text.length(), sentenceBounds.get(0));
	}

	@Test
	public void test5() {
		
		String text = "Mirracles happen    Asserted Oppy.    ";
		List<Hit> sentenceBounds = parse(text);
		outputSentences(text, sentenceBounds);
		
		if (sentenceBounds.size() != 2) {
			throw new IllegalStateException("5.A: Wrong number of sentences: " + sentenceBounds.size());
		}
		checkSentenceBounds("5.B", 0, 20, sentenceBounds.get(0));
		checkSentenceBounds("5.C", 20, text.length(), sentenceBounds.get(1));
	}

	@Test
	public void test6() {
		
		String text = "Management of the U.S. Department of Defence is awful.    ";
		List<Hit> sentenceBounds = parse(text);
		outputSentences(text, sentenceBounds);
		
		if (sentenceBounds.size() != 1) {
			throw new IllegalStateException("6.A: Wrong number of sentences: " + sentenceBounds.size());
		}
		checkSentenceBounds("6.B", 0, text.length(), sentenceBounds.get(0));
	}

	@Test
	public void test7() {
		
		String text = "Malika went away. Nobody knows where.God knows.";
		List<Hit> sentenceBounds = parse(text);
		outputSentences(text, sentenceBounds);
		
		if (sentenceBounds.size() != 3) {
			throw new IllegalStateException("7.A: Wrong number of sentences: " + sentenceBounds.size());
		}
		checkSentenceBounds("7.B", 0, 18, sentenceBounds.get(0));
		checkSentenceBounds("7.C", 18, 37, sentenceBounds.get(1));
		checkSentenceBounds("7.D", 37, text.length(), sentenceBounds.get(2));
	}

	@Test
	public void test8() {
		
		String text = "       Now we know that Web2.0 ideas are shallow         ";
		List<Hit> sentenceBounds = parse(text);
		outputSentences(text, sentenceBounds);
		
		if (sentenceBounds.size() != 2) {
			throw new IllegalStateException("8.A: Wrong number of sentences: " + sentenceBounds.size());
		}
		checkSentenceBounds("8.B", 0, 7, sentenceBounds.get(0));
		checkSentenceBounds("8.C", 7, text.length(), sentenceBounds.get(1));
	}

	@Test
	public void test9() {
		
		String text = "He said: “Awkward feeling.” All silently agreed.";
		List<Hit> sentenceBounds = parse(text);
		outputSentences(text, sentenceBounds);
		
		if (sentenceBounds.size() != 2) {
			throw new IllegalStateException("9.A: Wrong number of sentences: " + sentenceBounds.size());
		}
		checkSentenceBounds("9.B", 0, 28, sentenceBounds.get(0));
		checkSentenceBounds("9.C", 28, text.length(), sentenceBounds.get(1));
	}

	@Test
	public void test10() {
		
		String text = "He said: “Awkward feeling”. All silently agreed.";
		List<Hit> sentenceBounds = parse(text);
		outputSentences(text, sentenceBounds);
		
		if (sentenceBounds.size() != 2) {
			throw new IllegalStateException("10.A: Wrong number of sentences: " + sentenceBounds.size());
		}
		checkSentenceBounds("10.B", 0, 28, sentenceBounds.get(0));
		checkSentenceBounds("10.C", 28, text.length(), sentenceBounds.get(1));
	}

	@Test
	public void test11() {
		
		// hanging dot not followed by space
		// doesn't count as sentence break...
		
		String text = "He claimed .net domain name";
		List<Hit> sentenceBounds = parse(text);
		outputSentences(text, sentenceBounds);
		
		if (sentenceBounds.size() != 1) {
			throw new IllegalStateException("11.A: Wrong number of sentences: " + sentenceBounds.size());
		}
		checkSentenceBounds("11.B", 0, text.length(), sentenceBounds.get(0));
	}

	@Test
	public void test12() {
		
		// hanging dot not followed by space
		// doesn't count as sentence break...
		
		String text = "He claimed .NET domain name";
		List<Hit> sentenceBounds = parse(text);
		outputSentences(text, sentenceBounds);
		
		if (sentenceBounds.size() != 1) {
			throw new IllegalStateException("12.A: Wrong number of sentences: " + sentenceBounds.size());
		}
		checkSentenceBounds("12.B", 0, text.length(), sentenceBounds.get(0));
	}

	@Test
	public void test13() {
		
		// hanging dot followed by Uppercase
		// counts as sentence break...
		
		String text = "Whatever he claimed .  We don't accept.       ";
		List<Hit> sentenceBounds = parse(text);
		outputSentences(text, sentenceBounds);
		
		if (sentenceBounds.size() != 2) {
			throw new IllegalStateException("13.A: Wrong number of sentences: " + sentenceBounds.size());
		}
		checkSentenceBounds("13.B", 0, 23, sentenceBounds.get(0));
		checkSentenceBounds("13.C", 23, text.length(), sentenceBounds.get(1));
	}

	@Test
	public void test14() {
		
		// hanging dot followed by lowercase
		// doesn't count as sentence break...
		
		String text = "This dot . is not a delimiter.   ";
		List<Hit> sentenceBounds = parse(text);
		outputSentences(text, sentenceBounds);
		
		if (sentenceBounds.size() != 1) {
			throw new IllegalStateException("14.A: Wrong number of sentences: " + sentenceBounds.size());
		}
		checkSentenceBounds("14.B", 0, text.length(), sentenceBounds.get(0));
	}

	@Test
	public void test15() {
		
		// opening quote in the middle hints 
		// us that it's a new sentence...
		
		String text = "He appologised.“I am very sorry.”     ";
		List<Hit> sentenceBounds = parse(text);
		outputSentences(text, sentenceBounds);
		
		if (sentenceBounds.size() != 2) {
			throw new IllegalStateException("15.A: Wrong number of sentences: " + sentenceBounds.size());
		}
		checkSentenceBounds("15.B", 0, 15, sentenceBounds.get(0));
		checkSentenceBounds("15.B", 15, text.length(), sentenceBounds.get(1));
	}

	@Test
	public void test16() {
		
		// in this case we can't decide naively which 
		// sentence does the *middle* quote belong to...
		
		String text = "He appologised.\"I am very sorry.\"     ";
		List<Hit> sentenceBounds = parse(text);
		outputSentences(text, sentenceBounds);
		
		if (sentenceBounds.size() != 1) {
			throw new IllegalStateException("16.A: Wrong number of sentences: " + sentenceBounds.size());
		}
		checkSentenceBounds("16.B", 0, text.length(), sentenceBounds.get(0));
	}
	
}
