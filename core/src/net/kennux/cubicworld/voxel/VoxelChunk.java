package net.kennux.cubicworld.voxel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import net.kennux.cubicworld.CubicWorld;
import net.kennux.cubicworld.CubicWorldConfiguration;
import net.kennux.cubicworld.inventory.IInventory;
import net.kennux.cubicworld.inventory.IInventoryUpdateHandler;
import net.kennux.cubicworld.math.MathUtils;
import net.kennux.cubicworld.math.Vector3i;
import net.kennux.cubicworld.networking.packet.ClientChunkRequest;
import net.kennux.cubicworld.networking.packet.inventory.ServerBlockInventoryUpdate;
import net.kennux.cubicworld.voxel.handlers.IVoxelTileEntityHandler;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

/**
 * Voxel chunk implementation.
 * 
 * @author KennuX
 *
 */
public class VoxelChunk
{
	// STATIC DATA

	private static final Vector3[] LEFT_SIDE_VERTICES = new Vector3[] { new Vector3(0, 0, 1), new Vector3(0, 0, 0), new Vector3(0, 1, 0), new Vector3(0, 1, 1) };

	private static final Vector3[] LEFT_SIDE_NORMALS = new Vector3[] { new Vector3(1, 0, 0), new Vector3(1, 0, 0), new Vector3(1, 0, 0), new Vector3(1, 0, 0) };

	private static final short[] LEFT_SIDE_INDICES = new short[] { 1, 0, 2, 0, 3, 2 };

	private static final Vector3[] RIGHT_SIDE_VERTICES = new Vector3[] { new Vector3(1, 0, 0), new Vector3(1, 0, 1), new Vector3(1, 1, 1), new Vector3(1, 1, 0), };

	private static final Vector3[] RIGHT_SIDE_NORMALS = new Vector3[] { new Vector3(-1, 0, 0), new Vector3(-1, 0, 0), new Vector3(-1, 0, 0), new Vector3(-1, 0, 0) };

	private static final short[] RIGHT_SIDE_INDICES = new short[] { 1, 0, 2, 0, 3, 2 };

	private static final Vector3[] TOP_SIDE_VERTICES = new Vector3[] { new Vector3(0, 1, 0), new Vector3(1, 1, 0), new Vector3(1, 1, 1), new Vector3(0, 1, 1), };

	private static final Vector3[] TOP_SIDE_NORMALS = new Vector3[] { new Vector3(0, 1, 0), new Vector3(0, 1, 0), new Vector3(0, 1, 0), new Vector3(0, 1, 0) };

	private static final short[] TOP_SIDE_INDICES = new short[] { 1, 0, 2, 0, 3, 2 };

	private static final Vector3[] BOTTOM_SIDE_VERTICES = new Vector3[] { new Vector3(0, 0, 0), new Vector3(1, 0, 0), new Vector3(1, 0, 1), new Vector3(0, 0, 1) };

	private static final Vector3[] BOTTOM_SIDE_NORMALS = new Vector3[] { new Vector3(0, -1, 0), new Vector3(0, -1, 0), new Vector3(0, -1, 0), new Vector3(0, -1, 0) };

	private static final short[] BOTTOM_SIDE_INDICES = new short[] { 1, 2, 0, 2, 3, 0 };

	private static final Vector3[] BACK_SIDE_VERTICES = new Vector3[] { new Vector3(0, 0, 0), new Vector3(1, 0, 0), new Vector3(1, 1, 0), new Vector3(0, 1, 0) };

	private static final Vector3[] BACK_SIDE_NORMALS = new Vector3[] { new Vector3(0, 0, -1), new Vector3(0, 0, -1), new Vector3(0, 0, -1), new Vector3(0, 0, -1) };

	private static final short[] BACK_SIDE_INDICES = new short[] { 2, 1, 0, 0, 3, 2 };

	private static final Vector3[] FRON_SIDE_VERTICES = new Vector3[] { new Vector3(1, 0, 1), new Vector3(0, 0, 1), new Vector3(0, 1, 1), new Vector3(1, 1, 1) };

	private static final Vector3[] FRONT_SIDE_NORMALS = new Vector3[] { new Vector3(0, 0, 1), new Vector3(0, 0, 1), new Vector3(0, 0, 1), new Vector3(0, 0, 1) };

	private static final short[] FRONT_SIDE_INDICES = new short[] { 2, 1, 0, 0, 3, 2 };

	/**
	 * <pre>
	 * Rotation face mappings Indices:
	 * 0 - LEFT
	 * 1 - RIGHT
	 * 2 - TOP
	 * 3 - BOTTOM
	 * 4 - BACK
	 * 5 - FRONT
	 * 
	 * Sides mapped from initial rotation.
	 * </pre>
	 */
	public static final VoxelFace[][] ROTATION_MAPPINGS = new VoxelFace[][] {
			// Facing front
			new VoxelFace[] { VoxelFace.LEFT, VoxelFace.RIGHT, VoxelFace.TOP, VoxelFace.BOTTOM, VoxelFace.BACK, VoxelFace.FRONT },
			// Facing right
			new VoxelFace[] { VoxelFace.FRONT, VoxelFace.BACK, VoxelFace.TOP, VoxelFace.BOTTOM, VoxelFace.LEFT, VoxelFace.RIGHT },
			// Facing back
			new VoxelFace[] { VoxelFace.RIGHT, VoxelFace.LEFT, VoxelFace.TOP, VoxelFace.BOTTOM, VoxelFace.FRONT, VoxelFace.BACK },
			// Facing left
			new VoxelFace[] { VoxelFace.BACK, VoxelFace.FRONT, VoxelFace.TOP, VoxelFace.BOTTOM, VoxelFace.RIGHT, VoxelFace.LEFT } };

	/**
	 * <pre>
	 * The normal vectors for voxelfaces.
	 * Example usage:
	 * 
	 * VoxelFace f = VoxelFace.LEFT;
	 * Vector3 normal = FACE_NORMALS[f.getValue()];
	 * 
	 * All vectors are 1 unit long!.
	 * </pre>
	 */
	public static final Vector3[] FACE_NORMALS = new Vector3[] { new Vector3(1, 0, 0), new Vector3(-1, 0, 0), new Vector3(0, 1, 0), new Vector3(0, -1, 0), new Vector3(0, 0, 1), new Vector3(0, 0, -1), };
	/**
	 * Rotation quaternion mappings.
	 * This quaternions will get used to define a voxel model's rendering rotation based on the rotation byte.
	 * 
	 * They get calculated in the constructor.
	 */
	public static Quaternion[] rotationTransformMappings;

