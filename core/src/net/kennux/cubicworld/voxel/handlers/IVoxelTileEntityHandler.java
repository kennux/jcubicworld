package net.kennux.cubicworld.voxel.handlers;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelBatch;

import net.kennux.cubicworld.serialization.BitReader;
import net.kennux.cubicworld.serialization.BitWriter;
import net.kennux.cubicworld.voxel.VoxelData;

/**
 * <pre>
 * This interface gets used to hold voxel tile entity handlers.
 * Tile entity handlers can get used to recieve updates or other events and to store additional block data.
 * 
 * Tile entities will get instantiated by an implementation of ITileEntityHandlerFactory.
 * </pre>
 * 
 * @author KennuX
 *
 */
public interface IVoxelTileEntityHandler extends Cloneable
{
	/**
	 * x y and z are coordinates in absolute voxelspace.
	 * 
	 * @param voxelData
	 * @param x
	 * @param y
	 * @param z
	 * @param isServer
	 */
	public void handleUpdate(VoxelData voxelData, int x, int y, int z, boolean isServer);

	/**
	 * Gets called if the block action (clientside handler, on clicking it / opening
	 * it's gui) get called.
	 * 
	 * @param voxelData
	 * @param x
	 * @param y
	 * @param z
	 */
	public void handleAction(VoxelData voxelData, int x, int y, int z);

	/**
	 * Gets called if the tile entity should get rendered.
	 * 
	 * @param voxelData
	 * @param x
	 *            X-Position of the voxel in absolute voxelspace.
	 * @param y
	 *            Y-Position of the voxel in absolute voxelspace.
	 * @param z
	 *            Z-Position of the voxel in absolute voxelspace.
	 */
	public void handleRender(Camera camera, VoxelData voxelData, int x, int y, int z);

	public void serialize(BitWriter writer);

	public void deserialize(BitReader reader);
}
