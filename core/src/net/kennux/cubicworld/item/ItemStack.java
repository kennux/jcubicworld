package net.kennux.cubicworld.item;

import net.kennux.cubicworld.serialization.BitReader;
import net.kennux.cubicworld.serialization.BitWriter;

/**
 * Represents a stack of items.
 * A stack always has an itemtype and a size.
 * 
 * This class is fully thread safe.
 * 
 * @author KennuX
 *
 */
public class ItemStack
{
	/**
	 * Deserializes the item stack data from the given bit reader.
	 * 
	 * @param bitReader
	 */
	public static ItemStack deserialize(BitReader bitReader)
	{
		return new ItemStack(bitReader.readInt(), bitReader.readInt());
	}

	private Object stackLockObject = new Object();

	/**
	 * The item type.
	 */
	private ItemType type;

	/**
	 * The count of items in the current stack.
	 */
	private int itemCount;

	/**
	 * Constructs a new item stack by the given item id and count.
	 * 
	 * @param itemId
	 * @param count
	 */
	public ItemStack(int itemId, int count)
	{
		this.type = ItemSystem.getItemType(itemId);
		this.itemCount = (count > this.type.getStackSize()) ? this.type.getStackSize() : count;
	}

	/**
	 * Adds the given count to this item stack.
	 * This function will execute hasSpaceFor() before actually adding items.
	 * You dont seperately have to check if the stack has enough space.
	 * 
	 * @param count
	 * @return True if the action was successfull, false otherwise.
	 */
	public boolean addItems(int count)
	{
		synchronized (this.stackLockObject)
		{
			if (this.hasSpaceFor(count))
			{
				this.itemCount += count;
				return true;
			}

			return false;
		}
	}

	/**
	 * @return the itemCount
	 */
	public int getItemCount()
	{
		synchronized (this.stackLockObject)
		{
			return itemCount;
		}
	}

	/**
	 * @return the type
	 */
	public ItemType getType()
	{
		synchronized (this.stackLockObject)
		{
			return type;
		}
	}

	/**
	 * Returns true if this stack has atleast the given count of items of the same type.
	 * 
	 * @param count
	 * @return
	 */
	public boolean hasAtleast(int count)
	{
		synchronized (this.stackLockObject)
		{
			if (this.itemCount >= count)
				return true;
			return false;
		}
	}

	/**
	 * Returns true if this stack has space for the given count of items of the same type.
	 * 
	 * @param count
	 * @return
	 */
	public boolean hasSpaceFor(int count)
	{
		synchronized (this.stackLockObject)
		{
			if (this.itemCount + count <= this.type.getStackSize())
				return true;
			return false;
		}
	}

	/**
	 * Removes the count of items from this item stack.
	 * 
	 * @param count
	 * @return True if it was successfull, false otherwise.
	 */
	public boolean removeItems(int count)
	{
		synchronized (this.stackLockObject)
		{
			if (this.itemCount < count)
				return false;

			this.itemCount -= count;
			return true;
		}
	}

	/**
	 * Serializes this itemstack to the given bitWriter.
	 * [ItemID - Int32][ItemCount - Short]
	 * 
	 * @return
	 */
	public void serialize(BitWriter bitWriter)
	{
		synchronized (this.stackLockObject)
		{
			bitWriter.writeInt(this.type.getItemId());
			bitWriter.writeInt(this.itemCount);
		}
	}

	/**
	 * @param itemCount
	 *            the itemCount to set
	 */
	public void setItemCount(int itemCount)
	{
		synchronized (this.stackLockObject)
		{
			this.itemCount = itemCount;
		}
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(ItemType type)
	{
		synchronized (this.stackLockObject)
		{
			this.type = type;
		}
	}
}