package net.kennux.cubicworld.gui.elements;

import java.util.List;

import net.kennux.cubicworld.CubicWorld;
import net.kennux.cubicworld.gui.IGuiElement;
import net.kennux.cubicworld.gui.events.IMouseDownHandler;
import net.kennux.cubicworld.gui.events.IMouseUpHandler;
import net.kennux.cubicworld.gui.overlay.Overlay;
import net.kennux.cubicworld.gui.skin.AGuiSkin;
import net.kennux.cubicworld.inventory.IInventory;
import net.kennux.cubicworld.item.ItemStack;
import net.kennux.cubicworld.networking.packet.inventory.ClientItemMove;
import net.kennux.cubicworld.networking.packet.inventory.ClientItemTransaction;
import net.kennux.cubicworld.networking.packet.inventory.TransactionType;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Inventory slot gui element implementation.
 * 
 * @author kennux
 *
 */
public class InventorySlot extends AGuiElement
{
	/**
	 * null means no dragging in process.
	 */
	private static InventorySlot draggedSlot = null;

	/**
	 * The parent overlay.
	 */
	private Overlay parent;

	/**
	 * The slot id in the inventory set in the overlay data.
	 */
	private int slotId;

	/**
	 * The inventory to use for this slot.
	 */
	private String inventory;

	/**
	 * Sets the button rectangle.
	 * 
	 * @param rect
	 */
	public InventorySlot(Overlay parent, Rectangle absoluteRect, Rectangle relativeRect, int slotId, String inventory)
	{
		super(absoluteRect, relativeRect);

		this.parent = parent;
		this.slotId = slotId;
		this.inventory = inventory;

		// Set player inventory drag and drop handler
		final InventorySlot slot = this;

		this.setMouseDownHandler(new IMouseDownHandler()
		{
			@Override
			public void handleMouseDown(Vector2 mousePosition, int mouseButton)
			{
				draggedSlot = slot;
			}

		});

		this.setMouseUpHandler(new IMouseUpHandler()
		{
			@Override
			public void handleMouseUp(Vector2 mousePosition, int mouseButton)
			{
				if (draggedSlot != null)
				{
					// Get target slot
					List<IGuiElement> guiElementsList = CubicWorld.getClient().guiManager.getActiveOverlay().getGuiElementsAtPosition(new Vector2(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY()));

					for (IGuiElement guiElement : guiElementsList)
					{
						// Check if this gui element is an inventory slot
						if (guiElement instanceof InventorySlot)
						{
							InventorySlot targetInventorySlot = (InventorySlot) guiElement;

							// Get inventory
							IInventory sourceInventory = (IInventory) draggedSlot.parent.getOverlayData().get(draggedSlot.getInventoryName());
							
							// Get item stacks
							ItemStack sourceStack = sourceInventory.getItemStackInSlot(draggedSlot.getSlotId());

							// Get item counts
							int sourceItemCount = 0;

							if (sourceStack != null)
								sourceItemCount = sourceStack.getItemCount();

							// Player inventory transactions
							if (draggedSlot.getInventoryName().equals("playerInventory"))
							{
								if (targetInventorySlot.getInventoryName().equals("playerInventory"))
								{
									// Move!
									CubicWorld.getClient().playerController.moveItem(slot.slotId, targetInventorySlot.getSlotId());
								}
								// Block inventory
								else if (targetInventorySlot.getInventoryName().equals("inventory"))
								{
									// Get block position
									Vector3 blockPosition = (Vector3) targetInventorySlot.parent.getOverlayData().get("voxelPos");

									// Send transaction packet
									CubicWorld.getClient().client.sendPacket(ClientItemTransaction.create(draggedSlot.getSlotId(), TransactionType.PLAYER_TO_INVENTORY, (int) blockPosition.x, (int) blockPosition.y, (int) blockPosition.z, targetInventorySlot.getSlotId(), sourceItemCount));
								}
							}
							else if (draggedSlot.getInventoryName().equals("inventory"))
							{
								// Get block position
								Vector3 blockPosition = (Vector3) draggedSlot.parent.getOverlayData().get("voxelPos");

								if (targetInventorySlot.getInventoryName().equals("playerInventory"))
								{
									// Send transaction packet
									CubicWorld.getClient().client.sendPacket(ClientItemTransaction.create(targetInventorySlot.getSlotId(), TransactionType.INVENTORY_TO_PLAYER, (int) blockPosition.x, (int) blockPosition.y, (int) blockPosition.z, draggedSlot.getSlotId(), sourceItemCount));
								}
								// Block inventory
								else if (targetInventorySlot.getInventoryName().equals("inventory"))
								{
									CubicWorld.getClient().client.sendPacket(ClientItemMove.createVoxelInventoryMove(draggedSlot.getSlotId(), targetInventorySlot.getSlotId(), blockPosition));
								}
							}
						}
					}

					draggedSlot = null;
				}
			}

		});
	}

