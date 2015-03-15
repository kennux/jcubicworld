package net.kennux.cubicworld.environment;

import net.kennux.cubicworld.CubicWorldConfiguration;
import net.kennux.cubicworld.math.MathUtils;
import net.kennux.cubicworld.networking.packet.ServerTimeUpdate;

/**
 * <pre>
 * Day night cycle implementation.
 * 
 * Will get simulated on the server, client will only generate sun position,
 * skybox information and lighting out of that.
 * 
 * TODO Implement support for attaching a skybox to this.
 * This class should then handle sun movement, and so on.
 * </pre>
 * 
 * @author KennuX
 *
 */
public class DayNightCycle
{
	/**
	 * The current hour.
	 */
	private byte hour;

	/**
	 * The current minute.
	 */
	private byte minute;

	/**
	 * Temporary tick counter variable used for counting ticks from 1-10 (10 ticks = 1 minute).
	 * 
	 * @see DayNightCycle#tick()
	 */
	private int tickCounter;

	/**
	 * Returns a new instance of the ServerTimeUpdate. You can add this to the
	 * packet quene to boardcast a timeupdate.
	 * 
	 * @return
	 */
	public ServerTimeUpdate getTimeUpdatePacket()
	{
		ServerTimeUpdate packet = new ServerTimeUpdate();
		packet.hour = this.hour;
		packet.minute = this.minute;

		return packet;
	}

	/**
	 * Sets the time on this day night cycle object.
	 * 
	 * @param hour
	 * @param minute
	 */
	public void setTime(byte hour, byte minute)
	{
		this.hour = hour;
		this.minute = minute;
		this.validate();
	}

	/**
	 * Call this every tick on the server. It will increment the minute by 1
	 * every 10th tick.
	 */
	public void tick()
	{
		if (CubicWorldConfiguration.inDev)
		{
			this.hour = 14;
			this.minute = 0;
			return;
		}

		// Every 10th tick.
		if (tickCounter >= 10)
		{
			this.minute++;
			this.tickCounter = 0;
		}
		else
			this.tickCounter++;

		this.validate();
	}

	/**
	 * Call this after you've modified the hour or minute value.
	 */
	private void validate()
	{
		if (this.minute >= 60)
		{
			this.hour += MathUtils.floorToInt(this.minute / 60.0f);
			this.minute -= MathUtils.floorToInt(this.minute / 60.0f) * 60;
		}
	}

	/**
	 * Returns a string in format hour:minute both with leading 0's if hour or minute are less than 10.
	 * 
	 * @return
	 */
	public String getTimeString()
	{
		// Build strings
		String hourString = Byte.toString(this.hour);
		String minuteString = Byte.toString(this.minute);

		// Add prefix
		if (minuteString.length() == 1)
			minuteString = "0" + minuteString;

		if (hourString.length() == 1)
			hourString = "0" + hourString;

		return hourString + ":" + minuteString;
	}

	/**
	 * Calculates the sun light level for the current daytime.
	 * 
	 * @return
	 */
	public byte getLightLevel()
	{
		// minutes = (hours * 60) + minute
		int minutes = (this.hour * 60) + this.minute;

		// before 03:00 or after 22:00 there is no sunlight
		if (minutes < 3 * 60 || minutes > 22 * 60)
			return 1;

		// Use parabola formula f(x) = ax² + bx + c to determine the lightlevel
		// Fixed points are 3am is sunrise, 14am is highest level (1) and 22 am is sunset
		// Values are:
		// a = -0.0114
		// b = 0.2841
		// c = -0.75

		float hourValue = minutes / 60.0f;
		float lightPercentage = (-0.0114f * (hourValue * hourValue)) + (0.2841f * hourValue) - 0.75f;

		return (byte) ((lightPercentage * (CubicWorldConfiguration.maxLightLevel - 1)) + 1);
	}

	public byte getHour()
	{
		return this.hour;
	}

	public byte getMinute()
	{
		return this.minute;
	}
}