	// END OF STATIC DATA

	/**
	 * The current voxel mesh used for rendering.
	 */
	private Mesh voxelMesh;

	/**
	 * Gets used to synchronize voxel data changes.
	 */
	private Object voxelDataLockObject = new Object();

	/**
	 * If this flag is set to true the voxel mesh got marked as dirty, which
	 * means it needs to get re-generated.
	 */
	private boolean voxelMeshDirty;

	/**
	 * If this flag is set to true the new mesh data gots generated by the
	 * update thread.
	 */
	private boolean newMeshDataReady;

	/**
	 * The voxel mesh's bounding box.
	 */
	private BoundingBox boundingBox;

	/**
	 * The voxel mesh's new bounding box.
	 */
	private BoundingBox newBoundingBox;

	/**
	 * The voxel data (voxeltype id's, -1 for no block here). You must lock
	 * voxelDataLockObject when you are writing to this field. After direct
	 * writing to this you must call setDirty().
	 */
	private volatile VoxelData[][][] voxelData;

	/**
	 * The chunk x-position.
	 */
	private int chunkX;

	/**
	 * The chunk y-position.
	 */
	private int chunkY;

	/**
	 * The chunk z-position.
	 */
	private int chunkZ;

	/**
	 * Gets set to true after the world generation is done.
	 */
	private boolean generationDone = false;

	/**
	 * Gets set to true if the local lighting information is up 2 date.
	 */
	private boolean localLightingDirty = false;

	/**
	 * Gets set to true if the global lighting information is up 2 date.
	 */
	private boolean globalLightingDirty = false;

	/**
	 * Gets set to true if a save of this chunk to the voxel world file is
	 * needed.
	 * Will get handled in update().
	 */
	private boolean saveDirty = false;

	/**
	 * The voxel world master instance.
	 */
	public VoxelWorld master;

	/**
	 * The models which will get rendered in every frame.
	 * They get built in generateMeshData().
	 */
	private ArrayList<ModelInstance> models;
	/**
	 * The temporary new models value.
	 * Will get built in generateMeshData().
	 * 
	 * This gets first set and then used in the models arraylist.
	 */
	private ArrayList<ModelInstance> newModels;

	/**
	 * The voxel update handlers list.
	 */
	private HashMap<Vector3, IVoxelTileEntityHandler> tileEntityHandlers = new HashMap<Vector3, IVoxelTileEntityHandler>();
	
	/**
	 * The voxel update handlers list copy instance.
	 * The copy of this instance will get used for acutally firing the tile entity events.
	 * @see VoxelChunk#update()
	 */
	private HashMap<Vector3, IVoxelTileEntityHandler> tileEntityHandlersCopyInstance = new HashMap<Vector3, IVoxelTileEntityHandler>();

	private Object generationLockObject = new Object();

	// New mesh data list
	// Gets generated in the update() function which gets called by an own
	// thread separated from the main thread.
	private float[] newVertices;
	private short[] newIndices;
	private final int vertexSize = 6;

	/**
	 * The absolute chunk position vector.
	 */
	private Vector3i absoluteChunkPosition;

	/**
	 * The mesh pool fifo stack.
	 */
	private static LinkedList<Mesh> meshPool = new LinkedList<Mesh>();

	/**
	 * The last call id when update() was called.
	 * Used to limit chunk updates per frame for lag reduction.
	 */
	private static long lastUpdateCallId = -1;

	/**
	 * Contains the number of generateMesh() calls this frame.
	 */
	private static int generationsProcessedThisFrame = -1;

	/**
	 * The last frame id when render() was called.
	 * Used to limit chunk updates per frame for lag reduction.
	 */
	private static long lastRenderFrameId = -1;

	/**
	 * Contains the number of generateMesh() calls this frame.
	 */
	private static int creationsProcessedThisFrame = -1;

	private static Mesh newMesh()
	{
		return new Mesh(Mesh.VertexDataType.VertexBufferObject, false, 16368, 16368, new VertexAttribute(Usage.Position, 3, "v_Position"), new VertexAttribute(Usage.TextureCoordinates, 2, "v_Uv"), /* new VertexAttribute(Usage.Normal, 3, "v_Normal"), */new VertexAttribute(Usage.ColorUnpacked, 1, "v_Light"));
	}

	public VoxelChunk(int chunkX, int chunkY, int chunkZ, VoxelWorld master)
	{
		this.voxelData = null;

		// Init rotation mappings
		rotationTransformMappings = new Quaternion[] {
				// Facing front
				new Quaternion(0, 0, 0, 0),
				// Facing right
				new Quaternion(0, 0, 0, 0).setEulerAngles(90, 0, 0),
				// Facing back
				new Quaternion(0, 0, 0, 0).setEulerAngles(180, 0, 0),
				// Facing left
				new Quaternion(0, 0, 0, 0).setEulerAngles(270, 0, 0) };

		// Set chunkspae position
		this.chunkX = chunkX;
		this.chunkY = chunkY;
		this.chunkZ = chunkZ;

		// Set absolute chunk position
		this.absoluteChunkPosition = new Vector3i(this.chunkX * VoxelWorld.chunkWidth, this.chunkY * VoxelWorld.chunkHeight, this.chunkZ * VoxelWorld.chunkDepth);

		this.master = master;
	}

	/**
	 * Sets this chunk to dirty which will regenerate the mesh data.
	 * ONLY call this if you loaded the chunk data from the world file.
	 */
	private void chunkDataWasLoaded()
	{
		this.localLightingDirty = true;
		this.globalLightingDirty = true;
		this.voxelMeshDirty = true;
		this.saveDirty = false;
	}

	/**
	 * Causes this chunk to regenerate it's mesh.
	 * Will only recalculate lighting and the mesh.
	 * This will also cause the adjacent chunks to regenerate their meshes.
	 */
	public void regenerateLightingAndMesh()
	{
		this.localLightingDirty = true;
		this.globalLightingDirty = true;
		this.voxelMeshDirty = true;
		this.regenerateAdjacentMeshes();
	}

