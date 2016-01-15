package me.akuz.mnist.digits;

import me.akuz.ml.tensors.DenseTensor;
import me.akuz.ml.tensors.Location;
import me.akuz.ml.tensors.Shape;
import me.akuz.ml.tensors.Tensor;

public final class TensorGen {
	
	public static void fillPallete(
			double lineValue, 
			final double[] outputData, 
			final int startIdx)
	{
		double r = 0.1;
		double g = 0.1;
		double b = 0.1;
		if (lineValue < 0.5) {
			r = 0.5 - lineValue + 0.1;
			g = lineValue + 0.1;
		} else {
			lineValue -= 0.5;
			g = 0.5 - lineValue + 0.1;
			b = lineValue + 0.1;
		}
		outputData[startIdx]   = r;
		outputData[startIdx+1] = g;
		outputData[startIdx+2] = b;
	}

	public static Tensor colourSineImage(int height, int width) {

		final Shape shape = new Shape(height, width, 3);
		final DenseTensor image = new DenseTensor(shape);
		
		// y and x: -1.0 to 1.0
		for (int i=0; i<height; i++) {
			final double y = (2*i - height)/(double)height;
			for (int j=0; j<width; j++) {
				final double x = (2*j - width)/(double)width;
				
				double ymul = y > 0 ? 1.0 : (1.0 - Math.pow(-y, 0.5));
				double radius = Math.pow(2*x*x + ymul*y*y, 0.95) * 10.0;
				double z = (1.01 + Math.cos(radius)) / 2.02;
				
				if (z <= 0.0 || z >= 1.0) {
					throw new IllegalStateException("Bad function: " + z);
				}

				double r = Math.pow(z, 1.5);
				double g = Math.pow(z, 0.5);
				double b = Math.pow(1.0 - z, 3.0);
				
//				final int startIdx = image.shape.calcFlatIndexFromLocation(new Location(i, j, 0));
//				fillPallete(z, image.data(), startIdx);
				
				image.set(new Location(i, j, 0), r);
				image.set(new Location(i, j, 1), g);
				image.set(new Location(i, j, 2), b);
			}
		}
		
		return image;
	}
}
