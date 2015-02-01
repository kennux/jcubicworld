package net.kennux.cubicworld;

import net.kennux.cubicworld.util.ConsoleHelper;

public class CubicWorld
{
	/**
	 * Replacement of System.exit().
	 */
	public static void exit(int code)
	{
		System.exit(code);
	}

	/**
	 * @return the client
	 */
	public static synchronized CubicWorldGame getClient()
	{
		return client;
	}

	/**
	 * @return the server
	 */
	public static synchronized CubicWorldServer getServer()
	{
		return server;
	}

	/**
	 * <pre>
	 * Returns true if there is a cubicworld instance running atm.
	 * This can get used for example to seperate server from client code in plugins.
	 * 
	 * </pre>
	 * 
	 * @see CubicWorldServer#isRunning()
	 */
	public static boolean isClient()
	{
		return CubicWorld.getClient() != null;
	}

	/**
	 * <pre>
	 * Returns true if there is a cubicworld server running atm.
	 * This can get used for example to seperate server from client code in plugins.
	 * 
	 * </pre>
	 * 
	 * @see CubicWorldServer#isRunning()
	 */
	public static boolean isServer()
	{
		return CubicWorld.getServer() != null;
	}

	/**
	 * @param client
	 *            the client to set
	 */
	public static synchronized void setClient(CubicWorldGame client)
	{
		if (CubicWorld.getClient() != null)
		{
			ConsoleHelper.writeLog("ERROR", "Singleton pattern voilated! Tried to register new client instance! Shutting down...", "CubicWorld");
			CubicWorld.exit(-1);
		}
		CubicWorld.client = client;
	}

	/**
	 * @param server
	 *            the server to set
	 */
	public static synchronized void setServer(CubicWorldServer server)
	{
		if (CubicWorld.getServer() != null)
		{
			ConsoleHelper.writeLog("ERROR", "Singleton pattern voilated! Tried to register new server instance! Shutting down...", "CubicWorld");
			CubicWorld.exit(-1);
		}
		CubicWorld.server = server;
	}

	private static CubicWorldGame client;

	private static CubicWorldServer server;
}
