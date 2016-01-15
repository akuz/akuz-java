package me.akuz.mnist.digits;

import java.io.IOException;

import me.akuz.ml.tensors.DenseTensor;
import me.akuz.ml.tensors.Tensor;
import me.akuz.mnist.digits.visor.VisorLayerC;

public class ProgramVisor0 {
	
	private static final String PREFIX = "/Users/andrey/Desktop/test";
	
	private static void approximate(
			final Tensor image,
			final int colorCount,
			final int iterCount) throws IOException {

		final VisorLayerC layer = new VisorLayerC(image.shape, colorCount);
		
		layer.setInput(image);
		
		for (int i=0; i<iterCount; i++) {
			System.out.println(i);
			layer.infer(false);
			layer.learn();
		}
		
		System.out.println("dream");
		final Tensor dream = new DenseTensor(image.shape);
		layer.setInput(dream);
		layer.dream();
		
		TensorFiles.saveColourPNG(dream, PREFIX + colorCount + ".png");
		
		layer.print();
	}
	
	public static void main(String[] args) throws IOException {
		
		final Tensor image = TensorGens.colourSineImage(150, 200);
		TensorFiles.saveColourPNG(image, PREFIX + "0.png");
		for (int colorCount=2; colorCount<=8; colorCount*=2) {
			approximate(image, colorCount, colorCount*10);
		}
//		approximate(image, 32, 50);
	}

}

