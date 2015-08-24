package me.akuz.mnist.digits.hrp;

public final class FractalLink {

	private final FractalNode _parentNode;
	private final FractalNode _childNode;
	private double[] _downPatchProbs;
	private double _upLinkProb;

	public FractalLink(
			final FractalNode parentNode,
			final FractalNode childNode) {

		_parentNode = parentNode;
		_childNode = childNode;
		_downPatchProbs = null;
		_upLinkProb = Double.NaN;
	}
	
	public FractalNode getParentNode() {
		return _parentNode;
	}
	
	public FractalNode getChildNode() {
		return _childNode;
	}
	
	public double[] getDownPatchProbs() {
		return _downPatchProbs;
	}
	
	public double getUpLinkProb() {
		return _upLinkProb;
	}

}
