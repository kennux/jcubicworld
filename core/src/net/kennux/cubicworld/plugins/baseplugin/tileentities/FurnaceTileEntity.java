package net.kennux.cubicworld.plugins.baseplugin.tileentities;

import net.kennux.cubicworld.CubicWorld;
import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.gui.IGuiOverlay;
import net.kennux.cubicworld.gui.overlay.OverlayData;
import net.kennux.cubicworld.inventory.IInventory;
import net.kennux.cubicworld.item.ItemStack;
import net.kennux.cubicworld.plugins.baseplugin.BasePlugin;
import net.kennux.cubicworld.util.MeshUtil;
import net.kennux.cubicworld.voxel.VoxelData;
import net.kennux.cubicworld.voxel.handlers.AMachineTileEntityHandler;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

public class FurnaceTileEntity extends AMachineTileEntityHandler
{
	private static Mesh furnaceNotActiveMesh;
	private static Mesh furnaceActiveMesh;
	
	/**
	 * Constructs the models needed for the furnace tile entity.
	 */
	public FurnaceTileEntity()
	{
	}

	@Override
	protected Mesh getWorkingMesh()
	{
		if (furnaceNotActiveMesh == null)
		{
			furnaceNotActiveMesh = MeshUtil.buildBlockMesh(BasePlugin.furnaceTopId, BasePlugin.furnaceTopId, BasePlugin.furnaceSideId, BasePlugin.furnaceSideId, BasePlugin.furnaceFrontId, BasePlugin.furnaceSideId);
		}
		
		return furnaceNotActiveMesh;
	}

	@Override
	protected Mesh getNotWorkingMesh()
	{
		if (furnaceActiveMesh == null)
		{
			furnaceActiveMesh = MeshUtil.buildBlockMesh(BasePlugin.furnaceTopId, BasePlugin.furnaceTopId, BasePlugin.furnaceSideId, BasePlugin.furnaceSideId, BasePlugin.furnaceFrontLitId, BasePlugin.furnaceSideId);
		}
		
		return furnaceActiveMesh;
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
}
