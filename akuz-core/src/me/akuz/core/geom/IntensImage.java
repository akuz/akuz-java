package me.akuz.core.geom;

public final class IntensImage {

	private final double[][] _data;
	
	public IntensImage(int rowCount, int colCount) {
		_data = new double[rowCount][colCount];
	}
	
	public IntensImage(ByteImage byteImage) {
		this(byteImage.getRowCount(), byteImage.getColCount());
		for (int i=0; i<getRowCount(); i++) {
			for (int j=0; j<getColCount(); j++) {
				setIntensity(i, j, byteImage.getIntensity(i, j));
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
	
	public double getIntensity(final int i, final int j) {
		return _data[i][j];
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
		_data[i][j] = intensity;
	}
	
	public String toAsciiArt() { 
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<getRowCount(); i++) {
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
			sb.append("  | ");
			for (int j=0; j<getColCount(); j++) {
				
				final double intensity = getIntensity(i, j);

				sb.append(" ");
				sb.append(String.format("%.2f", intensity));
			}
			sb.append(System.lineSeparator());
		}
		return sb.toString();
	}
}
