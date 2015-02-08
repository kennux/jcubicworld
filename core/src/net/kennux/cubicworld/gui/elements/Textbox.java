package net.kennux.cubicworld.gui.elements;

import net.kennux.cubicworld.gui.skin.AGuiSkin;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Textbox element implementation.
 * 
 * @author kennux
 *
 */
public class Textbox extends AGuiElement
{
	private String contents;

	private boolean hasFocus;

	private ITextboxEnterHandler enterHandler;

	/**
	 * Sets the button rectangle.
	 * 
	 * @param rect
	 */
	public Textbox(Rectangle absoluteRect, Rectangle relativeRect)
	{
		super(absoluteRect, relativeRect);

		this.contents = "";
	}

	public String getContents()
	{
		return this.contents.trim();
	}

	@Override
	protected void handleKeyTyped(char typedChar)
	{
		if (this.hasFocus)
			if (this.enterHandler != null && typedChar == (char) 13)
				this.enterHandler.handleEnter(this);
			else if (typedChar == (char) 8)
				this.contents = this.removeLastChar(this.contents);
			else
				this.contents += typedChar;
	}

	/**
	 * Removes the last character from a string.
	 * 
	 * @param str
	 * @return
	 */
	private final String removeLastChar(String str)
	{
		if (str.length() > 0)
		{
			str = str.substring(0, str.length() - 1);
		}
		return str;
	}

	@Override
	public void render(SpriteBatch spriteBatch, BitmapFont font, boolean hasFocus, AGuiSkin skin)
	{
		// Update
		this.hasFocus = hasFocus;

		// Draw textbox background
		Texture texture = skin.getTexture("Textbox");
		spriteBatch.draw(texture, this.relativeRectangle.x, this.relativeRectangle.y, this.relativeRectangle.width, this.relativeRectangle.height);

		// Draw text
		// Set scale and color
		font.setColor(skin.getFontColor());

		// Calculate caption position
		TextBounds bounds = font.getBounds(this.contents);
		Vector2 textPosition = new Vector2(this.relativeRectangle.x, this.relativeRectangle.y);
		textPosition.x += 15.0f;
		textPosition.y += this.relativeRectangle.height - (bounds.height / 2.0f);

		// Now everything is ready, draw the caption!
		font.draw(spriteBatch, this.contents, textPosition.x, textPosition.y);
		font.setScale(1, 1);
	}

	public void setContents(String contents)
	{
		this.contents = contents;
	}

	public void setEnterHandler(ITextboxEnterHandler enterHandler)
	{
		this.enterHandler = enterHandler;
	}

}
