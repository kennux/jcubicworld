package net.kennux.cubicworld.util;

import java.lang.reflect.Array;

/**
 * <pre>
 * The object map holds multiple objects and maps them to a key.
 * The main difference between HashMap and ObjectMap is ObjectMap uses the
 * Equals() function to check if a key is similar to the given one.
 * 
 * Only use this map if you cannot use a hashmap!
 * 
 * This class is fully thread-safe!
 * </pre>
 * 
 * @author KennuX
 *
 */
public class ObjectMap<K, V>
{
	/**
	 * Gets locked if anything accesses keys or values array.
	 */
	private Object lockObject = new Object();

	/**
	 * Contains all keys stored in this map.
	 * The index of a key maps to an object in the values array.
	 */
	private K[] keys;

	/**
	 * Contains all valus stored in this map.
	 * The index of a value maps to an object in the keys array.
	 */
	private V[] values;

	/**
	 * The base size defines how big the array will get initialized and
	 * extended.
	 * So if the baseSize is 128, after adding 128 elements the arrays will get
	 * resized to 256.
	 */
	private final int baseSize = 128;

	/**
	 * The class of the key's object type.
	 * Used to call Array.newInstance()
	 * TODO Maybe don't use Array.newInstance?
	 */
	private Class<?> keyClass;

	/**
	 * The class of the value's object type.
	 * Used to call Array.newInstance()
	 * TODO Maybe don't use Array.newInstance?
	 */
	private Class<?> valueClass;

	@SuppressWarnings("unchecked")
	public ObjectMap(Class<?> keyClass, Class<?> valueClass)
	{
		// Init array
		this.keys = (K[]) Array.newInstance(keyClass, this.baseSize);
		this.values = (V[]) Array.newInstance(valueClass, this.baseSize);

		// Set classes
		this.keyClass = keyClass;
		this.valueClass = valueClass;
	}

	/**
	 * Checks if this map contains the given key.
	 * 
	 * @param key
	 * @return
	 */
	public boolean containsKey(K key)
	{
		synchronized (this.lockObject)
		{
			return this.findIndex(key) != -1;
		}
	}

	/**
	 * Extends the arrays keys and values.
	 * It will recreate them with their current size + baseSize and copy the old
	 * content into the new ones.
	 */
	@SuppressWarnings("unchecked")
	private void extendArrays()
	{
		int newSize = this.keys.length + this.baseSize;
		K[] newKeysArray = (K[]) Array.newInstance(this.keyClass, newSize);
		V[] newValuesArray = (V[]) Array.newInstance(this.valueClass, newSize);

		System.arraycopy(this.keys, 0, newKeysArray, 0, this.keys.length);
		System.arraycopy(this.values, 0, newValuesArray, 0, this.values.length);

		this.keys = newKeysArray;
		this.values = newValuesArray;
	}

	/**
	 * Returns a free index to store a new object.
	 * If the keys and values arrays are full it will extend them by baseSize.
	 * 
	 * @return
	 */
	private int findFreeIndex()
	{
		int freeIndex = -1;

		// Search free index
		for (int i = 0; i < this.keys.length; i++)
		{
			if (this.keys[i] == null)
			{
				freeIndex = i;
				break;
			}
		}

		// We got one?
		if (freeIndex == -1)
		{
			// Nope... extend the arrays!
			freeIndex = this.keys.length;
			this.extendArrays();
		}

		return freeIndex;
	}

	/**
	 * Finds the index for the given key.
	 * Will return -1 if the object was not found.
	 * 
	 * @param key
	 * @return
	 */
	private int findIndex(K key)
	{
		for (int i = 0; i < this.keys.length; i++)
		{
			// Check for equality
			if (this.keys[i] != null && this.keys[i].equals(key))
			{
				return i;
			}
		}

		return -1;
	}

	/**
	 * Returns the value saved to this map for key.
	 * Returns null if there is no value for the given key.
	 * 
	 * @param key
	 */
	public V get(K key)
	{
		synchronized (this.lockObject)
		{
			// Find the index
			int index = this.findIndex(key);

			// Wasn't it found?
			if (index == -1)
				return null;

			return this.values[index];
		}
	}

	/**
	 * Clones the keys array and returns it.
	 * 
	 * @return
	 */
	public K[] getKeys()
	{
		synchronized (this.lockObject)
		{
			return this.keys.clone();
		}
	}

	/**
	 * Clones the values array and returns it.
	 * 
	 * @return
	 */
	public V[] getValues()
	{
		synchronized (this.lockObject)
		{
			return this.values.clone();
		}
	}

	/**
	 * Puts the given value with the given key to this map.
	 * 
	 * @param key
	 * @param value
	 */
	public void put(K key, V value)
	{
		synchronized (this.lockObject)
		{
			int freeIndex = this.findFreeIndex();

			// Set data
			this.keys[freeIndex] = key;
			this.values[freeIndex] = value;
		}
	}
}
