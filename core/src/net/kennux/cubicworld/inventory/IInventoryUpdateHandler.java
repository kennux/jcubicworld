package net.kennux.cubicworld.inventory;

/**
 * Handles inventory updates.
 * 
 * @author KennuX
 *
 */
public interface IInventoryUpdateHandler
{
	/**
	 * Gets called when the inventory got updated.
	 */
	public void inventoryGotUpdated(IInventory inventory);
}
