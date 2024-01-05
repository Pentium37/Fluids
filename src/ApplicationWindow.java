import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class ApplicationWindow extends JFrame {
	public final int IMAGE_WIDTH;
	public final int IMAGE_HEIGHT;
	public BufferedImage buffer;

	public ApplicationWindow(int IMAGE_WIDTH, int IMAGE_HEIGHT) {
		super("Eulerian Fluid Simulation");
		this.IMAGE_WIDTH = IMAGE_WIDTH;
		this.IMAGE_HEIGHT = IMAGE_HEIGHT;
		setSize(IMAGE_WIDTH, IMAGE_HEIGHT + 30);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setFocusable(true);
		setVisible(true);
	}

	@Override
	public synchronized void paint(Graphics g) {
		if (buffer == null) {
			return;
		}

		g.drawImage(buffer, 0, 30, IMAGE_WIDTH, IMAGE_HEIGHT, null);
	}

	public synchronized void render(DataProvider pixelStream) {
		BufferedImage imageContainer = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_BYTE_GRAY);
		byte[] buffer = ((DataBufferByte) imageContainer.getRaster()
				.getDataBuffer()).getData();

		for (int y = 0; y < imageContainer.getHeight(); y++) {
			for (int x = 0; x < imageContainer.getWidth(); x++) {
				buffer[y * IMAGE_WIDTH + x] = pixelStream.provideData(x, y);
			}
		}

		this.buffer = imageContainer;
		repaint();
	}
}

//		BufferedImage imageContainer = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_BYTE_GRAY);
//		for (int y = 0; y < IMAGE_WIDTH; y++) {
//			for (int x = 0; x < IMAGE_HEIGHT; x++) {
//				int value = pixelStream.provideData(x, y);
//				int color = new Color(value, value, value).getRGB();
//				imageContainer.setRGB(x, y, color);
//			}
//		} this.buffer = imageContainer;
//		repaint();
