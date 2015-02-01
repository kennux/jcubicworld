package net.kennux.cubicworld.voxel.handlers;

import net.kennux.cubicworld.voxel.VoxelData;

/**
 * <pre>
 * This interface gets used to hold voxel update handlers.
 * A voxel type can hold a update handler which will get called on every tick.
 * </pre>
 * 
 * @author KennuX
 *
 */
public interface IVoxelUpdateHandler
{
	/**
	 * x y and z are in worldspace coordinates
	 * 
	 * @param voxelData
	 * @param x
	 * @param y
	 * @param z
	 * @param isServer
	 */
	public void handleUpdate(VoxelData voxelData, int x, int y, int z, boolean isServer);
}