	public String getInventoryName()
	{
		return this.inventory;
	}

	public int getSlotId()
	{
		return this.slotId;
	}

	@Override
	public void render(SpriteBatch spriteBatch, BitmapFont font, boolean hasFocus, AGuiSkin skin, ShapeRenderer shapeRenderer)
	{
		// Draw the slot itself
		Texture inventorySlotTexture = null;
		inventorySlotTexture = skin.getTexture("InventorySlot");
		spriteBatch.draw(inventorySlotTexture, this.relativeRectangle.x, this.relativeRectangle.y, this.relativeRectangle.width, this.relativeRectangle.height);

		// Get inventory instance
		IInventory inventoryInstance = (IInventory) this.parent.getOverlayData().get(this.inventory);

		// Get item texture
		ItemStack slotItemStack = inventoryInstance.getItemStackInSlot(this.slotId);

		// Check if there actually is an item stack in this slot.
		if (draggedSlot != this && slotItemStack != null)
		{
			this.renderItemStack(spriteBatch, font, skin, slotItemStack, this.relativeRectangle);
		}
	}

	private void renderItemStack(SpriteBatch spriteBatch, BitmapFont font, AGuiSkin skin, ItemStack slotItemStack, Rectangle relativeRectangle)
	{
		font.setColor(skin.getFontColor());

		// Render the item main texture
		spriteBatch.draw(slotItemStack.getType().getItemTextureRegion(), relativeRectangle.x, relativeRectangle.y, relativeRectangle.width, relativeRectangle.height);

		// Calculate font bounding box for item count
		TextBounds itemCountBoundingBox = font.getBounds(slotItemStack.getItemCount() + "");

		Vector2 itemCountLabelPos = new Vector2(relativeRectangle.x + (relativeRectangle.width - itemCountBoundingBox.width - 5.0f), relativeRectangle.y + itemCountBoundingBox.height + 5.0f);

		// Calculate item count label position
		font.draw(spriteBatch, slotItemStack.getItemCount() + "", itemCountLabelPos.x, itemCountLabelPos.y);
	}

	public void renderLast(SpriteBatch spriteBatch, BitmapFont font, boolean hasFocus, AGuiSkin skin, ShapeRenderer shapeRenderer)
	{
		// Get inventory instance
		ItemStack slotItemStack = ((IInventory) this.parent.getOverlayData().get(this.inventory)).getItemStackInSlot(this.slotId);
		
		// Check if there actually is an item stack in this slot.
		if (draggedSlot == this && slotItemStack != null)
		{
			Matrix4 backupTransform = null;
			Rectangle relativeRectangle = this.relativeRectangle;
			font.setColor(skin.getFontColor());

			backupTransform = new Matrix4(spriteBatch.getTransformMatrix());
			spriteBatch.setTransformMatrix(new Matrix4().idt().translate(new Vector3(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY(), 0)));
			relativeRectangle = new Rectangle(-(this.relativeRectangle.width / 2.0f), -(this.relativeRectangle.height / 2.0f), this.relativeRectangle.width, this.relativeRectangle.height);

			this.renderItemStack(spriteBatch, font, skin, slotItemStack, relativeRectangle);

			spriteBatch.setTransformMatrix(backupTransform);
		}
	}

}
