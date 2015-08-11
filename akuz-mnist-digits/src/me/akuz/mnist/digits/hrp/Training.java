package me.akuz.mnist.digits.hrp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class Training {
	
	private final Random _rnd;
	private final int[] _dims;
	private final Model _model;
	private final List<Fractal> _fractals;
	
	public Training(
			final int[] dims,
			final List<Image> images) {
		
		if (dims == null) {
			throw new NullPointerException("dims");
		}
		if (dims.length == 0) {
			throw new IllegalArgumentException("Argument dims length must be > 0");
		}
		
		_rnd = new Random(System.currentTimeMillis());
		
		_dims = dims;

		_model = new Model();
		_model.createNextLayer(_rnd, dims[0]);
		final Layer firstLayer = _model.getFirstLayer();

		_fractals = new ArrayList<>(images.size());
		for (Image image : images) {
			_fractals.add(new Fractal(firstLayer, image.getMinSize()));
		}
	}
	
	public void execute() {
		
		for (int depth=0; depth<_dims.length; depth++) {
			
			_model.ensureDepth(_rnd, _dims, depth);
			
			_fractals // ensure depth
			
			// loop E-M
			
			// TODO
		}
	}

}
