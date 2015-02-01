package net.kennux.cubicworld.networking.packet.inventory;

import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.CubicWorldServer;
import net.kennux.cubicworld.inventory.IInventory;
import net.kennux.cubicworld.networking.APacketModel;
import net.kennux.cubicworld.networking.BitReader;
import net.kennux.cubicworld.networking.BitWriter;
import net.kennux.cubicworld.networking.CubicWorldServerClient;
import net.kennux.cubicworld.voxel.VoxelData;

/**
 * The client drop item packet will get sent if a player requests a item drop from his own inventory.
 * 
 * @author KennuX
 *
 */
public class ServerBlockInventoryUpdate extends APacketModel
{
	/**
	 * The Block inventory which gets used for serializing.
	 */
	public IInventory inventory;

	/**
	 * The voxel position of the inventory voxel.
	 */
	public int voxelPositionX;
	/**
	 * The voxel position of the inventory voxel.
	 */
	public int voxelPositionY;
	/**
	 * The voxel position of the inventory voxel.
	 */
	public int voxelPositionZ;

	private BitReader inventoryReader;

	@Override
	public int getPlayerId()
	{
		return -2; // Distance based
	}

	@Override
	public void interpretClientSide(CubicWorldGame cubicWorld)
	{
		// Update block inventory
		VoxelData inventoryVoxel = cubicWorld.voxelWorld.getVoxel(this.voxelPositionX, this.voxelPositionY, this.voxelPositionZ);

		if (inventoryVoxel != null && inventoryVoxel.blockInventory != null)
		{
			inventoryVoxel.blockInventory.deserializeInventory(this.inventoryReader);
		}
	}

	@Override
	public void interpretServerSide(CubicWorldServer server, CubicWorldServerClient client)
	{
	}

	@Override
	public void readPacket(BitReader reader)
	{
		this.voxelPositionX = reader.readInt();
		this.voxelPositionY = reader.readInt();
		this.voxelPositionZ = reader.readInt();
		this.inventoryReader = reader;
	}

	@Override
	public void writePacket(BitWriter builder)
	{
		builder.writeInt(this.voxelPositionX);
		builder.writeInt(this.voxelPositionY);
		builder.writeInt(this.voxelPositionZ);
		this.inventory.serializeInventory(builder);
	}
}
