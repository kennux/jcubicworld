package net.kennux.cubicworld.voxel;

import com.badlogic.gdx.math.Vector3;

/**
 * Gets returned by a raycast. Holds information about the hit.
 * 
 * @author KennuX
 *
 */
public class RaycastHit
{
	/**
	 * The position of the hit voxel. Global blockspace.
	 */
	public Vector3 hitVoxelPosition;
	public VoxelData hitVoxelData;

	/**
	 * Returns the face hit by the ray. face is not transformed (transformed
	 * means it does not consider rotation).
	 */
	public VoxelFace hitFace;
}
