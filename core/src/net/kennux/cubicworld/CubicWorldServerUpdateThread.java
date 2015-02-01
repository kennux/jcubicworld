package net.kennux.cubicworld;

import java.util.ArrayList;

import net.kennux.cubicworld.entity.AEntity;
import net.kennux.cubicworld.networking.CubicWorldServerClient;
import net.kennux.cubicworld.networking.IPacketModel;
import net.kennux.cubicworld.networking.packet.ServerEntitySpawn;
import net.kennux.cubicworld.networking.packet.ServerEntityUpdate;
import net.kennux.cubicworld.util.ConsoleHelper;
import net.kennux.cubicworld.util.Mathf;

import com.badlogic.gdx.math.Vector3;

/**
 * Cubic world server update thread runnable.
 * 
 * @author KennuX
 *
 */
public class CubicWorldServerUpdateThread implements Runnable
{
	/**
	 * The cubic world server instance.
	 */
	private CubicWorldServer server;

	/**
	 * If a tick took more time than it should to compensate lag deltatime will get set to max time per tick and the milliseconds left are added to this long.
	 * If a tick then took less time tan it could, this value will get decremented again.
	 * 
	 * IMPORTANT: This is an experimental feature!
	 */
	private long millisBehind;

	public CubicWorldServerUpdateThread(CubicWorldServer server)
	{
		this.server = server;
	}

