package net.kennux.cubicworld.voxel.lighting;

import java.util.ArrayList;

import net.kennux.cubicworld.math.Vector3i;
import net.kennux.cubicworld.voxel.VoxelChunk;
import net.kennux.cubicworld.voxel.VoxelData;
import net.kennux.cubicworld.voxel.VoxelWorld;

public class GlobalLightingPass implements ILightingPass
{

	/**
	 * Temporary array list which will contain all voxels which depend on another voxel for lighting.
	 * Gets used in the global lighting pass to prevent searching ready voxels forever.
	 * 
	 * @see VoxelChunk#recalculateGlobalLighting()
	 */
	private ArrayList<Vector3i> dependencyVoxelsTemporary;

	@Override
	public boolean executePass(VoxelChunk chunk)
	{
		if (this.dependencyVoxelsTemporary == null)
			this.dependencyVoxelsTemporary = new ArrayList<Vector3i>();
		
		// Needed variables
		VoxelData[][][] voxelData = chunk.getVoxelData();
		VoxelData v = null;
		boolean blocksLeft = false;
		// Vector3i absolutePos = new Vector3i();

		// Shadow pass
		// This will flood light into caves
		for (int x = 0; x < VoxelWorld.chunkWidth; x++)
			for (int z = 0; z < VoxelWorld.chunkDepth; z++)
				for (int y = VoxelWorld.chunkHeight - 1; y >= 0; y--)
				{
					v = voxelData[x][y][z];
					if (v == null)
						continue;

					// This function will only iterate over air or transparent blocks which are uninitialized.
					if (v.getBlockLightLevel() == -1 && (v.voxelType == null || v.voxelType.transparent))
					{
						Vector3i absolutePos = chunk.getAbsoluteVoxelPosition(x, y, z);

						// Get all adjacent voxels
						VoxelData[] adjacentVoxels = new VoxelData[] {
								// Top Voxel
								(y == VoxelWorld.chunkHeight - 1) ? chunk.master.getVoxel(absolutePos.x, absolutePos.y + 1, absolutePos.z) : voxelData[x][y + 1][z],
								// Bottom Voxel
								(y == 0) ? chunk.master.getVoxel(absolutePos.x, absolutePos.y - 1, absolutePos.z) : voxelData[x][y - 1][z],
								// Left Voxel
								(x == 0) ? chunk.master.getVoxel(absolutePos.x - 1, absolutePos.y, absolutePos.z) : voxelData[x - 1][y][z],
								// Right Voxel
								(x == VoxelWorld.chunkWidth - 1) ? chunk.master.getVoxel(absolutePos.x + 1, absolutePos.y, absolutePos.z) : voxelData[x + 1][y][z],
								// Back Voxel
								(z == 0) ? chunk.master.getVoxel(absolutePos.x, absolutePos.y, absolutePos.z - 1) : voxelData[x][y][z - 1],
								// Front Voxel
								(z == VoxelWorld.chunkDepth - 1) ? chunk.master.getVoxel(absolutePos.x, absolutePos.y, absolutePos.z + 1) : voxelData[x][y][z + 1] };

						// Variable for the highest light level on the adjacent blocks
						byte highestLightLevel = -1;

						// Iterate through all adjacent voxels
						boolean onlyDependingVoxelsReady = true;

						for (VoxelData vd : adjacentVoxels)
						{
							// Determin whether the current block is a translucent block or not.
							boolean translucentVoxel = vd != null && (vd.voxelType == null || vd.voxelType.transparent);

							if (onlyDependingVoxelsReady && translucentVoxel && !this.dependencyVoxelsTemporary.contains(absolutePos))
							{
								onlyDependingVoxelsReady = false;
							}

							// Only translucent voxels AND
							// SunLightLevel is > 0 (means initialized and not shadow area) OR block light level == -1 (means uninitialized)
							if (translucentVoxel && (vd.getSunLightLevel() > 0 || vd.getBlockLightLevel() != -1))
							{
								byte lightLevel = vd.getLightLevel();
								if (lightLevel > highestLightLevel)
									highestLightLevel = lightLevel;
							}
						}

						// Dependency check
						if (onlyDependingVoxelsReady)
						{
							highestLightLevel = 0;
						}

						// If no blocks were ready...
						if (highestLightLevel <= -1 && !this.dependencyVoxelsTemporary.contains(absolutePos))
						{
							this.dependencyVoxelsTemporary.add(absolutePos);
							System.out.println("FAIL!!! " + absolutePos + " " + (highestLightLevel <= -1 && !this.dependencyVoxelsTemporary.contains(absolutePos)));
							// ... we're done here!
							blocksLeft = true;
							continue;
						}

						// If there were blocks ready
						byte lightLevel = (byte) (highestLightLevel - 1);

						if (lightLevel < 0)
							lightLevel = 0;

						v.setBlockLightLevel(lightLevel);

						// Remove block from the dependency list if it is in there
						this.dependencyVoxelsTemporary.remove(absolutePos);
					}
				}

		// Only if there were no blocks left, we are done calculating the lighting
		// If not, we will go on with the lighting in the next update() call.
		if (!blocksLeft)
		{
			this.dependencyVoxelsTemporary.clear();
			return true;
		}
		
		return false;
	}

}
