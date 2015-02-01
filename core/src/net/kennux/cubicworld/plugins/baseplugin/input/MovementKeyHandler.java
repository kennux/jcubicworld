package net.kennux.cubicworld.plugins.baseplugin.input;

import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.input.IKeyInputHandler;

/**
 * Abstract movement key handler class.
 * 
 * @author KennuX
 *
 */
public abstract class MovementKeyHandler implements IKeyInputHandler
{
	/**
	 * Gets set to true when key is pressed, set to false when key is released.
	 */
	protected boolean isPressed;

	@Override
	public void keyPressed(CubicWorldGame cubicWorld)
	{
		this.isPressed = true;
	}

	@Override
	public void keyReleased(CubicWorldGame cubicWorld)
	{
		this.isPressed = false;
	}

	/**
	 * Gets called every frame this key is pressed.
	 * 
	 */
	public abstract void move();

	/**
	 * Function to update
	 */
	public void update()
	{
		if (this.isPressed)
			this.move();
	}

}
