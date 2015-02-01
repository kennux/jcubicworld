package net.kennux.cubicworld;

import java.io.File;
import java.io.FileOutputStream;

import net.kennux.cubicworld.networking.CubicWorldServerClient;
import net.kennux.cubicworld.serialization.BitWriter;
import net.kennux.cubicworld.util.ConsoleHelper;

/**
 * <pre>
 * The server save thread handles user, entity and voxel saving.
 * The voxel save file takes chunk / voxel updates and enquenes them for the next save.
 * This thread flushes this quenes in a given interval.
 * </pre>
 * 
 * @author KennuX
 *
 */
public class CubicWorldServerSaveThread implements Runnable
{
	/**
	 * The cubic world server instance.
	 */
	private CubicWorldServer server;

	/**
	 * The millisecond timestamp when the last save was performed.
	 */
	private long lastSave;

	/**
	 * Save interval in milliseconds.
	 */
	private final int saveInterval = 1000;

	public CubicWorldServerSaveThread(CubicWorldServer server)
	{
		this.server = server;
	}

	@Override
	public void run()
	{
		while (server.isRunning())
		{
			// Save
			if (System.currentTimeMillis() - this.lastSave >= this.saveInterval)
			{
				try
				{
					// Save user info
					synchronized (this.server.clientsLockObject)
					{
						for (CubicWorldServerClient client : this.server.clients)
						{
							if (client != null)
								client.saveUserInfo();
						}
					}

					// Save entity manager state
					BitWriter writer = new BitWriter();
					this.server.entityManager.serialize(writer);

					// Write state to entity data file
					FileOutputStream fos = new FileOutputStream(new File(this.server.savePath + "entities.dat"));
					fos.write(writer.getPacket());
					fos.close();

					// Update world file
					this.server.voxelWorld.flushSave();
				}
				catch (Exception e)
				{
					e.printStackTrace();
					ConsoleHelper.writeLog("ERROR", "Error while writing savegame: ", "Server");
					ConsoleHelper.logError(e);
					System.exit(-1);
				}
				this.lastSave = System.currentTimeMillis();
			}

			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{ /* NOT EXPECTED */
			}
		}
	}
}
