package net.kennux.cubicworld.networking.model;

public abstract class AClientPacketModel extends APacketModel
{
	/**
	 * Returns null as the target needs no target info.
	 */
	public PacketTargetInfo getTargetInfo()
	{
		return null;
	}
}
