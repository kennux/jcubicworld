package net.kennux.cubicworld.pathfinder;

import java.util.ArrayList;
import java.util.LinkedList;

import net.kennux.cubicworld.util.ConsoleHelper;
import net.kennux.cubicworld.util.ObjectMap;
import net.kennux.cubicworld.util.VectorHelper;
import net.kennux.cubicworld.voxel.VoxelWorld;

import com.badlogic.gdx.math.Vector3;

/**
 * <pre>
 * The pathfinder class.
 * It uses a slightly modified version of A-Star pathfinding.
 * 
 * The pathfinder will run in it's own thread.
 * 
 * TODO implement character height consideration in findPath().
 * </pre>
 * 
 * @author kennux
 *
 */
public class Pathfinder implements Runnable
{
	/**
	 * This thread runs the pathfinder main loop.
	 */
	private Thread pathfinderThread;

	/**
	 * VoxelWorld instance from the main class.
	 * Gets used to perform the pathfinding.
	 */
	private VoxelWorld world;

	/**
	 * Contains all tasks still to process.
	 */
	private LinkedList<PathfinderTask> taskStack;
	private Object taskStackLockObject = new Object();

	
	public Pathfinder(VoxelWorld world)
	{
		// Start the pathfinder thread
		this.pathfinderThread = new Thread(this);
		this.pathfinderThread.setName("Pathfinder worker thread");
	
		// Initialize
		this.world = world;
		this.taskStack = new LinkedList<PathfinderTask>();
		
		// Start the thread
		this.pathfinderThread.start();
	}

	/**
	 * Enquenes a new path generation.
	 * 
	 * @param p
	 */
	public void addPathfinderTask(Path p)
	{
		synchronized (this.taskStackLockObject)
		{
			this.taskStack.push(new PathfinderTask(p));
		}
	}

