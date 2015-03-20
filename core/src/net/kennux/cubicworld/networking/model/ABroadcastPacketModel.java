package net.kennux.cubicworld.networking.model;

/**
 * Base class for packets which will get broadcasted.
 * 
 * @author KennuX
 */
public abstract class ABroadcastPacketModel extends APacketModel
{
	public PacketTargetInfo getTargetInfo()
	{
		return PacketTargetInfo.createBroadcast();
	}
}
