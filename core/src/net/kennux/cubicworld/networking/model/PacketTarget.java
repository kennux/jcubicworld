package net.kennux.cubicworld.networking.model;

/**
 * The packet target enum is used to specify the target of a packet.
 * The packet target can be a broadcast which EVERY connected player will recieve, or a distance culled packet, or a single player.
 * 
 * @author KennuX
 *
 */
public enum PacketTarget
{
	BROADCAST, DISTANCE_CULLED, PLAYER
}
