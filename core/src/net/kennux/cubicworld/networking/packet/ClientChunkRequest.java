package net.kennux.cubicworld.networking.packet;

import java.util.ArrayList;

import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.CubicWorldServer;
import net.kennux.cubicworld.networking.APacketModel;
import net.kennux.cubicworld.networking.CubicWorldServerClient;
import net.kennux.cubicworld.serialization.BitReader;
import net.kennux.cubicworld.serialization.BitWriter;

import com.badlogic.gdx.math.Vector3;

/**
 * Chunk request packet model implementation. Packet id: 10 Packet data: [3
 * integers, 12 byte - chunkPosition]
 * 
 * Client -> Server packet
 * 
 * @author KennuX
 *
 */
public class ClientChunkRequest extends APacketModel
{
	/**
	 * The pending chunk requests list.
	 */
	private static ArrayList<Vector3> pendingRequests = new ArrayList<Vector3>();

	/**
	 * Returns true if there are chunk requests pending.
	 * 
	 * @return
	 */
	public static boolean areRequestsPending()
	{
		return pendingRequests.size() > 0;
	}

	/**
	 * Call this function if a pending chunk request was processed.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public static void recievedChunkData(int x, int y, int z)
	{
		Vector3 vector = new Vector3(x, y, z);
		// System.out.println("Recieved: " + vector);
		pendingRequests.remove(vector);
	}

	/**
	 * Returns true if the client is currently waiting for the chunk with given x|y|z coordinates.
	 * 
	 * @return
	 */
	public static boolean isWaitingFor(Vector3 pos)
	{
		return pendingRequests.contains(pos);
	}

	/**
	 * Call this function if a chunk was requested.
	 * 
	 * @param chunkX
	 * @param chunkY
	 * @param chunkZ
	 */
	public static void requestedChunkData(int chunkX, int chunkY, int chunkZ)
	{
		// System.out.println("Requested: " + new Vector3(chunkX, chunkY, chunkZ));
		pendingRequests.add(new Vector3(chunkX, chunkY, chunkZ));
	}

	// Chunk coordinates
	public int chunkX = 0;
	public int chunkY = 0;
	public int chunkZ = 0;

	@Override
	public void interpretClientSide(CubicWorldGame cubicWorld)
	{
	}

	@Override
	public void interpretServerSide(CubicWorldServer server, CubicWorldServerClient client)
	{
		// Only add request if there is non pending
		for (ClientChunkRequest request : client.chunkRequests)
		{
			if (request.chunkX == this.chunkX && request.chunkY == this.chunkY && request.chunkZ == this.chunkZ)
			{
				return;
			}
		}

		client.chunkRequests.add(this);
	}

	@Override
	public void readPacket(BitReader reader)
	{
		this.chunkX = reader.readInt();
		this.chunkY = reader.readInt();
		this.chunkZ = reader.readInt();
		// System.out.println("Read chunkrequest " + this.chunkX + " " + this.chunkY + " " + this.chunkZ);
	}

	@Override
	public void writePacket(BitWriter builder)
	{
		// System.out.println("Requested chunk " + this.chunkX + " " + this.chunkY + " " + this.chunkZ);
		builder.writeInt(this.chunkX);
		builder.writeInt(this.chunkY);
		builder.writeInt(this.chunkZ);
	}

}
