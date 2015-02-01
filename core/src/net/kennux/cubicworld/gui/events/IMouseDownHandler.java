package net.kennux.cubicworld.gui.events;

import com.badlogic.gdx.math.Vector2;

public interface IMouseDownHandler
{
	/**
	 * Handles a mouse down event.
	 * Gets fired immediately after the user pressed a mouse button down on an gui element.
	 * 
	 * @param mousePosition
	 * @param mouseButton
	 */
	public void handleMouseDown(Vector2 mousePosition, int mouseButton);
}
