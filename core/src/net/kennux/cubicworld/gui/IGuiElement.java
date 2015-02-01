package net.kennux.cubicworld.gui;

import net.kennux.cubicworld.gui.events.IClickHandler;
import net.kennux.cubicworld.gui.events.IMouseDownHandler;
import net.kennux.cubicworld.gui.events.IMouseUpHandler;
import net.kennux.cubicworld.gui.skin.AGuiSkin;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * <pre>
 * Single gui element interface.
 * A GUI Element is for example a button.
 * 
 * It can get rendered to the screen or accept inputs.
 * Fous setting gets handled this way:
 * 
 * If the current element does not have focus and the user clicks at it, it will get focus.
 * The focus will control which input events are sent to the handler.
 * 
 * If the object has no focus, only direct click events will get fired.
 * Otherwise also keyboard events will get fired.
 * </pre>
 * 
 * @author KennuX
 *
 */
public interface IGuiElement
{
	/**
	 * <pre>
	 * Return your input handler in this function.
	 * I would recommend using an anonymous class instead of a real implementation of this interface.
	 * Anyway, both are possible.
	 * 
	 * May return null if there is no input processor.
	 * </pre>
	 * 
	 * @return
	 */
	public IGuiInputHandler getInputHandler();

	/**
	 * Renders and updates the gui element.
	 * This rendering method is only for rendering the gui base.
	 * Example in the InventorySlot this element will not get used if the item is dragged and should be rendered last, so it will get rendered in renderLast().
	 * 
	 * @param parent
	 *            Null if this element has no parent. (Should actually never happen when using overlays)
	 */
	public void render(SpriteBatch spriteBatch, BitmapFont font, boolean hasFocus, AGuiSkin skin);

	/**
	 * This pass is used for rendering on top of the gui, for example the InventorySlot uses this function for rendering dragged items.
	 * TODO May remove this and find a better method?
	 * 
	 * @param spriteBatch
	 * @param font
	 * @param hasFocus
	 * @param skin
	 */
	public void renderLast(SpriteBatch spriteBatch, BitmapFont font, boolean hasFocus, AGuiSkin skin);

	/**
	 * @param clickHandler
	 *            the clickHandler to set
	 */
	public void setClickHandler(IClickHandler clickHandler);

	/**
	 * @param mouseDownHandler
	 *            the mouse down handler to use when events get fired.
	 */
	public void setMouseDownHandler(IMouseDownHandler mouseDownHandler);

	/**
	 * @param mouseDownHandler
	 *            the mouse down handler to use when events get fired.
	 */
	public void setMouseUpHandler(IMouseUpHandler mouseUpHandler);
}
