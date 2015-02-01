package net.kennux.cubicworld.gui.hud;

import net.kennux.cubicworld.gui.IHudElement;
import net.kennux.cubicworld.voxel.VoxelData;
import net.kennux.cubicworld.voxel.VoxelEngine;
import net.kennux.cubicworld.voxel.VoxelType;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * <pre>
 * Simple block selector gui.
 * Draws the currently selected texture in the left bottom screen corner.
 * Very simple hud element mainly implemented to test the HUD GUI-System.
 * </pre>
 * 
 * @author KennuX
 *
 */
public class BlockSelectorGui implements IHudElement
{
	/**
	 * All front textures of the voxel types. indexed by their id.
	 */
	private Texture[] textures;

	/**
	 * All voxel types.
	 */
	private VoxelType[] types;

	/**
	 * The currently selected voxel type.
	 * 
	 */
	public int currentSelected = 0;

	/**
	 * Singleton pattern. gets set in the contructor.
	 */
	public static BlockSelectorGui instance;

	public BlockSelectorGui()
	{
		// Singleton
		instance = this;

		// Get voxel types
		this.types = VoxelEngine.getVoxelTypes();
		this.textures = new Texture[types.length];

		for (int i = 0; i < types.length; i++)
		{
			this.textures[i] = types[i].getGuiTexture();
		}
	}

	/**
	 * Constructs a new voxel data based on the currently selected block.
	 * Returns null in case of an error.
	 * 
	 * @return
	 */
	public VoxelData constructNewCurrentSelected()
	{
		if (this.currentSelected >= 0 && this.currentSelected < this.textures.length)
			return VoxelData.construct(this.types[this.currentSelected].voxelId);
		else
			return null;
	}

	/**
	 * Renders the top texture of the block the player is currently looking at.
	 * Where currentSelected is the voxel type id of the voxel where the player
	 * is currently looking at.
	 */
	@Override
	public void render(SpriteBatch spriteBatch, BitmapFont font)
	{
		if (this.currentSelected >= 0 && this.currentSelected < this.textures.length)
		{
			spriteBatch.draw(this.textures[this.currentSelected], Gdx.graphics.getWidth() - this.textures[this.currentSelected].getWidth(), 0);
		}
	}

	/**
	 * This wont do anything, this hud element does not require update().
	 */
	@Override
	public void update()
	{ /* Nothing to do here */
	}
}
