package me.akuz.core;

/**
 * Something that can be converted to String and back.
 *
 */
public interface Stringable {
	
	String convertToString();
	
	Object parseFromString(String str);
}
