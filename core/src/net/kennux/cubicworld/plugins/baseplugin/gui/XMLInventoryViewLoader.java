package net.kennux.cubicworld.plugins.baseplugin.gui;

import net.kennux.cubicworld.gui.IGuiElement;
import net.kennux.cubicworld.gui.elements.InventoryView;
import net.kennux.cubicworld.gui.overlay.IXMLObjectLoader;
import net.kennux.cubicworld.gui.overlay.Overlay;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ObjectMap;

public class XMLInventoryViewLoader implements IXMLObjectLoader
{

	@Override
	public IGuiElement load(ObjectMap<String, String> elementAttributes, Rectangle absoluteElementRectangle, Rectangle relativeElementRectangle, String id, Overlay overlay)
	{
		// Get additional attributes
		int startSlotId = Integer.parseInt(elementAttributes.get("start-slot-id"));
		int endSlotId = Integer.parseInt(elementAttributes.get("end-slot-id"));
		float cellspacing = Float.parseFloat(elementAttributes.get("cellspacing").replace("%", ""));
		int slotsPerRow = Integer.parseInt(elementAttributes.get("slots-per-row"));
		String inventory = elementAttributes.get("inventory");

		return new InventoryView(overlay, absoluteElementRectangle, relativeElementRectangle, id, startSlotId, endSlotId, slotsPerRow, inventory, cellspacing);
	}

}
