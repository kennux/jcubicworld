package net.kennux.cubicworld.inventory;

import net.kennux.cubicworld.item.ItemStack;
import net.kennux.cubicworld.serialization.BitReader;
import net.kennux.cubicworld.serialization.BitWriter;

/**
 * <pre>
 * Every inventory has to implement this interface!
 * An inventory for example can be a player inventory, or a chest inventory, or a machine inventory (...).
 * 
 * This interface defines general functions needed for all inventories.
 * 
 * In general an inventory consists of slots, every slot has an index (starting at 0).
 * When implementing inventory guis, the convention is displaying items from left to right, so in the gui slot x=0, y=0 there is item in slot 0.
 * In slot x=1, y=0 there will be 1, in x=2, y=0 there will be 2.
 * </pre>
 * 
 * @author kennux
 *
 */
public interface IInventory
{
	/**
	 * Adds items to the stack in the given slot id.
	 * It will return true if the action was successfull, false otherwise.
	 * 
	 * @param slotId
	 * @param count
	 * @return
	 */
	public boolean addItemsToStack(int slotId, int count);

	/**
	 * Deserializes the inventory.
	 * 
	 * @return
	 */
	public void deserializeInventory(BitReader reader);

	/**
	 * <pre>
	 * Returns the first free slot id.
	 * Returns -1 if there is no free slot.
	 * </pre>
	 * 
	 * @return
	 */
	public int getFirstFreeSlotId();

	/**
	 * <pre>
	 * Returns the first occupied slot id.
	 * Returns -1 if there is no occupied slot.
	 * </pre>
	 * 
	 * @return
	 */
	public int getFirstOccupiedSlotId();

	/**
	 * <pre>
	 * Returns the first item slot which is:
	 * - Occupied by an item stack
	 * - The occupying item stack's item type has the given item id.
	 * </pre>
	 * 
	 * @param itemId
	 * @return
	 */
	public int getFirstStackForItemTypeWithSpace(int itemId);

	/**
	 * <pre>
	 * Returns the size of this inventory.
	 * </pre>
	 * 
	 * @return
	 */
	public int getInventorySize();

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
	public ItemStack getItemStackInSlot(int slotId);

	/**
	 * <pre>
	 * Returns the last free slot id.
	 * Returns -1 if there is no free slot.
	 * </pre>
	 * 
	 * @return
	 */
	public int getLastFreeSlotId();

	/**
	 * <pre>
	 * Returns the last occupied slot id.
	 * Returns -1 if there is no occupied slot.
	 * </pre>
	 * 
	 * @return
	 */
	public int getLastOccupiedSlotId();

	/**
	 * <pre>
	 * Returns true if the given slot id is occupied by an item stack.
	 * </pre>
	 * 
	 * @param slotId
	 * @return
	 */
	public boolean hasItemStackInSlot(int slotId);

	/**
	 * Removes the given count of items from the stack in the given slot.
	 * 
	 * @param slotId
	 * @param count
	 * @return True if the action was successfull, false otherwise.
	 */
	public boolean removeItemsFromStack(int slotId, int count);

	/**
	 * Serializes the inventory.
	 * 
	 * @return
	 */
	public void serializeInventory(BitWriter writer);

	/**
	 * <pre>
	 * Sets the given item into the given slot.
	 * If this slot is already occupied, it will get overwritten.
	 * Pass in null as item if you want to empty the slot.
	 * </pre>
	 * 
	 * @param item
	 * @param slot
	 * @return true if setting was successfull, false otherwise.
	 */
	public boolean setItemStackInSlot(int slotId, ItemStack item);
}