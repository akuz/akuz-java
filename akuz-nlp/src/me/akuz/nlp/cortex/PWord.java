package me.akuz.nlp.cortex;

/**
 * Probabilistic word, with each char-place
 * represented as a discrete probability 
 * distribution over characters.
 *
 */
public final class PWord {
	
	private final int _charDim;
	private final PChar[] _chars;
	
	/**
	 * Initialize the PWord of specific size
	 * by assigning equal probability of all
	 * chars at each char-place.
	 * 
	 */
	public PWord(final int charDim, final int size) {
		if (charDim <= 1) {
			throw new IllegalArgumentException("Argument charDim must be > 1");
		}
		if (size < 0) {
			throw new IllegalArgumentException("Argument size must be non-negative");
		}
		_charDim = charDim;
		_chars = new PChar[size];
		for (int i=0; i<_chars.length; i++) {
			_chars[i] = new PChar(charDim);
		}
	}
	
	/**
	 * Initialize PWord from an array of specific PChars.
	 * 
	 */
	public PWord(final int charDim, final PChar[] chars) {
		if (charDim <= 1) {
			throw new IllegalArgumentException("Argument charDim must be > 1");
		}
		_charDim = charDim;
		_chars = chars;
		for (int i=0; i<_chars.length; i++) {
			if (_chars[i].getDim() != charDim) {
				throw new IllegalArgumentException(
						"Dimensionality of PChar (dim = " + _chars[i].getDim() + 
						") at index " + i + " doesn't mach the stated " +
						"dimensionality of chars: " + charDim);
			}
		}
	}
	
	public int getCharDim() {
		return _charDim;
	}
	
	public int size() {
		return _chars.length;
	}
	
	public PChar getChar(final int index) {
		return _chars[index];
	}
}
