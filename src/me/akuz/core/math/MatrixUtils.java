package me.akuz.core.math;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.akuz.core.FileUtils;
import me.akuz.core.Pair;
import me.akuz.core.PairComparator;
import me.akuz.core.SortOrder;
import Jama.Matrix;

public final class MatrixUtils {

	public final static void shiftColumnVertical(final Matrix matrix, final int columnIndex, final int shiftBy) {
		
		if (shiftBy < 0) {
			int i;
			for (i=0; i<matrix.getRowDimension() + shiftBy; i++) {
				matrix.set(i, columnIndex, matrix.get(i - shiftBy, columnIndex));
			}
			for (; i<matrix.getRowDimension(); i++) {
				matrix.set(i, columnIndex, Double.NaN);
			}
		} else if (shiftBy > 0) {
			int i;
			for (i=matrix.getRowDimension()-1; i>=shiftBy; i--) {
				matrix.set(i, columnIndex, matrix.get(i - shiftBy, columnIndex));
			}
			for (; i>=0; i--) {
				matrix.set(i, columnIndex, Double.NaN);
			}
		}
	}

	public final static Matrix combineLeftAndRight(Matrix m1, Matrix m2) {
		
		if (m1.getRowDimension() != m2.getRowDimension()) {
			throw new InvalidParameterException("Matrices should have the same number of rows for combining left to right (got " + m1.getRowDimension() + " != " + m2.getRowDimension() + ")");
		}
		
		Matrix result = new Matrix(m1.getRowDimension(), m1.getColumnDimension() + m2.getColumnDimension());
		
		for (int i=0; i<m1.getRowDimension(); i++) {
			for (int j=0; j<m1.getColumnDimension(); j++) {
				result.set(i, j, m1.get(i, j));
			}
			for (int j=0; j<m2.getColumnDimension(); j++) {
				result.set(i, j + m1.getColumnDimension(), m2.get(i, j));
			}
		}
		
		return result;
	}

	public final static Matrix subtractColumns(Matrix m1, int col1, Matrix m2, int col2) {

		if (m1.getRowDimension() != m2.getRowDimension()) {
			throw new InvalidParameterException("Matrices should have the same number of rows (got " + m1.getRowDimension() + " != " + m2.getRowDimension() + ")");
		}

		Matrix result = new Matrix(m1.getRowDimension(),1);
		
		for (int i=0; i<m1.getRowDimension(); i++) {
			result.set(i, 0, m1.get(i, col1) - m2.get(i, col2));
		}
		
		return result;
	}

	public final static Matrix columnSum(Matrix m1, int col1, Matrix m2, int col2) {

		if (m1.getRowDimension() != m2.getRowDimension()) {
			throw new InvalidParameterException("Matrices should have the same number of rows (got " + m1.getRowDimension() + " != " + m2.getRowDimension() + ")");
		}

		Matrix result = new Matrix(m1.getRowDimension(),1);
		
		for (int i=0; i<m1.getRowDimension(); i++) {
			result.set(i, 0, m1.get(i, col1) + m2.get(i, col2));
		}
		
		return result;
	}

	public final static void columnSet(Matrix mTarget, int columnTarget, Matrix mSource, int columnSource) {

		if (mTarget.getRowDimension() != mSource.getRowDimension()) {
			throw new InvalidParameterException("Matrices should have the same number of rows (got " + mTarget.getRowDimension() + " != " + mSource.getRowDimension() + ")");
		}

		for (int i=0; i<mTarget.getRowDimension(); i++) {
			mTarget.set(i, columnTarget, mSource.get(i, columnSource));
		}
	}
	
	public final static Matrix log(Matrix m) {
		Matrix result = new Matrix(m.getRowDimension(), m.getColumnDimension());
		for (int i=0; i<m.getRowDimension(); i++) {
			for (int j=0; j<m.getColumnDimension(); j++) {
				result.set(i, j, Math.log(m.get(i, j)));
			}
		}
		return result;
	}

	public static Matrix sqrt(Matrix m) {
		Matrix result = new Matrix(m.getRowDimension(), m.getColumnDimension());
		for (int i=0; i<m.getRowDimension(); i++) {
			for (int j=0; j<m.getColumnDimension(); j++) {
				result.set(i, j, Math.sqrt(m.get(i, j)));
			}
		}
		return result;
	}

	public static Matrix pow(Matrix m, double power) {
		Matrix result = new Matrix(m.getRowDimension(), m.getColumnDimension());
		for (int i=0; i<m.getRowDimension(); i++) {
			for (int j=0; j<m.getColumnDimension(); j++) {
				double value = m.get(i, j);
				result.set(i, j, Math.pow(value, power));
			}
		}
		return result;
	}

