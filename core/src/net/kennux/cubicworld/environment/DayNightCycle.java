package net.kennux.cubicworld.environment;

import net.kennux.cubicworld.networking.packet.ServerTimeUpdate;
import net.kennux.cubicworld.util.Mathf;

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
}
