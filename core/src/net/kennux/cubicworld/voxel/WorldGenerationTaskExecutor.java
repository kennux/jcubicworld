package net.kennux.cubicworld.voxel;

import java.util.Stack;

/**
 * Executes a world generation task.
 * 
 * @author KennuX
 *
 */
public class WorldGenerationTaskExecutor implements Runnable
{
	/**
	 * The LIFO world generation tasks stack.
	 */
	private Stack<WorldGenerationTask> tasks;
	private Object taskLockObject = new Object();

	public WorldGenerationTaskExecutor()
	{
		this.tasks = new Stack<WorldGenerationTask>();
	}

	/**
	 * Adds the task to the task list.
	 * If there is already a task for this chunk in quene this will do nothing.
	 * 
	 * @param task
	 */
	public void add(WorldGenerationTask task)
	{
		synchronized (this.taskLockObject)
		{
			if (!this.tasks.contains(task))
				this.tasks.push(task);
		}
	}

	/**
	 * Executes the task.
	 */
	@Override
	public void run()
	{
		while (true)
		{
			WorldGenerationTask task = null;

			synchronized (this.taskLockObject)
			{
				// Is there work to do?
				if (!this.tasks.empty())
				{
					task = this.tasks.pop();
				}
			}

			// Task avail?
			if (task != null)
			{
				task.executeTask();
			}

			// Wait for the next loop
			try
			{
				Thread.sleep(10);
			}
			catch (InterruptedException e)
			{

			}
		}
	}

	/**
	 * Returns true if the tasks stack is empty.
	 * 
	 * @return
	 */
	public boolean tasksEmpty()
	{
		synchronized (this.taskLockObject)
		{
			return this.tasks.empty();
		}
	}
}
