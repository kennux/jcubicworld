package net.kennux.cubicworld.pluginapi;

import net.kennux.cubicworld.gui.GuiManager;
import net.kennux.cubicworld.input.InputManager;

/**
 * Implement this class in your plugin. It will handle loading and registering
 * all your extensions.
 * 
 * @author KennuX
 *
 */
public abstract class APlugin
{
	/**
	 * <pre>
	 * Load all you entity types in here and add them to the entity system.
	 * 
	 * Called on client and server.
	 * </pre>
	 */
	public abstract void defineEntityTypes();

	/**
	 * <pre>
	 * Define all your input key actions in here.
	 * </pre>
	 */
	public abstract void defineInputHandlers(InputManager inputManager);

	/**
	 * <pre>
	 * Register all your item types in here.
	 * Use ItemSystem.registerItemType().
	 * </pre>
	 */
	public abstract void defineItemTypes();

	/**
	 * <pre>
	 * Define all your packets from server -> client in here.
	 * 
	 * Called only on client.
	 * </pre>
	 */
	public abstract void defineProtocol();

	/**
	 * <pre>
	 * Loads all voxel types for this plugin.
	 * Register them to VoxelEngine with registerType().
	 * 
	 * Called on client and server.
	 * </pre>
	 */
	public abstract void defineVoxelTypes();

	/**
	 * <pre>
	 * Define all your gui windows in here.
	 * 
	 * Called only on client.
	 * </pre>
	 */
	public abstract void initializeGuiManager(GuiManager guiManager);

	/**
	 * <pre>
	 * Load your XMLOverlayLoader object loader's in here.
	 * This function may will get used to load other things in future!
	 * 
	 * Called only on client.
	 * </pre>
	 */
	public abstract void initializeGuiSystem();

	/**
	 * <pre>
	 * Gets called when this plugin should load it's models.
	 * There is no need to register them to anything.
	 * You can use them later to set mesh rendered voxel types.
	 * 
	 * Called only on client.
	 * </pre>
	 */
	public abstract void loadModels();

	/**
	 * <pre>
	 * Gets called when this plugin should load it's sounds.
	 * 
	 * Called only on client.
	 * </pre>
	 */
	public abstract void loadSounds();

	// Bootstrap
	/**
	 * <pre>
	 * Gets called when this plugin should load it's textures and add them to
	 * the voxel engine or item system.
	 * 
	 * Called only on client.
	 * </pre>
	 */
	public abstract void loadTextures();
	
	/**
	 * Gets called last after all other methods of this class.
	 * Prepare anything else you need in here.
	 */
	public abstract void postInit(boolean isServer);
}
