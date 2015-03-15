package net.kennux.cubicworld.entity;

import net.kennux.cubicworld.CubicWorld;
import net.kennux.cubicworld.CubicWorldConfiguration;
import net.kennux.cubicworld.serialization.BitReader;
import net.kennux.cubicworld.serialization.BitWriter;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Vector3;

/**
 * <pre>
 * Abstract entity class.
 * EVERY entity has:
 * 
 * - id
 * - name
 * - position
 * - rotation
 * - master (EntityManager)
 * 
 * This class implements basic entity syncing.
 * It synchronizes entity position and rotation every frame.
 * 
 * This class does not implement physics, if you need a character entity with
 * physics and so on, you may better use ACharacterEntity or AMobEntity / AModelMobEntity (+ model rendering).
 * It is an extended version of the AEntity class.
 * </pre>
 * 
 * @author KennuX
 *
 */
public abstract class AEntity
{
	/**
	 * The last time recievedUpdate() was called.
	 */
	private long lastTimeUpdated = 0;

	/**
	 * The entity position. This position gets used for rendering and collision
	 * checks.
	 */
	protected Vector3 position = new Vector3();

	/**
	 * The entity euler angles. This angles get used for rendering and collision
	 * checks.
	 */
	protected Vector3 eulerAngles = new Vector3();

	/**
	 * The target position used for rendering interpolation.
	 */
	protected Vector3 targetPosition = new Vector3();

	/**
	 * The target euler angles used for rendering interpolation.
	 */
	protected Vector3 targetEulerAngles = new Vector3();

	/**
	 * The entity id
	 */
	protected int entityId;

	/**
	 * The entity manager which manages this entity.
	 */
	protected EntityManager master;

	/**
	 * <pre>
	 * The entity name.
	 * On normal mob entities it is the mob type's name.
	 * Players or bigger mobs like bosses should contain their name in here.
	 * 
	 * You can use this for rendering gui or a nameplate over the entities head.
	 * </pre>
	 */
	protected String entityName = "";

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
		this.setEulerAngles(reader.readVector3());
	}

	/**
	 * Deserializes this entity's initial state.
	 * 
	 * @see AEntity#deserialize(BitReader)
	 * @param reader
	 */
	public void deserializeInitial(BitReader reader)
	{
		this.deserialize(reader);
	}

	/**
	 * <pre>
	 * This function calls the CubicWorldServer.destroyEntity() function on the
	 * server.
	 * 
	 * On client it will only remove the entity from the entity manger.
	 * Normally you do not execute this on the client, it could cause some very
	 * strange things!
	 * </pre>
	 * 
	 */
	public void die()
	{
		// If server, send out destroy packets
		if (this.master.isServer())
			CubicWorld.getServer().destroyEntity(this);

		// If master initialized remove entity
		if (this.master != null)
			this.master.remove(this.getEntityId());
	}

	// Getters and setters
	public int getEntityId()
	{
		return this.entityId;
	}

	public String getEntityName()
	{
		return this.entityName;
	}

	public Vector3 getEulerAngles()
	{
		return eulerAngles;
	}

	public long getLastUpdateTime()
	{
		return this.lastTimeUpdated;
	}

	public Vector3 getPosition()
	{
		return position;
	}

	/**
	 * Initialize all resources of your entity in here.
	 * This function is called right after setMaster() got called.
	 */
	public abstract void init();

	/**
	 * <pre>
	 * Performs a position interpolation. If you call setPosition() the target
	 * position will get overwritten.
	 * 
	 * If direct is set the position will instantly be overwritten. Otherwise a
	 * lerp() is performed.
	 * </pre>
	 * 
	 */
	public void interpolatePosition(boolean direct)
	{
		if (direct)
		{
			this.position = this.targetPosition;
			this.eulerAngles = this.targetEulerAngles;
		}
		else
		{
			this.position.lerp(this.targetPosition, 0.8f);
			this.eulerAngles.lerp(this.targetEulerAngles, 0.8f);
		}
	}

	/**
	 * Returns true if the distance from this entity to the given player is less
	 * than CubicWorldConfiguration.entityCullDistance.
	 * 
	 * @param player
	 * @return
	 */
	public boolean isInEntityViewDistance(AEntity entity)
	{
		return new Vector3(entity.getPosition()).sub(this.getPosition()).len() < CubicWorldConfiguration.entityCullDistance;
	}

	/**
	 * Returns true if the distance from this entity to the given player is less
	 * than CubicWorldConfiguration.entityCullDistance.
	 * 
	 * @param position
	 * @return
	 */
	public boolean isInEntityViewDistance(Vector3 position)
	{
		return new Vector3(position).sub(this.getPosition()).len() < CubicWorldConfiguration.entityCullDistance;
	}

	/**
	 * Call this after your entity recieved a position update from the server.
	 * It will set the current time to the last update time.
	 */
	public void recievedUpdate()
	{
		this.lastTimeUpdated = System.currentTimeMillis();
	}

	/**
	 * Render this entity in this function. It will only get called on the
	 * client.
	 */
	public abstract void render(Camera camera, ModelBatch modelBatch, DecalBatch decalBatch, SpriteBatch spriteBatch, BitmapFont bitmapFont);

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
		writer.writeVector3(this.eulerAngles);
	}

	/**
	 * Serializes this entity's initial state.
	 * 
	 * @see AEntity#serialize(BitWriter)
	 * @param writer
	 */
	public void serializeInitial(BitWriter writer)
	{
		this.serialize(writer);
	}

	public void setEntityId(int id)
	{
		this.entityId = id;
	}

	public void setEntityName(String name)
	{
		this.entityName = name;
	}

	public void setEulerAngles(Vector3 euler)
	{
		this.targetEulerAngles = new Vector3(euler);
	}

	public void setMaster(EntityManager entityManager)
	{
		this.master = entityManager;
	}

	public void setPosition(Vector3 position)
	{
		this.targetPosition = new Vector3(position);
	}

	/**
	 * <pre>
	 * Simulate your entity behaviour in this function.
	 * Remember to keep all the logic on the server.
	 * Use this.master.isServer() or this.master.isClient() to check where you
	 * currently are.
	 * </pre>
	 * 
	 */
	public abstract void update();
}
