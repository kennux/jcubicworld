package net.kennux.cubicworld.serialization;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import net.kennux.cubicworld.voxel.VoxelData;

import com.badlogic.gdx.math.Vector3;

/**
 * Gets used to build datapackets.
 * 
 * @author KennuX
 *
 */
public class BitWriter
{
	/**
	 * This array list holds all bytes already added to the packet.
	 */
	private ArrayList<Byte> bytes;

	public BitWriter()
	{
		this.bytes = new ArrayList<Byte>();
	}

	public int getLength()
	{
		return this.bytes.size();
	}

	/**
	 * Builds the packet and returns a byte array ready 2 send.
	 * 
	 * @return
	 */
	public byte[] getPacket()
	{
		byte[] data = new byte[this.bytes.size()];
		int counter = 0;

		for (Byte b : this.bytes)
		{
			data[counter] = b.byteValue();
			counter++;
		}

		return data;
	}

	/**
	 * Writes a boolean (1 byte) to the packet.
	 * 
	 * @param b
	 */
	public void writeBool(boolean b)
	{
		if (b)
			this.bytes.add((byte) 1);
		else
			this.bytes.add((byte) 0);
	}

	/**
	 * Writes 1 byte to the packet.
	 * 
	 * @param b
	 */
	public void writeByte(byte b)
	{
		this.bytes.add(b);
	}

	/**
	 * Writes a 4 byte integer (data length) and then all bytes.
	 * 
	 * @param bytes
	 */
	public void writeBytes(byte[] bytes)
	{
		// Write len
		this.writeInt(bytes.length);

		// Write data
		for (int i = 0; i < bytes.length; i++)
		{
			this.writeByte(bytes[i]);
		}
	}

	/**
	 * Writes the given character to the packet data.
	 * 
	 * @param c
	 */
	public void writeChar(char c)
	{
		this.bytes.add((byte) c);
	}

	/**
	 * Writes a float (4 bytes) to the stream.
	 * 
	 * @param i
	 */
	public void writeFloat(float f)
	{
		// int intBits = Float.floatToRawIntBits(f);
		byte[] b = ByteBuffer.allocate(4).putFloat(f).array();

		this.bytes.add(b[0]);
		this.bytes.add(b[1]);
		this.bytes.add(b[2]);
		this.bytes.add(b[3]);
	}

	/**
	 * Writes a integer (4 bytes) to the stream.
	 * 
	 * @param i
	 */
	public void writeInt(int i)
	{
		byte[] b = ByteBuffer.allocate(4).putInt(i).array();
		this.bytes.add(b[0]);
		this.bytes.add(b[1]);
		this.bytes.add(b[2]);
		this.bytes.add(b[3]);
	}

	/**
	 * Writes a long (8 bytes) to the stream.
	 * 
	 * @param i
	 */
	public void writeLong(long i)
	{
		byte[] b = ByteBuffer.allocate(8).putLong(i).array();
		this.bytes.add(b[0]);
		this.bytes.add(b[1]);
		this.bytes.add(b[2]);
		this.bytes.add(b[3]);
		this.bytes.add(b[4]);
		this.bytes.add(b[5]);
		this.bytes.add(b[6]);
		this.bytes.add(b[7]);
	}

	/**
	 * Writes a short (2 bytes) to the stream.
	 * 
	 * @param s
	 */
	public void writeShort(short s)
	{
		byte[] b = ByteBuffer.allocate(2).putShort(s).array();
		this.bytes.add(b[0]);
		this.bytes.add(b[1]);
	}

	/**
	 * Writes the given string s to the packet data.
	 * Writing a string will first write an integer with the string's length
	 * followed by the characters.
	 * 
	 * @param s
	 * @param len
	 */
	public void writeString(String s)
	{
		this.writeInt(s.length());

		char[] characters = s.toCharArray();

		// Write characters
		for (int i = 0; i < characters.length; i++)
		{
			this.writeChar(characters[i]);
		}
	}

	/**
	 * Writes a vector3 (12 bytes) to the stream.
	 * 
	 * @param i
	 */
	public void writeVector3(Vector3 v)
	{
		this.writeFloat(v.x);
		this.writeFloat(v.y);
		this.writeFloat(v.z);
	}

	/**
	 * Writes a voxel data object to the packet.
	 * 
	 * @param v
	 */
	public void writeVoxelData(VoxelData v)
	{
		VoxelData.serialize(v, this);
	}

	/**
	 * Reads an object with given type serializationtypes.
	 * This method uses a switch-case to forward the calls to readInt(), readShort(), etc.
	 * Returns null in case of an error.
	 * 
	 * @param type
	 * @return
	 */
	public void writeField(SerializationTypes type, Object value)
	{
		switch (type)
		{
			case BYTE:
				this.writeByte((byte) value);
				return;
			case SHORT:
				this.writeShort((short) value);
				return;
			case CHAR:
				this.writeChar((char) value);
				return;
			case BYTEARRAY:
				this.writeBytes((byte[]) value);
				return;
			case FLOAT:
				this.writeFloat((float) value);
				return;
			case INTEGER:
				this.writeInt((int) value);
				return;
			case LONG:
				this.writeLong((long) value);
				return;
			case STRING:
				this.writeString((String) value);
				return;
			case VECTOR3:
				this.writeVector3((Vector3) value);
				return;
			case VOXELDATA:
				this.writeVoxelData((VoxelData) value);
				return;
			case BOOLEAN:
				this.writeBool((boolean) value);
				return;
		}
	}
}
