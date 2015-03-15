package net.kennux.cubicworld.voxel;

import java.util.HashMap;
import java.util.Map.Entry;

import net.kennux.cubicworld.inventory.InventoryFilterRuleSet;
import net.kennux.cubicworld.voxel.handlers.ITileEntityHandlerFactory;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Vector2;

/**
 * <pre>
 * Voxel type class used by the voxel engine to define different voxel types.
 * 
 * Voxel types can be light sources, in the voxel engine light sources for example do this:
 * The light source emitts with 3 blocks radius and an emitting light level of 10.
 * Now, the block where the light source is set in the world will have the light level 10.
 * The light level will linearly fall off the farer a block is away (if it is in range).
 * </pre>
 * 
 * @author KennuX
 *
 */
public class VoxelType
{
	public short voxelId;

	/**
	 * The name used for voxelid -> name and name -> voxelid mapping.
	 */
	public String voxelName;

	private Sound footstepSound;

	/**
	 * Controls alpha testing.
	 */
	public boolean transparent;

	private boolean canCollide = true;

	/**
	 * -1 means this block has no inventory.
	 * Otherwise, set the inventory slot size.
	 */
	private int inventorySize = -1;

	/**
	 * The texture which gets display for example in the block info hud element.
	 * If you got a normal voxel, just use any of it's side textures.
	 */
	private Texture guiTexture;

	/**
	 * Gets initialized in the setLightSource() function.
	 */
	private boolean isLightSource = false;

	/**
	 * The radius of this light source.
	 */
	private int lightRadius = 0;

	/**
	 * Gets initialized in the setLightSource() function.
	 * Contains the light level of this voxel type's light source (if there is one, otherwise this is 0).
	 */
	private int lightEmittingLevel = 0;

	/**
	 * The voxel type's update handler.
	 */
	private ITileEntityHandlerFactory tileEntityHandlerFactory;

	/**
	 * The filter rule set instance for this voxel type.
	 */
	private InventoryFilterRuleSet filterRuleSet;

	public int topTexture, bottomTexture, leftTexture, rightTexture, frontTexture, backTexture;
	public Vector2[] topUv, bottomUv, leftUv, rightUv, frontUv, backUv;

	public VoxelType setTextures(int topTexture, int bottomTexture, int leftTexture, int rightTexture, int frontTexture, int backTexture)
	{
		this.topTexture = topTexture;
		this.bottomTexture = bottomTexture;
		this.leftTexture = leftTexture;
		this.rightTexture = rightTexture;
		this.frontTexture = frontTexture;
		this.backTexture = backTexture;

		return this;
	}

	/**
	 * Gets uvs for the given faces.
	 * 
	 * @param face
	 * @return
	 */
	public Vector2[] getUvsForFace(VoxelFace face)
	{
		switch (face)
		{
			case FRONT:
				return this.frontUv;

			case BACK:
				return this.backUv;

			case LEFT:
				return this.leftUv;

			case RIGHT:
				return this.rightUv;

			case TOP:
				return this.topUv;

			case BOTTOM:
				return this.bottomUv;
		}

		return null;
	}

	/**
	 * Initializes the UV-Coordinates for this voxel type. Gets called by the
	 * voxelengine after the bootstrap was successfully executed.
	 */
	public void initializeUvs()
	{
		this.topUv = VoxelEngine.getUvForTexture(topTexture);
		this.bottomUv = VoxelEngine.getUvForTexture(bottomTexture);
		this.leftUv = VoxelEngine.getUvForTexture(leftTexture);
		this.rightUv = VoxelEngine.getUvForTexture(rightTexture);
		this.frontUv = VoxelEngine.getUvForTexture(frontTexture);
		this.backUv = VoxelEngine.getUvForTexture(backTexture);
	}