	/**
	 * Performs the A-Star pathfinding for given path p.
	 * TODO Consider character height!
	 * 
	 * @param p
	 */
	private void findPath(Path p)
	{
		// Declare lists
		ObjectMap<Vector3, PathNode> openList = new ObjectMap<Vector3, PathNode>(Vector3.class, PathNode.class);
		ArrayList<Vector3> closedList = new ArrayList<Vector3>();

		// Start pathfinding
		PathNode startNode = new PathNode(p.getStartPosition(), null, 0, p.getEndPosition());
		startNode.owner = startNode;
		PathNode currentNode = startNode;

		boolean pathFound = false;
		boolean noPath = false;
		int movementCost = 0;
		int stepsMade = 0;

		try
		{
			while (!pathFound && !noPath)
			{
				// Calculate voxel direction positions
				Vector3[] positions = new Vector3[] {
						// Front
						new Vector3(currentNode.position).add(VectorHelper.forward),
						// Back
						new Vector3(currentNode.position).add(VectorHelper.back),
						// Left
						new Vector3(currentNode.position).add(VectorHelper.left),
						// Right
						new Vector3(currentNode.position).add(VectorHelper.right),
						// Front right
						new Vector3(currentNode.position).add(VectorHelper.forward).add(VectorHelper.right),
						// Front left
						new Vector3(currentNode.position).add(VectorHelper.forward).add(VectorHelper.left),
						// Back right
						new Vector3(currentNode.position).add(VectorHelper.back).add(VectorHelper.right),
						// Back left
						new Vector3(currentNode.position).add(VectorHelper.back).add(VectorHelper.left),
						// Up Front
						new Vector3(currentNode.position).add(VectorHelper.forward).add(VectorHelper.up),
						// Up Back
						new Vector3(currentNode.position).add(VectorHelper.back).add(VectorHelper.up),
						// Up Left
						new Vector3(currentNode.position).add(VectorHelper.left).add(VectorHelper.up),
						// Up Right
						new Vector3(currentNode.position).add(VectorHelper.right).add(VectorHelper.up),
						// Down Front
						new Vector3(currentNode.position).add(VectorHelper.forward).add(VectorHelper.down),
						// Down Back
						new Vector3(currentNode.position).add(VectorHelper.back).add(VectorHelper.down),
						// Down Left
						new Vector3(currentNode.position).add(VectorHelper.left).add(VectorHelper.down),
						// Down Right
						new Vector3(currentNode.position).add(VectorHelper.right).add(VectorHelper.down), };

				// Analyze Surrounding path nodes
				PathNode[] nodes = new PathNode[positions.length];
				PathNode lowestCostNode = null;

				// Check which ones are walkable and add them to the nodes-array
				for (int i = 0; i < positions.length; i++)
				{
					int currentMovementCost = (int) VectorHelper.distance(positions[i], currentNode.position);

					// Check if node is walkable
					if (!closedList.contains(positions[i]) && !this.world.hasVoxel((int) positions[i].x, (int) positions[i].y, (int) positions[i].z) &&
					// Walkable / ground check
							(!p.needsGround() || this.world.hasVoxel((int) positions[i].x, (int) positions[i].y - 1, (int) positions[i].z)))
					{
						// Add node to the nodes-array
						if (openList.containsKey(positions[i]))
						{
							nodes[i] = openList.get(positions[i]);
						}
						else
						{
							nodes[i] = new PathNode(positions[i], currentNode, movementCost + currentMovementCost, p.getEndPosition());
							openList.put(positions[i], nodes[i]);
						}
					}

					// Check for lowest cost
					if (nodes[i] != null && (lowestCostNode == null || nodes[i].completeCost < lowestCostNode.completeCost))
					{
						lowestCostNode = nodes[i];
					}
				}

				// Failed? o_O
				if (lowestCostNode == null)
				{
					noPath = true;
					break;
				}

				if (currentNode.position.equals(p.getEndPosition()))
					pathFound = true;

				// Put the lowest cost node on the closed list
				if (currentNode.owner.position.equals(lowestCostNode.owner.position))
				{
					currentNode.owner.nextNode = lowestCostNode;
				}
				else
					currentNode.nextNode = lowestCostNode;

				closedList.add(currentNode.position);
				currentNode = lowestCostNode;

				stepsMade++;
				if (stepsMade == 100)
				{
					noPath = true;
					break;
				}
			}
		}
		catch (Exception e)
		{
			ConsoleHelper.writeLog("error", "Exception in pathfinder: ", "Pathfinder");
			ConsoleHelper.logError(e);
			noPath = true;
		}

		// Path found?
		if (noPath)
		{
			// No... :'(
			p.setStepData(null);
		}
		else
		{
			// :^)
			// This is needed because in the closedlist there can be movements
			// which are like
			// front, right
			// this should be done in one step frontright and this gets achieved
			// by generating an array from the path node's linked list.
			ArrayList<Vector3> steps = new ArrayList<Vector3>();
			PathNode cNode = startNode;

			while (cNode != null)
			{
				steps.add(cNode.position);
				cNode = cNode.nextNode;
			}

			p.setStepData(steps.toArray(new Vector3[steps.size()]));
		}
	}

	/**
	 * The pathfinder main loop.
	 */
	@Override
	public void run()
	{
		// This array list will get used as a temporary storage for tasks which needs to get executed.
		// This is needed because otherwise the addPathfinderTask function would block till all current tasks are done.
		ArrayList<PathfinderTask> tasks = new ArrayList<PathfinderTask>();
		
		while (true)
		{
			tasks.clear();
			
			// Work available?
			synchronized (this.taskStackLockObject)
			{
				if (this.taskStack.size() > 0)
				{
					tasks.add(this.taskStack.removeFirst());
				}
			}
			
			for (PathfinderTask task : tasks)
			{	
				// Process all tasks
				while (task != null)
				{
					// Actually find the path
					this.findPath(task.pathInstance);

					// Get next object
					if (this.taskStack.size() > 0)
						task = this.taskStack.removeFirst();
					else
						task = null;
				}
			}

			// Wait some time before re-loop
			try
			{
				Thread.sleep(1);
			}
			catch (InterruptedException e)
			{

			}
		}
	}

}
