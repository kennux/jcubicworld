package net.kennux.cubicworld.plugins.baseplugin.gui;

import net.kennux.cubicworld.gui.IGuiElement;
import net.kennux.cubicworld.gui.elements.InventorySlot;
import net.kennux.cubicworld.gui.overlay.IXMLObjectLoader;
import net.kennux.cubicworld.gui.overlay.Overlay;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ObjectMap;

public class XMLInventorySlotLoader implements IXMLObjectLoader
{

	@Override
	public IGuiElement load(ObjectMap<String, String> elementAttributes, Rectangle absoluteElementRectangle, Rectangle relativeElementRectangle, String id, Overlay overlay)
	{
		int inventorySlotId = Integer.parseInt(elementAttributes.get("slot-id"));
		String inventory = elementAttributes.get("inventory");

		return new InventorySlot(overlay, absoluteElementRectangle, relativeElementRectangle, inventorySlotId, inventory);
	}

}
