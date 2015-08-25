package me.akuz.mnist.digits.cortex;

public final class Layer {
	
	private final Column[][] _columns;
	
	public Layer(
			final int dim1,
			final int dim2,
			final int thisColumnHeight,
			final int lowerColumnHeight) {
		
		_columns = new Column[dim1][dim2];
		for (int i=0; i<dim1; i++) {
			for (int j=0; j<dim2; j++) {
				_columns[i][j] = new Column(
						thisColumnHeight,
						lowerColumnHeight);
			}
		}
	}
	
	public Column[][] getColumns() {
		return _columns;
	}

	public void beforeUpdate() {
		for (int i=0; i<_columns.length; i++) {
			for (int j=0; j<_columns[i].length; j++) {
				_columns[i][j].beforeUpdate();
			}
		}
	}
	
	public void update(
			final Brain brain,
			final Layer lowerLayer,
			final Layer higherLayer) {
		
		for (int i=0; i<_columns.length; i++) {
			for (int j=0; j<_columns[i].length; j++) {
				_columns[i][j].update(brain, i, j, lowerLayer, higherLayer);
			}
		}
	}

	public void afterUpdate(final Brain brain) {
		for (int i=0; i<_columns.length; i++) {
			for (int j=0; j<_columns[i].length; j++) {
				_columns[i][j].afterUpdate(brain);
			}
		}
	}
}
