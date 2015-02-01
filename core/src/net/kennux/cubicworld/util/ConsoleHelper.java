package net.kennux.cubicworld.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Console helper functions.
 * 
 * TODO Add logging to file.
 * 
 * @author KennuX
 *
 */
public class ConsoleHelper
{
	/**
	 * Logs a stacktrace to the console.
	 */
	public static void logError(Exception e)
	{
		if (silent)
			return;

		e.printStackTrace();
	}

	/**
	 * Writes a log to the console in the format
	 * [type][date and time][module]: message
	 * 
	 * @param type
	 * @param message
	 * @param module
	 */
	public static void writeLog(String type, String message, String module)
	{
		type = type.toUpperCase();

		if (silent && type != "ERROR")
			return;
		System.out.println("[" + type + "][" + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()) + "][" + module + "]: " + message);
	}

	public static boolean silent = false;
}
