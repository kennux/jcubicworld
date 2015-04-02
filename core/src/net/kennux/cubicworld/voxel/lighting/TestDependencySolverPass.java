package net.kennux.cubicworld.voxel.lighting;

import java.util.ArrayList;

import net.kennux.cubicworld.math.Vector3i;
import net.kennux.cubicworld.voxel.VoxelChunk;
import net.kennux.cubicworld.voxel.VoxelData;

/**
 * The sunlight pass.
 * 
 * @author KennuX
 *
 */
public class TestDependencySolverPass implements ILightingPass
{
	/**
	 * The lightin system master.
	 */
	private ALightingSystem lightingSystem;

	public TestDependencySolverPass(ALightingSystem lightingSystem)
	{
		this.lightingSystem = lightingSystem;
	}

	@Override
	public boolean executePass(VoxelChunk chunk)
	{
		// Get the voxels waiting for lighting
		TestSunlightLightingPass sunlightPass = this.lightingSystem.getLightingPass(0);
		ArrayList<Vector3i> waitList = sunlightPass.getWaitList();
		
		// Check if all adjacent chunks are done with sunlight propagation
		/*VoxelChunk[] adjacentChunks = new VoxelChunk[]
		{
			chunk.master.getChunk(chunk.getChunkX()-1, chunk.getChunkY(), chunk.getChunkZ(), false),
			chunk.master.getChunk(chunk.getChunkX()+1, chunk.getChunkY(), chunk.getChunkZ(), false),
			chunk.master.getChunk(chunk.getChunkX(), chunk.getChunkY()-1, chunk.getChunkZ(), false),
			chunk.master.getChunk(chunk.getChunkX(), chunk.getChunkY()+1, chunk.getChunkZ(), false),
			chunk.master.getChunk(chunk.getChunkX(), chunk.getChunkY(), chunk.getChunkZ()-1, false),
			chunk.master.getChunk(chunk.getChunkX(), chunk.getChunkY(), chunk.getChunkZ()+1, false),
		};
		
		// Test the adjacent chunks
		for (VoxelChunk c : adjacentChunks)
		{
			if (c == null || !c.isLightingPassDone(TestSunlightLightingPass.class))
			{
				// We're not ready to start dependencies yet.
				return false;
			}
		}*/
		
		// All adjacent chunks are ready, let's start!
		ArrayList<Vector3i> processedVoxels = new ArrayList<Vector3i>();
		
		while (waitList.size() > 0)
		{
			for (Vector3i localVoxelPos : waitList)
			{
				// Get the current voxel information
				VoxelData voxelData = chunk.getVoxel(localVoxelPos.x, localVoxelPos.y, localVoxelPos.z);
				
				// Get all adjacent voxels
				Vector3i[] adjacentVoxels = new Vector3i[]
				{
						new Vector3i(localVoxelPos.x+1,localVoxelPos.y,localVoxelPos.z),
						new Vector3i(localVoxelPos.x-1,localVoxelPos.y,localVoxelPos.z),
						new Vector3i(localVoxelPos.x,localVoxelPos.y+1,localVoxelPos.z),
						new Vector3i(localVoxelPos.x,localVoxelPos.y-1,localVoxelPos.z),
						new Vector3i(localVoxelPos.x,localVoxelPos.y,localVoxelPos.z+1),
						new Vector3i(localVoxelPos.x,localVoxelPos.y,localVoxelPos.z-1),
				};
				
				// Get highest light level
				byte highestLightLevel = -1;
				byte currentLightLevel = 0;
				for (Vector3i voxelPos : adjacentVoxels)
				{
					VoxelData v = chunk.getVoxel(voxelPos.x, voxelPos.y, voxelPos.z);
					
					// Only air or transparent blocks will get used for highest light level determination
					if (v != null && (v.voxelType == null || v.voxelType.transparent))
					{
						// Level comparison
						currentLightLevel = v.getLightLevel();
						if (currentLightLevel > highestLightLevel)
						{
							highestLightLevel = currentLightLevel;
						}
					}
				}
				
				if (highestLightLevel != -1)
				{
					// We found the light level!
					voxelData.setBlockLightLevel(highestLightLevel-1);
					processedVoxels.add(localVoxelPos);
				}
				else
				{
					System.out.println("Not found highest light level for voxel: " + localVoxelPos);
				}
			}
			
			// Remove all processed voxels from waiting list
			for (Vector3i processed : processedVoxels)
			{
				waitList.remove(processed);
			}
			
			processedVoxels.clear();
		}
		
		return true;
	}

}
