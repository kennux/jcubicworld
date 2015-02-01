package net.kennux.cubicworld.entity;

import net.kennux.cubicworld.CubicWorldConfiguration;
import net.kennux.cubicworld.serialization.BitReader;
import net.kennux.cubicworld.serialization.BitWriter;
import net.kennux.cubicworld.voxel.VoxelWorld;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Vector3;

/**
 * <pre>
 * The entity manager will hold multiple entities.
 * As it's name says, it will manage them.
 * So it can:
 * 
 * - Update them
 * - Interpolate their position
 * - Render them
 * 
 * The update() method currently only iterates overy every entity. If there was
 * a player position given, a range check will get performed to check if the
 * entity is in the player's entity update distance.
 * update() will also call the interpolatePosition() function on every object
 * with the interpolation mode given in the constructor (direct or not).
 * 
 * If direct the entity's target position will get immediately flushed to the
 * entity position.
 * If not direct, a lerp() will be performed.
 * 
 * The render() method calls the render() method on every entity in this entity
 * manager.
 * </pre>
 * 
 * @author KennuX
 *
 */
public class EntityManager
{
	/**
	 * The entity ids. The index in this array maps to the entities array.
	 */
	private Integer[] entityIds;

	/**
	 * The entities array.
	 */
	private AEntity[] entities;

	/**
	 * Lock this if you access entityids or entities.
	 */
	private Object lockObject = new Object();

	/**
	 * If this is set to true, direct interpolation will get used in the
	 * interpolatePosition() call.
	 */
	private boolean directPositionInterpolation = false;

	/**
	 * Gets set to false if we are on a server.
	 * Dont init anything rendering related on the server.
	 */
	private boolean isServer = false;

	/**
	 * The voxel world set in the constructor.
	 */
	private VoxelWorld voxelWorld;

	private int entityCounter = 0;

	/**
	 * Client constructor.
	 */
	public EntityManager(VoxelWorld voxelWorld)
	{
		this.entities = new AEntity[128];
		this.entityIds = new Integer[128];
		this.voxelWorld = voxelWorld;
	}

	/**
	 * Server constructor.
	 * 
	 * @param useDirectInterpolation
	 * @param isServer
	 * @param entityCounterStart
	 *            the Start id for the entity manager (slotcount) on server.
	 */
	public EntityManager(VoxelWorld voxelWorld, boolean useDirectInterpolation, boolean isServer, int entityCounterStart)
	{
		this(voxelWorld);
		this.directPositionInterpolation = useDirectInterpolation;
		this.isServer = isServer;
		this.entityCounter = entityCounterStart;
	}

	/**
	 * Adds an entity to this entity manager instance.
	 * 
	 * @param id
	 * @param entity
	 */
	public void add(int id, AEntity entity)
	{
		synchronized (this.lockObject)
		{
			if (!this.containsId(id))
			{
				int freeIndex = this.findFreeIndex();

				if (freeIndex == -1)
				{
					this.extend(this.entityIds.length + 128);
					this.add(id, entity);
				}

				// Set id on the entity
				entity.setEntityId(id);
				this.entities[freeIndex] = entity;
				this.entityIds[freeIndex] = new Integer(id);

				entity.setMaster(this);
				entity.init();
			}
		}
	}

	/**
	 * Performs a cleanup.
	 * Interates through every entity and checks if it is too far away from players to exist.
	 * 
	 * @param playerPositions
	 */
	public void cleanup(Vector3[] playerPositions)
	{
		synchronized (this.lockObject)
		{
			for (AEntity entity : this.entities)
			{
				if (entity != null)
				{
					for (Vector3 position : playerPositions)
					{
						if (new Vector3(entity.getPosition()).sub(position).len() > CubicWorldConfiguration.entityCullDistance)
						{
							entity.die();
						}
					}
				}
			}
		}
	}

	/**
	 * Deletes all entities in the list who haven't got an update since timeout
	 * milliseconds.
	 * 
	 * @param timeout
	 */
	public void cleanupUpdateTimeout(int timeout)
	{
		for (int i = 0; i < this.entityIds.length; i++)
		{
			if (this.entities[i] != null && System.currentTimeMillis() - this.entities[i].getLastUpdateTime() >= timeout)
			{
				this.remove(this.entityIds[i]);
			}
		}
	}

	/**
	 * Returns true if the given id is already in use.
	 * 
	 * @param id
	 * @return
	 */
	public boolean containsId(int id)
	{
		return this.findIndexById(id) != -1;
	}

	/**
	 * Deserializes the entities in the reader's data.
	 * 
	 * @param reader
	 */
	public void deserialize(BitReader reader)
	{
		synchronized (this.lockObject)
		{
			while (reader.hasDataLeft())
			{
				// Get entity id
				AEntity entity = EntitySystem.instantiateEntity(reader.readInt());

				// Deserialize entity data
				entity.deserializeInitial(reader);

				// Add entity
				this.add(this.getNextFreeId(), entity);
			}
		}
	}

