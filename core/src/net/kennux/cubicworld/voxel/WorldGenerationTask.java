package net.kennux.cubicworld.voxel;

/**
 * Dataholder which represents a world generation task.
 * 
 * @author KennuX
 *
 */
public class WorldGenerationTask
{
	public int chunkX;
	public int chunkY;
	public int chunkZ;
	public VoxelChunk chunk;
	public AWorldGenerator generatorInstance;

	public WorldGenerationTask(int chunkX, int chunkY, int chunkZ, VoxelChunk chunk, AWorldGenerator generatorInstance)
	{
		this.chunkX = chunkX;
		this.chunkY = chunkY;
		this.chunkZ = chunkZ;
		this.chunk = chunk;
		this.generatorInstance = generatorInstance;
	}

	/**
	 * Executes the task in this instance.
	 */
	public void executeTask()
	{
		// System.out.println("Generating chunk (XYZ): " + chunkX + " " + chunkY
		// + " " + chunkZ);

		// Check if the chunk exists on hdd
		if (chunk.master.hasWorldFile() && chunk.master.getWorldFile().hasChunk(this.chunkX, this.chunkY, this.chunkZ))
		{
			// Load from hdd
			chunk.loadVoxelData();
		}
		else
		{
			this.generatorInstance.GenerateWorld(chunkX, chunkY, chunkZ, chunk);
		}
	}
}
