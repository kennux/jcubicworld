package net.kennux.cubicworld.gui.elements;

import net.kennux.cubicworld.gui.skin.AGuiSkin;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class GuiImage extends AGuiElement
{

	/**
	 * The inventory slots used for rendering.
	 */
	private Texture imageTexture;

	/**
	 * Sets the button rectangle.
	 * 
	 * @param texture
	 *            the texture path in the assets folder for loading with new Texture(String).
	 */
	public GuiImage(Rectangle absoluteRect, Rectangle relativeRect, String texture)
	{
		super(absoluteRect, relativeRect);
		this.imageTexture = new Texture(texture);
	}

	@Override
	public void render(SpriteBatch spriteBatch, BitmapFont font, boolean hasFocus, AGuiSkin skin, ShapeRenderer shapeRenderer)
	{
		spriteBatch.draw(this.imageTexture, this.relativeRectangle.x, this.relativeRectangle.y, this.relativeRectangle.width, this.relativeRectangle.height);
	}

}
