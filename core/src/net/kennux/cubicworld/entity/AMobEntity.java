package net.kennux.cubicworld.entity;

import java.util.Random;

import net.kennux.cubicworld.CubicWorld;
import net.kennux.cubicworld.math.MathUtils;
import net.kennux.cubicworld.pathfinder.Path;
import net.kennux.cubicworld.voxel.VoxelWorld;

import com.badlogic.gdx.math.Vector3;

/**
 * <pre>
 * Abstract mob entity class.
 * A mob entity strictly follows one target entity or moves randomly around the world.
 * 
 * Overload the update() method and perform your target recognition in it.
 * </pre>
 * 
 * @author kennux
 *
 */
public abstract class AMobEntity extends ACharacterEntity
{
	/**
	 * The target entity.
	 * Only set this using the getter and setter methods.
	 */
	private AEntity targetEntity;

	/**
	 * The path the entity is currently walking along.
	 */
	private Path currentPath;

	/**
	 * The path which is getting processed right now.
	 */
	private Path nextPath;

	/**
	 * The current step data of the path.
	 * Gets only set if you use setPath() for setting a new or changed path.
	 */
	private Vector3[] currentSteps;

	/**
	 * The current random movement vector.
	 */
	private Vector3 randomMovementVector;

	private Object pathLockObject = new Object();

	private int currentStepIndex = 0;

	/**
	 * The current time millis when the last path claculation was.
	 */
	private long lastPathFind = 0;

	public AMobEntity()
	{

	}

	public AMobEntity(VoxelWorld voxelWorld)
	{
		super(voxelWorld);
	}

	/**
	 * Returns the entity's jump strength. Standard is 1 blocks per second.
	 * 
	 * @return
	 */
	public float getEntityJumpStrength()
	{
		return 1;
	}

	/**
	 * Returns your entity movement speed in here if you don't want the standard of 5 blocks per second.
	 * 
	 * @return
	 */
	public float getEntitySpeed()
	{
		return 5;
	}

	@Override
	public void init()
	{
	}

	/**
	 * Returns true if the last path recalculation was more than 2 seconds ago.
	 * 
	 * @return
	 */
	private boolean isTimeToGetNewPath()
	{
		return (System.currentTimeMillis() - this.lastPathFind) > 2000;
	}

	/**
	 * Sets the new current path.
	 * ALWAYS use this method, because it also creates the currentSteps array and resets the stepcounter.
	 * 
	 * @param p
	 */
	protected void setPath(Path p)
	{
		synchronized (this.pathLockObject)
		{
			this.currentPath = p;
			this.currentSteps = p.getStepData();
			this.currentStepIndex = 0;
		}
	}

	/**
	 * Sets the target entity for this mob.
	 */
	public void setTargetEntity(AEntity target)
	{
		synchronized (this.pathLockObject)
		{
			// Set entity
			this.targetEntity = target;

			// Re-calculate path
			if (!this.voxelWorld.getVoxelspacePosition(this.getPosition()).equals(this.voxelWorld.getVoxelspacePosition(target.getPosition())))
				this.nextPath = this.voxelWorld.findPath(new Vector3(this.getPosition()), new Vector3(target.getPosition()), true, MathUtils.ceilToInt(this.getCharacterHeight()));

			this.lastPathFind = System.currentTimeMillis();
		}
	}

	/**
	 * Checks if this entity should run around randomly or follow a path.
	 */
	public void update()
	{
		if (this.master.isServer())
		{
			synchronized (this.pathLockObject)
			{
				// New path ready?
				if (this.nextPath != null && this.nextPath.isProcessed())
				{
					// New path found?
					if (this.nextPath.isFound())
					{
						// Set new path
						this.setPath(this.nextPath);
						this.nextPath = null;
					}
					else
					{
						// Retry path calculation
						this.setTargetEntity(this.targetEntity);
					}
				}

				// Got entity not in my position and no path?
				if (this.targetEntity != null && this.currentPath == null && this.isTimeToGetNewPath() && this.nextPath == null && new Vector3(this.targetEntity.getPosition()).sub(this.getPosition()).len() > 1.0f)
					this.setTargetEntity(this.targetEntity);

				// Got path and target is too far away or invalid?
				if ((this.currentPath != null && this.targetEntity != null && this.currentPath.isFound() && this.isTimeToGetNewPath() && new Vector3(this.currentPath.getEndPosition()).sub(this.targetEntity.getPosition()).len() > 1.0f))
				{
					// Recalc
					this.setTargetEntity(this.targetEntity);
				}

				Vector3 movement = new Vector3();

				// Move towards path or randomly?
				if (this.targetEntity != null && this.currentPath != null && this.currentSteps != null)
				{
					// Get current step and blockspace position
					Vector3 blockspacePosition = this.voxelWorld.getVoxelspacePosition(this.getPosition());
					Vector3 currentStepPosition = this.currentSteps[this.currentStepIndex];

					if (blockspacePosition.equals(currentStepPosition))
					{
						// Get next step
						this.currentStepIndex++;

						if (this.currentStepIndex == this.currentSteps.length - 1)
							this.currentSteps = null;
						else
							currentStepPosition = this.currentSteps[this.currentStepIndex];
					}

					if (this.currentSteps != null)
					{
						// Move towards
						movement = new Vector3(currentStepPosition).sub(this.voxelWorld.getVoxelspacePosition(this.getPosition())).nor();

						if (movement.y > 0 && this.isGrounded())
						{
							// Jump
							this.impulse(new Vector3(0, 1, 0), this.getEntityJumpStrength());
						}
					}
				}
				else if (this.targetEntity == null)
				{
					// Move randomly and change direction every 60th tick.
					if (CubicWorld.getServer().tick % 60 == 0 || this.randomMovementVector == null)
					{
						Random r = new Random();

						this.randomMovementVector = new Vector3(0.5f - r.nextFloat(), 0, 0.5f - r.nextFloat()).nor();
					}

					// Move along the random vector
					movement = new Vector3(this.randomMovementVector);
				}

				// Multiplicate with entity speed
				movement.x *= this.getEntitySpeed();
				movement.y *= this.getEntitySpeed();
				movement.z *= this.getEntitySpeed();

				// Move
				this.move(movement);

				Vector3 direction = movement.nor();
				if (this.targetEntity != null)
				{
					direction = new Vector3(this.targetEntity.getPosition()).sub(this.getPosition()).nor();
				}

				// Calculate yaw from direction to target
				this.setEulerAngles(new Vector3(0, (float) Math.toDegrees(Math.atan2(direction.x, direction.z)), 0));
			}
		}

		super.update();

	}
}
