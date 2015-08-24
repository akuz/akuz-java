package me.akuz.core.geom;

/**
 * Black and white image.
 *
 */
public final class BWImage {

	private final double[][] _data;
	
	public BWImage(int rowCount, int colCount) {
		_data = new double[rowCount][colCount];
	}
	
	public BWImage(ByteImage byteImage) {
		this(byteImage.getRowCount(), byteImage.getColCount());
		for (int i=0; i<getRowCount(); i++) {
			for (int j=0; j<getColCount(); j++) {
				setColor(i, j, byteImage.getIntensity(i, j));
			}
		}
	}
	
	public double[][] getData() {
		return _data;
	}
	
	public int getRowCount() {
		return _data.length;
	}
	
	public int getColCount() {
		return _data[0].length;
	}
	
	public double getColor(final int i, final int j) {
		return _data[i][j];
	}

	public void addColor(final int i, final int j, final double color) {
		setColor(i, j, getColor(i, j) + color);
	}

	public void setColor(final int i, final int j, double color) {
		if (color < 0) {
			color = 0.0;
		}
		if (color > 1) {
			color = 1;
		}
		_data[i][j] = color;
	}
	
	public String toAsciiArt() { 
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<getRowCount(); i++) {
			for (int j=0; j<getColCount(); j++) {
				
				final double intensity = getColor(i, j);
				
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
			sb.append("  | ");
			for (int j=0; j<getColCount(); j++) {
				
				final double intensity = getColor(i, j);

				sb.append(" ");
				sb.append(String.format("%.2f", intensity));
			}
			sb.append(System.lineSeparator());
		}
		return sb.toString();
	}
}
