package me.akuz.mnist.digits;

public final class Digit {

	private byte _symbol;
	private byte[][] _data;
	
	public Digit(byte symbol, byte[][] data) {
		if (data.length == 0) {
			throw new IllegalArgumentException("Data has no rows");
		}
		final int colCount = data[0].length;
		if (colCount == 0) {
			throw new IllegalArgumentException("Data has no columns");
		}
		for (int i=1; i<data.length; i++) {
			if (colCount != data[i].length) {
				throw new IllegalArgumentException("Inconsistent row lengths in data");
			}
		}
		_symbol = symbol;
		_data = data;
	}
	
	public int getSymbol() {
		return _symbol;
	}
	public void setSymbol(byte symbol) {
		_symbol = symbol;
	}
	
	public byte[][] getData() {
		return _data;
	}
	
	public int getRowCount() {
		return _data.length;
	}
	
	public int getColCount() {
		return _data[0].length;
	}

}