	/**
	 * <pre>
	 * Executes the updates in the rate given in ticksPerSecond.
	 * Update execution flow:
	 * 
	 * - Client entity updates
	 * - Player socket update / cleanup
	 * - World simulation
	 * - World update
	 * - World cleanup
	 * - Packet sending
	 * - Entity updating
	 * - Day/night cycle
	 * 
	 * </pre>
	 * 
	 */
	@Override
	public void run()
	{
		while (server.isRunning())
		{
			long millis = System.currentTimeMillis();

			server.profiler.startProfiling("Playersocket update / slot cleanup", "");
			// Update player sockets
			synchronized (server.clientsLockObject)
			{
				for (int i = 0; i < server.clients.length; i++)
				{
					// Cleanup
					if (server.clients[i] != null && !server.clients[i].isAlive())
					{
						ConsoleHelper.writeLog("error", "Dropped connection of socket in slot " + i, "Server");

						if (server.clients[i].isLoggedin())
						{
							this.server.destroyEntity(server.clients[i].playerEntity);
						}

						server.clients[i].close();
						server.clients[i] = null;
					}
					else if (server.clients[i] != null)
					{
						server.clients[i].update();
					}
				}
			}
			server.profiler.stopProfiling("Playersocket update / slot cleanup");

			server.profiler.startProfiling("Server Client Entity Interpolation", "");

			// Update all client entities (interpolates their position
			// instantly)
			for (CubicWorldServerClient client : server.clients)
			{
				if (client != null && client.isLoggedin())
					client.playerEntity.interpolatePosition(true);
			}

			server.profiler.stopProfiling("Server Client Entity Interpolation");

			server.profiler.startProfiling("Server WorldSimulation", "");
			// Simulate world
			server.voxelWorld.simulate();
			server.profiler.stopProfiling("Server WorldSimulation");
			server.profiler.startProfiling("Server WorldUpdate", "");
			server.voxelWorld.update();
			server.profiler.stopProfiling("Server WorldUpdate");

			// Get player positions
			ArrayList<Vector3> playerPositions = new ArrayList<Vector3>();
			for (CubicWorldServerClient player : server.clients)
			{
				if (player != null && player.isLoggedin())
				{
					playerPositions.add(player.playerEntity.getPosition());
				}
			}

			Vector3[] playerPositionsArray = playerPositions.toArray(new Vector3[playerPositions.size()]);

			server.profiler.startProfiling("Server World Cleanup", "");
			// Cleanup
			server.voxelWorld.cleanup(playerPositionsArray, CubicWorldConfiguration.chunkLoadDistance);
			server.profiler.stopProfiling("Server World Cleanup");

			// Send packets
			synchronized (server.packetStackLockObject)
			{
				server.profiler.startProfiling("Server Packet Sending", "Packetcount: " + server.packets.size());
				while (!server.packets.isEmpty())
				{
					IPacketModel packet = server.packets.pop();

					// Send packet
					int playerId = packet.getPlayerId();
					if (playerId == -1)
					{
						// Broadcast
						synchronized (server.clientsLockObject)
						{
							for (CubicWorldServerClient client : server.clients)
							{
								if (client != null && client.isLoggedin())
									client.sendPacket(packet);
							}
						}
					}
					else if (playerId == -2)
					{
						// Distance-culled Broadcast
						synchronized (server.clientsLockObject)
						{
							for (CubicWorldServerClient client : server.clients)
							{
								// Client loggedin and in update distance?
								if (client != null && client.isLoggedin() && new Vector3(client.playerEntity.getPosition()).sub(packet.getCullPosition()).len() <= packet.getCullDistance())
								{
									client.sendPacket(packet);
								}
							}
						}
					}
					else
					{
						// Single user packet.
						synchronized (server.clientsLockObject)
						{
							if (server.clients[packet.getPlayerId()] != null && server.clients[packet.getPlayerId()].isLoggedin())
								server.clients[packet.getPlayerId()].sendPacket(packet);
						}
					}
				}
				server.profiler.stopProfiling("Server Packet Sending");
			}

			// Entity update routine
			// First will update() all entities in the entitymanager
			// Then it will iterate thorough every client playerentity and
			// constructs the entity update or spawn packet based on the
			// knowsabout return value.
			// Then it will do the same thing for all entities in entity
			// manager.
			synchronized (server.clientsLockObject)
			{
				AEntity entities[] = null;

				// Get entity list
				synchronized (server.entityManagerLockObject)
				{
					this.server.entityManager.update();
					entities = this.server.entityManager.getEntityArray();
				}

				server.profiler.startProfiling("Server Entity Sync", "");
				// For every client
				for (int clientId = 0; clientId < this.server.clients.length; clientId++)
				{
					CubicWorldServerClient client = this.server.clients[clientId];

					// Only if client was loggedin
					if (client != null && client.isLoggedin())
					{
						// Player updates
						for (int i = 0; i < server.clients.length; i++)
						{
							CubicWorldServerClient otherClient = server.clients[i];

							// Already connected and loggedin?
							if (i != clientId && otherClient != null && otherClient.isLoggedin())
							{
								boolean knowsAbout = client.checkIfPlayerKnowsAbout(otherClient.playerEntity);

								// Check if player knows about and in entity
								// view distancePerform distance culling
								if (knowsAbout || otherClient.playerEntity.isInEntityViewDistance(client.playerEntity))
								{
									if (knowsAbout)
									{
										// Add to update entity list
										ServerEntityUpdate entityUpdate = new ServerEntityUpdate();
										entityUpdate.entity = otherClient.playerEntity;
										entityUpdate.setPlayerId(clientId);

										this.server.addPacket(entityUpdate);
									}
									else
									{
										// Send spawn packet
										ServerEntitySpawn spawnPacket = new ServerEntitySpawn();

										// Init spawn packet
										spawnPacket.entity = otherClient.playerEntity;
										spawnPacket.setPlayerId(clientId);

										// Knows about now
										client.addEntityToKnowsAbout(otherClient.playerEntity);

										this.server.addPacket(spawnPacket);
									}
								}
								else
								{
									client.removeEntityFromKnowsAbout(otherClient.playerEntity);
								}
							}
						}

						// Entity updates
						if (entities != null)
							for (int i = 0; i < entities.length; i++)
							{
								AEntity entity = entities[i];

								// Check if player knows about and in entity
								// view distance. Perform distance culling
								if (entity != null)
								{
									boolean knowsAbout = client.checkIfPlayerKnowsAbout(entity);

									if (knowsAbout || entity.isInEntityViewDistance(client.playerEntity))
									{
										if (knowsAbout)
										{
											// Add to update entity list
											ServerEntityUpdate entityUpdate = new ServerEntityUpdate();
											entityUpdate.entity = entity;
											entityUpdate.setPlayerId(clientId);

											this.server.addPacket(entityUpdate);
										}
										else
										{
											// Send spawn packet
											ServerEntitySpawn spawnPacket = new ServerEntitySpawn();

											// Init spawn packet
											spawnPacket.entity = entity;
											spawnPacket.setPlayerId(clientId);

											// Knows about now
											client.addEntityToKnowsAbout(entity);

											this.server.addPacket(spawnPacket);
										}
									}
								}
								else
								{
									client.removeEntityFromKnowsAbout(entity);
								}
							}
					}
				}
				server.profiler.stopProfiling("Server Entity Sync");
			}

			server.profiler.startProfiling("Server Daynight Cycle", "");
			// Update time
			this.server.dayNightCycle.tick();
			server.profiler.stopProfiling("Server Daynight Cycle");

			// Update plugins
			this.server.profiler.startProfiling("Plugin updates", "");
			this.server.pluginManager.fireEvent("update", true);
			this.server.profiler.stopProfiling("Plugin updates");

			server.profiler.startProfiling("Entity Cleanup", "");
			// Entity cleanup
			this.server.entityManager.cleanup(playerPositionsArray);
			server.profiler.stopProfiling("Entity Cleanup");

			// Write update packet.
			this.server.addPacket(this.server.dayNightCycle.getTimeUpdatePacket());

			this.server.tick++;

			// Delta time and lag compensation calculation
			// Lag compensation works like this:
			// If a lag occurs (deltaTime bigger than it should be), the milliseconds the server is behind gets counted.
			// Then if for example a tick only takes 1 ms while 200ms behind, the deltatime will be 50 and the server then is just 150ms behind.
			int deltaTime = (int) (System.currentTimeMillis() - millis);
			int millisPerTick = (1000 / server.ticksPerSecond);
			int spareTime = millisPerTick - deltaTime;

			// Delta time more than it should be?
			if (deltaTime > millisPerTick)
			{
				// Server is hanging behind :/
				this.millisBehind = deltaTime - millisPerTick;
				deltaTime = millisPerTick;
			}
			// Or less than it could be and is millis behind?
			else if (deltaTime < millisPerTick && this.millisBehind > 0)
			{
				this.millisBehind -= spareTime;

				if (this.millisBehind < 0)
					this.millisBehind = 0;

				deltaTime = deltaTime + (int) Mathf.min(spareTime, this.millisBehind);
			}
			// ConsoleHelper.writeLog("info", "Delta time: " + deltaTime + ", Millis behind: " + this.millisBehind, "Server");

			try
			{
				if (spareTime > 0)
					Thread.sleep(spareTime);
				/*
				 * else
				 * ConsoleHelper.writeLog("info", "Can't keep up! Delta time: " + deltaTime + ", Millis behind: " + this.millisBehind, "Server");
				 */
			}
			catch (InterruptedException e)
			{
			}
			this.server.deltaTime = deltaTime;

			server.profiler.reset();
		}
	}

}
