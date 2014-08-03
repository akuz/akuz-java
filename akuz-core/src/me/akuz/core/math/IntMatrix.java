package me.akuz.core.math;

import java.util.Arrays;

public final class IntMatrix {

	private final int _rows;
	private final int _cols;
	private final int[][] _data;
	
	public IntMatrix(int rows, int cols) {
		this(rows, cols, 0);
	}
	
	public IntMatrix(int rows, int cols, int fill) {
		_rows = rows;
		_cols = cols;
		_data = new int[rows][];
		for (int i=0; i<rows; i++) {
			_data[i] = new int[cols];
			if (fill != 0) {
				Arrays.fill(_data[i], fill);
			}
		}
	}
	
	public int get(int row, int col) {
		return _data[row][col];
	}
	
	public void set(int row, int col, int value) {
		_data[row][col] = value;
	}

	public int getRowDimension() {
		return _rows;
	}
	
	public int getColumnDimension() {
		return _cols;
	}
}
