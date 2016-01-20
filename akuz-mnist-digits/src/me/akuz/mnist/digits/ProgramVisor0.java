package me.akuz.mnist.digits;

import java.io.IOException;

import me.akuz.ml.tensors.Tensor;
import me.akuz.mnist.digits.visor.learning.FiniteColorSpace;
import me.akuz.mnist.digits.visor.transforms.SplitLastDim;
import me.akuz.mnist.digits.visor.transforms.YpCbCr;

public class ProgramVisor0 {
	
	private static final String PREFIX = "/Users/andrey/Desktop/test";
	
	private static void approximate(
			final Tensor image,
			final int colorCount,
			final int iterCount) throws IOException {
		
		// testing split
		final SplitLastDim split = new SplitLastDim(image.shape, 1);
		split.setInput(image);
		split.infer(false);
		split.dream();
		
		// transform colors layer
		final YpCbCr layer0 = 
				new YpCbCr(YpCbCr.Mode.NORMALIZE, image.shape);
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
		final Tensor dream = new Tensor(image.shape);
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
		final Tensor image = TensorFiles.loadImage_sRGB("/Users/andrey/Desktop/mount.png");
//		final Tensor image = TensorFiles.loadImage("/Users/andrey/Desktop/andrey.jpg");
		
		TensorFiles.saveImage_sRGB(image, PREFIX + "0.png");
		for (int colorCount=2; colorCount<=32; colorCount*=2) {
			approximate(image, colorCount, 50);
		}
	}

}

