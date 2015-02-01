package net.kennux.cubicworld.entity;

import java.util.HashMap;
import java.util.Map.Entry;

import net.kennux.cubicworld.util.ConsoleHelper;

/**
 * Handles the registering of entities and their instancing.
 * 
 * @author KennuX
 *
 */
public class EntitySystem
{
	/**
	 * Initializes the entity system. Call this in the bootstrap first.
	 */
	@SuppressWarnings("rawtypes")
	public static void initialize()
	{
		entityCounter = 0;
		entityTypes = new HashMap<Integer, Class>();
	}

	/**
	 * Instantiates an entity of the type which maps to entityTypeId. Returns
	 * null if the type with the given id does not exist.
	 * 
	 * @param entityTypeId
	 * @return
	 */
	public static AEntity instantiateEntity(int entityTypeId)
	{
		// Does it exist?
		if (entityTypes.containsKey(entityTypeId))
		{
			try
			{
				return (AEntity) entityTypes.get(entityTypeId).newInstance();
			}
			// In case of an error return nothing
			catch (InstantiationException | IllegalAccessException e)
			{
				ConsoleHelper.writeLog("error", "Error instantiating entitytype with id " + entityTypeId + ": " + e, "EntitySystem");
				ConsoleHelper.logError(e);
				e.printStackTrace();
			}

			// Error :/
		}

		// It does not exist
		return null;
	}

	/**
	 * Registers an entity class in the entity system.
	 * 
	 * @param entityId
	 * @param entityType
	 */
	@SuppressWarnings("rawtypes")
	public static int registerEntity(Class entityClass)
	{
		if (!entityTypes.containsValue(entityClass))
		{
			// Add to types map
			entityTypes.put(entityCounter, entityClass);

			entityCounter++;
			return entityCounter - 1;
		}
		else
		{
			return EntitySystem.reverseLookup(entityClass);
		}
	}

	/**
	 * Performs a reverse lookup and serached for the type id of entity class.
	 * Will return -1 if the class is unknown.
	 * 
	 * @param entityClass
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static int reverseLookup(Class entityClass)
	{
		for (Entry<Integer, Class> entry : entityTypes.entrySet())
		{
			if (entry.getValue() == entityClass)
			{
				return entry.getKey().intValue();
			}
		}

		return -1;
	}

	/**
	 * Holds all available entitytypes
	 */
	@SuppressWarnings("rawtypes")
	private static HashMap<Integer, Class> entityTypes;

	/**
	 * The entity id counter.
	 * Will get incremented for every entity type added to the entity system.
	 */
	private static int entityCounter = 0;
}
