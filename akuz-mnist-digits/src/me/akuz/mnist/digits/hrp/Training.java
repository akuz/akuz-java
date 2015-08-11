package me.akuz.mnist.digits.hrp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class Training {
	
	private final Random _rnd;
	private final List<Image> _images;
	private final int _iterations_per_layer;
	private final Model _model;
	private final List<Fractal> _fractals;
	
	public Training(
			final int[] dims,
			final List<Image> images,
			final int iterations_per_layer) {
		
		if (dims == null) {
			throw new NullPointerException("dims");
		}
		if (dims.length == 0) {
			throw new IllegalArgumentException("Argument dims length must be > 0");
		}
		
		_rnd = new Random(System.currentTimeMillis());
		_images = images;
		_iterations_per_layer = iterations_per_layer;

		_model = new Model(dims);
		_model.ensureDepth(_rnd, 1);
		final Layer firstLayer = _model.getFirstLayer();
		
		_fractals = new ArrayList<>(images.size());
		for (final Image image : images) {
			
			final Fractal fractal = new Fractal(firstLayer, image.getMinSize());
			_fractals.add(fractal);
		}
	}
	
	public void execute() {
		
		for (int depth = 1; depth <= _model.getDepthMaximum(); depth++) {
			
			_model.ensureDepth(_rnd, depth);
			
			for (final Fractal fractal : _fractals) {
				fractal.ensureDepth(_model.getLayers(), depth);
			}
			
			for (int iter=1; iter<=_iterations_per_layer; iter++) {
				
				System.out.println("Depth: " + depth + ", Iteration: " + iter);
				
				// E: expectation
				System.out.print("Expectation... ");
				for (int i=0; i<_images.size(); i++) {

					final Image image = _images.get(i);
					final Fractal fractal = _fractals.get(i);
					
					fractal.calculatePatchProbs(
							image,
							image.getCenterX(),
							image.getCenterY(),
							depth);
				}

				// M: reset model
				System.out.print("Reset... ");
				_model.reset();
				
				// M: maximization
				System.out.print("Maximization... ");
				for (final Fractal fractal : _fractals) {

					fractal.updatePatchProbs();
				}
				System.out.println("Done.");
			}
		}

	}

}
