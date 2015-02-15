package net.kennux.cubicworld.entity;

import net.kennux.cubicworld.math.MathUtils;
import net.kennux.cubicworld.util.Time;
import net.kennux.cubicworld.voxel.VoxelCollision;
import net.kennux.cubicworld.voxel.VoxelWorld;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

/**
 * <pre>
 * Basic character physics implementation.
 * This class is a basic entity implementation for handling very, very simple physics.
 * The entity is a box, every frame the box will get moved by velocity (which can be manipulated by move() and impulse()).
 * </pre>
 * 
 * @author kennux
 *
 */
public abstract class ACharacterEntity extends AEntity
{
	/**
	 * The cubic world instance.
	 */
	public VoxelWorld voxelWorld;

	/**
	 * The player's velocity.
	 */
	private Vector3 velocity;

	public ACharacterEntity()
	{
		this.velocity = new Vector3(Vector3.Zero);
	}

	/**
	 * You MUST overload this constructor in your own implementation!
	 * 
	 * @param voxelWorld
	 */
	public ACharacterEntity(VoxelWorld voxelWorld)
	{
		this.voxelWorld = voxelWorld;
		this.velocity = new Vector3(Vector3.Zero);
	}

	/**
	 * Returns the bounding box for the current entity's position.
	 * 
	 * @return
	 */
	public BoundingBox getBoundingBox()
	{
		return this.getBoundingBox(this.getPosition());
	}

	/**
	 * Returns the bounding box of this character entity at a given position
	 * (bottom center).
	 * 
	 * @return
	 */
	public BoundingBox getBoundingBox(Vector3 position)
	{
		// Construct bounding box newPos is the center of the collider, this constructs the minimum position vector and the maximum position vector
		Vector3 minimumPos = new Vector3(position.x - (this.getCharacterWidth() / 2.0f), position.y, position.z - (this.getCharacterDepth() / 2.0f));
		Vector3 maximumPos = new Vector3(position.x + (this.getCharacterWidth() / 2.0f), position.y + (this.getCharacterHeight()), position.z + (this.getCharacterDepth() / 2.0f));

		// Instantiate bounding box
		return new BoundingBox(minimumPos, maximumPos);
	}

	/**
	 * The depth of the player in voxels.
	 * Override this in your own implementation if you dont want your character to be 1 depth.
	 */
	protected float getCharacterDepth()
	{
		return 1;
	}

	/**
	 * The height of the player in voxels.
	 * Override this in your own implementation if you dont want your character to be 2 height.
	 */
	protected float getCharacterHeight()
	{
		return 2;
	}

	/**
	 * The width of the player in voxels.
	 * Override this in your own implementation if you dont want your character to be 1 width.
	 */
	protected float getCharacterWidth()
	{
		return 1;
	}

	/**
	 * The maximum speed per axis.
	 * Standard is 3 which means speed can be theoretically between (-3,-3,-3) and (3,3,3).
	 * 
	 * @return
	 */
	protected float getMaximumSpeed()
	{
		return 3f;
	}

	/**
	 * Adds a force impulse towards the given direction.
	 * 
	 * @param direction
	 */
	public void impulse(Vector3 direction, float strength)
	{
		Vector3 dirS = new Vector3(direction.nor());
		dirS.x *= strength;
		dirS.y *= strength;
		dirS.z *= strength;

		this.velocity.add(dirS);
	}

	/**
	 * Performs a grounding check.
	 * 
	 * @return
	 */
	public boolean isGrounded()
	{
		// Perform collision check for y - 0.15
		Vector3 nPos = new Vector3(this.getPosition());
		nPos.y -= 0.15f;
		return this.voxelWorld.intersects(this.getBoundingBox(nPos));
	}

	/**
	 * Moves to the given direction.
	 * normalizes and multiplicates the direction with the delta time
	 * 
	 * @param direction
	 */
	public void move(Vector3 direction)
	{
		direction.y = 0;
		Vector3 dirD = new Vector3(direction);
		dirD.x *= Time.getDeltaTime();
		dirD.z *= Time.getDeltaTime();

		this.velocity.add(dirD);
	}

	/**
	 * <pre>
	 * Detects collisions and handles physic movement simulation.
	 * It does the following things:
	 * 
	 * - Check if the player is grounded (if no, apply gravitation)
	 * - Calculate the new position by adding velocity to the current position
	 * - Check if there are voxels at the new position which could intersect with the player bounding box
	 * -> if yes, reset position and velocity
	 * - Calculate deceleration
	 * - Update position
	 * 
	 * </pre>
	 */
	@Override
	public void update()
	{
		// Only serverside
		if (!this.master.isServer())
			return;

		// Physics
		if (!this.isGrounded())
			this.velocity.y += Time.getDeltaTime() * -2.5f;

		// Max check
		if (this.velocity.x > 0)
			this.velocity.x = MathUtils.min(this.velocity.x, this.getMaximumSpeed());
		else if (this.velocity.x < 0)
			this.velocity.x = MathUtils.max(this.velocity.x, -this.getMaximumSpeed());

		if (this.velocity.y > 0)
			this.velocity.y = MathUtils.min(this.velocity.y, this.getMaximumSpeed());
		else if (this.velocity.y < 0)
			this.velocity.y = MathUtils.max(this.velocity.y, -this.getMaximumSpeed());

		if (this.velocity.z > 0)
			this.velocity.z = MathUtils.min(this.velocity.z, this.getMaximumSpeed());
		else if (this.velocity.z < 0)
			this.velocity.z = MathUtils.max(this.velocity.z, -this.getMaximumSpeed());

		// Move
		Vector3 newPos = new Vector3(this.getPosition()).add(this.velocity);

		// Collision check
		VoxelCollision collision = this.voxelWorld.collisionCheck(this.getBoundingBox(newPos));
		if (collision != null)
		{
			// Intersection! reset collided axis!
			if (collision.collisionX)
				this.velocity.x = 0;
			else if (collision.collisionY)
				this.velocity.y = 0;
			else if (collision.collisionZ)
				this.velocity.z = 0;

			newPos = this.getPosition();
		}

		// Deceleration
		// TODO May reimplement?
		if (this.velocity.x > 0)
		{
			this.velocity.x -= 20.0f * Time.getDeltaTime();

			if (this.velocity.x < 0)
				this.velocity.x = 0;
		}
		else if (this.velocity.x < 0)
		{
			this.velocity.x += 20.0f * Time.getDeltaTime();

			if (this.velocity.x > 0)
				this.velocity.x = 0;
		}
		if (this.velocity.y > 0)
		{
			this.velocity.y -= 9.0f * Time.getDeltaTime();

			if (this.velocity.y < 0)
				this.velocity.y = 0;
		}
		else if (this.velocity.y < 0)
		{
			this.velocity.y += 9.0f * Time.getDeltaTime();

			if (this.velocity.y > 0)
				this.velocity.y = 0;
		}
		if (this.velocity.z > 0)
		{
			this.velocity.z -= 20.0f * Time.getDeltaTime();

			if (this.velocity.z < 0)
				this.velocity.z = 0;
		}
		else if (this.velocity.z < 0)
		{
			this.velocity.z += 20.0f * Time.getDeltaTime();

			if (this.velocity.z > 0)
				this.velocity.z = 0;
		}

		this.setPosition(newPos);
	}

}
