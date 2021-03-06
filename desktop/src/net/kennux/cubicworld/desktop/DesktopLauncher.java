package net.kennux.cubicworld.desktop;

import java.io.IOException;

import net.kennux.cubicworld.Bootstrap;
import net.kennux.cubicworld.CubicWorldConfiguration;
import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.CubicWorldServer;
import net.kennux.cubicworld.ServerBootstrap;
import net.kennux.cubicworld.util.ConsoleHelper;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher
{
	public static CubicWorldServer server;
	
	public static void main (String[] arg)
	{
		try
		{
			System.in.read();
		}
		catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// Pre-init
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
		
		Bootstrap.preInitialize();
		ServerBootstrap.preInitialize();
		
		//if (arg.length == 1 && arg[0].equals("-server"))
		{
			server = new CubicWorldServer((short)13371, "0.1", 6);
		}
		//else
		{
			final LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

			config.foregroundFPS = 60;
			config.vSyncEnabled = true;
			config.resizable = false;
			config.width = 1024;
			config.height = 768;
			config.depth = 32;
			
			new LwjglApplication(new CubicWorldGame(), config);
		} 
	}
}