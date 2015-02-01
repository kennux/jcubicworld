package net.kennux.cubicworld.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;

/**
 * Statics helper class contains some functions used to draw statistic information.
 * For example rendering a line diagram.
 * 
 * @author kennux
 *
 */
public class StatisticsHelper
{
	/**
	 * Initializes the spritebatch, shape renderer and bitmap font if they aren't initialized already.
	 */
	private static void init()
	{
		if (font == null)
			font = new BitmapFont();
		if (shapeRenderer == null)
			shapeRenderer = new ShapeRenderer();
		if (spriteBatch == null)
			spriteBatch = new SpriteBatch();
	}

	/**
	 * Renders a line diagram with min and max value sizes calculated from entries array.
	 * 
	 * @param position
	 * @param size
	 * @param entries
	 */
	public static void renderLineDiagram(String caption, Vector2 position, Vector2 size, float[] entries)
	{
		// Get lowest and highest entry
		// And pixels per entry.
		Float lowestEntry = null;
		Float highestEntry = null;

		for (float e : entries)
		{
			if (lowestEntry == null || lowestEntry.floatValue() > e)
				lowestEntry = new Float(e);
			else if (highestEntry == null || highestEntry.floatValue() < e)
				highestEntry = new Float(e);
		}

		renderLineDiagram(caption, position, size, entries, lowestEntry, highestEntry);
	}

	/**
	 * Renders a line diagram with max value sizes calculated from entries array.
	 * 
	 * @param position
	 * @param size
	 * @param entries
	 * @param minEntry
	 *            The minimum entry value
	 */
	public static void renderLineDiagram(String caption, Vector2 position, Vector2 size, float[] entries, Float minEntry)
	{
		// Get lowest and highest entry
		// And pixels per entry.
		Float highestEntry = null;

		for (float e : entries)
		{
			if (highestEntry == null || highestEntry.floatValue() < e)
				highestEntry = new Float(e);
		}

		renderLineDiagram(caption, position, size, entries, minEntry, highestEntry);
	}

	/**
	 * Renders a line diagram.
	 * The caption will get rendered at: Vector2(position.x, position.y + size.y + 20), so 20 pixel over the main diagram.
	 * If you don't want a caption put an empty string or null in caption.
	 * 
	 * @param size
	 *            The width and height of the line diagram in screenspace.
	 * @param position
	 *            The position of the line diagram in screenspace.
	 * @param entries
	 *            The entries to show. Index is their position, 0 = left.
	 * @param minEntry
	 *            The minimum entry value
	 * @param maxEntry
	 *            The maximum entry value
	 */
	public static void renderLineDiagram(String caption, Vector2 position, Vector2 size, float[] entries, Float minEntry, Float maxEntry)
	{
		init();

		shapeRenderer.begin(ShapeType.Line);

		// Render frame
		shapeRenderer.line(new Vector2(position).add(0, -1), new Vector2(position).add(0, size.y + 1)); // Y+
		shapeRenderer.line(position, new Vector2(position).add(size.x, 0)); // X+

		// Render entries
		if (maxEntry != null && minEntry != null)
		{
			// Calculate all needed variables for rendering
			float pixelsXPerEntry = size.x / entries.length;
			float highestLowestDistance = maxEntry - minEntry;
			float xAxisPointer = 0;
			Vector2 lastPosition = null;

			// Render lines from every entry point to the next.
			for (float e : entries)
			{
				float entryHeight = ((e - minEntry) / highestLowestDistance) * size.y;
				Vector2 pos = new Vector2(xAxisPointer, entryHeight).add(position);

				if (lastPosition != null)
				{
					// Extend
					shapeRenderer.line(lastPosition, pos);
				}

				xAxisPointer += pixelsXPerEntry;
				lastPosition = pos;
			}
		}
		shapeRenderer.end();

		spriteBatch.begin();

		// Render caption
		if (caption != null && !caption.isEmpty())
			font.draw(spriteBatch, caption + ":", position.x, position.y + size.y + 20);

		// Render fonts
		if (maxEntry != null && minEntry != null)
		{
			font.draw(spriteBatch, minEntry.toString(), position.x - 50, position.y + 20);
			font.draw(spriteBatch, maxEntry.toString(), position.x - 50, position.y + size.y);
		}
		spriteBatch.end();
	}

	/**
	 * Renders a pie chart to the screen at the given position with the given size.
	 * The caption will get rendered at: Vector2(position.x, position.y + (radius*2) + 20), so 20 pixel over the main diagram.
	 * If you don't want a caption put an empty string or null in caption.
	 * 
	 * @param caption
	 * @param position
	 * @param radius
	 * @param entryValues
	 * @param entryColors
	 */
	public static void renderPiechart(String caption, Vector2 position, float radius, float[] entryValues, Color[] entryColors)
	{
		init();

		// Add all entries
		int entriesAdded = 0;
		for (float e : entryValues)
			entriesAdded += e;

		// Get entry percentages
		float[] entryPercentages = new float[entryValues.length];
		for (int i = 0; i < entryPercentages.length; i++)
		{
			entryPercentages[i] = entryValues[i] / entriesAdded;
		}

		// Calculate center position
		Vector2 centerPosition = new Vector2(position).add(radius, radius);

		// Render arcs
		shapeRenderer.begin(ShapeType.Filled);
		float currentDegrees = 0;

		// Render all entries
		for (int i = 0; i < entryPercentages.length; i++)
		{
			// Calculate degrees
			float degrees = entryPercentages[i] * 360;

			// Set color
			shapeRenderer.setColor(entryColors[i]);

			// Render arc
			shapeRenderer.arc(centerPosition.x, centerPosition.y, radius, currentDegrees, degrees);

			currentDegrees += degrees;
		}

		shapeRenderer.end();

		// Reset color
		shapeRenderer.setColor(Color.WHITE);

		spriteBatch.begin();

		// Render caption
		if (caption != null && !caption.isEmpty())
			font.draw(spriteBatch, caption + ":", position.x, position.y + (radius * 2) + 20);

		spriteBatch.end();
	}

	/**
	 * The sprite batch used for rendering the debug information to the screen.
	 */
	private static SpriteBatch spriteBatch;

	/**
	 * The shape renderer used for rendering diagrams.
	 */
	private static ShapeRenderer shapeRenderer;

	/**
	 * The font used for rendering.
	 */
	private static BitmapFont font;
}
