package me.akuz.mnist.digits;

import java.io.IOException;

import me.akuz.ml.tensors.Tensor;

public class ProgramVisor0 {
	
	public static void main(String[] args) throws IOException {
		
		final Tensor image = TensorGenerate.colourSineImage(150, 200);
		
		// TODO
		
		TensorSave.saveColourPNG(image, "/Users/andrey/Desktop/test.png");
	}

}

