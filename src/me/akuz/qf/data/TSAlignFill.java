package me.akuz.qf.data;

public abstract class TSAlignFill implements Cloneable {

	public abstract TSAlignLogMsg next(double value);
	public abstract double get();
	
	@Override
	protected Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError("Clone error");
		}
	}
}
