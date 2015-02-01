package net.kennux.cubicworld.networking.packet;

import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.CubicWorldServer;
import net.kennux.cubicworld.networking.APacketModel;
import net.kennux.cubicworld.networking.CubicWorldServerClient;
import net.kennux.cubicworld.serialization.BitReader;
import net.kennux.cubicworld.serialization.BitWriter;
import net.kennux.cubicworld.voxel.VoxelData;

/**
 * Updates the voxel at given position. Packet id: 0x13 Packet data: [Vector3
 * pos][Vector3 euler]
 * 
 * Client -> Server packet
 * 
 * @author KennuX
 *
 */
public class ClientVoxelUpdate extends APacketModel
{
	// Chunk coordinates
	public int x;
	public int y;
	public int z;
	public VoxelData data;

	@Override
	public void interpretClientSide(CubicWorldGame cubicWorld)
	{
	}

	@Override
	public void interpretServerSide(CubicWorldServer server, CubicWorldServerClient client)
	{
		// Set the voxel given in the remove packet
		server.voxelWorld.setVoxel(this.x, this.y, this.z, this.data);
	}

	@Override
	public void readPacket(BitReader reader)
	{
		this.x = reader.readInt();
		this.y = reader.readInt();
		this.z = reader.readInt();
		this.data = reader.readVoxelData();
		// System.out.println("Read player update: " + this.position + " " +
		// this.euler);
	}

	@Override
	public void writePacket(BitWriter builder)
	{
		// System.out.println("Wrote player update: " + this.position + " " +
		// this.euler);
		builder.writeInt(x);
		builder.writeInt(y);
		builder.writeInt(z);
		builder.writeVoxelData(data);
	}

}
