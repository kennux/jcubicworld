package net.kennux.cubicworld.gui;

import java.util.ArrayList;
import java.util.HashMap;

import net.kennux.cubicworld.gui.skin.AGuiSkin;
import net.kennux.cubicworld.input.GuiManagerInputProcessor;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * <pre>
 * Manages the gui rendering, updating and input processing.
 * Register gui windows to this object in your bootstrap.
 * 
 * This manager contains hud elements and gui overlays.
 * The difference between them is, hud elements get rendered if there is no
 * overlay active.
 * There can only be one overlay active, if one is active no hud will get drawn,
 * the screen will get obscured and the overlay gets drawn.
 * </pre>
 * 
 * @author KennuX
 *
 */
public class GuiManager
{
	/**
	 * Holds class informations for all overlays.
	 */
	private HashMap<Integer, IGuiOverlay> overlays;

	/**
	 * Holds class informations for all hud elements.
	 */
	private ArrayList<IHudElement> hudElements;

	/**
	 * The spritebatch used to draw the gui.
	 */
	private SpriteBatch guiBatch;
	
	/**
	 * The shape render used to render gui elements
	 */
	private ShapeRenderer shapeRenderer;

	/**
	 * Holds the id of the currently active overlay.
	 * -1 if there is no overlay active.
	 */
	private int activeOverlay = -1;

	/**
	 * The font used for rendering.
	 */
	private BitmapFont font;

	/**
	 * Gets incremented for every overlay registered to this gui manager.
	 * Gets used for "generating" registered overlay identifiers.
	 */
	private int overlaysRegistered = 0;

	/**
	 * The skin used to draw the gui.
	 */
	private AGuiSkin skin;

	/**
	 * The local input processor used for event forwarding.
	 */
	private GuiManagerInputProcessor inputProcessor;

	/**
	 * Constructs all needed lists, maps and rendering stuff.
	 */
	public GuiManager(AGuiSkin skin)
	{
		this.overlays = new HashMap<Integer, IGuiOverlay>();
		this.hudElements = new ArrayList<IHudElement>();
		this.font = new BitmapFont();
		this.guiBatch = new SpriteBatch();
		this.shapeRenderer = new ShapeRenderer();
		this.inputProcessor = new GuiManagerInputProcessor(this);
		this.skin = skin;
	}

	/**
	 * Closes the current overlay.
	 * Just sets the id of the active overlay to -1 (which means none).
	 */
	public void closeOverlay()
	{
		this.activeOverlay = -1;
	}

	/**
	 * Returns the currently active overlay.
	 * If there is no overlay active this function returns null.
	 * 
	 * @return
	 */
	public IGuiOverlay getActiveOverlay()
	{
		// overlay available?
		if (!this.isOverlayActive())
		{
			return null;
		}
		else
		{
			// Update overlay
			IGuiOverlay overlay = this.overlays.get(new Integer(this.activeOverlay));
			return overlay;
		}
	}

	/**
	 * Returns the input processor of this gui manager instance.
	 * 
	 * @return
	 */
	public InputProcessor getInputProcessor()
	{
		return this.inputProcessor;
	}

	/**
	 * Returns the gui overlay instance for the gui with the given id.
	 * 
	 * @param id
	 * @return
	 */
	public IGuiOverlay getOverlayById(int id)
	{
		return this.overlays.get(new Integer(id));
	}

	/**
	 * Returns if there is an overlay active in this gui manager.
	 * 
	 * @return
	 */
	public boolean isOverlayActive()
	{
		return this.activeOverlay != -1 && this.overlays.containsKey(new Integer(this.activeOverlay));
	}

	/**
	 * Opens the overlay with the given id.
	 * Will deactivate the currently active overlay if there is one active.
	 * 
	 * @param id
	 */
	public void openOverlay(int id)
	{
		this.activeOverlay = id;
	}

	/**
	 * Registers the given hud element to this gui manager instance.
	 * A hud element registered to the manager will get drawn every frame if
	 * there is no overlay active.
	 * 
	 * @param hudElement
	 */
	public void registerHudElement(IHudElement hudElement)
	{
		this.hudElements.add(hudElement);
	}

	/**
	 * <pre>
	 * Registers the given gui overlay instance to the gui manager with the
	 * given id.
	 * This only registers an overlay with the given id to the gui manager.
	 * It does not display anything!
	 * 
	 * If the overlay is already in the overlays list, this function will do nothing.
	 * </pre>
	 * 
	 * @param id
	 * @param overlayisOverlayActive
	 *            The overlay's id used for activating it.
	 */
	public int registerOverlay(IGuiOverlay overlay)
	{
		if (!this.overlays.containsValue(overlay))
			this.overlays.put(this.overlaysRegistered, overlay);
		this.overlaysRegistered++;

		return this.overlaysRegistered - 1;
	}

	/**
	 * <pre>
	 * Renders all active gui elements.
	 * Call this atleast in your application and disable depth testing before
	 * you call it.
	 * Calls all hud element render method if there is no overlay active.
	 * 
	 * If there is an overlay active, nothing will get done with the hud
	 * elements but the active overlay gets rendered.
	 * </pre>
	 */
	public void render()
	{
		this.guiBatch.begin();

		// overlay active and available?
		if (!this.isOverlayActive())
		{
			for (IHudElement hudElement : this.hudElements)
			{
				hudElement.render(this.guiBatch, this.font);
			}
		}
		else
		{
			// Update overlay
			IGuiOverlay overlay = this.overlays.get(new Integer(this.activeOverlay));
			overlay.render(this.guiBatch, this.font, this.skin, this.shapeRenderer);
		}

		this.guiBatch.end();
	}

	/**
	 * Sets the skin used by this gui manager.
	 * 
	 * @param skin
	 */
	public void setSkin(AGuiSkin skin)
	{
		this.skin = skin;
	}

	/**
	 * Processing input data and updates all active gui elements.
	 * Calls all hud element updates if there is no overlay active.
	 * 
	 * If there is an overlay active, nothing will get done with the hud
	 * elements but the active overlay gets updated.
	 */
	public void update()
	{
		// Update input processor
		this.inputProcessor.update();

		// overlay active and available?
		if (!this.isOverlayActive())
		{
			// Only hud elements
			for (IHudElement hudElement : this.hudElements)
			{
				hudElement.update();
			}
			return;
		}

		// Update overlay
		IGuiOverlay overlay = this.overlays.get(new Integer(this.activeOverlay));
		overlay.update();
	}
}
