package net.kennux.cubicworld.gui.overlay;

import java.util.HashMap;

/**
 * <pre>
 * The overlay data is a hashmap with strings as keys and objects as values.
 * It will get set to overlay's if the overlay needs to display information like an inventory.
 * Conventional keynames:
 * 
 * "inventory" -> Inventory, example for block guis. Will get used for XML <inventoryslot> or <inventoryview> tags. With attribute inventory="inventory". TODO: Maybe this is too obvious? :P
 * "playerInventory" -> Reference to the player's inventory. Can also get used with the <inventoryslot> or <inventoryview> tag with attribute inventory="player"
 * "voxelPos" -> Voxel position
 * </pre>
 * 
 * @author KennuX
 *
 */
public class OverlayData extends HashMap<String, Object>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
