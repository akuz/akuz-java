package me.akuz.mnist.digits.hrp.prob;

public final class GaussRebased implements GaussDist {
	
	private final GaussDist _parent;
	private final GaussDist _child;
	
	public GaussRebased(
			final GaussDist parent,
			final GaussDist child) {
		_parent = parent;
		_child = child;
	}

	@Override
	public double getMean() {		
		return _parent.getMean() + 
			   _child.getMean();
	}

	@Override
	public double getVariance() {
		return _child.getVariance();
	}

}
