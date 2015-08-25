package me.akuz.mnist.digits.cortex;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class TestPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private Brain _brain;
	
	public TestPanel() {
		super();
		this.setBackground(Color.ORANGE);
	}
	
	public void setBrain(final Brain brain) {
		_brain = brain;
	}

	@Override
	public void paintComponent(Graphics g) {
		
	    super.paintComponent(g);
	
	    Graphics2D g2 = (Graphics2D)g;
	    
	    final int gap = 10;
	    int xLayerStart = gap;
	    int yLayerStart = gap;
	    
		final Layer[] layers = _brain.getLayers();
		
		for (int l=0; l<layers.length; l++) {
			
			final Layer layer = layers[l];
			
			Column[][] columns = layer.getColumns();
			
			int yColumnCount = columns.length;
			int xColumnCount = columns[0].length;
			int columnSize = (int)Math.ceil(Math.sqrt(columns[0][0].getNeurons().length));
			
			for (int i=0; i<columns.length; i++) {
				for (int j=0; j<columns[i].length; j++) {

					int yColumnStart = yLayerStart + i * (columnSize*2 + 1);
					int xColumnStart = xLayerStart + j * (columnSize*2 + 1);

					final Column column = columns[i][j];
					
					final Neuron[] neurons = column.getNeurons();
					
					for (int n=0; n<neurons.length; n++) {
						
						int yShift = n / columnSize;
						int xShift = n % columnSize;
						
						final Neuron neuron = neurons[n];
						
						final int yPixel = yColumnStart + yShift*2;
						final int xPixel = xColumnStart + xShift*2;
						
						float color = (float)neuron.getCurrentPotential();
						g2.setColor(new Color(color, color, color));
						g2.drawLine(xPixel, yPixel, xPixel+1, yPixel);
						g2.drawLine(xPixel, yPixel, xPixel, yPixel+1);
						g2.drawLine(xPixel+1, yPixel, xPixel+1, yPixel+1);
						g2.drawLine(xPixel, yPixel+1, xPixel+1, yPixel+1);
					}
				}
			}
			
			xLayerStart += xColumnCount * (columnSize*2 + 2) + gap;
			
			if (l > 0 && l % 4 == 0) {
				
				yLayerStart += yColumnCount * (columnSize*2 + 2) + gap;
				xLayerStart = gap;
			}
		}
	
	}

}
