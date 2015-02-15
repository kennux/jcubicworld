package net.kennux.cubicworld.math;

/**
 * Float math helper implementation.
 * 
 * Most functions in here are just wrapped Math.xy functions.
 * 
 * @author KennuX
 *
 */
public class MathUtils
{
	
	/**
	 * Clamps the given byte a to a maximum of max and minimum of min
	 * @param a
	 * @param max
	 * @return
	 */
	public static byte clamp(byte a, byte max, byte min)
	{
		if (a > max)
			return max;
		else if (a < min)
			return min;
		
		return a;
	}
	
	/**
	 * Linear interpolation.
	 * @param start
	 * @param end
	 * @param percent
	 * @return
	 */
	public static float lerp(float start, float end, float percent)
	{
		return (start + percent*(end - start));
	}
	
	/**
	 * Linear interpolation.
	 * @param start
	 * @param end
	 * @param percent
	 * @return
	 */
	public static byte lerp(byte start, byte end, float percent)
	{
		return (byte) ((float)start + percent*(float)(end - start));
	}
	
	/**
	 * Ceils a float to an integer.
	 * 
	 * @param val
	 * @return
	 */
	public static int ceilToInt(float val)
	{
		return (int) Math.ceil(val);
	}

	/**
	 * Floors a float to an integer.
	 * 
	 * @param val
	 * @return
	 */
	public static int floorToInt(float val)
	{
		return (int) Math.floor(val);
	}

	/**
	 * Returns the bigger float.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static float max(float a, float b)
	{
		return Math.max(a, b);
	}

	/**
	 * Returns the bigger integer.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static int max(int a, int b)
	{
		return Math.max(a, b);
	}

	/**
	 * Returns the smaller float.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static float min(float a, float b)
	{
		return Math.min(a, b);
	}

	/**
	 * Returns the smaller integer.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static int min(int a, int b)
	{
		return Math.min(a, b);
	}
	
	public static float repeat(float v, float r)
	{
		if (v > r)
		{
			float n = MathUtils.floorToInt(v / r);
			return (v - (n * r));
		}
		return v;
	}

	/**
	 * Floors a float to an integer.
	 * 
	 * @param val
	 * @return
	 */
	public static int textureId(float val)
	{
		return (int) Math.floor(val);
	}
}
