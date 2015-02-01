package net.kennux.cubicworld.networking.packet;

import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.CubicWorldServer;
import net.kennux.cubicworld.entity.AEntity;
import net.kennux.cubicworld.networking.APacketModel;
import net.kennux.cubicworld.networking.BitReader;
import net.kennux.cubicworld.networking.BitWriter;
import net.kennux.cubicworld.networking.CubicWorldServerClient;

/**
 * Server client packet for updating an entity. Packet id: 0x12 Packet data: [3
 * integers, 12 byte - chunk position (global chunkspace)][VoxelData]
 * 
 * Server -> Client packet
 * 
 * @author KennuX
 *
 */
public class ServerEntityUpdate extends APacketModel
{
	private int entityId;
	private byte[] entityData;
	public AEntity entity;

	@Override
	public void interpretClientSide(CubicWorldGame cubicWorld)
	{
		// Update entity
		AEntity entity = cubicWorld.entityManager.get(this.entityId);
		if (entity != null) // Ignore the update if it is not in the entity manager
		{
			entity.deserialize(new BitReader(this.entityData));

			entity.recievedUpdate();
		}
	}

	@Override
	public void interpretServerSide(CubicWorldServer server, CubicWorldServerClient client)
	{
	}

	@Override
	public void readPacket(BitReader reader)
	{
		this.entityId = reader.readInt();

		// Read state data
		this.entityData = reader.readBytes();
	}

	@Override
	public void writePacket(BitWriter builder)
	{
		builder.writeInt(this.entity.getEntityId());

		// Serialize entity
		BitWriter writer = new BitWriter();
		this.entity.serialize(writer);

		// Write entity data to stream
		byte[] stateData = writer.getPacket();

		// Write data len
		builder.writeBytes(stateData);
		// System.out.println("Wrote entity update: " + this.entityId);
	}

}
