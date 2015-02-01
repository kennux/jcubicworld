package net.kennux.cubicworld.voxel;

import java.util.ArrayList;

import net.kennux.cubicworld.CubicWorldServer;
import net.kennux.cubicworld.pathfinder.Path;
import net.kennux.cubicworld.pathfinder.Pathfinder;
import net.kennux.cubicworld.util.ConsoleHelper;
import net.kennux.cubicworld.util.Mathf;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

/**
 * The voxel world class. It handles chunk generation and rendering.
 * 
 * @author KennuX
 *
 */
public class VoxelWorld
{
	/**
	 * The chunk width used to control how much blocks can get placed in one
	 * chunk on the x-axis.
	 */
	public static final int chunkWidth = 16;

	/**
	 * The chunk height used to control how much blocks can get placed in one
	 * chunk on the y-axis.
	 */
	public static final int chunkHeight = 16;

	/**
	 * The chunk depth used to control how much blocks can get placed in one
	 * chunk on the z-axis.
	 */
	public static final int chunkDepth = 16;

	/**
	 * The world height controls how much chunks will get stacked on the y-axis.
	 */
	public int worldHeight = 128;

	/**
	 * The chunks hashmap used to manage the currently loaded chunks.
	 */
	private ChunkManager chunks;

	/**
	 * The chunks list lock object.
	 */
	private Object chunksLockObject = new Object();

	/**
	 * The world generator instace.
	 */
	private AWorldGenerator worldGenerator;

	/**
	 * The world generator thread pool for this voxel world instance.
	 */
	private WorldGeneratorThreadPool worldGeneratorThreadPool;

	/**
	 * Specifies the shader program used for chunk rendering. Shader attributes:
	 * v_Position -> vertex position (float3) v_Uv -> Uv-Coordinates (float2)
	 * v_Normal -> Normal (float3) v_Light -> Light color (float4)
	 * 
	 * Shader uniforms: m_cameraProj -> Camera projection matrix (mat4)
	 * r_textureAtlas -> texture atlas texture
	 */
	private ShaderProgram worldShader;

	/**
	 * Handles world updates.
	 */
	private Thread worldUpdateThread;

	/**
	 * The master server instance.
	 */
	private CubicWorldServer master;

	/**
	 * If this is set to true the rendering modules wont get executed.
	 */
	private boolean isServer = false;

	private VoxelWorldSave voxelWorldSave;

	/**
	 * The pathfinder instance used to perform path finding actions.
	 */
	private Pathfinder pathfinderInstance;

	/**
	 * Gets used to render model blocks in the chunks.
	 */
	private ModelBatch chunkModelBatch;

	/**
	 * The camer used for model rendering.
	 */
	private Camera camera;

	// Event handlers
	private IVoxelDataUpdateHandler voxelDataUpdateHandler;

	/**
	 * Initializes the voxel world without any graphical support.
	 * 
	 * @param isServer
	 */
	public VoxelWorld(CubicWorldServer master)
	{
		this.isServer = true;
		this.master = master;

		this.chunks = new ChunkManager();
		this.worldGeneratorThreadPool = new WorldGeneratorThreadPool(Runtime.getRuntime().availableProcessors());
	}

	public VoxelWorld(ShaderProgram shader, Camera camera)
	{
		this.chunks = new ChunkManager();

		// Set references
		this.camera = camera;
		this.chunkModelBatch = new ModelBatch();
		this.worldShader = shader;

		this.initUpdateThread();
	}

	public VoxelWorld(ShaderProgram shader, Camera camera, int worldHeight)
	{
		this(shader, camera);

		this.worldHeight = worldHeight;
	}

	/**
	 * Returns the count of chunks stacked on the y-axis.
	 * Counting begins at 0!
	 * 
	 * @return
	 */
	public int chunksOnYAxis()
	{
		return (this.worldHeight / chunkHeight) - 1;
	}

