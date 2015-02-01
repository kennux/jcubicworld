package net.kennux.cubicworld.gui.overlay;

import net.kennux.cubicworld.gui.IGuiElement;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * <pre>
 * This interface gets used to define xml gui element instantiation.
 * You can use implementations of this interface to register new gui xml elements to the XMLOverlayLoader.
 * </pre>
 * 
 * @author KennuX
 *
 */
public interface IXMLObjectLoader
{
	public IGuiElement load(ObjectMap<String, String> elementAttributes, Rectangle absoluteElementRectangle, Rectangle relativeElementRectangle, String id, Overlay overlay);
}
