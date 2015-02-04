package net.kennux.cubicworld.voxel;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.zip.DataFormatException;

import net.kennux.cubicworld.item.ItemSystem;
import net.kennux.cubicworld.item.ItemType;
import net.kennux.cubicworld.serialization.BitReader;
import net.kennux.cubicworld.serialization.BitWriter;
import net.kennux.cubicworld.util.CompressionUtils;
import net.kennux.cubicworld.util.ConsoleHelper;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteConfig.SynchronousMode;

import com.badlogic.gdx.math.Vector3;

/**
 * <pre>
 * This class handles voxel world saving / loading.
 * Chunks will get packed to regions, which are single files in the world save.
 * Each region can hold regionWidth*regionHeight*worldChunkHeight chunks.
 * 
 * If a region got modified the modified chunk data will get compressed and written to the region file.
 * 
 * The saving of the actual world data will get handled in flushSave().
 * So writing data to the world with writeChunk(int,int,in) will enquene data in the save stack.
 * In flushSave they will get written to the harddisk.
 * 
 * flushSave() should get called in a fixed interval by the server.
 * 
 * This class is completely thread-safe.
 * </pre>
 * 
 * @author KennuX
 *
 */
public class VoxelWorldSave
{
	public static String bytesToHex(byte[] bytes)
	{
		char[] hexChars = new char[bytes.length * 2];

		for (int j = 0; j < bytes.length; j++)
		{
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}

		return new String(hexChars);
	}

	/**
	 * The database connection.
	 */
	private DB databaseConnection;

	/**
	 * Locks the connection.
	 */
	private Object connectionLockObject = new Object();

	/**
	 * The queue of jobs waiting for writing.
	 */
	private LinkedList<AbstractMap.SimpleEntry<Vector3, VoxelData[][][]>> writerQueue;

	private Object writerQueueLock = new Object();
	
	/**
	 * Gets initialized in constructor.
	 * Holds all chunk entry infos.
	 */
	private ConcurrentNavigableMap<ChunkKey, byte[]> chunkEntries;

	// Helper function
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	/**
	 * Opens or creates the table and data file.
	 * 
	 * @param tableFilePath
	 * @param dataFilePath
	 * @throws IOException
	 *             Gets thrown if creation or opening of the files failed.
	 */
	public VoxelWorldSave(String savePath) throws Exception
	{
		// Create save file if not existing
		boolean fileWasCreated = false;
		File saveFile = new File(savePath + "world.dat");

		if (!saveFile.exists())
		{
			saveFile.createNewFile();
			fileWasCreated = true;
		}
		
		this.writerQueue = new LinkedList<AbstractMap.SimpleEntry<Vector3, VoxelData[][][]>>();
		
		// Create db connection
		this.databaseConnection = DBMaker.newFileDB(saveFile).closeOnJvmShutdown().make();

		// Load data
		this.chunkEntries = this.databaseConnection.getTreeMap("chunks");
		
		// TODO voxel and item type check
		if (fileWasCreated)
		{
		}
	}

	/**
	 * Flushs all enquened save operations in the voxel world save.
	 */
	public void flushSave()
	{
		HashMap<Vector3, VoxelData[][][]> updateJobs = new HashMap<Vector3, VoxelData[][][]>();

		synchronized (this.writerQueueLock)
		{
			while (!this.writerQueue.isEmpty())
			{
				// Get job from queue
				AbstractMap.SimpleEntry<Vector3, VoxelData[][][]> queueEntry = this.writerQueue.poll();
				updateJobs.put(queueEntry.getKey(), queueEntry.getValue());
			}
		}

		// Write jobs to database
		synchronized (this.connectionLockObject)
		{
			//long startTime = System.currentTimeMillis();
			
			// Iterate through every update job.
			for (Entry<Vector3, VoxelData[][][]> e : updateJobs.entrySet())
			{
				// Get chunk position
				int chunkX = (int) e.getKey().x;
				int chunkY = (int) e.getKey().y;
				int chunkZ = (int) e.getKey().z;

				// Serialize & compress chunk data
				BitWriter writer = new BitWriter();
				VoxelData[][][] voxelData = e.getValue();

				for (int x = 0; x < VoxelWorld.chunkWidth; x++)
					for (int y = 0; y < VoxelWorld.chunkHeight; y++)
						for (int z = 0; z < VoxelWorld.chunkDepth; z++)
							VoxelData.serialize(voxelData[x][y][z], writer);

				try
				{
					// Prepare chunk data
					byte[] data = CompressionUtils.compress(writer.getPacket());
					
					this.chunkEntries.put(new ChunkKey(chunkX, chunkY, chunkZ), data);
				}
				catch (Exception e1)
				{
					ConsoleHelper.writeLog("ERROR", "Error while flushing save jobs: ", "WorldSave");
					ConsoleHelper.logError(e1);
				}
			}
			
			// Commit to db
			this.databaseConnection.commit();

			//System.out.println("DB Sync done in: " + (System.currentTimeMillis() - startTime) + " ms");
		}
	}

	/**
	 * Checks if the chunk for the given position exists in the save file.
	 * 
	 * @param chunkX
	 * @param chunkY
	 * @param chunkZ
	 * @return
	 */
	public boolean hasChunk(int chunkX, int chunkY, int chunkZ)
	{
		synchronized (this.connectionLockObject)
		{
			return this.chunkEntries.containsKey(new ChunkKey(chunkX, chunkY, chunkZ));
		}
	}

	/**
	 * Reads the chunk for the given position from the save file. Returns null
	 * if the chunk is not found.
	 * 
	 * @param chunkX
	 * @param chunkY
	 * @param chunkZ
	 * @return
	 */
	public VoxelData[][][] readChunk(int chunkX, int chunkY, int chunkZ)
	{
		synchronized (this.connectionLockObject)
		{
			try
			{
				// Get chunk data blob
				byte[] chunkData = this.chunkEntries.get(new ChunkKey(chunkX, chunkY, chunkZ));

				// Deserialize data
				byte[] decompressedChunkData = CompressionUtils.decompress(chunkData);

				// Read data
				BitReader reader = new BitReader(decompressedChunkData);

				VoxelData[][][] voxelData = new VoxelData[VoxelWorld.chunkWidth][VoxelWorld.chunkHeight][VoxelWorld.chunkDepth];
				for (int x = 0; x < VoxelWorld.chunkWidth; x++)
					for (int y = 0; y < VoxelWorld.chunkHeight; y++)
						for (int z = 0; z < VoxelWorld.chunkDepth; z++)
							voxelData[x][y][z] = VoxelData.deserialize(reader);

				return voxelData;
			}
			catch (IOException | DataFormatException e)
			{
				ConsoleHelper.writeLog("ERROR", "Error while reading chunk: ", "WorldSave");
				ConsoleHelper.logError(e);
				return null;
			}
		}
	}

	/**
	 * Writes the chunk for the given position to the save file.
	 * If the chunk already exists in the file it will get overwritten,
	 * otherwise it will get added.
	 * 
	 * @param chunkX
	 * @param chunkY
	 * @param chunkZ
	 * @return
	 */
	public void writeChunk(int chunkX, int chunkY, int chunkZ, VoxelData[][][] data)
	{
		synchronized (this.writerQueueLock)
		{
			// Create Writer entry
			AbstractMap.SimpleEntry<Vector3, VoxelData[][][]> queueEntry = new AbstractMap.SimpleEntry<Vector3, VoxelData[][][]>(new Vector3(chunkX, chunkY, chunkZ), data);
			this.writerQueue.add(queueEntry);
		}
	}
}
