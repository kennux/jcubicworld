package net.kennux.cubicworld.plugins.baseplugin;

import net.kennux.cubicworld.CubicWorld;
import net.kennux.cubicworld.util.Mathf;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;

/**
 * <pre>
 * This class controls the player's movement.
 * The main idea behind it is to take movement direction vectors from the input handlers and then calculate movement in the update() function.
 * </pre>
 * 
 * @author KennuX
 *
 */
public class InputMovementController
{
	/**
	 * The current movement vector.
	 * Gets reset in update().
	 */
	private Vector3 currentMovement;

	/**
	 * <pre>
	 * The movement vector is calculated by the following:
	 * 
	 * - Get a normal vector which points towards the direction the player wants to move (X and Z axis, keyboard)
	 * - Multiplicate this vector by movementSpeed
	 * </pre>
	 */
	private final float movementSpeed = 7.5f;

	/**
	 * Initializes this input movement controller.
	 * 
	 * @param camera
	 */
	public InputMovementController()
	{
		this.currentMovement = new Vector3(0, 0, 0);
	}

	/**
	 * Adds a movement vector for the current frame to this controller.
	 * 
	 * @param dir
	 */
	public void move(Vector3 dir)
	{
		this.currentMovement = this.currentMovement.add(dir);
	}

	/**
	 * Call this every frame to flush the movement inputs set by calling move
	 * 
	 * @see InputMovementController#move(Vector3)
	 */
	public void update()
	{
		// Get the game camera
		PerspectiveCamera camera = CubicWorld.getClient().cam;

		// Move camera / player along movement vecotr
		Vector3 movement = this.currentMovement;
		Vector3 forward = camera.direction;
		Vector3 left = new Vector3(camera.up.x, camera.up.y, camera.up.z);
		left.crs(forward);

		movement.x *= 4f;
		movement.y *= 0f;
		movement.z *= 4f;

		float movementSpeed = Mathf.min(movement.len(), this.movementSpeed);
		movement.nor();
		movement.x *= movementSpeed;
		movement.z *= movementSpeed;

		CubicWorld.getClient().playerController.move(movement);

		this.currentMovement = new Vector3(0, 0, 0);
	}
}
