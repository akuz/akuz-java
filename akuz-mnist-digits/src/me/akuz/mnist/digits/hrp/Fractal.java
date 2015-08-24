package me.akuz.mnist.digits.hrp;

import java.util.ArrayList;
import java.util.List;

public final class Fractal {
	
	private final Model _model;
	private final List<FractalLayer> _layers;
	
	public Fractal(final Model model) {
		_model = model;
		_layers = new ArrayList<>();
	}
	
	public Model getModel() {
		return _model;
	}
	
	public List<FractalLayer> getLayers() {
		return _layers;
	}

	public void ensureDepth(final int requestedDepth) {
		
		final List<Layer> modelLayers = _model.getLayers();
		
		if (requestedDepth > modelLayers.size()) {

			// checking here that there are enough
			// layers for the created fractal
			throw new IllegalStateException(
					"Requested fractal depth is " + requestedDepth +
					", but there are not enough layers in the " + 
					"model (" + modelLayers.size() + ")");
		}
		
		while (_layers.size() < requestedDepth) {
			
			final int nextDepth = _layers.size() + 1;
			final Layer nextModelLayer = modelLayers.get(nextDepth-1);
			final FractalLayer nextLayer = new FractalLayer(nextModelLayer, nextDepth, nextDepth);
			
			if (_layers.size() > 0) {
				
				final FractalLayer prevLayer = _layers.get(_layers.size()-1);
				final FractalNode[][] prevNodes = prevLayer.getNodes();
				
				for (int i=0; i<prevNodes.length; i++) {
					for (int j=0; j<prevNodes[i].length; j++) {
						
						final FractalNode parentNode = prevNodes[i][j];
						
						for (int k=0; k<2; k++) {
							for (int l=0; l<2; l++) {

								final FractalNode childNode = nextLayer.getNode(i+k, j+l);
								final FractalLink link = new FractalLink(parentNode, childNode);
								
								parentNode.setChild(k, l, link);
								
								// for the child, the parent
								// is in the opposite direction
								childNode.setParent(1-k, 1-l, link);
							}
						}
					}
				}
			}
		}
	}
	
}
