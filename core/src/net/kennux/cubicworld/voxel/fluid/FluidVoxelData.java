package net.kennux.cubicworld.voxel.fluid;

import net.kennux.cubicworld.voxel.VoxelData;

/**
 * Voxel data extendation for fluid voxels.
 * This for example can hold the fluid level used for rendering.
 * 
 * @author KennuX
 *
 */
public class FluidVoxelData extends VoxelData
{
	/**
	 * The fluid level in percents.
	 */
	private float fluidLevel;

	/**
	 * @return the fluidLevel
	 */
	public float getFluidLevel()
	{
		return fluidLevel;
	}

	/**
	 * @param fluidLevel
	 *            the fluidLevel to set
	 */
	public void setFluidLevel(float fluidLevel)
	{
		this.fluidLevel = fluidLevel;
	}
}
