package me.akuz.mnist.digits.cortex;

import java.util.concurrent.ThreadLocalRandom;

import me.akuz.core.math.DirDist;
import me.akuz.core.math.StatsUtils;

public final class Column {
	
	private final Neuron[] _neurons;
	
	public Column(
			final Brain brain,
			final int thisColumnHeight,
			final int lowerColumnHeight) {
		
		_neurons = new Neuron[thisColumnHeight];
		for (int l=0; l<thisColumnHeight; l++) {
			_neurons[l] = new Neuron(brain, lowerColumnHeight);
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
		final double[] newPotentials = new double[_neurons.length];
		for (int n=0; n<_neurons.length; n++) {
			
			newPotentials[n] =  _neurons[n].getPreviousPotential() * decay;
		}
		
		// ------------------------------------
		// determine if it's time to reactivate
		//
		final double maxPotential = StatsUtils.maxValue(newPotentials);
		if (maxPotential < brain.getReactivationThreshold() && 
			ThreadLocalRandom.current().nextDouble() < brain.getReactivationProbability()) {
			
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
									final DirDist higherWeightsDist = higherDendrite.getWeightsDist();
									
									if (higherWeightsDist.getDim() != _neurons.length) {
										throw new IllegalStateException(
												"Higher layer dendrite has " + higherWeightsDist.getDim() +
												" weights, but the lower column has " + _neurons.length +
												" neurons.");
									}
									
									// sum up dendrite weights, weighted by the
									// higher neuron potential treated as probability
									for (int n=0; n<_neurons.length; n++) {
										higherProbs[n] += higherWeightsDist.getUnnormalisedPosteriorMean(n) * higherPotential; 
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
			double[] lowerProbs = null;
			if (lowerLayer != null) {
				lowerProbs = new double[_neurons.length];
				for (int n=0; n<_neurons.length; n++) {
					lowerProbs[n] = _neurons[n].calculateLowerLogLike(i0, j0, lowerLayer);
				}
				StatsUtils.logLikesToProbsReplace(lowerProbs);
			}
			
			if (higherProbs == null && lowerProbs == null) {
				throw new IllegalStateException(
						"Column cannot be updated because both " +
						"higher and lower layers not provided");
			}
			
			// -------------------
			// combine information
			//
			final double combineLowerWeight = brain.getCombineLowerWeight();
			final double combineHigherWeight = brain.getCombineHigherWeight();
			for (int n=0; n<_neurons.length; n++) {
				newPotentials[n] = 0.0;
				if (higherProbs != null) {
					newPotentials[n] += higherProbs[n] * combineHigherWeight;
				}
				if (lowerProbs != null) {
					newPotentials[n] += lowerProbs[n] * combineLowerWeight;
				}
			}
			
			// --------------------------
			// activate highest potential
			//
			final int newActiveIndex = StatsUtils.maxValueIndex(newPotentials);
			final double newActivePotential = 0.97;
			final double newInactivePotential = (1.0 - newActivePotential) / (_neurons.length - 1);
			for (int n=0; n<_neurons.length; n++) {
				newPotentials[n] = (n == newActiveIndex) ? newActivePotential : newInactivePotential;
			}
			
		}

		// -------------------------
		// set new neuron potentials
		//
		for (int n=0; n<_neurons.length; n++) {
			_neurons[n].setCurrentPotential(brain, newPotentials[n]);
		}
	}
	
	public void afterUpdate(
			final Brain brain,
			final int i0,
			final int j0,
			final Layer lowerLayer) {
		
		for (int n=0; n<_neurons.length; n++) {
			
			if (lowerLayer != null) {
				_neurons[n].learnTick(brain, i0, j0, lowerLayer);
			}
			
			_neurons[n].updateHistoricalPotential(brain);
			
			
		}
	}
}
