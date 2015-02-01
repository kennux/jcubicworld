package net.kennux.cubicworld.voxel.generator;

import net.kennux.cubicworld.plugins.baseplugin.BasePlugin;
import net.kennux.cubicworld.voxel.VoxelChunk;
import net.kennux.cubicworld.voxel.VoxelData;
import net.kennux.cubicworld.voxel.VoxelWorld;
import net.kennux.cubicworld.voxel.generator.noise.SimplexNoise3D;

/**
 * <pre>
 * World generator implementation.
 * <strong>This is currently experimental!</strong>
 * 
 * </pre>
 * 
 * @author kennux
 *
 */
public class WorldGenerator extends AWorldGenerator
{

	public void GenerateWorld(int chunkX, int chunkY, int chunkZ, VoxelChunk chunk)
	{
		// Initialize voxel data
		VoxelData[][][] voxelData = new VoxelData[VoxelWorld.chunkWidth][VoxelWorld.chunkHeight][VoxelWorld.chunkDepth];

		// Initialize chunk base position
		int chunkBaseX = (chunkX * VoxelWorld.chunkWidth);
		int chunkBaseY = (chunkY * VoxelWorld.chunkHeight);
		int chunkBaseZ = (chunkZ * VoxelWorld.chunkDepth);

		for (int x = 0; x < VoxelWorld.chunkWidth; x++)
		{
			// Calculate absolute position
			int absoluteX = x + chunkBaseX;
			for (int z = 0; z < VoxelWorld.chunkDepth; z++)
			{
				// Calculate absolute position
				int absoluteZ = z + chunkBaseZ;

				// Calculate 2d simplex noise for height
				int absoluteHeight = Math.max(64, Math.abs((int) (chunk.master.worldHeight * SimplexNoise3D.noise(absoluteX * 0.005f, absoluteZ * 0.005f))));

				// Calculate x|z height
				int height = Math.min(VoxelWorld.chunkHeight, absoluteHeight - chunkBaseY);

				// Now the world generation algorithm starts
				for (int y = 0; y < height; y++)
				{
					// If this voxel data is not already set
					if (voxelData[x][y][z] == null)
					{
						// Calculate absolute position
						int absoluteY = y + chunkBaseY;

						double noise = SimplexNoise3D.noise(absoluteX * 0.05f, absoluteY * 0.05f, absoluteZ * 0.05f);

						// Block in this position?
						if (noise < 0.5f)
						{
							// Grass?
							if (absoluteY == absoluteHeight - 1)
							{
								voxelData[x][y][z] = VoxelData.construct((short) BasePlugin.voxelGrassId);
							}
							else if (absoluteY < absoluteHeight && absoluteY > absoluteHeight - 6)
							{
								voxelData[x][y][z] = VoxelData.construct((short) BasePlugin.voxelDirtId);
							}
							else if (absoluteY == 0)
							{
								voxelData[x][y][z] = VoxelData.construct((short) BasePlugin.voxelBedrockId);
							}
							else
							{
								voxelData[x][y][z] = VoxelData.construct((short) BasePlugin.voxelStoneId);
							}
						}
						else
						{
							voxelData[x][y][z] = null;
						}
					}
				}
			}
		}

		chunk.setVoxelData(voxelData);
	}

}