	/**
	 * Causes this chunk to regenerate it's mesh.
	 */
	public void regenerateMesh()
	{
		this.voxelMeshDirty = true;
	}
	
	/**
	 * Regenerates the adjacent meshes.
	 * Only the meshes! no lighting calculation will be performed!
	 */
	private void regenerateAdjacentMeshes()
	{
		VoxelChunk[] chunks = new VoxelChunk[]
		{
			this.master.getChunk(chunkX+1, chunkY, chunkZ, false),
			this.master.getChunk(chunkX-1, chunkY, chunkZ, false),
			this.master.getChunk(chunkX, chunkY+1, chunkZ, false),
			this.master.getChunk(chunkX, chunkY-1, chunkZ, false),
			this.master.getChunk(chunkX, chunkY, chunkZ+1, false),
			this.master.getChunk(chunkX, chunkY, chunkZ-1, false),
		};
		
		for (VoxelChunk vc : chunks)
		{
			if (vc != null)
				vc.regenerateMesh();
		}
	}

	/**
	 * Sets this chunk to dirty which will regenerate the mesh data. You have to
	 * call this function after direct writing to voxelData.
	 */
	private void chunkDataWasModified()
	{
		this.localLightingDirty = true;
		this.globalLightingDirty = true;
		this.voxelMeshDirty = true;
		this.saveDirty = true;
	}

	/**
	 * Creates the new mesh from newVertices, newUvs, newColors and newIndices
	 * lists.
	 */
	private void createNewMesh()
	{
		synchronized (this.generationLockObject)
		{
			// Mesh empty?
			if (this.newVertices.length == 0)
			{
				this.voxelMesh = null;
			}
			else
			{
				// Construct new mesh
				if (this.voxelMesh == null)
				{
					this.voxelMesh = meshPool.poll();

					if (this.voxelMesh == null)
					{
						this.voxelMesh = newMesh();
					}
				}

				// Set the vertices
				this.voxelMesh.setVertices(this.newVertices);
				this.voxelMesh.setIndices(this.newIndices);

				// Calculate bounding box
				try
				{
					// DISABLED DUE TO HIGH PERFORMANCE COST
					// INSTEAD STATIC BOUNDING BOXES WILL GET USED GENERATED IN generateMesh()
					// this.boundingBox = newMesh.calculateBoundingBox();
					this.boundingBox = this.newBoundingBox;
				}
				catch (Exception e)
				{
					// Happens if there are no vertices in this mesh, so just ignore this error.
				}
			}

			this.newMeshDataReady = false;
			this.voxelMeshDirty = false;

			// Free old data
			this.newBoundingBox = null;
			this.newVertices = null;
			this.newIndices = null;

			// Dispose old mesh if available
			/*
			 * if (this.voxelMesh != null)
			 * this.voxelMesh.dispose();
			 */

			this.models = this.newModels;
			// this.voxelMesh = newMesh;
		}
	}

	/**
	 * This call is not thread-safe, only call this on an object which wont get used anymore.
	 * 
	 * This will not get called by the garbage collector as this class does not implement the disposable interface.
	 * It is only used to add the current mesh to the mesh pool.
	 */
	public void dispose()
	{
		meshPool.add(this.voxelMesh);
	}

