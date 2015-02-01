package net.kennux.cubicworld.gui.events;

import com.badlogic.gdx.math.Vector2;

public interface IMouseUpHandler
{
	/**
	 * Handles a mouse up event.
	 * Gets fired immediately after the user released a mouse button on an gui element.
	 * 
	 * @param mousePosition
	 * @param mouseButton
	 */
	public void handleMouseUp(Vector2 mousePosition, int mouseButton);
}
