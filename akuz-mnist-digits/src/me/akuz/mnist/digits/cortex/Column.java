package me.akuz.mnist.digits.cortex;

import java.util.concurrent.ThreadLocalRandom;

import me.akuz.core.math.Randoms;
import me.akuz.core.math.StatsUtils;

public final class Column {
	
	private final Neuron[] _neurons;
	
	public Column(
			final int thisColumnHeight,
			final int lowerColumnHeight) {
		
		_neurons = new Neuron[thisColumnHeight];
		for (int l=0; l<thisColumnHeight; l++) {
			_neurons[l] = new Neuron(lowerColumnHeight);
		}
	}

	public Neuron[] getNeurons() {
		return _neurons;
	}
	
	public void beforeUpdate() {
		for (int i=0; i<_neurons.length; i++) {
			_neurons[i].beforeUpdate();
		}
	}
	
	public void update(
			final Brain brain,
			final int i0,
			final int j0,
			final Layer lowerLayer,
			final Layer higherLayer) {
		
		// ----------------
		// time persistence
		//
		final double decay = Math.exp(-brain.getDecayLambda() * brain.getTickDuration());
		final double[] timeProbs = new double[_neurons.length];
		for (int n=0; n<_neurons.length; n++) {
			
			final Neuron neuron = _neurons[n];
			timeProbs[n] = neuron.getPreviousPotential() * decay;
		}
		
		// -------------------------
		// top-down information flow
		//
		double[] higherProbs = null;
		if (higherLayer != null) {

			final Column[][] higherColumns = higherLayer.getColumns();
			
			higherProbs = new double[_neurons.length];
			int higherColumnCount = 0;
			
			for (int i=0; i<=1; i++) {
				final int ii = i0 + i;
				if (ii >= 0 && ii < higherColumns.length) {
					final Column[] iiHigherColumns = higherColumns[ii];
					for (int j=0; j<=1; j++) {
						final int jj = j0 + j;
						if (jj >= 0 && jj < iiHigherColumns.length) {
							
							final Column higherColumn = iiHigherColumns[jj];
							final Neuron[] higherNeurons = higherColumn.getNeurons();
							
							for (int h=0; h<higherNeurons.length; h++) {
								
								final Neuron higherNeuron = higherNeurons[h];
								final double higherPotential = higherNeuron.getPreviousPotential();
								final Dendrite[] higherDendrites = higherNeuron.getDendrites();
								
								// carefully determine the correct dendrite
								// to get from the neuron of a higher layer
								// column: if the higher column is on top-left,
								// then we need to take its bottom-right dendrite,
								// if it's on top-right, we need to take its
								// bottom-left dendrite, and so on
								final Dendrite higherDendrite = higherDendrites[(1-i)*2 + 1-j];
								final double[] higherWeights = higherDendrite.getWeights();
								
								if (higherWeights.length != _neurons.length) {
									throw new IllegalStateException(
											"Higher layer dendrite has " + higherWeights.length +
											" weights, but the lower column has " + _neurons.length +
											" neurons.");
								}
								
								// sum up dendrite weights, weighted by the
								// higher neuron potential treated as probability
								for (int n=0; n<_neurons.length; n++) {
									higherProbs[n] += higherWeights[n] * higherPotential; 
								}
							}
							
							higherColumnCount++;
						}
					}
				}
			}
			
			if (higherColumnCount == 0) {
				throw new IllegalStateException(
						"Could not find any columns in the higher layer, " +
						"which would have dendrites into this column");
			}
			
			StatsUtils.normalize(higherProbs);
		}
		
		// --------------------------
		// bottom-up information flow
		//
		final double[] lowerProbs = new double[_neurons.length];
		for (int n=0; n<_neurons.length; n++) {
			lowerProbs[n] = _neurons[n].calculateLowerLogLike(i0, j0, lowerLayer);
		}
		StatsUtils.logLikesToProbsReplace(lowerProbs);
		
		// -------------------
		// combine information
		//
//		System.out.println("Time: " + StringUtils.arrayToString(timeProbs, ", "));
//		System.out.println("Lower: " + StringUtils.arrayToString(lowerProbs, ", "));
//		if (higherProbs != null) {
//			System.out.println("Higher: " + StringUtils.arrayToString(higherProbs, ", "));
//		}
		final double combineTimeWeight = brain.getCombineTimeWeight();
		final double combineLowerWeight = brain.getCombineLowerWeight();
		final double combineHigherWeight = brain.getCombineHigherWeight();
		final double[] probs = new double[_neurons.length];
		for (int n=0; n<_neurons.length; n++) {
			probs[n] += timeProbs[n] * combineTimeWeight;
			if (higherProbs == null) {
				probs[n] += lowerProbs[n] * (combineLowerWeight + combineHigherWeight);
			} else {
				probs[n] += lowerProbs[n] * combineLowerWeight;
				probs[n] += higherProbs[n] * combineHigherWeight;
			}
		}
		
		// ----------------------------------
		// activate at random, if all decayed
		//
		final double threshold = brain.getRandomActivationThreshold();
		boolean allBelowThreshold = true;
		for (int n=0; n<_neurons.length; n++) {
			if (probs[n] > threshold) {
				allBelowThreshold = false;
				break;
			}
		}
		final double randomActivationProbability = brain.getRandomActivationProbability();
		if (allBelowThreshold && ThreadLocalRandom.current().nextDouble() < randomActivationProbability) {
			
			StatsUtils.normalize(probs);
			
			// activate random neuron in this column
			final int randomNeuronIndex = brain.getRandoms().nextDiscrete(probs);
			for (int n=0; n<_neurons.length; n++) {
				probs[n] = (n == randomNeuronIndex) ? 1.0 : 0.0;
			}
		}
		
		// -----------------------------
		// set current neuron potentials
		//
		for (int n=0; n<_neurons.length; n++) {
			_neurons[n].setCurrentPotential(probs[n]);
		}
	}
	
	public void afterUpdate(final Brain brain) {
		
		// TODO: weights learning
	}
}
