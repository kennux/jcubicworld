package net.kennux.cubicworld.inventory;

import net.kennux.cubicworld.item.ItemStack;
import net.kennux.cubicworld.serialization.BitReader;
import net.kennux.cubicworld.serialization.BitWriter;

/**
 * Basic inventory implementation.
 * This class is fully thread-safe.
 * 
 * @author KennuX
 *
 */
public class BasicInventory implements IInventory
{
	/**
	 * The items array.
	 */
	private ItemStack[] items;
	private Object itemsLockObject = new Object();

	/**
	 * The inventory update handler.
	 */
	private IInventoryUpdateHandler updateHandler;

	public BasicInventory(int slotCount)
	{
		this.items = new ItemStack[slotCount];
	}

	/**
	 * Adds items to the stack in the given slot id.
	 * It will return true if the action was successfull, false otherwise.
	 * 
	 * @param slotId
	 * @param count
	 * @return
	 */
	public boolean addItemsToStack(int slotId, int count)
	{
		synchronized (this.itemsLockObject)
		{
			if (slotId >= 0 && slotId < this.items.length && this.items[slotId] != null)
			{
				boolean ret = this.items[slotId].addItems(count);
				this.callUpdateHandler();
				return ret;
			}
			return false;
		}
	}

	/**
	 * Adds items to the stack in the given slot id.
	 * This function creates a new item stack if there is none at the given slot.
	 * 
	 * @param slotId
	 * @param count
	 * @param itemId
	 * @return
	 */
	public boolean addItemsToStack(int slotId, int count, int itemId)
	{
		synchronized (this.itemsLockObject)
		{
			if (slotId >= 0 && slotId < this.items.length)
			{
				boolean ret = false;

				// Add items to existing stack
				if (this.items[slotId] != null)
					ret = this.items[slotId].addItems(count);
				else
				{
					// Create new stack
					ItemStack itemStack = new ItemStack(itemId, 0);
					ret = itemStack.addItems(count);

					if (ret)
						ret = ret && this.setItemStackInSlot(slotId, itemStack);
				}

				this.callUpdateHandler();
				return ret;
			}
			return false;
		}
	}

	/**
	 * Calls the update handler if there is one set.
	 */
	private void callUpdateHandler()
	{
		if (this.updateHandler != null)
			this.updateHandler.inventoryGotUpdated(this);
	}

	/**
	 * Deserializes the inventory.
	 * 
	 * @return
	 */
	public void deserializeInventory(BitReader reader)
	{
		synchronized (this.itemsLockObject)
		{
			// First off, we need to clear the current content of the inventory
			for (int i = 0; i < this.items.length; i++)
			{
				this.items[i] = null;
			}

			short occupiedSlots = reader.readShort();
			for (int i = 0; i < occupiedSlots; i++)
			{
				int slotId = reader.readInt();
				this.setItemStackInSlot(slotId, ItemStack.deserialize(reader));
			}
		}
	}

	/**
	 * <pre>
	 * Returns the first free slot id.
	 * Returns -1 if there is no free slot.
	 * </pre>
	 * 
	 * @return
	 */
	public int getFirstFreeSlotId()
	{
		synchronized (this.itemsLockObject)
		{
			for (int i = 0; i < this.items.length; i++)
			{
				if (this.items[i] == null)
					return i;
			}

			return -1;
		}
	}

	/**
	 * <pre>
	 * Returns the first occupied slot id.
	 * Returns -1 if there is no occupied slot.
	 * </pre>
	 * 
	 * @return
	 */
	public int getFirstOccupiedSlotId()
	{
		synchronized (this.itemsLockObject)
		{
			for (int i = 0; i < this.items.length; i++)
			{
				if (this.items[i] != null)
					return i;
			}

			return -1;
		}
	}

	@Override
	public int getFirstStackForItemTypeWithSpace(int itemId)
	{
		synchronized (this.itemsLockObject)
		{
			for (int i = 0; i < this.items.length; i++)
			{
				// Check if: Slot is not empty, stack in the slot's item type id is same as the given one, has space
				if (this.items[i] != null && this.items[i].getType().getItemId() == itemId && this.items[i].getItemCount() < this.items[i].getType().getStackSize())
					return i;
			}

			return -1;
		}
	}

	/**
	 * <pre>
	 * Returns the free item count in the given slot id.
	 * Returns -1 if there is no itemstack.
	 * </pre>
	 * 
	 * @param slotId
	 * @return
	 */
	public int getFreeSpaceInSlot(int slotId)
	{
		synchronized (this.itemsLockObject)
		{
			if (slotId >= 0 && slotId < this.items.length && this.items[slotId] != null)
				return (this.items[slotId].getType().getStackSize() - this.items[slotId].getItemCount());
			else
				return -1;
		}
	}

	/**
	 * <pre>
	 * Returns the size of this inventory.
	 * </pre>
	 * 
	 * @return
	 */
	public int getInventorySize()
	{
		synchronized (this.itemsLockObject)
		{
			return this.items.length;
		}
	}

