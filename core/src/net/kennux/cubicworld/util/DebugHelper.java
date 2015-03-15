package net.kennux.cubicworld.util;

import java.text.DecimalFormat;
import java.util.LinkedList;

import net.kennux.cubicworld.CubicWorld;
import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.networking.CubicWorldClient;
import net.kennux.cubicworld.voxel.ChunkKey;
import net.kennux.cubicworld.voxel.VoxelWorld;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Renders debug information.
 * The renderDebugInformation() function will render a minecraft like debug
 * screen.
 * 
 * @author KennuX
 *
 */
public class DebugHelper
{
	/**
	 * Renders debug information in 3d-space.
	 * 
	 * @param camera
	 * @param client
	 */
	public static void renderDebug(PerspectiveCamera camera, CubicWorldGame cubicWorld)
	{
		if (shapeRenderer == null)
		{
			shapeRenderer = new ShapeRenderer();
		}

		if (renderChunkBoundingBoxes)
		{
			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.setProjectionMatrix(camera.combined);
			shapeRenderer.setColor(Color.RED);
			// Get all currently loaded chunk keys.
			ChunkKey[] keys = cubicWorld.voxelWorld.getKeys();
			for (ChunkKey key : keys)
			{
				Vector3 position = new Vector3(key.x * VoxelWorld.chunkWidth, key.y * VoxelWorld.chunkHeight, key.z * VoxelWorld.chunkDepth);
				shapeRenderer.box(position.x, position.y, position.z, VoxelWorld.chunkWidth, VoxelWorld.chunkHeight, -VoxelWorld.chunkDepth);
			}
			shapeRenderer.end();
		}
	}

