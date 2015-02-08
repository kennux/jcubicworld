package net.kennux.cubicworld.networking.packet;

import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.CubicWorldServer;
import net.kennux.cubicworld.networking.APacketModel;
import net.kennux.cubicworld.networking.CubicWorldServerClient;
import net.kennux.cubicworld.serialization.BitReader;
import net.kennux.cubicworld.serialization.BitWriter;
import net.kennux.cubicworld.voxel.VoxelChunk;
import net.kennux.cubicworld.voxel.VoxelData;
import net.kennux.cubicworld.voxel.VoxelEngine;

/**
 * Chunk request packet model implementation. Packet id: 0x09 Packet data: [3
 * integers, 12 byte - chunk position (global chunkspace)][VoxelData]
 * 
 * Server -> Client packet
 * 
 * @author KennuX
 *
 */
public class ServerChunkData extends APacketModel
{
	// Chunk coordinates
	public int chunkX = 0;
	public int chunkY = 0;
	public int chunkZ = 0;
	public VoxelData[][][] voxelData;

	@Override
	public void interpretClientSide(CubicWorldGame cubicWorld)
	{
		// Set chunk data
		VoxelChunk chunk = cubicWorld.voxelWorld.getChunk(this.chunkX, this.chunkY, this.chunkZ, true);
		chunk.setVoxelData(this.voxelData);

		ClientChunkRequest.recievedChunkData(this.chunkX, this.chunkY, this.chunkZ);
	}

	@Override
	public void interpretServerSide(CubicWorldServer server, CubicWorldServerClient client)
	{
	}

	@Override
	public void readPacket(BitReader reader)
	{
		this.chunkX = reader.readInt();
		this.chunkY = reader.readInt();
		this.chunkZ = reader.readInt();

		byte[] serializedVoxelData = reader.readBytes();

		// init voxel data
		this.voxelData = VoxelEngine.deserializeVoxelData(serializedVoxelData);

		// System.out.println("Got chunkdata " + this.chunkX + " " + this.chunkY
		// + " " + this.chunkZ);
	}

	@Override
	public void writePacket(BitWriter builder)
	{
		builder.writeInt(this.chunkX);
		builder.writeInt(this.chunkY);
		builder.writeInt(this.chunkZ);

		builder.writeBytes(VoxelEngine.serializeVoxelData(this.voxelData));

		// Write compressed data
		// System.out.println("Wrote chunkdata " + this.chunkX + " " +
		// this.chunkY + " " + this.chunkZ);
	}
}
