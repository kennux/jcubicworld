package net.kennux.cubicworld.voxel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import net.kennux.cubicworld.CubicWorld;
import net.kennux.cubicworld.CubicWorldConfiguration;
import net.kennux.cubicworld.inventory.IInventory;
import net.kennux.cubicworld.inventory.IInventoryUpdateHandler;
import net.kennux.cubicworld.networking.packet.ClientChunkRequest;
import net.kennux.cubicworld.networking.packet.inventory.ServerBlockInventoryUpdate;
import net.kennux.cubicworld.voxel.handlers.IVoxelUpdateHandler;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
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
import com.badlogic.gdx.utils.Disposable;

/**
 * Voxel chunk implementation.
 * 
 * @author KennuX
 *
 */
public class VoxelChunk implements Disposable
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
	public static final Vector3[] FACE_NORMALS = new Vector3[]
	{
		new Vector3(1,0,0),
		new Vector3(-1,0,0),
		new Vector3(0,1,0),
		new Vector3(0,-1,0),
		new Vector3(0,0,1),
		new Vector3(0,0,-1),
	};
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
	 * The mesh's lock object used for synchronizing mesh access.
	 */
	private Object meshLockObject = new Object();

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
	 * The voxel data (voxeltype id's, -1 for no block here). You must lock
	 * voxelDataLockObject when you are writing to this field. After direct
	 * writing to this you must call setDirty().
	 */
	private VoxelData[][][] voxelData;

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
	 * Gets set to true if the lighting information is up 2 date.
	 */
	private boolean lightingDirty = false;

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
	private HashMap<Vector3, IVoxelUpdateHandler> voxelUpdateHandlers = new HashMap<Vector3, IVoxelUpdateHandler>();

	private Object generationLockObject = new Object();

	// New mesh data list
	// Gets generated in the update() function which gets called by an own
	// thread separated from the main thread.
	private ArrayList<Vector3> newVertices = new ArrayList<Vector3>();
	private ArrayList<Vector3> newNormals = new ArrayList<Vector3>();
	private ArrayList<Vector2> newUvs = new ArrayList<Vector2>();
	private ArrayList<Short> newIndices = new ArrayList<Short>();
	private ArrayList<Color> newColors = new ArrayList<Color>();

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

		// Set chunk size.
		this.chunkX = chunkX;
		this.chunkY = chunkY;
		this.chunkZ = chunkZ;

		this.master = master;
	}

	/**
	 * Sets this chunk to dirty which will regenerate the mesh data.
	 * ONLY call this if you loaded the chunk data from the world file.
	 */
	public void chunkDataWasLoaded()
	{
		this.lightingDirty = true;
		this.voxelMeshDirty = true;
		this.saveDirty = false;
	}

	/**
	 * Sets this chunk to dirty which will regenerate the mesh data. You have to
	 * call this function after direct writing to voxelData.
	 */
	public void chunkDataWasModified()
	{
		this.lightingDirty = true;
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
			/*
			 * if (this.newVertices.size() == 0 && this.models.size() == 0)
			 * {
			 * // Free old data
			 * this.newColors = null;
			 * this.newVertices = null;
			 * this.newUvs = null;
			 * this.newIndices = null;
			 * this.newNormals = null;
			 * this.newModels = null;
			 * 
			 * this.newMeshDataReady = false;
			 * this.voxelMeshDirty = false;
			 * 
			 * return;
			 * }
			 */

			// Construct new mesh
			Mesh newMesh = new Mesh(true, this.newVertices.size(), this.newIndices.size(), new VertexAttribute(Usage.Position, 3, "v_Position"), new VertexAttribute(Usage.TextureCoordinates, 2, "v_Uv"), new VertexAttribute(Usage.Normal, 3, "v_Normal"), new VertexAttribute(Usage.ColorUnpacked, 4, "v_Light"));
			float[] vertexData = new float[this.newVertices.size() * 12];

			for (int i = 0; i < vertexData.length; i += 12)
			{
				// Calculate the vertex index
				int index = i / 12;

				// Vertex position and uv coordinates
				Vector3 position = this.newVertices.get(index);
				Vector3 normal = this.newNormals.get(index);
				Vector2 uv = this.newUvs.get(index);
				Color color = this.newColors.get(index);

				// Vertex data
				vertexData[i] = position.x;
				vertexData[i + 1] = position.y;
				vertexData[i + 2] = position.z;
				vertexData[i + 3] = uv.x;
				vertexData[i + 4] = uv.y;
				vertexData[i + 5] = normal.x;
				vertexData[i + 6] = normal.y;
				vertexData[i + 7] = normal.z;
				vertexData[i + 8] = color.r;
				vertexData[i + 9] = color.g;
				vertexData[i + 10] = color.b;
				vertexData[i + 11] = color.a;
			}

			// Build indices
			short[] indexArray = new short[this.newIndices.size()];

			int i = 0;
			for (Short index : this.newIndices)
			{
				indexArray[i] = index.shortValue();
				i++;
			}

			// Set mesh data
			newMesh.setVertices(vertexData);
			newMesh.setIndices(indexArray);

			// Calculate bounding box
			try
			{
				this.boundingBox = newMesh.calculateBoundingBox();
			}
			catch (Exception e)
			{
				// Happens if there are no vertices in this mesh, so just ignore this error.
			}
			this.newMeshDataReady = false;
			this.voxelMeshDirty = false;

			// Free old data
			this.newColors = null;
			this.newVertices = null;
			this.newUvs = null;
			this.newIndices = null;
			this.newNormals = null;

			synchronized (this.meshLockObject)
			{
				// Dispose old mesh if available
				if (this.voxelMesh != null)
					this.voxelMesh.dispose();

				this.models = this.newModels;
				this.voxelMesh = newMesh;
			}
		}
	}

	@Override
	public void dispose()
	{
		// Dispose mesh
		synchronized (this.meshLockObject)
		{
			if (this.voxelMesh != null)
				this.voxelMesh.dispose();
		}

		synchronized (this.voxelDataLockObject)
		{
			this.voxelData = null;
		}
	}

	/**
	 * Generates the voxel mesh based on the current chunk's voxel data.
	 */
	private void generateMesh()
	{
		synchronized (this.generationLockObject)
		{
			if (this.lightingDirty)
				return;

			// the vertices array list
			ArrayList<Vector3> vertices = new ArrayList<Vector3>();
			ArrayList<Vector3> normals = new ArrayList<Vector3>();
			ArrayList<Vector2> uvs = new ArrayList<Vector2>();
			ArrayList<Short> indices = new ArrayList<Short>();
			ArrayList<Color> colors = new ArrayList<Color>();
			ArrayList<ModelInstance> modelList = new ArrayList<ModelInstance>();
			short indicesCounter = 0;

			synchronized (this.voxelDataLockObject)
			{
				for (int x = 0; x < VoxelWorld.chunkWidth; x++)
				{
					for (int z = 0; z < VoxelWorld.chunkDepth; z++)
					{
						for (int y = 0; y < VoxelWorld.chunkHeight; y++)
						{
							// Voxel in my position?
							if (voxelData[x][y][z] == null || voxelData[x][y][z].voxelType == null)
								continue;

							// Model or normal voxel rendering?
							if (this.voxelData[x][y][z].voxelType.isModelRendering() && (
							// Atleast any side visible?
									(x == 0 || (voxelData[x - 1][y][z] == null || voxelData[x - 1][y][z].voxelType.voxelId < 0 || voxelData[x - 1][y][z].voxelType.transparent)) || (x == VoxelWorld.chunkWidth - 1 || ((voxelData[x + 1][y][z] == null || voxelData[x + 1][y][z].voxelType.voxelId < 0 || voxelData[x + 1][y][z].voxelType.transparent))) || (y == VoxelWorld.chunkHeight - 1 || ((voxelData[x][y + 1][z] == null || voxelData[x][y + 1][z].voxelType.voxelId < 0 || voxelData[x][y + 1][z].voxelType.transparent))) || (y == 0 || (voxelData[x][y - 1][z] == null || voxelData[x][y - 1][z].voxelType.voxelId < 0 || voxelData[x][y - 1][z].voxelType.transparent)) || (z == 0 || (voxelData[x][y][z - 1] == null || voxelData[x][y][z - 1].voxelType.voxelId < 0 || voxelData[x][y][z - 1].voxelType.transparent)) || (z == VoxelWorld.chunkDepth - 1 || ((voxelData[x][y][z + 1] == null || voxelData[x][y][z + 1].voxelType.voxelId < 0 || voxelData[x][y][z + 1].voxelType.transparent)))))
							{
								// Model rendering
								Model m = this.voxelData[x][y][z].voxelType.getModel();
								ModelInstance mInstance = new ModelInstance(m);

								Vector3 vert = new Vector3(x, y, z);
								vert.x += 0.5f + ((float) this.chunkX * (float) VoxelWorld.chunkWidth);
								vert.y += 0.5f + ((float) this.chunkY * (float) VoxelWorld.chunkHeight);
								vert.z += 0.5f + ((float) this.chunkZ * (float) VoxelWorld.chunkDepth);

								// Set model transformation
								mInstance.transform.set(vert, VoxelChunk.rotationTransformMappings[this.voxelData[x][y][z].rotation]);
								modelList.add(mInstance);
							}
							else
							{
								// Normal voxel rendering
								VoxelFace[] faceMappings = ROTATION_MAPPINGS[voxelData[x][y][z].rotation];
								Vector3 absolutePos = this.getAbsoluteBlockPosition(x, y, z);
								int absX = (int)absolutePos.x;
								int absY = (int)absolutePos.y;
								int absZ = (int)absolutePos.z;
								
								// Check which sites are visible and which not.
								boolean leftSideVisible = x == 0 || (voxelData[x - 1][y][z] == null || voxelData[x - 1][y][z].voxelType == null || voxelData[x - 1][y][z].voxelType.voxelId < 0 || voxelData[x - 1][y][z].voxelType.transparent);
								boolean rightSideVisible = x == VoxelWorld.chunkWidth - 1 || ((voxelData[x + 1][y][z] == null || voxelData[x + 1][y][z].voxelType == null || voxelData[x + 1][y][z].voxelType.voxelId < 0 || voxelData[x + 1][y][z].voxelType.transparent));
								boolean topSideVisible = y == VoxelWorld.chunkHeight - 1 || ((voxelData[x][y + 1][z] == null || voxelData[x][y + 1][z].voxelType == null || voxelData[x][y + 1][z].voxelType.voxelId < 0 || voxelData[x][y + 1][z].voxelType.transparent));
								boolean bottomSideVisible = y == 0 || (voxelData[x][y - 1][z] == null || voxelData[x][y - 1][z].voxelType == null || voxelData[x][y - 1][z].voxelType.voxelId < 0 || voxelData[x][y - 1][z].voxelType.transparent);
								boolean backSideVisible = z == 0 || (voxelData[x][y][z - 1] == null || voxelData[x][y][z - 1].voxelType == null || voxelData[x][y][z - 1].voxelType.voxelId < 0 || voxelData[x][y][z - 1].voxelType.transparent);
								boolean frontSideVisible = z == VoxelWorld.chunkDepth - 1 || ((voxelData[x][y][z + 1] == null || this.voxelData[x][y][z+1].voxelType == null || voxelData[x][y][z + 1].voxelType.voxelId < 0 || voxelData[x][y][z + 1].voxelType.transparent));
								
								// Variables for lighting
								byte leftLighting = (leftSideVisible ? this.master.getLightLevel(absX+1, absY, absZ) : -1);
								byte rightLighting = (rightSideVisible ? this.master.getLightLevel(absX-1, absY, absZ) : -1);
								byte topLighting = (topSideVisible ? this.master.getLightLevel(absX, absY+1, absZ) : -1);
								byte bottomLighting = (bottomSideVisible ? this.master.getLightLevel(absX, absY-1, absZ) : -1);
								byte backLighting = (backSideVisible ? this.master.getLightLevel(absX, absY, absZ-1) : -1);
								byte frontLighting = (frontSideVisible ? this.master.getLightLevel(absX, absY, absZ+1) : -1);
								
								// Write mesh data
								if (leftSideVisible)
								{
									this.WriteSideData(vertices, indices, uvs, colors, normals, LEFT_SIDE_VERTICES, LEFT_SIDE_NORMALS, LEFT_SIDE_INDICES, indicesCounter, x, y, z, voxelData[x][y][z], faceMappings[0], leftLighting);
									indicesCounter += LEFT_SIDE_VERTICES.length;
								}
								if (rightSideVisible)
								{
									this.WriteSideData(vertices, indices, uvs, colors, normals, RIGHT_SIDE_VERTICES, RIGHT_SIDE_NORMALS, RIGHT_SIDE_INDICES, indicesCounter, x, y, z, voxelData[x][y][z], faceMappings[1], rightLighting);
									indicesCounter += RIGHT_SIDE_VERTICES.length;
								}
								if (topSideVisible)
								{
									this.WriteSideData(vertices, indices, uvs, colors, normals, TOP_SIDE_VERTICES, TOP_SIDE_NORMALS, TOP_SIDE_INDICES, indicesCounter, x, y, z, voxelData[x][y][z], faceMappings[2], topLighting);
									indicesCounter += TOP_SIDE_VERTICES.length;
								}
								if (bottomSideVisible)
								{
									this.WriteSideData(vertices, indices, uvs, colors, normals, BOTTOM_SIDE_VERTICES, BOTTOM_SIDE_NORMALS, BOTTOM_SIDE_INDICES, indicesCounter, x, y, z, voxelData[x][y][z], faceMappings[3], bottomLighting);
									indicesCounter += BOTTOM_SIDE_VERTICES.length;
								}
								if (backSideVisible)
								{
									this.WriteSideData(vertices, indices, uvs, colors, normals, BACK_SIDE_VERTICES, BACK_SIDE_NORMALS, BACK_SIDE_INDICES, indicesCounter, x, y, z, voxelData[x][y][z], faceMappings[4], backLighting);
									indicesCounter += BACK_SIDE_VERTICES.length;
								}
								if (frontSideVisible)
								{
									this.WriteSideData(vertices, indices, uvs, colors, normals, FRON_SIDE_VERTICES, FRONT_SIDE_NORMALS, FRONT_SIDE_INDICES, indicesCounter, x, y, z, voxelData[x][y][z], faceMappings[5], frontLighting);
									indicesCounter += FRON_SIDE_VERTICES.length;
								}
							}
						}
					}
				}
			}

			this.newVertices = vertices;
			this.newNormals = normals;
			this.newUvs = uvs;
			this.newColors = colors;
			this.newIndices = indices;
			this.newModels = modelList;

			this.newMeshDataReady = true;
		}
	}

	public Vector3 getAbsoluteBlockPosition(int x, int y, int z)
	{
		return new Vector3(x + (this.chunkX * VoxelWorld.chunkWidth), y + (this.chunkY * VoxelWorld.chunkHeight), z + (this.chunkZ * VoxelWorld.chunkDepth));
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
	 * Returns the light level of the block at the given position.
	 * Returns -1 if there is no voxel in the given position or if an error
	 * happend.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public byte getLightLevel(int x, int y, int z)
	{
		// Bounds check
		if (this.voxelData != null && x >= 0 && y >= 0 && z >= 0 && x < VoxelWorld.chunkWidth && y < VoxelWorld.chunkHeight && z < VoxelWorld.chunkDepth)
		{
			synchronized (this.voxelDataLockObject)
			{
				if (this.voxelData[x][y][z] != null)
				return this.voxelData[x][y][z].lightLevel;
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
	 * @return
	 */
	public boolean isReadyForRendering()
	{
		return this.isInitializedAndLightingReady() && !this.voxelMeshDirty && this.voxelMesh != null;
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
	 * @return
	 */
	public boolean isInitializedAndLightingReady()
	{
		return this.isInitialized() && !this.lightingDirty;
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
				this.setVoxelUpdateHandlerAll();
			}
		}
	}

	/**
	 * Calculates an absolute position from a local blockspace position.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public Vector3 localToAbsolutePosition(int x, int y, int z)
	{
		return new Vector3((this.chunkX * VoxelWorld.chunkWidth) + x, (this.chunkY * VoxelWorld.chunkHeight) + y, (this.chunkZ * VoxelWorld.chunkDepth) + z);
	}

	/**
	 * The last frame id when render() was called.
	 * Used to limit chunk updates per frame for lag reduction.
	 */
	private static long lastRenderFrameId = -1;

	/**
	 * Contains the number of createNewMesh() calls this frame.
	 */
	private static int creationProcessedThisFrame = -1;
	
	/**
	 * Contains the number of generateMesh() calls this frame.
	 */
	private static int generationsProcessedThisFrame = -1;
	
	/**
	 * Renders this chunk. Will do nothing if the current voxel mesh is not
	 * available yet.
	 * 
	 * Otherwise it will just call the render() method of the chunk mesh object.
	 */
	public void render(Camera cam, ShaderProgram shader)
	{
		boolean frameMismatch = lastRenderFrameId != Gdx.graphics.getFrameId();
		
		if (this.voxelMeshDirty && this.newMeshDataReady && (CubicWorldConfiguration.meshCreationPerFrameLimit == -1 || lastRenderFrameId == -1 || 
				frameMismatch || creationProcessedThisFrame <= CubicWorldConfiguration.meshCreationPerFrameLimit))
		{
			// If the frame ids mismatch
			if (frameMismatch)
			{
				// Update frame id and reset counter
				lastRenderFrameId = Gdx.graphics.getFrameId();
				creationProcessedThisFrame = 0;
			}
			
			this.createNewMesh();
			creationProcessedThisFrame++;
			
		}

		synchronized (this.meshLockObject)
		{
			if (this.voxelMesh != null && this.boundingBox != null && cam.frustum.boundsInFrustum(this.boundingBox))
			{
				synchronized (this.meshLockObject)
				{
					// Render chunk mesh
					this.voxelMesh.render(shader, GL20.GL_TRIANGLES);
				}
			}
		}
	}

	/**
	 * Renders all voxels on this chunk which are model rendered.
	 * This is part of the second rendering pass of the voxel world.
	 * 
	 * 
	 * @param modelBatch
	 */
	public void renderModels(Camera cam, ModelBatch modelBatch)
	{
		synchronized (this.meshLockObject)
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

					CubicWorld.getServer().addPacket(updatePacket);
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
						Vector3 absolutePos = this.getAbsoluteBlockPosition(x, y, z);
						this.setInventoryUpdateHandler((int) absolutePos.x, (int) absolutePos.y, (int) absolutePos.z, this.voxelData[x][y][z]);
					}
	}

	/**
	 * Sets the voxel data at the given x|y|z position.
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

			// Remove update handler if existing
			Vector3 voxelPos = new Vector3(x, y, z);
			this.voxelUpdateHandlers.remove(voxelPos);

			this.voxelData[x][y][z] = voxel;

			// Inventory
			if (voxel != null && voxel.blockInventory != null)
			{
				Vector3 absolutePos = this.getAbsoluteBlockPosition(x, y, z);
				this.setInventoryUpdateHandler((int) absolutePos.x, (int) absolutePos.y, (int) absolutePos.z, voxel);
			}

			this.chunkDataWasModified();

			// notify about update
			if (this.master.getVoxelDataUpdateHandler() != null)
			{
				Vector3 absolutePos = this.localToAbsolutePosition(x, y, z);
				this.master.getVoxelDataUpdateHandler().handleVoxelDataUpdate((int) absolutePos.x, (int) absolutePos.y, (int) absolutePos.z, voxel);
			}

			// Update voxel handlers map
			if (voxel != null)
			{
				IVoxelUpdateHandler updateHandler = voxel.voxelType.getUpdateHandler();
				if (updateHandler != null)
				{
					this.voxelUpdateHandlers.put(voxelPos, updateHandler);
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
			this.voxelData = voxelData;
			this.setInventoryUpdateHandlerAll();
			this.chunkDataWasModified();
			this.setGenerationDone(true);
			this.setVoxelUpdateHandlerAll();
		}
	}

	/**
	 * Iterates through every voxel data in this instance and collects all update handler.
	 */
	private void setVoxelUpdateHandlerAll()
	{
		synchronized (this.voxelDataLockObject)
		{
			this.voxelUpdateHandlers.clear();

			for (int x = 0; x < this.voxelData.length; x++)
				for (int y = 0; y < this.voxelData[x].length; y++)
					for (int z = 0; z < this.voxelData[x][y].length; z++)
					{
						if (this.voxelData[x][y][z] != null && this.voxelData[x][y][z].voxelType != null)
						{
							// Update voxel handlers map
							IVoxelUpdateHandler updateHandler = this.voxelData[x][y][z].voxelType.getUpdateHandler();
							if (updateHandler != null)
							{
								this.voxelUpdateHandlers.put(new Vector3(x, y, z), updateHandler);
							}
						}
					}
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
		if (this.lightingDirty && this.isInitialized() &&
				(this.chunkY == this.master.chunksOnYAxis() || this.master.chunkLightingReady(new ChunkKey(this.chunkX, this.chunkY+1, this.chunkZ))))
		{
			this.recalculateLighting();
		}

		synchronized (this.voxelDataLockObject)
		{
			HashMap<Vector3, IVoxelUpdateHandler> updateHandlersClone = (HashMap<Vector3, IVoxelUpdateHandler>) this.voxelUpdateHandlers.clone();

			// Exec voxel updates
			for (Entry<Vector3, IVoxelUpdateHandler> entry : updateHandlersClone.entrySet())
			{
				int x = (int) entry.getKey().x;
				int y = (int) entry.getKey().y;
				int z = (int) entry.getKey().z;

				Vector3 absolutePosition = this.getAbsoluteBlockPosition((int) entry.getKey().x, (int) entry.getKey().y, (int) entry.getKey().z);

				int absoluteX = (int) absolutePosition.x;
				int absoluteY = (int) absolutePosition.y;
				int absoluteZ = (int) absolutePosition.z;
				
				VoxelData voxelData = this.getVoxel(x, y, z);
				
				entry.getValue().handleUpdate(voxelData, absoluteX, absoluteY, absoluteZ, this.master.isServer(), voxelData.dataModel);
			}
			
			boolean frameMismatch = (lastRenderFrameId != this.master.updateCallId);
			
			if (!ClientChunkRequest.areRequestsPending() && !this.lightingDirty && this.voxelMeshDirty && this.generationDone && !this.master.isServer() && 
					(CubicWorldConfiguration.meshGenerationsPerFrameLimit == -1 || lastRenderFrameId == -1 || 
					frameMismatch || generationsProcessedThisFrame <= CubicWorldConfiguration.meshGenerationsPerFrameLimit))
			{
				// If the frame ids mismatch
				if (frameMismatch)
				{
					// Update frame id and reset counter
					lastRenderFrameId = this.master.updateCallId;
					generationsProcessedThisFrame = 0;
				}
				
				this.generateMesh();
				generationsProcessedThisFrame++;
				
			}
		}
	}
	
	/**
	 * Calculates the light for the given position and face of a voxel.
	 */
	private void recalculateLighting()
	{
		synchronized (this.voxelDataLockObject)
		{
			for (int x = VoxelWorld.chunkWidth-1; x >= 0; x--)
				for (int y = VoxelWorld.chunkHeight-1; y >= 0; y--)
					for (int z = VoxelWorld.chunkDepth-1; z >= 0; z--)
						if (this.voxelData[x][y][z] != null)
						{
							VoxelData v = this.voxelData[x][y][z];
							Vector3 absolutePos = this.getAbsoluteBlockPosition(x, y, z);
							int absX = (int)absolutePos.x;
							int absY = (int)absolutePos.y;
							int absZ = (int)absolutePos.z;
							
							if (absY == this.master.worldHeight-1)
							{
								v.lightLevel = CubicWorldConfiguration.maxLightLevel;
							}
							else
							{
								byte topLightLevel = this.master.getLightLevel(absX, absY+1, absZ);
								
								// Forward light through air and transparent blocks
								if (v.voxelType == null || v.voxelType.transparent)
									v.lightLevel = topLightLevel;
								else
								{
									// Divide light by 2 if this is a solid block
									v.lightLevel = (byte) ((float)topLightLevel / 1.25f);
								}
							}
							
						}
			
			this.lightingDirty = false;
		}
	}

	/**
	 * Writes mesh data to the given lists.
	 * 
	 * @param vertices
	 *            Main vertex list.
	 * @param indices
	 *            Main index list.
	 * @param uvs
	 *            Main uvs list.
	 * @param colors
	 *            Main colors list.
	 * @param sideVertices
	 *            Vertex array from the side vertices array.
	 * @param sideIndices
	 *            Side indices from the side indices array.
	 * @param indicesCounter
	 *            The current index counter.
	 * @param x
	 *            The current voxel worldspace position.
	 * @param y
	 *            The current voxel worldspace position.
	 * @param z
	 *            The current voxel worldspace position.
	 * @param color
	 *            The voxel color.
	 * @param blockId
	 *            The voxel type id.
	 * @param face
	 *            The foxel face to use for getting uv coordinates.
	 */
	private void WriteSideData(ArrayList<Vector3> vertices, ArrayList<Short> indices, ArrayList<Vector2> uvs, ArrayList<Color> colors, ArrayList<Vector3> normals, Vector3[] sideVertices, Vector3[] sideNormals, short[] sideIndices, short indicesCounter, int x, int y, int z, VoxelData voxelData, VoxelFace face, byte lightLevel)
	{
		short blockId = voxelData.voxelType.voxelId;

		Vector2[] uv = voxelData.getRenderState().getUvsForFace(face);

		@SuppressWarnings("unused")
		boolean transparent = VoxelEngine.getVoxelType(blockId).transparent;

		// Calculate absolute vertex index count.
		for (int i = 0; i < sideIndices.length; i++)
		{
			indices.add((short) (indicesCounter + sideIndices[i]));
		}

		float lightValue = lightLevel / (float) CubicWorldConfiguration.maxLightLevel;
		Color color = new Color(lightValue, lightValue, lightValue, 1);

		// Transform vertices based on the block's position.
		for (int i = 0; i < sideVertices.length; i++)
		{
			Vector3 vert = new Vector3(sideVertices[i].x, sideVertices[i].y, sideVertices[i].z);
			vert.x += x + ((float) this.chunkX * (float) VoxelWorld.chunkWidth);
			vert.y += y + ((float) this.chunkY * (float) VoxelWorld.chunkHeight);
			vert.z += z + ((float) this.chunkZ * (float) VoxelWorld.chunkDepth);

			normals.add(sideNormals[i]);
			vertices.add(vert);
			colors.add(color);
			uvs.add(uv[i]);
		}
	}
}
