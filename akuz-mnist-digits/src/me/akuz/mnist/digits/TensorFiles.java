package me.akuz.mnist.digits;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import me.akuz.ml.tensors.DenseTensor;
import me.akuz.ml.tensors.Location;
import me.akuz.ml.tensors.Shape;
import me.akuz.ml.tensors.Tensor;

public final class TensorFiles {
	
	public static Tensor loadImage(final String fileName) throws IOException {
		
		final BufferedImage img = ImageIO.read(new File(fileName));
		
		final Shape shape = new Shape(img.getHeight(), img.getWidth(), 3);
		final Tensor tensor = new DenseTensor(shape);
		
		final int[] indices = new int[3];
		final Location loc = new Location(indices);
		for (int i=0; i<img.getHeight(); i++) {
			indices[0] = i;
			for (int j=0; j<img.getWidth(); j++) {
				indices[1] = j;
				
				final Color color = new Color(img.getRGB(j, i));
				final double r = Math.pow((1 + color.getRed()) / 257.0, 2.2);
				final double g = Math.pow((1 + color.getGreen()) / 257.0, 2.2);
				final double b = Math.pow((1 + color.getBlue()) / 257.0, 2.2);
				
				indices[2] = 0;
				tensor.set(loc, r);
				
				indices[2] = 1;
				tensor.set(loc, g);
				
				indices[2] = 2;
				tensor.set(loc, b);
			}
		}
		
		return tensor;
	}

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
				
				int r = (int)(Math.pow(image.get(new Location(i, j, 0)), 1/2.2) * 255);
				int g = (int)(Math.pow(image.get(new Location(i, j, 1)), 1/2.2) * 255);
				int b = (int)(Math.pow(image.get(new Location(i, j, 2)), 1/2.2) * 255);
				
				final Color color = new Color(r, g, b);
				graphics.setColor(color);
				graphics.fillRect(j, i, 1, 1);
			}
		}
		
		File imgFile = new File(fileName);
	    ImageIO.write(img, "png", imgFile);
	}
}
