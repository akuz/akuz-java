package me.akuz.core.geom;

/**
 * Image represented internally with a matrix of bytes (to save memory).
 * The pixels can be interpreted as having 256 gradations of gray-scale 
 * intensity (using get/setIntensity methods), or as 256 categories of 
 * color (using get/setCategory methods).
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
	
	public double getIntensity(final int i, final int j) {
		return (_data[i][j] & 0xFF) / 255.0;
	}

	public void addIntensity(final int i, final int j, final double intensity) {
		setIntensity(i, j, getIntensity(i, j) + intensity);
	}

	public void setIntensity(final int i, final int j, double intensity) {
		if (intensity < 0) {
			intensity = 0.0;
		}
		if (intensity > 1) {
			intensity = 1;
		}
		_data[i][j] = (byte)Math.round(255.0 * intensity);
	}
	
	public int getCategory(final int i, final int j) {
		return _data[i][j] & 0xFF;
	}
	
	public void setCategory(final int i, final int j, final int category) {
		if (category < 0 || category > 255) {
			throw new IllegalArgumentException("Category must be within interval [0,255]");
		}
		_data[i][j] = (byte)category;
	}
	
	public String toAsciiArt() { 
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<getRowCount(); i++) {
			for (int j=0; j<getColCount(); j++) {
				
				final double intensity = getIntensity(i, j);

				sb.append(" ");
				sb.append((int)Math.floor(intensity * 10));
			}
			sb.append("  | ");
			for (int j=0; j<getColCount(); j++) {
				
				final double intensity = getIntensity(i, j);
				
				if (intensity < 0.1) {
					sb.append("  ");
				} else if (intensity < 0.2) {
					sb.append(" .");
				} else if (intensity < 0.4) {
					sb.append(" +");
				} else if (intensity < 0.6) {
					sb.append(" @");
				} else {
					sb.append(" *");
				}
			}
			sb.append(System.lineSeparator());
		}
		return sb.toString();
	}
}
