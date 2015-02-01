package net.kennux.cubicworld.voxel;

/**
 * The world generator thread pool class.
 * 
 * Handles world generation in multiple threads. It accepts WorldGenerationTasks
 * and executes them in lifo (last-in-first-out) order.
 * 
 * @author KennuX
 *
 */
public class WorldGeneratorThreadPool
{
	/**
	 * The worker threads.
	 */
	private Thread[] workerThreads;

	/**
	 * The worker threads executor instances.
	 */
	private WorldGenerationTaskExecutor[] workExecutors;

	/**
	 * The last index of the workerThreads array used by the pool.
	 */
	private int lastThreadNum = 0;

	/**
	 * Initializes the worker thread and management thread.
	 * 
	 * @param threads
	 */
	public WorldGeneratorThreadPool(int threads)
	{
		// Init worker threads
		this.workerThreads = new Thread[threads];
		this.workExecutors = new WorldGenerationTaskExecutor[threads];
		for (int i = 0; i < threads; i++)
		{
			this.workExecutors[i] = new WorldGenerationTaskExecutor();
			this.workerThreads[i] = new Thread(this.workExecutors[i]);
			this.workerThreads[i].setName("World generator Thread #" + i);
			this.workerThreads[i].start();
		}

		this.lastThreadNum = 0;
	}

	/**
	 * Enquenes a generation job.
	 * 
	 * @param task
	 */
	public void EnqueGenerationJob(WorldGenerationTask task)
	{
		// Add task load balanced
		this.workExecutors[this.lastThreadNum].add(task);

		// Update last thread num
		this.lastThreadNum++;

		if (this.lastThreadNum >= this.workerThreads.length)
		{
			this.lastThreadNum = 0;
		}
	}

	/**
	 * Waits for all generation tasks to be finished.
	 */
	public void waitForGenerationFinished()
	{
		while (true)
		{
			// Check if all generation threads are ready
			boolean threadsReady = true;

			for (int i = 0; i < this.workExecutors.length; i++)
			{
				if (!this.workExecutors[i].tasksEmpty())
				{
					threadsReady = false;
					break;
				}
			}

			if (threadsReady)
				return;

			try
			{
				Thread.sleep(5);
			}
			catch (InterruptedException e)
			{
				// Interrupt not expected!
			}
		}
	}
}
