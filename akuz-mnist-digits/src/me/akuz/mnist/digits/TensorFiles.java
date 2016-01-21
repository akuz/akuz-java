package me.akuz.mnist.digits;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import me.akuz.ml.tensors.Tensor;
import me.akuz.ml.tensors.Location;
import me.akuz.ml.tensors.Shape;
import me.akuz.mnist.digits.visor.VisorUtils;

public final class TensorFiles {
	
	public static Tensor loadImage_sRGB(final String fileName) throws IOException {
		
		final BufferedImage img = ImageIO.read(new File(fileName));
		
		final Shape shape = new Shape(img.getHeight(), img.getWidth(), 3);
		final Tensor tensor = new Tensor(shape);
		final double[] data = tensor.data();
		
		final int[] indices = new int[3];
		final Location loc = new Location(indices);
		for (int i=0; i<img.getHeight(); i++) {
			indices[0] = i;
			for (int j=0; j<img.getWidth(); j++) {
				indices[1] = j;
				
				final Color color = new Color(img.getRGB(j, i));
				final int idx = shape.calcFlatIndexFromLocation(loc);
				
				data[idx+0] = VisorUtils.clip01(color.getRed() / 255.0);
				data[idx+1] = VisorUtils.clip01(color.getGreen() / 255.0);
				data[idx+2] = VisorUtils.clip01(color.getBlue() / 255.0);
			}
		}
		
		return tensor;
	}

	public static void saveImage_sRGB(
			final Tensor tensor,
			final String fileName) throws IOException {

		final Shape shape = tensor.shape;
		if (shape.ndim != 3) {
			throw new IllegalArgumentException("Tensor ndim must be 3");
		}
		if (shape.sizes[2] != 3) {
			throw new IllegalArgumentException("Tensor dim 2 size must be 3");
		}
		final double[] data = tensor.data();
		
		final BufferedImage img = new BufferedImage(
				shape.sizes[1],
				shape.sizes[0],
				BufferedImage.TYPE_INT_ARGB);
		
		final Graphics2D graphics = img.createGraphics();
		
		final int[] indices = new int[3];
		final Location loc = new Location(indices);
		for (int i=0; i<shape.sizes[0]; i++) {
			indices[0] = i;
			for (int j=0; j<shape.sizes[1]; j++) {
				indices[1] = j;

				final int idx = shape.calcFlatIndexFromLocation(loc);
				
				final int R = (int)(255.0 * VisorUtils.clip01(data[idx+0]));
				final int G = (int)(255.0 * VisorUtils.clip01(data[idx+1]));
				final int B = (int)(255.0 * VisorUtils.clip01(data[idx+2]));

				final Color color = new Color(R, G, B);
				graphics.setColor(color);
				graphics.fillRect(j, i, 1, 1);
			}
		}
		
		final File imgFile = new File(fileName);
	    ImageIO.write(img, "png", imgFile);
	}
}
