package net.kennux.cubicworld.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

/**
 * Contains gui helper functions.
 * 
 * @author KennuX
 *
 */
public class GuiHelper
{
	/**
	 * Calculates the absolute position for a given percentage.
	 * Uses Gdx.graphics.getWidth / getHeight() to get the screen size.
	 * 
	 * @param percentagePos
	 * @return
	 */
	public static Vector2 getAbsoluteFromPercentagePosition(Vector2 percentagePos)
	{
		// Normalize between 0 and 1
		Vector2 percentage = new Vector2(percentagePos);

		percentage.x /= 100.0f;
		percentage.y /= 100.0f;

		percentage.x *= Gdx.graphics.getWidth();
		percentage.y *= Gdx.graphics.getHeight();

		return percentage;
	}

	/**
	 * Calculates the absolute position for a given percentage.
	 * Uses instead of Gdx.graphics.getWidth / getHeight() the variables width and height
	 * 
	 * @param percentagePos
	 * @return
	 */
	public static Vector2 getAbsoluteFromPercentagePosition(Vector2 percentagePos, int width, int height)
	{
		// Normalize between 0 and 1
		Vector2 percentage = new Vector2(percentagePos);

		percentage.x /= 100.0f;
		percentage.y /= 100.0f;

		percentage.x *= width;
		percentage.y *= height;

		return percentage;
	}
}
