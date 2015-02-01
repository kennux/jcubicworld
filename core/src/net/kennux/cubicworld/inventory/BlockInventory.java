package net.kennux.cubicworld.inventory;

import net.kennux.cubicworld.item.ItemStack;

/**
 * <pre>
 * Inventory implementation which contains all functions needed to manage a block's inventory.
 * This class is fully thread-safe!
 * </pre>
 * 
 * @author KennuX
 *
 */
public class BlockInventory extends BasicInventory
{
	/**
	 * The inventory filter rule set of this block inventory.
	 */
	private InventoryFilterRuleSet filterRuleSet;

	/**
	 * Creates a new block inventory with the given slots count.
	 * 
	 * @param slots
	 */
	public BlockInventory(int slots)
	{
		super(slots);
	}

	/**
	 * @return the filterRuleSet
	 */
	public InventoryFilterRuleSet getFilterRuleSet()
	{
		return filterRuleSet;
	}

	/**
	 * @param filterRuleSet
	 *            the filterRuleSet to set
	 */
	public void setFilterRuleSet(InventoryFilterRuleSet filterRuleSet)
	{
		this.filterRuleSet = filterRuleSet;
	}

	@Override
	public boolean setItemStackInSlot(int slotId, ItemStack item)
	{
		if (this.filterRuleSet != null && item.getType() != null)
			if (!this.filterRuleSet.filter(slotId, item.getType()))
				return false;

		return super.setItemStackInSlot(slotId, item);
	}
}
