package net.kennux.cubicworld.item;

/**
 * <pre>
 * Item implementation.
 * Every item ingame is an instance of this object.
 * </pre>
 * 
 * @author kennux
 *
 */
public class Item
{
	/**
	 * The item type of this item.
	 */
	private ItemType type;

	/**
	 * Constructs a new item instance.
	 * 
	 * @param type
	 */
	public Item(ItemType type)
	{
		this.type = type;
	}

	/**
	 * Returns the item view instance of this item's type.
	 * 
	 * @return
	 */
	public IItemView getItemView()
	{
		return this.type.getItemView();
	}

	/**
	 * @return the type
	 */
	public ItemType getType()
	{
		return type;
	}

	/**
	 * Checks if the item type is a type of the given class.
	 * 
	 * @param type
	 * @return
	 */
	public boolean isClass(ItemClass clazz)
	{
		return this.type.getType().equals(clazz);
	}
}
