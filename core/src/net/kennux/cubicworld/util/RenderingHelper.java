package net.kennux.cubicworld.util;

import com.badlogic.gdx.graphics.Pixmap;

/**
 * Contains some rendering helper functions like flipping a pixmap.
 * 
 * @author kennux
 *
 */
public class RenderingHelper
{
	/**
	 * Fĺips the given pixmap on Y-axis.
	 * This function gets for example used to take screenshots.
	 * When opengl renders an image to a framebuffer y axis will be flipped, so this function can reflip a pixmap.
	 * 
	 * @param src
	 * @return
	 */
	public static Pixmap flipPixmapY(Pixmap src)
	{
		final int width = src.getWidth();
		final int height = src.getHeight();
		Pixmap flipped = new Pixmap(width, height, src.getFormat());

		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				flipped.drawPixel(x, y, src.getPixel(x, height - y - 1));
			}
		}
		return flipped;
	}
}
