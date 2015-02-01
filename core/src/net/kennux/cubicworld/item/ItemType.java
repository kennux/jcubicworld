package net.kennux.cubicworld.item;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * <pre>
 * Used to define item types.
 * Register them in your bootstrap.
 * </pre>
 * 
 * @author kennux
 *
 */
public class ItemType
{
	/**
	 * The name of this item.
	 */
	private String itemName = "";

	/**
	 * The item type's type (sounds strange o_O).
	 */
	private ItemClass type;

	/**
	 * The tool view for this item type.
	 */
	private IItemView itemView;

	/**
	 * The model of the item used for instancing ModelInstances.
	 */
	private Model itemModel;

	/**
	 * Reference to the texture atlas which will get used for rendering.
	 */
	private Texture textureAtlas;

	/**
	 * The maximum stacksize.
	 * Standard stacksize : 64.
	 */
	private byte stackSize = 64;

	/**
	 * The id of this item type used for serialization.
	 */
	private int itemId;

	/**
	 * The uv coordinates for the item texture on the textureatlas
	 * 
	 * @see ItemType#textureAtlas
	 */
	private Vector2[] itemTextureUv;

	/**
	 * The texture region of the item main texture in the current textureatlas.
	 */
	private TextureRegion itemTextureRegion;

	/**
	 * @return the itemId
	 */
	public int getItemId()
	{
		return itemId;
	}

	/**
	 * @return the itemModel
	 */
	public Model getItemModel()
	{
		return itemModel;
	}

	/**
	 * @return the itemName
	 */
	public String getItemName()
	{
		return itemName;
	}

	/**
	 * Returns the item texture region of this item's main texture.
	 * 
	 * @return
	 */
	public TextureRegion getItemTextureRegion()
	{
		return this.itemTextureRegion;
	}

	/**
	 * Returns the uv coordinates for the texture atlas set in this itemtype.
	 * The textureatlas is the atlas from ItemSystem.
	 * This may be null if the item type is model rendered.
	 * 
	 * @return
	 */
	public Vector2[] getItemTextureUv()
	{
		return this.itemTextureUv;
	}

	/**
	 * @return the itemView
	 */
	public IItemView getItemView()
	{
		return itemView;
	}

	/**
	 * @return the stackSize
	 */
	public byte getStackSize()
	{
		return stackSize;
	}

	/**
	 * @return the textureAtlas
	 */
	public Texture getTextureAtlas()
	{
		return textureAtlas;
	}

	/**
	 * @return the type. DO NOT MODIFY IT!
	 */
	public ItemClass getType()
	{
		return type;
	}

	/**
	 * Returns true if this item gets rendered by rendering it's model instead of a texture billboard.
	 * 
	 * @return
	 */
	public boolean isModelRendered()
	{
		return (this.itemModel != null);
	}

	/**
	 * @param itemId
	 *            the itemId to set
	 */
	public ItemType setItemId(int itemId)
	{
		this.itemId = itemId;
		return this;
	}

	/**
	 * @param itemModel
	 *            the itemModel to set
	 * @return
	 */
	public ItemType setItemModel(Model itemModel)
	{
		this.itemModel = itemModel;
		return this;
	}

	/**
	 * @param itemName
	 *            the itemName to set
	 */
	public ItemType setItemName(String itemName)
	{
		this.itemName = itemName;
		return this;
	}

	/**
	 * Sets this item's texture id.
	 * This texture is used for rendering in inventories or as billboard if there is no model available.
	 * Every item has to have a item texture!
	 * 
	 * @param textureId
	 */
	public ItemType setItemTexture(int textureId)
	{
		this.itemTextureUv = ItemSystem.getUvForTexture(textureId);
		Rectangle rect = ItemSystem.textureAtlas.getAtlasRegion(textureId);

		// Only init texture region on the client
		if (ItemSystem.textureAtlas.atlasTexture != null)
			this.itemTextureRegion = new TextureRegion(ItemSystem.textureAtlas.atlasTexture, (int) rect.x, (int) rect.y, (int) rect.width, (int) rect.height);

		return this;
	}

	/**
	 * @param itemView
	 *            the itemView to set
	 */
	public ItemType setItemView(IItemView itemView)
	{
		this.itemView = itemView;
		return this;
	}

	/**
	 * @param stackSize
	 *            the stackSize to set
	 */
	public ItemType setStackSize(byte stackSize)
	{
		this.stackSize = stackSize;
		return this;
	}

	/**
	 * IMPORTANT: Only set this manually if you really need it.
	 * The atlas will get set in the ItemSystem.finalization() method.
	 * 
	 * @see ItemSystem#finalizeTextureAtlas()
	 * @param textureAtlas
	 *            the textureAtlas to set
	 */
	public ItemType setTextureAtlas(Texture textureAtlas)
	{
		this.textureAtlas = textureAtlas;
		return this;
	}

	/**
	 * Sets the item type of this type.
	 * 
	 * @param type
	 * @return
	 */
	public ItemType setType(ItemClass type)
	{
		this.type = type;
		return this;
	}
}
