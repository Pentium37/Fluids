public class Fluid {
	public static final int WIDTH = 120;
	public static final int HEIGHT = 100;
	private static final int ITERATIONS = 4;
	public static final double OVERRELAXATION = 1.5;
	public double[] dens, u, v;
	public double diffusionRate, viscosity;
	double dt; // change to deltaTime soon

	public Fluid(final double diffusionRate, final double viscosity) {
		this.diffusionRate = diffusionRate;
		this.viscosity = viscosity;

		// possible error dens = dens_prev = u = ...
		int size = (WIDTH + 2) * (HEIGHT + 2);

		dens = new double[size];
		u = new double[size];
		v = new double[size];

		for (int i = 0; i < WIDTH + 1; i++) {
			for (int j = 0; j < HEIGHT + 1; j++) {
				this.dens[index(i, j)] = 0;
				this.u[index(i, j)] = 0;
				this.v[index(i, j)] = 0;
			}
		}
	}

	public void step(double[] dens_prev, double[] u_prev, double[] v_prev, double deltaTime) {
		this.dt = deltaTime;
		velocityStep(u, v, u_prev, v_prev, viscosity);
		densityStep(dens, dens_prev, u, v, diffusionRate);
	}

	private void densityStep(double[] dens, double[] dens_prev, double[] u, double[] v, double diffusionRate) {
		addSources(dens, dens_prev);
		diffuse(0, dens_prev, dens, diffusionRate);
		advect(0, dens, dens_prev, u, v);
	}

	private void velocityStep(double[] u, double[] v, double[] u_prev, double[] v_prev, double viscosity) {
		addSources(u, u_prev);
		addSources(v, v_prev);

		diffuse(1, u_prev, u, viscosity);
		diffuse(2, v_prev, v, viscosity);

		project(u_prev, v_prev, u, v);

		advect(1, u, u_prev, u_prev, v_prev);
		advect(2, v, v_prev, u_prev, v_prev);
		project(u, v, u_prev, v_prev);
	}

	public void addSources(double[] destination, double[] source) {
		for (int i = 0; i < source.length; i++) {
			destination[i] += dt * source[i];
		}
	}

	public void diffuse(int b, double[] destination, double[] source, double diffusionRate) {
		double a = dt * diffusionRate * WIDTH * HEIGHT;
		gaussSeidel(b, destination, source, a, 1 + 4 * a);
	}

	public void advect(int b, double[] destination, double[] source, double[] u, double[] v) {
		int i0, j0, i1, j1;
		double x, y, s0, t0, s1, t1, dt0_i, dt0_j;

		dt0_i = dt * WIDTH;
		dt0_j = dt * HEIGHT;
		for (int i = 1; i < WIDTH + 1; i++) {
			for (int j = 1; j < HEIGHT + 1; j++) {
				x = i - dt0_i * u[index(i, j)];
				y = j - dt0_j * v[index(i, j)];
				if (x < 0.5) {
					x = 0.5;
				} else if (x > WIDTH + 0.5) {
					x = WIDTH + 0.5;
				}

				i0 = (int) x;
				i1 = i0 + 1;

				if (y < 0.5) {
					y = 0.5;
				} else if (y > HEIGHT + 0.5) {
					y = HEIGHT + 0.5;
				}

				j0 = (int) y;
				j1 = j0 + 1;

				s1 = x - i0;
				s0 = 1 - s1;
				t1 = y - j0;
				t0 = 1 - t1;

				destination[index(i, j)] = s0 * (t0 * source[index(i0, j0)] + t1 * source[index(i0, j1)]) + s1 * (
						t0 * source[index(i1, j0)] + t1 * source[index(i1, j1)]);
			}
		}
		setBoundary(b, destination);
	}

	public void project(double[] u, double[] v, double[] p, double[] divergenceField) {
		double h = FluidSimulation.PPGS;
		for (int i = 1; i < WIDTH + 1; i++) {
			for (int j = 1; j < HEIGHT + 1; j++) {
				divergenceField[index(i, j)] =
						-0.5 * h * (u[index(i + 1, j)] - u[index(i - 1, j)] + v[index(i, j + 1)] - v[index(i, j - 1)]);
				p[index(i, j)] = 0;
			}
		}
		setBoundary(0, divergenceField);
		setBoundary(0, p);
		gaussSeidel(0, p, divergenceField, 1, 4);

		for (int i = 1; i < WIDTH + 1; i++) {
			for (int j = 1; j < HEIGHT + 1; j++) {
				u[index(i, j)] -= OVERRELAXATION * 0.5 * (p[index(i + 1, j)] - p[index(i - 1, j)]) / h;
				v[index(i, j)] -= OVERRELAXATION * 0.5 * (p[index(i, j + 1)] - p[index(i, j - 1)]) / h;
			}
		}
		//potentially need to change this
		setBoundary(1, u);
		setBoundary(2, v);
	}

	public static void gaussSeidel(int b, double[] destination, double[] source, double a, double factor) {
		for (int k = 0; k < ITERATIONS; k++) {
			for (int i = 1; i < WIDTH + 1; i++) {
				for (int j = 1; j < HEIGHT + 1; j++) {
					destination[index(i, j)] =
							(source[index(i, j)] + a * (destination[index(i - 1, j)] + destination[index(i + 1, j)]
									+ destination[index(i, j - 1)] + destination[index(i, j + 1)])) / (factor);
				}
			}
			setBoundary(b, destination);
		}
	}

	public static void setBoundary(int b, double[] destination) {
		// make a global factor for -1 or 1
		for (int i = 1; i < WIDTH + 1; i++) {
			destination[index(0, i)] = (b == 1) ? -1 * destination[index(1, i)] : destination[index(1, i)];
			destination[index(WIDTH + 1, i)] =
					(b == 1) ? -1 * destination[index(WIDTH, i)] : destination[index(WIDTH, i)];

			destination[index(i, 0)] = (b == 2) ? -1 * destination[index(i, 1)] : destination[index(i, 1)];
			destination[index(i, HEIGHT + 1)] =
					(b == 2) ? -1 * destination[index(i, HEIGHT)] : destination[index(i, HEIGHT)];
		}
		destination[index(0, 0)] = 0.5 * (destination[index(1, 0)] + destination[index(0, 1)]);
		destination[index(0, HEIGHT + 1)] = 0.5 * (destination[index(1, HEIGHT + 1)] + destination[index(0, HEIGHT)]);
		destination[index(WIDTH + 1, 0)] = 0.5 * (destination[index(WIDTH, 0)] + destination[index(WIDTH + 1, 1)]);
		destination[index(WIDTH + 1, HEIGHT + 1)] =
				0.5 * (destination[index(WIDTH, HEIGHT + 1)] + destination[index(WIDTH + 1, HEIGHT)]);
	}

	public static int index(int i, int j) {
		if (i < 0) {
			i = 0;
		}
		if (i > WIDTH + 1) {
			i = WIDTH + 1;
		}
		if (j < 0) {
			j = 0;
		}
		if (j > HEIGHT + 1) {
			j = HEIGHT + 1;
		}
		return (i + j * (HEIGHT + 2));
	}
}
