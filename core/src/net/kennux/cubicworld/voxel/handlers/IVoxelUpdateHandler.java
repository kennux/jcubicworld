package net.kennux.cubicworld.voxel.handlers;

import net.kennux.cubicworld.voxel.VoxelData;
import net.kennux.cubicworld.voxel.datamodels.IVoxelDataModel;

/**
 * <pre>
 * This interface gets used to hold voxel update handlers.
 * A voxel type can hold a update handler which will get called on every tick.
 * 
 * Use the generic type parameter to define the class of the data model.
 * If there is no data model attached to your voxel type, just use the non-generic version of this interface.
 * Null will then be passed in as data model.
 * </pre>
 * 
 * @author KennuX
 *
 */
public interface IVoxelUpdateHandler<T extends IVoxelDataModel>
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
	public void handleUpdate(VoxelData voxelData, int x, int y, int z, boolean isServer, T dataModel);
}
