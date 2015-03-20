package net.kennux.cubicworld.networking.model;

import net.kennux.cubicworld.CubicWorldConfiguration;

import com.badlogic.gdx.math.Vector3;

/**
 * Extendation of the APacketModel class.
 * This class can get used to implement packets only sent to single players.
 * It offers the additional attribute playerId which is the player's server slot index.
 * 
 * @author KennuX
 *
 */
public abstract class ADistanceCulledPacketModel extends APacketModel
{
	/**
	 * The cull distance.
	 * If this is set to -1 (the value it got initialized with) the
	 * CubicWorldConfiguration.standardCullDistance will get used.
	 */
	private float cullDistance = -1;

	/**
	 * The position used to execute the culling.
	 */
	private Vector3 cullPosition;

	@Override
	public PacketTargetInfo getTargetInfo()
	{
		return PacketTargetInfo.createDistanceCulled(this.getCullPosition(), this.getCullDistance());
	}

	/**
	 * @return the cullDistance
	 */
	public float getCullDistance()
	{
		if (this.cullDistance != -1)
			return cullDistance;

		return CubicWorldConfiguration.standardCullDistance;
	}

	/**
	 * @param cullDistance
	 *            the cullDistance to set
	 */
	public void setCullDistance(float cullDistance)
	{
		this.cullDistance = cullDistance;
	}

	/**
	 * @return the cullPosition
	 */
	public Vector3 getCullPosition()
	{
		return cullPosition;
	}

	/**
	 * @param cullPosition
	 *            the cullPosition to set
	 */
	public void setCullPosition(Vector3 cullPosition)
	{
		this.cullPosition = cullPosition;
	}

}
