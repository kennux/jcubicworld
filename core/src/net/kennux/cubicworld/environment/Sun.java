package net.kennux.cubicworld.environment;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;

/**
 * <pre>
 * Sun implementation for usage in a day / night system.
 * This class is not finished yet.
 * It will handle sun sprite rendering. It is currently just a placeholder class.
 * A sun will get added to the skybox as soon as the skybox supports it.
 * TODO
 * </pre>
 * 
 * @author KennuX
 *
 */
public class Sun
{
	/**
	 * The directional light dir.
	 */
	private Vector3 direction;

	/**
	 * The light's color.
	 */
	private Color lightColor;

	public Sun()
	{
		this.setDirection(new Vector3());
		this.setLightColor(Color.YELLOW);
	}

	public Vector3 getDirection()
	{
		return direction;
	}

	public Color getLightColor()
	{
		return lightColor;
	}

	public void setDirection(Vector3 direction)
	{
		this.direction = direction;
	}

	public void setLightColor(Color lightColor)
	{
		this.lightColor = lightColor;
	}
}
