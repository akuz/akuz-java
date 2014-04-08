package me.akuz.core;

import java.util.Comparator;

public final class HitsSorter implements Comparator<Hit> {

	@Override
	public int compare(Hit h1, Hit h2) {
		return h1.compareTo(h2);
	}

}
