package net.kennux.cubicworld.profiler;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map.Entry;

import net.kennux.cubicworld.util.ConsoleHelper;

import com.badlogic.gdx.Gdx;

/**
 * <pre>
 * Profiler used to profile different code sections.
 * Reset this after every frame, so this profiler collects data every frame.
 * 
 * This class is fully threadsafe.
 * </pre>
 * 
 * @author KennuX
 *
 */
public class Profiler
{
	public enum FileFormat
	{
		BINARY, PLAINTEXT
	}

	/**
	 * When this is set to true the profiler will write to the file opened with it.
	 * Set this to false in a production environment to not waste performance.
	 */
	private static final boolean writeToFile = false;

	/**
	 * The current running profilings.
	 * Key is the name of the profiling, value is the time millis when the
	 * profiling was started.
	 */
	private HashMap<String, Long> currentProfilings;

	/**
	 * Contains the additional infos passed in in startProfiling().
	 * Value may be null!
	 */
	private HashMap<String, String> additionalInfos;

	/**
	 * Contains the finished profilings's runtime's in nanoseconds.
	 */
	private HashMap<String, Long> finishedProfilings;
	private Object lockObject = new Object();

	/**
	 * The output stream to the file opened in openProfilingFile().
	 */
	private DataOutputStream profilingFileOutput;
	private FileFormat fileFormat;

	/**
	 * Initially contains -1.
	 * Will get resetted to -1 in reset().
	 * 
	 * After the first profiling was started this gets set to the
	 * System.nanoTime().
	 */
	private long frameStarted = -1;

	public Profiler()
	{
		if (this.currentProfilings == null)
			this.currentProfilings = new HashMap<String, Long>();

		if (this.finishedProfilings == null)
			this.finishedProfilings = new HashMap<String, Long>();

		if (this.additionalInfos == null)
			this.additionalInfos = new HashMap<String, String>();
	}

	/**
	 * Returns the profiling result for the given name.
	 * Call this before reset()!
	 * 
	 * Will return null if there is no profiling result.
	 * 
	 * @param name
	 * @return
	 */
	public float getProfilerResult(String name)
	{
		synchronized (this.lockObject)
		{
			if (finishedProfilings.containsKey(name))
			{
				return finishedProfilings.get(name).floatValue();
			}

			return 0;
		}
	}

	/**
	 * Returns all profiler results currently stored in this profiler.
	 * 
	 * @return
	 */
	public ProfilerResult[] getResults()
	{
		ProfilerResult[] results = new ProfilerResult[this.finishedProfilings.size()];
		int i = 0;

		// Pack entries into an array of profiler results.
		synchronized (this.lockObject)
		{
			for (Entry<String, Long> entry : finishedProfilings.entrySet())
			{
				results[i] = new ProfilerResult(entry.getKey(), entry.getValue(), additionalInfos.get(entry.getKey()));
				i++;
			}
		}

		return results;
	}

	/**
	 * <pre>
	 * Will create a file at the given path.
	 * If the file already exists it will get DELETED and recreated.
	 * If a file got opened and reset() gets executed, the current results will get written to the file as one frame.
	 * </pre>
	 * 
	 * @throws IOException
	 */
	public void openProfilingFile(String path, FileFormat format) throws IOException
	{
		synchronized (this.lockObject)
		{
			File profilingFile = new File(path);

			if (profilingFile.exists())
			{
				profilingFile.delete();
			}

			if (profilingFile.createNewFile())
			{
				profilingFileOutput = new DataOutputStream(new FileOutputStream(profilingFile));
			}

			fileFormat = format;
		}
	}

