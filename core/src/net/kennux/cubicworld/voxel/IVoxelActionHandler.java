package net.kennux.cubicworld.voxel;

import com.badlogic.gdx.math.Vector3;

/**
 * <pre>
 * This interface gets used for attaching action handlers to voxels.
 * An action gets fired by a client, it is also only clientside.
 * </pre>
 * 
 * @author KennuX
 *
 */
public interface IVoxelActionHandler
{
	/**
	 * This function gets called when a voxel action gets triggered.
	 * 
	 * @param voxelData
	 *            The voxel data object.
	 * @param voxelPosition
	 *            The voxel position vector.
	 */
	public void handleAction(VoxelData voxelData, Vector3 voxelPosition);
}
