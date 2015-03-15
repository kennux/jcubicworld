package net.kennux.cubicworld.networking;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import net.kennux.cubicworld.CubicWorldServer;
import net.kennux.cubicworld.entity.AEntity;
import net.kennux.cubicworld.entity.PlayerEntity;
import net.kennux.cubicworld.networking.packet.ClientChunkRequest;
import net.kennux.cubicworld.networking.packet.ClientLogin;
import net.kennux.cubicworld.networking.packet.ServerChunkData;
import net.kennux.cubicworld.serialization.BitWriter;
import net.kennux.cubicworld.util.ConsoleHelper;
import net.kennux.cubicworld.voxel.VoxelChunk;

/**
 * Cubic world server client. This class represents a single client connected to
 * the server.
 * 
 * @author KennuX
 *
 */
public class CubicWorldServerClient extends AClientSocket
{
	/**
	 * The master server instance.
	 */
	private CubicWorldServer master;

	/**
	 * Pending chunk requests.
	 */
	public ArrayList<ClientChunkRequest> chunkRequests;

	/**
	 * This client's player entity.
	 */
	public PlayerEntity playerEntity;

	/**
	 * Lists all entities the player knows about.
	 */
	public ArrayList<AEntity> knowsAboutList;

	/**
	 * The player's data file.
	 */
	private File playerFile;

	/**
	 * 
	 */
	private FileOutputStream playerFileOutputStream;

	/**
	 * The reference index from the server.clients array. Gets set in the
	 * constructor.
	 */
	private int clientIndex;
	
	/**
	 * Serverside only variable used to store all available roles.
	 */
	public String[] roles;

	public CubicWorldServerClient(CubicWorldServer master, Socket socket, int clientIndex) throws IOException
	{
		super(socket);

		// Set master ref
		this.master = master;

		// Init requests
		this.chunkRequests = new ArrayList<ClientChunkRequest>();

		// Init knows about
		this.knowsAboutList = new ArrayList<AEntity>();

		this.clientIndex = clientIndex;
	}

	/**
	 * Adds an entity to the knows about list. You have to send the
	 * ServerEntitySpawn packet yourself.
	 * 
	 * This function wont do anything if the given entity already exists or the
	 * player is not loggedin.
	 * 
	 * @param entity
	 */
	public void addEntityToKnowsAbout(AEntity entity)
	{
		if (this.isLoggedin() && !this.knowsAboutList.contains(entity))
			this.knowsAboutList.add(entity);
	}

	/**
	 * Checks if this player knows about the given entity.
	 * 
	 * If the player is not loggedin, this function will return false.
	 * 
	 * @param entity
	 */
	public boolean checkIfPlayerKnowsAbout(AEntity entity)
	{
		// Loggedin?
		if (this.playerEntity == null)
			return false;

		// Do we know about it?
		if (this.knowsAboutList.contains(entity))
		{
			return true;
		}

		return false;
	}

	public int getClientId()
	{
		return this.clientIndex;
	}

	@Override
	protected IPacketModel getPacketInstance(short packetId) throws InstantiationException, IllegalAccessException
	{
		return Protocol.getPacket(packetId);
	}

	/**
	 * Returns true if the player already sent the login packet and was loggedin
	 * successfully.
	 * 
	 * @return
	 */
	public boolean isLoggedin()
	{
		return this.playerEntity != null;
	}

	/**
	 * Removes the given entity from the knows about list.
	 * If the knowsabout list does not contain the given entity,
	 * this function will not do anything!
	 * 
	 * @param entity
	 */
	public void removeEntityFromKnowsAbout(AEntity entity)
	{
		if (this.isLoggedin() && this.knowsAboutList.contains(entity))
			this.knowsAboutList.remove(entity);
	}

	/**
	 * Saves this user's entity data.
	 * It will save inventory.
	 */
	public void saveUserInfo()
	{
		// Save user info
		try
		{
			// Prepare player file
			if (this.playerFile == null && this.playerEntity != null)
			{
				this.playerFile = new File(master.savePath + this.playerEntity.getSaveFilePath());

				if (!this.playerFile.exists())
					this.playerFile.createNewFile();
			}

			if (this.playerFile != null)
			{
				this.playerFileOutputStream = new FileOutputStream(this.playerFile);

				// Serialize player
				BitWriter bitWriter = new BitWriter();
				this.playerEntity.serializeFull(bitWriter);
				this.playerFileOutputStream.write(bitWriter.getPacket());
				this.playerFileOutputStream.close();
			}
		}
		catch (FileNotFoundException e)
		{
			ConsoleHelper.writeLog("ERROR", "Player data file not found: " + this.playerFile.getAbsolutePath(), "ServerClient");
			try
			{
				this.playerFile.createNewFile();
			}
			catch (IOException e1)
			{
				ConsoleHelper.writeLog("ERROR", "Cannot create player file: " + this.playerFile.getAbsolutePath(), "ServerClient");
				ConsoleHelper.logError(e);
				this.close();
			}
			ConsoleHelper.logError(e);
		}
		catch (IOException e)
		{
			ConsoleHelper.writeLog("ERROR", "Cannot write to player file: " + this.playerFile.getAbsolutePath(), "ServerClient");
			ConsoleHelper.logError(e);
		}
	}

	/**
	 * Updates this server client.
	 * Update order:
	 * 
	 * - Interpret all packets read by the reader thread.
	 * - Handle Chunk requests
	 * -> Send out chunk data if the chunk was successfully generated and remove
	 * the request
	 * 
	 */
	public void update()
	{
		super.update();
		while (this.hasPacket())
		{
			// Get next packet model
			IPacketModel packet = this.getPacket();

			if (packet == null)
				break;

			// Login check
			if (!(packet instanceof ClientLogin) && !this.isLoggedin())
			{
				ConsoleHelper.writeLog("info", "Got packet from not loggedin socket. Closing connection.", "ServerClient");
				this.close();
				return;
			}

			packet.interpretServerSide(this.master, this);
		}

		// Chunk requests pending?
		ArrayList<ClientChunkRequest> requestsHandled = new ArrayList<ClientChunkRequest>();

		for (ClientChunkRequest qr : this.chunkRequests)
		{
			VoxelChunk chunk = this.master.voxelWorld.getChunk(qr.chunkX, qr.chunkY, qr.chunkZ, false);

			// If we got the chunk, send it's data.
			if (chunk != null && chunk.isInitialized())
			{
				ServerChunkData chunkData = new ServerChunkData();

				chunkData.chunkX = qr.chunkX;
				chunkData.chunkY = qr.chunkY;
				chunkData.chunkZ = qr.chunkZ;
				chunkData.voxelData = chunk.getVoxelData();

				chunkData.setPlayerId(this.clientIndex);

				this.master.sendPacket(chunkData);
				requestsHandled.add(qr);
			}
			else
			{
				// Check if the chunk is already initialized
				if (!this.master.voxelWorld.isChunkInitialized(qr.chunkX, qr.chunkY, qr.chunkZ))
				{
					this.master.voxelWorld.generateChunk(qr.chunkX, qr.chunkY, qr.chunkZ, false);
				}
			}
		}

		// Remove handled requests
		for (ClientChunkRequest qr : requestsHandled)
		{
			this.chunkRequests.remove(qr);
		}
	}

	/**
	 * Returns the cubicworld server instance which owns this client.
	 * 
	 * @return
	 */
	public CubicWorldServer getMaster()
	{
		return this.master;
	}
}
