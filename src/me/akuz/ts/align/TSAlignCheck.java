package me.akuz.ts.align;

public abstract class TSAlignCheck implements Cloneable {

	public abstract TSAlignLogMsg next(double value);
	
	@Override
	protected Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError("Clone error");
		}
	}
}
