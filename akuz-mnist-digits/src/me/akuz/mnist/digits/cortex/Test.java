package me.akuz.mnist.digits.cortex;

public class Test {

	public static void main(String[] args) throws InterruptedException {
		
		final Brain brain = new Brain(10, 10, 5, 4);
		
		double slowDownFactor = 30.0;
		
		long lastTickTime = System.currentTimeMillis();
		
		long counter = 0;
		while (true) {
			
			final long nextTickTime = lastTickTime + (long)(1000.0 * brain.getTickDuration() * slowDownFactor);

			long currentTime = System.currentTimeMillis();
			
			if (nextTickTime > currentTime) {
				Thread.sleep(nextTickTime - currentTime);
			} else if (nextTickTime < currentTime) {
				System.out.println("Computation is too slow, delayed by " + (currentTime - nextTickTime) + " ms");
			}
			
			currentTime = System.currentTimeMillis();
			
			// TODO: update retina
			
			System.out.println("Tick " + (++counter));

			brain.tick();
			
			printBrain(brain);
			
			lastTickTime = currentTime;
		}
	}
	
	private static final void printBrain(final Brain brain) {
		
		System.out.println("-------------------------------------------");
		System.out.println("-------------------------------------------");
		System.out.println("-------------------------------------------");
		
		final Layer[] layers = brain.getLayers();
		
		for (int l=0; l<layers.length; l++) {

			System.out.println("-------------------------------------------");
			System.out.println("-------------------------------------------");
			
			final Layer layer = layers[l];
			
			Column[][] columns = layer.getColumns();
			
			for (int i=0; i<columns.length; i++) {
				for (int j=0; j<columns[i].length; j++) {

					System.out.println("-------------------------------------------");

					final Column column = columns[i][j];
					
					final Neuron[] neurons = column.getNeurons();
					
					for (int n=0; n<neurons.length; n++) {
						
						final Neuron neuron = neurons[n];
						
						System.out.println(neuron.getCurrentPotential());
						
					}
				}
			}
		}
		// TODO
	}

}
