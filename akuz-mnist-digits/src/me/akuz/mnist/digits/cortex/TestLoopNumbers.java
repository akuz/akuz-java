package me.akuz.mnist.digits.cortex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.akuz.core.geom.BWImage;
import me.akuz.core.math.StatsUtils;
import me.akuz.mnist.digits.load.MNIST;

public final class TestLoopNumbers implements Runnable {

	private final Brain _brain;
	private final Classifier _classifier;
	private final TestPanel _panel;
	
	public TestLoopNumbers(final TestPanel panel) {

		_brain = new Brain(14, 14, 4, 9, 16);
		_classifier = new Classifier(_brain.getHighestLayer(), 10);

		_panel = panel;
		_panel.setBrain(_brain);
	}

	@Override
	public void run() {
		
		final String fileName = "/Users/andrey/SkyDrive/Documents/Data/digits_input/train.csv";
		final int maxImageCount = 500;
		
		List<Integer> digits = new ArrayList<>();
		List<BWImage> images = new ArrayList<>();
		try {
			MNIST.load_train(fileName, digits, images, maxImageCount);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		// sort by digit
		List<List<BWImage>> byDigit = new ArrayList<>();
		for (int d=0; d<10; d++) {
			byDigit.add(new ArrayList<BWImage>());
		}
		for (int i=0; i<images.size(); i++) {
			byDigit.get(digits.get(i)).add(images.get(i));
		}
		
		double slowDownFactor = 10.0;
		
		long lastTickTime = System.currentTimeMillis();
		
		long tickCounter = 1;
		int currentDigit = -1;
		final BWImage emptyImage = new BWImage(28, 28);
		List<BWImage> currentImages = null;
		int currentImageIndex = -1;
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
			if (currentImages == null || tickCounter % 100 == 0) {
				
				currentDigit = (currentDigit + 1) % 10;
				currentImages = byDigit.get(currentDigit);
				currentImageIndex = 0;
				
			} else {
				
				currentImageIndex = 0; // (byteImageIndex + 1) % byteImages.size();
			}
			BWImage currentImage = currentImages.get(currentImageIndex);
			if (tickCounter % 100 > 80) {
				currentImage = emptyImage;
			} else if (tickCounter % 100 > 20) {
				_classifier.observe(currentDigit);
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
						
						neurons[n].setCurrentPotential(_brain, currentImage.getColor(iImage, jImage));
					}
				}
			}
			
			final double[] probs = _classifier.classify();
			final int recognized = StatsUtils.maxValueIndex(probs);
			
			System.out.println("Tick " + (++tickCounter) + " => " + recognized);

			_brain.tick();
			
			_panel.repaint();
			
			lastTickTime = currentTime;
		}
	}

}
