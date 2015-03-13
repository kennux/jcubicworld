package net.kennux.cubicworld.pathfinder;

import com.badlogic.gdx.math.Vector3;

/**
 * <pre>
 * Path node implementation.
 * Holds all data for path nodes.
 * 
 * Implementation details:
 * All costs are distances times 10.
 * So heuristic cost is calculated by (int)(Vector3.distance(myPosition, goalPosition) * 10).
 * This is done to avoid floating-point calculations for better performance.
 * </pre>
 * 
 * @author kennux
 *
 */
public class PathNode
{
	public Vector3 position;

	/**
	 * The movement cost.
	 */
	public int movementCost;

	/**
	 * The heuristic cost from this position to the target.
	 */
	public int heuristicCost;

	/**
	 * The complete cost (movementCost + heuristicCost).
	 */
	public int completeCost;

	/**
	 * The owner of this path node.
	 */
	public PathNode owner;

	/**
	 * Reference to the next path node.
	 */
	public PathNode nextNode;

	public PathNode(Vector3 position, PathNode owner, int movementCost, Vector3 targetPosition)
	{
		this.position = new Vector3(position);

		// Calculate costs
		this.heuristicCost = (int) (new Vector3(position).sub(targetPosition).len() * 10);
		this.movementCost = movementCost;
		this.completeCost = this.heuristicCost + this.movementCost;

		// Set owner reference
		this.owner = owner;
	}
}
