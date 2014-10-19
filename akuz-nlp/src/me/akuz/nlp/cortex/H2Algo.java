package me.akuz.nlp.cortex;

import java.util.ArrayList;
import java.util.List;

import me.akuz.core.logs.LocalMonitor;
import me.akuz.core.logs.Monitor;

public class H2Algo {
	
	private final List<H2Layer> _layers;
	
	public H2Algo(
			final Monitor parentMonitor,
			final int dataDim,
			final List<PWord> data, 
			final int[] featureDims,
			final int maxIterationCount,
			final double logLikeChangeThreshold,
			final int passCount) {
		
		final Monitor monitor = parentMonitor == null ? null : new LocalMonitor(getClass().getSimpleName(), parentMonitor);
		
		_layers = new ArrayList<>();
		
		for (int passIndex=0; passIndex<passCount; passIndex++) {
			
			for (int layerIndex=0; layerIndex<featureDims.length; layerIndex++) {
				
				final H2Layer layer;
				
				if (layerIndex < _layers.size()) {
					
					// already created layer
					layer = _layers.get(layerIndex);
					
				} else {
					
					// determine new layer parameters
					int layerFeatureDim = featureDims[layerIndex];
					int layerDataDim;
					List<PWord> layerData;
					if (layerIndex > 0) {
						layerDataDim = _layers.get(layerIndex-1).getFeatureDim();
						layerData = _layers.get(layerIndex-1).getDataFeatures();
					} else {
						layerDataDim = dataDim;
						layerData = data;
					}
					
					// create new layer
					layer = new H2Layer(layerFeatureDim, layerData, layerDataDim);
					
					// set as parent
					if (layerIndex > 0) {
						_layers.get(layerIndex - 1).setParent(layer);
					}
				}
				
				layer.execute(monitor, maxIterationCount, logLikeChangeThreshold);
			}
		}
	}
	
	public List<H2Layer> getLayers() {
		return _layers;
	}

}
