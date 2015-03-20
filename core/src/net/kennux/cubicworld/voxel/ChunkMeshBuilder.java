package net.kennux.cubicworld.voxel;

import java.util.ArrayList;

import net.kennux.cubicworld.CubicWorldConfiguration;
import net.kennux.cubicworld.math.Vector3i;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

/**
 * This class contains static functions used for chunk mesh building and chunk data analyzation.
 * Analyzation means for example getting all visible tile entities in the current chunks as a list.
 * 
 * @author KennuX
 *
 */
public class ChunkMeshBuilder
{
	/**
	 * "Dataholder" class for chunk generation data.
	 * For example, this can hold the new mesh vertices, colors and so on.
	 * 
	 * @author KennuX
	 *
	 */
	public static class ChunkMeshBuilderResult
	{
		/**
		 * The voxel mesh's bounding box.
		 */
		private BoundingBox boundingBox;

		// New mesh data list
		// Gets generated in the update() function which gets called by an own
		// thread separated from the main thread.
		private float[] vertices;
		private short[] indices;

		/**
		 * The visible tile entity positions.
		 * Will get built in the generateMesh() function.
		 * This list is the "new" one which will get set to visibleTileEntities in createMesh().
		 */
		private ArrayList<Vector3i> visibleTileEntities = new ArrayList<Vector3i>();

		/**
		 * @return the boundingBox
		 */
		public BoundingBox getBoundingBox()
		{
			return boundingBox;
		}

		public float[] getVertices()
		{
			return this.vertices;
		}

		public short[] getIndices()
		{
			return this.indices;
		}

		public ArrayList<Vector3i> getVisibleTileEntities()
		{
			return this.visibleTileEntities;
		}
	}

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

	private static final int vertexSize = 6;

