package net.kennux.cubicworld.voxel.lighting;

import net.kennux.cubicworld.math.Vector3i;
import net.kennux.cubicworld.networking.packet.ClientChunkRequest;
import net.kennux.cubicworld.voxel.VoxelChunk;
import net.kennux.cubicworld.voxel.VoxelData;
import net.kennux.cubicworld.voxel.VoxelWorld;

/**
 * The sunlight pass.
 * @author KennuX
 *
 */
public class LocalLightingPass implements ILightingPass
{
	/**
	 * The lightin system master.
	 */
	private ALightingSystem lightingSystem;
	
	public LocalLightingPass(ALightingSystem lightingSystem)
	{
		this.lightingSystem = lightingSystem;
	}
	
	@Override
	public boolean executePass(VoxelChunk chunk)
	{
		// Check if pass above this is already calculated or this is the top chunk of the world.
		if (!ClientChunkRequest.areRequestsPending() && chunk.isInitialized() && chunk.getChunkY() != chunk.master.chunksOnYAxis())
		{
			VoxelChunk topChunk = chunk.master.getChunk(chunk.getChunkX(), chunk.getChunkY()+1, chunk.getChunkZ(), false);
			
			if (topChunk == null || !topChunk.isLightingPassDone(LocalLightingPass.class))
				return false;
		}
		
		VoxelData[][][] voxelData = chunk.getVoxelData();
		
		// Check if top chunk is ready
		
		// Needed variables
		VoxelData v = null;
		Vector3i absolutePos = new Vector3i(0, 0, 0);

		// Clear pass
		// This clears the shadow and sun light level.
		for (int x = 0; x < VoxelWorld.chunkWidth; x++)
			for (int z = 0; z < VoxelWorld.chunkDepth; z++)
				for (int y = VoxelWorld.chunkHeight - 1; y >= 0; y--)
				{
					v = voxelData[x][y][z];
					if (v == null)
						continue;

					v.setSunLightLevel(-1);

					// The blocklight level will get initialized with 0
					// Because -1 will mean that there is a need to calculate a block light level.
					// Only blocks occupied with shadows or in range of a light source will need an actual block light level.
					v.setBlockLightLevel(0);
				}

		// Sunlight propagation pass
		// This casts rays from the top of the world on every x|y coordinate pair
		// After a block was hit by the ray, the sunlight level will get set to -1.
		// Solid blocks will recieve the sunlight level 0.
		for (int x = 0; x < VoxelWorld.chunkWidth; x++)
			for (int z = 0; z < VoxelWorld.chunkDepth; z++)
				for (int y = VoxelWorld.chunkHeight - 1; y >= 0; y--)
				{
					// Calculate the absolute position of the current voxel
					chunk.getAbsoluteVoxelPosition(x, y, z, absolutePos);
					v = voxelData[x][y][z];
					if (v == null)
						continue;

					// If the current voxel is the most at the upper border of the world bounding
					// Set the sunlight level to it for propagating it down.
					if (absolutePos.y == chunk.master.worldHeight - 1)
					{
						v.setSunLightLevel(chunk.master.getSunLightLevel());
					}
					// Upper chunk border propagation
					else if (y == VoxelWorld.chunkHeight - 1)
					{
						// Porpagate light downwards
						// On the upper chunk border the sun light level will be topLightLevel - 1 but minimum 0
						VoxelData topVoxel = chunk.master.getVoxel(absolutePos.x, absolutePos.y + 1, absolutePos.z);
						byte topLightLevel = (byte) (topVoxel == null ? chunk.master.getSunLightLevel() - ((chunk.master.chunksOnYAxis() - 2) - chunk.getChunkY()) : topVoxel.getSunLightLevel());

						byte lightLevel = (byte) (topLightLevel - 1);

						if (lightLevel < 0)
							lightLevel = 0;

						v.setSunLightLevel(lightLevel);
					}
					// Solid blocks
					else if (v.voxelType != null && !v.voxelType.transparent)
					{
						// Solid blocks will stop light porpagation
						// They also cant have a shadow level so will get initialized with 0
						v.setSunLightLevel(0);
					}
					// Air / transparent blocks
					else if (v.voxelType == null || v.voxelType.transparent)
					{
						// Air and transparent blocks will get their light from the upper voxel
						VoxelData topVoxel = chunk.master.getVoxel(absolutePos.x, absolutePos.y + 1, absolutePos.z);
						byte chunkLightLevel = (byte) (chunk.master.getSunLightLevel() - (chunk.master.chunksOnYAxis() - chunk.getChunkY()));

						// Set the sunlight level
						v.setSunLightLevel(topVoxel.getSunLightLevel());

						// If this voxel is in the shadow of another voxel (i.e. not directly facing to the sun)
						// It's block light level will get marked for calculation
						// The global lighting pass then will calculate the final block light level for this voxel.
						if (v.getSunLightLevel() < chunkLightLevel)
						{
							v.setBlockLightLevel(-1);
						}
					}
				}
		
		return true;
	}

}
