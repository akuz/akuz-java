package me.akuz.mnist.digits.hrp.prob;

public final class GaussOvertakeMean implements Gauss {
	
	private final String _name;
	private final Gauss _parent;
	private final Gauss _child;
	
	public GaussOvertakeMean(
			final String name,
			final Gauss parent,
			final Gauss child) {
		_name = name;
		_parent = parent;
		_child = child;
	}

	@Override
	public String getName() {
		return _name;
	}
	
	@Override
	public double getMean() {		
		return _parent.getMean() + 
			   _child.getMean();
	}

	@Override
	public double getTau() {
		return _child.getTau();
	}

}
