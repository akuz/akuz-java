package me.akuz.mnist.digits.hrp;

import java.util.ArrayList;
import java.util.List;

/**
 * Trained image analysis model, containing
 * blocks for use at different fractal levels.
 */
public final class Model {
	
	private final List<ModelLevel> _levels;
	
	public Model() {
		_levels = new ArrayList<>();
	}
	
	public void createNextLevel(final int dim) {
		final ModelLevel level = new ModelLevel(_levels.size(), dim);
		if (_levels.size() > 0) {
			_levels.get(_levels.size()-1).onNextLevelCreated(dim);
		}
		_levels.add(level);
	}
	
}
