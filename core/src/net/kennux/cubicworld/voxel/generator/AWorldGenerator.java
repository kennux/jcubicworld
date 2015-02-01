package net.kennux.cubicworld.voxel.generator;

import net.kennux.cubicworld.voxel.VoxelChunk;

/**
 * Abstract world generator class. Implement this abstract class in your own
 * world generator and use it for world generation with a VoxelWorld instance.
 * 
 * @author KennuX
 *
 */
public abstract class AWorldGenerator
{
	/**
	 * Generate the given chunk.
	 * 
	 * @param chunkX
	 * @param chunkY
	 * @param chunkZ
	 * @param chunk
	 *            Chunk to generate.
	 */
	public abstract void GenerateWorld(int chunkX, int chunkY, int chunkZ, VoxelChunk chunk);
}
