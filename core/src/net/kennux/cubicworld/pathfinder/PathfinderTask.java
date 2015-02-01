package net.kennux.cubicworld.pathfinder;

/**
 * Pathfinder task data holder.
 * Will get enquened in Pathfinder for processing.
 * 
 * @author kennux
 *
 */
public class PathfinderTask
{
	/**
	 * The path instance.
	 */
	public Path pathInstance;

	/**
	 * Sets pathInstance.
	 * 
	 * @param pathInstance
	 */
	public PathfinderTask(Path pathInstance)
	{
		this.pathInstance = pathInstance;
	}
}
