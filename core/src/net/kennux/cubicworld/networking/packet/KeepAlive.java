package net.kennux.cubicworld.networking.packet;

import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.CubicWorldServer;
import net.kennux.cubicworld.networking.APacketModel;
import net.kennux.cubicworld.networking.BitReader;
import net.kennux.cubicworld.networking.BitWriter;
import net.kennux.cubicworld.networking.CubicWorldServerClient;

/**
 * Keep alive packet model implementation. Packet id: 0x01 Packet data: nothing
 * 
 * Client -> Server packet
 * 
 * @author KennuX
 *
 */
public class KeepAlive extends APacketModel
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
