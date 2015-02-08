package net.kennux.cubicworld.test.serializer;

import net.kennux.cubicworld.serialization.BitReader;
import net.kennux.cubicworld.serialization.BitWriter;
import net.kennux.cubicworld.serialization.SerializationTypes;
import net.kennux.cubicworld.serialization.annotations.SerializerField;
import net.kennux.cubicworld.voxel.VoxelData;

import com.badlogic.gdx.math.Vector3;

public class SerializerTestClass
{
	public SerializerTestClass()
	{

	}

	@SerializerField(order = 0, type = SerializationTypes.BOOLEAN)
	public boolean bool;

	@SerializerField(order = 1, type = SerializationTypes.BYTE)
	public byte b;

	@SerializerField(order = 2, type = SerializationTypes.SHORT)
	public short s;

	@SerializerField(order = 3, type = SerializationTypes.INTEGER)
	public int i;

	@SerializerField(order = 4, type = SerializationTypes.LONG)
	public long l;

	@SerializerField(order = 5, type = SerializationTypes.STRING)
	public String str;

	@SerializerField(order = 6, type = SerializationTypes.VOXELDATA)
	public VoxelData voxelData;

	@SerializerField(order = 7, type = SerializationTypes.BYTEARRAY)
	public byte[] bA;

	@SerializerField(order = 8, type = SerializationTypes.FLOAT)
	public float f;

	@SerializerField(order = 9, type = SerializationTypes.CHAR)
	public char c;

	@SerializerField(order = 10, type = SerializationTypes.VECTOR3)
	public Vector3 v3;

	public void serialize(BitWriter writer)
	{
		writer.writeBool(this.bool);
		writer.writeByte(this.b);
		writer.writeShort(this.s);
		writer.writeInt(this.i);
		writer.writeLong(this.l);
		writer.writeString(this.str);
		writer.writeVoxelData(this.voxelData);
		writer.writeBytes(this.bA);
		writer.writeFloat(this.f);
		writer.writeChar(this.c);
		writer.writeVector3(this.v3);
	}

	public static SerializerTestClass deserialize(BitReader reader)
	{
		SerializerTestClass testClass = new SerializerTestClass();

		testClass.bool = reader.readBoolean();
		testClass.b = reader.readByte();
		testClass.s = reader.readShort();
		testClass.i = reader.readInt();
		testClass.l = reader.readLong();
		testClass.str = reader.readString();
		testClass.voxelData = reader.readVoxelData();
		testClass.bA = reader.readBytes();
		testClass.f = reader.readFloat();
		testClass.c = reader.readChar();
		testClass.v3 = reader.readVector3();

		return testClass;
	}
}
