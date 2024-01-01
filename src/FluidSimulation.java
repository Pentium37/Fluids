import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class FluidSimulation extends JComponent implements Runnable {
	public static final int PPGS = 3;
	public static final double FPS = 60, TPS = 60;
	public BufferedImage bufferedImage;
	public JFrame frame;
	Thread simulationThread;
	MouseHandler mouseHandler;
	Fluid fluid;
	double[] dens_prev, u_prev, v_prev;
	int nColors = 600;
	int[] colorInt = new int[nColors];

	public FluidSimulation() {
		mouseHandler = new MouseHandler();

		for (int c = 0; c < nColors; c++) {
			double h = (2.0 / 3) * (1 - c * 1.0 / nColors);
			h += 0.03 * Math.sin(6 * Math.PI * h);
			colorInt[c] = Color.HSBtoRGB((float) h, (float) 1, (float) 1);
		}

		fluid = new Fluid(4, 4);

		int size = (Fluid.WIDTH + 2) * (Fluid.HEIGHT + 2);
		dens_prev = new double[size];
		u_prev = new double[size];
		v_prev = new double[size];

		bufferedImage =
				new BufferedImage(Fluid.WIDTH * PPGS, Fluid.HEIGHT * FluidSimulation.PPGS, BufferedImage.TYPE_INT_RGB);

		createFrame();
	}

	public void startSimulation() {
		startSimulationThread();
	}

	private void createFrame() {
		frame = new JFrame("Fluids Through a tunnel");

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(true);
		frame.getContentPane()
				.add(this);
		frame.pack();
		frame.setVisible(true);
		this.frame.addMouseMotionListener(mouseHandler);
		this.setFocusable(true);
	}

	@Override
	public void addNotify() {
		setPreferredSize(new Dimension(Fluid.WIDTH * PPGS, Fluid.HEIGHT * PPGS));
	}

	@Override
	public void paint(Graphics g) {
		g.drawImage(bufferedImage, 0, 0, null);
	}

	public void startSimulationThread() {
		simulationThread = new Thread(this);
		simulationThread.start();
	}

	@Override
	public void run() {
		double deltaTimeSeconds = 1.0 / TPS;
		double lastFrameTime = nanosToSeconds(System.nanoTime());
		double secondsToConsume = 0.0;

		while (simulationThread != null) {
			double currentFrameTime = nanosToSeconds(System.nanoTime());
			double lastFrameNeeded = currentFrameTime - lastFrameTime;
			lastFrameTime = currentFrameTime;

			secondsToConsume += lastFrameNeeded;
			while (secondsToConsume >= deltaTimeSeconds) {
				update(deltaTimeSeconds);
				secondsToConsume -= deltaTimeSeconds;
			}

			render();

			double currentFPS = 1.0 / lastFrameNeeded;
			if (currentFPS > FPS) {
				double targetSecondsPerFrame = 1.0 / FPS;
				double secondsToWaste = Math.abs(targetSecondsPerFrame - lastFrameNeeded) / 1000000;
				try {
					TimeUnit.SECONDS.sleep(secondsToMillis(secondsToWaste));
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	public void update(double deltaTime) {
		if (mouseHandler.mouseDragged) {
			addDensity(mouseHandler.x, mouseHandler.y, 10);
			double amountX = mouseHandler.x - mouseHandler.px;
			double amountY = mouseHandler.y - mouseHandler.py;
			addVelocity(mouseHandler.x, mouseHandler.y, amountX, amountY);
		}
		fluid.step(dens_prev, u_prev, v_prev, deltaTime);
	}

	public void addDensity(int i, int j, double amount) {
		dens_prev[Fluid.index(i, j)] += amount;
	}

	public void addVelocity(int i, int j, double amountX, double amountY) {
		u_prev[Fluid.index(i, j)] += amountX;
		v_prev[Fluid.index(i, j)] += amountY;
	}

	public void render() {
		int colorIndex;
		for (int i = 0; i < Fluid.WIDTH; i++) {
			for (int j = 0; j < Fluid.HEIGHT; j++) {
				double currentDensity = fluid.dens[Fluid.index(i, j)];
				//
				//				colorIndex = (int) (nColors * (currentDensity * 20 * 0.3));
				//				System.out.println(currentDensity);
				//				if (colorIndex < 0) {
				//					colorIndex = 0;
				//				}
				//				if (colorIndex >= nColors) {
				//					colorIndex = nColors - 1;
				//				}
				colorIndex = Color.HSBtoRGB((float) 0, (float) 0, (float) currentDensity);
				colorCell(i, j, colorIndex);
			}
		}

		frame.invalidate();
		frame.validate();
		frame.repaint();
	}

	private static double nanosToSeconds(long nanos) {
		return nanos / 1E9;
	}

	private static long secondsToMillis(double seconds) {
		return (long) (seconds * 1E3);
	}

	public void colorCell(int i, int j, int RGB) {
		// from (3i, 3j) -> (3i+2,3j+2)
		for (int x = 0; x < FluidSimulation.PPGS; x++) {
			for (int y = 0; y < FluidSimulation.PPGS; y++) {
				bufferedImage.setRGB(PPGS * i + x, PPGS * j + y, RGB);
			}
		}
	}
}


