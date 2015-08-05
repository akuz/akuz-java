package me.akuz.mnist.digits.hrp;

import java.util.ArrayList;
import java.util.List;

public final class ModelLevel {
	
	private final int _index;
	private final List<ModelBlock> _blocks;
	
	public ModelLevel(final int index, final int dim) {
		_index = index;
		_blocks = new ArrayList<>(dim);
		for (int i=0; i<dim; i++) {
			_blocks.add(new ModelBlock(index, i));
		}
	}
	
	public int getIndex() {
		return _index;
	}
	
	public void onNextLevelCreated(final int dim) {
		for (int i=0; i<_blocks.size(); i++) {
			_blocks.get(i).onNextLevelCreated(dim);
		}
	}

}
