package net.kennux.cubicworld.voxel;

/**
 * <pre>
 * A key implementation for use in a hashtable. Gets used in VoxelWorld for
 * managing chunks.
 * 
 * This is just a tuple implementation with 3 keys.
 * It implements compareTo() and equals().
 * 
 * hashCode() is not supported!
 * </pre>
 * 
 * @author KennuX
 *
 */
public class ChunkKey implements Comparable<ChunkKey>
{
	public int x, y, z;

	public ChunkKey(int chunkX, int chunkY, int chunkZ)
	{
		this.x = chunkX;
		this.y = chunkY;
		this.z = chunkZ;
	}

	@Override
	public int compareTo(ChunkKey o)
	{
		if (x != o.x)
			return Integer.compare(x, o.x);
		if (y != o.y)
			return Integer.compare(y, o.y);
		return Integer.compare(z, o.z);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof ChunkKey)
		{
			ChunkKey cObj = (ChunkKey) obj;
			return cObj.x == this.x && cObj.y == this.y && cObj.z == this.z;
		}

		return false;
	}

}
