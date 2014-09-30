package me.akuz.ts.sync;

public interface TimeMovable<T extends Comparable<T>> {

	void moveToTime(final T time);

}
