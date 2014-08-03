package me.akuz.core.math;

public class SparseTensor<TKey, TValue> extends SparseVector<TKey, SparseMatrix<TKey, TValue>> {

	public SparseTensor(int initialCapacity) {
		super(initialCapacity);
	}
}
