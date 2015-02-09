package net.kennux.cubicworld.networking;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import net.kennux.cubicworld.CubicWorldConfiguration;
import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.networking.packet.ClientChunkRequest;
import net.kennux.cubicworld.networking.packet.ClientLogin;
import net.kennux.cubicworld.networking.packet.ClientPlayerUpdate;
import net.kennux.cubicworld.networking.packet.ServerChunkData;
import net.kennux.cubicworld.networking.packet.ServerPlayerSpawn;
import net.kennux.cubicworld.networking.packet.ServerTimeUpdate;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;

/**
 * Client socket for connecting to a cubic world server.
 * 
 * @author KennuX
 *
 */
public class CubicWorldClient extends AClientSocket
{
	/**
	 * The cubic world game instance.
	 */
	private CubicWorldGame cubicWorld;

	public CubicWorldClient(CubicWorldGame master, String server, short port) throws UnknownHostException, IOException
	{
		super(new Socket(server, port));

		this.cubicWorld = master;

		// Send login
		ClientLogin login = new ClientLogin();
		login.username = "Username";
		this.sendPacket(login);

		// Await spawn packet
		while (true)
		{
			try
			{
				// Spawn packet?
				IPacketModel packet = this.getPacket();

				if (packet != null && packet instanceof ServerPlayerSpawn)
				{
					packet.interpretClientSide(master);
					break;
				}

				Thread.sleep(10);
			}
			catch (InterruptedException e)
			{

			}
		}
	}

	@Override
	protected IPacketModel getPacketInstance(short packetId) throws InstantiationException, IllegalAccessException
	{
		return Protocol.getPacket(packetId);
	}

	public void sendPacket(IPacketModel packet)
	{
		super.sendPacket(packet);
	}

	/**
	 * Iterates through all packets in processing queue but will only process ServerChunkData packets
	 */
	public void waitForChunkPackets()
	{
		ArrayList<IPacketModel> packetsList = new ArrayList<IPacketModel>();

		while (this.hasPacket())
		{
			// Get next packet model
			IPacketModel packet = this.getPacket();

			if (packet == null)
				break;

			packetsList.add(packet);
		}

		for (IPacketModel packet : packetsList)
		{
			if (packet instanceof ServerChunkData || packet instanceof ServerTimeUpdate)
			{
				packet.interpretClientSide(this.cubicWorld);
			}
			else
			{
				synchronized (this.packetsRecievedLockObject)
				{
					this.packetsRecieved.add(packet);
				}
			}
		}
	}

	/**
	 * Call this every tick / frame. It will read data from the socket. And
	 * interpret updates.
	 * 
	 * @params playerPosition The Player position used for chunk requesting.
	 * @throws IOException
	 */
	public void update(Vector3 playerPosition)
	{
		super.update();

		// Make sure all chunks are loaded every 60th frame or in the init() method which has frameid -1
		if (Gdx.graphics.getFrameId() % 60 == 0 || Gdx.graphics.getFrameId() == -1)
		{
			Vector3[] chunksToRequest = this.cubicWorld.voxelWorld.getNeededChunks(playerPosition, CubicWorldConfiguration.chunkLoadDistance, true);

			for (Vector3 chunkPos : chunksToRequest)
			{
				// Prevent double-requesting
				if (!ClientChunkRequest.isWaitingFor(chunkPos))
				{
					ClientChunkRequest chunkRequest = new ClientChunkRequest();
					chunkRequest.chunkX = (int) chunkPos.x;
					chunkRequest.chunkY = (int) chunkPos.y;
					chunkRequest.chunkZ = (int) chunkPos.z;

					ClientChunkRequest.requestedChunkData(chunkRequest.chunkX, chunkRequest.chunkY, chunkRequest.chunkZ);

					this.sendPacket(chunkRequest);
				}
			}
		}

		// Send player update
		ClientPlayerUpdate playerUpdate = new ClientPlayerUpdate();
		playerUpdate.euler = Vector3.Zero;
		playerUpdate.position = playerPosition;
		this.sendPacket(playerUpdate);

		while (this.hasPacket())
		{
			// Get next packet model
			IPacketModel packet = this.getPacket();

			if (packet == null)
				break;

			packet.interpretClientSide(this.cubicWorld);
		}
	}
}
