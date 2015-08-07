package me.akuz.mnist.digits.hrp.prob;

public interface DiscreteDist {
	
	int getDim();

	double[] getValues();
	double getValue(int i);

	double[] getProbs();
	double getProb(int i);

	double getLogLike(int i);
}
