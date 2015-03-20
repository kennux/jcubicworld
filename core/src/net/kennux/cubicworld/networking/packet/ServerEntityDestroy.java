package net.kennux.cubicworld.networking.packet;

import net.kennux.cubicworld.CubicWorldConfiguration;
import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.CubicWorldServer;
import net.kennux.cubicworld.networking.CubicWorldServerClient;
import net.kennux.cubicworld.networking.model.ADistanceCulledPacketModel;
import net.kennux.cubicworld.serialization.BitReader;
import net.kennux.cubicworld.serialization.BitWriter;

/**
 * Server entity destroy packet. Packet id: 0x13 Packet data: [entity id - int]
 * 
 * Server -> Client packet
 * 
 * @author KennuX
 *
 */
public class ServerEntityDestroy extends ADistanceCulledPacketModel
{
	public int entityId;

	@Override
	public float getCullDistance()
	{
		return CubicWorldConfiguration.entityCullDistance;
	}

	@Override
	public int getPlayerId()
	{
		return -2; // Distance based
	}

	@Override
	public void interpretClientSide(CubicWorldGame cubicWorld)
	{
		// System.out.println("Interpreted entity destroy: " + this.entityId);
		// Spawn entity
		cubicWorld.entityManager.remove(this.entityId);
	}

	@Override
	public void interpretServerSide(CubicWorldServer server, CubicWorldServerClient client)
	{
	}

	@Override
	public void readPacket(BitReader reader)
	{
		this.entityId = reader.readInt();
		// System.out.println("Read entity destroy: " + this.entityId);
	}

	@Override
	public void writePacket(BitWriter builder)
	{
		builder.writeInt(this.entityId);
		// System.out.println("Wrote entity destroy: " + this.entityId);
	}
}
