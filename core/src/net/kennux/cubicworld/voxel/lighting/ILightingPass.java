package net.kennux.cubicworld.voxel.lighting;

import net.kennux.cubicworld.voxel.VoxelChunk;

/**
 * Interface for implementing lighting passes.
 * 
 * @author KennuX
 *
 */
public interface ILightingPass
{
	/**
	 * Executes the lighting pass and returns true if the lighting is done.
	 * 
	 * @param chunk
	 * @return
	 */
	public boolean executePass(VoxelChunk chunk);
}