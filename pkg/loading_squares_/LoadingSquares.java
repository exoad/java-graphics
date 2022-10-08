import javax.swing.*;
import java.awt.*;
import javax.imageio.*;
import java.awt.image.*;
import java.io.*;
import java.util.List;
import java.awt.geom.*;
import java.util.ArrayList;
import java.awt.event.*;
import java.math.*;

public class LoadingSquares {
    public static final FPS bufferTimer = new FPS(), paintTimer = new FPS();
    public static BigInteger framesCount = new BigInteger("0");
    static {
        bufferTimer.start();
        paintTimer.start();
    }
    public static final int [] CONFIG = {
        400, // DEFAULT ROOT_CONTENT WIDTH
        170, // DEFAULT ROOT_CONTENT HEIGHT
        300, // DEFAULT ANALYTICS_CONTENT WIDTH
        250, // DEFAULT ANALYTICS_CONTENT HEIGHT
        15, // MILLIS FOR DELAY OF EACH ROOT CONTENT PAINT TO PANE (translates not directly to FPS)
        30, // MILLIS FOR DELAY OF EACH ROOT CONTNET BUFFER TO PAINT (translates not directly to FPS)
        15, // WIDTH & HEIGHT OF INNER SQUARE (FILLED_SQUARE)
        20, // WIDTH & HEIGHT OF OUTER SQUARE (OUTLINED_SQUARE)
        80, // SPEED PER DEGREE OF INNER ROTATION SQUARE
    };
    
    public static final Color [] COLORS = {
        new Color(81, 216, 219), // PRIMARY
        new Color(48, 54, 86), // SECONDARY
    };
    
    public static class RootContent extends JPanel {
        private transient BufferedImage buffImg;
        private float lastRotation = 0.1111F, d1 = (float) Math.PI, d2 = (float) Math.PI;
        
        public RootContent() {
            setPreferredSize(new Dimension(CONFIG[0], CONFIG[1]));
            setOpaque(true);
            setBackground(Color.WHITE);

            buffImg = new BufferedImage(CONFIG[0], CONFIG[1], BufferedImage.TYPE_INT_RGB);
            
            Timer bufferWorker = new Timer(CONFIG[5], new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    alive();
                }
            });
            bufferWorker.setRepeats(true);
            
            Timer paintWorker = new Timer(CONFIG[4], new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    repaint();
                }
            });
            paintWorker.setRepeats(true);
            paintWorker.start();
            bufferWorker.start();
        }
        
        private synchronized void alive() {
            BufferedImage temp = new BufferedImage(buffImg.getWidth(), buffImg.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = temp.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            AffineTransform a = new AffineTransform();
            AffineTransform a2 = new AffineTransform();
            
            Rectangle filledRect = new Rectangle(0, getHeight() / 2, CONFIG[6], CONFIG[6]);
            a.rotate(lastRotation, filledRect.getX() + filledRect.getWidth() / 2, filledRect.getY() + filledRect.getHeight() / 2);
            
            lastRotation = lastRotation % (Math.PI * 2) == 0 ? 0.11111F : lastRotation + d1 / CONFIG[8];
            System.out.println(lastRotation);
            
            g.setColor(COLORS[0]);
            g.fill(a.createTransformedShape(filledRect));
            
            g.dispose();
            
            buffImg = temp;
            temp = null;
            bufferTimer.interrupt();
        }
        
        @Override
        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                
            g2.drawImage(buffImg, null, (getWidth() - buffImg.getWidth()) / 2, (getHeight() - buffImg.getHeight()) / 2);
            
            paintTimer.interrupt();
            framesCount = framesCount.add(BigInteger.ONE);
            g2.dispose();
        }
    }
    
    
    public static class AnalyticsContent extends JPanel {
        public AnalyticsContent() {
            setPreferredSize(new Dimension(CONFIG[2] + 50, CONFIG[3]));
            setOpaque(true);
            setDoubleBuffered(false);
            setBackground(Color.BLACK);
            
            setLayout(new BorderLayout());
            
            JButton killAll = new JButton("kill_all_refs");
            killAll.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Runtime.getRuntime().gc();
                    framesCount = new BigInteger("0");
                }
            });
            killAll.setRolloverIcon(null);
            killAll.setBackground(Color.YELLOW);
            
            JPanel enWrap = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics gr) {
                        super.paintComponent(gr);
                        Graphics2D g = (Graphics2D) gr;
                        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g.setColor(Color.CYAN);
                        g.setFont(new Font("Monospaced", Font.BOLD, 13));
                        g.drawString("BUFFER  ->            " + (int) bufferTimer.getFPS(), 20, 29);
                        g.setColor(Color.PINK);
                        g.drawString("FPS     ->            " + (int) paintTimer.getFPS(), 20, 49);
                        g.setColor(Color.YELLOW);
                        g.drawString("MIN     ->            " + (int) paintTimer.getMin(), 20, 69);
                        g.setColor(Color.GREEN);
                        g.drawString("MAX     ->            " + (int) paintTimer.getMax(), 20, 89);
                        g.setColor(Color.RED);
                        g.drawString("LOSS    ->            " + (int) Math.abs((((bufferTimer.max - bufferTimer.min) / bufferTimer.getFPS()) - ((paintTimer.max - paintTimer.min) / paintTimer.getFPS()))), 20, 109);
                        g.setColor(Color.WHITE);
                        g.drawString("MEM     ->            " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024) + "/" + (Runtime.getRuntime().totalMemory() / 1024 / 1024),                20, 129);
                        g.setColor(Color.ORANGE);
                        g.drawString("FR      ->            " + framesCount, 20, 149);
                        g.dispose();
                }
            };
            enWrap.setOpaque(true);
            enWrap.setPreferredSize(new Dimension(CONFIG[0], CONFIG[1]));
            enWrap.setBackground(Color.BLACK);
            
            add(killAll, BorderLayout.SOUTH);
            add(enWrap, BorderLayout.NORTH);
        }
        

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
	
    public interface FPSPromise {
		void promiseUpdate();
	}
	
    public static class DeepFrame extends JFrame implements Runnable {
        public DeepFrame(JComponent rootContent) {
            super();
            setPreferredSize(new Dimension(rootContent.getPreferredSize().width, rootContent.getPreferredSize().height));
            try {
				setIconImage(ImageIO.read(new File("../uwu.png")));
			} catch (IOException e) {
				e.printStackTrace();
			}
			setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			getContentPane().add(rootContent);
        }
        
        @Override
        public void run() {
            pack();
            setLocationRelativeTo(null);
            setVisible(true);
        }
        
        @Override
        public void paint(Graphics g) {
            super.paint(g);
        }
    }

    public static void main(String ... args) throws Exception {
        System.setProperty("sun.java2d.opengl", "true");
        System.out.println(System.getProperties());
        
        DeepFrame f = new DeepFrame(new RootContent());
        f.setTitle("master_display ~ exoad");
        
        AnalyticsContent ac = new AnalyticsContent();
        paintTimer.addUpdatePromise(new FPSPromise() {
            @Override
            public void promiseUpdate() {
                SwingUtilities.invokeLater(ac::repaint);
            }
        });
        bufferTimer.addUpdatePromise(new FPSPromise() {
            @Override
            public void promiseUpdate() {
                SwingUtilities.invokeLater(ac::repaint);
            }
        });
        DeepFrame f2 = new DeepFrame(ac);
        f2.setTitle("analytics_display ~ exoad");
        
        SwingUtilities.invokeLater(f::run);
        SwingUtilities.invokeLater(f2::run);
    }
}