	/**
	 * Generates the voxel mesh based on the current chunk's voxel data.
	 */
	private void generateMesh()
	{
		synchronized (this.generationLockObject)
		{
			if (this.localLightingDirty || this.voxelData == null)
				return;

			// The local copy of the voxel data object
			VoxelData[][][] voxelData = this.getVoxelData();

			// the vertices array list
			final int initListLength = 16000; // Start with a length of 16000 to avoid re-allocation
			ArrayList<Float> vertices = new ArrayList<Float>(initListLength * vertexSize);
			ArrayList<Short> indices = new ArrayList<Short>(initListLength);
			ArrayList<ModelInstance> modelList = new ArrayList<ModelInstance>();
			short indicesCounter = 0;

			for (int x = 0; x < VoxelWorld.chunkWidth; x++)
			{
				for (int z = 0; z < VoxelWorld.chunkDepth; z++)
				{
					for (int y = 0; y < VoxelWorld.chunkHeight; y++)
					{
						// Voxel in my position?
						if (voxelData[x][y][z] == null || voxelData[x][y][z].voxelType == null)
							continue;

						Vector3i absolutePos = this.getAbsoluteVoxelPosition(x, y, z);
						int absX = absolutePos.x;
						int absY = absolutePos.y;
						int absZ = absolutePos.z;

						VoxelData leftVoxel = (x == 0 ? this.master.getVoxel(absX - 1, absY, absZ) : voxelData[x - 1][y][z]);
						VoxelData rightVoxel = (x == VoxelWorld.chunkWidth - 1 ? this.master.getVoxel(absX + 1, absY, absZ) : voxelData[x + 1][y][z]);
						VoxelData topVoxel = (y == VoxelWorld.chunkHeight - 1 ? this.master.getVoxel(absX, absY + 1, absZ) : voxelData[x][y + 1][z]);
						VoxelData bottomVoxel = (y == 0 ? this.master.getVoxel(absX, absY - 1, absZ) : voxelData[x][y - 1][z]);
						VoxelData backVoxel = (z == 0 ? this.master.getVoxel(absX, absY, absZ - 1) : voxelData[x][y][z - 1]);
						VoxelData frontVoxel = (z == VoxelWorld.chunkDepth - 1 ? this.master.getVoxel(absX, absY, absZ + 1) : voxelData[x][y][z + 1]);

						boolean leftSideVisible = x != 0 ? (leftVoxel == null || leftVoxel.voxelType == null || leftVoxel.voxelType.voxelId < 0 || leftVoxel.voxelType.transparent) : true;
						boolean rightSideVisible = x != VoxelWorld.chunkWidth - 1 ? (rightVoxel == null || rightVoxel.voxelType == null || rightVoxel.voxelType.voxelId < 0 || rightVoxel.voxelType.transparent) : true;
						boolean topSideVisible = y != VoxelWorld.chunkHeight - 1 ? (topVoxel == null || topVoxel.voxelType == null || topVoxel.voxelType.voxelId < 0 || topVoxel.voxelType.transparent) : true;
						boolean bottomSideVisible = y != 0 ? (bottomVoxel == null || bottomVoxel.voxelType == null || bottomVoxel.voxelType.voxelId < 0 || bottomVoxel.voxelType.transparent) : true;
						boolean backSideVisible = z != 0 ? (backVoxel == null || backVoxel.voxelType == null || backVoxel.voxelType.voxelId < 0 || backVoxel.voxelType.transparent) : true;
						boolean frontSideVisible = z != VoxelWorld.chunkDepth - 1 ? (frontVoxel == null || frontVoxel.voxelType == null || frontVoxel.voxelType.voxelId < 0 || frontVoxel.voxelType.transparent) : true;

						// Model or normal voxel rendering?
						if (voxelData[x][y][z].voxelType.isModelRendering() &&
						// Atleast any side visible?
								(leftSideVisible || rightSideVisible || topSideVisible || bottomSideVisible || backSideVisible || frontSideVisible))
						{
							// Model rendering
							Model m = voxelData[x][y][z].voxelType.getModel();
							ModelInstance mInstance = new ModelInstance(m);

							Vector3 vert = new Vector3(x, y, z);
							vert.x += 0.5f + ((float) this.chunkX * (float) VoxelWorld.chunkWidth);
							vert.y += 0.5f + ((float) this.chunkY * (float) VoxelWorld.chunkHeight);
							vert.z += 0.5f + ((float) this.chunkZ * (float) VoxelWorld.chunkDepth);

							// Set model transformation
							mInstance.transform.set(vert, VoxelChunk.rotationTransformMappings[voxelData[x][y][z].rotation]);
							modelList.add(mInstance);
						}
						else
						{
							// Normal voxel rendering
							VoxelFace[] faceMappings = ROTATION_MAPPINGS[voxelData[x][y][z].rotation];

							byte leftLighting = leftVoxel == null ? 0 : leftVoxel.getLightLevel();
							byte rightLighting = rightVoxel == null ? 0 : rightVoxel.getLightLevel();
							byte topLighting = topVoxel == null ? 0 : topVoxel.getLightLevel();
							byte bottomLighting = bottomVoxel == null ? 0 : bottomVoxel.getLightLevel();
							byte backLighting = backVoxel == null ? 0 : backVoxel.getLightLevel();
							byte frontLighting = frontVoxel == null ? 0 : frontVoxel.getLightLevel();

							// Write mesh data
							if (leftSideVisible)
							{
								this.WriteSideData(vertices, indices, LEFT_SIDE_VERTICES, LEFT_SIDE_NORMALS, LEFT_SIDE_INDICES, indicesCounter, x, y, z, voxelData[x][y][z], faceMappings[0], leftLighting);
								indicesCounter += LEFT_SIDE_VERTICES.length;
							}
							if (rightSideVisible)
							{
								this.WriteSideData(vertices, indices, RIGHT_SIDE_VERTICES, RIGHT_SIDE_NORMALS, RIGHT_SIDE_INDICES, indicesCounter, x, y, z, voxelData[x][y][z], faceMappings[1], rightLighting);
								indicesCounter += RIGHT_SIDE_VERTICES.length;
							}
							if (topSideVisible)
							{
								this.WriteSideData(vertices, indices, TOP_SIDE_VERTICES, TOP_SIDE_NORMALS, TOP_SIDE_INDICES, indicesCounter, x, y, z, voxelData[x][y][z], faceMappings[2], topLighting);
								indicesCounter += TOP_SIDE_VERTICES.length;
							}
							if (bottomSideVisible)
							{
								this.WriteSideData(vertices, indices, BOTTOM_SIDE_VERTICES, BOTTOM_SIDE_NORMALS, BOTTOM_SIDE_INDICES, indicesCounter, x, y, z, voxelData[x][y][z], faceMappings[3], bottomLighting);
								indicesCounter += BOTTOM_SIDE_VERTICES.length;
							}
							if (backSideVisible)
							{
								this.WriteSideData(vertices, indices, BACK_SIDE_VERTICES, BACK_SIDE_NORMALS, BACK_SIDE_INDICES, indicesCounter, x, y, z, voxelData[x][y][z], faceMappings[4], backLighting);
								indicesCounter += BACK_SIDE_VERTICES.length;
							}
							if (frontSideVisible)
							{
								this.WriteSideData(vertices, indices, FRON_SIDE_VERTICES, FRONT_SIDE_NORMALS, FRONT_SIDE_INDICES, indicesCounter, x, y, z, voxelData[x][y][z], faceMappings[5], frontLighting);
								indicesCounter += FRON_SIDE_VERTICES.length;
							}
						}
					}
				}
			}

			// Set new models list and bounding box
			this.newModels = modelList;
			this.newBoundingBox = new BoundingBox(this.getAbsoluteVoxelPosition(0, 0, 0).toFloatVector(), this.getAbsoluteVoxelPosition(VoxelWorld.chunkWidth, VoxelWorld.chunkHeight, VoxelWorld.chunkDepth).toFloatVector());

			// Generate vertex data
			this.newVertices = new float[vertices.size()];

			for (int i = 0; i < this.newVertices.length; i++)
			{
				this.newVertices[i] = vertices.get(i).floatValue();
			}

			// Build indices
			this.newIndices = new short[indices.size()];

			int i = 0;
			for (Short index : indices)
			{
				this.newIndices[i] = index.shortValue();
				i++;
			}

			this.newMeshDataReady = true;
		}
	}

	/**
	 * Calculates an absolute position from the given local blockspace position.
	 * @param x
	 * @param y
	 * @param z
	 * @param vector
	 * @return
	 */
	public Vector3i getAbsoluteVoxelPosition(int x, int y, int z, Vector3i vector)
	{
		vector.x = x + this.absoluteChunkPosition.x;
		vector.y = y + this.absoluteChunkPosition.y;
		vector.z = z + this.absoluteChunkPosition.z;
		return vector;
	}

