package net.kennux.cubicworld.plugins.baseplugin.tileentities;

import net.kennux.cubicworld.CubicWorld;
import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.gui.IGuiOverlay;
import net.kennux.cubicworld.gui.overlay.OverlayData;
import net.kennux.cubicworld.inventory.IInventory;
import net.kennux.cubicworld.item.ItemStack;
import net.kennux.cubicworld.plugins.baseplugin.BasePlugin;
import net.kennux.cubicworld.voxel.VoxelData;
import net.kennux.cubicworld.voxel.handlers.AMachineTileEntityHandler;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

public class FurnaceTileEntity extends AMachineTileEntityHandler
{
	private static Model furnaceNotActiveModel;
	private static Model furnaceActiveModel;
	
	/**
	 * Constructs the models needed for the furnace tile entity.
	 */
	public FurnaceTileEntity()
	{
		// Initialize resources
		if (furnaceNotActiveModel != null)
		{
			// TODO
		}
		if (furnaceActiveModel != null)
		{
			// TODO
		}
	}
	
	@Override
	protected boolean getWorkingState(IInventory inventory)
	{
		ItemStack fuelStack = inventory.getItemStackInSlot(0);
		return fuelStack != null && fuelStack.getType().getItemId() == BasePlugin.itemCoalId;
	}

	@Override
	protected void workTick()
	{
		// System.out.println("Work tick!");
	}

	@Override
	public void handleAction(VoxelData voxelData, int x, int y, int z)
	{
		// Get cubic world game instance.
		CubicWorldGame cubicWorld = CubicWorld.getClient();

		// Init overlay data
		OverlayData overlayData = new OverlayData();
		overlayData.put("inventory", voxelData.blockInventory);
		overlayData.put("playerInventory", CubicWorld.getClient().playerController.getPlayerInventory());
		overlayData.put("voxelPos", new Vector3(x, y, z));

		// Activate overlay
		IGuiOverlay blockOverlay = cubicWorld.guiManager.getOverlayById(BasePlugin.furnaceGuiOverlayId);

		blockOverlay.setOverlayData(overlayData);

		// Now open the overlay
		cubicWorld.guiManager.openOverlay(BasePlugin.furnaceGuiOverlayId);
	}
	
	@Override
	public void handleRender(Camera camera, VoxelData voxelData, int x, int y, int z)
	{
		
	}
}
