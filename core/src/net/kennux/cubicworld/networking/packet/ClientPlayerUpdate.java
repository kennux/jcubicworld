package net.kennux.cubicworld.networking.packet;

import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.CubicWorldServer;
import net.kennux.cubicworld.networking.APacketModel;
import net.kennux.cubicworld.networking.BitReader;
import net.kennux.cubicworld.networking.BitWriter;
import net.kennux.cubicworld.networking.CubicWorldServerClient;

import com.badlogic.gdx.math.Vector3;

/**
 * Updates the player's position and rotation. Packet id: 0x11 Packet data:
 * [Vector3 pos][Vector3 euler]
 * 
 * Client -> Server packet
 * 
 * @author KennuX
 *
 */
public class ClientPlayerUpdate extends APacketModel
{
	// Chunk coordinates
	public Vector3 position;
	public Vector3 euler;

	@Override
	public void interpretClientSide(CubicWorldGame cubicWorld)
	{
	}

	@Override
	public void interpretServerSide(CubicWorldServer server, CubicWorldServerClient client)
	{
		// Update player's position
		client.playerEntity.setPosition(this.position);
	}

	@Override
	public void readPacket(BitReader reader)
	{
		this.position = reader.readVector3();
		this.euler = reader.readVector3();
		// System.out.println("Read player update: " + this.position + " " +
		// this.euler);
	}

	@Override
	public void writePacket(BitWriter builder)
	{
		// System.out.println("Wrote player update: " + this.position + " " +
		// this.euler);
		builder.writeVector3(position);
		builder.writeVector3(euler);
	}

}
