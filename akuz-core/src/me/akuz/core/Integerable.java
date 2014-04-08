package me.akuz.core;

/**
 * Something that can be converted to Integer and back.
 *
 */
public interface Integerable {
	
	Integer convertToInteger();
	
	Object convertFromInteger(Integer num);
}
