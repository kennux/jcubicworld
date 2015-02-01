package net.kennux.cubicworld.input;

import java.util.ArrayList;
import java.util.List;

import net.kennux.cubicworld.CubicWorld;
import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.gui.GuiManager;
import net.kennux.cubicworld.gui.IGuiElement;
import net.kennux.cubicworld.gui.IGuiInputHandler;
import net.kennux.cubicworld.gui.IGuiOverlay;
import net.kennux.cubicworld.plugins.baseplugin.BasePlugin;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;

/**
 * Gui manager input processor.
 * Handles input processing for currently displayed gui elements.
 * 
 * @author kennux
 *
 */
public class GuiManagerInputProcessor implements InputProcessor
{
	/**
	 * The master of this input processor.
	 */
	private GuiManager master;

	/**
	 * Gets set to true when button 0 gets released.
	 */
	private boolean mouse0GotReleased = true;

	/**
	 * Contains all elements which need to recieve an mouseup event if one happens.
	 * They are in this list, because if the player fires a mousedown on an element and moves the mouse out of it's scope it is not possible to detect where to send the mouse up event.
	 */
	private ArrayList<IGuiInputHandler> mouseDownFiredElements;

	/**
	 * Sets the local master manager.
	 * 
	 * @param master
	 */
	public GuiManagerInputProcessor(GuiManager master)
	{
		this.master = master;
		this.mouseDownFiredElements = new ArrayList<IGuiInputHandler>();
	}

	/**
	 * Unused
	 */
	@Override
	public boolean keyDown(int keycode)
	{
		CubicWorldGame cubicWorld = CubicWorld.getClient();

		if (cubicWorld.guiManager.isOverlayActive())
		{
			// If there is gui active, only process ESC
			if (keycode == Input.Keys.ESCAPE)
			{
				if (cubicWorld.guiManager.isOverlayActive())
					cubicWorld.guiManager.closeOverlay();
				else
					cubicWorld.guiManager.openOverlay(BasePlugin.mainMenuOverlayId);

				return false;
			}
			else
			{
				return false;
			}
		}
		return false;
	}

	/**
	 * Forwards keyTyped events to the currently focused element.
	 */
	@Override
	public boolean keyTyped(char character)
	{
		// Get active overlay
		IGuiOverlay activeOverlay = this.master.getActiveOverlay();

		// Is there an active overlay?
		if (activeOverlay != null)
		{
			// Does it have a focus element?
			IGuiElement focusElement = activeOverlay.getFocusElement();

			if (focusElement != null)
			{
				IGuiInputHandler handler = focusElement.getInputHandler();
				if (handler != null)
					handler.handleKeyType(character);

				return true;
			}
		}

		return false;
	}

	/**
	 * Unused
	 */
	@Override
	public boolean keyUp(int keycode)
	{
		return false;
	}

	/**
	 * Unused
	 */
	@Override
	public boolean mouseMoved(int screenX, int screenY)
	{
		return false;
	}

	/**
	 * Forwards the scrolled() event
	 */
	@Override
	public boolean scrolled(int amount)
	{
		return false;
	}

	/**
	 * Unused
	 */
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button)
	{
		return false;
	}

	/**
	 * Unused
	 */
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer)
	{
		return false;
	}

	/**
	 * Unused
	 */
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button)
	{
		return false;
	}

	/**
	 * Call this function in your update routine.
	 * It handles mouse clicks.
	 */
	public void update()
	{
		// Get current screen mouse position
		Vector2 mousePosition = new Vector2(Gdx.input.getX(), Gdx.input.getY());

		// Mirror mouse position y
		// Rendering origin is bottom-left
		// Mouse origin is top-left
		mousePosition.y = Gdx.graphics.getHeight() - mousePosition.y;
		// Get active overlay
		IGuiOverlay activeOverlay = this.master.getActiveOverlay();

		// Is there an active overlay?
		if (activeOverlay != null)
		{
			// Get current element(s)
			List<IGuiElement> elements = activeOverlay.getGuiElementsAtPosition(mousePosition);
			boolean button0State = Gdx.input.isButtonPressed(0);

			// Forward mouse up
			if (!this.mouse0GotReleased && !button0State)
			{
				// forward mouse down
				// for (IGuiElement element : elements)
				for (IGuiInputHandler handler : this.mouseDownFiredElements)
				{
					// Forward if the element has an input handler.
					// IGuiInputHandler handler = element.getInputHandler();
					if (handler != null)
						handler.handleMouseUp(mousePosition, 0);
				}
				this.mouseDownFiredElements.clear();
			}

			if (elements.size() > 0)
			{
				if (this.mouse0GotReleased && button0State)
				{
					for (IGuiElement element : elements)
					{
						// Forward if the element has an input handler.
						IGuiInputHandler handler = element.getInputHandler();
						if (handler != null)
						{
							handler.handleMouseDown(mousePosition, 0);
							this.mouseDownFiredElements.add(handler);
						}
					}

					this.mouse0GotReleased = false;
				}

				// Click forwarding?
				if (!this.mouse0GotReleased && !button0State)
				{
					// Set focus to the first one
					activeOverlay.setFocusElement(elements.get(0));

					// Forward click
					for (IGuiElement element : elements)
					{
						// Forward if the element has an input handler.
						IGuiInputHandler handler = element.getInputHandler();
						if (handler != null)
							handler.handleClick(mousePosition, 0);
					}

					this.mouse0GotReleased = true;
				}
			}
		}
	}
}
