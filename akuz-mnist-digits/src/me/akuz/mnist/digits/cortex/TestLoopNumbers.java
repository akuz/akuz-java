package me.akuz.mnist.digits.cortex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
		
		// sort by digit
		List<List<ByteImage>> byDigit = new ArrayList<>();
		for (int d=0; d<10; d++) {
			byDigit.add(new ArrayList<ByteImage>());
		}
		for (final MNISTImage mnistImage : mnistImages) {
			byDigit.get(mnistImage.getDigit()).add(mnistImage.getByteImage());
		}
		
		double slowDownFactor = 10.0;
		
		long lastTickTime = System.currentTimeMillis();
		
		long tickCounter = 1;
		int loopDigit = -1;
		List<ByteImage> byteImages = null;
		int byteImageIndex = -1;
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
			if (byteImages == null || tickCounter % 100 == 0) {
				
				loopDigit = (loopDigit + 1) % 10;
				byteImages = byDigit.get(loopDigit);
				byteImageIndex = 0;
				
			} else {
				
				byteImageIndex = (byteImageIndex + 1) % byteImages.size();
			}
			final ByteImage byteImage = byteImages.get(byteImageIndex);

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
			
			System.out.println("Tick " + (++tickCounter));

			_brain.tick();
			
			_panel.repaint();
			
			lastTickTime = currentTime;
		}
	}

}
