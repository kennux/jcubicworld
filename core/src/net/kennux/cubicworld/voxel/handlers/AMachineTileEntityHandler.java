package net.kennux.cubicworld.voxel.handlers;

import com.badlogic.gdx.graphics.Camera;

import net.kennux.cubicworld.CubicWorld;
import net.kennux.cubicworld.inventory.IInventory;
import net.kennux.cubicworld.serialization.BitReader;
import net.kennux.cubicworld.serialization.BitWriter;
import net.kennux.cubicworld.voxel.VoxelData;

/**
 * <pre>
 * Abstract machine update handler.
 * You can use this to implement machines in a simple way.
 * 
 * This update handler has 2 states, working and not working.
 * Based on the state the following render states are set:
 * 
 * 0 - Not working
 * 1 - Working
 * 
 * This handler will check every tick if the machine can start working.
 * 
 * IMPORTANT: This class requires the voxel data to has a inventory attached.
 * </pre>
 * 
 * @author KennuX
 *
 */
public abstract class AMachineTileEntityHandler implements IVoxelTileEntityHandler
{
	/**
	 * Return true in here if the machine's conditions for starting working are met.
	 * 
	 * @return
	 */
	protected abstract boolean getWorkingState(IInventory inventory);
	
	/**
	 * Is true if the machine is currently in working state.
	 */
	private boolean isWorking = false;
	
	public void handleUpdate(VoxelData voxelData, int x, int y, int z, boolean isServer)
	{
		// Only blocks with inventories are allowed!
		if (voxelData.blockInventory == null)
			return;

		// Get the current target working state
		boolean workingState = this.getWorkingState(voxelData.blockInventory);

		if (isServer)
		{
			if (workingState)
				this.workTick();
		}
		else
		{
			// Client render things
			if (!isWorking && workingState)
			{
				// Change to working
				//voxelData.setRenderStateId(1);
				// TODO
				this.isWorking = true;

				// Set to voxel world
				CubicWorld.getClient().voxelWorld.setVoxel(x, y, z, voxelData);
			}
			else if (isWorking && !workingState)
			{
				// Change to not working
				//voxelData.setRenderStateId(0);
				// TODO
				this.isWorking = false;

				// Set to voxel world
				CubicWorld.getClient().voxelWorld.setVoxel(x, y, z, voxelData);
			}
		}
	}

	/**
	 * Gets called every tick the machine is working.
	 */
	protected abstract void workTick();

	public void serialize(BitWriter writer)
	{

	}

	public void deserialize(BitReader reader)
	{

	}
}
