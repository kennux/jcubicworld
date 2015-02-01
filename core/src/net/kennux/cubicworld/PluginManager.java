package net.kennux.cubicworld;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.kennux.cubicworld.gui.GuiManager;
import net.kennux.cubicworld.input.InputManager;
import net.kennux.cubicworld.pluginapi.APlugin;
import net.kennux.cubicworld.pluginapi.annotations.Event;
import net.kennux.cubicworld.pluginapi.annotations.PluginInfo;
import net.kennux.cubicworld.util.ConsoleHelper;

/**
 * 
 * @author KennuX
 *
 */
public class PluginManager
{
	/**
	 * The plugins list loaded in preInitialize().
	 */
	public ArrayList<APlugin> plugins;

	/**
	 * Contains all methods which were marked as events in the plugin class.
	 */
	private HashMap<String, ArrayList<AbstractMap.SimpleEntry<APlugin, Method>>> events = new HashMap<String, ArrayList<AbstractMap.SimpleEntry<APlugin, Method>>>();;

	private HashMap<APlugin, PluginInfo> pluginInfos = new HashMap<APlugin, PluginInfo>();

	private final String[] knownEvents = new String[] { "update" };

	public void defineEntityTypes()
	{
		for (APlugin plugin : plugins)
		{
			plugin.defineEntityTypes();
		}
	}

	public void defineInputHandlers(InputManager inputManager)
	{
		for (APlugin plugin : plugins)
		{
			plugin.defineInputHandlers(inputManager);
		}
	}

	public void defineItemTypes()
	{
		for (APlugin plugin : plugins)
		{
			plugin.defineItemTypes();
		}
	}

	public void defineProtocol()
	{
		for (APlugin plugin : plugins)
		{
			plugin.defineProtocol();
		}
	}

	public void defineVoxelTypes()
	{
		for (APlugin plugin : plugins)
		{
			plugin.defineVoxelTypes();
		}
	}

	/**
	 * Fires all registered event handlers for the given event name.
	 * 
	 * @param eventName
	 * @param params
	 */
	public void fireEvent(String eventName, Object... params)
	{
		// Get all methods for this event.
		ArrayList<SimpleEntry<APlugin, Method>> methods = events.get(eventName);

		for (Entry<APlugin, Method> m : methods)
		{
			try
			{
				m.getValue().invoke(m.getKey(), params);
			}
			catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
			{
				ConsoleHelper.writeLog("ERROR", "Error while firing event: " + eventName, "PluginManager");
				ConsoleHelper.logError(e);
			}
		}
	}

	public void initialize(ArrayList<APlugin> plugins)
	{
		// Set plugins reference
		this.plugins = plugins;

		// Create event handlers
		List<String> knownEventNames = Arrays.asList(knownEvents);

		// Prepare events builder list
		for (String event : knownEvents)
		{
			events.put(event, new ArrayList<AbstractMap.SimpleEntry<APlugin, Method>>());
		}

		for (APlugin plugin : plugins)
		{
			if (!plugin.getClass().isAnnotationPresent(PluginInfo.class))
			{
				ConsoleHelper.writeLog("ERROR", "Plugin without plugin annotation registered! Exiting...", "PluginManager");
				System.exit(-1);
			}

			// Write plugin info
			PluginInfo pluginInfo = plugin.getClass().getAnnotation(PluginInfo.class);
			pluginInfos.put(plugin, pluginInfo);

			for (Method m : plugin.getClass().getDeclaredMethods())
			{
				// Check if this method has the event annotation
				if (m.isAnnotationPresent(Event.class))
				{
					// Get the event annotation
					Event eventAnnotation = m.getAnnotation(Event.class);
					String eventType = eventAnnotation.eventType();

					// Check if this event is allowed / known
					if (knownEventNames.contains(eventType))
					{
						events.get(eventType).add(new AbstractMap.SimpleEntry<APlugin, Method>(plugin, m));
					}
					else
					{
						ConsoleHelper.writeLog("ERROR", "Plugin " + pluginInfo.pluginName() + " Tried to register event with unknown type: " + eventType, "PluginManager");
					}
				}
			}
		}
	}

	public void initializeGuiManager(GuiManager guiManager)
	{
		for (APlugin plugin : plugins)
		{
			plugin.initializeGuiManager(guiManager);
		}
	}

	public void initializeGuiSystem()
	{
		for (APlugin plugin : plugins)
		{
			plugin.initializeGuiSystem();
		}
	}

	public void loadModels()
	{
		for (APlugin plugin : plugins)
		{
			plugin.loadModels();
		}
	}

	public void loadSounds()
	{
		for (APlugin plugin : plugins)
		{
			plugin.loadSounds();
		}
	}

	// Plugin proxy functions
	public void loadTextures()
	{
		for (APlugin plugin : plugins)
		{
			plugin.loadTextures();
		}
	}
}