	/**
	 * Calculates an absolute position from the given local blockspace position.
	 * @see VoxelChunk#getAbsoluteVoxelPosition(int, int, int, Vector3i)
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public Vector3i getAbsoluteVoxelPosition(int x, int y, int z)
	{
		return this.getAbsoluteVoxelPosition(x, y, z, new Vector3i(0,0,0));
	}

	/**
	 * Returns the bounding box for a voxel given in local voxelspace.
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
		// Bounds check
		if (this.voxelData != null && x >= 0 && y >= 0 && z >= 0 && x < VoxelWorld.chunkWidth && y < VoxelWorld.chunkHeight && z < VoxelWorld.chunkDepth && this.hasVoxel(x, y, z))
		{
			return new BoundingBox(new Vector3(x, y, z), new Vector3(x + 1, y + 1, z + 1));
		}

		// Not found!
		return null;
	}

	/**
	 * Returns the global light level of the block at the given position.
	 * Returns -1 if there is no voxel in the given position or if an error
	 * happend.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public byte getGlobalLightLevel(int x, int y, int z)
	{
		// Bounds check
		if (this.voxelData != null && x >= 0 && y >= 0 && z >= 0 && x < VoxelWorld.chunkWidth && y < VoxelWorld.chunkHeight && z < VoxelWorld.chunkDepth)
		{
			synchronized (this.voxelDataLockObject)
			{
				if (this.voxelData[x][y][z] != null)
					return this.voxelData[x][y][z].getBlockLightLevel();
			}
		}

		// Not found!
		return -1;
	}

	/**
	 * Gets the voxel data at the given x|y|z position.
	 * Returns null in the case of an error.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param voxel
	 * @return
	 */
	public VoxelData getVoxel(int x, int y, int z)
	{
		// Bounds check
		if (this.voxelData != null && x >= 0 && y >= 0 && z >= 0 && x < VoxelWorld.chunkWidth && y < VoxelWorld.chunkHeight && z < VoxelWorld.chunkDepth)
		{
			synchronized (this.voxelDataLockObject)
			{
				return this.voxelData[x][y][z];
			}
		}

		// Not found!
		return null;
	}

	/**
	 * Returns a copy of the voxel data array.
	 * Only the array object is a copy, the voxeldata contents of the array are
	 * references.
	 * 
	 * @return
	 */
	public VoxelData[][][] getVoxelData()
	{
		synchronized (this.voxelDataLockObject)
		{
			return this.voxelData.clone();
		}
	}

	/**
	 * Checks if the the voxel data at the given x|y|z position is not null or
	 * type id -1. Returns false in the case of an error.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param voxel
	 * @return
	 */
	public boolean hasVoxel(int x, int y, int z)
	{
		// Bounds check
		if (this.voxelData != null && x >= 0 && y >= 0 && z >= 0 && x < VoxelWorld.chunkWidth && y < VoxelWorld.chunkHeight && z < VoxelWorld.chunkDepth)
		{
			synchronized (this.voxelDataLockObject)
			{
				return this.voxelData[x][y][z] != null && this.voxelData[x][y][z].voxelType != null;
			}
		}

		// Not found!
		return false;
	}

	/**
	 * Returns true if the world generator already generated this chunk.
	 * 
	 * @return
	 */
	public boolean isGenerationDone()
	{
		return generationDone;
	}

	/**
	 * Returns true if this voxel chunk is ready for rendering (this means, lighting is done and mesh is generated).
	 * 
	 * @return
	 */
	public boolean isReadyForRendering()
	{
		return this.isInitializedAndLightingReady() && !this.voxelMeshDirty && !this.newMeshDataReady;
	}

	/**
	 * Chunk gets initialized after data got set by the generator or it was
	 * loaded from hdd
	 * 
	 * @return
	 */
	public boolean isInitialized()
	{
		return this.voxelData != null && this.isGenerationDone();
	}

	/**
	 * Returns true if this chunk is initialized and it's lighting is not dirty.
	 * 
	 * @return
	 */
	public boolean isInitializedAndLightingReady()
	{
		return this.isInitialized() && !this.localLightingDirty && !this.globalLightingDirty;
	}

	/**
	 * Returns true if this chunk is initialized and it's local lighting is not dirty.
	 * 
	 * @return
	 */
	public boolean isInitializedAndLocalLightingReady()
	{
		return this.isInitialized() && !this.localLightingDirty;
	}

	/**
	 * Loads the voxel data from the voxel chunk file.
	 */
	public void loadVoxelData()
	{
		if (this.master.hasWorldFile() && this.master.getWorldFile().hasChunk(chunkX, chunkY, chunkZ))
		{
			synchronized (this.voxelDataLockObject)
			{
				// Read
				this.voxelData = this.master.getWorldFile().readChunk(chunkX, chunkY, chunkZ);
				this.setInventoryUpdateHandlerAll();
				this.chunkDataWasLoaded();
				this.setGenerationDone(true);
				this.setTileEntityHandlerAll();
			}
		}
	}

	/**
	 * <pre>
	 * Renders this chunk. Will do nothing if the current voxel mesh is not
	 * available yet.
	 * 
	 * Otherwise it will just call the render() method of the chunk mesh object.
	 * 
	 * This method is <b>NOT</b> thread-safe.
	 * </pre>
	 */
	public void render(Camera cam, ShaderProgram shader)
	{
		CubicWorld.getClient().profiler.startProfiling("MeshCreation" + this.chunkX + "|" + this.chunkY + "|" + this.chunkZ, "");
		boolean frameMismatch = lastRenderFrameId != Gdx.graphics.getFrameId();

		if (this.voxelMeshDirty && this.newMeshDataReady && (CubicWorldConfiguration.meshCreationsPerFrameLimit == -1 || frameMismatch || creationsProcessedThisFrame <= CubicWorldConfiguration.meshCreationsPerFrameLimit))
		{
			// If the frame ids mismatch
			if (frameMismatch)
			{
				// Update frame id and reset counter
				lastRenderFrameId = Gdx.graphics.getFrameId();
				creationsProcessedThisFrame = 0;
			}

			this.createNewMesh();
			creationsProcessedThisFrame++;
		}
		CubicWorld.getClient().profiler.stopProfiling("MeshCreation" + this.chunkX + "|" + this.chunkY + "|" + this.chunkZ);

		CubicWorld.getClient().profiler.startProfiling("MeshRendering" + this.chunkX + "|" + this.chunkY + "|" + this.chunkZ, "");
		if (this.voxelMesh != null && this.boundingBox != null && cam.frustum.boundsInFrustum(this.boundingBox))
		{
			// Render chunk mesh
			this.voxelMesh.render(shader, GL20.GL_TRIANGLES);
		}
		CubicWorld.getClient().profiler.stopProfiling("MeshRendering" + this.chunkX + "|" + this.chunkY + "|" + this.chunkZ);
	}

