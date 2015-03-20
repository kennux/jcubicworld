package net.kennux.cubicworld.voxel.fluid;

import net.kennux.cubicworld.voxel.VoxelType;

import com.badlogic.gdx.graphics.Color;

/**
 * Slightly extended voxel type class.
 * Adds additional components to the voxeltype class used for fluids.
 * 
 * @author KennuX
 *
 */
public class FluidType extends VoxelType
{
	/**
	 * The fluid color used for rendering.
	 */
	private Color fluidColor;

	/**
	 * @return the fluidColor
	 */
	public Color getFluidColor()
	{
		return fluidColor;
	}

	/**
	 * @param fluidColor
	 *            the fluidColor to set
	 */
	public void setFluidColor(Color fluidColor)
	{
		this.fluidColor = fluidColor;
	}
}
