package net.kennux.cubicworld.voxel;

import com.badlogic.gdx.math.Vector3;

/**
 * <pre>
 * Gets returned by the voxel world's collisionCheck() function.
 * </pre>
 * 
 * @author kennux
 *
 */
public class VoxelCollision
{
	public Vector3 originCenterPosition;
	public Vector3 colliderPosition;

	/**
	 * Is true if there was a collision on x-axis.
	 */
	public boolean collisionX;

	/**
	 * Is true if there was a collision on y-axis.
	 */
	public boolean collisionY;

	/**
	 * Is true if there was a collision on z-axis.
	 */
	public boolean collisionZ;

	/**
	 * <pre>
	 * Initializes a voxel collision object.
	 * It calculates collisionX, collisionY, collisionZ by itself.
	 * </pre>
	 * 
	 * @param origin
	 * @param collider
	 */
	public VoxelCollision(Vector3 originCenter, Vector3 collider)
	{
		Vector3 dir = new Vector3(originCenter).sub(collider).nor();

		float xDot = Vector3.dot(dir.x, dir.y, dir.z, 1, 0, 0);
		float yDot = Vector3.dot(dir.x, dir.y, dir.z, 0, 1, 0);
		float zDot = Vector3.dot(dir.x, dir.y, dir.z, 0, 0, 1);

		this.originCenterPosition = originCenter;
		this.colliderPosition = collider;

		// collision on x?
		if (xDot > yDot && xDot > zDot)
		{
			this.collisionX = true;
		}
		// collision on y?
		else if (yDot > xDot && yDot > zDot)
		{
			this.collisionY = true;
		}
		// collision on z?
		else if (zDot > yDot && zDot > xDot)
		{
			this.collisionZ = true;
		}
	}

	/**
	 * Manual setting of collision axis constructor.
	 * 
	 * @param originCenter
	 * @param collider
	 * @param collisionX
	 * @param collisionY
	 * @param collisionZ
	 */
	public VoxelCollision(Vector3 originCenter, Vector3 collider, boolean collisionX, boolean collisionY, boolean collisionZ)
	{
		this.originCenterPosition = originCenter;
		this.colliderPosition = collider;
		this.collisionX = collisionX;
		this.collisionY = collisionY;
		this.collisionZ = collisionZ;
	}
}
