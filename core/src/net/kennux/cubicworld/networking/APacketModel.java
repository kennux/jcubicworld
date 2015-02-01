package net.kennux.cubicworld.networking;

import net.kennux.cubicworld.CubicWorldConfiguration;

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
 * -1 means broadcast. This is a server wide broadcast which EVERY player will
 * get.
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
	/**
	 * The reciever player id.
	 */
	private int playerId;

	/**
	 * The position used to cull this packet.
	 */
	private Vector3 cullPosition;

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
	@Override
	public float getCullDistance()
	{
		return CubicWorldConfiguration.standardDistanceCullDistance;
	}

	/**
	 * Returns the vector3 in worldspace which will get used for culling if the
	 * playerid is -2. Otherwise, ignore this.
	 * 
	 * @return
	 */
	@Override
	public Vector3 getCullPosition()
	{
		return this.cullPosition;
	}

	public final short getPacketId()
	{
		return Protocol.getPacketId(this);
	}

	/**
	 * -1 means broadcast, > -1 is the client slot index. -2 means distance
	 * If you want to specify broadcast or distance culled sending, you must
	 * overwrite this method and return a static value.
	 */
	@Override
	public int getPlayerId()
	{
		return this.playerId;
	}

	/**
	 * Sets the vector3 in worldspace which will get used for culling if the
	 * playerid is -2. Otherwise, ignore this.
	 * 
	 * @return
	 */
	@Override
	public void setCullPosition(Vector3 cullPos)
	{
		this.cullPosition = cullPos;
	}

	public void setPlayerId(int id)
	{
		this.playerId = id;
	}
}
