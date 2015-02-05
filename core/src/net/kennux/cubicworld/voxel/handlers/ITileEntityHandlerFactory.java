package net.kennux.cubicworld.voxel.handlers;

/**
 * Tile entity handler factories can get set to voxel types for creating tile entity handlers for the final voxel data object.
 * @author KennuX
 *
 */
public interface ITileEntityHandlerFactory
{
	public IVoxelTileEntityHandler newInstance();
}
