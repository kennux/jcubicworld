package net.kennux.cubicworld.inventory;

import net.kennux.cubicworld.item.ItemType;

/**
 * This interface defines an inventory filter rule.
 * A voxel type can hold one InventoryFilterSet
 * 
 * @author KennuX
 *
 */
public interface IInventoryFilterRule
{
	/**
	 * Return true if the item type passes this filter rule, false otherwise
	 * 
	 * @param itemType
	 * @return
	 */
	public boolean filter(ItemType itemType);
}
