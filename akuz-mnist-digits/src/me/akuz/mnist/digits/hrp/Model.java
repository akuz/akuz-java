package me.akuz.mnist.digits.hrp;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Image analysis model, containing layers for 
 * analyzing the images at different levels of 
 * detalisation.
 */
public final class Model {
	
	private final int[] _dims;
	private final List<Layer> _layers;
	
	public Model(final int[] dims) {
		if (dims == null) {
			throw new NullPointerException("dims");
		}
		if (dims.length == 0) {
			throw new IllegalArgumentException(
					"Argument dims length must be > 0");
		}
		_dims = dims;
		_layers = new ArrayList<>();
	}
	
	public int[] getDims() {
		return _dims;
	}
	
	public int getDepthCurrent() {
		return _layers.size();
	}
	
	public int getDepthMaximum() {
		return _dims.length;
	}
	
	public List<Layer> getLayers() {
		return _layers;
	}
	
	public Layer getFirstLayer() {
		if (_layers.size() == 0) {
			throw new IllegalStateException("There are no layers in the model");
		}
		return _layers.get(0);
	}
	
	public void ensureDepth(
			final Random rnd,
			final int depth) {
		
		if (depth > _dims.length) {
			throw new IllegalStateException(
					"Requested model depth " + depth + 
					", but the maximum depth is " + _dims.length);
		}
		
		while (_layers.size() < depth) {
			
			final Layer nextLayer = new Layer(
					rnd, 
					_layers.size() + 1, 
					_dims[_layers.size()]);
			
			if (_layers.size() > 0) {
				_layers.get(_layers.size()-1).onNextLayerCreated(nextLayer);
			}
			
			_layers.add(nextLayer);
		}
	}
	
	public void normalize() {
		for (int i=0; i<_layers.size(); i++) {
			_layers.get(i).normalize();
		}
	}
	
	public void reset() {
		for (int i=0; i<_layers.size(); i++) {
			_layers.get(i).reset();
		}
	}
	
	public void print() {
		for (int i=0; i<_layers.size(); i++) {
			System.out.println("---------------------------");
			System.out.println("----- layer " + (i + 1));
			System.out.println("---------------------------");
			_layers.get(i).print();
		}
	}
	
}
