package net.kennux.cubicworld.voxel;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
	 * This hashmap contains all chunks.
	 */
	private ConcurrentHashMap<ChunkKey, VoxelChunk> chunks = new ConcurrentHashMap<ChunkKey, VoxelChunk>();

	/**
	 * Adds a chunk to the grid.
	 * 
	 * @param key
	 * @param chunk
	 */
	public void put(ChunkKey key, VoxelChunk chunk)
	{
		this.chunks.put(key, chunk);
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
		return this.chunks.containsKey(key);
	}

	/**
	 * Adds a chunk to the grid. Returns null if the key was not found.
	 * 
	 * @param key
	 * @param chunk
	 */
	public VoxelChunk get(ChunkKey key)
	{
		return this.chunks.get(key);
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
		ArrayList<ChunkKey> chunksNotInside = new ArrayList<ChunkKey>();

		for (ChunkKey key : this.chunks.keySet())
		{
			// Do radius check
			if (key != null)
				if (new Vector3(chunkPos).sub(new Vector3(key.x, chunkPos.y, key.z)).len() > radius)
					chunksNotInside.add(key);
		}

		// Return the chunks not inside this radius.
		return chunksNotInside.toArray(new ChunkKey[chunksNotInside.size()]);
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
		ArrayList<ChunkKey> chunksNotInside = new ArrayList<ChunkKey>();

		for (ChunkKey key : this.chunks.keySet())
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

	/**
	 * Returns all keys in this instance (only copies of them).
	 * 
	 * @return
	 */
	public ChunkKey[] getKeys()
	{
		Set<ChunkKey> keySet = this.chunks.keySet();
		return keySet.toArray(new ChunkKey[keySet.size()]);
	}

	/**
	 * Removes the given chunk key from the grid.
	 * 
	 * @param key
	 */
	public void remove(ChunkKey key)
	{
		VoxelChunk chunk = this.chunks.get(key);
		this.chunks.remove(key);

		if (chunk != null)
			chunk.dispose();
	}

	/**
	 * Calls the render and renderModels method on all chunk objects.
	 * 
	 * @param cam
	 * @param shader
	 */
	public void render(Camera cam, ShaderProgram shader)
	{
		// Start world rendering pass (only chunk meshes)
		shader.begin();

		shader.setUniformMatrix("m_cameraProj", cam.combined);

		VoxelEngine.textureAtlas.atlasTexture.bind(0);
		shader.setUniformi("r_textureAtlas", 0);
		
		// Render all chunks
		for (VoxelChunk c : this.chunks.values())
		{
			if (c != null)
				c.render(cam, shader);
		}
		
		shader.end();
		
		// Tile entity pass
		for (VoxelChunk c : this.chunks.values())
		{
			if (c != null)
				c.renderTileEntities(cam);
		}
	}

	/**
	 * Calls the update and simulate method on all chunk objects.
	 */
	public void update()
	{
		for (VoxelChunk c : this.chunks.values())
		{
			if (c != null)
			{
				c.simulate();
				c.update();
			}
		}
	}
	
	/**
	 * Gets called if the skylight value changes, regenerates the lighting and meshes for all chunks
	 */
	public void recalculateLightingAndMeshes()
	{
		for (VoxelChunk c : this.chunks.values())
		{
			if (c != null)
			{
				c.regenerateLightingAndMesh();
			}
		}
	}

	/**
	 * Returns true if all chunks are ready for rendering.
	 */
	public boolean allChunksReady()
	{
		boolean allReady = true;

		for (VoxelChunk c : this.chunks.values())
		{
			if (c != null && !c.isReadyForRendering())
				allReady = false;
		}

		return allReady;
	}
}
