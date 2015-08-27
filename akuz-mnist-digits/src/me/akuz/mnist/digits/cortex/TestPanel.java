package me.akuz.mnist.digits.cortex;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.text.DecimalFormat;

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
	    
	    DecimalFormat fmt = new DecimalFormat("0.0000");
	    
	    Graphics2D g2 = (Graphics2D)g;
	    
	    final int px = 2;
	    final int gap = 12;
	    int xLayerStart = gap;
	    int yLayerStart = gap;
	    
	    g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, gap - 2));

	    final Layer[] layers = _brain.getLayers();
		
		for (int l=0; l<layers.length; l++) {
			
			final Layer layer = layers[l];
			
			Column[][] columns = layer.getColumns();
			
			int yColumnCount = columns.length;
			int xColumnCount = columns[0].length;
			int columnSize = (int)Math.ceil(Math.sqrt(columns[0][0].getNeurons().length));
			
			int avgCount = 0;
			double avgCurrent = 0.0;
			double avgHistorical = 0.0;
			
			int midCount = 0;
			double midCurrent = 0.0;
			double midHistorical = 0.0;
			
			for (int i=0; i<columns.length; i++) {
				for (int j=0; j<columns[i].length; j++) {

					int yColumnStart = yLayerStart + i * (columnSize*px + 1);
					int xColumnStart = xLayerStart + j * (columnSize*px + 1);

					final Column column = columns[i][j];
					
					final Neuron[] neurons = column.getNeurons();
					
					double maxCurrent = Double.NEGATIVE_INFINITY;
					double maxHistorical = Double.NEGATIVE_INFINITY;
					
					for (int n=0; n<neurons.length; n++) {
						
						int yShift = n / columnSize;
						int xShift = n % columnSize;
						
						final Neuron neuron = neurons[n];
						
						int yPixel = yColumnStart + yShift*px;
						final int xPixel = xColumnStart + xShift*px;
						
						float color = (float)neuron.getCurrentPotential();
						g2.setColor(new Color(color, color, color));
						g2.drawLine(xPixel, yPixel, xPixel+px-1, yPixel);
						g2.drawLine(xPixel, yPixel, xPixel, yPixel+px-1);
						g2.drawLine(xPixel+px-1, yPixel, xPixel+px-1, yPixel+px-1);
						g2.drawLine(xPixel, yPixel+px-1, xPixel+px-1, yPixel+px-1);
						
						yPixel += (yColumnCount * (columnSize*px + 1) + gap);
						
						color = (float)neuron.getHistoricalPotential();
						g2.setColor(new Color(color, color, color));
						g2.drawLine(xPixel, yPixel, xPixel+px-1, yPixel);
						g2.drawLine(xPixel, yPixel, xPixel, yPixel+px-1);
						g2.drawLine(xPixel+px-1, yPixel, xPixel+px-1, yPixel+px-1);
						g2.drawLine(xPixel, yPixel+px-1, xPixel+px-1, yPixel+px-1);
						
						
						// TODO max within column!
						if (maxCurrent < neuron.getCurrentPotential()) {
							maxCurrent = neuron.getCurrentPotential();
						}
						if (maxHistorical < neuron.getHistoricalPotential()) {
							maxHistorical = neuron.getHistoricalPotential();
						}
					}
					
					avgCount++;
					avgCurrent += maxCurrent;
					avgHistorical += maxHistorical;
					
					if (i >= columns.length / 3 && i < columns.length / 3 * 2 &&
						j >= columns[i].length / 3 && j < columns[i].length / 3 * 2) {
						
						midCount++;
						midCurrent += maxCurrent;
						midHistorical += maxHistorical;
					}
				}
			}
			
			avgCurrent /= avgCount;
			avgHistorical /= avgCount;

			midCurrent /= midCount;
			midHistorical /= midCount;
			
			String avgCurrentStr = fmt.format(avgCurrent);
			String avgHistoricalStr = fmt.format(avgHistorical);
			
			avgCurrentStr += ", " + fmt.format(midCurrent);
			avgHistoricalStr += ", " + fmt.format(midHistorical);
			
			g2.setColor(new Color(0.0f, 0.2f, 1.0f));
			g2.drawChars(avgCurrentStr.toCharArray(), 0, avgCurrentStr.length(), xLayerStart, yLayerStart - 1);
			g2.drawChars(avgHistoricalStr.toCharArray(), 0, avgHistoricalStr.length(), xLayerStart, yLayerStart - 1 + yColumnCount * (columnSize*px + 1) + gap);
			
			xLayerStart += xColumnCount * (columnSize*px + 1) + gap;
			
			if (l > 0 && l % 4 == 0) {
				
				yLayerStart += 2 * (yColumnCount * (columnSize*px + 1) + gap);
				xLayerStart = gap;
			}
		}
	
	}

}