	/**
	 * Renders a minecraft like debug screen.
	 * 
	 * @param camera
	 */
	public static void renderDebugInformation(PerspectiveCamera camera, CubicWorldClient client)
	{
		// Init
		if (spriteBatch == null)
		{
			spriteBatch = new SpriteBatch();
			font = new BitmapFont();
			fpsCounterStack = new LinkedList<Integer>();
			drawcallCounterStack = new LinkedList<Integer>();
			upstreamBytesStack = new LinkedList<Integer>();
			downstreamBytesStack = new LinkedList<Integer>();
		}

		// Update information stacks every 20th frame
		if (Gdx.graphics.getFrameId() % 2 == 0)
		{
			// Fps counter
			fpsCounterStack.addLast(new Integer(Gdx.graphics.getFramesPerSecond()));
			// More than 120 in there?
			if (fpsCounterStack.size() > 120)
			{
				fpsCounterStack.removeFirst();
			}
		}

		// Networking update
		upstreamBytesStack.addLast(client.getBytesUpstream());
		// More than 120 in there?
		if (upstreamBytesStack.size() > 120)
		{
			upstreamBytesStack.removeFirst();
		}
		downstreamBytesStack.addLast(client.getBytesDownstream());
		// More than 120 in there?
		if (downstreamBytesStack.size() > 120)
		{
			downstreamBytesStack.removeFirst();
		}

		// Drawcall counter update
		drawcallCounterStack.addLast(new Integer(GLProfiler.drawCalls));
		// More than 120 in there?
		if (drawcallCounterStack.size() > 120)
		{
			drawcallCounterStack.removeFirst();
		}

		int screenHeight = Gdx.graphics.getHeight();
		int screenWidth = Gdx.graphics.getWidth();

		// Render information
		spriteBatch.begin();
		font.draw(spriteBatch, CubicWorld.getClient().dayNightCycle.getTimeString(), screenWidth - 100, screenHeight);

		font.draw(spriteBatch, "Position (XYZ): " + camera.position.x + "|" + camera.position.y + "|" + camera.position.z, 10, screenHeight);
		font.draw(spriteBatch, "LookDir (XYZ): " + camera.direction.x + "|" + camera.direction.y + "|" + camera.direction.z, 10, screenHeight - 20);
		font.draw(spriteBatch, "FPS: " + Gdx.graphics.getFramesPerSecond(), screenWidth - 100, screenHeight - 20);
		font.draw(spriteBatch, "Draw calls: " + GLProfiler.drawCalls, 10, screenHeight - 40);
		// TODO Why does this produce the same as Draw calls: ?
		font.draw(spriteBatch, "Vertex Count: " + (int) GLProfiler.vertexCount.total, 10, screenHeight - 60);
		font.draw(spriteBatch, "Texture Bindings: " + GLProfiler.textureBindings, 10, screenHeight - 80);

		// VM Info
		int mb = 1024 * 1024;

		// Getting the runtime reference from system
		Runtime runtime = Runtime.getRuntime();

		font.draw(spriteBatch, "Used Memory: " + (runtime.totalMemory() - runtime.freeMemory()) / mb + " MB", 10, screenHeight - 120);
		font.draw(spriteBatch, "Free Memory: " + runtime.freeMemory() / mb + " MB", 10, screenHeight - 140);
		font.draw(spriteBatch, "Total Memory: " + runtime.totalMemory() / mb + " MB", 10, screenHeight - 160);
		font.draw(spriteBatch, "Max Memory: " + runtime.maxMemory() / mb + " MB", 10, screenHeight - 180);
		font.draw(spriteBatch, "Java VM: " + System.getProperty("java.vm.name"), 10, screenHeight - 200);

		spriteBatch.end();

		// Diagrams
		if (drawDiagrams)
		{
			// FPS-Diagram
			float[] fpsEntries = new float[fpsCounterStack.size()];
			for (int i = 0; i < fpsEntries.length; i++)
				fpsEntries[i] = fpsCounterStack.get(i);

			StatisticsHelper.renderLineDiagram("FPS", new Vector2(screenWidth - 350, 10), new Vector2(300, 200), fpsEntries, 0f);

			// Drawcall-Diagram
			float[] drawcallEntries = new float[drawcallCounterStack.size()];
			for (int i = 0; i < drawcallEntries.length; i++)
				drawcallEntries[i] = drawcallCounterStack.get(i);

			StatisticsHelper.renderLineDiagram("Drawcalls", new Vector2(screenWidth - 350, 250), new Vector2(300, 200), drawcallEntries, 0f);

			// Render profiling results
			String[] profilingNames = new String[] { "Update", "Render" };

			Color[] profilingColors = new Color[] { Color.RED, Color.GREEN };

			// Get profiling results
			float[] profilingValues = new float[profilingNames.length];
			for (int i = 0; i < profilingValues.length; i++)
			{
				// Get profiling result
				profilingValues[i] = CubicWorld.getClient().profiler.getProfilerResult(profilingNames[i]);
			}

			// Render profiling legend
			int yAxisPosition = 700;
			spriteBatch.begin();
			for (int i = 0; i < profilingNames.length; i++)
			{
				Vector2 currentPosition = new Vector2(screenWidth - 130, yAxisPosition);

				font.setColor(profilingColors[i]);
				font.draw(spriteBatch, profilingNames[i] + " " + new DecimalFormat("#.##").format((profilingValues[i] / 1000.0f) / 1000.0f), currentPosition.x, currentPosition.y);

				yAxisPosition -= 20;
			}
			spriteBatch.end();

			// Reset font color
			font.setColor(Color.WHITE);

			StatisticsHelper.renderPiechart("Profilings", new Vector2(screenWidth - 350, 500), 100, profilingValues, profilingColors);
		}

		// Network diagrams
		if (drawNetworkDiagrams)
		{
			// Upstream-Diagram
			float[] upstreamEntries = new float[upstreamBytesStack.size()];
			for (int i = 0; i < upstreamEntries.length; i++)
				upstreamEntries[i] = upstreamBytesStack.get(i).floatValue() / 1024f;

			StatisticsHelper.renderLineDiagram("Upstream (kB)", new Vector2(screenWidth - 700, 250), new Vector2(300, 200), upstreamEntries, 0f);

			// Downstream-Diagram
			float[] downstreamEntries = new float[downstreamBytesStack.size()];
			for (int i = 0; i < downstreamEntries.length; i++)
				downstreamEntries[i] = downstreamBytesStack.get(i).floatValue() / 1024f;

			StatisticsHelper.renderLineDiagram("Downstream (kB)", new Vector2(screenWidth - 700, 10), new Vector2(300, 200), downstreamEntries, 0f);
		}
	}

	/**
	 * The sprite batch used for rendering the debug information to the screen.
	 */
	private static SpriteBatch spriteBatch;

	/**
	 * The font used for rendering.
	 */
	private static BitmapFont font;
	/**
	 * The lifo stack used for recording the 60 latest fps counts.
	 */
	private static LinkedList<Integer> fpsCounterStack;
	/**
	 * The lifo stack used for recording the 60 latest drawcall counts.
	 */
	private static LinkedList<Integer> drawcallCounterStack;

	/**
	 * The lifo stack used for recording the 60 latest drawcall counts.
	 */
	private static LinkedList<Integer> upstreamBytesStack;

	/**
	 * The lifo stack used for recording the 60 latest drawcall counts.
	 */
	private static LinkedList<Integer> downstreamBytesStack;

	// renderDebug() variables

	/**
	 * If this is set to true, diagrams will get drawn.
	 */
	public static boolean drawDiagrams = false;

	/**
	 * If this is set to true, network diagrams will get drawn.
	 */
	public static boolean drawNetworkDiagrams = false;

	private static ShapeRenderer shapeRenderer;

	/**
	 * If this is set to true renderDebug() will render chunk bounding boxes.
	 */
	public static boolean renderChunkBoundingBoxes = false;

}
