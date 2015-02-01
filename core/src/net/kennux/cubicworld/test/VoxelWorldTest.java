package net.kennux.cubicworld.test;

import java.lang.reflect.Field;

import junit.framework.TestCase;
import net.kennux.cubicworld.CubicWorldServer;
import net.kennux.cubicworld.plugins.baseplugin.BasePlugin;
import net.kennux.cubicworld.voxel.RaycastHit;
import net.kennux.cubicworld.voxel.VoxelChunk;
import net.kennux.cubicworld.voxel.VoxelData;
import net.kennux.cubicworld.voxel.VoxelEngine;
import net.kennux.cubicworld.voxel.VoxelType;
import net.kennux.cubicworld.voxel.VoxelWorld;
import net.kennux.cubicworld.voxel.VoxelWorldSave;
import net.kennux.cubicworld.voxel.generator.AWorldGenerator;
import net.kennux.cubicworld.voxel.handlers.IVoxelDataUpdateHandler;
import net.kennux.cubicworld.voxel.handlers.IVoxelUpdateHandler;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.badlogic.gdx.math.Vector3;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VoxelWorldTest extends TestCase
{
	/**
	 * Creates a test world generator which will generate bedrock blocks from level 0-3.
	 * 
	 * @return
	 */
	private AWorldGenerator createTestWorldGenerator()
	{
		return new AWorldGenerator()
		{
			@Override
			public void GenerateWorld(int chunkX, int chunkY, int chunkZ, VoxelChunk chunk)
			{
				VoxelData[][][] voxelData = new VoxelData[VoxelWorld.chunkWidth][VoxelWorld.chunkHeight][VoxelWorld.chunkDepth];

				for (int x = 0; x < VoxelWorld.chunkWidth; x++)
				{
					for (int y = 0; y < VoxelWorld.chunkHeight; y++)
					{
						for (int z = 0; z < VoxelWorld.chunkDepth; z++)
						{
							// int absoluteX = x + (chunkX * VoxelWorld.chunkWidth);
							int absoluteY = y + (chunkY * VoxelWorld.chunkHeight);
							// int absoluteZ = z + (chunkZ * VoxelWorld.chunkDepth);

							if (absoluteY < 3)
							{
								voxelData[x][y][z] = VoxelData.construct(BasePlugin.voxelBedrockId);
							}
							else
							{
								voxelData[x][y][z] = null;
							}
						}
					}
				}

				chunk.setVoxelData(voxelData);
			}
		};
	}

	/**
	 * Tests world generation
	 */
	@Test
	public void testAChunkGeneration()
	{
		// Create server mock object
		CubicWorldServer serverInstance = EasyMock.createMock(CubicWorldServer.class);

		// Create voxel world object
		VoxelWorld voxelWorld = new VoxelWorld(serverInstance);

		// Init test world generator
		voxelWorld.setWorldGenerator(this.createTestWorldGenerator());

		// Generate 0|0|0 chunk
		voxelWorld.generateChunk(0, 0, 0, true);

		// Test if the chunk was generated correctly
		for (int x = 0; x < VoxelWorld.chunkWidth; x++)
			for (int z = 0; z < VoxelWorld.chunkDepth; z++)
			{
				for (int y = 0; y < 3; y++)
				{
					VoxelData voxelData = voxelWorld.getVoxel(x, y, z);
					assertNotNull(voxelData);
					assertEquals(voxelData.voxelType, VoxelEngine.getVoxelType(BasePlugin.voxelBedrockId));
				}

				for (int y = 3; y < VoxelWorld.chunkHeight; y++)
				{
					assertNull(voxelWorld.getVoxel(x, y, z));
				}
			}
	}

	/**
	 * Tests getting and setting voxels
	 */
	@Test
	public void testBVoxelGettingSetting()
	{
		// Create server mock object
		CubicWorldServer serverInstance = EasyMock.createMock(CubicWorldServer.class);

		// Create voxel world object
		VoxelWorld voxelWorld = new VoxelWorld(serverInstance);

		// Init test world generator
		voxelWorld.setWorldGenerator(this.createTestWorldGenerator());

		// Generate 0|0|0 chunk
		voxelWorld.generateChunk(0, 0, 0, true);

		// Test if the chunk was generated correctly
		voxelWorld.setVoxel(0, 4, 0, VoxelData.construct(BasePlugin.voxelDirtId));

		// Test normal setting
		VoxelData voxelData = voxelWorld.getVoxel(0, 4, 0);
		assertNotNull(voxelData);
		assertEquals(voxelData.voxelType, VoxelEngine.getVoxelType(BasePlugin.voxelDirtId));

		// Test getting null voxel
		voxelData = voxelWorld.getVoxel(0, 5, 0);
		assertNull(voxelData);

		// Test getting null voxel out of generated range
		voxelData = voxelWorld.getVoxel(100, 5, 0);
		assertNull(voxelData);

		// Test getting null voxel out of world range
		voxelData = voxelWorld.getVoxel(100, 2005, 0);
		assertNull(voxelData);

		// Test removing voxel
		voxelWorld.setVoxel(0, 4, 0, null);
		assertNull(voxelWorld.getVoxel(0, 4, 0));
	}

	/**
	 * Tests raycasting
	 */
	@Test
	public void testRaycasting()
	{
		// Create server mock object
		CubicWorldServer serverInstance = EasyMock.createMock(CubicWorldServer.class);

		// Create voxel world object
		VoxelWorld voxelWorld = new VoxelWorld(serverInstance);

		// Init test world generator
		voxelWorld.setWorldGenerator(this.createTestWorldGenerator());

		// Generate 0|0|0 chunk
		voxelWorld.generateChunk(0, 0, 0, true);

		// Execute raycast
		RaycastHit hit = voxelWorld.raycast(new Vector3(0, 5, 0), new Vector3(0, -1, 0), 2f);

		// Test if hit was successfull
		assertEquals(new Vector3(0, 2, 0), hit.hitVoxelPosition);

		// Execute raycast
		hit = voxelWorld.raycast(new Vector3(0, 5, 0), new Vector3(0, -1, 0), 1f);

		// Test if hit was unsucessfull
		assertNull(hit);

		// Execute raycast
		hit = voxelWorld.raycast(new Vector3(0, -2, 0), new Vector3(0, 1, 0), 2f);

		// Test if hit was unsucessfull
		assertEquals(new Vector3(0, 0, 0), hit.hitVoxelPosition);
	}

	/**
	 * Tests voxeldata update handlers
	 */
	@Test
	public void testVoxelDataUpdateHandler()
	{
		// Create server mock object
		CubicWorldServer serverInstance = EasyMock.createMock(CubicWorldServer.class);
		IVoxelDataUpdateHandler voxelDataUpdateHandler = EasyMock.createStrictMock(IVoxelDataUpdateHandler.class);

		// Create voxel world object
		VoxelWorld voxelWorld = new VoxelWorld(serverInstance);

		// Init test world generator
		voxelWorld.setWorldGenerator(this.createTestWorldGenerator());
		voxelWorld.setVoxelDataUpdateHandler(voxelDataUpdateHandler);

		// Generate 0|0|0 chunk
		voxelWorld.generateChunk(0, 0, 0, true);

		// Create the voxel data for testing
		VoxelData voxelData = VoxelData.construct(BasePlugin.voxelDirtId);

		// Record expected update handler behaviour
		voxelDataUpdateHandler.handleVoxelDataUpdate(0, 4, 0, voxelData);
		EasyMock.replay(voxelDataUpdateHandler);

		// Test if the chunk was generated correctly
		voxelWorld.setVoxel(0, 4, 0, voxelData);

		// Verify update handler
		EasyMock.verify(voxelDataUpdateHandler);
	}

	/**
	 * Tests saving and reading from the world file
	 * 
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	@Test
	public void testVoxelSave() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
	{
		// Create server mock object
		CubicWorldServer serverInstance = EasyMock.createMock(CubicWorldServer.class);
		VoxelWorldSave saveMock = EasyMock.createStrictMock(VoxelWorldSave.class);

		// Create voxel world object
		VoxelWorld voxelWorld = new VoxelWorld(serverInstance);

		// Init test world generator
		voxelWorld.setWorldGenerator(this.createTestWorldGenerator());
		voxelWorld.setWorldFile(saveMock);

		// Record save behaviour
		EasyMock.expect(saveMock.hasChunk(0, 0, 0)).andAnswer(new IAnswer<Boolean>()
		{
			@Override
			public Boolean answer() throws Throwable
			{
				return true;
			}
		}).times(2);

		// Returns a voxel data array with one dirt voxel at 0|0|0
		EasyMock.expect(saveMock.readChunk(0, 0, 0)).andAnswer(new IAnswer<VoxelData[][][]>()
		{
			@Override
			public VoxelData[][][] answer() throws Throwable
			{
				VoxelData[][][] voxelData = new VoxelData[VoxelWorld.chunkWidth][VoxelWorld.chunkHeight][VoxelWorld.chunkDepth];
				voxelData[0][0][0] = VoxelData.construct(BasePlugin.voxelDirtId);
				return voxelData;
			}
		});

		EasyMock.replay(saveMock);

		// Generate 0|0|0 chunk
		voxelWorld.generateChunk(0, 0, 0, true);

		assertNotNull(voxelWorld.getVoxel(0, 0, 0));
		assertEquals(voxelWorld.getVoxel(0, 0, 0).voxelType, VoxelEngine.getVoxelType(BasePlugin.voxelDirtId));

		// Verify save mock object calls
		EasyMock.verify(saveMock);

		// Reset save mock and prepare to test writing
		EasyMock.reset(saveMock);

		// Trigger save
		voxelWorld.setVoxel(0, 0, 0, voxelWorld.getVoxel(0, 0, 0));

		// Use java reflection to get the private voxel data
		Field voxelDataField = VoxelChunk.class.getDeclaredField("voxelData");
		voxelDataField.setAccessible(true);
		VoxelData[][][] expectedData = (VoxelData[][][]) voxelDataField.get(voxelWorld.getChunk(0, 0, 0, false));

		// Record expected behaviour
		saveMock.writeChunk(0, 0, 0, expectedData);
		EasyMock.replay(saveMock);

		// Execute save
		voxelWorld.update();

		// Verify save mock object calls
		EasyMock.verify(saveMock);
	}

	/**
	 * Tests voxel update handlers
	 */
	@Test
	public void testVoxelUpdateHandler()
	{
		// Create server mock object
		CubicWorldServer serverInstance = EasyMock.createMock(CubicWorldServer.class);
		IVoxelUpdateHandler voxelUpdateHandler = EasyMock.createStrictMock(IVoxelUpdateHandler.class);

		// Create voxel world object
		VoxelWorld voxelWorld = new VoxelWorld(serverInstance);

		// Init test world generator
		voxelWorld.setWorldGenerator(this.createTestWorldGenerator());

		// Set the update handler temporary
		VoxelType dirtType = VoxelEngine.getVoxelType(BasePlugin.voxelDirtId);
		dirtType.setUpdateHandler(voxelUpdateHandler);

		// Generate 0|0|0 chunk
		voxelWorld.generateChunk(0, 0, 0, true);

		// Create the voxel data for testing
		VoxelData voxelData = VoxelData.construct(BasePlugin.voxelDirtId);

		// Record expected update handler behaviour
		voxelUpdateHandler.handleUpdate(voxelData, 0, 4, 0, true);

		EasyMock.replay(voxelUpdateHandler);

		voxelWorld.setVoxel(0, 4, 0, voxelData);
		voxelWorld.update();

		// Verify update handler
		EasyMock.verify(voxelUpdateHandler);

		// Restart replay
		EasyMock.reset(voxelUpdateHandler);
		EasyMock.replay(voxelUpdateHandler);

		voxelData = VoxelData.construct(BasePlugin.voxelBedrockId);

		// Test if now the update handler wont get executed anymore
		voxelWorld.setVoxel(0, 4, 0, voxelData);
		voxelWorld.update();

		// Verify update handler
		EasyMock.verify(voxelUpdateHandler);

		// Remove update handler
		dirtType.setUpdateHandler(null);
	}
}
