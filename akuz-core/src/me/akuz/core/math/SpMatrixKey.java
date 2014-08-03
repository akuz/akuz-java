package me.akuz.core.math;

public final class SpMatrixKey {
	
	private final int _i;
	private final int _j;
	
	public SpMatrixKey(final int i, final int j) {
		_i = i;
		_j = j;
	}
	
	@Override
	public final int hashCode() {
		int hash = 23;
		hash = hash * 31 + _i;
		hash = hash * 31 + _j;
		return hash;
	}
	
	@Override
	public final boolean equals(final Object obj) {
		SpMatrixKey o = (SpMatrixKey)obj;
		return _i == o._i && _j == o._j;
	}

}
