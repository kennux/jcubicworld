package net.kennux.cubicworld.math;

import com.badlogic.gdx.math.Vector3;

/**
 * Integer 3-component vector implementation.
 * Used instead of libgdx's vector3 if only integers are needed.
 * 
 * @author KennuX
 *
 */
public class Vector3i
{
	public int x;
	public int y;
	public int z;

	public Vector3i(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector3i(Vector3 floatVector)
	{
		this.x = (int) floatVector.x;
		this.y = (int) floatVector.y;
		this.z = (int) floatVector.z;
	}

	public Vector3 toFloatVector()
	{
		return new Vector3(this.x, this.y, this.z);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof Vector3i)
		{
			Vector3i cObj = (Vector3i) obj;
			return cObj.x == this.x && cObj.y == this.y && cObj.z == this.z;
		}

		return false;
	}

	public int hashCode()
	{
		return (this.x ^ this.y ^ this.z);
	}
}
