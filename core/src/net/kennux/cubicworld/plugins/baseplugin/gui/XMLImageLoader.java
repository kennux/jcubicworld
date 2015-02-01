package net.kennux.cubicworld.plugins.baseplugin.gui;

import net.kennux.cubicworld.gui.IGuiElement;
import net.kennux.cubicworld.gui.elements.GuiImage;
import net.kennux.cubicworld.gui.overlay.IXMLObjectLoader;
import net.kennux.cubicworld.gui.overlay.Overlay;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ObjectMap;

public class XMLImageLoader implements IXMLObjectLoader
{

	@Override
	public IGuiElement load(ObjectMap<String, String> elementAttributes, Rectangle absoluteElementRectangle, Rectangle relativeElementRectangle, String id, Overlay overlay)
	{
		// Get values
		String texturePath = elementAttributes.get("texture");

		// Add element to gui overlay
		return new GuiImage(absoluteElementRectangle, relativeElementRectangle, texturePath);
	}

}
