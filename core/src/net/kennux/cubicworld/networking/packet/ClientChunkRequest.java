package net.kennux.cubicworld.networking.packet;

import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.CubicWorldServer;
import net.kennux.cubicworld.networking.APacketModel;
import net.kennux.cubicworld.networking.BitReader;
import net.kennux.cubicworld.networking.BitWriter;
import net.kennux.cubicworld.networking.CubicWorldServerClient;

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
