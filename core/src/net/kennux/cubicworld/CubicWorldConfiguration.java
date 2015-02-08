package net.kennux.cubicworld;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class CubicWorldConfiguration
{
	/**
	 * Loads the cubicworld configuration.
	 * 
	 * @throws IOException
	 *             Gets thrown if something went wrong when reading the configs.
	 */
	public static void load() throws IOException
	{
		// Plugin config existing?
		File pluginConfig = new File("plugins.conf");

		// If file not exists, create it.
		if (!pluginConfig.exists())
		{
			// If non existing, write standard configuration
			pluginConfig.createNewFile();
			BufferedWriter configWriter = new BufferedWriter(new FileWriter(pluginConfig));
			configWriter.write("# 1 plugin classpath per line, if the line starts with # it marks a comment.\r\nnet.kennux.cubicworld.plugins.baseplugin.BasePlugin");
			configWriter.flush();
			configWriter.close();
		}

		// Load plugin config
		BufferedReader reader = new BufferedReader(new FileReader(pluginConfig));
		String currentLine = null;
		ArrayList<String> classPaths = new ArrayList<String>();
		while ((currentLine = reader.readLine()) != null)
		{
			// Not a comment?
			if (currentLine.charAt(0) != '#')
			{
				// Add classpath
				classPaths.add(currentLine);
			}
		}
		reader.close();

		plugins = classPaths.toArray(new String[classPaths.size()]);
	}

	/**
	 * The chunkload distance aka viewdistance.
	 * This gets used to check if the player needs to request any new chunks
	 * from the server.
	 */
	public static final int chunkLoadDistance = 6;

	/**
	 * The distance used to cull chunk update packets.
	 */
	public static final int chunkUpdateDistance = chunkLoadDistance * 16;

	/**
	 * The lightlevel every block will get from beginning.
	 */
	public static final int baseLightLevel = 7;

	/**
	 * The maximum light level a block can have.
	 */
	public static final int maxLightLevel = 15;

	/**
	 * The distance used to cull entity update packets.
	 * Same as chunk update distance.
	 */
	public static final float entityCullDistance = chunkUpdateDistance;

	/**
	 * The timeout after a socket is considered "dead".
	 */
	public static final int socketTimeout = 2000;

	/**
	 * After recieving no entity update for entityUpdateTimeout milliseconds.
	 */
	public static final int entityUpdateTimeout = 15000; // After recieving no
															// update since timeout
															// milliseconds an
															// entity will get
															// removed on client
															// side.

	/**
	 * The standard packet cull distance.
	 * Will get returned by getCullDistance() in APacketModel by standard.
	 */
	public static final float standardCullDistance = entityCullDistance;

	/**
	 * Limits the voxel mesh generations (calls to VoxelChunk.generateMesh()) for chunks per frame.
	 * -1 means there is no limit.
	 */
	public static int meshGenerationsPerFrameLimit = 1;

	/**
	 * Limits the voxel mesh creations (calls to VoxelChunk.createNewMesh()) for chunks per frame.
	 * -1 means there is no limit.
	 */
	public static int meshCreationsPerFrameLimit = 1;
	
	/**
	 * All plugin classpaths are stored in here after load() was called.
	 */
	public static String[] plugins;
}
