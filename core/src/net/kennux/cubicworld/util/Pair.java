package net.kennux.cubicworld.util;

/**
 * Java pair implementation.
 * 2-element tuple.
 * 
 * @author KennuX
 *
 * @param <T1>
 * @param <T2>
 */
public class Pair<T1, T2>
{
	private T1 t1;
	private T2 t2;

	public Pair(T1 t1, T2 t2)
	{
		this.t1 = t1;
		this.t2 = t2;
	}

	public T1 getT1()
	{
		return this.t1;
	}

	public T2 getT2()
	{
		return this.t2;
	}

	@Override
	public int hashCode()
	{
		return this.t1.hashCode() ^ this.t2.hashCode();
	}
}
