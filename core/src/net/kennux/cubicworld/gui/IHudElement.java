package net.kennux.cubicworld.gui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Hud elements are alway rendered by the gui manager.
 * Only if a overlay is active the hud wont get rendered anymore.
 * 
 * @author KennuX
 *
 */
public interface IHudElement
{
	/**
	 * Renders the gui element.
	 */
	public void render(SpriteBatch spriteBatch, BitmapFont font);

	/**
	 * Updates this gui element.
	 */
	public void update();
}
