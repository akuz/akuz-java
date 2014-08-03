package me.akuz.core.math;

public class SparseMatrix<TKey, TValue> extends SparseVector<TKey, SparseVector<TKey, TValue>> {

	public SparseMatrix() {
		super();
	}

	public SparseMatrix(int initialCapacity) {
		super(initialCapacity);
	}
}
