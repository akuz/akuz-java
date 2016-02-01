package me.akuz.mnist.digits.visor.transform;

import me.akuz.ml.tensors.Tensor;
import me.akuz.ml.tensors.Location;
import me.akuz.ml.tensors.Shape;
import me.akuz.mnist.digits.visor.VisorUtils;
import me.akuz.mnist.digits.visor.VisorLayer;

/**
 * Color converter layer with YpCbCr space
 * at the top and sRGB at the bottom, where
 * Yp stands for Y' (gamma adjusted).
 *
 */
public class YpCbCr extends VisorLayer {
	
	public enum Mode {
		STANDARD,
		NORMALIZE,
	}

	public final Mode mode;
	public final Shape outputShape;
	public final Tensor output;
	
	public YpCbCr(final Mode mode, final Shape inputShape) {
		super(inputShape);
		
		if (inputShape == null) {
			throw new NullPointerException("inputShape");
		}
		if (inputShape.ndim != 3) {
			throw new IllegalArgumentException("inputShape.ndim must be 3, got " + inputShape.ndim);
		}
		if (inputShape.sizes[2] != 3) {
			throw new IllegalArgumentException("inputShape.sizes[2] must be 3, got " + inputShape.sizes[2]);
		}
		this.mode = mode;
		this.outputShape = inputShape;
		this.output = new Tensor(inputShape);
	}

	@Override
	public void infer(boolean useOutputAsPrior) {
		
		final Tensor input = getInputNotNull();
		
		final int[] sizes = this.outputShape.sizes;
		final double[] outputData = this.output.data();
		final double[] inputData = input.data();

		final int[] indices = new int[3];
		final Location loc = new Location(indices);
		for (int i=0; i<sizes[0]; i++) {
			indices[0] = i;
			for (int j=0; j<sizes[1]; j++) {
				indices[1] = j;
				
				int idx = this.outputShape.calcFlatIndexFromLocation(loc);
				
				final double R = VisorUtils.clip01(inputData[idx+0]);
				final double G = VisorUtils.clip01(inputData[idx+1]);
				final double B = VisorUtils.clip01(inputData[idx+2]);

				final double adj = this.mode == Mode.NORMALIZE ? 0.5 : 0.0;

				final double Yp = VisorUtils.clip01(  0.299*R    + 0.587*G    + 0.114*B    );
				final double Cb = VisorUtils.clip55( -0.168736*R - 0.331264*G + 0.5*B      ) + adj;
				final double Cr = VisorUtils.clip55(  0.5*R      - 0.418688*G - 0.081312*B ) + adj;
				
				outputData[idx+0] = Yp;
				outputData[idx+1] = Cb;
				outputData[idx+2] = Cr;
			}
		}
	}

	@Override
	public void learn() {
		//nothing to do
	}

	@Override
	public void dream() {

		final Tensor input = getInputNotNull();
		
		final int[] sizes = this.outputShape.sizes;
		final double[] outputData = this.output.data();
		final double[] inputData = input.data();

		final int[] indices = new int[3];
		final Location loc = new Location(indices);
		for (int i=0; i<sizes[0]; i++) {
			indices[0] = i;
			for (int j=0; j<sizes[1]; j++) {
				indices[1] = j;
				
				int idx = this.outputShape.calcFlatIndexFromLocation(loc);
				
				final double adj = this.mode == Mode.NORMALIZE ? 0.5 : 0.0;
				
				final double Yp = VisorUtils.clip01(outputData[idx+0]);
				final double Cb = VisorUtils.clip55(outputData[idx+1] - adj);
				final double Cr = VisorUtils.clip55(outputData[idx+2] - adj);
				
				final double r = Yp               + 1.4019996*Cr;
				final double g = Yp - 0.344136*Cb - 0.714136*Cr;
				final double b = Yp + 1.772000*Cb;

				inputData[idx+0] = VisorUtils.clip01(r);
				inputData[idx+1] = VisorUtils.clip01(g);
				inputData[idx+2] = VisorUtils.clip01(b);
			}
		}
	}

}