	/**
	 * Performs a cleanup, iterates through every chunkobject and checks if it
	 * is inside of the loading range.
	 * 
	 * @param playerPosition
	 * @param chunkRadius
	 */
	public void cleanup(Vector3 playerPosition, int chunkRadius)
	{
		synchronized (this.chunksLockObject)
		{
			// Get chunk keys to identify chunks we don't need any longer.
			Vector3 chunkPos = this.getChunkspacePosition(playerPosition);
			ChunkKey[] chunksToDelete = this.chunks.getChunksNotInside(chunkPos, chunkRadius);

			for (ChunkKey chunkToDelete : chunksToDelete)
			{
				if (this.isChunkInitialized(chunkToDelete.x, chunkToDelete.y, chunkToDelete.z))
				{
					this.chunks.remove(chunkToDelete);
				}
			}
		}
	}

	/**
	 * Performs a cleanup, iterates through every chunkobject and checks if it
	 * is inside of the loading range.
	 * 
	 * @param playerPosition
	 * @param chunkRadius
	 */
	public void cleanup(Vector3[] playerPositions, int chunkRadius)
	{
		synchronized (this.chunksLockObject)
		{
			// Get chunk keys to identify chunks we don't need any longer.
			Vector3[] chunkPositions = new Vector3[playerPositions.length];
			for (int i = 0; i < chunkPositions.length; i++)
			{
				chunkPositions[i] = this.getChunkspacePosition(playerPositions[i]);
			}

			ChunkKey[] chunksToDelete = this.chunks.getChunksNotInside(chunkPositions, chunkRadius);

			for (ChunkKey chunkToDelete : chunksToDelete)
			{
				if (this.isChunkInitialized(chunkToDelete.x, chunkToDelete.y, chunkToDelete.z))
				{
					this.chunks.remove(chunkToDelete);
				}
			}
		}
	}

