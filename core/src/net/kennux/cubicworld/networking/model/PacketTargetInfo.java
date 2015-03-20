package net.kennux.cubicworld.networking.model;

import com.badlogic.gdx.math.Vector3;

/**
 * The packet target info class gets used to define a packet's target.
 * 
 * @see PacketTarget
 * @author KennuX
 *
 */
public class PacketTargetInfo
{
	/**
	 * Creates a broadcast packet target info.
	 * 
	 * @see PacketTarget#BROADCAST
	 * @return
	 */
	public static PacketTargetInfo createBroadcast()
	{
		return new PacketTargetInfo(PacketTarget.BROADCAST, -1);
	}

	/**
	 * Creates a packet target info for a single player.
	 * 
	 * @param playerIndex
	 * @return
	 */
	public static PacketTargetInfo createPlayer(int playerIndex)
	{
		return new PacketTargetInfo(PacketTarget.PLAYER, playerIndex);
	}

	/**
	 * Creates a distance culled packet target info.
	 * 
	 * @param position
	 * @param cullDistance
	 * @return
	 */
	public static PacketTargetInfo createDistanceCulled(Vector3 position, float cullDistance)
	{
		return new PacketTargetInfo(PacketTarget.DISTANCE_CULLED, position, cullDistance);
	}

	private PacketTarget packetTarget;

	// Player packet
	/**
	 * The player index.
	 */
	private int playerIndex;

	// Distance-culled
	private float cullDistance;
	private Vector3 position;

	/**
	 * The private constructor used for creating target infos in the static functions.
	 * 
	 * @param target
	 * @param playerIndex
	 */
	private PacketTargetInfo(PacketTarget target, int playerIndex)
	{
		this.packetTarget = target;
		this.playerIndex = playerIndex;
	}

	/**
	 * The private constructor used for creating target infos in the static functions.
	 * 
	 * @param target
	 * @param playerIndex
	 */
	private PacketTargetInfo(PacketTarget target, Vector3 targetPosition, float cullDistance)
	{
		this.packetTarget = target;
		this.position = targetPosition;
		this.cullDistance = cullDistance;
	}

	/**
	 * @return the playerIndex
	 */
	public int getPlayerIndex()
	{
		return playerIndex;
	}

	/**
	 * @return the cullDistance
	 */
	public float getCullDistance()
	{
		return cullDistance;
	}

	/**
	 * @return the position
	 */
	public Vector3 getPosition()
	{
		return position;
	}

	/**
	 * @return the packetTarget
	 */
	public PacketTarget getPacketTarget()
	{
		return packetTarget;
	}
}
