package me.akuz.core.geom;

/**
 * Image represented with a matrix of bytes,
 * which can be interpreted as having 256 
 * gradations of gray-scale intensity.
 * 
 */
public final class ByteImage {

	private final byte[][] _data;
	
	public ByteImage(int rowCount, int colCount) {
		_data = new byte[rowCount][colCount];
	}
	
	public ByteImage(byte[][] data) {
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
		_data = data;
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
	
	public double getIntensity(int i, int j) {
		return (_data[i][j] & 0xFF) / 255.0;
	}

	public void setIntensity(int i, int j, double intensity) {
		if (intensity < 0 || intensity > 1) {
			throw new IllegalArgumentException("Intensity must be within interval [0,1]");
		}
		_data[i][j] = (byte)Math.round(255.0 * intensity);
	}

}
