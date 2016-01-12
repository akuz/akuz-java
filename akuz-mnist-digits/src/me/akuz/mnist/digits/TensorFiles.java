package me.akuz.mnist.digits;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import me.akuz.ml.tensors.Location;
import me.akuz.ml.tensors.Shape;
import me.akuz.ml.tensors.Tensor;

public final class TensorFiles {

	public static void saveColourPNG(
			final Tensor image,
			final String fileName) throws IOException {

		final Shape shape = image.shape;
		
		BufferedImage img = new BufferedImage(
				shape.sizes[1], 
				shape.sizes[0], 
				BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D graphics = img.createGraphics();
		
		for (int i=0; i<shape.sizes[0]; i++) {
			for (int j=0; j<shape.sizes[1]; j++) {
				
				int r = (int)(image.get(new Location(i, j, 0)) * 255);
				int g = (int)(image.get(new Location(i, j, 1)) * 255);
				int b = (int)(image.get(new Location(i, j, 2)) * 255);
				
				final Color color = new Color(r, g, b);
				graphics.setColor(color);
				graphics.fillRect(j, i, 1, 1);
			}
		}
		
		File imgFile = new File(fileName);
	    ImageIO.write(img, "png", imgFile);
	}
}
