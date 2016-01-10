package me.akuz.tensors.test;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import me.akuz.tensors.DenseTensor;
import me.akuz.tensors.Location;
import me.akuz.tensors.Shape;
import me.akuz.tensors.Tensor;
import me.akuz.tensors.ViewTensor;

public class TensorsTest {

	@Test
	public void testDenseTensorSums() {
		
		Random rnd = new Random();
		
		DenseTensor dense = new DenseTensor(new Shape(2, 3, 4));
		
		Assert.assertEquals(dense.ndim, 3);
		Assert.assertEquals(dense.shape.ndim, 3);
		Assert.assertEquals(dense.size, 24);
		Assert.assertEquals(dense.shape.size, 24);
		
		double sum1 = 0.0;
		for (int k=0; k<200; k++) {
			
			int[] indices = new int[dense.ndim];
			for (int i=0; i<dense.ndim; i++) {
				indices[i] = rnd.nextInt(dense.shape.sizes[i]);
			}
			Location loc = new Location(indices);
			sum1 -= dense.get(loc);
			double value = rnd.nextDouble();
			dense.set(loc, value);
			sum1 += value;
		}
		
		double sum2 = 0.0;
		double[] data = dense.data();
		for (int i=0; i<data.length; i++) {
			sum2 += data[i];
		}
		
		Assert.assertEquals(sum1, sum2, 1e-10);
		
		try {
			dense.get(-1);
			Assert.fail();
		} catch (IndexOutOfBoundsException e) {
			// expected
		}
		
		try {
			dense.get(dense.size);
			Assert.fail();
		} catch (IndexOutOfBoundsException e) {
			// expected
		}
		
		try {
			dense.get(new Location(-1, 0, 0));
			Assert.fail();
		} catch (IndexOutOfBoundsException e) {
			// expected
		}
		
		try {
			dense.get(new Location(1, 3, 2));
			Assert.fail();
		} catch (IndexOutOfBoundsException e) {
			// expected
		}
	}
	
	@Test
	public void testViewTensorSums() {
		
		Random rnd = new Random();

		DenseTensor dense = new DenseTensor(new Shape(8, 8, 2));

		Tensor view = new ViewTensor(dense, new Location(2, 2, 0), new Shape(2, 3, 2));
		
		double sum1 = 0.0;
		for (int k=0; k<50; k++) {
			
			int[] indices = new int[view.ndim];
			for (int i=0; i<view.ndim; i++) {
				indices[i] = rnd.nextInt(view.shape.sizes[i]);
			}
			Location loc = new Location(indices);
			sum1 -= view.get(loc);
			double value = rnd.nextDouble();
			view.set(loc, value);
			sum1 += value;
		}
		
		double sum2 = 0.0;
		double[] data = dense.data();
		for (int i=0; i<data.length; i++) {
			sum2 += data[i];
		}
		
		Assert.assertEquals(sum1, sum2, 1e-10);
		
		try {
			new ViewTensor(dense, new Location(-1, 2, 0), view.shape);
			Assert.fail();
		} catch (IndexOutOfBoundsException e) {
			// expected
		}
		
		try {
			new ViewTensor(dense, new Location(-1, 2, 1), view.shape);
			Assert.fail();
		} catch (IndexOutOfBoundsException e) {
			// expected
		}
	}
	
	@Test
	public void testViewTensor2D() {
		
		Tensor dense = new DenseTensor(new Shape(5, 8));
		Tensor view = new ViewTensor(dense, new Location(1, 2), new Shape(2, 3));
		
		for (int i=0; i<view.size; i++) {
			view.set(i, 1.0);
		}
		
		for (int i=0; i<5; i++) {
			for (int j=0; j<8; j++) {
				
				double expected;
				if (i >= 1 && i <= 2 && j >= 2 && j <= 4) {
					expected = 1.0;
				} else {
					expected = 0.0;
				}
				
				Assert.assertEquals(expected, dense.get(new Location(i, j)), 0.0);
			}
		}
	}
	
	@Test
	public void testViewTensor3D() {
		
		Tensor dense = new DenseTensor(new Shape(5, 8, 4));
		Tensor view = new ViewTensor(dense, new Location(1, 2, 2), new Shape(2, 3, 2));
		
		for (int i=0; i<view.size; i++) {
			view.set(i, 1.0);
		}
		
		for (int i=0; i<5; i++) {
			for (int j=0; j<8; j++) {
				for (int k=0; k<4; k++) {
					
					double expected;
					if (i >= 1 && i <= 2 && 
						j >= 2 && j <= 4 && 
						k >= 2 && k <= 3) {
						expected = 1.0;
					} else {
						expected = 0.0;
					}
					
					Assert.assertEquals(expected, dense.get(new Location(i, j, k)), 0.0);
				}
			}
		}
	}
}
