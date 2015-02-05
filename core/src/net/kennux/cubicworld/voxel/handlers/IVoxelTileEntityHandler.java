package net.kennux.cubicworld.voxel.handlers;

import net.kennux.cubicworld.serialization.BitReader;
import net.kennux.cubicworld.serialization.BitWriter;
import net.kennux.cubicworld.voxel.VoxelData;

/**
 * <pre>
 * This interface gets used to hold voxel tile entity handlers.
 * Tile entity handlers can get used to recieve updates or other events and to store additional block data.
 * 
 * If your tile entity handler needs initialization, place it in a constructor <b>WITHOUT</b> any parameters.
 * </pre>
 * 
 * @author KennuX
 *
 */
public interface IVoxelTileEntityHandler extends Cloneable
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

	/**
	 * Gets called if the block action (clientside, on clicking it / opening
	 * it's gui) get called.
	 * 
	 * @param voxelData
	 * @param x
	 * @param y
	 * @param z
	 */
	public void handleAction(VoxelData voxelData, int x, int y, int z);

	public void serialize(BitWriter writer);

	public void deserialize(BitReader reader);
}
