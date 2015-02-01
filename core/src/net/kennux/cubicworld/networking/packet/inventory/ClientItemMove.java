package net.kennux.cubicworld.networking.packet.inventory;

import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.CubicWorldServer;
import net.kennux.cubicworld.inventory.BlockInventory;
import net.kennux.cubicworld.item.ItemStack;
import net.kennux.cubicworld.item.ItemType;
import net.kennux.cubicworld.networking.APacketModel;
import net.kennux.cubicworld.networking.BitReader;
import net.kennux.cubicworld.networking.BitWriter;
import net.kennux.cubicworld.networking.CubicWorldServerClient;
import net.kennux.cubicworld.voxel.VoxelData;

import com.badlogic.gdx.math.Vector3;

/**
 * <pre>
 * The client item move packet will get used to move items in player or block inventories to other slots.
 * This packet's serverside logic also considers stacking, so moving a stack of 10 coal to another with 15 on it it will create a 25 items stack in the target.
 * </pre>
 * 
 * @author KennuX
 *
 */
public class ClientItemMove extends APacketModel
{
	/**
	 * Creates a client item move packet model object for the player inventory.
	 * After creating it on the client, just send it to the server.
	 * 
	 * @param sourceSlotId
	 * @param targetSlotId
	 * @return
	 */
	public static ClientItemMove createPlayerInventoryMove(int sourceSlotId, int targetSlotId)
	{
		ClientItemMove move = new ClientItemMove();

		move.sourceSlotId = sourceSlotId;
		move.targetSlotId = targetSlotId;

		return move;
	}

	/**
	 * Creates a client item move packet model object for the voxel inventory.
	 * After creating it on the client, just send it to the server.
	 * 
	 * @param sourceSlotId
	 * @param targetSlotId
	 * @param voxelPosition
	 * @return
	 */
	public static ClientItemMove createVoxelInventoryMove(int sourceSlotId, int targetSlotId, Vector3 voxelPosition)
	{
		ClientItemMove move = new ClientItemMove();

		move.sourceSlotId = sourceSlotId;
		move.targetSlotId = targetSlotId;
		move.voxelPosition = voxelPosition;

		return move;
	}

	/**
	 * The source item slot id.
	 */
	public int sourceSlotId;

	/**
	 * The target item slot id.
	 */
	public int targetSlotId;

	/**
	 * Set this only if you want to move an item in the inventory of a voxel.
	 */
	public Vector3 voxelPosition = null;

	@Override
	public void interpretClientSide(CubicWorldGame cubicWorld)
	{
	}

	@Override
	public void interpretServerSide(CubicWorldServer server, CubicWorldServerClient client)
	{
		// Voxel inventory?
		if (this.voxelPosition != null)
		{
			// Voxel inventory!
			// Get voxel data
			VoxelData voxelData = server.voxelWorld.getVoxel((int) this.voxelPosition.x, (int) this.voxelPosition.y, (int) this.voxelPosition.z);

			if (voxelData != null && voxelData.blockInventory != null)
			{
				// Get the inventory
				BlockInventory inventory = voxelData.blockInventory;

				// Get source and target information
				ItemStack sourceStack = inventory.getItemStackInSlot(this.sourceSlotId);
				ItemStack targetStack = inventory.getItemStackInSlot(this.targetSlotId);
				ItemType sourceType = null;
				ItemType targetType = null;

				if (targetStack != null)
					targetType = targetStack.getType();
				if (sourceStack != null)
					sourceType = sourceStack.getType();

				// Move item in the voxel inventory if possible
				// This checks for:
				// - Source stack not null (source slot not empty)
				// - Target stack empty (target slot empty) OR
				// - Target and source item types (ids) are equal AND target stack has space for the count of items in sourceStack.
				if (sourceStack != null && (targetStack == null || (targetType.getItemId() == sourceType.getItemId() && targetStack.hasSpaceFor(sourceStack.getItemCount()))))
				{
					int sourceItemCount = sourceStack.getItemCount();

					// Remove items from stack
					if (inventory.removeItemsFromStack(this.sourceSlotId, sourceStack.getItemCount()))
					{
						// If that was a success, add them to the target stack
						if (!inventory.addItemsToStack(this.targetSlotId, sourceItemCount, sourceType.getItemId()))
						{
							// If it was not successfull, rollback
							inventory.addItemsToStack(this.sourceSlotId, sourceItemCount, sourceType.getItemId());
						}
					}
				}
			}
		}
		else if (client.playerEntity != null && client.playerEntity.playerInventory != null) // Player inventory
		{
			// Get source and target stack
			ItemStack sourceStack = client.playerEntity.playerInventory.getItemStackInSlot(this.sourceSlotId);
			ItemStack targetStack = client.playerEntity.playerInventory.getItemStackInSlot(this.targetSlotId);
			ItemType sourceType = null;
			ItemType targetType = null;

			if (targetStack != null)
				targetType = targetStack.getType();
			if (sourceStack != null)
				sourceType = sourceStack.getType();

			// Move item in the player inventory if possible
			// This checks for:
			// - Source stack not null (source slot not empty)
			// - Target stack empty (target slot empty) OR
			// - Target and source item types (ids) are equal AND target stack has space for the count of items in sourceStack.
			if (sourceStack != null && (targetStack == null || (targetType.getItemId() == sourceType.getItemId() && targetStack.hasSpaceFor(sourceStack.getItemCount()))))
			{
				int sourceItemCount = sourceStack.getItemCount();

				// Remove items from stack
				if (client.playerEntity.playerInventory.removeItemsFromStack(this.sourceSlotId, sourceStack.getItemCount()))
				{
					// If that was a success, add them to the target stack
					if (!client.playerEntity.playerInventory.addItemsToStack(this.targetSlotId, sourceItemCount, sourceType.getItemId()))
					{
						// If it was not successfull, rollback
						client.playerEntity.playerInventory.addItemsToStack(this.sourceSlotId, sourceItemCount, sourceType.getItemId());
					}
				}
			}
		}
	}

	@Override
	public void readPacket(BitReader reader)
	{
		byte inventory = reader.readByte();

		if (inventory == 1)
		{
			// Get inventory pos
			this.voxelPosition = reader.readVector3();
		}

		this.sourceSlotId = reader.readInt();
		this.targetSlotId = reader.readInt();
	}

	@Override
	public void writePacket(BitWriter builder)
	{
		if (this.voxelPosition == null)
		{
			// Player inventory
			builder.writeByte((byte) 0);
		}
		else
		{
			// Voxel inventory
			builder.writeByte((byte) 1);
			builder.writeVector3(this.voxelPosition);
		}
		builder.writeInt(this.sourceSlotId);
		builder.writeInt(this.targetSlotId);
	}

}
