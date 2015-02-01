package net.kennux.cubicworld.networking.packet.inventory;

/**
 * <pre>
 * Defines different type of inventory transactions.
 * 
 * PLAYER_TO_INVENTORY -> Player to block inventory
 * INVENTORY_TO_PLAYER -> Block inventory to player
 * 
 * @see ClientItemTransaction
 * </pre>
 * 
 * @author KennuX
 *
 */
public enum TransactionType
{
	PLAYER_TO_INVENTORY(0), INVENTORY_TO_PLAYER(1);

	private final int value;

	private TransactionType(int value)
	{
		this.value = value;
	}

	public int getValue()
	{
		return value;
	}
}