	/**
	 * <pre>
	 * Renders all voxels on this chunk which are model rendered.
	 * This is part of the second rendering pass of the voxel world.
	 * 
	 * This method is <b>NOT</b> thread-safe.
	 * </pre>
	 * 
	 * @param modelBatch
	 */
	public void renderModels(Camera cam, ModelBatch modelBatch)
	{
		if (this.voxelMesh != null && this.boundingBox != null && cam.frustum.boundsInFrustum(this.boundingBox))
		{
			// Render block models
			for (ModelInstance mI : this.models)
			{
				modelBatch.render(mI);
			}
		}
	}

	/**
	 * Call this with true as parameter after the generation of the chunk is
	 * done.
	 * 
	 * @param generationDone
	 */
	public void setGenerationDone(boolean generationDone)
	{
		this.generationDone = generationDone;
	}

	/**
	 * Helper function for setting the inventory update handler.
	 * 
	 * @param positionX
	 * @param positionY
	 * @param positionZ
	 * @param inventory
	 */
	private void setInventoryUpdateHandler(int positionX, int positionY, int positionZ, VoxelData inventoryVoxel)
	{
		if (this.master.isServer() && inventoryVoxel != null && inventoryVoxel.blockInventory != null)
		{
			// Declare final positions
			final int posX = positionX;
			final int posY = positionY;
			final int posZ = positionZ;
			final VoxelChunk chunkInstance = this;

			inventoryVoxel.blockInventory.setUpdateHandler(new IInventoryUpdateHandler()
			{
				@Override
				public void inventoryGotUpdated(IInventory inventory)
				{
					ServerBlockInventoryUpdate updatePacket = new ServerBlockInventoryUpdate();
					updatePacket.inventory = inventory;
					updatePacket.voxelPositionX = posX;
					updatePacket.voxelPositionY = posY;
					updatePacket.voxelPositionZ = posZ;
					updatePacket.setCullPosition(new Vector3(posX, posY, posZ));

					CubicWorld.getServer().sendPacket(updatePacket);
					chunkInstance.chunkDataWasModified();
				}
			});
		}
	}

	/**
	 * This function will call the setInventoryUpdateHandler for every voxel data which has a inventory attached.
	 * 
	 * @see VoxelChunk#setInventoryUpdateHandler(int, int, int, VoxelData)
	 */
	private void setInventoryUpdateHandlerAll()
	{
		if (!this.master.isServer())
			return;

		for (int x = 0; x < VoxelWorld.chunkWidth; x++)
			for (int y = 0; y < VoxelWorld.chunkHeight; y++)
				for (int z = 0; z < VoxelWorld.chunkDepth; z++)
					if (this.voxelData[x][y][z] != null && this.voxelData[x][y][z].blockInventory != null)
					{
						Vector3i absolutePos = this.getAbsoluteVoxelPosition(x, y, z);
						this.setInventoryUpdateHandler(absolutePos.x, absolutePos.y, absolutePos.z, this.voxelData[x][y][z]);
					}
	}

	/**
	 * Sets the voxel data at the given x|y|z position.
	 * Pass in null or just new VoxelData() as voxel for setting air.
	 * You can and must use this function also if you update a voxel data object to send out a world update on the server.
	 * Don't use this for changes on the client as the client has to send packets for updating the voxel world.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param voxel
	 */
	public void setVoxel(int x, int y, int z, VoxelData voxel)
	{
		synchronized (this.voxelDataLockObject)
		{
			if (this.voxelData == null)
				return;

			// if voxel is null, create air voxel
			if (voxel == null)
				voxel = new VoxelData();

			// Remove update handler if existing
			Vector3 voxelPos = new Vector3(x, y, z);
			this.tileEntityHandlers.remove(voxelPos);

			this.voxelData[x][y][z] = voxel;

			// Inventory
			if (voxel != null && voxel.blockInventory != null)
			{
				Vector3i absolutePos = this.getAbsoluteVoxelPosition(x, y, z);
				this.setInventoryUpdateHandler(absolutePos.x, absolutePos.y, absolutePos.z, voxel);
			}

			this.chunkDataWasModified();

			// notify about update
			if (this.master.getVoxelDataUpdateHandler() != null)
			{
				Vector3i absolutePos = this.getAbsoluteVoxelPosition(x, y, z);
				this.master.getVoxelDataUpdateHandler().handleVoxelDataUpdate(absolutePos.x, absolutePos.y, absolutePos.z, voxel);
			}

			// Update voxel handlers map
			if (voxel != null && voxel.voxelType != null)
			{
				if (voxel.voxelType.isTileEntity() && voxel.tileEntity != null)
				{
					this.tileEntityHandlers.put(voxelPos, voxel.tileEntity);
				}
			}
		}
	}

	/**
	 * Sets the voxel data array.
	 * 
	 * @param voxelData
	 */
	public void setVoxelData(VoxelData[][][] voxelData)
	{
		synchronized (this.voxelDataLockObject)
		{
			// Iterate through all voxel datas and set null voxels to air voxels
			for (int x = 0; x < voxelData.length; x++)
				for (int y = 0; y < voxelData[x].length; y++)
					for (int z = 0; z < voxelData[x][y].length; z++)
						if (voxelData[x][y][z] == null)
							voxelData[x][y][z] = new VoxelData();

			this.voxelData = voxelData;
			this.setInventoryUpdateHandlerAll();
			this.chunkDataWasModified();
			this.setGenerationDone(true);
			this.setTileEntityHandlerAll();
		}
	}

	/**
	 * Iterates through every voxel data in this instance and collects all update handler.
	 */
	private void setTileEntityHandlerAll()
	{
		synchronized (this.voxelDataLockObject)
		{
			this.tileEntityHandlers.clear();

			// Iterate through all voxel data instances
			for (int x = 0; x < this.voxelData.length; x++)
				for (int y = 0; y < this.voxelData[x].length; y++)
					for (int z = 0; z < this.voxelData[x][y].length; z++)
						// Check if the voxel at the given position is not null, not air and a tile entity
						if (this.voxelData[x][y][z] != null && this.voxelData[x][y][z].voxelType != null && this.voxelData[x][y][z].voxelType.isTileEntity())
							// Add to tile entity handlers
							this.tileEntityHandlers.put(new Vector3(x, y, z), this.voxelData[x][y][z].tileEntity);

		}
	}

