package net.kennux.cubicworld.serialization;

public enum SerializationTypes
{
	BYTE(0), CHAR(1), SHORT(2), INTEGER(3), FLOAT(4), LONG(5), VECTOR3(6), BYTEARRAY(7), STRING(8), VOXELDATA(9), BOOLEAN(10);

	private final int value;

	private SerializationTypes(int value)
	{
		this.value = value;
	}

	public int intValue()
	{
		return value;
	}
}
