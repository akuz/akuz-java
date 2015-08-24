package me.akuz.mnist.digits.cortex;

public final class Layer {
	
	private final Column[][] _columns;
	
	public Layer(
			final int dim1,
			final int dim2,
			final int neuronsPerColumn) {
		
		_columns = new Column[dim1][dim2];
		for (int i=0; i<dim1; i++) {
			for (int j=0; j<dim2; j++) {
				_columns[i][j] = new Column(neuronsPerColumn);
			}
		}
	}
	
	public Column[][] getColumns() {
		return _columns;
	}

	public void beginUpdate() {
		for (int i=0; i<_columns.length; i++) {
			for (int j=0; j<_columns[i].length; j++) {
				_columns[i][j].beginUpdate();
			}
		}
	}

}
