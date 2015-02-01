package net.kennux.cubicworld.voxel;

import java.util.HashMap;
import java.util.Map.Entry;

import net.kennux.cubicworld.inventory.InventoryFilterRuleSet;
import net.kennux.cubicworld.voxel.handlers.IVoxelActionHandler;
import net.kennux.cubicworld.voxel.handlers.IVoxelUpdateHandler;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;

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
	 * Gets set by setModel().
	 * If this is null, normal textured block rendering gets used.
	 */
	private Model model;

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
	 * The action handler of this voxel type.
	 */
	private IVoxelActionHandler actionHandler;

	/**
	 * The voxel type's update handler.
	 */
	private IVoxelUpdateHandler updateHandler;

	/**
	 * The filter rule set instance for this voxel type.
	 */
	private InventoryFilterRuleSet filterRuleSet;

	/**
	 * The rendering definitions.
	 * Key = state id
	 * Value = rendering definition
	 */
	private HashMap<Integer, VoxelRenderState> renderStates = new HashMap<Integer, VoxelRenderState>();

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
	 * @return the actionHandler
	 */
	public IVoxelActionHandler getActionHandler()
	{
		return actionHandler;
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

	/**
	 * Returns the model used by this voxel type.
	 * Before calling this you should check if this voxel type is model rendered.
	 * You can use isModelRendering() to check this.
	 * 
	 * This may returns null if the model is not set.
	 * 
	 * @return
	 */
	public Model getModel()
	{
		return this.model;
	}

	/**
	 * Gets the renderstate for the given state id.
	 * 
	 * @param stateId
	 * @return
	 */
	public VoxelRenderState getRenderState(int stateId)
	{
		return this.renderStates.get(new Integer(stateId));
	}

	/**
	 * @return the updateHandler
	 */
	public IVoxelUpdateHandler getUpdateHandler()
	{
		return updateHandler;
	}

	/**
	 * Initializes the UV-Coordinates for this voxel type. Gets called by the
	 * voxelengine after the bootstrap was successfully executed.
	 */
	public void initializeUvs()
	{
		for (Entry<Integer, VoxelRenderState> entry : this.renderStates.entrySet())
		{
			entry.getValue().initializeUvs();
		}
	}

	/**
	 * @return the isLightSource
	 */
	public boolean isLightSource()
	{
		return isLightSource;
	}

	/**
	 * Returns true if a model was set to this voxel type with setModel().
	 * 
	 * @return
	 */
	public boolean isModelRendering()
	{
		return this.model != null;
	}

	/**
	 * @param actionHandler
	 *            the actionHandler to set
	 */
	public VoxelType setActionHandler(IVoxelActionHandler actionHandler)
	{
		this.actionHandler = actionHandler;
		return this;
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
	 * If you want to use a model as a voxel type, you can set the model in this method.
	 * If the model is set, no block will get rendered for this voxel type. Instead the model will get drawn in the voxel's <strong>CENTER</strong> position.
	 * 
	 * @return
	 */
	public VoxelType setModel(Model m)
	{
		this.model = m;
		return this;
	}

	/**
	 * Sets the renderstate for the given state id.
	 * 
	 * @param face
	 * @param textureId
	 */
	public VoxelType setRenderState(int stateId, VoxelRenderState renderState)
	{
		this.renderStates.put(new Integer(stateId), renderState);
		return this;
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
	public VoxelType setUpdateHandler(IVoxelUpdateHandler updateHandler)
	{
		this.updateHandler = updateHandler;
		return this;
	}
}