	/**
	 * <pre>
	 * Call this after your frame got rendered.
	 * Will remove all current profilings and re-initialize the profile.
	 * Also clears the profiler result set.
	 * 
	 * This function will write to the profiling file if it was set used
	 * openProfilingFile().
	 * </pre>
	 */
	@SuppressWarnings("unused")
	// Because of the writeToFile flag.
	public void reset()
	{
		synchronized (this.lockObject)
		{

			// Write to file?
			if (writeToFile && profilingFileOutput != null)
			{
				// Write Frame Header
				long frameId = -1;

				if (Gdx.graphics != null)
					frameId = Gdx.graphics.getFrameId();
				long timeStamp = System.currentTimeMillis();
				int entries = finishedProfilings.size();

				try
				{
					switch (fileFormat)
					{
						case BINARY:
							// Write in binary format for further usage in the
							// viewer
							profilingFileOutput.writeLong(frameId);
							profilingFileOutput.writeLong(timeStamp);
							profilingFileOutput.writeInt(entries);

							// Write data
							for (Entry<String, Long> entry : finishedProfilings.entrySet())
							{
								profilingFileOutput.write(entry.getKey().getBytes());
								profilingFileOutput.writeLong(entry.getValue().longValue());
							}
							break;
						case PLAINTEXT:
							// Write in simple plaintext format
							profilingFileOutput.write("<====================================================================================>\r\n\r\n\r\n".getBytes());
							profilingFileOutput.write(("Profile Frame: " + frameId + "\r\n").getBytes());
							profilingFileOutput.write(("Profiling time: " + new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) + "\r\n").getBytes());
							profilingFileOutput.write(("Profiling entries: " + entries + "\r\n").getBytes());
							profilingFileOutput.write("\r\n---------------------------------------\r\n".getBytes());

							// Write down entries
							for (Entry<String, Long> entry : finishedProfilings.entrySet())
							{
								profilingFileOutput.write(("Entry: " + entry.getKey() + "\r\n").getBytes());
								profilingFileOutput.write(("Nanoseconds: " + entry.getValue().longValue() + "\r\n").getBytes());
								profilingFileOutput.write(("Milliseconds: " + (entry.getValue().longValue() / 1000000.0f) + "\r\n").getBytes());
								profilingFileOutput.write(("Additional info: " + additionalInfos.get(entry.getKey()) + "\r\n").getBytes());
								profilingFileOutput.write("\r\n---------------------------------------\r\n".getBytes());
							}

							profilingFileOutput.write("<====================================================================================>\r\n\r\n\r\n".getBytes());
							break;
					}

					// Flush
					profilingFileOutput.flush();
				}
				catch (IOException e)
				{
					// Something bad happend
					ConsoleHelper.writeLog("error", "Profiling save failed!", "Profiler");
					ConsoleHelper.logError(e);
					profilingFileOutput = null;
				}
			}

			// The actual reset
			frameStarted = -1;
			currentProfilings.clear();
			additionalInfos.clear();
			finishedProfilings.clear();
		}
	}

	/**
	 * Starts a profiling for the given name.
	 * 
	 * @param name
	 * @param additionalInfo
	 *            Additional info, like parameters. Can be null.
	 */
	public void startProfiling(String name, String additionalInfo)
	{
		synchronized (this.lockObject)
		{
			// First this frame?
			if (frameStarted == -1)
			{
				frameStarted = System.nanoTime();
			}

			currentProfilings.put(name, System.nanoTime());
			additionalInfos.put(name, additionalInfo);
		}
	}

	/**
	 * <pre>
	 * Stops the profiling for the given profiling name.
	 * Will add the result of the profiling to the profiler result set.
	 * 
	 * If no profiling with the given name is running atm nothing will happen in
	 * here.
	 * </pre>
	 * 
	 * @param name
	 */
	public void stopProfiling(String name)
	{
		synchronized (this.lockObject)
		{
			if (currentProfilings.containsKey(name))
			{
				// Get profilier start time.
				long timeDifference = System.nanoTime() - currentProfilings.get(name);
				finishedProfilings.put(name, timeDifference);
				currentProfilings.remove(name);
			}
		}
	}
}
