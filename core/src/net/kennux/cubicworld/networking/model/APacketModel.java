package net.kennux.cubicworld.networking.model;

import net.kennux.cubicworld.networking.IPacketModel;
import net.kennux.cubicworld.networking.Protocol;

import com.badlogic.gdx.math.Vector3;

/**
 * <pre>
 * Abstract packet model implementation.
 * 
 * The abstract packet model extends the packet model interface.
 * It adds possibilities to send a packet to different players on different
 * ways.
 * The sending typ gets decided by calling getPlayerId().
 * If the returned integer is:
 * 
 * -1 means broadcast. This is a server wide broadcast which EVERY player will get.
 * -2 means culled broadcast. This is a culled sending type, to use it you have
 * to set the cull position by calling setCullPosition().
 * >= 0 means single player. Only the player with the id returned will recieve
 * the packet.
 * 
 * Remember: a player id is the player's index in the server's client array.
 * </pre>
 * 
 * @author KennuX
 *
 */
public abstract class APacketModel implements IPacketModel
{
	public final short getPacketId()
	{
		return Protocol.getPacketId(this);
	}

	/**
	 * Empty placeholder function used for overwriting in later packet models.
	 * 
	 * @return
	 */
	public int getPlayerId()
	{
		return -1;
	}

	/**
	 * Empty placeholder function used for overwriting in later packet models.
	 * 
	 * @return
	 */
	public float getCullDistance()
	{
		return 0;
	}

	/**
	 * Empty placeholder function used for overwriting in later packet models.
	 * 
	 * @return
	 */
	public Vector3 getCullPosition()
	{
		return null;
	}
}
