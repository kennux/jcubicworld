package net.kennux.cubicworld.voxel;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;

/**
 * Chunk grid implementation.
 * This is basically the same as a hashmap only with a ChunkKey as key.
 * 
 * @author KennuX
 *
 */
public class ChunkManager
{
	/**
	 * The chunk keys
	 */
	private ChunkKey[] keys;

	/**
	 * The voxel chunks
	 */
	private VoxelChunk[] chunks;

	private Object lockObject = new Object();

	/**
	 * Initializes the chunkgrid with standard capacity (128).
	 */
	public ChunkManager()
	{
		this(128);
	}

	/**
	 * Chunk grid constructor. It initializes the grid with the given capacity
	 * (elements). Standard is 128.
	 * 
	 * @param gridCapacity
	 */
	public ChunkManager(int gridCapacity)
	{
		this.keys = new ChunkKey[gridCapacity];
		this.chunks = new VoxelChunk[gridCapacity];
	}

	/**
	 * Adds a chunk to the grid.
	 * 
	 * @param key
	 * @param chunk
	 */
	public void add(ChunkKey key, VoxelChunk chunk)
	{
		synchronized (this.lockObject)
		{
			int freeIndex = this.findFreeIndex();

			if (freeIndex == -1)
			{
				freeIndex = this.keys.length;
				this.extend(this.keys.length + 128); // Extend by 128
			}

			// Save values
			this.keys[freeIndex] = key;
			this.chunks[freeIndex] = chunk;
		}
	}

