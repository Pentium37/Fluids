import java.awt.event.MouseEvent;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import static java.lang.Math.hypot;

public class MouseAdapter extends java.awt.event.MouseAdapter {
	private int[] previousCoords;
	private boolean mouseHeld = false;
	private List<FluidInput> sourceQueue;
	private Instant startAdd;

	public MouseAdapter() {
		sourceQueue = new LinkedList<>();
		startAdd = Instant.now();
	}

	public void mousePressed(MouseEvent e) {
		previousCoords = new int[] { e.getX(), e.getY() };
		mouseHeld = true;
		startAdd = Instant.now();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		mouseHeld = false;
	}

	public synchronized void mouseDragged(MouseEvent e) {
		double velocityX = 0;
		double velocityY = 0;
		double dragScalar = 5.0;

		if (previousCoords != null) {
			velocityX = dragScalar * ((double) e.getX() - previousCoords[0]);
			velocityY = dragScalar * ((double) e.getY() - previousCoords[1]);
		}

		startAdd = Instant.now();
		previousCoords = new int[] { e.getX(), e.getY() };
		sourceQueue.add(new FluidInput(previousCoords[0], previousCoords[1] - (30), velocityX, velocityY,
				6.0 * hypot(velocityX, velocityY)));
	}

	synchronized void consumeSources(Consumer<FluidInput> sourceConsumer) {
		if (mouseHeld) {
			long timeHeld = Duration.between(startAdd, Instant.now())
					.toMillis();
			startAdd = Instant.now();
			sourceQueue.add(new FluidInput(previousCoords[0], previousCoords[1], 0, 0, timeHeld * 20.0));
		}

		for (FluidInput source : sourceQueue) {
			sourceConsumer.accept(source);
		}
		sourceQueue.clear();
	}
}