	/**
	 * Extends this instance's capacity. newSize is the new capacity.
	 * 
	 * @param newSize
	 */
	private void extend(int newSize)
	{
		// Create new arrays
		Integer[] newEntityIds = new Integer[newSize];
		AEntity[] newEntities = new AEntity[newSize];

		// Copy old data
		System.arraycopy(this.entityIds, 0, newEntityIds, 0, this.entityIds.length);
		System.arraycopy(this.entities, 0, newEntities, 0, this.entities.length);

		// Set new arrays
		this.entityIds = newEntityIds;
		this.entities = newEntities;
	}

	/**
	 * Searches in the keys array for a free index and return's it's index.
	 * REMEMBER: key and value array indices are the same.
	 * 
	 * Returns -1 if there was no free index found (extend should get called
	 * then to extend the arrays).
	 * 
	 * @return
	 */
	private int findFreeIndex()
	{
		for (int i = 0; i < this.entityIds.length; i++)
		{
			if (this.entityIds[i] == null)
				return i;
		}

		return -1;
	}

	/**
	 * Returns the index of the entity to search. Returns -1 if the entity was
	 * not found.
	 * 
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unused")
	private int findIndexByEntity(AEntity entity)
	{
		for (int i = 0; i < this.entities.length; i++)
		{
			// Search in the entity index.
			if (this.entities[i] != null && this.entities[i] == entity)
			{
				return i;
			}
		}

		// Not found
		return -1;
	}

	/**
	 * Returns the index of the key to search. Returns -1 if the key was not
	 * found.
	 * 
	 * @param key
	 * @return
	 */
	private int findIndexById(Integer id)
	{
		for (int i = 0; i < this.entityIds.length; i++)
		{
			// Search in the entity index.
			if (this.entityIds[i] != null && this.entityIds[i].equals(id))
			{
				return i;
			}
		}

		// Not found
		return -1;
	}

	/**
	 * Returns the entity saved with the given id. Returns null if the entity
	 * was not found.
	 * 
	 * @param id
	 * @return
	 */
	public AEntity get(int id)
	{
		synchronized (this.lockObject)
		{
			// Search index
			int index = this.findIndexById(id);

			if (index == -1)
				return null;

			return this.entities[index];
		}
	}

	/**
	 * Returns all entitys as an array.
	 * 
	 * @return
	 */
	public AEntity[] getEntityArray()
	{
		return this.entities.clone();
	}

	/**
	 * Increments the entity counter and returns it (First increment, then
	 * return so counterstart at 60 means first id is 61).
	 * 
	 * @return
	 */
	public int getNextFreeId()
	{
		this.entityCounter++;
		return this.entityCounter;
	}

	/**
	 * Used to return a reference of the voxelworld set to this instance.
	 * 
	 * @return
	 */
	public VoxelWorld getWorld()
	{
		return this.voxelWorld;
	}

	public boolean isClient()
	{
		return !this.isServer;
	}

	public boolean isServer()
	{
		return this.isServer;
	}

	/**
	 * Removes the entity. If it is not in the list, nothing will be done here.
	 * This class does not handle any destroy packet sending.
	 * If you need immediate the object to be immediately destroyed on the
	 * client, usethe destroyEntity() function of the CubicWorldServer class.
	 * Otherwise it will die after some time not recieving updates from the
	 * server.
	 * 
	 * @param id
	 */
	public void remove(int id)
	{
		synchronized (this.lockObject)
		{
			int index = this.findIndexById(id);

			if (index != -1)
				this.removeIndex(index);
		}
	}

	/**
	 * Removes the entity in the given index (set's the references to null).
	 * 
	 * @param index
	 */
	private void removeIndex(int index)
	{
		this.entities[index] = null;
		this.entityIds[index] = null;
	}

	/**
	 * Renders all entities managed (added by the add() method).
	 */
	public void render(Camera camera, ModelBatch modelBatch, DecalBatch decalBatch, SpriteBatch spriteBatch, BitmapFont bitmapFont)
	{
		synchronized (this.lockObject)
		{
			for (AEntity entity : this.entities)
			{
				if (entity != null)
					entity.render(camera, modelBatch, decalBatch, spriteBatch, bitmapFont);
			}
		}
	}

	/**
	 * Serializes ALL entities in this entity manager.
	 * 
	 * @param writer
	 */
	public void serialize(BitWriter writer)
	{
		synchronized (this.lockObject)
		{
			for (AEntity entity : this.entities)
			{
				// Skip not initialized entities
				if (entity == null)
					continue;

				// Write entity data
				writer.writeInt(EntitySystem.reverseLookup(entity.getClass()));
				entity.serializeInitial(writer);
			}
		}
	}

	/**
	 * Updates all entities managed (added by the add() method).
	 * Calls the update method and interpolate position!
	 */
	public void update()
	{
		synchronized (this.lockObject)
		{
			for (AEntity entity : this.entities)
			{
				if (entity != null)
				{
					entity.interpolatePosition(this.directPositionInterpolation);
					entity.update();
				}
			}
		}
	}

	/**
	 * Updates all entities managed (added by the add() method).
	 * This call also performs distance culling and removes all entities out of
	 * range.
	 */
	public void update(Vector3 playerPosition)
	{
		synchronized (this.lockObject)
		{
			for (AEntity entity : this.entities)
			{
				if (entity != null)
				{
					entity.interpolatePosition(this.directPositionInterpolation);

					if (entity.isInEntityViewDistance(playerPosition))
						entity.update();
				}
			}
		}
	}
}
