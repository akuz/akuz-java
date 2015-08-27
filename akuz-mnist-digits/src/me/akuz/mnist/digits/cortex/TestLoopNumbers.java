package me.akuz.mnist.digits.cortex;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import me.akuz.core.geom.ByteImage;
import me.akuz.mnist.digits.load.MNIST;
import me.akuz.mnist.digits.load.MNISTImage;

public final class TestLoopNumbers implements Runnable {

	private final TestPanel _panel;
	private final Brain _brain;
	
	public TestLoopNumbers(final TestPanel panel) {

		_panel = panel;
		_brain = new Brain(14, 14, 4, 9, 16);
		_panel.setBrain(_brain);
	}

	@Override
	public void run() {
		
		final String fileName = "/Users/andrey/SkyDrive/Documents/Data/digits_input/train.csv";
		final int maxImageCount = 500;
		
		List<MNISTImage> mnistImages = null;
		try {
			mnistImages = MNIST.load(fileName, maxImageCount);
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		
		double slowDownFactor = 10.0;
		
		long lastTickTime = System.currentTimeMillis();
		
		long counter = 0;
		ByteImage byteImage = null;
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
			
			// select image
			if (byteImage == null || (counter / 100) % 2 == 0) {
				
				final int randomImageIndex = ThreadLocalRandom.current().nextInt(mnistImages.size());
				final MNISTImage mnistImage = mnistImages.get(randomImageIndex);
				byteImage = mnistImage.getByteImage();
			}

			// update retina
			final Layer retina = _brain.getRetina();
			final Column[][] columns = retina.getColumns();
			for (int i=0; i<columns.length; i++) {
				for (int j=0; j<columns[i].length; j++) {
					final Column column = columns[i][j];
					Neuron[] neurons = column.getNeurons();
					for (int n=0; n<neurons.length; n++) {
						
						final int iImage = i*2 + n/2;
						final int jImage = j*2 + n%2;
						
						neurons[n].setCurrentPotential(_brain, byteImage.getIntensity(iImage, jImage));
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