	/**
	 * <pre>
	 * Gets all voxel bounding boxes which could possibly hit the bounding box.
	 * Then it perfoms an intersection check on b for all bounding boxes.
	 * This function returns a voxel collision object.
	 * It returns null if there was no collision.
	 * 
	 * If there is no chunk at the player's position (or under him if he is beyond the world height), a voxel collision with all collision axes set to true.
	 * 
	 * IMPORTANT: If a non-ready chunk is needed for calculation, this will return true!
	 * </pre>
	 * 
	 * @param b
	 * @return
	 */
	public VoxelCollision collisionCheck(BoundingBox boundingBox)
	{
		// TODO Check if we really need all that voxels
		int width = Mathf.ceilToInt(boundingBox.max.x - boundingBox.min.x) + 1;
		int height = Mathf.ceilToInt(boundingBox.max.y - boundingBox.min.y) + 1;
		int depth = Mathf.ceilToInt(boundingBox.max.z - boundingBox.min.z) + 1;

		Vector3 voxelspaceCenter = this.getVoxelspacePosition(new Vector3(boundingBox.min));
		@SuppressWarnings("deprecation")
		Vector3 boundingBoxCenter = boundingBox.getCenter();

		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				for (int z = 0; z < depth; z++)
				{
					// Check if chunk is loaded
					Vector3 chunkPos = this.getChunkspacePosition(new Vector3((int) voxelspaceCenter.x + x, (int) voxelspaceCenter.y + y, (int) voxelspaceCenter.z + z));
					if (chunkPos.y < (this.worldHeight / VoxelWorld.chunkHeight) && !this.isChunkInitialized((int) chunkPos.x, (int) chunkPos.y, (int) chunkPos.z))
					{
						return new VoxelCollision(boundingBoxCenter, boundingBoxCenter, true, true, true);
					}

					// Get bounding box
					BoundingBox voxelBoundingBox = this.getBoundingBox((int) voxelspaceCenter.x + x, (int) voxelspaceCenter.y + y, (int) voxelspaceCenter.z + z);

					// Check for intersection
					if (voxelBoundingBox != null && boundingBox.intersects(voxelBoundingBox))
					{
						@SuppressWarnings("deprecation")
						Vector3 colliderCenter = voxelBoundingBox.getCenter();
						return new VoxelCollision(boundingBoxCenter, colliderCenter);
					}
				}
			}
		}

		return null;
	}

	/**
	 * Transforms the given positions from worldspace into global voxelspace.
	 * It returns the Path instance sent to the pathfinder to process.
	 * 
	 * @param worldspaceStart
	 * @param worldspaceEnd
	 */
	public Path findPath(Vector3 worldspaceStart, Vector3 worldspaceEnd, boolean needsGround, int characterHeight)
	{
		this.initializePathfinder();
		Path p = new Path(this.getVoxelspacePosition(worldspaceStart), this.getVoxelspacePosition(worldspaceEnd), needsGround, characterHeight);

		this.pathfinderInstance.addPathfinderTask(p);
		return p;
	}

	/**
	 * Flushs all enquened save operations in the voxel world save.
	 * This function wont do anything if there is no voxel world save object attached to this instance.
	 * 
	 * @see VoxelWorldSave#flushSave()
	 */
	public void flushSave()
	{
		if (this.voxelWorldSave != null)
			this.voxelWorldSave.flushSave();
	}

	/**
	 * Generates the chunk at the given chunk position.
	 * 
	 * @param chunkX
	 * @param chunkY
	 * @param chunkZ
	 */
	public void generateChunk(int chunkX, int chunkY, int chunkZ, boolean immediately)
	{
		synchronized (this.chunksLockObject)
		{
			// Instantiate a voxelchunk if non exists
			VoxelChunk chunk = this.getChunk(chunkX, chunkY, chunkZ, true);
			// Enquene for generation
			WorldGenerationTask task = new WorldGenerationTask(chunkX, chunkY, chunkZ, chunk, this.worldGenerator);

			if (immediately)
				task.executeTask();
			else
				this.worldGeneratorThreadPool.EnqueGenerationJob(task);
		}
	}

	/**
	 * Generates chunks around the given position in the given radius.
	 * 
	 * @param position
	 * @param chunkRadius
	 */
	public void generateChunksAround(Vector3 position, int chunkRadius, boolean immediately)
	{
		synchronized (this.chunksLockObject)
		{
			Vector3 chunkPos = this.getChunkspacePosition(position);

			// Make sure all chunks are getting generated or already
			// initialized.
			for (int x = (int) (chunkPos.x - chunkRadius); x <= chunkPos.x + chunkRadius; x++)
				for (int z = (int) (chunkPos.z - chunkRadius); z <= chunkPos.z + chunkRadius; z++)
					for (int y = 0; y < this.worldHeight / VoxelWorld.chunkHeight; y++)
						if (!this.chunks.containsKey(new ChunkKey(x, y, z)) && y <= this.worldHeight / VoxelWorld.chunkHeight && new Vector3(chunkPos).sub(new Vector3(x, chunkPos.y, z)).len() <= chunkRadius)
							this.generateChunk(x, y, z, false);
		}

		// Wait for all threads ready
		this.worldGeneratorThreadPool.waitForGenerationFinished();
	}

	/**
	 * Returns the bounding box for a voxel given in global voxelspace.
	 * If there is no voxel at this position, null will be returned.
	 * If the voxel is a non-collidable (like vegetation), also null is
	 * returned.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public BoundingBox getBoundingBox(int x, int y, int z)
	{
		// Calculate chunk position for calculating relative position
		Vector3 chunk = this.getChunkspacePosition(new Vector3(x, y, z));

		// Calculate relative position
		x -= (int) (chunk.x * VoxelWorld.chunkWidth);
		y -= (int) (chunk.y * VoxelWorld.chunkHeight);
		z -= (int) (chunk.z * VoxelWorld.chunkDepth);
		ChunkKey chunkKey = new ChunkKey((int) chunk.x, (int) chunk.y, (int) chunk.z);

		BoundingBox boundingBox = null;

		synchronized (this.chunksLockObject)
		{
			if (!this.chunks.containsKey(chunkKey))
			{
				return null;
			}

			// Offset the bounding box
			boundingBox = this.chunks.get(chunkKey).getBoundingBox(x, y, z);
		}

		if (boundingBox == null)
			return null;
		else
		{
			int xOffset = (int) (VoxelWorld.chunkWidth * chunk.x);
			int yOffset = (int) (VoxelWorld.chunkHeight * chunk.y);
			int zOffset = (int) (VoxelWorld.chunkDepth * chunk.z);
			boundingBox.min.add(xOffset, yOffset, zOffset);
			boundingBox.max.add(xOffset, yOffset, zOffset);

			boundingBox.set(boundingBox);

			return boundingBox;
		}
	}

	/**
	 * Returns the chunk at the given chunkspace position. If the chunk doesnt
	 * exists it will get created if the createChunk flag is set to true,
	 * otherwise null is returned.
	 * 
	 * @param chunkX
	 * @param chunkY
	 * @param chunkZ
	 */
	public VoxelChunk getChunk(int chunkX, int chunkY, int chunkZ, boolean createChunk)
	{
		synchronized (this.chunksLockObject)
		{
			VoxelChunk chunk = this.chunks.get(new ChunkKey(chunkX, chunkY, chunkZ));

			if (chunk == null)
			{
				if (createChunk)
				{
					// Create chunk
					return this.instantiateChunk(chunkX, chunkY, chunkZ);
				}

				// Nothing to return here, not found and i should not create it.
				return null;
			}

			// We found it! :-)
			return chunk;
		}
	}

	/**
	 * Calculates the chunk where this worldspace position is located on.
	 * 
	 * @param worldspace
	 * @return
	 */
	public Vector3 getChunkspacePosition(Vector3 worldspace)
	{
		return new Vector3(Mathf.floorToInt((worldspace.x / VoxelWorld.chunkWidth)), Mathf.floorToInt((worldspace.y / VoxelWorld.chunkHeight)), Mathf.floorToInt((worldspace.z / VoxelWorld.chunkDepth)));
	}

	/**
	 * Returns all keys in this instance (only copies of them).
	 * 
	 * @return
	 */
	public ChunkKey[] getKeys()
	{
		return this.chunks.getKeys();
	}

	/**
	 * Gets the voxel light level at the given global voxelspace position.
	 * Returns -1 in case of an error.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public byte getLightLevel(int x, int y, int z)
	{
		// Calculate chunk position for calculating relative position
		Vector3 chunk = this.getChunkspacePosition(new Vector3(x, y, z));

		// Calculate relative position
		x -= (int) (chunk.x * VoxelWorld.chunkWidth);
		y -= (int) (chunk.y * VoxelWorld.chunkHeight);
		z -= (int) (chunk.z * VoxelWorld.chunkDepth);
		ChunkKey chunkKey = new ChunkKey((int) chunk.x, (int) chunk.y, (int) chunk.z);

		synchronized (this.chunksLockObject)
		{
			if (!this.chunks.containsKey(chunkKey))
			{
				ConsoleHelper.writeLog("error", "Tried to get voxel light level from non existing chunk: " + chunk + " at position " + x + "|" + y + "|" + z + ", isServer:" + this.isServer, "VoxelWorld");
				return -1;
			}

			return this.chunks.get(chunkKey).getLightLevel(x, y, z);
		}
	}

	/**
	 * Returns an array of unloaded chunkspace chunk positions around position
	 * with radius chunkRadius.
	 * 
	 * @param position
	 * @param chunkRadius
	 * @param create
	 *            Create chunks which don't exist.
	 * @return
	 */
	public Vector3[] getNeededChunks(Vector3 position, int chunkRadius, boolean create)
	{
		synchronized (this.chunksLockObject)
		{
			ArrayList<Vector3> positions = new ArrayList<Vector3>();

			Vector3 chunkPos = this.getChunkspacePosition(position);

			for (int x = (int) (chunkPos.x - chunkRadius); x <= chunkPos.x + chunkRadius; x++)
				for (int z = (int) (chunkPos.z - chunkRadius); z <= chunkPos.z + chunkRadius; z++)
					for (int y = 0; y < this.worldHeight / VoxelWorld.chunkHeight; y++)
						if (!this.chunks.containsKey(new ChunkKey(x, y, z)) && y <= this.worldHeight / VoxelWorld.chunkHeight && new Vector3(chunkPos).sub(new Vector3(x, chunkPos.y, z)).len() <= chunkRadius)
						{
							positions.add(new Vector3(x, y, z));

							if (create)
								this.instantiateChunk(x, y, z);
						}

			return positions.toArray(new Vector3[positions.size()]);
		}
	}

	/**
	 * Returns the master server instance.
	 * 
	 * @return
	 */
	public CubicWorldServer getServer()
	{
		return this.master;
	}

	/**
	 * <pre>
	 * Gets the voxel at the given global voxelspace position. Returns null if
	 * the voxel was not found.
	 * 
	 * If you modify any values of the voxel returned by this function, you must re-set it by calling setVoxel() or call chunkDataWasModified() after your work is done.
	 * </pre>
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public VoxelData getVoxel(int x, int y, int z)
	{
		// Calculate chunk position for calculating relative position
		Vector3 chunk = this.getChunkspacePosition(new Vector3(x, y, z));

		// Calculate relative position
		x -= (int) (chunk.x * VoxelWorld.chunkWidth);
		y -= (int) (chunk.y * VoxelWorld.chunkHeight);
		z -= (int) (chunk.z * VoxelWorld.chunkDepth);
		ChunkKey chunkKey = new ChunkKey((int) chunk.x, (int) chunk.y, (int) chunk.z);

		synchronized (this.chunksLockObject)
		{
			if (!this.chunks.containsKey(chunkKey))
			{
				ConsoleHelper.writeLog("error", "Tried to get voxel from non existing chunk: " + chunk + " at position " + x + "|" + y + "|" + z + ", isServer:" + this.isServer, "VoxelWorld");
				return null;
			}

			return this.chunks.get(chunkKey).getVoxel(x, y, z);
		}
	}

	/**
	 * @return the voxelUpdateHandler
	 */
	public IVoxelDataUpdateHandler getVoxelDataUpdateHandler()
	{
		return voxelDataUpdateHandler;
	}

	/**
	 * Generates the blockspace position where this worldspace is located in.
	 * 
	 * @param worldspace
	 * @return
	 */
	public Vector3 getVoxelspacePosition(Vector3 worldspace)
	{
		return new Vector3(Mathf.floorToInt(worldspace.x), Mathf.floorToInt(worldspace.y), Mathf.floorToInt(worldspace.z));
	}

	/**
	 * Sets the world file instance. If it gets set the world data will get read
	 * and written to the world file.
	 * 
	 * @param file
	 */
	public VoxelWorldSave getWorldFile()
	{
		return this.voxelWorldSave;
	}

	/**
	 * Checks if the block at the given global blockspace position exists.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public boolean hasVoxel(int x, int y, int z)
	{
		// Calculate chunk position for calculating relative position
		Vector3 chunk = this.getChunkspacePosition(new Vector3(x, y, z));

		// Calculate relative position
		x -= (int) (chunk.x * VoxelWorld.chunkWidth);
		y -= (int) (chunk.y * VoxelWorld.chunkHeight);
		z -= (int) (chunk.z * VoxelWorld.chunkDepth);
		ChunkKey chunkKey = new ChunkKey((int) chunk.x, (int) chunk.y, (int) chunk.z);

		synchronized (this.chunksLockObject)
		{
			if (!this.chunks.containsKey(chunkKey))
			{
				return false;
			}

			return this.chunks.get(chunkKey).hasVoxel(x, y, z);
		}
	}

	/**
	 * Returns true if this instance has a voxel world file property which is
	 * not null.
	 * 
	 * @return
	 */
	public boolean hasWorldFile()
	{
		return this.voxelWorldSave != null;
	}

	/**
	 * Initializes the pathfinder instance if it is not already initialized.
	 */
	private void initializePathfinder()
	{
		if (this.pathfinderInstance == null)
			this.pathfinderInstance = new Pathfinder(this);
	}

	/**
	 * Starts the world update thread handler.
	 * This gets only called on the client. The server got it's own update thread handler-
	 * The thread will continously call chunks.update().
	 */
	private void initUpdateThread()
	{
		// World update handler
		this.worldUpdateThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while (true)
				{
					synchronized (chunksLockObject)
					{
						// Fire updates
						chunks.update();
					}

					// Wait some time
					try
					{
						Thread.sleep(10);
					}
					catch (InterruptedException e)
					{ // No interrupt is expected here.
					}
				}
			}

		});

		this.worldUpdateThread.setName("World update thread");
		this.worldUpdateThread.start();
	}

	/**
	 * Instantiates a chunk and adds it to the chunks list.
	 * 
	 * @param chunkX
	 * @param chunkY
	 * @param chunkZ
	 * @return
	 */
	private VoxelChunk instantiateChunk(int chunkX, int chunkY, int chunkZ)
	{
		VoxelChunk chunk = new VoxelChunk(chunkX, chunkY, chunkZ, this);

		chunks.add(new ChunkKey(chunkX, chunkY, chunkZ), chunk);

		return chunk;
	}

	/**
	 * <pre>
	 * Gets all voxel bounding boxes which could possibly hit the bounding box.
	 * Then it perfoms an intersection check on b for all bounding boxes.
	 * If any voxel collieded the function will return true.
	 * 
	 * IMPORTANT: If a non-ready chunk is needed for calculation, this will return true!
	 * </pre>
	 * 
	 * @param b
	 * @return
	 */
	public boolean intersects(BoundingBox boundingBox)
	{
		// TODO Check if we really need all that voxels
		int width = Mathf.ceilToInt(boundingBox.max.x - boundingBox.min.x) + 1;
		int height = Mathf.ceilToInt(boundingBox.max.y - boundingBox.min.y) + 1;
		int depth = Mathf.ceilToInt(boundingBox.max.z - boundingBox.min.z) + 1;

		Vector3 voxelspaceCenter = this.getVoxelspacePosition(new Vector3(boundingBox.min));

		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				for (int z = 0; z < depth; z++)
				{
					// Check if chunk is loaded
					Vector3 chunkPos = this.getChunkspacePosition(new Vector3((int) voxelspaceCenter.x + x, (int) voxelspaceCenter.y + y, (int) voxelspaceCenter.z + z));
					if (chunkPos.y < (this.worldHeight / VoxelWorld.chunkHeight) && !this.isChunkInitialized((int) chunkPos.x, (int) chunkPos.y, (int) chunkPos.z))
					{
						return true;
					}

					// Get bounding box
					BoundingBox voxelBoundingBox = this.getBoundingBox((int) voxelspaceCenter.x + x, (int) voxelspaceCenter.y + y, (int) voxelspaceCenter.z + z);

					// Check for intersection
					if (voxelBoundingBox != null && boundingBox.intersects(voxelBoundingBox))
					{
						return true;
					}
				}
			}
		}

		return false;
	}

	/**
	 * Checks if the chunk with the given chunkspace position already got
	 * initialized.
	 * 
	 * @param chunkX
	 * @param chunkY
	 * @param chunkZ
	 * @return
	 */
	public boolean isChunkInitialized(int chunkX, int chunkY, int chunkZ)
	{
		synchronized (this.chunksLockObject)
		{
			VoxelChunk chunk = this.chunks.get(new ChunkKey(chunkX, chunkY, chunkZ));
			if (chunk == null)
				return false;
			return chunk.isInitialized();
		}
	}

	/**
	 * Returns true if the current voxel world is a server instance.
	 * 
	 * @return
	 */
	public boolean isServer()
	{
		return this.isServer;
	}

	/**
	 * Performs a raycast check in the voxel world and returns a raycast hit.
	 * Will return null if nothing was found on the ray.
	 * 
	 * @param start
	 * @param direction
	 * @param length
	 * @return
	 */
	public RaycastHit raycast(Vector3 start, Vector3 direction, float length)
	{

		float distanceTraveled = 0;
		float distancePerStep = 0.05f;

		Vector3 directionPerStep = new Vector3(direction).nor();
		directionPerStep.x *= distancePerStep;
		directionPerStep.y *= distancePerStep;
		directionPerStep.z *= distancePerStep;

		Vector3 currentPosition = new Vector3(start);

		// Perform raycast
		while (distanceTraveled <= length)
		{
			// Block hit check
			currentPosition = currentPosition.add(directionPerStep);
			Vector3 blockPosition = this.getVoxelspacePosition(currentPosition);

			if (this.hasVoxel((int) blockPosition.x, (int) blockPosition.y, (int) blockPosition.z))
			{
				// Voxel hit
				RaycastHit hitInfo = new RaycastHit();

				// Calculate face
				Vector3[] faceNormals = new Vector3[] { new Vector3(-1, 0, 0),// Vector3.left,
						new Vector3(1, 0, 0),// Vector3.right,
						new Vector3(0, 1, 0),// Vector3.up,
						new Vector3(0, -1, 0),// Vector3.down,
						new Vector3(0, 0, 1),// Vector3.forward,
						new Vector3(0, 0, -1),// Vector3.back
				};

				// Get block center position
				Vector3 blockCenterPosition = new Vector3(blockPosition).add(0.5f, 0.5f, 0.5f);

				// Get shortest distance face.
				float shortestDistance = distancePerStep + 10;
				int nearestFace = -1;

				for (int i = 0; i < faceNormals.length; i++)
				{
					// Get distance from hit point to the current normal + blockcenter
					Vector3 blockNormalPosition = new Vector3(blockCenterPosition).add(faceNormals[i]);
					float distance = new Vector3(currentPosition).sub(blockNormalPosition).len(); // Vector3.Distance(currentPos,
																									// blockNormalPosition);
					if (shortestDistance > distance)
					{
						shortestDistance = distance;
						nearestFace = i;
					}
				}

				if (nearestFace != -1)
					hitInfo.hitFace = VoxelFace.values()[nearestFace];

				// get voxel's data
				VoxelData voxel = this.getVoxel((int) blockPosition.x, (int) blockPosition.y, (int) blockPosition.z);
				hitInfo.hitVoxelPosition = blockPosition;
				hitInfo.hitVoxelData = voxel;

				return hitInfo;
			}
			distanceTraveled += distancePerStep;
		}

		return null;
	}

	/**
	 * Renders the voxel world.
	 */
	public void render(Camera cam)
	{
		// Voxel rendering pass
		// Set shader values
		this.worldShader.begin();

		this.worldShader.setUniformMatrix("m_cameraProj", cam.combined);

		VoxelEngine.textureAtlas.atlasTexture.bind(0);
		this.worldShader.setUniformi("r_textureAtlas", 0);

		synchronized (this.chunksLockObject)
		{
			this.chunks.render(cam, this.worldShader);
		}

		this.worldShader.end();

		// Model rendering pass

		synchronized (this.chunksLockObject)
		{
			this.chunkModelBatch.begin(this.camera);
			this.chunks.renderModels(cam, this.chunkModelBatch);
			this.chunkModelBatch.end();
		}
	}

	/**
	 * Sets the block at the given global blockspace position.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public void setVoxel(int x, int y, int z, VoxelData voxel)
	{
		// Calculate chunk position for calculating relative position
		Vector3 chunk = this.getChunkspacePosition(new Vector3(x, y, z));

		// Calculate relative position
		x -= (int) (chunk.x * VoxelWorld.chunkWidth);
		y -= (int) (chunk.y * VoxelWorld.chunkHeight);
		z -= (int) (chunk.z * VoxelWorld.chunkDepth);
		ChunkKey chunkKey = new ChunkKey((int) chunk.x, (int) chunk.y, (int) chunk.z);

		synchronized (this.chunksLockObject)
		{
			if (!this.chunks.containsKey(chunkKey))
			{
				ConsoleHelper.writeLog("error", "Tried to set voxel from non existing chunk: " + chunk + " at position " + x + "|" + y + "|" + z + ", isServer: " + this.isServer, "VoxelWorld");
				return;
			}

			this.chunks.get(chunkKey).setVoxel(x, y, z, voxel);
		}
	}

	/**
	 * @param voxelUpdateHandler
	 *            the voxelUpdateHandler to set
	 */
	public void setVoxelDataUpdateHandler(IVoxelDataUpdateHandler voxelUpdateHandler)
	{
		this.voxelDataUpdateHandler = voxelUpdateHandler;
	}

	/**
	 * Sets the world file instance. If it gets set the world data will get read
	 * and written to the world file.
	 * 
	 * @param file
	 */
	public void setWorldFile(VoxelWorldSave file)
	{
		this.voxelWorldSave = file;
	}

	/**
	 * Sets the world generator instance.
	 * 
	 * @param worldGenerator
	 */
	public void setWorldGenerator(AWorldGenerator worldGenerator)
	{
		// Init world generator thread pool
		if (this.worldGeneratorThreadPool == null)
			this.worldGeneratorThreadPool = new WorldGeneratorThreadPool(Runtime.getRuntime().availableProcessors());

		this.worldGenerator = worldGenerator;
	}

	/**
	 * Simulate the world, calls the simulate() function on all chunks.
	 */
	public void simulate()
	{
		synchronized (this.chunksLockObject)
		{
			this.chunks.simulate();
		}
	}

	/**
	 * Updates the world, calls the update() function on all chunks.
	 */
	public void update()
	{
		synchronized (this.chunksLockObject)
		{
			this.chunks.update();
		}
	}
}
