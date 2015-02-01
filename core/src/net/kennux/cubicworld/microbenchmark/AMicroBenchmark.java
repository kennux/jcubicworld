package net.kennux.cubicworld.microbenchmark;

import java.lang.reflect.Method;

/**
 * <pre>
 * Abstract base class for fast implementation of profilings.
 * You can use this to easily microbenchmark java snippets.
 * 
 * I personally use this for example to measure the difference between array list and direct array accesses.
 * </pre>
 * 
 * @author KennuX
 *
 */
public abstract class AMicroBenchmark
{
	/**
	 * Call this in your microbenchmark main() routine.
	 */
	public void benchmark()
	{
		// Reflect all benchmarks
		Method[] methods = this.getClass().getMethods();

		for (int i = 0; i < methods.length; i++)
		{
			// Get microbenchmark annotation
			Method currentMethod = methods[i];

			if (currentMethod.isAnnotationPresent(MicroBenchmark.class))
			{
				try
				{
					MicroBenchmark annotation = currentMethod.getAnnotation(MicroBenchmark.class);

					System.out.println("Executing Microbenchmark " + annotation.name() + "...");

					// Prepare
					int iterationCount = annotation.iterations();
					long start = System.nanoTime();
					double highestTime = -1;
					double lowestTime = -1;

					// Benchmark
					for (int j = 0; j < iterationCount; j++)
					{
						long execStart = System.nanoTime();
						currentMethod.invoke(this);
						double elapsedTime = (double) (System.nanoTime() - execStart) / 1000000000.0;

						if (highestTime == -1 || elapsedTime > highestTime)
							highestTime = elapsedTime;

						if (lowestTime == -1 || elapsedTime < lowestTime)
							lowestTime = elapsedTime;
					}

					// Calculate values
					long end = System.nanoTime();
					long elapsedTime = end - start;
					double elapsedSeconds = (double) elapsedTime / 1000000000.0;

					double meanTime = elapsedSeconds / (double) iterationCount;

					// Output stats
					System.out.println("");
					System.out.println("Iterations: " + iterationCount);
					System.out.println("All iterations (Seconds): " + elapsedSeconds);
					System.out.println("Meantime (Seconds): " + elapsedSeconds);
					System.out.println("Lowest time (Seconds): " + lowestTime);
					System.out.println("Highest time (Seconds): " + highestTime);
					System.out.println("");
				}
				catch (Exception e)
				{
					System.err.println("Error while benchmarking method " + currentMethod.getName());
				}
			}
		}
	}
}