	public static String formatDouble(DecimalFormat fmt, double value) {
		if (Double.isNaN(value)) {
			return "NaN";
		} else if (Double.isInfinite(value)) {
			if (value > 0) {
				return "+Inf";
			} else {
				return "-Inf";
			}
		} else {
			return fmt.format(value);
		}
	}

	public static String toString(Matrix m) {
		
		DecimalFormat fmt = new DecimalFormat("0.00000000");
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<m.getRowDimension(); i++) {
			for (int j=0; j<m.getColumnDimension(); j++) {
				double value = m.get(i, j);
				if (Double.isNaN(value)) {
					sb.append("");
				} else if (Double.isInfinite(value)) {
					if (value > 0) {
						sb.append("+Inf");
					} else {
						sb.append("-Inf");
					}
				} else {
					sb.append(fmt.format(value));
				}
				if (j<m.getColumnDimension()-1) {
					sb.append(",");
				}
			}
			sb.append("\n");
		}
		
		return sb.toString();
	}

	public static void writeMatrix(String fileName, Matrix m) throws IOException {
		
		FileUtils.writeEntireFile(fileName, toString(m));
	}

	public static final void columnSet(Matrix m, int columnIndex, double[] columnData) {
		
		if (m.getRowDimension() != columnData.length) {
			throw new InvalidParameterException("Column data length should equal the number of rows in matrix");
		}
		
		for (int i=0; i<m.getRowDimension(); i++) {
			m.set(i, columnIndex, columnData[i]);
		}
	}

	public static final void columnAdd(Matrix m, int columnIndex, double[] columnData) {
		
		if (m.getRowDimension() != columnData.length) {
			throw new InvalidParameterException("Column data length should equal the number of rows in matrix");
		}
		
		for (int i=0; i<m.getRowDimension(); i++) {
			m.set(i, columnIndex, m.get(i, columnIndex) + columnData[i]);
		}
	}

	public final static List<Pair<Integer, Double>> sortColumn(SortOrder sortOrder, Matrix m, int j) {
		
		List<Pair<Integer, Double>> sorted = new ArrayList<Pair<Integer,Double>>();

		for (int i=0; i<m.getRowDimension(); i++) {
			sorted.add(new Pair<Integer, Double>(i, m.get(i, j)));
		}
		Collections.sort(sorted, new PairComparator<Integer, Double>(sortOrder));
		
		return sorted;
	}
	
	public final static List<Pair<Integer, Double>> compareColumns(SortOrder sortOrder, Matrix m, int j1, int j2) {
		
		List<Pair<Integer, Double>> sorted = new ArrayList<Pair<Integer,Double>>();

		for (int i=0; i<m.getRowDimension(); i++) {
			sorted.add(new Pair<Integer, Double>(i, m.get(i, j1) - m.get(i, j2)));
		}
		Collections.sort(sorted, new PairComparator<Integer, Double>(sortOrder));
		
		return sorted;
	}

	public final static List<Pair<Integer, Double>> compareColumnsLog(SortOrder sortOrder, Matrix m, int j1, int j2) {
		
		List<Pair<Integer, Double>> sorted = new ArrayList<Pair<Integer,Double>>();

		for (int i=0; i<m.getRowDimension(); i++) {
			sorted.add(new Pair<Integer, Double>(i, Math.log(m.get(i, j1)) - Math.log(m.get(i, j2))));
		}
		Collections.sort(sorted, new PairComparator<Integer, Double>(sortOrder));
		
		return sorted;
	}
	public final static List<Pair<Integer, Double>> compareColumnsDiv(SortOrder sortOrder, Matrix m, int j1, int j2) {
		
		List<Pair<Integer, Double>> sorted = new ArrayList<Pair<Integer,Double>>();

		for (int i=0; i<m.getRowDimension(); i++) {
			double v1 = m.get(i, j1);
			double v2 = m.get(i, j2);
			sorted.add(new Pair<Integer, Double>(i, v1 / v2));
		}
		Collections.sort(sorted, new PairComparator<Integer, Double>(sortOrder));
		
		return sorted;
	}

	public static Matrix getRows(Matrix m, int startRow, int endRow) {
		Matrix res = new Matrix(endRow - startRow, m.getColumnDimension());
		for (int i=startRow; i<endRow; i++) {
			for (int j=0; j<m.getColumnDimension(); j++) {
				res.set(i - startRow, j, m.get(i, j));
			}
		}
		return res;
	}

	public static Matrix getColumns(Matrix m, int startColumn, int endColumn) {
		Matrix res = new Matrix(m.getRowDimension(), endColumn - startColumn);
		for (int i=0; i<m.getRowDimension(); i++) {
			for (int j=startColumn; j<endColumn; j++) {
				res.set(i, j - startColumn, m.get(i, j));
			}
		}
		return res;
	}
	
	public static Matrix accumulateRows(Matrix m) {
		return accumulateRows(m, 0, m.getRowDimension());
	}
	
	public static Matrix accumulateRows(Matrix m, int startRow, int endRow) {
		
		Matrix res = (Matrix)m.clone();

		for (int i=startRow+1; i<endRow; i++) {
			
			for (int j=0; j<m.getColumnDimension(); j++) {
				
				res.set(i, j, res.get(i-1, j) + m.get(i, j));
			}
		}
		
		return res;
	}

	public static void normalizeColumns(Matrix m) {
		for (int j=0; j<m.getColumnDimension(); j++) {
			normalizeColumn(m, j);
		}
	}

	public static void normalizeColumn(Matrix m, int j) {
		double sum = 0;
		for (int i=0; i<m.getRowDimension(); i++) {
			sum += Math.abs(m.get(i, j));
		}
		for (int i=0; i<m.getRowDimension(); i++) {
			m.set(i, j, m.get(i, j) / sum);
		}
	}

	public final static void normalizeRows(Matrix m) {
		for (int i=0; i<m.getRowDimension(); i++) {
			normalizeRow(m, i);
		}
	}

	public final static void normalizeRow(Matrix m, int i) {
		double sum = 0;
		for (int j=0; j<m.getColumnDimension(); j++) {
			sum += Math.abs(m.get(i, j));
		}
		for (int j=0; j<m.getColumnDimension(); j++) {
			m.set(i, j, m.get(i, j) / sum);
		}
	}

	public static Matrix sumRows(Matrix m) {
		Matrix res = new Matrix(1, m.getColumnDimension());
		for (int j=0; j<m.getColumnDimension(); j++) {
			for (int i=0; i<m.getRowDimension(); i++) {
				res.set(0, j, res.get(0, j) + m.get(i, j));
			}
		}
		return res;
	}

	public static Matrix sumColumns(Matrix m) {
		Matrix res = new Matrix(m.getRowDimension(), 1);
		for (int i=0; i<m.getRowDimension(); i++) {
			for (int j=0; j<m.getColumnDimension(); j++) {
				res.set(i, 0, res.get(i, 0) + m.get(i, j));
			}
		}
		return res;
	}

	public static double[] columnToArray(Matrix m, int j) {
		double[] arr = new double[m.getRowDimension()];
		for (int i=0; i<m.getRowDimension(); i++) {
			arr[i] = m.get(i, j);
		}
		return arr;
	}

	public static double[] rowToArray(Matrix m, int i) {
		double[] arr = new double[m.getColumnDimension()];
		for (int j=0; j<m.getColumnDimension(); j++) {
			arr[j] = m.get(i, j);
		}
		return arr;
	}

	public static Matrix subtractEachRow(Matrix m, Matrix subtractRow) {
		return subtractEachRow(m, subtractRow, 0, m.getRowDimension());
	}

	public static Matrix subtractEachRow(Matrix m, Matrix subtractRow, int startRow, int endRow) {
		Matrix res = (Matrix)m.clone();
		subtractEachRow_inPlace(res, subtractRow, startRow, endRow);
		return res;
	}
	
	public static void subtractEachRow_inPlace(Matrix m, Matrix subtractRow) {
		subtractEachRow_inPlace(m, subtractRow, 0, m.getRowDimension());
	}

	public static void subtractEachRow_inPlace(Matrix m, Matrix subtractRow, int startRow, int endRow) {
		if (m.getColumnDimension() != subtractRow.getColumnDimension()) {
			throw new IllegalArgumentException("Column dimensions don't match");
		}
		if (subtractRow.getRowDimension() != 1) {
			throw new IllegalArgumentException("Matrix subtractRow must have only one row");
		}
		for (int i=startRow; i<endRow; i++) {
			for (int j=0; j<m.getColumnDimension(); j++) {
				m.set(i, j, m.get(i, j) - subtractRow.get(0, j));
			}
		}
	}

	public static Matrix subtractEachColumn(Matrix m, Matrix subtractCol) {
		return subtractEachColumn(m, subtractCol, 0, m.getColumnDimension());
	}

	public static Matrix subtractEachColumn(Matrix m, Matrix subtractCol, int startCol, int endCol) {
		Matrix res = (Matrix)m.clone();
		subtractEachColumn_inPlace(res, subtractCol, startCol, endCol);
		return res;
	}

	public static void subtractEachColumn_inPlace(Matrix m, Matrix subtractCol) {
		subtractEachColumn_inPlace(m, subtractCol, 0, m.getColumnDimension());
	}

	public static void subtractEachColumn_inPlace(Matrix m, Matrix subtractCol, int startCol, int endCol) {
		if (m.getRowDimension() != subtractCol.getRowDimension()) {
			throw new IllegalArgumentException("Row dimensions don't match");
		}
		if (subtractCol.getColumnDimension() != 1) {
			throw new IllegalArgumentException("Matrix subtractCol must have only one column");
		}
		for (int i=0; i<m.getRowDimension(); i++) {
			for (int j=startCol; j<endCol; j++) {
				m.set(i, j, m.get(i, j) - subtractCol.get(i, 0));
			}
		}
	}

	public static Matrix addEachRow(Matrix m, Matrix addRow) {
		return addEachRow(m, addRow, 0, m.getRowDimension());
	}

	public static Matrix addEachRow(Matrix m, Matrix addRow, int startRow, int endRow) {
		Matrix res = (Matrix)m.clone();
		addEachRow_inPlace(res, addRow, startRow, endRow);
		return res;
	}

	public static void addEachRow_inPlace(Matrix m, Matrix addRow) {
		addEachRow_inPlace(m, addRow, 0, m.getRowDimension());
	}

	public static void addEachRow_inPlace(Matrix m, Matrix addRow, int startRow, int endRow) {
		if (m.getColumnDimension() != addRow.getColumnDimension()) {
			throw new IllegalArgumentException("Column dimensions don't match");
		}
		if (addRow.getRowDimension() != 1) {
			throw new IllegalArgumentException("Matrix addRow must have only one row");
		}
		for (int i=startRow; i<endRow; i++) {
			for (int j=0; j<m.getColumnDimension(); j++) {
				m.set(i, j, m.get(i, j) + addRow.get(0, j));
			}
		}
	}

	public static Matrix addEachColumn(Matrix m, Matrix addCol) {
		return addEachColumn(m, addCol, 0, m.getColumnDimension());
	}

	public static Matrix addEachColumn(Matrix m, Matrix addCol, int startCol, int endCol) {
		Matrix res = (Matrix)m.clone();
		addEachColumn_inPlace(res, addCol, startCol, endCol);
		return res;
	}

	public static void addEachColumn_inPlace(Matrix m, Matrix addCol) {
		addEachColumn_inPlace(m, addCol, 0, m.getColumnDimension());
	}

	public static void addEachColumn_inPlace(Matrix m, Matrix addCol, int startCol, int endCol) {
		if (m.getRowDimension() != addCol.getRowDimension()) {
			throw new IllegalArgumentException("Row dimensions don't match");
		}
		if (addCol.getColumnDimension() != 1) {
			throw new IllegalArgumentException("Matrix addCol must have only one column");
		}
		for (int j=startCol; j<endCol; j++) {
			for (int i=0; i<m.getRowDimension(); i++) {
				m.set(i, j, m.get(i, j) + addCol.get(j, 0));
			}
		}
	}

	public static Matrix averageRows(Matrix m) {
		return averageRows(m, 0, m.getRowDimension());
	}

	public static Matrix averageRows(Matrix m, int startRow, int endRow) {
		int rowCount = endRow - startRow;
		Matrix res = new Matrix(1, m.getColumnDimension());
		for (int i=startRow; i<endRow; i++) {
			for (int j=0; j<m.getColumnDimension(); j++) {
				res.set(0, j, res.get(0, j) + m.get(i, j) / rowCount);
			}
		}
		return res;
	}

	public static void setRange(Matrix m, int startRow, int endRow, int startCol, int endCol, double value) {
		for (int i=startRow; i<endRow; i++) {
			for (int j=startCol; j<endCol; j++) {
				m.set(i, j, value);
			}
		}
	}

	public static void setRangeRows(Matrix m, int startRow, int endRow, double value) {
		for (int i=startRow; i<endRow; i++) {
			for (int j=0; j<m.getColumnDimension(); j++) {
				m.set(i, j, value);
			}
		}
	}

	public static void setRangeColumns(Matrix m, int startCol, int endCol, double value) {
		for (int i=0; i<m.getRowDimension(); i++) {
			for (int j=startCol; j<endCol; j++) {
				m.set(i, j, value);
			}
		}
	}

	public static Matrix arrayAsColumn(double[] arr) {
		final Matrix m = new Matrix(arr.length, 1);
		for (int i=0; i<arr.length; i++) {
			m.set(i, 0, arr[i]);
		}
		return m;
	}

	public static Matrix arrayAsRow(double[] arr) {
		final Matrix m = new Matrix(1, arr.length);
		for (int j=0; j<arr.length; j++) {
			m.set(0, j, arr[j]);
		}
		return m;
	}
}
