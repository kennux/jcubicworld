package net.kennux.cubicworld.item;

import java.util.HashMap;
import java.util.Map.Entry;

import net.kennux.cubicworld.assets.TextureAtlas;
import net.kennux.cubicworld.voxel.VoxelEngine;
import net.kennux.cubicworld.voxel.VoxelType;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMesh;
import com.badlogic.gdx.math.Vector2;

/**
 * <pre>
 * The main item system class.
 * The item system handles item registration and instantiation.
 * 
 * Items are divided into normal "useless" items, which can only get dropped or used for crafting.
 * There are also "tools" which change the behaviour of the fps rendering, for example it can be an
 * Weapon or a tool like a pickaxe.
 * The last type are "food", food is used to regenerate health.
 * 
 * </pre>
 * 
 * @author kennux
 *
 */
public class ItemSystem
{
	/**
	 * Do this directly after you added all textures.
	 */
	public static void compileTextureAtlas()
	{
		textureAtlas.compileTexture();
	}

	/**
	 * Creates item types for all voxel types registered in VoxelEngine.
	 * It creates block models for all voxel types.
	 * The block model's 0|0|0 origin is their center.
	 * Their diameter is 0.2 units.
	 * 
	 * TODO: Use meshbuilder
	 */
	public static void createItemsForVoxelTypes()
	{
		// Get all voxel types
		VoxelType[] voxelTypes = VoxelEngine.getVoxelTypes();

		// Construct models
		for (int i = 0; i < voxelTypes.length; i++)
		{
			// Prepare model data
			ModelData modelData = new ModelData();
			ModelMesh modelMesh = new ModelMesh();

			// Setup attributes
			modelMesh.attributes = new VertexAttribute[] { new VertexAttribute(Usage.Position, 4, "a_position"),
					// new VertexAttribute(Usage.Normal, 4, "a_normal"),
					new VertexAttribute(Usage.TextureCoordinates, 2, "a_uv"), };

			// Setup vertices
			float[] vertices = new float[] { -0.1f, -0.1f, -0.1f, 0, 0, 0.1f, -0.1f, -0.1f, 0, 0, 0.1f, 0.1f, -0.1f, 0, 0, -0.1f, 0.1f, -0.1f, 0, 0,

			-0.1f, -0.1f, 0.1f, 0, 0, 0.1f, -0.1f, 0.1f, 0, 0, 0.1f, 0.1f, 0.1f, 0, 0, -0.1f, 0.1f, 0.1f, 0, 0 };

			modelMesh.vertices = vertices;
			Model voxelModel = new Model(modelData);

			// Set item type information
			@SuppressWarnings("unused")
			int itemId = ItemSystem.registerItemType(VoxelEngine.getNameByVoxelId(voxelTypes[i].voxelId)).setItemModel(voxelModel).getItemId();
		}
	}

	/**
	 * Finalizes the item system's texture atlas.
	 * This will set the texture atlas reference where it is not already set.
	 * Will get called after all items were registered to the system.
	 */
	public static void finalizeTextureAtlas()
	{
		for (Entry<Integer, ItemType> e : itemTypes.entrySet())
		{
			if (e.getValue().getTextureAtlas() == null)
			{
				e.getValue().setTextureAtlas(ItemSystem.textureAtlas.atlasTexture);
			}
		}
	}

	/**
	 * Returns the item type for the given item id.
	 * 
	 * @param itemId
	 * @return
	 */
	public static ItemType getItemType(int itemId)
	{
		return itemTypes.get(itemId);
	}

	/**
	 * Returns the item id for a given item name.
	 * IMPORTANT: Try to avoid this method, as it performs a very expensive search.
	 * Returns -1 if the item was not found.
	 * 
	 * @param name
	 * @return
	 */
	public static int getItemTypeIdByName(String name)
	{
		for (Entry<Integer, String> e : typeDictionary.entrySet())
		{
			if (e.getValue() == name)
			{
				return e.getKey().intValue();
			}
		}

		return -1;
	}

	/**
	 * Returns the texture with the given texture id. Returns null if the
	 * texture is not found.
	 * 
	 * @param textureId
	 * @return
	 */
	public static Texture getTexture(int textureId)
	{
		// Search texture by id
		Integer texId = new Integer(textureId);
		if (textures.containsKey(texId))
		{
			return textures.get(texId);
		}

		return null;
	}

	/**
	 * Returns the texture coordinates for the given texture id.
	 * 
	 * @param textureId
	 * @return
	 */
	public static Vector2[] getUvForTexture(int textureId)
	{
		return textureAtlas.getUvForTexture(textureId);
	}

	public static ItemType[] getItemTypes()
	{
		return itemTypes.values().toArray(new ItemType[itemTypes.size()]);
	}

	/**
	 * Initializes all hashmaps if they aren't already.
	 */
	public static void initialize(int textureWidth, int textureHeight)
	{
		typeDictionary = new HashMap<Integer, String>();
		itemTypes = new HashMap<Integer, ItemType>();
		textures = new HashMap<Integer, Texture>();
		textureDictionary = new HashMap<String, Integer>();
		textureAtlas = new TextureAtlas(textureWidth, textureHeight);
		typeIdCounter = 0;

		// Init static meshes
		/*
		 * itemBillboardingMesh = new Mesh(true, 4, 4, new VertexAttribute(Usage.Position, 3, "a_position"), new VertexAttribute(Usage.TextureCoordinates, 1, "a_uv"));
		 * float[] vertices = new float[]
		 * {
		 * 0,0,0,1,
		 * 1,0,0,2,
		 * 1,1,0,3,
		 * 0,1,0,4
		 * };
		 * short[] indices = new short[] { 1,2,3,4 };
		 * itemBillboardingMesh.setVertices(vertices);
		 * itemBillboardingMesh.setIndices(indices);
		 */
	}

	/**
	 * Registers an item type to this item system.
	 * 
	 * @return The id of the registered item.
	 */
	public static ItemType registerItemType(String name)
	{
		if (!typeDictionary.containsValue(name))
		{
			ItemType type = new ItemType().setItemName(name).setItemId(typeIdCounter);

			typeDictionary.put(typeIdCounter, name);
			itemTypes.put(typeIdCounter, type);

			typeIdCounter++;
			return type;
		}
		else
		{
			return ItemSystem.getItemType(ItemSystem.getItemTypeIdByName(name));
		}
	}

	/**
	 * Registers a texture and returns it's texture id.
	 * 
	 * @param textureName
	 * @return
	 */
	public static int registerTexture(String textureName, Texture texture)
	{
		if (!textureDictionary.containsKey(textureName))
		{
			int textureId = textureAtlas.addTexture(texture);

			textureDictionary.put(textureName, textureId);
			textures.put(textureId, texture);

			return textureId;
		}
		else
		{
			return textureDictionary.get(textureName).intValue();
		}
	}

	/**
	 * The key in this hashmap maps to the item id as value.
	 * The item id is generated while registering items.
	 */
	private static HashMap<Integer, String> typeDictionary;

	/**
	 * Contains all item types registered to the item system.
	 */
	private static HashMap<Integer, ItemType> itemTypes;

	/**
	 * The type id counter used for getting item ids.
	 */
	private static int typeIdCounter = 0;

	/**
	 * The voxel textures registered in the engine
	 */
	private static HashMap<Integer, Texture> textures;

	/**
	 * The types dictionary maps type names to shorts.
	 */
	private static HashMap<String, Integer> textureDictionary;

	/**
	 * The item billboarding mesh.
	 * Initialized in initalize().
	 */
	public static Mesh itemBillboardingMesh;

	public static TextureAtlas textureAtlas;
}
