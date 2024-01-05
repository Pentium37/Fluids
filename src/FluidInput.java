import java.util.StringJoiner;

class FluidInput {
	double forceX, forceY;
	double density;
	int x;
	int y;

	FluidInput(int x, int y, double forceX, double forceY, double density) {
		this.x = (x / FluidSimulation.CELL_LENGTH);
		this.y = (y / FluidSimulation.CELL_LENGTH);
		this.forceX = forceX;
		this.forceY = forceY;
		this.density = density;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", FluidInput.class.getSimpleName() + "[", "]").add("forceX=" + forceX)
				.add("forceY=" + forceY)
				.add("density=" + density)
				.add("x=" + x)
				.add("y=" + y)
				.toString();
	}
}
