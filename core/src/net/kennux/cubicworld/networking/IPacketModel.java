package net.kennux.cubicworld.networking;

import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.CubicWorldServer;

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
	 * <pre>
	 * Returns the cull distance.
	 * The cull distance specifies how far away a player can be to a cull
	 * position to recieve the update.
	 * 
	 * If this is set to 10 for example and the cull position is 0|0|0 then
	 * every player whose distance to 0|0|0 is < 10 will get the packet.
	 * Standard is CubicWorldConfiguration.standardDistanceCullDistance.
	 * </pre>
	 * 
	 * @return
	 */
	public float getCullDistance();

	/**
	 * Returns the vector3 in worldspace which will get used for culling if the
	 * playerid is -2. Otherwise, ignore this.
	 * 
	 * @return
	 */
	public Vector3 getCullPosition();

	/**
	 * Overwrite this in your implementation.
	 */
	public short getPacketId();

	/**
	 * <pre>
	 * Returns the player id who should get this packet. Id is the index of the
	 * client socket in the clients-array of the server instance. -1 means
	 * broadcast.
	 * -2 means distance culled broadcast if you packet is a distance culled
	 * packet, make sure you set the correct cull position.
	 * </pre>
	 * 
	 * @return
	 */
	public int getPlayerId();

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
	 *            The clien instance
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
	 * Sets the vector3 in worldspace which will get used for culling if the
	 * playerid is -2. Otherwise, ignore this.
	 * 
	 * @return
	 */
	public void setCullPosition(Vector3 cullPos);

	/**
	 * Writes the packet data (packetid and length will get written by the
	 * sender).
	 * 
	 * @param outputStream
	 */
	public void writePacket(BitWriter builder);
}
