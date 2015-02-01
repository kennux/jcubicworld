package net.kennux.cubicworld.voxel;

import net.kennux.cubicworld.CubicWorldConfiguration;
import net.kennux.cubicworld.inventory.BlockInventory;
import net.kennux.cubicworld.networking.BitReader;
import net.kennux.cubicworld.networking.BitWriter;
import net.kennux.cubicworld.plugins.baseplugin.BasePlugin;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.collision.BoundingBox;

/**
 * The voxel data class which gets used for holding data which every voxel has.
 * The voxelchunk uses it to manage it's voxel data information.
 * 
 * @author KennuX
 *
 */
public class VoxelData
{
	/**
	 * Constructs a voxeldata instance for the given type id.
	 * if type id is < 0 it will return null.
	 * 
	 * @param typeId
	 * @return
	 */
	public static VoxelData construct(short typeId)
	{
		if (typeId < 0)
			return null;

		VoxelData data = new VoxelData();

		data.voxelType = VoxelEngine.getVoxelType(typeId);
		data.rotation = 0;
		data.lightLevel = CubicWorldConfiguration.baseLightLevel;

		if (data.voxelType.getInventorySize() > 0)
		{
			data.blockInventory = new BlockInventory(data.voxelType.getInventorySize());
			data.blockInventory.setFilterRuleSet(data.voxelType.getFilterRuleSet());
			data.blockInventory.addItemsToStack(1, 10, BasePlugin.itemCoalId);
		}

		return data;
	}

	public static VoxelData construct(short typeId, byte rotation)
	{
		// Is it < 0?
		if (typeId < 0)
			return null;

		VoxelData d = construct(typeId);
		d.rotation = rotation;
		return d;
	}

	/**
	 * Deserializes a voxel data object from the given reader.
	 * 
	 * @param reader
	 * @return
	 */
	public static VoxelData deserialize(BitReader reader)
	{
		short voxelId = reader.readShort();
		byte rotation = reader.readByte();
		reader.readInt();

		// Filter invalid block data
		if (voxelId == -1 || rotation == -1)
			return null;

		VoxelData v = VoxelData.construct(voxelId);
		v.rotation = rotation;

		if (v.voxelType.getInventorySize() > 0)
		{
			v.blockInventory.deserializeInventory(reader);
		}

		return v;
	}

	/**
	 * Serializes the given voxel data object to the given writer.
	 * 
	 * @return
	 */
	public static void serialize(VoxelData voxelDataObject, BitWriter writer)
	{

		if (voxelDataObject == null || voxelDataObject.voxelType == null)
		{
			writer.writeShort((short) -1);
			writer.writeByte((byte) 0);
			writer.writeInt(0);
		}
		else
		{
			writer.writeShort(voxelDataObject.voxelType.voxelId);
			writer.writeByte(voxelDataObject.rotation);
			writer.writeInt(0);

			// Write inventory
			if (voxelDataObject.voxelType.getInventorySize() > 0)
			{
				voxelDataObject.blockInventory.serializeInventory(writer);
			}
		}
	}

	/**
	 * The footstep sound used if no special one was specified.
	 * Gets loaded in the bootstrap's loadSounds().
	 */
	public static Sound standardFootstepSound;

	public VoxelType voxelType;

	public byte rotation = 0;

	public byte lightLevel;

	/**
	 * Gets created in the construct() methods.
	 */
	public BoundingBox boundingBox;

	/**
	 * <pre>
	 * Is null if this block does not have an inventory.
	 * Otherwise it contains it's block inventory instance.
	 * 
	 * When accessing the inventory you need to call the setVoxel method of the voxel world in order to keep things synchron.
	 * Example code:
	 * 
	 * int itemId = 1;
	 * VoxelData v = this.voxelWorld.getVoxel(x,y,z);
	 * v.blockInventory.setItemStackInSlot(new ItemStack(itemId, 10), 0);
	 * this.voxelWorld.setVoxel(x,y,z,v);
	 * 
	 * The call to set voxel will make the server send out the voxel update packet.
	 * </pre>
	 */
	public BlockInventory blockInventory;

	private int renderStateId = 0;

	/**
	 * @return the renderStateId
	 */
	public VoxelRenderState getRenderState()
	{
		return this.voxelType.getRenderState(this.renderStateId);
	}

	/**
	 * @param renderStateId
	 *            the renderStateId to set
	 */
	public int getRenderStateId()
	{
		return this.renderStateId;
	}

	/**
	 * <pre>
	 * Transforms the given voxel face.
	 * The face is the face detected by raycasting.
	 * If the block got rotated, the textures on the left side may not be on the left side because of the uv flip.
	 * This function transforms a worldspace facing to the uv textures facing.
	 * </pre>
	 * 
	 * @param facing
	 * @return
	 */
	public VoxelFace getTransformedFacing(VoxelFace facing)
	{
		return VoxelChunk.ROTATION_MAPPINGS[this.rotation][facing.getValue()];
	}

	/**
	 * @param renderStateId
	 *            the renderStateId to set
	 */
	public void setRenderStateId(int renderStateId)
	{
		this.renderStateId = renderStateId;
	}
}
