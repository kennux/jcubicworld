package net.kennux.cubicworld.gui.events;

import com.badlogic.gdx.math.Vector2;

/**
 * You can pass an anoynmous implementation of this in the button-constructor.
 * 
 * @author kennux
 *
 */
public interface IClickHandler
{
	/**
	 * Handles a click.
	 * mousePosition is the mouse position in screenspace at the moment the user clicked.
	 * 
	 * @param mousePosition
	 */
	public void handleClick(Vector2 mousePosition);
}
