package net.kennux.cubicworld.gui;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Represents a gui input handler.
 * It can accept inputs if the gui element which returned the handler got the current focus.
 * 
 * Mouse events:
 * down - Gets called once after the player pressed the mouse button
 * up - Gets called once after the player released the mouse button
 * click - Gets called after up.
 * 
 * @author KennuX
 *
 */
public interface IGuiInputHandler
{
	/**
	 * Returns a rectangle which contains the space this element occupies.
	 * Focus setting and input event forwarding will be done if the user clicks a point which intersects this rectangle.
	 * 
	 * @return
	 */
	public Rectangle getBoundingRectangle();

	/**
	 * Handles a direct click event.
	 * A click event will get fired no matter if the element currently has focus.
	 * 
	 * @param mousePosition
	 *            The mouse position at the time the click happend
	 * @param mouseButton
	 *            The mouse button used to click at this element. 0 = left, 1 = right, ...
	 */
	public void handleClick(Vector2 mousePosition, int mouseButton);

	/**
	 * <pre>
	 * This event will get fired if the element has focus and the user typed a key.
	 * Typed means he pressed and released it again.
	 * 
	 * This function is meant for use with for example textboxes.
	 * </pre>
	 * 
	 * @param keyId
	 * @param typedChar
	 */
	public void handleKeyType(char typedChar);

	/**
	 * Handles a direct mouse down.
	 * A mouse down event will get fired no matter if the element currently has focus.
	 * 
	 * @param mousePosition
	 *            The mouse position at the time the click happend
	 * @param mouseButton
	 *            The mouse button used to click at this element. 0 = left, 1 = right, ...
	 */
	public void handleMouseDown(Vector2 mousePosition, int mouseButton);

	/**
	 * Handles a direct mouse up.
	 * A mouse down event will get fired no matter if the element currently has focus.
	 * 
	 * @param mousePosition
	 *            The mouse position at the time the click happend
	 * @param mouseButton
	 *            The mouse button used to click at this element. 0 = left, 1 = right, ...
	 */
	public void handleMouseUp(Vector2 mousePosition, int mouseButton);
}
