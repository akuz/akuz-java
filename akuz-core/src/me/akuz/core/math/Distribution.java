package me.akuz.core.math;

public interface Distribution {

	void addObservation(double value);
	void addObservation(double value, double weight);
	
	void removeObservation(double value);
	void removeObservation(double value, double weight);

	void reset();
}
