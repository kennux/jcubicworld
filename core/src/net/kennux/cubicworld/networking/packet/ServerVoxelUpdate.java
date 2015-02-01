package net.kennux.cubicworld.networking.packet;

import net.kennux.cubicworld.CubicWorldConfiguration;
import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.CubicWorldServer;
import net.kennux.cubicworld.networking.APacketModel;
import net.kennux.cubicworld.networking.BitReader;
import net.kennux.cubicworld.networking.BitWriter;
import net.kennux.cubicworld.networking.CubicWorldServerClient;
import net.kennux.cubicworld.voxel.VoxelData;

/**
 * Chunk data model implementation. Updates a single block on a chunk. Packet
 * Packet data: [3 integers, 12 byte - block position (global
 * blockspace)][VoxelData]
 * 
 * This packet is distance based, set the cull position using the
 * setCullPosition() method!
 * 
 * Server -> Client packet
 * 
 * @author KennuX
 *
 */
public class ServerVoxelUpdate extends APacketModel
{
	// Chunk coordinates
	public int x = 0;
	public int y = 0;
	public int z = 0;
	public VoxelData voxel;

	@Override
	public float getCullDistance()
	{
		return CubicWorldConfiguration.chunkUpdateDistance;
	}

	@Override
	public int getPlayerId()
	{
		return -2; // Distance based
	}

	@Override
	public void interpretClientSide(CubicWorldGame cubicWorld)
	{
		// System.out.println("Voxel Update!");
		// Set the voxel
		cubicWorld.voxelWorld.setVoxel(this.x, this.y, this.z, this.voxel);
	}

	@Override
	public void interpretServerSide(CubicWorldServer server, CubicWorldServerClient client)
	{
	}

	@Override
	public void readPacket(BitReader reader)
	{
		this.x = reader.readInt();
		this.y = reader.readInt();
		this.z = reader.readInt();

		this.voxel = reader.readVoxelData();
	}

	@Override
	public void writePacket(BitWriter builder)
	{
		builder.writeInt(this.x);
		builder.writeInt(this.y);
		builder.writeInt(this.z);
		builder.writeVoxelData(this.voxel);
	}
}
