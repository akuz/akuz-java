package me.akuz.mnist.digits.cortex;

import java.util.concurrent.ThreadLocalRandom;

public final class TestLoop implements Runnable {

	private final TestPanel _panel;
	private final Brain _brain;
	
	public TestLoop(final TestPanel panel) {

		_panel = panel;
		_brain = new Brain(10, 10, 9, 16);
		_panel.setBrain(_brain);
	}

	@Override
	public void run() {
		
		double slowDownFactor = 10.0;
		
		long lastTickTime = System.currentTimeMillis();
		
		long counter = 0;
		while (true) {
			
			final long nextTickTime = lastTickTime + (long)(1000.0 * _brain.getTickDuration() * slowDownFactor);

			long currentTime = System.currentTimeMillis();
			
			if (nextTickTime > currentTime) {
				try {
					Thread.sleep(nextTickTime - currentTime);
				} catch (InterruptedException e) {
					return;
				}
			} else if (nextTickTime < currentTime) {
				System.out.println("Computation is too slow, delayed by " + (currentTime - nextTickTime) + " ms");
			}
			
			currentTime = System.currentTimeMillis();
			
			// TODO: update retina
			if ((counter / 100) % 2 == 0) {
				final Layer retina = _brain.getRetina();
				final Column[][] columns = retina.getColumns();
				for (int i=0; i<columns.length; i++) {
					for (int j=0; j<columns[i].length; j++) {
						final Column column = columns[i][j];
						Neuron[] neurons = column.getNeurons();
						for (int n=0; n<neurons.length; n++) {
							neurons[n].setCurrentPotential(ThreadLocalRandom.current().nextDouble());
						}
					}
				}
			}
			
			System.out.println("Tick " + (++counter));

			_brain.tick();
			
			_panel.repaint();
			
			lastTickTime = currentTime;
		}
	}

}
