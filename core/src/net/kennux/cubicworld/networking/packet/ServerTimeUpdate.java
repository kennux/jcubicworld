package net.kennux.cubicworld.networking.packet;

import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.CubicWorldServer;
import net.kennux.cubicworld.networking.APacketModel;
import net.kennux.cubicworld.networking.BitReader;
import net.kennux.cubicworld.networking.BitWriter;
import net.kennux.cubicworld.networking.CubicWorldServerClient;

/**
 * This packet contains new day night cycle data. Packet id: 0x14 Packet data:
 * [byte - hour][byte - minute]
 * 
 * Server -> Client packet
 * 
 * @author KennuX
 *
 */
public class ServerTimeUpdate extends APacketModel
{
	public byte hour;

	public byte minute;

	@Override
	public int getPlayerId()
	{
		return -1; // Broadcast
	}

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
