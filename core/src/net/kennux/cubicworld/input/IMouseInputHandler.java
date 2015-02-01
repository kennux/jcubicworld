package net.kennux.cubicworld.input;

import com.badlogic.gdx.math.Vector2;

/**
 * This interface defines how mouse input will get handled.
 * Implement this interface in a class and attach it to the InputManager.
 * 
 * @author KennuX
 *
 */
public interface IMouseInputHandler
{
	/**
	 * Gets called every frame if a mouse button was pressed.
	 * 
	 * @param buttonId
	 */
	public void mouseButtonPressed(int buttonId);

	/**
	 * Gets called every frame if a mouse button was released.
	 * 
	 * @param buttonId
	 */
	public void mouseButtonReleased(int buttonId);

	/**
	 * Gets called in every frame if the mouse got moved.
	 * 
	 * @param difference
	 *            The position difference between the current frame and the last frame.
	 * @param absolute
	 *            The absolute position of the mouse in the current frame.
	 */
	public void mouseMoved(Vector2 difference, Vector2 absolute);
}
