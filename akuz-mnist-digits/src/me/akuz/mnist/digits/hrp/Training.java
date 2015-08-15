package me.akuz.mnist.digits.hrp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.akuz.mnist.digits.load.MNISTImage;

public final class Training {
	
	private final Map<Integer, double[]> _digitPatchProbs;
	
	private final List<Image> _images;
	private final Model _model;
	private final List<Fractal> _fractals;
	
	public Training(
			final List<MNISTImage> images,
			final List<LayerConfig> layerConfigs) {
		
		if (images == null) {
			throw new NullPointerException("images");
		}
		if (images.size() < 10) {
			throw new IllegalArgumentException("There must be >= 10 images in the training set");
		}
		
		if (layerConfigs == null) {
			throw new NullPointerException("layerConfigs");
		}
		if (layerConfigs.size() == 0) {
			throw new IllegalArgumentException("There must be at least one layerConfig");
		}
		
		// keep images
		_images = new ArrayList<>(images.size());
		for (final MNISTImage mnistImage : images) {
			_images.add(new Image(mnistImage));
		}

		// create model to train
		_model = new Model();
		
		// add first layer for 10 digits
		final LayerConfig firstLayerConfig = new LayerConfig(10, Spread.SPATIAL);
		final Layer firstLayer = _model.addLayer(firstLayerConfig);
		
		// add the rest of the layers based on specs
		for (final LayerConfig layerConfig : layerConfigs) {
			_model.addLayer(layerConfig);
		}
		
		// create fractals for all images
		_fractals = new ArrayList<>(images.size());
		for (int i=0; i<_images.size(); i++) {

			final Fractal fractal = new Fractal(firstLayer, null, -1);
			fractal.ensureDepth(_model.getLayers(), _model.getDepth());
			_fractals.add(fractal);
		}
		
		_digitPatchProbs = new HashMap<>();
		for (int digit = 0; digit < 10; digit++) {
			
			double[] probs = new double[10];
			probs[digit] = 1.0;
			_digitPatchProbs.put(digit, probs);
		}
	}
	
	public Model getModel() {
		return _model;
	}
	
	private void executeIterations(
			final String prefix,
			final int iterCount,
			final int minDepth,
			final int maxDepth) {

		for (int iter=1; iter<=iterCount; iter++) {
			
			System.out.print(prefix + ", Iteration: " + iter);
			
			// E: expectation
			System.out.print(", Expectation...");
			for (int i=0; i<_images.size(); i++) {
	
				final Image image = _images.get(i);
				final int digit = image.getDigit();
				final Fractal fractal = _fractals.get(i);
				
				double[] forcePatchProbs = _digitPatchProbs.get(digit);
				
				fractal.calculatePatchProbs(
						image,
						image.getCenterX(),
						image.getCenterY(),
						image.getMinSize(),
						minDepth,
						maxDepth,
						forcePatchProbs);
			}
	
			// M: maximization
			System.out.print(" Maximization...");
			_model.reset(minDepth, maxDepth);
			for (int i=0; i<_fractals.size(); i++) {
	
				_fractals.get(i).updatePatchProbs(
						minDepth,
						maxDepth);
			}
			_model.normalize(minDepth, maxDepth);
			System.out.println(" OK");
		}		
	}
	
	public void execute(int loops, int iterationsPerStep) {
		
		for (int loop=1; loop<=loops; loop++) {
			for (int s=_model.getLayers().size(); s>=1; s--) {
				int minDepth = s;
				int maxDepth = s;
				executeIterations(
						"Loop: " + loop + " (UP) (" + minDepth + "-" + maxDepth + ")", 
						iterationsPerStep, 
						minDepth, 
						maxDepth);
			}
			for (int s=2; s<_model.getLayers().size(); s++) {
				int minDepth = s;
				int maxDepth = s;
				executeIterations(
						"Loop: " + loop + " (DN) (" + minDepth + "-" + maxDepth + ")", 
						iterationsPerStep, 
						minDepth, 
						maxDepth);
			}
		}
		
		_model.print();
	}

}
