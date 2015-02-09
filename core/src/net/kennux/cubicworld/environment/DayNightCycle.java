package net.kennux.cubicworld.environment;

import net.kennux.cubicworld.CubicWorldConfiguration;
import net.kennux.cubicworld.math.Mathf;
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

	private int tickCounter;

	public byte getHour()
	{
		return this.hour;
	}

	public byte getMinute()
	{
		return this.minute;
	}

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
			this.hour += Mathf.floorToInt(this.minute / 60.0f);
			this.minute -= Mathf.floorToInt(this.minute / 60.0f) * 60;
		}
	}
	
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
	
	public byte getLightLevel()
	{
		int minutes = (this.hour * 60) + this.minute;
		
		if (minutes < 3*60 || minutes > 22*60)
			return 1;
		
		float lightPercentage = ((minutes - (3*60)) / (19f * 60f)) * 2.0f;
		if (lightPercentage > 1)
			lightPercentage = 1.0f - Mathf.repeat(lightPercentage, 1.0f);
		
		return (byte) ((lightPercentage * (CubicWorldConfiguration.maxLightLevel-1))+1);
	}
}
