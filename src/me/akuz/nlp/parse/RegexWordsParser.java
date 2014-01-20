package me.akuz.nlp.parse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.akuz.core.Hit;
import me.akuz.core.Pair;
import me.akuz.nlp.porter.PorterStemmer;

/**
 * Finds words in the text using regular expressions 
 * (good for filtering out garbage non-words); 
 * also, this doesn't match numbers.
 *
 */
public final class RegexWordsParser {
	
	// regex hint: (?:) means a non-capturing group
	
	// this will match simple words: two chars, either all letters (like "mom") or 
	// possibly with numbers at the beginning or the end, like "4Quants" or "Q3":
	private static final String _simpleWord = "(?:(?:[a-zA-Z]\\w{1,20})|(?:\\w{1,20}[a-zA-Z]))";
	
	// this will match word suffixes, like 's, 'd, and 'll:
	private static final String _wordSuffixS = "(?:['’]s)";
	private static final String _wordSuffixAll = "(?:['’](?:s|d|ll))";
	
	// used to match next no-space block as word candidate
	private final static Pattern _patternWordCandidate = Pattern.compile("\\S{1,50}");
	
	// used to check if word candidate is a simple word
	private final static Pattern _patternWordSimple = Pattern.compile(
			"^" + 
			"(\\W*)" + 
			"(" + _simpleWord + ")" +
			"(" + _wordSuffixAll + "*\\W*)" +
			"$");
	
	// used to check if word candidate is a composite word
	private final static Pattern _patternWordComposite = Pattern.compile(
			"^" + 
			"(\\W*)" + 
			"(" + _simpleWord + ")" + 
			"-" + 
			"(" + _simpleWord + ")" + 
			"(" + _wordSuffixAll + "*\\W*)" +
			"$");
	
	// used to check if block is an letter/number abbreviation
	private final static Pattern _patternAbbrev = Pattern.compile(
			"^" + 
			"(\\W*)" + 
			"([a-zA-Z]\\w{0,20}[.](?:\\w{1,20}[.]?)+)" + 
			"(" + _wordSuffixS + "*\\W*)" +
			"$");

	// private fields
	private final PorterStemmer _porterStemmer;
	private final Set<String> _stopStems;

	public RegexWordsParser(PorterStemmer porterStemmer) {
		this(porterStemmer, null);
	}

	public RegexWordsParser(PorterStemmer porterStemmer, Set<String> stopStems) {
		_porterStemmer = porterStemmer;
		_stopStems = stopStems;
	}

	public Map<String, List<Hit>> extractHitsByStem(String str, Hit bounds) {
		
		Map<String, List<Hit>> hitsByStem = null;

		// find words
		Matcher blockMatcher = _patternWordCandidate.matcher(str);
		if (bounds != null) {
			blockMatcher.region(bounds.start(), bounds.end());
		}
		List<Pair<String, Hit>> toAdd = new ArrayList<Pair<String, Hit>>();
		while (blockMatcher.find()) {
			
			// clear
			toAdd.clear();
			
			// get the block to analyze
			String block = blockMatcher.group();
			Matcher matcher;

			// check if simple word
			matcher = _patternWordSimple.matcher(block);
			if (matcher.find()) {
				
				final int GROUP = 2;
				
				String word = matcher.group(GROUP);
				String stem = _porterStemmer.stem(word);
				
				if (_stopStems == null || _stopStems.contains(stem) == false) {
					
					int matchStart = blockMatcher.start() + matcher.start(GROUP);
					int matchEnd = blockMatcher.start() + matcher.end(GROUP);
					Hit hit = new Hit(matchStart, matchEnd);
					toAdd.add(new Pair<String, Hit>(stem, hit));
				}
				
			} else {

				// check if composite word
				matcher = _patternWordComposite.matcher(block);
				if (matcher.find()) {
					
					final int GROUP1 = 2;
					final int GROUP2 = 3;

					String word1 = matcher.group(GROUP1);
					String stem1 = _porterStemmer.stem(word1);

					String word2 = matcher.group(GROUP2);
					String stem2 = _porterStemmer.stem(word2);
					
					if (_stopStems == null || _stopStems.contains(stem1) == false) {
						int matchStart = blockMatcher.start() + matcher.start(GROUP1);
						int matchEnd = blockMatcher.start() + matcher.end(GROUP1);
						Hit hit = new Hit(matchStart, matchEnd);
						toAdd.add(new Pair<String, Hit>(stem1, hit));
					}
					
					if (_stopStems == null || _stopStems.contains(stem2) == false) {
						int matchStart = blockMatcher.start() + matcher.start(GROUP2);
						int matchEnd = blockMatcher.start() + matcher.end(GROUP2);
						Hit hit = new Hit(matchStart, matchEnd);
						toAdd.add(new Pair<String, Hit>(stem2, hit));
					}
									
				} else {
					
					matcher = _patternAbbrev.matcher(block);
					if (matcher.find()) {
						
						final int GROUP = 2;
						
						String word = matcher.group(GROUP);
						String stem = word.toLowerCase();
						
						if (_stopStems == null || _stopStems.contains(stem) == false) {
							
							int matchStart = blockMatcher.start() + matcher.start(GROUP);
							int matchEnd = blockMatcher.start() + matcher.end(GROUP);
							Hit hit = new Hit(matchStart, matchEnd);
							toAdd.add(new Pair<String, Hit>(stem, hit));
						}
					}
				}
			}
			
			for (int i=0; i<toAdd.size(); i++) {
				
				Pair<String, Hit> pair = toAdd.get(i);
				String stem = pair.v1();
				Hit hit = pair.v2();
				
				if (hitsByStem == null) {
					hitsByStem = new HashMap<String, List<Hit>>();
				}
				List<Hit> hits = hitsByStem.get(stem);
				if (hits == null) {
					hits = new ArrayList<Hit>();
					hitsByStem.put(stem, hits);
				}
				hits.add(hit);
			}
		}
		
		return hitsByStem;
	}
	
}