	/**
	 * Returns the item count of the stack in the given slot id.
	 * If there is no item stack in the given slot id, this will return 0.
	 * 
	 * @param slotId
	 * @return
	 */
	public int getItemCountInSlot(int slotId)
	{
		synchronized (this.itemsLockObject)
		{
			if (slotId >= 0 && slotId < this.items.length && this.items[slotId] != null)
				return this.items[slotId].getItemCount();
			else
				return 0;
		}
	}

	/**
	 * <pre>
	 * Returns the item located in the given slot id.
	 * If the slot is non existing or there is no item in this slot it returns null.
	 * 
	 * IMPORTANT: Never write to an item stack through the reference returned by this function.
	 * Instead use the inventory functions.
	 * </pre>
	 * 
	 * @param slotId
	 * @return
	 */
	public ItemStack getItemStackInSlot(int slotId)
	{
		synchronized (this.itemsLockObject)
		{
			if (slotId >= 0 && slotId < this.items.length)
			{
				return this.items[slotId];
			}
			else
				return null;
		}
	}

	/**
	 * <pre>
	 * Returns the last free slot id.
	 * </pre>
	 * 
	 * @return
	 */
	public int getLastFreeSlotId()
	{
		synchronized (this.itemsLockObject)
		{
			for (int i = this.items.length - 1; i >= 0; i--)
			{
				if (this.items[i] == null)
					return i;
			}

			return -1;
		}
	}

	/**
	 * <pre>
	 * Returns the last occupied slot id.
	 * </pre>
	 * 
	 * @return
	 */
	public int getLastOccupiedSlotId()
	{
		synchronized (this.itemsLockObject)
		{
			for (int i = this.items.length - 1; i >= 0; i--)
			{
				if (this.items[i] != null)
					return i;
			}

			return -1;
		}
	}

	/**
	 * Returns the number of item slots which are occupied by an item stack.
	 * 
	 * @return
	 */
	public int getOccupiedSlotCount()
	{
		int occupied = 0;
		for (int i = 0; i < this.items.length; i++)
		{
			if (this.items[i] != null)
				occupied++;
		}

		return occupied;
	}

	/**
	 * @return the updateHandler
	 */
	public IInventoryUpdateHandler getUpdateHandler()
	{
		return updateHandler;
	}

	/**
	 * <pre>
	 * Returns true if the given slot id is occupied by an item.
	 * </pre>
	 * 
	 * @param slotId
	 * @return
	 */
	public boolean hasItemStackInSlot(int slotId)
	{
		synchronized (this.itemsLockObject)
		{
			if (slotId >= 0 && slotId < this.items.length)
				return (this.items[slotId] != null);
			else
				return false;
		}
	}

	/**
	 * <pre>
	 * Removes the given count of items from the stack in the given slot.
	 * If after the remove the item count of the stack is 0 it will get deleted!
	 * </pre>
	 * 
	 * @param slotId
	 * @param count
	 * @return True if the action was successfull, false otherwise.
	 */
	public boolean removeItemsFromStack(int slotId, int count)
	{
		synchronized (this.itemsLockObject)
		{
			if (slotId >= 0 && slotId < this.items.length && this.items[slotId] != null && this.items[slotId].getItemCount() >= count)
			{
				// Remove items from stack
				boolean ret = this.items[slotId].removeItems(count);

				// If no more items in the stack, delete it.
				if (this.items[slotId].getItemCount() <= 0)
					this.items[slotId] = null;

				this.callUpdateHandler();
				return ret;
			}
			return false;
		}
	}

	/**
	 * Serializes the inventory.
	 * Serialization format:
	 * [SlotID - Int][ItemStack][SlotID - Int][ItemStack][SlotID - Int][ItemStack]...
	 * 
	 * Only a 0byte as slot count means no inventory!
	 * 
	 * @returns
	 */
	public void serializeInventory(BitWriter writer)
	{
		synchronized (this.itemsLockObject)
		{
			// Write occupied slots
			writer.writeShort((short) this.getOccupiedSlotCount());
			for (int i = 0; i < this.items.length; i++)
			{
				if (this.items[i] != null)
				{
					writer.writeInt(i);
					this.items[i].serialize(writer);
				}
			}
		}
	}

	/**
	 * <pre>
	 * Sets the given item into the given slot.
	 * If this slot is already occupied, it will get overwritten.
	 * Pass in null as item if you want to empty the slot.
	 * </pre>
	 * 
	 * @param item
	 * @param slot
	 */
	public boolean setItemStackInSlot(int slotId, ItemStack item)
	{
		synchronized (this.itemsLockObject)
		{
			if (slotId >= 0 && slotId < this.items.length)
			{
				this.items[slotId] = item;
				this.callUpdateHandler();
				return true;
			}
			return false;
		}
	}

	/**
	 * @param updateHandler
	 *            the updateHandler to set
	 */
	public void setUpdateHandler(IInventoryUpdateHandler updateHandler)
	{
		this.updateHandler = updateHandler;
	}
}
