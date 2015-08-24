package me.akuz.mnist.digits.hrp;

public final class FractalLayer {
	
	private final FractalNode[][] _nodes;
	
	public FractalLayer(
			final Layer layer,
			final int rowCount, 
			final int colCount) {
		
		if (rowCount < 1) {
			throw new IllegalArgumentException(
					"Row count must be >= 1, got " + rowCount);
		}
		if (colCount < 1) {
			throw new IllegalArgumentException(
					"Column count must be >= 1, got " + colCount);
		}
		
		_nodes = new FractalNode[rowCount][];
		for (int i=0; i<rowCount; i++) {
			_nodes[i] = new FractalNode[colCount];
			for (int j=0; j<colCount; j++) {
				_nodes[i][j] = new FractalNode(layer);
			}
		}
	}
	
	public int getRowCount() {
		return _nodes.length;
	}
	
	public int getColCount() {
		return _nodes[0].length;
	}
	
	public FractalNode[][] getNodes() {
		return _nodes;
	}
	
	public FractalNode getNode(final int i, final int j) {
		return _nodes[i][j];
	}

}
