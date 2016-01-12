package me.akuz.mnist.digits;

import java.io.IOException;

import me.akuz.ml.tensors.DenseTensor;
import me.akuz.ml.tensors.Tensor;
import me.akuz.mnist.digits.visor.VisorLayerC;

public class ProgramVisor0 {
	
	private static final String PREFIX = "/Users/andrey/Desktop/test";
	
	private static void approximate(
			final Tensor image,
			final int colorCount) throws IOException {

		final VisorLayerC layer = new VisorLayerC(image.shape, colorCount);
		
		layer.setInput(image);
		
		for (int i=0; i<colorCount*10; i++) {
			System.out.println(i);
			layer.infer();
			layer.learn();
		}
		
		System.out.println("dream");
		final Tensor dream = new DenseTensor(image.shape);
		layer.setInput(dream);
		layer.dream();
		
		TensorFiles.saveColourPNG(dream, PREFIX + colorCount + ".png");
	}
	
	public static void main(String[] args) throws IOException {
		
		final Tensor image = TensorGens.colourSineImage(150, 200);
		TensorFiles.saveColourPNG(image, PREFIX + "0.png");
		approximate(image, 6);
		approximate(image, 5);
		approximate(image, 4);
		approximate(image, 3);
		approximate(image, 2);
	}

}

