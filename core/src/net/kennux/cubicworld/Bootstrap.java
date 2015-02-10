package net.kennux.cubicworld;

import java.util.ArrayList;

import net.kennux.cubicworld.entity.EntitySystem;
import net.kennux.cubicworld.gui.AGuiOverlay;
import net.kennux.cubicworld.item.ItemSystem;
import net.kennux.cubicworld.pluginapi.APlugin;
import net.kennux.cubicworld.util.ConsoleHelper;
import net.kennux.cubicworld.voxel.VoxelData;
import net.kennux.cubicworld.voxel.VoxelEngine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

/**
 * <pre>
 * Bootstrap class. This class will get used to initialize different block types.
 * 
 * This class is static.
 * It got a normal bootstrap() which will load all resources and data needed for the client.
 * It also got a bootstrapServer() which will load all resources and data needed for the server.
 * 
 * @author KennuX
 * 
 * <pre>
 */
public class Bootstrap
{
	/**
	 * <pre>
	 * Client bootstrap.
	 * It executes:
	 * 
	 * - loadTextures()
	 * - loadModels()
	 * - loadSounds()
	 * 
	 * - defineVoxelTypes()
	 * - defineItemTypes()
	 * - defineEntityTypes()
	 * - defineClientProtocol()
	 * - initializeGuiManager()
	 * - defineInputKeyActions()
	 * </pre>
	 * 
	 * @param cubicWorld
	 *            the game's main instance.
	 * @param pluginManager
	 *            the game's plugin manager.
	 */
	public static void bootstrap(CubicWorldGame cubicWorld, PluginManager pluginManager)
	{
		pluginManager.initialize(plugins);

		loadTextures(pluginManager);
		pluginManager.loadModels();
		pluginManager.loadSounds();

		pluginManager.defineVoxelTypes();
		ItemSystem.createItemsForVoxelTypes();
		pluginManager.defineItemTypes();
		ItemSystem.finalizeTextureAtlas();
		EntitySystem.initialize();
		pluginManager.defineEntityTypes();
		pluginManager.defineProtocol();
		pluginManager.initializeGuiSystem();
		pluginManager.initializeGuiManager(cubicWorld.guiManager);
		pluginManager.defineInputHandlers(cubicWorld.inputManager);

		VoxelEngine.finalization();

		pluginManager.postInit(false);
	}

	/**
	 * Load your textures and register them here.
	 */
	private static void loadTextures(PluginManager pluginManager)
	{
		// Load obscure texture
		AGuiOverlay.overlayObscureTexture = new Texture(Gdx.files.internal("textures\\gui\\obscure.png"));

		// init
		VoxelEngine.initialize(128, 128);
		ItemSystem.initialize(128, 128);

		// Load textures from all plugins
		pluginManager.loadTextures();

		// Compile atlases
		VoxelEngine.compileTextureAtlas();
		ItemSystem.compileTextureAtlas();
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
			catch (InstantiationException e)
			{
				ConsoleHelper.writeLog("error", "Instantiation exception while loading plugins: ", "Bootstrap");
				e.printStackTrace();
				System.exit(-1);
			}
			catch (IllegalAccessException e)
			{
				ConsoleHelper.writeLog("error", "Illegal access exception while loading plugins: ", "Bootstrap");
				e.printStackTrace();
				System.exit(-1);
			}
			catch (ClassNotFoundException e)
			{
				ConsoleHelper.writeLog("error", "Class not found exception while loading plugins: ", "Bootstrap");
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	private static ArrayList<APlugin> plugins;
}
