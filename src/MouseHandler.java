import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class MouseHandler implements MouseMotionListener {
	public boolean mouseDragged;
	public int x;
	public int px;
	public int y;
	public int py;

	public MouseHandler() {
//		px = 0;
//		py = 0;
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		mouseDragged = true;
		int a = e.getX() / FluidSimulation.PPGS;
		int b = e.getY() / FluidSimulation.PPGS;

		if (a >= 0 && a <= Fluid.WIDTH && b >= 0 && b <= Fluid.HEIGHT) {
			px = x;
			py = y;

			x = a;
			y = b;
		}
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
		mouseDragged = false;
	}
}
