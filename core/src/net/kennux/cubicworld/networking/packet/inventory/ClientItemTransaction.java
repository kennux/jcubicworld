package net.kennux.cubicworld.networking.packet.inventory;

import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.CubicWorldServer;
import net.kennux.cubicworld.item.ItemStack;
import net.kennux.cubicworld.item.ItemType;
import net.kennux.cubicworld.networking.APacketModel;
import net.kennux.cubicworld.networking.BitReader;
import net.kennux.cubicworld.networking.BitWriter;
import net.kennux.cubicworld.networking.CubicWorldServerClient;
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
		transaction.playerItemSlotId = playerItemSlotId;
		transaction.transactionType = transactionType;
		transaction.voxelPositionX = voxelPositionX;
		transaction.voxelPositionY = voxelPositionY;
		transaction.voxelPositionZ = voxelPositionZ;
		transaction.voxelItemSlotId = voxelItemSlotId;
		transaction.itemCount = itemCount;

		return transaction;
	}

	/**
	 * The item slot id in the player inventory.
	 */
	public int playerItemSlotId;

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
	public int voxelItemSlotId;

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
				ItemStack playerItemStack = client.playerEntity.playerInventory.getItemStackInSlot(this.playerItemSlotId);
				ItemStack voxelItemStack = inventoryVoxel.blockInventory.getItemStackInSlot(this.voxelItemSlotId);

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
							if (voxelItemStack.getItemCount() >= this.itemCount && client.playerEntity.playerInventory.getFreeSpaceInSlot(this.playerItemSlotId) >= this.itemCount)
							{
								// Perform inventory -> player transaction
								// Remove from voxel
								if (inventoryVoxel.blockInventory.removeItemsFromStack(this.voxelItemSlotId, this.itemCount))
								{
									// Add items to player
									if (!client.playerEntity.playerInventory.addItemsToStack(this.playerItemSlotId, this.itemCount))
									{
										// Rollback if the changes weren't successfull
										inventoryVoxel.blockInventory.addItemsToStack(this.voxelItemSlotId, this.itemCount, voxelItemType.getItemId());
									}
								}
							}
							break;
						case PLAYER_TO_INVENTORY:
							// Validity checks
							if (playerItemStack.getItemCount() >= this.itemCount && inventoryVoxel.blockInventory.getFreeSpaceInSlot(this.voxelItemSlotId) >= this.itemCount)
							{
								// Perform inventory -> player transaction
								// Remove from player
								if (client.playerEntity.playerInventory.removeItemsFromStack(this.playerItemSlotId, this.itemCount))
								{
									// Add items to voxel
									if (!inventoryVoxel.blockInventory.addItemsToStack(this.voxelItemSlotId, this.itemCount))
									{
										// Rollback if the changes weren't successfull
										client.playerEntity.playerInventory.addItemsToStack(this.playerItemSlotId, this.itemCount, voxelItemType.getItemId());
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
							if (!client.playerEntity.playerInventory.hasItemStackInSlot(this.playerItemSlotId) && inventoryVoxel.blockInventory.getItemCountInSlot(this.voxelItemSlotId) >= this.itemCount)
							{
								// Remove items from voxel
								if (inventoryVoxel.blockInventory.removeItemsFromStack(this.voxelItemSlotId, this.itemCount))
								{
									// Add to players inventory
									if (!client.playerEntity.playerInventory.addItemsToStack(this.playerItemSlotId, this.itemCount, voxelItemType.getItemId()))
									{
										// Rollback if adding was not successfull
										inventoryVoxel.blockInventory.addItemsToStack(this.voxelItemSlotId, this.itemCount, voxelItemType.getItemId());
									}
								}
							}
							break;
						case PLAYER_TO_INVENTORY:
							// Validity check
							if (!inventoryVoxel.blockInventory.hasItemStackInSlot(this.voxelItemSlotId) && client.playerEntity.playerInventory.getItemCountInSlot(this.playerItemSlotId) >= this.itemCount)
							{
								// Remove items from player
								if (client.playerEntity.playerInventory.removeItemsFromStack(this.playerItemSlotId, this.itemCount))
								{
									// Add to voxels inventory
									if (!inventoryVoxel.blockInventory.addItemsToStack(this.voxelItemSlotId, this.itemCount, playerItemType.getItemId()))
									{
										// Rollback if adding was not successfull
										client.playerEntity.playerInventory.addItemsToStack(this.playerItemSlotId, this.itemCount, playerItemType.getItemId());
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
		this.playerItemSlotId = reader.readInt();
		this.transactionType = TransactionType.values()[reader.readInt()];
		this.voxelPositionX = reader.readInt();
		this.voxelPositionY = reader.readInt();
		this.voxelPositionZ = reader.readInt();
		this.voxelItemSlotId = reader.readInt();
		this.itemCount = reader.readInt();
	}

	@Override
	public void writePacket(BitWriter builder)
	{
		builder.writeInt(this.playerItemSlotId);
		builder.writeInt(this.transactionType.getValue());
		builder.writeInt(this.voxelPositionX);
		builder.writeInt(this.voxelPositionY);
		builder.writeInt(this.voxelPositionZ);
		builder.writeInt(this.voxelItemSlotId);
		builder.writeInt(this.itemCount);
	}

}
