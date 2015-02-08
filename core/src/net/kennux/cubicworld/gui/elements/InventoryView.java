package net.kennux.cubicworld.gui.elements;

import net.kennux.cubicworld.gui.GuiHelper;
import net.kennux.cubicworld.gui.overlay.Overlay;
import net.kennux.cubicworld.gui.skin.AGuiSkin;
import net.kennux.cubicworld.math.Mathf;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * The inventory view is a collection of multiple inventory slots.
 * You can configure how much slots, how much on x and y-axis, how much spacing between them, etc.
 * 
 * @author kennux
 *
 */
public class InventoryView extends AGuiElement
{
	/**
	 * Sets the button rectangle.
	 * 
	 * @param startSlot
	 *            The inventory start slot where this view should start.
	 * @param endSlot
	 *            The inventory end slot where this view should end.
	 * @param slotsPerRow
	 *            The count of inventory slots on x-axis (so, per row).
	 * @param inventory
	 *            The inventory which will get used for displaying.
	 * @param cellspacing
	 *            The cellspacing in percentage.
	 */
	public InventoryView(Overlay parent, Rectangle absoluteRect, Rectangle relativeRect, String id, int startSlot, int endSlot, int slotsPerRow, String inventory, float cellspacing)
	{
		super(absoluteRect, relativeRect);

		// Calculate view dimensions
		int slotCount = endSlot - startSlot;
		int slotRows = Mathf.ceilToInt(slotCount / slotsPerRow);
		Vector2 absoluteCellspacing = GuiHelper.getAbsoluteFromPercentagePosition(new Vector2(cellspacing, cellspacing));

		// Generate item slot size
		Vector2 itemSlotSize = new Vector2((relativeRect.width / slotsPerRow) - absoluteCellspacing.x, (relativeRect.height / slotRows) - absoluteCellspacing.y);

		int i = startSlot;
		for (float x = 0; x < slotsPerRow && i <= endSlot; x++)
		{
			for (float y = 0; y < slotRows && i <= endSlot; y++)
			{
				// Generate inventory slot positions
				Rectangle slotRelativeRect = new Rectangle(relativeRect);
				Rectangle slotAbsoluteRect = new Rectangle(absoluteRect);
				float xOffset = (itemSlotSize.x * x) + (x * absoluteCellspacing.x);
				float yOffset = (itemSlotSize.y * y) + (y * absoluteCellspacing.y);

				slotRelativeRect.x += xOffset;
				slotRelativeRect.y += yOffset;
				slotRelativeRect.width = itemSlotSize.x;
				slotRelativeRect.height = itemSlotSize.y;

				slotAbsoluteRect.x += xOffset;
				slotAbsoluteRect.y += yOffset;
				slotAbsoluteRect.width = itemSlotSize.x;
				slotAbsoluteRect.height = itemSlotSize.y;

				parent.addElement(id + "_inventoryslot_" + x + "_" + y, new InventorySlot(parent, slotAbsoluteRect, slotRelativeRect, i, inventory));
				i++;
			}
		}

		// Set the inventory view's width and height to 0 to prevent the click events will not get forwarded to the inventory slots.
		this.absoluteRectangle.width = 0;
		this.absoluteRectangle.height = 0;
		this.relativeRectangle.width = 0;
		this.relativeRectangle.height = 0;
	}

	@Override
	public void render(SpriteBatch spriteBatch, BitmapFont font, boolean hasFocus, AGuiSkin skin, ShapeRenderer shapeRenderer)
	{

	}

}
