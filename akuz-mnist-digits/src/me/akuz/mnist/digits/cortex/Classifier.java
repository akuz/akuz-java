package me.akuz.mnist.digits.cortex;

import me.akuz.core.math.DirDist;
import me.akuz.core.math.StatsUtils;

/**
 * Note: not thread safe.
 */
public class Classifier {
	
	private static final double ALPHA = 10.0;
	
	private final Layer _layer;
	private final DirDist _classDist;
	private final DirDist[] _inClassDists;
	
	private final double[] _lastClassInput;
	private final double[] _lastClassProbs;
	
	public Classifier(final Layer layer, final int classCount) {
		
		final Column[][] columns = layer.getColumns();
		final int dim = columns.length * columns[0].length * columns[0][0].getNeurons().length;
		
		_layer = layer;
		_classDist = new DirDist(classCount, 0.000000001);
		_inClassDists = new DirDist[classCount];
		for (int i=0; i<_inClassDists.length; i++) {
			_inClassDists[i] = new DirDist(dim, ALPHA);
		}
		
		_lastClassInput = new double[dim];
		_lastClassProbs = new double[classCount];
	}
	
	public void observe(final int classIndex) {
		
		_classDist.addObservation(classIndex, 1.0);
		final DirDist inClassDist = _inClassDists[classIndex];
		final Column[][] columns = _layer.getColumns();
		
		int index = 0;
		for (int i=0; i<columns.length; i++) {
			for (int j=0; j<columns[i].length; j++) {
				
				final Neuron[] neurons = columns[i][j].getNeurons();
				for (int n=0; n<neurons.length; n++) {
					
					inClassDist.addObservation(index, neurons[n].getHistoricalPotential());
					index++;
				}
			}
		}
	}
	
	public double[] classify() {

		final Column[][] columns = _layer.getColumns();
		int index = 0;
		for (int i=0; i<columns.length; i++) {
			for (int j=0; j<columns[i].length; j++) {
				
				final Neuron[] neurons = columns[i][j].getNeurons();
				for (int n=0; n<neurons.length; n++) {
					
					_lastClassInput[index] = neurons[n].getHistoricalPotential();
					if (Double.isNaN(_lastClassInput[index])) {
						_lastClassInput[index] = 0.0; // FIXME
					}
					if (_lastClassInput[index] <= 0.0) {
						_lastClassInput[index] = 0.001; // FIXME
					}
					index++;
				}
			}
		}
		
		for (int c=0; c<_inClassDists.length; c++) {
			_lastClassProbs[c] 
					= Math.log(_classDist.getUnnormalisedPosteriorMean(c))
					+ _inClassDists[c].getPosteriorLogProb(_lastClassInput);
		}
		StatsUtils.logLikesToProbsReplace(_lastClassProbs);
		
		return _lastClassProbs;
	}

}
