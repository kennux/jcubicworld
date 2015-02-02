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
import java.util.zip.DataFormatException;

import net.kennux.cubicworld.item.ItemSystem;
import net.kennux.cubicworld.item.ItemType;
import net.kennux.cubicworld.serialization.BitReader;
import net.kennux.cubicworld.serialization.BitWriter;
import net.kennux.cubicworld.util.CompressionUtils;
import net.kennux.cubicworld.util.ConsoleHelper;

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
	private Connection readerDatabaseConnection;

	private Connection writerDatabaseConnection;
	/**
	 * Locks the SQL connection.
	 */
	private Object readerConnectionLockObject = new Object();

	private Object writerConnectionLockObject = new Object();
	/**
	 * The queue of jobs waiting for writing.
	 */
	private LinkedList<AbstractMap.SimpleEntry<Vector3, VoxelData[][][]>> writerQueue;

	private Object writerQueueLock = new Object();

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

		Class.forName("org.sqlite.JDBC");
		SQLiteConfig config = new SQLiteConfig();
		config.setSharedCache(true);
		config.setSynchronous(SynchronousMode.FULL);

		this.readerDatabaseConnection = DriverManager.getConnection("jdbc:sqlite:" + saveFile.getAbsolutePath(), config.toProperties());
		this.writerDatabaseConnection = DriverManager.getConnection("jdbc:sqlite:" + saveFile.getAbsolutePath(), config.toProperties());
		this.writerQueue = new LinkedList<AbstractMap.SimpleEntry<Vector3, VoxelData[][][]>>();

		if (fileWasCreated)
		{
			// Initial database structure
			Statement statement = this.writerDatabaseConnection.createStatement();

			statement.execute("CREATE TABLE chunks\r\n" + "(\r\n" + "chunkX INT,\r\n" + "chunkY INT,\r\n" + "chunkZ INT,\r\n" + "chunkData BLOB,\r\n" + "PRIMARY KEY (chunkX, chunkY, chunkZ)\r\n" + ");\r\n" + "CREATE INDEX chunkIndex ON chunks (chunkX, chunkY, chunkZ);");
			statement.execute("CREATE TABLE `voxeltypes`\r\n (\r\n`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\r\n`name`\r\nTEXT NOT NULL UNIQUE\r\n);\r\nCREATE INDEX typeIndex ON `voxeltypes` (`id`)");
			statement.execute("CREATE TABLE `itemtypes`\r\n (\r\n`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\r\n`name`\r\nTEXT NOT NULL UNIQUE\r\n);\r\nCREATE INDEX typeIndex ON `itemtypes` (`id`)");
			
			// Write voxel and item types table
			VoxelType[] voxelTypes = VoxelEngine.getVoxelTypes();
			ItemType[] itemTypes = ItemSystem.getItemTypes();
			for (VoxelType type : voxelTypes)
			{
				statement.execute("INSERT INTO `voxeltypes` (`id`, `name`) VALUES ("+type.voxelId+", '"+type.voxelName+"');");
			}
			
			for (ItemType type : itemTypes)
			{
				statement.execute("INSERT INTO `itemtypes` (`id`, `name`) VALUES ("+type.getItemId()+", '"+type.getItemName()+"');");
			}
			
			statement.close();
		}
		
		// Match voxel and item types of the local instance against the ones stored in sqlite
		VoxelType[] voxelTypes = VoxelEngine.getVoxelTypes();
		ItemType[] itemTypes = ItemSystem.getItemTypes();
		Statement statement = this.readerDatabaseConnection.createStatement();
		
		// Query db for all types
		ResultSet voxelTypesResultSet = statement.executeQuery("SELECT * FROM voxeltypes");
		ResultSet itemTypesResultSet = statement.executeQuery("SELECT * FROM itemtypes");
		
		// Check voxel types
		while (voxelTypesResultSet.next())
		{
			boolean typeFoundAndCorrect = false;
			for (VoxelType type : voxelTypes)
			{
				if (type != null && type.voxelName.equals(voxelTypesResultSet.getString("name")))
				{
					if (type.voxelId != voxelTypesResultSet.getInt("id"))
					{
						ConsoleHelper.writeLog("ERROR", "Voxel id mismatch for type: " + type.voxelName, "WorldSave");
					}
					
					typeFoundAndCorrect = true;
				}
			}
			
			if (!typeFoundAndCorrect)
			{
				ConsoleHelper.writeLog("ERROR", "Save game voxel type info table doesnt match local table. Porting worlds is not implemented yet!", "WorldSave");
			}
		}
		
		// Check item types
		while (itemTypesResultSet.next())
		{
			boolean typeFoundAndCorrect = false;
			for (ItemType type : itemTypes)
			{
				if (type != null && type.getItemName().equals(voxelTypesResultSet.getString("name")))
				{
					if (type.getItemId() != voxelTypesResultSet.getInt("id"))
					{
						ConsoleHelper.writeLog("ERROR", "Item id mismatch for type: " + type.getItemName(), "WorldSave");
					}
					
					typeFoundAndCorrect = true;
				}
			}
			
			if (!typeFoundAndCorrect)
			{
				ConsoleHelper.writeLog("ERROR", "Save game item type info table doesnt match local table. Porting worlds is not implemented yet!", "WorldSave");
			}
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
		synchronized (this.writerConnectionLockObject)
		{
			// Create statement
			Statement statement = null;

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
					if (statement == null)
					{
						statement = this.writerDatabaseConnection.createStatement();
						statement.execute("BEGIN TRANSACTION");
					}

					// Prepare chunk data
					byte[] data = CompressionUtils.compress(writer.getPacket());
					String hexData = "x'" + bytesToHex(data) + "'";

					// Add insert or replace to batch
					statement.addBatch("INSERT OR REPLACE INTO chunks (chunkX, chunkY, chunkZ, chunkData) VALUES\r\n (" + chunkX + ",\r\n" + chunkY + ",\r\n" + chunkZ + ",\r\n" + hexData + "\r\n" + ");\r\n");

				}
				catch (Exception e1)
				{
					ConsoleHelper.writeLog("ERROR", "Error while flushing save jobs: ", "WorldSave");
					ConsoleHelper.logError(e1);
				}
			}

			try
			{
				// long startTime = System.currentTimeMillis();

				if (statement != null)
				{
					// Execute save statement
					statement.executeBatch();

					statement.execute("END TRANSACTION");

					// Close statement
					statement.close();
				}

				// System.out.println("DB Sync done in: " + (System.currentTimeMillis() - startTime) + " ms");
			}
			catch (Exception e)
			{
				ConsoleHelper.writeLog("ERROR", "Error while flushing save jobs: ", "WorldSave");
				ConsoleHelper.logError(e);
			}
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
		synchronized (this.readerConnectionLockObject)
		{
			try
			{
				// Create select statement
				Statement statement = this.readerDatabaseConnection.createStatement();

				// Get count
				ResultSet resultSet = statement.executeQuery("SELECT chunkX FROM chunks WHERE chunkX = '" + chunkX + "' AND chunkY = '" + chunkY + "' AND chunkZ = '" + chunkZ + "'");

				boolean ret = resultSet.next();

				statement.close();

				return ret;
			}
			catch (SQLException e)
			{
				ConsoleHelper.writeLog("ERROR", "Error while checking if a chunk exists: ", "WorldSave");
				ConsoleHelper.logError(e);
				return false;
			}
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
		synchronized (this.readerConnectionLockObject)
		{
			try
			{
				// Create select statement
				Statement statement = this.readerDatabaseConnection.createStatement();

				// Get count
				ResultSet resultSet = statement.executeQuery("SELECT chunkData FROM chunks WHERE chunkX = '" + chunkX + "' AND chunkY = '" + chunkY + "' AND chunkZ = '" + chunkZ + "'");

				// Get chunk data blob
				byte[] chunkData = resultSet.getBytes("chunkData");

				statement.close();

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
			catch (SQLException | IOException | DataFormatException e)
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
