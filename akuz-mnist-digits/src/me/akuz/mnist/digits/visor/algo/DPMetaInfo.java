package me.akuz.mnist.digits.visor.algo;

public final class DPMetaInfo {
	
	private final double _alpha;
	private final double _logNorm;
	
	public DPMetaInfo(
			final double alpha,
			final double logNorm) {
		
		_alpha = alpha;
		_logNorm = logNorm;
	}
	
	public double alpha() {
		return _alpha;
	}
	
	public double logNorm() {
		return _logNorm;
	}

}
