package me.akuz.core.math;

public final class MatrixVar {
	
	private final int _columnIndex;
	private final double _columnWeight;
	
	public MatrixVar(int columnIndex, double columnWeight) {
		_columnIndex = columnIndex;
		_columnWeight = columnWeight;
	}
	
	public int getColumnIndex() {
		return _columnIndex;
	}
	
	public double getColumnWeight() {
		return _columnWeight;
	}
}
