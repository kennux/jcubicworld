package net.kennux.cubicworld.assets;

import java.util.ArrayList;
import java.util.HashMap;

import net.kennux.cubicworld.util.Mathf;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * <pre>
 * Texture atlas implementation.
 * It can build a texture atlas based on multiple textures given or it can load
 * already packed atlases.
 * 
 * Important thing about this class is, all your textures need to have the same size (power of 2)!
 * </pre>
 * 
 * @author KennuX
 *
 */
public class TextureAtlas
{
	/**
	 * The already builded atlas.
	 */
	public Texture atlasTexture;

	/**
	 * Single atlas textures used when compiling this texture atlas.
	 */
	private ArrayList<Texture> textures;

	/**
	 * Contains the texture coordinates for the texture id in the key.
	 */
	private static HashMap<Integer, Vector2[]> textureCoordinates;

	/**
	 * Single texture width.
	 */
	private int textureWidth;

	/**
	 * Single texture height.
	 */
	private int textureHeight;

	/**
	 * Gets set to the maximum number of textures this atlas can hold.
	 */
	private int spaceForTextures;

	/**
	 * The maximum atlas size x and y
	 */
	private static final int maximumAtlasSize = 4096;

	// Atlas dimensions calculated in compile function
	private int atlasWidth;
	private int atlasHeight;

	/**
	 * <pre>
	 * Initializes the texture atlas in texture compilation mode.
	 * Add textures by addTextures().
	 * If you're done call compile().
	 * </pre>
	 * 
	 * @param textureHeight
	 * @param textureWidth
	 */
	public TextureAtlas(int textureWidth, int textureHeight)
	{
		// Init compilation mode
		this.textures = new ArrayList<Texture>();

		// Init texture width
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;

		textureCoordinates = new HashMap<Integer, Vector2[]>();

		this.spaceForTextures = (this.textureWidth * this.textureHeight) / (maximumAtlasSize * maximumAtlasSize);
	}

	/**
	 * <pre>
	 * Adds a texture to the texture atlas in compilation mode.
	 * You cannot edit this after you compiled the texture atlas.
	 * Or if you are not in compilation mode.
	 * 
	 * Returns -1 in case of an error, otherwise the texture's id.
	 * </pre>
	 * 
	 * @param t
	 */
	public int addTexture(Texture t)
	{
		if (this.textures != null)
		{
			this.textures.add(t);
			return this.textures.size() - 1;
		}
		else
			return -1;
	}

	/**
	 * <pre>
	 * Compiles all textures in this.textures.
	 * After calling this function you cannot add any more textures.
	 * </pre>
	 */
	public void compileTexture()
	{
		// Max textures per axis
		int maxTexturesOnX = Mathf.floorToInt((float) maximumAtlasSize / (float) textureWidth);
		int maxTexturesOnY = Mathf.floorToInt((float) maximumAtlasSize / (float) textureHeight);

		// texture per axis
		int texturesOnX = Mathf.min(textures.size(), maxTexturesOnX);
		int texturesOnY = Mathf.ceilToInt((float) textures.size() / (float) maxTexturesOnY);

		// Calculate atlas dimensions
		this.atlasWidth = Mathf.min(textures.size(), maxTexturesOnX) * textureWidth;
		this.atlasHeight = Mathf.ceilToInt((float) textures.size() / (float) maxTexturesOnY) * textureWidth;

		// Create texture
		Pixmap atlasData = new Pixmap(atlasWidth, atlasHeight, Format.RGBA8888);

		// Build atlas
		int texture = 0;

		for (int x = 0; x < texturesOnX; x++)
		{
			for (int y = 0; y < texturesOnY; y++)
			{
				// Get current starting position
				int xPosition = x * textureWidth;
				int yPosition = y * textureHeight;

				// Get current texture
				Texture currentTexture = textures.get(texture);
				currentTexture.getTextureData().prepare();

				// Calculate the texture coordinates
				textureCoordinates.put(texture, new Vector2[] { new Vector2((float) xPosition / (float) atlasWidth, ((float) yPosition / (float) atlasHeight) + ((float) textureHeight / (float) atlasHeight)), new Vector2(((float) xPosition / (float) atlasWidth) + ((float) textureWidth / (float) atlasWidth), ((float) yPosition / (float) atlasHeight) + ((float) textureHeight / (float) atlasHeight)), new Vector2(((float) xPosition / (float) atlasWidth) + ((float) textureWidth / (float) atlasWidth), (float) yPosition / (float) atlasHeight), new Vector2((float) xPosition / (float) atlasWidth, (float) yPosition / (float) atlasHeight) });

				// Write texture to atlas
				atlasData.drawPixmap(currentTexture.getTextureData().consumePixmap(), xPosition, yPosition);
				texture++;
			}
		}

		this.atlasTexture = new Texture(atlasData, Format.RGBA8888, false);
		this.atlasTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
	}

	/**
	 * <pre>
	 * Gets the atlas region for the texture texture at position n.
	 * n is the number of the texture id.
	 * the ids are given in the order you called addTexture.
	 * </pre>
	 * 
	 * @param n
	 */
	public Rectangle getAtlasRegion(int textureId)
	{
		int maxTexturesOnX = Mathf.floorToInt((float) maximumAtlasSize / (float) textureWidth);
		int maxTexturesOnY = Mathf.floorToInt((float) maximumAtlasSize / (float) textureHeight);

		int textureY = textureId % maxTexturesOnY;
		int textureX = Mathf.floorToInt(textureId / (float) maxTexturesOnX);

		// build rectangle
		return new Rectangle(textureX * this.textureWidth, textureY * this.textureHeight, this.textureWidth, this.textureHeight);
	}

	/**
	 * <pre>
	 * Returns the current atlas texture.
	 * Returns null if in compilation state and not compiled yet.
	 * 
	 * </pre>
	 * 
	 * @return
	 */
	public Texture getAtlasTexture()
	{
		return this.atlasTexture;
	}

	/**
	 * Returns the texture coordinates for the given texture id.
	 * 
	 * @param textureId
	 * @return
	 */
	public Vector2[] getUvForTexture(int textureId)
	{
		/*
		 * int maxTexturesOnX = Mathf.floorToInt((float) maximumAtlasSize / (float) textureWidth);
		 * int maxTexturesOnY = Mathf.floorToInt((float) maximumAtlasSize / (float) textureHeight);
		 * 
		 * int xPosition = textureId % maxTexturesOnY;
		 * int yPosition = Mathf.floorToInt(textureId / (float) maxTexturesOnX);
		 * 
		 * return new Vector2[] { new Vector2((float) xPosition / (float) this.atlasWidth,
		 * ((float) yPosition / (float) this.atlasHeight) + ((float) this.textureHeight / (float) this.atlasHeight)),
		 * new Vector2(((float) xPosition / (float) this.atlasWidth) + ((float) this.textureWidth / (float) this.atlasWidth),
		 * ((float) yPosition / (float) this.atlasHeight) + ((float) this.textureHeight / (float) this.atlasHeight)),
		 * new Vector2(((float) xPosition / (float) this.atlasWidth) + ((float) this.textureWidth / (float) this.atlasWidth),
		 * (float) yPosition / (float) this.atlasHeight), new Vector2((float) xPosition / (float) this.atlasWidth,
		 * (float) yPosition / (float) this.atlasHeight) };
		 */
		return textureCoordinates.get(new Integer(textureId));
	}

	/**
	 * Returns true if this texture atlas has space for another texture.
	 * 
	 * @return
	 */
	public boolean hasSpace()
	{
		return this.textures.size() < this.spaceForTextures;
	}
}
