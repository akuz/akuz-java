package me.akuz.core;

import java.security.InvalidParameterException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringUtils {
	
	private static final Pattern _capitalLetter = Pattern.compile("[A-Z]");
	private static final Pattern _space = Pattern.compile("\\s");

	public static final String trim(String str, String regex) {
		if (str != null && str.length() > 0) {
			str = str.replaceAll("^" + regex + "{1,}", "");
			str = str.replaceAll(regex + "{1,}$", "");
		}
		return str;
	}

	public static final String unquote(String str) {
		if (str.length() >= 2) {
			if (str.startsWith("'") && str.endsWith("'")) {
				str = str.substring(1, str.length()-1);
			} else if (str.startsWith("\"") && str.endsWith("\"")) {
				str = str.substring(1, str.length()-1);
			} 
		}
		return str;
	}
	
	public static final String concatPath(String path0, String path1) {
		
		if (path0 == null || path0.length() == 0) {
			return path1;
		}
		if (path1 == null || path1.length() == 0) {
			return path0;
		}
		if (path0.endsWith("/")) {
			if (path1.startsWith("/")) {
				return path0 + path1.substring(1);
			} else {
				return path0 + path1;
			}
		} else {
			if (path1.startsWith("/")) {
				return path0 + path1;
			} else {
				return path0 + "/" + path1;
			}
		}
	}

	public static String collectionToString(Collection<?> c, String separator) {
		Iterator<?> i = c.iterator();
		StringBuffer sb = new StringBuffer();
		boolean isNext = false;
		while (i.hasNext()) {
			if (isNext) {
				sb.append(separator);
			} else {
				isNext = true;
			}
			sb.append(i.next());
		}
		return sb.toString();
	}

	public static String arrayToString(int[] c, String separator) {
		StringBuffer sb = new StringBuffer();
		
		for (int i=0; i<c.length; i++) {
			if (i > 0) {
				sb.append(separator);
			}
			sb.append(c[i]);
		}
		return sb.toString();
	}

	public final static String trimBySpace(String str, int maxLen) {
		
		final String elipsis = " ...";
		if (maxLen <= elipsis.length()) {
			throw new InvalidParameterException("Max length must be more than elipsis length of " + elipsis.length());
		}
		
		if (str != null && str.length() > maxLen) {
			
			int i = maxLen - elipsis.length();
			while (i>0) {
				char ch = str.charAt(i);
				if (ch == ' ') {
					break;
				}
				i--;
			}
			
			str = str.substring(0, i) + elipsis;
		}
		return str;
	}
	
	public final static String trimSharp(String str, int maxLen) {
		
		if (str != null && str.length() > maxLen) {
			str = str.substring(0, maxLen);
		}
		return str;
	}

	public final static String trimOrFillSpaces(String str, int len) {
		
		StringBuilder sb = new StringBuilder();
		if (str != null) {
			if (str.length() > len) {
				sb.append(str.substring(0, len));
			} else {
				sb.append(str);
			}
		}
		while(sb.length() < len) {
			sb.append(" ");
		}
		return sb.toString();
	}

	public static String formatDoubleArray(double[] arr, String format) {
		
		DecimalFormat fmt = new DecimalFormat(format);
		
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i=0; i<arr.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(fmt.format(arr[i]));
		}
		sb.append("]");
		return sb.toString();
	}

	public static String insertAt(String str, int at, String ins) {
		return str.substring(0, at) + ins + str.substring(at);
	}
	
	public static String toLowercaseFirstLetter(String str) {
		if (str == null || str.length() == 0) {
			return str;
		}
		
		char firstLetter = str.charAt(0);
		if (Character.isUpperCase(firstLetter)) {
			
			if (str.length() > 1) {
				return "" + Character.toLowerCase(firstLetter) + str.substring(1);
			} else {
				return "" + Character.toLowerCase(firstLetter);
			}

		} else {
			return str;
		}
 	}
	
	public static String toUppercaseFirstLetter(String str) {
		if (str == null || str.length() == 0) {
			return str;
		}
		
		char firstLetter = str.charAt(0);
		if (Character.isLowerCase(firstLetter)) {
			
			if (str.length() > 1) {
				return "" + Character.toUpperCase(firstLetter) + str.substring(1);
			} else {
				return "" + Character.toUpperCase(firstLetter);
			}

		} else {
			return str;
		}
 	}

	public static String capitalizeIfNoCaps(String str) {
		
		if (str == null || str.isEmpty()) {
			return str;
		}

		Matcher spaceMatcher = _space.matcher(str);
		String firstWord;
		String theRest;

		if (spaceMatcher.find()) {
			
			firstWord = str.substring(0, spaceMatcher.start());
			firstWord = capitalizeIfNoCaps(firstWord);
			theRest = str.substring(spaceMatcher.start());
			return String.format("%s%s", firstWord, theRest);
			
		} else {
			
			if (_capitalLetter.matcher(str).find()) {

				return str; // found some capitals, for example like in "iSomething"

			} else {
				if (str.length() > 1) {
					return String.format("%s%s", str.substring(0, 1).toUpperCase(), str.substring(1));
				} else {
					return str.substring(0, 1).toUpperCase();
				}
			}
		}
	}
	
	public static final StringBuilder appendIfNotEmpty(StringBuilder sb, String str) {
		if (sb.length() > 0) {
			sb.append(str);
		}
		return sb;
	}
}
