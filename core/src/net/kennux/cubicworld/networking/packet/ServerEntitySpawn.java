package net.kennux.cubicworld.networking.packet;

import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.CubicWorldServer;
import net.kennux.cubicworld.entity.AEntity;
import net.kennux.cubicworld.entity.EntitySystem;
import net.kennux.cubicworld.networking.APacketModel;
import net.kennux.cubicworld.networking.BitReader;
import net.kennux.cubicworld.networking.BitWriter;
import net.kennux.cubicworld.networking.CubicWorldServerClient;

/**
 * Server entity spawn packet. Packet id: 0x13 Packet data: [entity type id -
 * int][entity id - int][entity position - vector3][entity euler - vector3]
 * 
 * Server -> Client packet
 * 
 * @author KennuX
 *
 */
public class ServerEntitySpawn extends APacketModel
{
	// Only used for reading
	private int entityId;
	private int entityType;
	private byte[] entityData;
	public String entityName = "";

	public AEntity entity;

	@Override
	public void interpretClientSide(CubicWorldGame cubicWorld)
	{
		// Spawn entity
		AEntity entity = EntitySystem.instantiateEntity(this.entityType);
		entity.setEntityName(this.entityName);
		entity.deserializeInitial(new BitReader(this.entityData));
		entity.interpolatePosition(true);
		entity.recievedUpdate();
		cubicWorld.entityManager.add(this.entityId, entity);
	}

	@Override
	public void interpretServerSide(CubicWorldServer server, CubicWorldServerClient client)
	{
	}

	@Override
	public void readPacket(BitReader reader)
	{
		this.entityType = reader.readInt();
		this.entityId = reader.readInt();

		// Read state data
		this.entityData = reader.readBytes();
		this.entityName = reader.readString();
	}

	@Override
	public void writePacket(BitWriter builder)
	{
		builder.writeInt(EntitySystem.reverseLookup(this.entity.getClass()));
		builder.writeInt(this.entity.getEntityId());

		// Serialize entity
		BitWriter writer = new BitWriter();
		this.entity.serializeInitial(writer);

		// Write entity data to stream
		byte[] stateData = writer.getPacket();

		// Write data
		builder.writeBytes(stateData);

		builder.writeString(this.entity.getEntityName());
	}

}
