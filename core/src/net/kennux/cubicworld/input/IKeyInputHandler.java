package net.kennux.cubicworld.input;

import net.kennux.cubicworld.CubicWorldGame;

/**
 * <pre>
 * This interface gets used for defining keyboard input actions.
 * Register your controls to the InputManager by implementing an anonymous class which implements this interface.
 * </pre>
 * 
 * @author KennuX
 *
 */
public interface IKeyInputHandler
{
	/**
	 * Gets called when the key was pressed.
	 */
	public void keyPressed(CubicWorldGame cubicWorld);

	/**
	 * Gets called in the frame when the key got released ONCE.
	 */
	public void keyReleased(CubicWorldGame cubicWorld);
}