	/**
	 * Returns true if the ChunkGrid contains the given chunk key. Use this only
	 * if you just need to check if the key exists. If you want to get the chunk
	 * just call get() and check for null.
	 * 
	 * @param key
	 */
	public boolean containsKey(ChunkKey key)
	{
		synchronized (this.lockObject)
		{
			// Search index
			for (int i = 0; i < this.keys.length; i++)
			{
				if (this.keys[i] != null && this.keys[i].equals(key))
				{
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Extends this instance's capacity. newSize is the new capacity.
	 * 
	 * @param newSize
	 */
	private void extend(int newSize)
	{
		// Create new arrays
		VoxelChunk[] newChunkArray = new VoxelChunk[newSize];
		ChunkKey[] newKeyArray = new ChunkKey[newSize];

		// Copy old data
		System.arraycopy(this.keys, 0, newKeyArray, 0, this.keys.length);
		System.arraycopy(this.chunks, 0, newChunkArray, 0, this.chunks.length);

		// Set new arrays
		this.keys = newKeyArray;
		this.chunks = newChunkArray;
	}

	/**
	 * Searches in the keys array for a free index and return's it's index.
	 * REMEMBER: key and value array indices are the same.
	 * 
	 * Returns -1 if there was no free index found (extend should get called
	 * then to extend the arrays).
	 * 
	 * @return
	 */
	private int findFreeIndex()
	{
		for (int i = 0; i < this.keys.length; i++)
		{
			if (this.keys[i] == null)
				return i;
		}

		return -1;
	}

	/**
	 * Adds a chunk to the grid. Returns null if the key was not found.
	 * 
	 * @param key
	 * @param chunk
	 */
	public VoxelChunk get(ChunkKey key)
	{
		synchronized (this.lockObject)
		{
			// Search index
			for (int i = 0; i < this.keys.length; i++)
			{
				if (this.keys[i] != null && this.keys[i].equals(key))
				{
					return this.chunks[i];
				}
			}

			return null;
		}
	}

	/**
	 * Performs a bounds check for all loaded chunks. chunkPos is the position
	 * of the player (global chunkspace), radius the chunk loading radius.
	 * 
	 * @param middle
	 * @param radius
	 * @return
	 */
	public ChunkKey[] getChunksNotInside(Vector3 chunkPos, int radius)
	{
		synchronized (this.lockObject)
		{
			ArrayList<ChunkKey> chunksNotInside = new ArrayList<ChunkKey>();

			for (ChunkKey key : this.keys)
			{
				// Do radius check
				if (key != null)
					if (new Vector3(chunkPos).sub(new Vector3(key.x, chunkPos.y, key.z)).len() > radius)
						chunksNotInside.add(key);
			}

			// Return the chunks not inside this radius.
			return chunksNotInside.toArray(new ChunkKey[chunksNotInside.size()]);
		}
	}

	/**
	 * Performs a bounds check for all loaded chunks. chunkPositions are the
	 * positions of the players (global chunkspace), radius the chunk loading
	 * radius.
	 * 
	 * @param middle
	 * @param radius
	 * @return
	 */
	public ChunkKey[] getChunksNotInside(Vector3[] chunkPositions, int radius)
	{
		synchronized (this.lockObject)
		{
			ArrayList<ChunkKey> chunksNotInside = new ArrayList<ChunkKey>();

			for (ChunkKey key : this.keys)
			{
				boolean isInRadius = true;

				// Do radius check
				if (key != null)
					for (Vector3 chunkPos : chunkPositions)
						if (new Vector3(chunkPos).sub(new Vector3(key.x, chunkPos.y, key.z)).len() > radius)
							isInRadius = false;

				if (!isInRadius)
				{
					chunksNotInside.add(key);
				}
			}

			// Return the chunks not inside this radius.
			return chunksNotInside.toArray(new ChunkKey[chunksNotInside.size()]);
		}
	}

	/**
	 * Returns all keys in this instance (only copies of them).
	 * 
	 * @return
	 */
	public ChunkKey[] getKeys()
	{
		synchronized (this.lockObject)
		{
			ArrayList<ChunkKey> keys = new ArrayList<ChunkKey>();
			for (ChunkKey key : this.keys)
			{
				if (key != null)
					keys.add(key);
			}

			return keys.toArray(new ChunkKey[keys.size()]);
		}
	}

	/**
	 * Removes the given chunk key from the grid.
	 * 
	 * @param key
	 */
	public void remove(ChunkKey key)
	{
		synchronized (this.lockObject)
		{
			for (int i = 0; i < this.keys.length; i++)
			{
				if (this.keys[i] != null && this.keys[i].equals(key))
				{
					this.keys[i] = null;
					this.chunks[i].dispose();
					this.chunks[i] = null;
				}
			}
		}
	}

	/**
	 * Calls the render method on all chunk objects.
	 * 
	 * @param cam
	 * @param shader
	 */
	public void render(Camera cam, ShaderProgram shader)
	{
		synchronized (this.lockObject)
		{
			for (VoxelChunk c : this.chunks)
			{
				if (c != null)
					c.render(cam, shader);
			}
		}
	}

	/**
	 * Calls the renderModels method on all chunk objects.
	 * 
	 * @param cam
	 * @param modelBatch
	 */
	public void renderModels(Camera cam, ModelBatch modelBatch)
	{
		synchronized (this.lockObject)
		{
			for (VoxelChunk c : this.chunks)
			{
				if (c != null)
					c.renderModels(cam, modelBatch);
			}
		}
	}

	/**
	 * Simulates all chunks in the grid.
	 */
	public void simulate()
	{
		synchronized (this.lockObject)
		{
			for (VoxelChunk c : this.chunks)
			{
				if (c != null)
					c.simulate();
			}
		}
	}

	/**
	 * Calls the update method on all chunk objects.
	 */
	public void update()
	{
		synchronized (this.lockObject)
		{
			for (VoxelChunk c : this.chunks)
			{
				if (c != null)
					c.update();
			}
		}
	}

	/**
	 * Returns true if all chunks are ready for rendering.
	 */
	public boolean allChunksReady()
	{
		boolean allReady = true;

		synchronized (this.lockObject)
		{
			for (VoxelChunk c : this.chunks)
			{
				if (c != null && !c.isReadyForRendering())
					allReady = false;
			}
		}
		
		return allReady;
	}
}
