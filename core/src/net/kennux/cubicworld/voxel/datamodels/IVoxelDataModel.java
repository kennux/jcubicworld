package net.kennux.cubicworld.voxel.datamodels;

import net.kennux.cubicworld.serialization.BitReader;
import net.kennux.cubicworld.serialization.BitWriter;

/**
 * The voxel data model interface.
 * Your implementation of this datamodel must have a constructor without any params for reflection instantiation.
 * @author KennuX
 *
 */
public interface IVoxelDataModel
{
	public void serialize(BitWriter writer);
	public void deserialize(BitReader reader);
}