	public static ChunkMeshBuilderResult buildMeshData(VoxelChunk chunk)
	{
		// Create a local copy of the voxel chunk data
		VoxelData[][][] voxelData = chunk.getVoxelData();

		// Create the job data object
		ChunkMeshBuilderResult resultData = new ChunkMeshBuilderResult();

		// the vertices array list
		final int initListLength = 16000; // Start with a length of 16000 to avoid re-allocation
		ArrayList<Float> vertices = new ArrayList<Float>(initListLength * vertexSize);
		ArrayList<Short> indices = new ArrayList<Short>(initListLength);
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

					Vector3i absolutePos = chunk.getAbsoluteVoxelPosition(x, y, z);
					Vector3i localPos = new Vector3i(x, y, z);
					int absX = absolutePos.x;
					int absY = absolutePos.y;
					int absZ = absolutePos.z;

					VoxelData leftVoxel = (x == 0 ? chunk.master.getVoxel(absX - 1, absY, absZ) : voxelData[x - 1][y][z]);
					VoxelData rightVoxel = (x == VoxelWorld.chunkWidth - 1 ? chunk.master.getVoxel(absX + 1, absY, absZ) : voxelData[x + 1][y][z]);
					VoxelData topVoxel = (y == VoxelWorld.chunkHeight - 1 ? chunk.master.getVoxel(absX, absY + 1, absZ) : voxelData[x][y + 1][z]);
					VoxelData bottomVoxel = (y == 0 ? chunk.master.getVoxel(absX, absY - 1, absZ) : voxelData[x][y - 1][z]);
					VoxelData backVoxel = (z == 0 ? chunk.master.getVoxel(absX, absY, absZ - 1) : voxelData[x][y][z - 1]);
					VoxelData frontVoxel = (z == VoxelWorld.chunkDepth - 1 ? chunk.master.getVoxel(absX, absY, absZ + 1) : voxelData[x][y][z + 1]);

					boolean leftSideVisible = x != 0 ? (leftVoxel == null || leftVoxel.voxelType == null || leftVoxel.voxelType.voxelId < 0 || leftVoxel.voxelType.transparent) : true;
					boolean rightSideVisible = x != VoxelWorld.chunkWidth - 1 ? (rightVoxel == null || rightVoxel.voxelType == null || rightVoxel.voxelType.voxelId < 0 || rightVoxel.voxelType.transparent) : true;
					boolean topSideVisible = y != VoxelWorld.chunkHeight - 1 ? (topVoxel == null || topVoxel.voxelType == null || topVoxel.voxelType.voxelId < 0 || topVoxel.voxelType.transparent) : true;
					boolean bottomSideVisible = y != 0 ? (bottomVoxel == null || bottomVoxel.voxelType == null || bottomVoxel.voxelType.voxelId < 0 || bottomVoxel.voxelType.transparent) : true;
					boolean backSideVisible = z != 0 ? (backVoxel == null || backVoxel.voxelType == null || backVoxel.voxelType.voxelId < 0 || backVoxel.voxelType.transparent) : true;
					boolean frontSideVisible = z != VoxelWorld.chunkDepth - 1 ? (frontVoxel == null || frontVoxel.voxelType == null || frontVoxel.voxelType.voxelId < 0 || frontVoxel.voxelType.transparent) : true;

					// Model or normal voxel rendering?
					if (voxelData[x][y][z].voxelType.isTileEntity() &&
					// Atleast any side visible?
							(leftSideVisible || rightSideVisible || topSideVisible || bottomSideVisible || backSideVisible || frontSideVisible))
					{
						// Add to the visible list
						resultData.visibleTileEntities.add(localPos);
					}
					else
					{
						// Normal voxel rendering
						VoxelFace[] faceMappings = VoxelChunk.ROTATION_MAPPINGS[voxelData[x][y][z].rotation];

						byte leftLighting = leftVoxel == null ? 0 : leftVoxel.getLightLevel();
						byte rightLighting = rightVoxel == null ? 0 : rightVoxel.getLightLevel();
						byte topLighting = topVoxel == null ? 0 : topVoxel.getLightLevel();
						byte bottomLighting = bottomVoxel == null ? 0 : bottomVoxel.getLightLevel();
						byte backLighting = backVoxel == null ? 0 : backVoxel.getLightLevel();
						byte frontLighting = frontVoxel == null ? 0 : frontVoxel.getLightLevel();

						// Write mesh data
						if (leftSideVisible)
						{
							WriteSideData(vertices, indices, LEFT_SIDE_VERTICES, LEFT_SIDE_NORMALS, LEFT_SIDE_INDICES, indicesCounter, x, y, z, voxelData[x][y][z], faceMappings[0], leftLighting, chunk.getChunkX(), chunk.getChunkY(), chunk.getChunkZ());
							indicesCounter += LEFT_SIDE_VERTICES.length;
						}
						if (rightSideVisible)
						{
							WriteSideData(vertices, indices, RIGHT_SIDE_VERTICES, RIGHT_SIDE_NORMALS, RIGHT_SIDE_INDICES, indicesCounter, x, y, z, voxelData[x][y][z], faceMappings[1], rightLighting, chunk.getChunkX(), chunk.getChunkY(), chunk.getChunkZ());
							indicesCounter += RIGHT_SIDE_VERTICES.length;
						}
						if (topSideVisible)
						{
							WriteSideData(vertices, indices, TOP_SIDE_VERTICES, TOP_SIDE_NORMALS, TOP_SIDE_INDICES, indicesCounter, x, y, z, voxelData[x][y][z], faceMappings[2], topLighting, chunk.getChunkX(), chunk.getChunkY(), chunk.getChunkZ());
							indicesCounter += TOP_SIDE_VERTICES.length;
						}
						if (bottomSideVisible)
						{
							WriteSideData(vertices, indices, BOTTOM_SIDE_VERTICES, BOTTOM_SIDE_NORMALS, BOTTOM_SIDE_INDICES, indicesCounter, x, y, z, voxelData[x][y][z], faceMappings[3], bottomLighting, chunk.getChunkX(), chunk.getChunkY(), chunk.getChunkZ());
							indicesCounter += BOTTOM_SIDE_VERTICES.length;
						}
						if (backSideVisible)
						{
							WriteSideData(vertices, indices, BACK_SIDE_VERTICES, BACK_SIDE_NORMALS, BACK_SIDE_INDICES, indicesCounter, x, y, z, voxelData[x][y][z], faceMappings[4], backLighting, chunk.getChunkX(), chunk.getChunkY(), chunk.getChunkZ());
							indicesCounter += BACK_SIDE_VERTICES.length;
						}
						if (frontSideVisible)
						{
							WriteSideData(vertices, indices, FRON_SIDE_VERTICES, FRONT_SIDE_NORMALS, FRONT_SIDE_INDICES, indicesCounter, x, y, z, voxelData[x][y][z], faceMappings[5], frontLighting, chunk.getChunkX(), chunk.getChunkY(), chunk.getChunkZ());
							indicesCounter += FRON_SIDE_VERTICES.length;
						}
					}
				}
			}
		}

		// Set new models list and bounding box
		resultData.boundingBox = new BoundingBox(chunk.getAbsoluteVoxelPosition(0, 0, 0).toFloatVector(), chunk.getAbsoluteVoxelPosition(VoxelWorld.chunkWidth, VoxelWorld.chunkHeight, VoxelWorld.chunkDepth).toFloatVector());

		// Generate vertex data
		resultData.vertices = new float[vertices.size()];

		for (int i = 0; i < resultData.vertices.length; i++)
		{
			resultData.vertices[i] = vertices.get(i).floatValue();
		}

		// Build indices
		resultData.indices = new short[indices.size()];

		int i = 0;
		for (Short index : indices)
		{
			resultData.indices[i] = index.shortValue();
			i++;
		}

		return resultData;
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
	private static final void WriteSideData(ArrayList<Float> vertices, ArrayList<Short> indices, Vector3[] sideVertices, Vector3[] sideNormals, short[] sideIndices, short indicesCounter, int x, int y, int z, VoxelData voxelData, VoxelFace face, byte lightLevel, int chunkX, int chunkY, int chunkZ)
	{
		// short blockId = voxelData.voxelType.voxelId;

		Vector2[] uv = voxelData.voxelType.getUvsForFace(face);

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
			float vertX = sideVertices[i].x + x + ((float) chunkX * (float) VoxelWorld.chunkWidth);
			float vertY = sideVertices[i].y + y + ((float) chunkY * (float) VoxelWorld.chunkHeight);
			float vertZ = sideVertices[i].z + z + ((float) chunkZ * (float) VoxelWorld.chunkDepth);

			vertices.add(vertX);
			vertices.add(vertY);
			vertices.add(vertZ);
			vertices.add(uv[i].x);
			vertices.add(uv[i].y);
			vertices.add(lightValue);
		}
	}
}
