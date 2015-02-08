package net.kennux.cubicworld.admin;

import java.util.HashMap;

/**
 * The admin system handles chat command registration and permissions.
 * 
 * This is a server-only component.
 * 
 * @author KennuX
 *
 */
public class AdminSystem
{
	/**
	 * The command name -> command instance map.
	 */
	private static HashMap<String, IChatCommand> commands = new HashMap<String, IChatCommand>();

	/**
	 * Registers a command for the given name.
	 * 
	 * @param name
	 * @param command
	 */
	public static void registerCommand(String name, IChatCommand command)
	{
		commands.put(name, command);
	}

	/**
	 * Returns the chat command for the given name.
	 * Will return null if the chat command is not found.
	 * 
	 * @param name
	 * @return
	 */
	public static IChatCommand getCommand(String name)
	{
		return commands.get(name);
	}
}
