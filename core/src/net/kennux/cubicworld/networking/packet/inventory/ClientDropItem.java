package net.kennux.cubicworld.networking.packet.inventory;

import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.CubicWorldServer;
import net.kennux.cubicworld.entity.ItemEntity;
import net.kennux.cubicworld.item.ItemStack;
import net.kennux.cubicworld.networking.APacketModel;
import net.kennux.cubicworld.networking.CubicWorldServerClient;
import net.kennux.cubicworld.serialization.BitReader;
import net.kennux.cubicworld.serialization.BitWriter;

import com.badlogic.gdx.math.Vector3;

/**
 * The client drop item packet will get sent if a player requests a item drop from his own inventory.
 * 
 * @author KennuX
 *
 */
public class ClientDropItem extends APacketModel
{
	/**
	 * The item slot id in the player inventory.
	 */
	public int itemSlotId;

	@Override
	public void interpretClientSide(CubicWorldGame cubicWorld)
	{
	}

	@Override
	public void interpretServerSide(CubicWorldServer server, CubicWorldServerClient client)
	{
		// Check if player entity and inventory got initialized
		if (client.playerEntity != null && client.playerEntity.playerInventory != null)
		{
			ItemStack itemStack = client.playerEntity.playerInventory.getItemStackInSlot(this.itemSlotId);
			if (client.playerEntity.playerInventory.removeItemsFromStack(this.itemSlotId, 1))
			{
				// If remove was successfull, spawn an item entity
				int itemId = itemStack.getType().getItemId();

				ItemEntity itemEntity = new ItemEntity(itemId);
				itemEntity.setPosition(client.playerEntity.getPosition());
				itemEntity.interpolatePosition(true);

				// Calculate item physics impulse
				itemEntity.impulse(new Vector3(0, 1, 0), 1);

				server.entityManager.add(server.entityManager.getNextFreeId(), itemEntity);
			}
		}
	}

	@Override
	public void readPacket(BitReader reader)
	{
		this.itemSlotId = reader.readInt();
	}

	@Override
	public void writePacket(BitWriter builder)
	{
		builder.writeInt(this.itemSlotId);
	}

}
