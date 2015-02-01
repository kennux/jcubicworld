package net.kennux.cubicworld.gui.hud;

import net.kennux.cubicworld.gui.IHudElement;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Draws a crosshair in the center of the screen.
 * 
 * @author KennuX
 *
 */
public class Crosshair implements IHudElement
{
	private Texture crosshairTexture;

	public Crosshair()
	{
		this.crosshairTexture = new Texture("textures\\crosshair.png");
	}

	@Override
	public void render(SpriteBatch spriteBatch, BitmapFont font)
	{
		int positionX = (Gdx.graphics.getWidth() / 2) - (this.crosshairTexture.getWidth() / 2);
		int positionY = (Gdx.graphics.getHeight() / 2) - (this.crosshairTexture.getHeight() / 2);

		spriteBatch.draw(this.crosshairTexture, positionX, positionY);
	}

	@Override
	public void update()
	{
		// TODO Auto-generated method stub

	}

}