	/**
	 * Simulates the current chunk. Currently this doesnt do anything but it
	 * will be called once per tick (ticksPerSecond = fps on client, target
	 * ticks on server are 20).
	 */
	public void simulate()
	{
		// TODO Fluid Simulation
	}

	/**
	 * <pre>
	 * Updates this voxel chunk instance.
	 * Does the following:
	 * 
	 * - Checks if save to hdd needed
	 * -> If yes, it will save to hdd
	 * - Checks if the lighting is dirty and needs a regeneration
	 * -> If yes, it regenerates it.
	 * - Checks if the mesh is dirty
	 * -> If yes, it regenerates it.
	 * </pre>
	 */
	public void update()
	{
		// Save needed?
		if (this.saveDirty && this.master.hasWorldFile() && this.isInitialized())
		{
			synchronized (this.voxelDataLockObject)
			{
				// Save chunk
				this.master.getWorldFile().writeChunk(this.chunkX, this.chunkY, this.chunkZ, this.voxelData);
				this.saveDirty = false;
			}
		}

		// Lighting check
		// The lighting algorithm needs all chunks around this chunk to be ready.
		if (!ClientChunkRequest.areRequestsPending() && this.localLightingDirty && this.isInitialized() && (this.chunkY == this.master.chunksOnYAxis() || this.master.chunkLocalLightingReady(this.chunkX, this.chunkY + 1, this.chunkZ)))
		{
			this.recalculateLocalLighting();
			this.localLightingDirty = false;
		}
		
		if (!ClientChunkRequest.areRequestsPending() && this.globalLightingDirty && this.isInitialized() &&
			this.master.chunkLocalLightingReady(this.chunkX, this.chunkY + 1, this.chunkZ) && 
			this.master.chunkLocalLightingReady(this.chunkX, this.chunkY - 1, this.chunkZ) && 
			this.master.chunkLocalLightingReady(this.chunkX + 1, this.chunkY, this.chunkZ) && 
			this.master.chunkLocalLightingReady(this.chunkX - 1, this.chunkY, this.chunkZ) && 
			this.master.chunkLocalLightingReady(this.chunkX, this.chunkY, this.chunkZ + 1) &&
			this.master.chunkLocalLightingReady(this.chunkX, this.chunkY, this.chunkZ - 1))
		{
			this.recalculateGlobalLighting();
			
			// If global lighting finished, regenerate adjacent meshes.
			if (!this.globalLightingDirty)
				this.regenerateAdjacentMeshes();
		}

		synchronized (this.voxelDataLockObject)
		{
			this.tileEntityHandlersCopyInstance.clear();
			for (Entry<Vector3, IVoxelTileEntityHandler> entry : this.tileEntityHandlers.entrySet())
			{
				this.tileEntityHandlersCopyInstance.put(entry.getKey(), entry.getValue());
			}
			
			// Exec tile entity updates
			for (Entry<Vector3, IVoxelTileEntityHandler> entry : this.tileEntityHandlersCopyInstance.entrySet())
			{
				int x = (int) entry.getKey().x;
				int y = (int) entry.getKey().y;
				int z = (int) entry.getKey().z;

				Vector3i absolutePosition = this.getAbsoluteVoxelPosition((int) entry.getKey().x, (int) entry.getKey().y, (int) entry.getKey().z);

				VoxelData voxelData = this.getVoxel(x, y, z);

				entry.getValue().handleUpdate(voxelData, absolutePosition.x, absolutePosition.y, absolutePosition.z, this.master.isServer());
			}
		}

		boolean frameMismatch = (lastUpdateCallId != this.master.updateCallId);

		if (!ClientChunkRequest.areRequestsPending() && !this.localLightingDirty && !this.globalLightingDirty && this.voxelMeshDirty && this.generationDone && !this.master.isServer() &&
		// Check all neighbours if lighting is ready
				this.master.chunkLightingReady(this.chunkX + 1, this.chunkY, this.chunkZ) && this.master.chunkLightingReady(this.chunkX - 1, this.chunkY, this.chunkZ) && this.master.chunkLightingReady(this.chunkX, this.chunkY + 1, this.chunkZ) && this.master.chunkLightingReady(this.chunkX, this.chunkY - 1, this.chunkZ) && this.master.chunkLightingReady(this.chunkX, this.chunkY, this.chunkZ + 1) && this.master.chunkLightingReady(this.chunkX, this.chunkY, this.chunkZ - 1) && (CubicWorldConfiguration.meshGenerationsPerFrameLimit == -1 || frameMismatch || generationsProcessedThisFrame <= CubicWorldConfiguration.meshGenerationsPerFrameLimit))
		{
			// If the frame ids mismatch
			if (frameMismatch)
			{
				// Update frame id and reset counter
				lastUpdateCallId = this.master.updateCallId;
				generationsProcessedThisFrame = 0;
			}
			
			this.generateMesh();
			generationsProcessedThisFrame++;
		}
	}
	
	/**
	 * Temporary array list which will contain all voxels which depend on another voxel for lighting.
	 * Gets used in the global lighting pass to prevent searching ready voxels forever.
	 * @see VoxelChunk#recalculateGlobalLighting()
	 */
	private ArrayList<Vector3i> dependencyVoxelsTemporary;
	