	/**
	 * Sets the texture for a given face.
	 * 
	 * @param face
	 * @param textureId
	 */
	public void setTexture(VoxelFace face, int textureId)
	{
		switch (face)
		{
			case FRONT:
				this.frontTexture = textureId;
				break;

			case BACK:
				this.backTexture = textureId;
				break;

			case LEFT:
				this.leftTexture = textureId;
				break;

			case RIGHT:
				this.rightTexture = textureId;
				break;

			case TOP:
				this.topTexture = textureId;
				break;

			case BOTTOM:
				this.bottomTexture = textureId;
				break;
		}
	}

	/**
	 * Returns true if this voxel type is able to physically collide with other objects.
	 * 
	 * @return
	 */
	public boolean canCollide()
	{
		return canCollide;
	}

	/**
	 * @return the filterRuleSet
	 */
	public InventoryFilterRuleSet getFilterRuleSet()
	{
		return filterRuleSet;
	}

	/**
	 * @return the footstepSound, if not set it will return the standard footstep sound.
	 */
	public Sound getFootstepSound()
	{
		if (footstepSound != null)
			return footstepSound;
		else
			return VoxelData.standardFootstepSound;
	}

	/**
	 * @return the guiTexture
	 */
	public Texture getGuiTexture()
	{
		return guiTexture;
	}

	/**
	 * @return the inventorySize
	 */
	public int getInventorySize()
	{
		return inventorySize;
	}

	/**
	 * @return the lightEmittingLevel
	 */
	public int getLightEmittingLevel()
	{
		return lightEmittingLevel;
	}

	/**
	 * @return the lightRadius
	 */
	public int getLightRadius()
	{
		return lightRadius;
	}

	public boolean isTileEntity()
	{
		return this.tileEntityHandlerFactory != null;
	}

	/**
	 * @return the updateHandler
	 */
	public ITileEntityHandlerFactory getTileEntityHandlerFactory()
	{
		return tileEntityHandlerFactory;
	}

	/**
	 * @return the isLightSource
	 */
	public boolean isLightSource()
	{
		return isLightSource;
	}

	/**
	 * Sets whether this voxel can collide with entities and other physics stuff or not.
	 * 
	 * @param canCollide
	 * @return
	 */
	public VoxelType setCanCollide(boolean canCollide)
	{
		this.canCollide = canCollide;
		return this;
	}

	/**
	 * @param filterRuleSet
	 *            the filterRuleSet to set
	 */
	public VoxelType setFilterRuleSet(InventoryFilterRuleSet filterRuleSet)
	{
		this.filterRuleSet = filterRuleSet;
		return this;
	}

	/**
	 * @param footstepSound
	 *            the footstepSound to set
	 */
	public VoxelType setFootstepSound(Sound footstepSound)
	{
		this.footstepSound = footstepSound;
		return this;
	}

	/**
	 * @param guiTexture
	 *            the guiTexture to set
	 */
	public VoxelType setGuiTexture(Texture guiTexture)
	{
		this.guiTexture = guiTexture;
		return this;
	}

	/**
	 * @param inventorySize
	 *            the inventorySize to set
	 */
	public VoxelType setInventorySize(int inventorySize)
	{
		this.inventorySize = inventorySize;
		return this;
	}

	/**
	 * Initializes this voxel type as a light source.
	 * 
	 * @param lightRadius
	 *            The light radius in blockspace distance (integer).
	 * @param emittingLevel
	 *            The strength of the light emitted by the light source.
	 */
	public void setLightSource(int lightRadius, int emittingLevel)
	{
		this.isLightSource = true;
		this.lightRadius = lightRadius;
		this.lightEmittingLevel = emittingLevel;
	}

	/**
	 * Sets the transparent boolean.
	 * 
	 * @param transparent
	 */
	public VoxelType setTransparent(boolean transparent)
	{
		this.transparent = transparent;
		return this;
	}

	/**
	 * @param updateHandler
	 *            the updateHandler to set
	 */
	public VoxelType setTileEntityHandlerFactory(ITileEntityHandlerFactory tileEntityHandlerFactory)
	{
		this.tileEntityHandlerFactory = tileEntityHandlerFactory;
		return this;
	}
}
