package net.kennux.cubicworld.networking.model;

/**
 * Extendation of the APacketModel class.
 * This class can get used to implement packets only sent to single players.
 * It offers the additional attribute playerId which is the player's server slot index.
 * 
 * @author KennuX
 *
 */
public abstract class APlayerPacketModel extends APacketModel
{
	private int playerId;

	@Override
	public PacketTargetInfo getTargetInfo()
	{
		return PacketTargetInfo.createPlayer(this.playerId);
	}

	/**
	 * @return the playerId
	 */
	public int getPlayerId()
	{
		return playerId;
	}

	/**
	 * @param playerId
	 *            the playerId to set
	 */
	public void setPlayerId(int playerId)
	{
		this.playerId = playerId;
	}

}
