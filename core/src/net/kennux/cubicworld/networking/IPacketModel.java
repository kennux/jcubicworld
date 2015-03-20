package net.kennux.cubicworld.networking;

import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.CubicWorldServer;
import net.kennux.cubicworld.networking.model.PacketTargetInfo;
import net.kennux.cubicworld.serialization.BitReader;
import net.kennux.cubicworld.serialization.BitWriter;

import com.badlogic.gdx.math.Vector3;

/**
 * Packet model interface. Implement packet models for enquening packets in the
 * server class.
 * 
 * @author KennuX
 *
 */
public interface IPacketModel
{
	/**
	 * Overwrite this in your implementation.
	 */
	public short getPacketId();

	/**
	 * Returns the packet's target info.
	 * 
	 * @return
	 */
	public PacketTargetInfo getTargetInfo();

	/**
	 * Interprets this packet model on the client side. If this packet is a
	 * client -> server packet just do nothing in here.
	 * 
	 * @param cubicWorld
	 *            The cubicworld instance.
	 */
	public void interpretClientSide(CubicWorldGame cubicWorld);

	/**
	 * Interprets this packet model on the server side. If this packet is a
	 * server -> client packet just do nothing in here.
	 * 
	 * @param server
	 *            The server instance.
	 * @param client
	 *            The client instance
	 */
	public void interpretServerSide(CubicWorldServer server, CubicWorldServerClient client);

	/**
	 * Writes the packet data (packetid and length will get written by the
	 * sender).
	 * 
	 * @param outputStream
	 */
	public void readPacket(BitReader reader);

	/**
	 * Writes the packet data (packetid and length will get written by the
	 * sender).
	 * 
	 * @param outputStream
	 */
	public void writePacket(BitWriter builder);

	public Vector3 getCullPosition();

	public float getCullDistance();

	public int getPlayerId();
}
