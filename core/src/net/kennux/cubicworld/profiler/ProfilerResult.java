package net.kennux.cubicworld.profiler;

/**
 * Represents a profiler result returned by the profiler (getResults).
 * 
 * @author KennuX
 *
 */
public class ProfilerResult
{
	/**
	 * The name of the profiling object
	 */
	private String name;

	/**
	 * Nanoseconds elapsed.
	 */
	private long nanoseconds;

	/**
	 * The additional info passed in when calling startProfiling().
	 */
	private String additionalInfo;

	public ProfilerResult(String name, long nanoseconds, String additionalInfo)
	{
		this.name = name;
		this.nanoseconds = nanoseconds;
		this.additionalInfo = additionalInfo;
	}

	/**
	 * Returns additional info passed in in the constructor.
	 * 
	 * @return
	 */
	public String getAdditionalInfo()
	{
		return additionalInfo;
	}

	/**
	 * @return The time elapsed in milliseconds
	 */
	public float getMilliseconds()
	{
		return nanoseconds / 1000000.0f;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return the nanoseconds
	 */
	public long getNanoseconds()
	{
		return nanoseconds;
	}
}
