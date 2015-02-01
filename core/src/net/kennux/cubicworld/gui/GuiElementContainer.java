package net.kennux.cubicworld.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.kennux.cubicworld.gui.skin.AGuiSkin;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * <pre>
 * This class can hold multiple gui elements.
 * It can handle input forwarding as well as rendering for all elements.
 * 
 * This class also handles focus setting for elements.
 * If an element is clicked, the click event is immediately forwarded and focus gets set.
 * If a user types a key the keytyped event will get fired on the current focus element.
 * </pre>
 * 
 * @author KennuX
 *
 */
public class GuiElementContainer
{
	/**
	 * The position of the overlay.
	 */
	protected Vector2 position;

	/**
	 * The size of the overlay.
	 */
	protected Vector2 size;

	/**
	 * Contains all gui elements added to this overlay.
	 * The key of this hashmap is the element's id used for setting handler.
	 */
	protected final HashMap<String, IGuiElement> guiElements;

	/**
	 * The current focus gui element.
	 * May be null!
	 */
	private IGuiElement focusElement;

	/**
	 * You <strong>HAVE TO</strong> overload this constructor!
	 */
	public GuiElementContainer()
	{
		// Init lists
		this.guiElements = new HashMap<String, IGuiElement>();
	}

	/**
	 * Adds an gui element to this list elements to draw.
	 * If the element is already in the gui elements list, nothing will get done in here.
	 * 
	 * @param element
	 */
	public final void addElement(String id, IGuiElement element)
	{
		if (!this.guiElements.containsValue(element))
			this.guiElements.put(id, element);
	}

	/**
	 * <pre>
	 * Returns the element with the given id.
	 * Will return null if the element was not found.
	 * 
	 * Basically this function only forwards the get() function of the gui elements hashmap.
	 * </pre>
	 * 
	 * @param id
	 * @return
	 */
	public IGuiElement getElementById(String id)
	{
		return this.guiElements.get(id);
	}

	/**
	 * Returns the current focus element set by setFocusElement.
	 * 
	 * @return
	 */
	public IGuiElement getFocusElement()
	{
		return this.focusElement;
	}

	/**
	 * Returns the gui element at the given position.
	 * Perform an intersection check for all gui bounding boxes in here.
	 * 
	 * @param screenPosition
	 * @return
	 */
	public List<IGuiElement> getGuiElementsAtPosition(Vector2 screenPosition)
	{
		List<IGuiElement> returnList = new ArrayList<IGuiElement>();

		for (Entry<String, IGuiElement> guiElement : this.guiElements.entrySet())
		{
			IGuiInputHandler handler = guiElement.getValue().getInputHandler();

			if (handler != null && handler.getBoundingRectangle().contains(screenPosition))
				returnList.add(guiElement.getValue());
		}

		return returnList;
	}

	/**
	 * @return the position
	 */
	public Vector2 getPosition()
	{
		return position;
	}

	/**
	 * @return the size
	 */
	public Vector2 getSize()
	{
		return size;
	}

	public boolean hasFocusElement()
	{
		return this.focusElement != null;
	}

	/**
	 * This function will remove a gui element in the gui elements list.
	 * If the element was not found, nothing gets done.
	 * 
	 * IMPORTANT: This function only forwards the remove() function of the hashmap.
	 * 
	 * @param id
	 */
	public final void removeElement(String id)
	{
		if (!this.guiElements.containsKey(id))
			this.guiElements.remove(id);
	}

	/**
	 * Renders all gui elements in this container.
	 * 
	 * @param spriteBatch
	 */
	public void render(SpriteBatch spriteBatch, BitmapFont font, AGuiSkin skin)
	{
		spriteBatch.setTransformMatrix(new Matrix4().idt().translate(new Vector3(this.getPosition().x, this.getPosition().y, 0))); // Offset

		for (Entry<String, IGuiElement> guiElement : this.guiElements.entrySet())
			guiElement.getValue().render(spriteBatch, font, this.focusElement == guiElement.getValue(), skin);

		for (Entry<String, IGuiElement> guiElement : this.guiElements.entrySet())
			guiElement.getValue().renderLast(spriteBatch, font, this.focusElement == guiElement.getValue(), skin);

		spriteBatch.setTransformMatrix(new Matrix4().idt());
	}

	/**
	 * Sets the current focus element.
	 * 
	 * @param element
	 */
	public void setFocusElement(IGuiElement element)
	{
		this.focusElement = element;
	}

	/**
	 * @param position
	 *            the position to set
	 */
	public void setPosition(Vector2 position)
	{
		this.position = position;
	}

	/**
	 * @param size
	 *            the size to set
	 */
	public void setSize(Vector2 size)
	{
		this.size = size;
	}
}
