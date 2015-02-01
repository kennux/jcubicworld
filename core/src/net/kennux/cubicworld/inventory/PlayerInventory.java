package net.kennux.cubicworld.inventory;

/**
 * <pre>
 * Inventory implementation which contains all functions needed to manage the player inventory.
 * This class is fully thread-safe!
 * </pre>
 * 
 * @author KennuX
 *
 */
public class PlayerInventory extends BasicInventory
{
	public PlayerInventory()
	{
		super(30);
	}

}