	/**
	 * Calculates the global light for the given position and face of a voxel.
	 */
	private void recalculateGlobalLighting()
	{
		if (this.dependencyVoxelsTemporary == null)
			this.dependencyVoxelsTemporary = new ArrayList<Vector3i>();
		
		synchronized(this.voxelDataLockObject)
		{
			// Needed variables
			VoxelData v = null;
			boolean blocksLeft = false;
			// Vector3i absolutePos = new Vector3i();
			
			// Shadow pass
			// This will flood light into caves
			for (int x = 0; x < VoxelWorld.chunkWidth; x++)
				for (int z = 0; z < VoxelWorld.chunkDepth; z++)
					for (int y = VoxelWorld.chunkHeight-1; y >= 0; y--)
					{
						v = this.voxelData[x][y][z];
						
						// This function will only iterate over air or transparent blocks which are uninitialized.
						if (v.getBlockLightLevel() == -1 && (v.voxelType == null || v.voxelType.transparent))
						{
							Vector3i absolutePos =  this.getAbsoluteVoxelPosition(x, y, z);
							
							// Get all adjacent voxels
							VoxelData[] adjacentVoxels = new VoxelData[]
							{
								// Top Voxel
								(y == VoxelWorld.chunkHeight - 1) ? this.master.getVoxel(absolutePos.x, absolutePos.y+1, absolutePos.z) : this.voxelData[x][y+1][z],
								// Bottom Voxel
								(y == 0) ? this.master.getVoxel(absolutePos.x, absolutePos.y-1, absolutePos.z) : this.voxelData[x][y-1][z],
								// Left Voxel
								(x == 0) ? this.master.getVoxel(absolutePos.x-1, absolutePos.y, absolutePos.z) : this.voxelData[x-1][y][z],
								// Right Voxel
								(x == VoxelWorld.chunkWidth - 1) ? this.master.getVoxel(absolutePos.x+1, absolutePos.y, absolutePos.z) : this.voxelData[x+1][y][z],
								// Back Voxel
								(z == 0) ? this.master.getVoxel(absolutePos.x, absolutePos.y, absolutePos.z-1) : this.voxelData[x][y][z-1],
								// Front Voxel
								(z == VoxelWorld.chunkDepth - 1) ? this.master.getVoxel(absolutePos.x, absolutePos.y, absolutePos.z+1) : this.voxelData[x][y][z+1]
							};
							
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
							if (highestLightLevel <= -1)
							{
								this.dependencyVoxelsTemporary.add(absolutePos);
								System.out.println("FAIL!!! " + absolutePos);
								// ... we're done here!
								blocksLeft = true;
								continue;
							}
							
							// If there were blocks ready
							byte lightLevel = (byte) (highestLightLevel-1);
							
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
				this.globalLightingDirty = false;
			}
		}
	}
	

	/**
	 * Calculates the local light for the given position and face of a voxel.
	 */
	private void recalculateLocalLighting()
	{
		synchronized(this.voxelDataLockObject)
		{
			// Needed variables
			VoxelData v = null;
			Vector3i absolutePos = new Vector3i(0,0,0);

			// Clear pass
			// This clears the shadow and sun light level.
			for (int x = 0; x < VoxelWorld.chunkWidth; x++)
				for (int z = 0; z < VoxelWorld.chunkDepth; z++)
					for (int y = VoxelWorld.chunkHeight-1; y >= 0; y--)
					{
						v = this.voxelData[x][y][z];
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
					for (int y = VoxelWorld.chunkHeight-1; y >= 0; y--)
					{
						// Calculate the absolute position of the current voxel
						this.getAbsoluteVoxelPosition(x, y, z, absolutePos);
						v = this.voxelData[x][y][z];
						
						// If the current voxel is the most at the upper border of the world bounding
						// Set the sunlight level to it for propagating it down.
						if (absolutePos.y == this.master.worldHeight-1)
						{
							v.setSunLightLevel(this.master.getSunLightLevel());
						}
						// Upper chunk border propagation
						else if (y == VoxelWorld.chunkHeight - 1)
						{
							// Porpagate light downwards
							// On the upper chunk border the sun light level will be topLightLevel - 1 but minimum 0
							VoxelData topVoxel = this.master.getVoxel(absolutePos.x, absolutePos.y + 1, absolutePos.z);
							byte topLightLevel = (byte) (topVoxel == null ? this.master.getSunLightLevel() - ((this.master.chunksOnYAxis()-2)-this.chunkY) : topVoxel.getSunLightLevel());
							
							byte lightLevel = (byte) (topLightLevel-1);
							
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
							VoxelData topVoxel = this.master.getVoxel(absolutePos.x, absolutePos.y + 1, absolutePos.z);
							byte chunkLightLevel = (byte) (this.master.getSunLightLevel() - (this.master.chunksOnYAxis() - this.chunkY));
							
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
		}
	}

	/**
	 * Writes mesh data to the given lists.
	 * 
	 * @param vertices Main vertex list.
	 * @param indices Main index list.
	 * @param uvs Main uvs list.
	 * @param colors Main colors list.
	 * @param sideVertices Vertex array from the side vertices array.
	 * @param sideIndices Side indices from the side indices array.
	 * @param indicesCounter The current index counter.
	 * @param x The current voxel worldspace position.
	 * @param y The current voxel worldspace position.
	 * @param z The current voxel worldspace position.
	 * @param color The voxel color.
	 * @param blockId The voxel type id.
	 * @param face The foxel face to use for getting uv coordinates.
	 */
	private void WriteSideData(ArrayList<Float> vertices, ArrayList<Short> indices, Vector3[] sideVertices, Vector3[] sideNormals, short[] sideIndices, short indicesCounter, int x, int y, int z, VoxelData voxelData, VoxelFace face, byte lightLevel)
	{
		// short blockId = voxelData.voxelType.voxelId;

		Vector2[] uv = voxelData.getRenderState().getUvsForFace(face);

		// boolean transparent = VoxelEngine.getVoxelType(blockId).transparent;

		// Calculate absolute vertex index count.
		for (int i = 0; i < sideIndices.length; i++)
		{
			indices.add((short) (indicesCounter + sideIndices[i]));
		}

		float lightValue = lightLevel / (float) CubicWorldConfiguration.maxLightLevel;

		// Transform vertices based on the block's position.
		for (int i = 0; i < sideVertices.length; i++)
		{
			float vertX = sideVertices[i].x + x + ((float) this.chunkX * (float) VoxelWorld.chunkWidth);
			float vertY = sideVertices[i].y + y + ((float) this.chunkY * (float) VoxelWorld.chunkHeight);
			float vertZ = sideVertices[i].z + z + ((float) this.chunkZ * (float) VoxelWorld.chunkDepth);

			vertices.add(vertX);
			vertices.add(vertY);
			vertices.add(vertZ);
			vertices.add(uv[i].x);
			vertices.add(uv[i].y);
			vertices.add(lightValue);
		}
	}
}
