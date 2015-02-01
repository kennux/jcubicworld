package net.kennux.cubicworld.test;

import java.util.ArrayList;

import net.kennux.cubicworld.Bootstrap;
import net.kennux.cubicworld.CubicWorldConfiguration;
import net.kennux.cubicworld.CubicWorldServer;
import net.kennux.cubicworld.PluginManager;
import net.kennux.cubicworld.ServerBootstrap;
import net.kennux.cubicworld.util.ConsoleHelper;

/**
 * This class contains a main function for executing cubic world tests.
 * TODO
 * Very much test implementations :/
 * 
 * @author KennuX
 *
 */
public class Tests
{
	public static void main(String[] args)
	{
		// Change this to true if you want to test the networking
		// Normally this is set to false as networking tests take some time.
		boolean executeNetworkTests = false;

		ArrayList<String> tests = new ArrayList<String>();
		tests.add(BitReaderWriterTest.class.getName());
		tests.add(InventoryTest.class.getName());
		tests.add(VoxelWorldTest.class.getName());
		tests.add(InputSystemTest.class.getName());
		tests.add(SerializerTest.class.getName());

		if (executeNetworkTests)
		{
			tests.add(NetworkingProtocolTest.class.getName());
		}

		// Activate silent mode
		ConsoleHelper.silent = true;

		// Load config
		try
		{
			CubicWorldConfiguration.load();
		}
		catch (Exception e)
		{
			ConsoleHelper.writeLog("error", "Configuration load failed!", "Launcher");
			e.printStackTrace();
			System.exit(-1);
		}

		// Execute bootstrap
		serverPluginManager = new PluginManager();
		ServerBootstrap.preInitialize();
		Bootstrap.preInitialize();
		ServerBootstrap.bootstrap(serverPluginManager);

		// Start testserver
		serverInstance = new CubicWorldServer((short) 1337, "TEST", 5);

		// Run tests
		org.junit.runner.JUnitCore.main(tests.toArray(new String[tests.size()]));
	}

	/**
	 * The server plugin manager loaded in the initialization.
	 */
	public static PluginManager serverPluginManager;

	/**
	 * The server instance listening on port 1337
	 */
	public static CubicWorldServer serverInstance;
}
