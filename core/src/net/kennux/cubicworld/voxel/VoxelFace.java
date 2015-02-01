package net.kennux.cubicworld.voxel;

public enum VoxelFace
{
	LEFT(0), RIGHT(1), TOP(2), BOTTOM(3), FRONT(4), BACK(5);

	private final int value;

	private VoxelFace(int value)
	{
		this.value = value;
	}

	public int getValue()
	{
		return value;
	}
}
