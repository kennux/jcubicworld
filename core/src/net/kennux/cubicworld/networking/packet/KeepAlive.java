package net.kennux.cubicworld.networking.packet;

import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.CubicWorldServer;
import net.kennux.cubicworld.networking.CubicWorldServerClient;
import net.kennux.cubicworld.networking.model.APlayerPacketModel;
import net.kennux.cubicworld.serialization.BitReader;
import net.kennux.cubicworld.serialization.BitWriter;

/**
 * Keep alive packet model implementation. Packet id: 0x01 Packet data: nothing
 * 
 * Client -> Server packet
 * 
 * @author KennuX
 *
 */
public class KeepAlive extends APlayerPacketModel
{
	@Override
	public void interpretClientSide(CubicWorldGame cubicWorld)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void interpretServerSide(CubicWorldServer server, CubicWorldServerClient client)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void readPacket(BitReader reader)
	{
	}

	@Override
	public void writePacket(BitWriter builder)
	{
	}
}
