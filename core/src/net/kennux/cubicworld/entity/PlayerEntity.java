package net.kennux.cubicworld.entity;

import net.kennux.cubicworld.inventory.PlayerInventory;
import net.kennux.cubicworld.networking.BitReader;
import net.kennux.cubicworld.networking.BitWriter;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

/**
 * <pre>
 * Player entity implementation.
 * Each client will have it's own player entity, the server holds a list for every player's entity and fires the deserialize function if a player update packet arrives.
 * </pre>
 * 
 * @author KennuX
 *
 */
public class PlayerEntity extends AEntity
{
	private static Model cubeModel;
	private ModelInstance cubeInstance;
	private static FrameBuffer nameTextFramebuffer;

	public PlayerInventory playerInventory;

	public PlayerEntity()
	{
		this.playerInventory = new PlayerInventory();
	}

	/**
	 * Deserializes the player entity for saving.
	 */
	public void deserializeFull(BitReader reader)
	{
		this.playerInventory.deserializeInventory(reader);
	}

	protected float getCharacterHeight()
	{
		return 1.75f;
	}

	/**
	 * Returns the save file path for this player entity.
	 * Relative to the save location.
	 * 
	 * @return
	 */
	public String getSaveFilePath()
	{
		return "players/" + this.getEntityName() + ".dat";
	}

	/**
	 * Initializes rendering stuff if this is clientside,
	 * Otherwise this does just nothing.
	 */
	@Override
	public void init()
	{
		if (this.master.isClient())
		{
			if (cubeModel == null)
			{
				// Init
				ModelBuilder modelBuilder = new ModelBuilder();
				cubeModel = modelBuilder.createBox(1f, 2f, 1f, new Material(ColorAttribute.createDiffuse(Color.GREEN)), Usage.Position | Usage.Normal);

				// Init framebuffer
				if (nameTextFramebuffer == null)
					nameTextFramebuffer = new FrameBuffer(Format.RGBA8888, 200, 200, false);
			}
			// Init locally
			cubeInstance = new ModelInstance(cubeModel);
		}
	}

	/**
	 * Renders the player.
	 * Currently only renders a green box which got loaded in init().
	 * 
	 * TODO Implement REAL player rendering
	 */
	@Override
	public void render(Camera camera, ModelBatch modelBatch, DecalBatch decalBatch, SpriteBatch spriteBatch, BitmapFont bitmapFont)
	{
		Vector3 rootPos = new Vector3(this.getPosition());
		rootPos.y += 1;

		// Frustum check
		if (camera.frustum.boundsInFrustum(new BoundingBox(new Vector3(this.position), new Vector3(this.position).add(0, 2, 0))))
		{
			cubeInstance.transform.set(rootPos, new Quaternion());
			modelBatch.render(cubeInstance);

			rootPos.y += 1.5f;

			// Project worldpos
			Vector3 screenPos = camera.project(rootPos);
			screenPos.x -= bitmapFont.getBounds(this.getEntityName()).width / 2;
			bitmapFont.draw(spriteBatch, this.getEntityName(), screenPos.x, screenPos.y);
		}
	}

	/**
	 * Serializes the player entity for saving.
	 */
	public void serializeFull(BitWriter writer)
	{
		this.playerInventory.serializeInventory(writer);
	}

	/**
	 * A player entity is not updateable, this function doesnt do anything.
	 */
	@Override
	public void update()
	{

	}
}
