package net.kennux.cubicworld.voxel.handlers;

import net.kennux.cubicworld.voxel.VoxelData;
import net.kennux.cubicworld.voxel.datamodels.IVoxelDataModel;

import com.badlogic.gdx.math.Vector3;

/**
 * <pre>
 * This interface gets used for attaching action handlers to voxels.
 * An action gets fired by a client, it is also only clientside.
 * 
 * Use the generic type parameter to define the class of the data model.
 * If there is no data model attached to your voxel type, just use the non-generic version of this interface.
 * Null will then be passed in as data model.
 * </pre>
 * 
 * @author KennuX
 *
 */
public interface IVoxelActionHandler<T extends IVoxelDataModel>
{
	/**
	 * This function gets called when a voxel action gets triggered.
	 * 
	 * @param voxelData
	 *            The voxel data object.
	 * @param voxelPosition
	 *            The voxel position vector.
	 */
	public void handleAction(VoxelData voxelData, Vector3 voxelPosition, T dataModel);
}
