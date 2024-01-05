public class Fluid {
	public static final int WIDTH = 128;
	public static final int HEIGHT = 128;
	private static final int ITERATIONS = 20;
	public static final double OVERRELAXATION = 1;
	public double[] dens, u, v;
	public double diffusionRate, viscosity;
	double dt; // change to deltaTime soon

	// possible error dens = dens_prev = u = ...

	public Fluid(final double diffusionRate, final double viscosity) {
		this.diffusionRate = diffusionRate;
		this.viscosity = viscosity;

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

	public void step(double deltaTime, double[] u_prev, double[] v_prev, double[] dens_prev) {
		this.dt = deltaTime;
		diffuse(1, u_prev, u, viscosity);
		diffuse(2, v_prev, v, viscosity);

		project(u_prev, v_prev);

		advect(1, u, u_prev, u_prev, v_prev);
		advect(2, v, v_prev, u_prev, v_prev);
		project(u, v);

		diffuse(0, dens_prev, dens, diffusionRate);
		advect(0, dens, dens_prev, u, v);
	}

	public void diffuse(int b, double[] destination, double[] source, double diffusionRate) {
		double a = dt * diffusionRate; // be wary
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

	public void project(double[] u, double[] v) {
		int size = (WIDTH + 2) * (HEIGHT + 2);
		double[] pressure = new double[size];
		double[] divergenceField = new double[size];
		double h = FluidSimulation.CELL_LENGTH;

		for (int i = 1; i < WIDTH + 1; i++) {
			for (int j = 1; j < HEIGHT + 1; j++) {
				divergenceField[index(i, j)] =
						-0.5 * h * (u[index(i + 1, j)] - u[index(i - 1, j)] + v[index(i, j + 1)] - v[index(i, j - 1)]);
				pressure[index(i, j)] = 0;
			}
		}
		setBoundary(0, divergenceField);
		setBoundary(0, pressure);
		gaussSeidel(0, pressure, divergenceField, 1, 4.0);

		for (int i = 1; i < WIDTH + 1; i++) {
			for (int j = 1; j < HEIGHT + 1; j++) {
				u[index(i, j)] -= OVERRELAXATION * 0.5 * (pressure[index(i + 1, j)] - pressure[index(i - 1, j)]) / h;
				v[index(i, j)] -= OVERRELAXATION * 0.5 * (pressure[index(i, j + 1)] - pressure[index(i, j - 1)]) / h;
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
			destination[index(0, i)] = (b == 1) ? -destination[index(1, i)] : destination[index(1, i)];
			destination[index(WIDTH + 1, i)] = (b == 1) ? -destination[index(WIDTH, i)] : destination[index(WIDTH, i)];
			destination[index(i, 0)] = (b == 2) ? -destination[index(i, 1)] : destination[index(i, 1)];
			destination[index(i, HEIGHT + 1)] =
					(b == 2) ? -destination[index(i, HEIGHT)] : destination[index(i, HEIGHT)];
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
		return (i + j * (WIDTH + 2));
	}
}


//		for (int ix = 1; ix < WIDTH - 1; ix++) {
//			for (int iy = 1; iy < HEIGHT - 1; iy++) {
//				//center of tile
//				double x = ix - dt * u[index(ix, iy)];
//				double y = iy - dt * v[index(ix, iy)];
//
//				x = min(max(x, 1.5), WIDTH - 0.5);
//				y = min(max(y, 0.5), HEIGHT - 0.5);
//
//				int i0 = (int) min(max(x, 1), WIDTH - 2);
//				int j0 = (int) min(max(y, 1), HEIGHT - 2);
//
//				double t1 = y - j0;
//				double t0 = 1 - t1;
//
//				destination[index(ix, iy)] = (1 + i0 - x) * (t0 * source[index(i0, j0)] + t1 * source[index(i0, j0 + 1)]) +
//						(x - i0) * (t0 * source[index(i0 + 1, j0)] + t1 * source[index(i0 + 1, j0 + 1)]);
//			}
//		}
//		setBoundary(b, destination);
// Code to save for later
//					if (factor == 4) {
//						if (i == WIDTH || i == 1 || j == HEIGHT || j == 1) {
//							if ((i == WIDTH && j == HEIGHT) || (i == WIDTH && j == 1) || (i == 1 && j == HEIGHT) || (
//									i == 1 && j == 1)) {
//								destination[index(i, j)] =
//										(source[index(i, j)] + a * (destination[index(i - 1, j)] + destination[index(
//												i + 1, j)] + destination[index(i, j - 1)] + destination[index(i,
//												j + 1)])) / (2);
//							} else {
//								destination[index(i, j)] =
//										(source[index(i, j)] + a * (destination[index(i - 1, j)] + destination[index(
//												i + 1, j)] + destination[index(i, j - 1)] + destination[index(i,
//												j + 1)])) / (3);
//							}
//							continue;
//						}
//					}