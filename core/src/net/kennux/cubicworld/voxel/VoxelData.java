package net.kennux.cubicworld.voxel;

import net.kennux.cubicworld.CubicWorldConfiguration;
import net.kennux.cubicworld.inventory.BlockInventory;
import net.kennux.cubicworld.math.MathUtils;
import net.kennux.cubicworld.plugins.baseplugin.BasePlugin;
import net.kennux.cubicworld.serialization.BitReader;
import net.kennux.cubicworld.serialization.BitWriter;
import net.kennux.cubicworld.voxel.handlers.IVoxelTileEntityHandler;

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

		if (data.voxelType.getInventorySize() > 0)
		{
			data.blockInventory = new BlockInventory(data.voxelType.getInventorySize());
			data.blockInventory.setFilterRuleSet(data.voxelType.getFilterRuleSet());
			data.blockInventory.addItemsToStack(1, 10, BasePlugin.itemCoalId);
		}

		// Tile entity instantiation
		if (data.voxelType.isTileEntity())
		{
			data.tileEntity = data.voxelType.getTileEntityHandlerFactory().newInstance();
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

		// Filter invalid block data
		if (voxelId == -1 || rotation == -1)
			return new VoxelData();

		VoxelData v = VoxelData.construct(voxelId);
		v.rotation = rotation;

		if (v.voxelType.getInventorySize() > 0)
		{
			v.blockInventory.deserializeInventory(reader);
		}

		if (v.tileEntity != null)
		{
			v.tileEntity.deserialize(reader);
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
		}
		else
		{
			writer.writeShort(voxelDataObject.voxelType.voxelId);
			writer.writeByte(voxelDataObject.rotation);

			// Write inventory
			if (voxelDataObject.voxelType.getInventorySize() > 0)
			{
				voxelDataObject.blockInventory.serializeInventory(writer);
			}

			// Write data model
			if (voxelDataObject.tileEntity != null)
			{
				voxelDataObject.tileEntity.serialize(writer);
			}
		}
	}

	/**
	 * The footstep sound used if no special one was specified.
	 * Gets loaded in the bootstrap's loadSounds().
	 */
	public static Sound standardFootstepSound;

	/**
	 * The voxel type of this voxel data.
	 */
	public VoxelType voxelType;

	/**
	 * The rotation of this voxel data.
	 */
	public byte rotation = 0;

	/**
	 * This voxeldata's sun light level.
	 * This light level will get set in the local lighting pass.
	 * -1 means unitinitialized.
	 */
	private byte sunLightLevel = -1;

	/**
	 * This voxeldata's block light level.
	 * -1 means unitinitialized.
	 */
	private byte blockLightLevel = -1;

	/**
	 * The voxel data model.
	 */
	public IVoxelTileEntityHandler tileEntity;

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
	 * @return the lightLevel
	 */
	public byte getBlockLightLevel()
	{
		return this.blockLightLevel;
	}
	
	/**
	 * @return the lightLevel
	 */
	public byte getSunLightLevel()
	{
		return this.sunLightLevel;
	}

	/**
	 * @param lightLevel the lightLevel to set
	 */
	public void setSunLightLevel(byte lightLevel)
	{
		this.sunLightLevel = lightLevel;
	}
	
	/**
	 * @param lightLevel the lightLevel to set
	 */
	public void setSunLightLevel(int lightLevel)
	{
		this.setSunLightLevel((byte) lightLevel);
	}

	/**
	 * @param lightLevel the lightLevel to set
	 */
	public void setBlockLightLevel(byte shadowLevel)
	{
		this.blockLightLevel = shadowLevel;
	}
	
	/**
	 * @param lightLevel the lightLevel to set
	 */
	public void setBlockLightLevel(int shadowLevel)
	{
		this.setBlockLightLevel((byte) shadowLevel);
	}
	
	/**
	 * Returns the lightlevel, composed of sunlight and shadow level.
	 * @return
	 */
	public byte getLightLevel()
	{
		byte highestLightLevel = this.blockLightLevel > this.sunLightLevel ? this.blockLightLevel : this.sunLightLevel;
		return MathUtils.clamp(highestLightLevel, (byte)CubicWorldConfiguration.maxLightLevel, (byte)0);
	}
}
