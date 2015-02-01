package net.kennux.cubicworld.plugins.baseplugin.gui;

import net.kennux.cubicworld.gui.IGuiElement;
import net.kennux.cubicworld.gui.elements.Button;
import net.kennux.cubicworld.gui.overlay.IXMLObjectLoader;
import net.kennux.cubicworld.gui.overlay.Overlay;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ObjectMap;

public class XMLButtonLoader implements IXMLObjectLoader
{

	@Override
	public IGuiElement load(ObjectMap<String, String> elementAttributes, Rectangle absoluteElementRectangle, Rectangle relativeElementRectangle, String id, Overlay overlay)
	{
		// Get values
		float fontScale = Float.parseFloat(elementAttributes.get("font-scale"));
		String caption = elementAttributes.get("caption");

		// Add element to gui overlay
		Button btn = new Button(absoluteElementRectangle, relativeElementRectangle, caption);
		btn.setFontScale(fontScale);

		return btn;
	}

}
