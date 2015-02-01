package net.kennux.cubicworld;

import java.util.ArrayList;

import net.kennux.cubicworld.entity.EntitySystem;
import net.kennux.cubicworld.item.ItemSystem;
import net.kennux.cubicworld.pluginapi.APlugin;
import net.kennux.cubicworld.util.ConsoleHelper;
import net.kennux.cubicworld.voxel.VoxelEngine;

public class ServerBootstrap
{
	/**
	 * <pre>
	 * Server bootstrap.
	 * It executes:
	 * 
	 * - defineVoxelTypes()
	 * - defineItemTypes()
	 * - defineEntityTypes()
	 * - defineServerProtocol()
	 * </pre>
	 * 
	 * @param pluginManager
	 *            The server's plugin manager.
	 */
	public static void bootstrap(PluginManager pluginManager)
	{
		pluginManager.initialize(plugins);
		VoxelEngine.initialize(0, 0);
		ItemSystem.initialize(0, 0);

		pluginManager.defineVoxelTypes();
		ItemSystem.createItemsForVoxelTypes();
		pluginManager.defineItemTypes();
		EntitySystem.initialize();
		pluginManager.defineEntityTypes();
		pluginManager.defineProtocol();

		VoxelEngine.finalization();
	}

	/**
	 * <pre>
	 * Call this before anything else to load all available plugins.
	 * Even before instancing the CubicWorld or CubicWorldServer!
	 * 
	 * It will load all plugins by reflecting all classes in net.kennux.cubicworld.plugins.
	 * TODO: Dynamic reflection package implementation (Load package paths by config file).
	 * </pre>
	 */
	@SuppressWarnings("rawtypes")
	public static void preInitialize()
	{
		ConsoleHelper.writeLog("info", "Loading plguins...", "Bootstrap");

		// Load all plugins
		plugins = new ArrayList<APlugin>();

		// Instantiate all plugins
		for (String classPath : CubicWorldConfiguration.plugins)
		{
			try
			{
				Class pClass = Class.forName(classPath);
				APlugin plugin = (APlugin) pClass.newInstance();
				plugins.add(plugin);
				ConsoleHelper.writeLog("info", "Loaded plugin " + pClass.getName(), "Bootstrap");
			}
			catch (InstantiationException | IllegalAccessException | ClassNotFoundException e)
			{
				ConsoleHelper.writeLog("error", "Instantiation exception while loading plugins: ", "Bootstrap");
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	private static ArrayList<APlugin> plugins;
}
