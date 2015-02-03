package net.kennux.cubicworld.item;

/**
 * <pre>
 * Items are divided into normal "useless" items, which can only get dropped or used for crafting.
 * There are also "tools" which change the behaviour of the player fps / tps rendering, for example it can be an
 * Weapon or a tool like a pickaxe.
 * 
 * </pre>
 * 
 * @author kennux
 *
 */
public enum ItemClass
{
	ITEM, TOOL, WEAPON, CONSUMABLE
}
