package net.kennux.cubicworld.gui.elements;

import net.kennux.cubicworld.gui.events.IMouseDownHandler;
import net.kennux.cubicworld.gui.events.IMouseUpHandler;
import net.kennux.cubicworld.gui.skin.AGuiSkin;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Button element implementation.
 * 
 * @author kennux
 *
 */
public class Button extends AGuiElement
{
	/**
	 * The caption of the button.
	 */
	private String caption;

	/**
	 * Contains the font scale of this button.
	 * Font scaling will get used when rendering the caption.
	 */
	private float fontScale = 1.0f;

	/**
	 * If this is set to true, the "Button_Clicked" texture gets used for rendering.
	 */
	private boolean isClicked = false;

	/**
	 * Sets the button rectangle.
	 * 
	 * @param rect
	 */
	public Button(Rectangle absoluteRect, Rectangle relativeRect, String caption)
	{
		super(absoluteRect, relativeRect);
		this.caption = new String(caption);

		final Button element = this;

		// Set mouse up / down events
		this.setMouseDownHandler(new IMouseDownHandler()
		{

			@Override
			public void handleMouseDown(Vector2 mousePosition, int mouseButton)
			{
				element.isClicked = true;
			}

		});

		this.setMouseUpHandler(new IMouseUpHandler()
		{

			@Override
			public void handleMouseUp(Vector2 mousePosition, int mouseButton)
			{
				element.isClicked = false;
			}

		});
	}

	/**
	 * @return the fontScale
	 */
	public float getFontScale()
	{
		return fontScale;
	}

	@Override
	public void render(SpriteBatch spriteBatch, BitmapFont font, boolean hasFocus, AGuiSkin skin, ShapeRenderer shapeRenderer)
	{
		// Draw button itself
		Texture buttonTexture = null;
		if (this.isClicked)
			buttonTexture = skin.getTexture("Button_Clicked");
		else
			buttonTexture = skin.getTexture("Button");

		spriteBatch.draw(buttonTexture, this.relativeRectangle.x, this.relativeRectangle.y, this.relativeRectangle.width, this.relativeRectangle.height);

		// Draw caption
		// Set scale and color
		font.setScale(this.fontScale, this.fontScale);
		font.setColor(skin.getFontColor());

		// Calculate caption position
		TextBounds bounds = font.getBounds(this.caption);
		Vector2 captionPosition = new Vector2(this.relativeRectangle.x, this.relativeRectangle.y);
		captionPosition.x += (this.relativeRectangle.width / 2) - (bounds.width / 2);
		captionPosition.y += this.relativeRectangle.height - bounds.height;

		// Now everything is ready, draw the caption!
		font.draw(spriteBatch, this.caption, captionPosition.x, captionPosition.y);
		font.setScale(1, 1);
	}

	/**
	 * @param fontScale
	 *            the fontScale to set
	 */
	public void setFontScale(float fontScale)
	{
		this.fontScale = fontScale;
	}
}
