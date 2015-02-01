package net.kennux.cubicworld.networking;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import net.kennux.cubicworld.voxel.VoxelData;

import com.badlogic.gdx.math.Vector3;

/**
 * Gets used to read datapackets.
 * 
 * @author KennuX
 *
 */
public class BitReader
{
	/**
	 * Contains the packet's byte.
	 */
	private byte[] data;

	/**
	 * The current data-array pointer.
	 */
	private int pointer;

	public BitReader(byte[] data)
	{
		this.data = data;
		this.pointer = 0;
	}

	/**
	 * Returns this bit reader's data array.
	 * 
	 * @return
	 */
	public byte[] getData()
	{
		return this.data;
	}

	/**
	 * Retruns true if this bit reader still has data to read.
	 * 
	 * @return
	 */
	public boolean hasDataLeft()
	{
		return this.pointer < this.data.length;
	}

	/**
	 * Reads one boolean (1 byte) from the packet data.
	 * 
	 * @return
	 */
	public boolean readBoolean()
	{
		this.pointer++;
		return this.data[this.pointer - 1] == 1 ? true : false;
	}

	/**
	 * Reads one byte from the packet data.
	 * 
	 * @return
	 */
	public byte readByte()
	{
		this.pointer++;
		return this.data[this.pointer - 1];
	}

	/**
	 * Reads bytes from the data.
	 * First reads the 4-bytes length integer and then all data.
	 * 
	 * @return
	 */
	public byte[] readBytes()
	{
		int len = this.readInt();
		byte[] data = new byte[len];

		for (int i = 0; i < len; i++)
		{
			data[i] = this.readByte();
		}

		return data;
	}

	/**
	 * Writes the given character to the packet data.
	 * 
	 * @param c
	 */
	public char readChar()
	{
		this.pointer++;
		return (char) this.data[this.pointer - 1];
	}

	/**
	 * Reads one float (4 byte) from the packet data.
	 */
	public float readFloat()
	{
		this.pointer += 4;
		byte[] data = new byte[] { this.data[this.pointer - 4], this.data[this.pointer - 3], this.data[this.pointer - 2], this.data[this.pointer - 1] };

		return ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN).getFloat();
	}

	/**
	 * Reads one integer (4 byte) from the packet data.
	 */
	public int readInt()
	{
		this.pointer += 4;
		byte[] data = new byte[] { this.data[this.pointer - 4], this.data[this.pointer - 3], this.data[this.pointer - 2], this.data[this.pointer - 1] };

		return ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN).getInt();
	}

	/**
	 * Reads one long (8 byte) from the packet data.
	 */
	public long readLong()
	{
		this.pointer += 8;
		byte[] data = new byte[] { this.data[this.pointer - 8], this.data[this.pointer - 7], this.data[this.pointer - 6], this.data[this.pointer - 5], this.data[this.pointer - 4], this.data[this.pointer - 3], this.data[this.pointer - 2], this.data[this.pointer - 1] };

		return ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN).getLong();
	}

	/**
	 * Reads one short (2 byte) from the packet data.
	 */
	public short readShort()
	{
		this.pointer += 2;
		return (short) (this.data[this.pointer - 1] & 0xFF | (this.data[this.pointer - 2] & 0xFF) << 8);
	}

	/**
	 * Reads an integer (string length) and some characters from the data.
	 * 
	 * @param len
	 */
	public String readString()
	{
		// Prepare
		int stringLen = this.readInt();
		char[] characters = new char[stringLen];

		// Read characters
		for (int i = 0; i < characters.length; i++)
		{
			characters[i] = this.readChar();
		}

		return new String(characters);
	}

	/**
	 * Reads a vector3 (12 bytes) from the packet data.
	 * 
	 * @return
	 */
	public Vector3 readVector3()
	{
		return new Vector3(this.readFloat(), this.readFloat(), this.readFloat());
	}

	/**
	 * Reads a voxel data object from the packet data.
	 * 
	 * @param v
	 */
	public VoxelData readVoxelData()
	{
		return VoxelData.deserialize(this);
	}
}
