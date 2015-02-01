package net.kennux.cubicworld.gui.skin;

import java.util.HashMap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;

/**
 * <pre>
 * GUI Skin implementation.
 * Customizable skinning system.
 * 
 * Mainly this is just a dataholder class which holds the skin confiugration
 * used by the gui renderer.
 * 
 * Skin texture naming convention:
 * [ControlElementName]_[Location][_Optional]
 * 
 * Examples:
 * button_lefttop
 * button_rightbottom
 * button_centertop
 * 
 * IMPORTANT: You can add a texture named "NOTFOUND", which will get used if any texture of any element is missing.
 * 
 * After instantiating a gui skin AGuiSkin.current will get set to that skin.
 * 
 * </pre>
 * 
 * @author KennuX
 *
 */
public abstract class AGuiSkin
{
	/**
	 * The current gui skin.
	 * Gets set in the constructor of the skin.
	 */
	public static AGuiSkin current;

	/**
	 * Maps textures names to integers returned by texture atlas texture
	 * insertion.
	 */
	private HashMap<String, Texture> textures;

	/**
	 * The skin's primary color.
	 * You can change this in the bootstrap().
	 */
	protected Color primaryColor;

	/**
	 * The skin's font color.
	 * You can change this in the bootstrap().
	 */
	protected Color fontColor;

	/**
	 * The skin's secondary color.
	 * You can change this in the bootstrap().
	 */
	protected Color secondaryColor;

	/**
	 * You must overload this constructor in your own implementation.
	 * Otherwise the skin will not get initialized correctly.
	 * 
	 * This will set the AGuiSkin.current variable, after everything is initialized.
	 */
	public AGuiSkin()
	{
		// Init
		this.textures = new HashMap<String, Texture>();
		this.bootstrap();

		// Set static current skin variable
		AGuiSkin.current = this;
	}

	/**
	 * Adds a texture with the given name to the texture atlas.
	 * If a texture with the given name already exists it will get overwritten.
	 * 
	 * @param textureName
	 * @param texture
	 */
	protected void addTexture(String textureName, Texture texture)
	{
		texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		this.textures.put(textureName, texture);
	}

	/**
	 * <pre>
	 * Add all needed textures to the texture atlas in here and set all colors of this gui skin.
	 * You must use this class's addTexture() function.
	 */
	public abstract void bootstrap();

	public Color getFontColor()
	{
		return new Color(fontColor);
	}

	// Getter functions.
	public Color getPrimaryColor()
	{
		return new Color(primaryColor);
	}

	public Color getSecondaryColor()
	{
		return new Color(secondaryColor);
	}

	/**
	 * Gets a texture from the current textures map.
	 * Returns null if the given texture name does not exist.
	 * 
	 * @param textureName
	 * @return
	 */
	public Texture getTexture(String textureName)
	{
		if (this.textures.containsKey(textureName))
			return this.textures.get(textureName);
		else
			return null;
	}
}
