import java.util.concurrent.TimeUnit;

public class FluidSimulation implements Runnable, DataProvider {
	public static final int CELL_LENGTH = 5;
	public static final double FPS = 60, TPS = 60;
	Thread simulationThread;
	Fluid fluid;
	ApplicationWindow window;

	public final int IMAGE_WIDTH = Fluid.WIDTH * CELL_LENGTH;
	public final int IMAGE_HEIGHT = Fluid.HEIGHT * CELL_LENGTH;

	public double[] u0, v0, d0;
	private final MouseAdapter mouseAdapter;

	public FluidSimulation() {
		fluid = new Fluid(2,2);
		mouseAdapter = new MouseAdapter();
		window = new ApplicationWindow(IMAGE_WIDTH, IMAGE_HEIGHT);
		window.addMouseListener(mouseAdapter);
		window.addMouseMotionListener(mouseAdapter);

		int size = (Fluid.WIDTH + 2) * (Fluid.HEIGHT + 2);
		u0 = new double[size];
		v0 = new double[size];
		d0 = new double[size];
	}

	public void startSimulation() {
		startSimulationThread();
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
		addSourcesFromUI();
		fluid.step(deltaTime, u0, v0, d0);
	}

	public void addSourcesFromUI() {
		mouseAdapter.consumeSources(fluidInput -> {
			fluid.u[Fluid.index(fluidInput.x, fluidInput.y)] += fluidInput.forceX;
			fluid.v[Fluid.index(fluidInput.x, fluidInput.y)] += fluidInput.forceY;
			fluid.u[Fluid.index(fluidInput.x + 1, fluidInput.y)] += fluidInput.forceX;
			fluid.v[Fluid.index(fluidInput.x + 1, fluidInput.y)] += fluidInput.forceY;
			fluid.u[Fluid.index(fluidInput.x - 1, fluidInput.y)] += fluidInput.forceX;
			fluid.v[Fluid.index(fluidInput.x - 1, fluidInput.y)] += fluidInput.forceY;
			fluid.u[Fluid.index(fluidInput.x, fluidInput.y + 1)] += fluidInput.forceX;
			fluid.v[Fluid.index(fluidInput.x, fluidInput.y + 1)] += fluidInput.forceY;
			fluid.u[Fluid.index(fluidInput.x, fluidInput.y - 1)] += fluidInput.forceX;
			fluid.v[Fluid.index(fluidInput.x, fluidInput.y - 1)] += fluidInput.forceY;
			fluid.dens[Fluid.index(fluidInput.x, fluidInput.y)] += fluidInput.density;
		});
	}

	@Override
	public byte provideData(final int x, final int y) {
		double num = fluid.dens[Fluid.index(x / CELL_LENGTH, y / CELL_LENGTH)];
		return (byte) ((num > 255) ? 255 : num);
	}

	public void render() {
		window.render(this);
	}

	private static double nanosToSeconds(long nanos) {
		return nanos / 1E9;
	}

	private static long secondsToMillis(double seconds) {
		return (long) (seconds * 1E3);
	}

}
//
//	int colorIndex;
//		for(int i=0;i<Fluid.WIDTH;i++){
//		for(int j=0;j<Fluid.HEIGHT;j++){
//		double currentDensity=fluid.dens[Fluid.index(i,j)];
//
//		colorIndex=(int)(nColors*(currentDensity*20*0.3));
//		System.out.println(currentDensity);
//		if(colorIndex< 0){
//		colorIndex=0;
//		}
//		if(colorIndex>=nColors){
//		colorIndex=nColors-1;
//		}
//		colorIndex=Color.HSBtoRGB((float)0,(float)0,(float)currentDensity);
//		colorCell(i,j,colorIndex);
//		}
//		}
//
//		if (mouseHandler.mouseDragged) {
//			addDensity(mouseHandler.x, mouseHandler.y, 10);
//			double amountX = mouseHandler.x - mouseHandler.px;
//			double amountY = mouseHandler.y - mouseHandler.py;
//			addVelocity(mouseHandler.x, mouseHandler.y, amountX, amountY);
//		}
