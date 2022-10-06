import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Arrays;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class SquareDisplay {
	private static FPS tracker1 = new FPS(), tracker2 = new FPS();
	public static final Random rng = new Random();
	static final int[] CONFIG = {
			5, // The equal dimensions of equal square (in this case, 10*10 for [width *
					// height])
			100, // The dimensions of the panel to be drawn, in which case is also a square
			5, // Y_Offset for each square
			5, // X_Offset for each square
			8, // Per Square offset from each other both X and Y, AKA the gap
			50, // Casted to long to represent the delay between each IMG redraw itself in milliseconds
			3, // DISTORT Factor
			5, // Round Arc for Width & Height
			1, // 1 == Use gray scale, else use color
			1000, // Represents the millis per second to draw
	};

	public static class DistortLayer extends LayerUI<Component> {
		private transient BufferedImageOp imageOp;

		public DistortLayer(int radi, float strength) {
			float[] matrix = new float[radi * radi];
			float f = strength / (radi * radi);
			for (int i = 0; i < (radi * radi); i++) {
				matrix[i] = f;
			}

			Map<RenderingHints.Key, Object> hintsPrefecture = new HashMap<>();
			hintsPrefecture.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

			RenderingHints hints = new RenderingHints(hintsPrefecture);

			imageOp = new ConvolveOp(new Kernel(radi, radi, matrix), ConvolveOp.EDGE_NO_OP, hints);
		}

		@Override
		public void paint(Graphics g, JComponent comp) {
			if (comp.getWidth() == 0 || comp.getHeight() == 0)
				return;

			BufferedImage img = new BufferedImage(comp.getWidth(), comp.getHeight(), (CONFIG[8] == 1 ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_INT_RGB));

			Graphics2D ig2 = img.createGraphics();
			ig2.setClip(g.getClip());
			super.paint(ig2, comp);
			ig2.dispose();
			
			Graphics2D g2 = (Graphics2D) g;
			g2.drawImage(img, imageOp, 0, 0);
			g2.dispose();
			g.dispose();
		}
	}
	
	public static class FastBlur {
		
	
	}

	public interface FPSPromise {
		void promiseUpdate();
	}

	/**
	 * This class is useful to represent FPS
	 * Calling FPS.interrupt() represents a new
	 * Frame has been drawn.
	 * 
	 * @author Jack Meng
	 */
	public static class FPS extends Thread {
		private long last;
		private double fps, min = 3000000000.0d, max = 0.0d;
		private List<FPSPromise> listeners = new ArrayList<>();

		public void addUpdatePromise(FPSPromise... promises) {
			for (FPSPromise r : promises) {
				listeners.add(r);
			}
		}

		private void notifyPromises() {
			listeners.forEach(FPSPromise::promiseUpdate);
		}

		@Override
		public void run() {
			while (true) {
				last = System.nanoTime();
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException e) {
					// IGNORED
				}
				fps = 1000000000.0 / (System.nanoTime() - last);
				if (fps > max)
					max = fps;
				if (fps < min)
					min = fps;
				notifyPromises();
				last = System.nanoTime();
			}
		}

		public double getFPS() {
			return fps;
		}

		public double getMin() {
			return min;
		}

		public double getMax() {
			return max;
		}

	}
	
	public static void out(Object ... args) {
		for(Object str : args) {
			System.out.print(str);
		}
 	}

	public static class RootPaneDefault extends JPanel {
		private int w_count, h_count, skipOffset;
		private transient BufferedImage buffer;
		private transient Color[] colorSeq;

		static final int[] SHADES_OF_GREY = {
				30,
				34,
				53,
				62,
				94,
				126,
				158,
				190,
				222,
				254,
		};

		public RootPaneDefault(int w_count, int h_count) {
			super();

			this.w_count = w_count;
			this.h_count = h_count;
			this.skipOffset = CONFIG[1] - (CONFIG[1] / w_count);
			this.buffer = new BufferedImage(CONFIG[1], CONFIG[1], BufferedImage.TYPE_INT_RGB);

			setPreferredSize(new Dimension(CONFIG[1], CONFIG[1]));
			setOpaque(true);
			setDoubleBuffered(false);
			setIgnoreRepaint(true);
			setBackground(Color.black);
				
			colorSeq = new Color[w_count * h_count];
			for(int i = 0; i < colorSeq.length; i++) {
				colorSeq[i] = rndShade();
			}
			
			tracker1.start();
			
			// Average time to print the image on screen
			Timer repaint = new Timer(CONFIG[9] / 1_000, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent x) {
					repaint();
				}
			});
			repaint.setRepeats(true);
			
			// Average time to update the buffered image
			Timer imgPainter = new Timer(CONFIG[5] / 1_000, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					__alive__();
					__schedule__();
				}
			});
			imgPainter.setRepeats(true);
			
			// ORDER HERE IS VERY IMPORTANT
			tracker2.start();
			imgPainter.start();
			repaint.start();

		}

		public RootPaneDefault() {
			this(6, 20);
		}

		private Color rndShade() {
			int curr = SHADES_OF_GREY[new Random().nextInt(SHADES_OF_GREY.length)];
			return new Color(curr, curr, curr);
		}
		
		private synchronized void __alive__() {
			for(int i = 0; i < colorSeq.length; i++) {
				if(colorSeq[i].getRed() + colorSeq[i].getBlue() + colorSeq[i].getGreen() >= SHADES_OF_GREY[6] * 3) {
					colorSeq[i].darker();
				} else {
					colorSeq[i].brighter();
				}
			}
		}

		private synchronized void __schedule__() {
			BufferedImage tempBuffer = new BufferedImage(buffer.getWidth(), buffer.getWidth(), buffer.getType());
			Graphics2D g2 = tempBuffer.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			for (int y = CONFIG[2], i = 0; y < this.getPreferredSize().height - CONFIG[2] && i < colorSeq.length; y += CONFIG[0] + CONFIG[4]) {
				for (int x = CONFIG[3]; x < this.getPreferredSize().width - CONFIG[3]; x += CONFIG[0] + CONFIG[4], i++) {
					g2.setColor(colorSeq[i]);
					g2.fillRoundRect(x, y, CONFIG[0], CONFIG[0], CONFIG[7], CONFIG[7]);
				}
			}
			tracker1.interrupt();
			g2.dispose();
			this.buffer = tempBuffer;
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.drawImage(buffer, null, (this.getWidth() - buffer.getWidth()) / 2, (this.getHeight() - buffer.getWidth()) / 2);
			tracker2.interrupt();
			g2.dispose();
		}
	}

	public static class DeepFrame extends JFrame implements Runnable {
		public DeepFrame(JComponent rootContent) {
			super();
			setTitle("test-squares_ ~ exoad");
			setPreferredSize(new Dimension(rootContent.getPreferredSize().width + CONFIG[3] + 300, rootContent.getPreferredSize().height + CONFIG[2] + 400));
			try {
				setIconImage(ImageIO.read(new File("../uwu.png")));
			} catch (IOException e) {
				e.printStackTrace();
			}
			JLayer<Component> distort = new JLayer<>(rootContent, new DistortLayer(CONFIG[6], 1.0F));
			distort.setPreferredSize(rootContent.getPreferredSize());

			JPanel util = new JPanel() {
				@Override
				public void paintComponent(Graphics gr) {
					super.paintComponent(gr);
					Graphics2D g = (Graphics2D) gr;
					g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					g.setColor(Color.CYAN);
					g.setFont(new Font("Monospaced", Font.PLAIN, 13));
					g.drawString("BUFFER  ->            " + tracker1.getFPS(), 20, 24);
					g.setColor(Color.PINK);
					g.drawString("FPS     ->            " + tracker2.getFPS(), 20, 34);
					g.setColor(Color.YELLOW);
					g.drawString("MIN     ->            " + tracker2.getMin(), 20, 44);
					g.setColor(Color.GREEN);
					g.drawString("MAX     ->            " + tracker2.getMax(), 20, 54);
					g.setColor(Color.RED);
					g.drawString("LOSS    ->            " + Math.abs((((tracker1.max - tracker1.min) / tracker1.getFPS()) - ((tracker2.max - tracker2.min) / tracker2.getFPS()))), 20, 64);
					g.setColor(Color.WHITE);
					g.drawString("MEM     ->            " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024) + "/" + (Runtime.getRuntime().totalMemory() / 1024 / 1024), 20, 74);
					g.dispose();
				}
			};

			tracker1.addUpdatePromise(new FPSPromise() {
				@Override
				public void promiseUpdate() {
					SwingUtilities.invokeLater(util::repaint);
				}
			});

			tracker2.addUpdatePromise(new FPSPromise() {
				@Override
				public void promiseUpdate() {
					SwingUtilities.invokeLater(util::repaint);
				}
			});

			JButton callGC = new JButton("Print_Trace");
			callGC.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Runtime.getRuntime().gc();
				}
			});
			callGC.setRolloverEnabled(false);
			callGC.setBackground(Color.YELLOW);
			callGC.setForeground(Color.BLACK);
			
			util.setPreferredSize(new Dimension(100, 100));
			util.setOpaque(true);
			util.setBackground(Color.BLACK);

			getContentPane().setBackground(Color.BLACK);
			getContentPane().setLayout(new BorderLayout());
			getContentPane().add(callGC, BorderLayout.NORTH);
			getContentPane().add(distort, BorderLayout.CENTER);
			getContentPane().add(util, BorderLayout.SOUTH);
		}

		@Override
		public void run() {
			pack();
			setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			setVisible(true);
		}
		
		@Override
		public void paint(Graphics g) {
			super.paint(g);
			tracker2.interrupt();
		}
	}

	private static void invoke() {
		System.setProperty("sun.java2d.opengl", "true");
	}

	public static void main(String... args) throws Exception {
		invoke();

		System.out.println(System.getProperties());

		RootPaneDefault rpd = new RootPaneDefault();
		DeepFrame df = new DeepFrame(rpd);
		
		SwingUtilities.invokeLater(df::run);
	}
}
