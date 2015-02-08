package net.kennux.cubicworld.networking.packet.inventory;

import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.CubicWorldServer;
import net.kennux.cubicworld.item.ItemStack;
import net.kennux.cubicworld.item.ItemType;
import net.kennux.cubicworld.networking.APacketModel;
import net.kennux.cubicworld.networking.CubicWorldServerClient;
import net.kennux.cubicworld.serialization.BitReader;
import net.kennux.cubicworld.serialization.BitWriter;
import net.kennux.cubicworld.voxel.VoxelData;

/**
 * The client item transaction packet will get used to move items from the player's inventory to a block inventory.
 * 
 * @author KennuX
 *
 */
public class ClientItemTransaction extends APacketModel
{
	/**
	 * Creates a client item transaction.
	 * After creating it on the client, just send it to the server.
	 * 
	 * @param playerItemSlotId
	 * @param transactionType
	 * @param voxelPositionX
	 * @param voxelPositionY
	 * @param voxelPositionZ
	 * @return
	 */
	public static ClientItemTransaction create(int playerItemSlotId, TransactionType transactionType, int voxelPositionX, int voxelPositionY, int voxelPositionZ, int voxelItemSlotId, int itemCount)
	{
		ClientItemTransaction transaction = new ClientItemTransaction();
		transaction.sourceItemSlotId = playerItemSlotId;
		transaction.transactionType = transactionType;
		transaction.voxelPositionX = voxelPositionX;
		transaction.voxelPositionY = voxelPositionY;
		transaction.voxelPositionZ = voxelPositionZ;
		transaction.targetItemSlotId = voxelItemSlotId;
		transaction.itemCount = itemCount;

		return transaction;
	}

	/**
	 * The item slot id in the player inventory.
	 */
	public int sourceItemSlotId;

	/**
	 * The transaction type.
	 */
	public TransactionType transactionType;

	/**
	 * The voxel position of the inventory voxel.
	 */
	public int voxelPositionX;
	/**
	 * The voxel position of the inventory voxel.
	 */
	public int voxelPositionY;
	/**
	 * The voxel position of the inventory voxel.
	 */
	public int voxelPositionZ;

	/**
	 * The item slot in the inventory.
	 */
	public int targetItemSlotId;

	/**
	 * The count of items to move.
	 */
	public int itemCount;

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
			VoxelData inventoryVoxel = server.voxelWorld.getVoxel(this.voxelPositionX, this.voxelPositionY, this.voxelPositionZ);

			if (inventoryVoxel != null && inventoryVoxel.blockInventory != null)
			{
				// Read stack info
				ItemStack playerItemStack = client.playerEntity.playerInventory.getItemStackInSlot(this.sourceItemSlotId);
				ItemStack voxelItemStack = inventoryVoxel.blockInventory.getItemStackInSlot(this.targetItemSlotId);

				ItemType voxelItemType = null;
				ItemType playerItemType = null;

				if (voxelItemStack != null)
					voxelItemType = voxelItemStack.getType();

				if (playerItemStack != null)
					playerItemType = playerItemStack.getType();

				// Check for type equality
				if (playerItemStack != null && voxelItemStack != null && voxelItemType == playerItemType)
				{
					switch (this.transactionType)
					{
						case INVENTORY_TO_PLAYER:
							// Validity checks
							if (voxelItemStack.getItemCount() >= this.itemCount && client.playerEntity.playerInventory.getFreeSpaceInSlot(this.sourceItemSlotId) >= this.itemCount)
							{
								// Perform inventory -> player transaction
								// Remove from voxel
								if (inventoryVoxel.blockInventory.removeItemsFromStack(this.targetItemSlotId, this.itemCount))
								{
									// Add items to player
									if (!client.playerEntity.playerInventory.addItemsToStack(this.sourceItemSlotId, this.itemCount))
									{
										// Rollback if the changes weren't successfull
										inventoryVoxel.blockInventory.addItemsToStack(this.targetItemSlotId, this.itemCount, voxelItemType.getItemId());
									}
								}
							}
							break;
						case PLAYER_TO_INVENTORY:
							// Validity checks
							if (playerItemStack.getItemCount() >= this.itemCount && inventoryVoxel.blockInventory.getFreeSpaceInSlot(this.targetItemSlotId) >= this.itemCount)
							{
								// Perform inventory -> player transaction
								// Remove from player
								if (client.playerEntity.playerInventory.removeItemsFromStack(this.sourceItemSlotId, this.itemCount))
								{
									// Add items to voxel
									if (!inventoryVoxel.blockInventory.addItemsToStack(this.targetItemSlotId, this.itemCount))
									{
										// Rollback if the changes weren't successfull
										client.playerEntity.playerInventory.addItemsToStack(this.sourceItemSlotId, this.itemCount, voxelItemType.getItemId());
									}
								}
							}
							break;
					}
				}
				else
				{
					// No type equality
					switch (this.transactionType)
					{
						case INVENTORY_TO_PLAYER:
							// Validity check
							if (!client.playerEntity.playerInventory.hasItemStackInSlot(this.sourceItemSlotId) && inventoryVoxel.blockInventory.getItemCountInSlot(this.targetItemSlotId) >= this.itemCount)
							{
								// Remove items from voxel
								if (inventoryVoxel.blockInventory.removeItemsFromStack(this.targetItemSlotId, this.itemCount))
								{
									// Add to players inventory
									if (!client.playerEntity.playerInventory.addItemsToStack(this.sourceItemSlotId, this.itemCount, voxelItemType.getItemId()))
									{
										// Rollback if adding was not successfull
										inventoryVoxel.blockInventory.addItemsToStack(this.targetItemSlotId, this.itemCount, voxelItemType.getItemId());
									}
								}
							}
							break;
						case PLAYER_TO_INVENTORY:
							// Validity check
							if (!inventoryVoxel.blockInventory.hasItemStackInSlot(this.targetItemSlotId) && client.playerEntity.playerInventory.getItemCountInSlot(this.sourceItemSlotId) >= this.itemCount)
							{
								// Remove items from player
								if (client.playerEntity.playerInventory.removeItemsFromStack(this.sourceItemSlotId, this.itemCount))
								{
									// Add to voxels inventory
									if (!inventoryVoxel.blockInventory.addItemsToStack(this.targetItemSlotId, this.itemCount, playerItemType.getItemId()))
									{
										// Rollback if adding was not successfull
										client.playerEntity.playerInventory.addItemsToStack(this.sourceItemSlotId, this.itemCount, playerItemType.getItemId());
									}
								}
							}
							break;
					}
				}
			}
		}
	}

	@Override
	public void readPacket(BitReader reader)
	{
		this.sourceItemSlotId = reader.readInt();
		this.transactionType = TransactionType.values()[reader.readInt()];
		this.voxelPositionX = reader.readInt();
		this.voxelPositionY = reader.readInt();
		this.voxelPositionZ = reader.readInt();
		this.targetItemSlotId = reader.readInt();
		this.itemCount = reader.readInt();
	}

	@Override
	public void writePacket(BitWriter builder)
	{
		builder.writeInt(this.sourceItemSlotId);
		builder.writeInt(this.transactionType.getValue());
		builder.writeInt(this.voxelPositionX);
		builder.writeInt(this.voxelPositionY);
		builder.writeInt(this.voxelPositionZ);
		builder.writeInt(this.targetItemSlotId);
		builder.writeInt(this.itemCount);
	}

}
