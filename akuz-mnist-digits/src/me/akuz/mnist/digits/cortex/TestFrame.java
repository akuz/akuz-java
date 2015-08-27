package me.akuz.mnist.digits.cortex;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.border.EmptyBorder;

public class TestFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private TestPanel _panel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				TestFrame frame = null;
				try {
					frame = new TestFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (frame != null) {
					
					TestPanel panel = frame.getPanel();
					//Thread thread = new Thread(new TestLoopNumbers(panel));
					Thread thread = new Thread(new TestLoopRandom(panel));
					thread.start();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public TestFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		_panel = new TestPanel();
		_panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		_panel.setLayout(new BorderLayout(0, 0));
		setContentPane(_panel);
	}

	public TestPanel getPanel() {
		return _panel;
	}
}
