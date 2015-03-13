package net.kennux.cubicworld.admin;

import net.kennux.cubicworld.networking.CubicWorldServerClient;

/**
 * Use this interface for implementing chat commands.
 * A chatcommand has this structure:
 * 
 * /[COMMAND_NAME] [ARGUMENTS]...
 * 
 * Arguments are separated by spaces.
 * 
 * @author KennuX
 *
 */
public interface IChatCommand
{
	/**
	 * Gets called if a user sends a command.
	 * 
	 * @param sender The user who sent this command.
	 * @param args The command arguments.
	 */
	public void executeCommand(CubicWorldServerClient sender, String[] args);
}
