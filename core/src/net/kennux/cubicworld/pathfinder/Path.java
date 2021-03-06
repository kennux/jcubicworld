package net.kennux.cubicworld.pathfinder;

import com.badlogic.gdx.math.Vector3;

/**
 * <pre>
 * Dataholder object.
 * Holds the data of a path generated by the Pathfinder.
 * 
 * As the Pathfinder runs in it's own thread, this class is fully thread-safe.
 * </pre>
 * 
 * @author kennux
 *
 */
public class Path
{
	private Object lockObject = new Object();

	/**
	 * Single steps found by the pathfinder.
	 */
	private Vector3[] steps;

	/**
	 * Gets set to true after the pathfinder processed this path's request.
	 */
	private boolean processed = false;

	/**
	 * Gets set to true when a path was found.
	 */
	private boolean found = false;

	/**
	 * The start position of this path in blockspace.
	 * Gets set in the constructor.
	 */
	private Vector3 startPosition;

	/**
	 * The end position of this path in blockspace.
	 * Gets set in the constructor.
	 */
	private Vector3 endPosition;

	/**
	 * Gets set in the constructor.
	 * If it is true the pathfinder needs to check if there is a voxel beyond
	 * the current processed one.
	 * This should be true if paths for example animals are needed.
	 */
	private boolean needsGround;

	/**
	 * The character height.
	 */
	private int characterHeight;

	/**
	 * <pre>
	 * Initializes a new path instance.
	 * Start position and endPosition should be in blockspace.
	 * They should be the most bottom block position an for example entity is
	 * occuping.
	 * 
	 * </pre>
	 * 
	 * @param startPosition
	 * @param endPosition
	 * @params needsGround when this is set to true the pathfinder will check if there is a block beyond the voxel which it currently processes.
	 *         This should be true if paths for example animals are needed.
	 * @param characterBlockHeight
	 *            The height of the character used for pathfinding.
	 */
	public Path(Vector3 startPosition, Vector3 endPosition, boolean needsGround, int characterBlockHeight)
	{
		// Set variables
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		this.needsGround = needsGround;
		this.characterHeight = characterBlockHeight;
	}

	public int getCharacterHeight()
	{
		return characterHeight;
	}

	public Vector3 getEndPosition()
	{
		return endPosition;
	}

	/**
	 * Gets the movement direction towards the next point.
	 * Transforms the position into global blockspace and gets it's next point in list.
	 * TODO
	 * 
	 * @param position
	 */
	public void getMovementDirection(Vector3 position)
	{

	}

	// ////////////////////////////// GETTERS AND SETTERS //////////////////////////////////

	public Vector3 getStartPosition()
	{
		return startPosition;
	}

	/**
	 * Returns the step data vectors set in setStepData() (in the pathfinder).
	 * 
	 * @return
	 */
	public Vector3[] getStepData()
	{
		synchronized (this.lockObject)
		{
			return this.steps;
		}
	}

	public boolean isFound()
	{
		return found;
	}

	public boolean isProcessed()
	{
		return processed;
	}

	public boolean needsGround()
	{
		return needsGround;
	}

	public void setEndPosition(Vector3 endPosition)
	{
		this.endPosition = endPosition;
	}

	public void setFound(boolean found)
	{
		this.found = found;
	}

	public void setProcessed(boolean processed)
	{
		this.processed = processed;
	}

	public void setStartPosition(Vector3 startPosition)
	{
		this.startPosition = startPosition;
	}

	/**
	 * Sets the steps array for this path.
	 * If this function was called this path gets marked as processed and found.
	 * 
	 * Pass null in as steps to set this instance to path was processed but not
	 * found.
	 * 
	 * @param steps
	 */
	public void setStepData(Vector3[] steps)
	{
		synchronized (this.lockObject)
		{
			this.steps = steps;

			this.found = (steps != null);

			this.processed = true;
		}
	}
}
