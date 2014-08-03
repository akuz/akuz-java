package me.akuz.core.math;


public final class InvGammaCDF implements SingleArgFunction {
	
	private final double _alpha;
	private final double _beta;
	
	public InvGammaCDF(double alpha, double beta) {
		_alpha = alpha;
		_beta = beta;
	}
	
	public double getValueAt(double x) {
		return GammaFunction.incompleteGammaQ(_alpha, _beta / x);
	}

}
