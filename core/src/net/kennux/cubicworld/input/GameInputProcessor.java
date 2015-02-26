package net.kennux.cubicworld.input;

import net.kennux.cubicworld.CubicWorldGame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;

/**
 * <pre>
 * The player inputprocessor.
 * It handles input processing for everything related to the PlayerController.
 * 
 * TODO: Update documentation
 * </pre>
 * 
 * @author KennuX
 *
 */
public class GameInputProcessor extends InputAdapter
{
	/**
	 * If this is set to false no input will get processed.
	 */
	private boolean processInput = true;

	/**
	 * The master instance.
	 */
	private CubicWorldGame cubicWorld;

	/**
	 * Contains the last mouse position written in mouseMoved().
	 */
	private Vector2 lastMousePosition = new Vector2();

	private boolean mouse0GotReleased;
	private boolean mouse1GotReleased;

	public GameInputProcessor(CubicWorldGame cubicWorld)
	{
		this.cubicWorld = cubicWorld;
	}

	/**
	 * <pre>
	 * Sets the current input processor's state if the player changed anything
	 * by pressing a key.
	 * The actual calculations for movement and so on will get done in update().
	 * 
	 * This function only updates the current state.
	 * </pre>
	 * 
	 */
	@Override
	public boolean keyDown(int keycode)
	{
		if (!this.processInput)
			return false;

		// Send the key release
		IKeyInputHandler keyInputAction = this.cubicWorld.inputManager.getInputActionForKey(keycode);
		if (keyInputAction != null)
		{
			keyInputAction.keyPressed(cubicWorld);
			return true;
		}

		return false;
	}

	@Override
	public boolean keyTyped(char character)
	{
		return false;
	}

	/**
	 * <pre>
	 * Sets the current input processor's state if the player changed anything
	 * by pressing a key.
	 * The actual calculations for movement and so on will get done in update().
	 * 
	 * This function only updates the current state.
	 * </pre>
	 */
	@Override
	public boolean keyUp(int keycode)
	{
		if (!this.processInput)
			return false;

		// Send the key release
		IKeyInputHandler keyInputAction = this.cubicWorld.inputManager.getInputActionForKey(keycode);
		if (keyInputAction != null)
		{
			keyInputAction.keyReleased(cubicWorld);

			return true;
		}

		return false;
	}

	/**
	 * This function currently only rotates the camera according to the player's
	 * mouse movement.
	 */
	@Override
	public boolean mouseMoved(int screenX, int screenY)
	{
		// If input should get processed & there is a mouse input handler registered in the InputManager.
		if (!this.processInput || this.cubicWorld.inputManager.getMouseInputHandler() == null)
		{
			// Save last mouse position
			this.lastMousePosition = new Vector2(screenX, screenY);
			return false;
		}

		// Process the input!
		Vector2 currentScreenPos = new Vector2(screenX, screenY);

		// Process mouse input
		this.cubicWorld.inputManager.getMouseInputHandler().mouseMoved(new Vector2(currentScreenPos).sub(this.lastMousePosition), currentScreenPos);

		// Save last mouse position
		this.lastMousePosition = currentScreenPos;

		return true;
	}

	@Override
	public boolean scrolled(int amount)
	{
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button)
	{
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer)
	{
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button)
	{
		return false;
	}

	/**
	 * Updates the mouse buttons and fires events if they got pressed or released.
	 */
	public void update()
	{
		this.processInput = !this.cubicWorld.guiManager.isOverlayActive();

		if (this.processInput && this.cubicWorld.inputManager.getMouseInputHandler() != null)
		{
			IMouseInputHandler mouseInputHandler = this.cubicWorld.inputManager.getMouseInputHandler();
			if (Gdx.input.isButtonPressed(0) && this.mouse0GotReleased)
			{
				mouseInputHandler.mouseButtonPressed(0);
				this.mouse0GotReleased = false;
			}
			else if (!Gdx.input.isButtonPressed(0))
			{
				this.mouse0GotReleased = true;
			}
			if (Gdx.input.isButtonPressed(1) && this.mouse1GotReleased)
			{
				mouseInputHandler.mouseButtonPressed(1);
				this.mouse1GotReleased = false;
			}
			else if (!Gdx.input.isButtonPressed(1))
			{
				this.mouse1GotReleased = true;
			}
		}
	}

}
