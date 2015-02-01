package net.kennux.cubicworld.entity;

import net.kennux.cubicworld.CubicWorld;
import net.kennux.cubicworld.inventory.PlayerInventory;
import net.kennux.cubicworld.networking.packet.inventory.ClientDropItem;
import net.kennux.cubicworld.networking.packet.inventory.ClientItemMove;
import net.kennux.cubicworld.util.Mathf;
import net.kennux.cubicworld.util.Time;
import net.kennux.cubicworld.voxel.VoxelCollision;
import net.kennux.cubicworld.voxel.VoxelWorld;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

/**
 * <pre>
 * The player controller for simulating physics of a player.
 * This class is most likely a slightly modified version of ACharacterEntity.
 * It uses the same functions for collision detection, it is only more specialized for player controlling.
 * </pre>
 * 
 * @author KennuX
 *
 */
public class PlayerController extends AEntity
{
	/**
	 * The player's camera.
	 */
	private PerspectiveCamera camera;

	/**
	 * The cubic world instance.
	 */
	public VoxelWorld voxelWorld;

	/**
	 * The currently playing footstep loop.
	 * -1 if not playing.
	 */
	private long footstepSoundInstance = -1;

	/**
	 * The currently playing footstep sound.
	 */
	private Sound currentFootstepSound;

	/**
	 * The player inventory instance.
	 * It will get set in the ServerPlayerSpawn packet interpretation on the client side.
	 */
	private PlayerInventory playerInventory;

	/**
	 * The player's velocity.
	 */
	private Vector3 velocity;

	public PlayerController(VoxelWorld voxelWorld, PerspectiveCamera camera)
	{
		this.voxelWorld = voxelWorld;
		this.velocity = new Vector3(Vector3.Zero);

		this.camera = camera;
		this.camera.update(true);
		this.playerInventory = new PlayerInventory();
	}

	/**
	 * Drops an item from the stack located in the given slot from the player inventory.
	 * 
	 * @param itemSlot
	 */
	public void dropItem(int itemSlot)
	{
		// Send item drop packet
		ClientDropItem dropItemPacket = new ClientDropItem();
		dropItemPacket.itemSlotId = itemSlot;
		CubicWorld.getClient().client.sendPacket(dropItemPacket);
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
	 * <pre>
	 * Returns the bounding box of this character entity at a given position
	 * (bottom center).
	 * It will calculate the minimum and maximum vector in this way:
	 * 
	 * min = pos + Vector3(characterWidth/2, 0, characterDepth / 2)
	 * max = pos + Vector3(characterWidth/2, characterHeight, characterDepth / 2)
	 * </pre>
	 * 
	 * @return
	 */
	public BoundingBox getBoundingBox(Vector3 position)
	{
		// Construct bounding box position is the center of the collider, this constructs the minimum position vector and the maximum position vector
		Vector3 minimumPos = new Vector3(position.x - (this.getCharacterWidth() / 2.0f), position.y, position.z - (this.getCharacterDepth() / 2.0f));
		Vector3 maximumPos = new Vector3(position.x + (this.getCharacterWidth() / 2.0f), position.y + (this.getCharacterHeight()), position.z + (this.getCharacterDepth() / 2.0f));

		// Instantiate bounding box
		return new BoundingBox(minimumPos, maximumPos);
	}

	/**
	 * The depth of the player in voxels.
	 */
	protected int getCharacterDepth()
	{
		return 1;
	}

	/**
	 * The height of the player in voxels.
	 */
	protected float getCharacterHeight()
	{
		return 1.75f;
	}

	/**
	 * The width of the player in voxels.
	 */
	protected int getCharacterWidth()
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
	 * <pre>
	 * Returns the player inventory instance.
	 * DON'T do any manipulation of the inventory data local!
	 * 
	 * If you want to manipulate player inventory data use functions of the playercontroller!
	 * This is needed to keep the inventory in sync with the server.
	 * </pre>
	 * 
	 * @return the playerInventory
	 */
	public PlayerInventory getPlayerInventory()
	{
		return this.playerInventory;
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
	 * Does nothing!
	 */
	@Override
	public void init()
	{
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
	 * Moves an item in the player inventory from sourceSlot to targetSlot.
	 * 
	 * @param sourceSlot
	 * @param targetSlot
	 */
	public void moveItem(int sourceSlotId, int targetSlotId)
	{
		// Send item move packet
		CubicWorld.getClient().client.sendPacket(ClientItemMove.createPlayerInventoryMove(sourceSlotId, targetSlotId));
	}

	/**
	 * TODO Weapon / item view rendering.
	 */
	@Override
	public void render(Camera camera, ModelBatch modelBatch, DecalBatch decalBatch, SpriteBatch spriteBatch, BitmapFont bitmapFont)
	{
		// This cannot get rendered!
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
	 * - Update camera
	 * </pre>
	 * 
	 */
	@Override
	public void update()
	{
		boolean grounded = this.isGrounded();

		// Physics
		if (!grounded)
		{
			this.velocity.y += Time.getDeltaTime() * -9.81f;
		}

		// Max check
		if (this.velocity.x > 0)
			this.velocity.x = Mathf.min(this.velocity.x, this.getMaximumSpeed());
		else if (this.velocity.x < 0)
			this.velocity.x = Mathf.max(this.velocity.x, -this.getMaximumSpeed());

		if (this.velocity.y > 0)
			this.velocity.y = Mathf.min(this.velocity.y, this.getMaximumSpeed());
		else if (this.velocity.y < 0)
			this.velocity.y = Mathf.max(this.velocity.y, -this.getMaximumSpeed());

		if (this.velocity.z > 0)
			this.velocity.z = Mathf.min(this.velocity.z, this.getMaximumSpeed());
		else if (this.velocity.z < 0)
			this.velocity.z = Mathf.max(this.velocity.z, -this.getMaximumSpeed());

		boolean moving = new Vector3(this.velocity).add(0, -this.velocity.y, 0).len() > 0.1f;

		if (grounded && moving)
		{
			// Grounded, footstep sound handling
			// Get voxel beyond player's position
			Vector3 voxelPos = this.voxelWorld.getVoxelspacePosition(this.getPosition());
			voxelPos.y -= 1;

			Sound footstepSound = null;
			if (this.voxelWorld.hasVoxel((int) voxelPos.x, (int) voxelPos.y, (int) voxelPos.z))
			{
				footstepSound = this.voxelWorld.getVoxel((int) voxelPos.x, (int) voxelPos.y, (int) voxelPos.z).voxelType.getFootstepSound();
			}

			if (footstepSound != null)
			{
				if (this.footstepSoundInstance == -1 && this.currentFootstepSound == null)
				{
					this.footstepSoundInstance = footstepSound.loop(1.0f);
					this.currentFootstepSound = footstepSound;
				}
				// Stop playing and play other sound
				else if (this.footstepSoundInstance != -1 && this.currentFootstepSound != null && this.currentFootstepSound != footstepSound)
				{
					this.currentFootstepSound.stop(this.footstepSoundInstance);
					this.footstepSoundInstance = footstepSound.loop(1.0f);
					this.currentFootstepSound = footstepSound;
				}
			}
		}

		if ((!grounded || !moving) && this.footstepSoundInstance != -1)
		{
			this.currentFootstepSound.stop(this.footstepSoundInstance);
			this.currentFootstepSound = null;
			this.footstepSoundInstance = -1;
		}

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

		// Calculate camera position
		this.interpolatePosition(true);

		Vector3 camPos = new Vector3(this.getPosition());
		camPos.y += this.getCharacterHeight();
		this.camera.position.set(camPos);
		this.camera.update(true);
	}
}
