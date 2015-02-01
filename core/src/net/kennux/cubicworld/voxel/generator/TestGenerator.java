package net.kennux.cubicworld.voxel.generator;

import net.kennux.cubicworld.voxel.AWorldGenerator;
import net.kennux.cubicworld.voxel.VoxelChunk;
import net.kennux.cubicworld.voxel.VoxelData;
import net.kennux.cubicworld.voxel.VoxelWorld;

public class TestGenerator extends AWorldGenerator
{
	@Override
	public void GenerateWorld(int chunkX, int chunkY, int chunkZ, VoxelChunk chunk)
	{
		VoxelData[][][] voxelData = new VoxelData[VoxelWorld.chunkWidth][VoxelWorld.chunkHeight][VoxelWorld.chunkDepth];

		for (int x = 0; x < VoxelWorld.chunkWidth; x++)
		{
			for (int y = 0; y < VoxelWorld.chunkHeight; y++)
			{
				for (int z = 0; z < VoxelWorld.chunkDepth; z++)
				{
					// int absoluteX = x + (chunkX * VoxelWorld.chunkWidth);
					int absoluteY = y + (chunkY * VoxelWorld.chunkHeight);
					// int absoluteZ = z + (chunkZ * VoxelWorld.chunkDepth);

					if (absoluteY < 100)
					{
						voxelData[x][y][z] = VoxelData.construct((short) 0);
					}
					else
					{
						voxelData[x][y][z] = null;
					}
				}
			}
		}

		chunk.setVoxelData(voxelData);
	}

}
