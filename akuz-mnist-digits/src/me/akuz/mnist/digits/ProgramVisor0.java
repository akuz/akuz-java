package me.akuz.mnist.digits;

import java.io.IOException;

import me.akuz.ml.tensors.DenseTensor;
import me.akuz.mnist.digits.visor.layers.FiniteColorSpace;
import me.akuz.mnist.digits.visor.layers.YpCbCr_sRGB;

public class ProgramVisor0 {
	
	private static final String PREFIX = "/Users/andrey/Desktop/test";
	
	private static void approximate(
			final DenseTensor image,
			final int colorCount,
			final int iterCount) throws IOException {
		
		// transform colors layer
		final YpCbCr_sRGB layer0 = 
				new YpCbCr_sRGB(YpCbCr_sRGB.Mode.NORMALIZE, image.shape);
		layer0.setInput(image);
		layer0.infer(false);

		// infer finite colors layer
		final FiniteColorSpace layer1 = 
				new FiniteColorSpace(image.shape, colorCount);
		layer1.setInput(layer0.output);
		
		// perform learning
		for (int i=0; i<iterCount; i++) {
			System.out.println(i);
			layer1.infer(false);
			layer1.learn();
		}
		
		System.out.println("dream");
		final DenseTensor dream = new DenseTensor(image.shape);
		layer0.setInput(dream);
		layer1.dream();
		layer0.dream();
		
		TensorFiles.saveImage_sRGB(dream, PREFIX + colorCount + ".png");
		
		System.out.println();
		System.out.println("DONE " + colorCount + " colors.");
		System.out.println();
	}
	
	public static void main(String[] args) throws IOException {
		
//		final Tensor image = TensorGen.colourSineImage(150, 200);
//		final DenseTensor image = TensorFiles.loadImage_sRGB("/Users/andrey/Desktop/baz.jpg");
		final DenseTensor image = TensorFiles.loadImage_sRGB("/Users/andrey/Desktop/mount.png");
//		final Tensor image = TensorFiles.loadImage("/Users/andrey/Desktop/andrey.jpg");
		
		TensorFiles.saveImage_sRGB(image, PREFIX + "0.png");
		for (int colorCount=2; colorCount<=32; colorCount*=2) {
			approximate(image, colorCount, 50);
		}
	}

}

