package net.kennux.cubicworld.gui.elements;

import net.kennux.cubicworld.gui.IGuiElement;
import net.kennux.cubicworld.gui.IGuiInputHandler;
import net.kennux.cubicworld.gui.events.IClickHandler;
import net.kennux.cubicworld.gui.events.IMouseDownHandler;
import net.kennux.cubicworld.gui.events.IMouseUpHandler;
import net.kennux.cubicworld.gui.skin.AGuiSkin;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * <pre>
 * Abstract gui element implementation.
 * Mainly handles setting of the input handler.
 * 
 * <strong>Call initialize() in your constructor!</strong>
 * </pre>
 * 
 * @author kennux
 *
 */
public abstract class AGuiElement implements IGuiElement
{
	/**
	 * The click handler for this element.
	 */
	protected IClickHandler clickHandler;

	/**
	 * The mouse down event handler for this element.
	 */
	protected IMouseDownHandler mouseDownHandler;

	/**
	 * The mouse up event handler for this element.
	 */
	protected IMouseUpHandler mouseUpHandler;

	/**
	 * The rectangle used for rendering.
	 * This variable gets written in the
	 */
	protected Rectangle relativeRectangle;

	/**
	 * The rectangle used for input handling.
	 * Write this variable in your own implementation in the constructor!
	 */
	protected Rectangle absoluteRectangle;

	/**
	 * The local inputhandler instance.
	 */
	private IGuiInputHandler inputHandler;

	/**
	 * Overload this constructor in your own implementation.
	 * It initializes all needed local variables.
	 * 
	 * @param absoluteRect
	 * @param relativeRect
	 */
	public AGuiElement(Rectangle absoluteRect, Rectangle relativeRect)
	{
		this.absoluteRectangle = absoluteRect;
		this.relativeRectangle = relativeRect;

		// Init input handling
		final AGuiElement element = this;

		this.setInputHandler(new IGuiInputHandler()
		{

			@Override
			public Rectangle getBoundingRectangle()
			{
				return element.absoluteRectangle;
			}

			@Override
			public void handleClick(Vector2 mousePosition, int mouseButton)
			{
				if (element.clickHandler != null)
				{
					element.clickHandler.handleClick(mousePosition);
				}
			}

			@Override
			public void handleKeyType(char typedChar)
			{
				element.handleKeyTyped(typedChar);
			}

			@Override
			public void handleMouseDown(Vector2 mousePosition, int mouseButton)
			{
				if (element.mouseDownHandler != null)
					element.mouseDownHandler.handleMouseDown(mousePosition, mouseButton);
			}

			@Override
			public void handleMouseUp(Vector2 mousePosition, int mouseButton)
			{
				if (element.mouseUpHandler != null)
					element.mouseUpHandler.handleMouseUp(mousePosition, mouseButton);
			}
		});
	}

	/**
	 * Returns the input handler of this button.
	 */
	@Override
	public IGuiInputHandler getInputHandler()
	{
		return this.inputHandler;
	}

	/**
	 * Gets called by the input handler if handleKeyType() was called.
	 * Override this in your own implementation if you need this.
	 */
	protected void handleKeyTyped(char typedChar)
	{

	}

	/**
	 * Dummy method, this does nothing.
	 * Override this if you need this method.
	 * 
	 * @see IGuiElement#renderLast(SpriteBatch, BitmapFont, boolean, AGuiSkin)
	 */
	public void renderLast(SpriteBatch spriteBatch, BitmapFont font, boolean hasFocus, AGuiSkin skin, ShapeRenderer shapeRenderer)
	{

	}

	/**
	 * @param clickHandler
	 *            the clickHandler to set
	 */
	public void setClickHandler(IClickHandler clickHandler)
	{
		this.clickHandler = clickHandler;
	}

	public void setInputHandler(IGuiInputHandler inputHandler)
	{
		this.inputHandler = inputHandler;
	}

	/**
	 * @param mouseDownHandler
	 *            the mouse down handler to use when events get fired.
	 */
	public void setMouseDownHandler(IMouseDownHandler mouseDownHandler)
	{
		this.mouseDownHandler = mouseDownHandler;
	}

	/**
	 * @param mouseDownHandler
	 *            the mouse down handler to use when events get fired.
	 */
	public void setMouseUpHandler(IMouseUpHandler mouseUpHandler)
	{
		this.mouseUpHandler = mouseUpHandler;
	}
}
