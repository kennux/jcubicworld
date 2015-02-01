package net.kennux.cubicworld.networking.packet.inventory;

import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.CubicWorldServer;
import net.kennux.cubicworld.inventory.IInventory;
import net.kennux.cubicworld.networking.APacketModel;
import net.kennux.cubicworld.networking.BitReader;
import net.kennux.cubicworld.networking.BitWriter;
import net.kennux.cubicworld.networking.CubicWorldServerClient;

/**
 * This packet gets sent by the server if a player's inventory got updated.
 * 
 * @author KennuX
 *
 */
public class ServerPlayerInventoryUpdate extends APacketModel
{
	/**
	 * The item slot id in the player inventory.
	 */
	public IInventory inventory;

	/**
	 * The reader got in readPacket() saved for later reading in interpretClientSide.
	 */
	private BitReader inventoryReader;

	/**
	 * Updates the player controller's inventory with the data sent by the server.
	 */
	@Override
	public void interpretClientSide(CubicWorldGame cubicWorld)
	{
		if (cubicWorld.playerController != null && cubicWorld.playerController.getPlayerInventory() != null)
		{
			cubicWorld.playerController.getPlayerInventory().deserializeInventory(this.inventoryReader);
		}
	}

	@Override
	public void interpretServerSide(CubicWorldServer server, CubicWorldServerClient client)
	{
	}

	@Override
	public void readPacket(BitReader reader)
	{
		this.inventoryReader = reader;
	}

	@Override
	public void writePacket(BitWriter builder)
	{
		this.inventory.serializeInventory(builder);
	}

}
