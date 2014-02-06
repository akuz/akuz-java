package me.akuz.mnist.digits;

public final class Digit {

	private byte _symbol;
	private byte[][] _data;
	
	public Digit(byte symbol, byte[][] data) {
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

}
