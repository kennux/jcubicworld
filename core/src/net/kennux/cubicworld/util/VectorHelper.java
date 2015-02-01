package net.kennux.cubicworld.util;

import com.badlogic.gdx.math.Vector3;

/**
 * Contains some vector math magic.
 * 
 * @author KennuX
 *
 */
public class VectorHelper
{
	/**
	 * Calculates the euler angles from the given direction (normalized).
	 * 
	 * TODO Implement correctly
	 * 
	 * @param direction
	 * @return
	 */
	@SuppressWarnings("unused")
	public static Vector3 directionToEuler(Vector3 direction)
	{
		// Normalize direction
		Vector3 dir = new Vector3(direction).nor();
		Vector3 eulerAngles = new Vector3();

		// Calculate angles
		eulerAngles.x = Vector3.dot(direction.x, direction.y, direction.z, left.x, left.y, left.z);
		eulerAngles.y = Vector3.dot(direction.x, direction.y, direction.z, up.x, up.y, up.z);
		eulerAngles.z = Vector3.dot(direction.x, direction.y, direction.z, forward.x, forward.y, forward.z);

		return eulerAngles;
	}

	/**
	 * Calculates the distance between two vector by:
	 * 
	 * distance = (start - end).length()
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	public static float distance(Vector3 start, Vector3 end)
	{
		return new Vector3(start).sub(end).len();
	}

	/**
	 * Multiplicates vector vec with the scalar scalar.
	 * 
	 * vec.x = vec.x * scalar;
	 * vec.y = vec.y * scalar;
	 * vec.z = vec.z * scalar;
	 * 
	 * @param vec
	 * @param scalar
	 * @return
	 */
	public static Vector3 mulVectorScalar(Vector3 vec, float scalar)
	{
		return new Vector3(vec.x * scalar, vec.y * scalar, vec.z * scalar);
	}

	/**
	 * The left standard vector definition.
	 */
	public static Vector3 left = new Vector3(-1, 0, 0);
	/**
	 * The up standard vector definition.
	 */
	public static Vector3 up = new Vector3(0, 1, 0);
	/**
	 * The forward standard vector definition.
	 */
	public static Vector3 forward = new Vector3(0, 0, 1);

	/**
	 * The right standard vector definition.
	 */
	public static Vector3 right = new Vector3(1, 0, 0);

	/**
	 * The down standard vector definition.
	 */
	public static Vector3 down = new Vector3(0, -1, 0);

	/**
	 * The back standard vector definition.
	 */
	public static Vector3 back = new Vector3(0, 0, -1);
}
