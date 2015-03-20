package net.kennux.cubicworld.networking.packet;

import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.CubicWorldServer;
import net.kennux.cubicworld.networking.CubicWorldServerClient;
import net.kennux.cubicworld.networking.model.ABroadcastPacketModel;
import net.kennux.cubicworld.serialization.BitReader;
import net.kennux.cubicworld.serialization.BitWriter;

/**
 * This packet contains new day night cycle data. Packet id: 0x14 Packet data:
 * [byte - hour][byte - minute]
 * 
 * Server -> Client packet
 * 
 * @author KennuX
 *
 */
public class ServerTimeUpdate extends ABroadcastPacketModel
{
	public byte hour;

	public byte minute;

	@Override
	public void interpretClientSide(CubicWorldGame cubicWorld)
	{
		// Update time
		cubicWorld.dayNightCycle.setTime(this.hour, this.minute);
	}

	@Override
	public void interpretServerSide(CubicWorldServer server, CubicWorldServerClient client)
	{
	}

	@Override
	public void readPacket(BitReader reader)
	{
		this.hour = reader.readByte();
		this.minute = reader.readByte();
	}

	@Override
	public void writePacket(BitWriter builder)
	{
		builder.writeByte(this.hour);
		builder.writeByte(this.minute);
	}

}
