package net.kennux.cubicworld.gui.hud;

import net.kennux.cubicworld.CubicWorld;
import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.gui.GuiHelper;
import net.kennux.cubicworld.gui.IHudElement;
import net.kennux.cubicworld.voxel.VoxelData;
import net.kennux.cubicworld.voxel.VoxelEngine;
import net.kennux.cubicworld.voxel.VoxelType;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

/**
 * <pre>
 * Block information hud element.
 * It draws the currently looking at block's fronttexture to the screen at
 * position: 25% margin left and right, 15% height 50% width
 * </pre>
 * 
 * @author KennuX
 *
 */
public class HudBlockInformation implements IHudElement
{
	/**
	 * The current block texture used in render().
	 */
	private Texture currentBlockTexture;

	/**
	 * The currently selected voxel type.
	 */
	private VoxelType currentVoxelType;

	private CubicWorldGame cubicWorld;

	public HudBlockInformation()
	{
		this.cubicWorld = CubicWorld.getClient();
	}

	@Override
	public void render(SpriteBatch spriteBatch, BitmapFont font)
	{
		if (this.currentBlockTexture != null)
		{
			// Render gui
			Vector2 textureSize = GuiHelper.getAbsoluteFromPercentagePosition(new Vector2(10, 13));
			Vector2 texturePosition = GuiHelper.getAbsoluteFromPercentagePosition(new Vector2(30, 86));
			Vector2 infoTextPosition = GuiHelper.getAbsoluteFromPercentagePosition(new Vector2(45, 95));

			spriteBatch.draw(this.currentBlockTexture, texturePosition.x, texturePosition.y, textureSize.x, textureSize.y);

			// Render text
			font.draw(spriteBatch, "Block: " + VoxelEngine.getNameByVoxelId(this.currentVoxelType.voxelId), infoTextPosition.x, infoTextPosition.y);
		}
	}

	@Override
	public void update()
	{
		// Block selected?
		if (this.cubicWorld.currentBlockHit == null)
		{
			this.currentBlockTexture = null;
			this.currentVoxelType = null;

			return;
		}

		// Get voxel type
		VoxelData selectedVoxel = this.cubicWorld.currentBlockHit.hitVoxelData;

		if (selectedVoxel == null)
		{
			this.currentBlockTexture = null;
			this.currentVoxelType = null;
		}
		else
		{
			// Get current block texture
			this.currentBlockTexture = selectedVoxel.voxelType.getGuiTexture();
			this.currentVoxelType = selectedVoxel.voxelType;
		}
	}

}
