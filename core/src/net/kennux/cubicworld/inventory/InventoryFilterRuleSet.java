package net.kennux.cubicworld.inventory;

import java.util.HashMap;

import net.kennux.cubicworld.item.ItemType;

public class InventoryFilterRuleSet
{
	/**
	 * The filter rules for the filter rule set.
	 */
	private HashMap<Integer, IInventoryFilterRule[]> filterRules = new HashMap<Integer, IInventoryFilterRule[]>();

	/**
	 * Checks all filter rules for the given slot id.
	 * Forwards the filter call to the filter rule if there is one.
	 * This function checks if atleast one filter for the given slot passes.
	 * 
	 * @param slotId
	 * @param itemType
	 * @return
	 */
	public boolean filter(int slotId, ItemType itemType)
	{
		IInventoryFilterRule[] ruleArray = this.filterRules.get(new Integer(slotId));

		// Check all filters
		if (ruleArray != null)
		{
			for (IInventoryFilterRule rule : ruleArray)
			{
				if (rule.filter(itemType))
					return true;
			}
			return false;
		}

		return true;
	}

	/**
	 * Sets all filter rules for the given slot id.
	 * 
	 * @param rules
	 * @param slotId
	 */
	public void setRules(int slotId, IInventoryFilterRule... rules)
	{
		this.filterRules.put(new Integer(slotId), rules);
	}
}
