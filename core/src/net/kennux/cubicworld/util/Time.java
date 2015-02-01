package net.kennux.cubicworld.util;

import net.kennux.cubicworld.CubicWorld;

import com.badlogic.gdx.Gdx;

/**
 * Time static class.
 * Proxifies function calls to for example getDeltaTime() to provide this
 * functionality also on server.
 * 
 * @author KennuX
 *
 */
public class Time
{
	/**
	 * On client: returns Gdx.graphics.getDeltaTime();
	 * On server: TODO
	 * 
	 * @return
	 */
	public static float getDeltaTime()
	{
		if (Gdx.graphics != null)
			return Gdx.graphics.getDeltaTime();
		else
			return CubicWorld.getServer().deltaTime;
	}
}
