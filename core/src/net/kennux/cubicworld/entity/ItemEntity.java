package net.kennux.cubicworld.entity;

import net.kennux.cubicworld.CubicWorld;
import net.kennux.cubicworld.CubicWorldServer;
import net.kennux.cubicworld.item.ItemSystem;
import net.kennux.cubicworld.item.ItemType;
import net.kennux.cubicworld.networking.BitReader;
import net.kennux.cubicworld.networking.BitWriter;
import net.kennux.cubicworld.networking.CubicWorldServerClient;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Vector3;

/**
 * <pre>
 * The item entity implementation will get used for syncing and rendering of iems in the world.
 * </pre>
 * 
 * @author KennuX
 *
 */
public class ItemEntity extends ACharacterEntity
{
	private ItemType itemType;

	private Decal itemDecal;

	/**
	 * The milliseconds stamp when this item was initialized
	 */
	private long started;

	/**
	 * 
	 */
	private int framesPassedSinceLastUserCheck = 0;

	public ItemEntity()
	{
		super();
		this.started = System.currentTimeMillis();
	}

	public ItemEntity(int itemId)
	{
		this();
		this.itemType = ItemSystem.getItemType(itemId);
	}

	/**
	 * <pre>
	 * Reads serialized data obtained from a server packet.
	 * This implementation will just read position and eulerAngles as vector3
	 * (in this order) from the stream.
	 * Override this method if you want to extend the synchronization.
	 * </pre>
	 * 
	 * @param reader
	 */
	public void deserialize(BitReader reader)
	{
		this.setPosition(reader.readVector3());
	}

	/**
	 * Deserializes this entity's initial state.
	 * 
	 * @see AEntity#deserialize(BitReader)
	 * @param reader
	 */
	public void deserializeInitial(BitReader reader)
	{
		this.itemType = ItemSystem.getItemType(reader.readInt());
		this.deserialize(reader);
	}

	@Override
	protected float getCharacterHeight()
	{
		return 1;
	}

	@Override
	public void init()
	{
		this.voxelWorld = this.master.getWorld();
		this.setEntityName("Item");
	}

	@Override
	public void render(Camera camera, ModelBatch modelBatch, DecalBatch decalBatch, SpriteBatch spriteBatch, BitmapFont bitmapFont)
	{
		// Only if this item already got initialized
		if (this.itemType != null)
		{
			if (this.itemType.getItemModel() != null)
			{
				// Model rendering
				// TODO
			}
			else
			{
				// Billboard rendering
				if (this.itemDecal == null)
				{
					this.itemDecal = Decal.newDecal(this.itemType.getItemTextureRegion(), true);
					this.itemDecal.setWidth(1.0f);
					this.itemDecal.setHeight(1.0f);
				}

				// Offset + 0.5f + random
				Vector3 renderPos = new Vector3(this.position);
				renderPos.y += 0.5f + (0.1f * (float) Math.sin((System.currentTimeMillis() - this.started) / 200.0f));

				// Calculate direction from item to camera
				Vector3 dir = new Vector3(renderPos).sub(camera.position).nor();

				this.itemDecal.setRotation(dir, Vector3.Y);
				this.itemDecal.setPosition(renderPos);

				decalBatch.add(this.itemDecal);
			}
		}
	}

	/**
	 * <pre>
	 * Serializes the entity.
	 * This implementation will just write position and eulerAngles as vector3
	 * (in this order) to the stream.
	 * Override this method if you want to extend the synchronization.
	 * </pre>
	 * 
	 * @param writer
	 */
	public void serialize(BitWriter writer)
	{
		writer.writeVector3(this.position);
	}

	/**
	 * Serializes this entity's initial state.
	 * 
	 * @see AEntity#serialize(BitWriter)
	 * @param writer
	 */
	public void serializeInitial(BitWriter writer)
	{
		writer.writeInt(this.itemType.getItemId());
		this.serialize(writer);
	}

	@Override
	public void update()
	{
		super.update();

		if (this.master.isServer())
		{
			if (this.framesPassedSinceLastUserCheck >= 40)
			{
				// Check if there is a player around this item entity.
				CubicWorldServer server = CubicWorld.getServer();
				CubicWorldServerClient playerNearThisItem = null;

				synchronized (server.clientsLockObject)
				{
					for (CubicWorldServerClient serverClient : server.clients)
					{
						// Check if nearby
						if (serverClient != null && new Vector3(serverClient.playerEntity.getPosition()).sub(this.getPosition()).len() < 1.5f)
						{
							// Nearby!
							playerNearThisItem = serverClient;
							break;
						}
					}
				}

				if (playerNearThisItem != null)
				{
					// Pickup!
					int slotId = playerNearThisItem.playerEntity.playerInventory.getFirstStackForItemTypeWithSpace(this.itemType.getItemId());

					// Destroy if pickup was successfull
					if (playerNearThisItem.playerEntity.playerInventory.addItemsToStack(slotId, 1))
					{
						server.destroyEntity(this);
						this.master.remove(this.getEntityId());
					}
				}

				this.framesPassedSinceLastUserCheck = 0;
			}
			else
			{
				this.framesPassedSinceLastUserCheck++;
			}
		}
	}

}
