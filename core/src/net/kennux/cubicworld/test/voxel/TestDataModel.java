package net.kennux.cubicworld.test.voxel;

import net.kennux.cubicworld.serialization.BitReader;
import net.kennux.cubicworld.serialization.BitWriter;
import net.kennux.cubicworld.voxel.datamodels.IVoxelDataModel;

public class TestDataModel implements IVoxelDataModel
{
	public int test;

	@Override
	public void serialize(BitWriter writer)
	{
		writer.writeInt(test);
	}

	@Override
	public void deserialize(BitReader reader)
	{
		this.test = reader.readInt();
	}

}
