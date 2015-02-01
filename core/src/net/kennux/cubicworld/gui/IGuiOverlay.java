package net.kennux.cubicworld.gui;

import java.util.List;

import net.kennux.cubicworld.gui.overlay.OverlayData;
import net.kennux.cubicworld.gui.skin.AGuiSkin;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public interface IGuiOverlay
{
	/**
	 * Returns the current focus element set by setFocusElement.
	 * 
	 * @return
	 */
	public IGuiElement getFocusElement();

	/**
	 * Returns the gui element at the given position.
	 * Perform an intersection check for all gui bounding boxes in here.
	 * 
	 * @param screenPosition
	 * @return
	 */
	public List<IGuiElement> getGuiElementsAtPosition(Vector2 screenPosition);

	/**
	 * @param overlayData
	 *            the overlayData to set
	 */
	public OverlayData getOverlayData();

	/**
	 * Returns true if the current focus element is not null.
	 * 
	 * @return
	 */
	public boolean hasFocusElement();

	/**
	 * Renders this gui element.
	 * Will get called every frame.
	 * 
	 * @param spriteBatch
	 */
	public void render(SpriteBatch spriteBatch, BitmapFont font, AGuiSkin skin);

	/**
	 * Sets the current focus element.
	 * 
	 * @param element
	 */
	public void setFocusElement(IGuiElement element);

	/**
	 * Returns a reference to the overlay data.
	 * If you modify the overlay data use the setOverlayData() function.
	 */
	public void setOverlayData(OverlayData overlayData);

	/**
	 * Updates this gui element.
	 * Will get called every frame.
	 */
	public void update();
}
