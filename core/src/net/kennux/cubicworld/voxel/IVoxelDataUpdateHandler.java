package net.kennux.cubicworld.voxel;

/**
 * <pre>
 * Interface for anonymous voxel update handler function.
 * Use VoxelWorld.voxelUpdateHandler = new IVoxelUpdateHandler() { ... } to set
 * your handler.
 * </pre>
 * 
 * @author KennuX
 *
 */
public interface IVoxelDataUpdateHandler
{
	/**
	 * Gets called everytime a voxel gets modified.
	 * On the serverside this handler gets used to send out a chunk update.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param newData
	 */
	public void handleVoxelDataUpdate(int x, int y, int z, VoxelData newData);
}
