package net.kennux.cubicworld;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Stack;

import net.kennux.cubicworld.entity.AEntity;
import net.kennux.cubicworld.entity.EntityManager;
import net.kennux.cubicworld.entity.EntitySystem;
import net.kennux.cubicworld.environment.DayNightCycle;
import net.kennux.cubicworld.networking.CubicWorldServerClient;
import net.kennux.cubicworld.networking.IPacketModel;
import net.kennux.cubicworld.networking.packet.ServerEntityDestroy;
import net.kennux.cubicworld.networking.packet.ServerVoxelUpdate;
import net.kennux.cubicworld.profiler.Profiler;
import net.kennux.cubicworld.profiler.Profiler.FileFormat;
import net.kennux.cubicworld.serialization.BitReader;
import net.kennux.cubicworld.util.ConsoleHelper;
import net.kennux.cubicworld.voxel.VoxelData;
import net.kennux.cubicworld.voxel.VoxelWorld;
import net.kennux.cubicworld.voxel.VoxelWorldSave;
import net.kennux.cubicworld.voxel.generator.TestGenerator;
import net.kennux.cubicworld.voxel.generator.WorldGenerator;
import net.kennux.cubicworld.voxel.generator.noise.SimplexNoise3D;
import net.kennux.cubicworld.voxel.handlers.IVoxelDataUpdateHandler;

import com.badlogic.gdx.math.Vector3;

/**
 * The cubic world server implementation.
 * 
 * @author KennuX
 *
 */
public class CubicWorldServer implements Runnable
{
	/**
	 * The libgdx server socket instance.
	 */
	private ServerSocket serverSocket;

	/**
	 * The server thread. It will run the run() method.
	 */
	private Thread serverThread;

	/**
	 * The update thread.
	 */
	private Thread updateThread;

	/**
	 * The voxel world instance.
	 */
	public VoxelWorld voxelWorld;

	/**
	 * Ticks per second to execute.
	 */
	public final int ticksPerSecond = 20;

	/**
	 * The current tick.
	 * Gets incremented after every tick.
	 */
	public int tick = 0;

	/**
	 * The clients array contains all connected clients. If you access this
	 * value, you must lock / synchronize the clientsLockObject.
	 */
	public CubicWorldServerClient[] clients;
	public Object clientsLockObject = new Object();

	/**
	 * The current stack of packets to send in this frame. If you access this
	 * value, you must lock / synchronize the packetStackLockObject.
	 */
	public Stack<IPacketModel> packets;
	public Object packetStackLockObject = new Object();

	/**
	 * The entity manager.
	 */
	public EntityManager entityManager;
	public Object entityManagerLockObject = new Object();

	/**
	 * Day night cycle instance, will get initialized before the server update
	 * thread starts.
	 */
	public DayNightCycle dayNightCycle;

	/**
	 * The profiler instance used by this instance.
	 */
	public Profiler profiler;

	/**
	 * The deltatime of the server.
	 */
	public float deltaTime;

	/**
	 * The plugin manager of this server.
	 */
	public PluginManager pluginManager;

	/**
	 * The path to the folder where the savedata is located with ending slash.
	 */
	public String savePath;

	/**
	 * The save thread which will handle the server saves.
	 */
	private Thread saveThread;

	/**
	 * If this gets set to false, server threads will stop.
	 */
	private boolean isRunning;

	/**
	 * <pre>
	 * Initializes the server socket, starts listening.
	 * Initialization order:
	 * 
	 * - Singleton pattern
	 * - Profiler
	 * - Server Socket
	 * - Server Thread (Listener)
	 * - Bootstrap
	 * - Packet stack
	 * - Voxel World
	 * - Entity manager
	 * - Spawn area preparation (generating all chunks around 0|0|0 based on chunkViewDistance).
	 * - Update thread
	 * - Daynight cycle
	 * </pre>
	 * 
	 * @param protocol
	 * @param port
	 * @param version
	 */
	public CubicWorldServer(short port, String version, int slots)
	{
		if (CubicWorld.getServer() != null)
		{
			ConsoleHelper.writeLog("ERROR", "Server instance already initialized!", "Server Init");
			System.exit(-1);
		}

		CubicWorld.setServer(this);

		ConsoleHelper.writeLog("info", "Initializing CubicWorldServer...", "Server Init");

		// Init profiler
		this.profiler = new Profiler();
		try
		{
			this.profiler.openProfilingFile("server_profilings.txt", FileFormat.PLAINTEXT);
		}
		catch (IOException e)
		{
			ConsoleHelper.writeLog("error", "Couldn't initialize profiler!", "Server Profiler");
			ConsoleHelper.logError(e);
		}

		this.profiler.startProfiling("ServerInit()", "");

		// Init save path
		this.savePath = "world/";
		this.prepareSaveStructure();

		// Init socket
		try
		{
			this.serverSocket = new ServerSocket(port);
		}
		catch (IOException e)
		{
			ConsoleHelper.writeLog("error", "Couldn't initialize server socket on port " + port + "\r\n" + e.getMessage(), "Server Init");
			return;
		}

		this.serverThread = new Thread(this);

		ConsoleHelper.writeLog("info", "Executing bootstrap.", "Server Init");
		this.pluginManager = new PluginManager();
		ServerBootstrap.bootstrap(this.pluginManager);
		ConsoleHelper.writeLog("info", "Bootstrap executed.", "Server Init");

		// Create tick update thread
		this.packets = new Stack<IPacketModel>();

		// Init world
		this.voxelWorld = new VoxelWorld(this);
		this.voxelWorld.setVoxelDataUpdateHandler(new IVoxelDataUpdateHandler()
		{
			/**
			 * Constructs a voxel update packet and adds it to the server packet
			 * quene.
			 */
			@Override
			public void handleVoxelDataUpdate(int x, int y, int z, VoxelData newData)
			{
				// Construct chunk update
				ServerVoxelUpdate voxelUpdate = new ServerVoxelUpdate();
				voxelUpdate.setCullPosition(new Vector3(x, y, z));
				voxelUpdate.x = x;
				voxelUpdate.y = y;
				voxelUpdate.z = z;
				voxelUpdate.voxel = newData;

				CubicWorld.getServer().addPacket(voxelUpdate);
			}

		});

		SimplexNoise3D.seed(1337);

		this.voxelWorld.setWorldGenerator(new WorldGenerator());
		this.entityManager = new EntityManager(this.voxelWorld, true, true, slots);

		ConsoleHelper.writeLog("info", "Preparing spawn area...", "Server Init");

		try
		{
			this.voxelWorld.setWorldFile(new VoxelWorldSave(this.savePath));
		}
		catch (Exception e)
		{
			ConsoleHelper.writeLog("ERROR", "Voxel world save file initialization failed: ", "Server");
			ConsoleHelper.logError(e);
			System.exit(-1);
		}

		this.voxelWorld.generateChunksAround(Vector3.Zero, CubicWorldConfiguration.chunkLoadDistance, true);
		this.voxelWorld.update();

		// load entity manager save
		File entityFile = new File(this.savePath + "entities.dat");
		if (entityFile.length() > 0)
		{
			try
			{
				// Read entity save data
				FileInputStream entityFileIs = new FileInputStream(entityFile);
				byte[] data = new byte[(int) entityFile.length()];
				entityFileIs.read(data);

				// Deserialize
				this.entityManager.deserialize(new BitReader(data));
				entityFileIs.close();
			}
			catch (Exception e)
			{
				ConsoleHelper.writeLog("ERROR", "Entity save reading failed: ", "Server");
				ConsoleHelper.logError(e);
				System.exit(-1);
			}
		}
		ConsoleHelper.writeLog("info", "Spawn area prepared! Server running!", "Server Init");

		// Init client socket
		this.clients = new CubicWorldServerClient[slots];

		// Update thread init
		this.updateThread = new Thread(new CubicWorldServerUpdateThread(this));
		this.updateThread.setName("Server update thread");

		// init day night cycle
		this.dayNightCycle = new DayNightCycle();
		this.dayNightCycle.setTime((byte) 6, (byte) 0);

		this.profiler.stopProfiling("ServerInit()");

		// Init save thread
		this.saveThread = new Thread(new CubicWorldServerSaveThread(this));

		// Start threads
		this.isRunning = true;
		this.serverThread.start();
		this.updateThread.start();
		this.saveThread.start();
	}

	/**
	 * Adds a packet to the quene to send in the current frame.
	 */
	public void addPacket(IPacketModel packet)
	{
		synchronized (packetStackLockObject)
		{
			this.packets.push(packet);
		}
	}

	/**
	 * <pre>
	 * Destroys an entity.
	 * Sends out a ServerEntityDestroy packet to all players who know about the
	 * given entity.
	 * 
	 * This only sends out the entity destroy packet and remove the entity from
	 * all knows about lists.
	 * It will not handle any EntityManager related cleanups.
	 * Only call this for example for player entities.
	 * 
	 * If you want to immediately destroy an entity, use the entity's die()
	 * function which will remove it from every player knows about and send all
	 * needed updates.
	 * It will also them remove itself from the entity manager.
	 * </pre>
	 * 
	 * @param entity
	 */
	public void destroyEntity(AEntity entity)
	{
		// Send destroy entity update
		ServerEntityDestroy destroyPacket = new ServerEntityDestroy();
		destroyPacket.entityId = entity.getEntityId();
		destroyPacket.setCullPosition(new Vector3(entity.getPosition()));
		this.addPacket(destroyPacket);

		// Remove from knows about
		for (int i = 0; i < this.clients.length; i++)
		{
			if (this.clients[i] != null)
				this.clients[i].removeEntityFromKnowsAbout(entity);
		}
	}

	/**
	 * Returns a free slot index in the clients array. Free means null. Returns
	 * -1 if there is no free slot.
	 * 
	 * @return
	 */
	private int findFreeSlot()
	{
		// Iterate through all indices
		synchronized (this.clientsLockObject)
		{
			for (int i = 0; i < this.clients.length; i++)
			{
				if (this.clients[i] == null)
					return i;
			}
		}

		return -1;
	}

	/**
	 * @return the isRunning
	 */
	public boolean isRunning()
	{
		return isRunning;
	}

	/**
	 * Prepares the save path folder structure.
	 * It will create all needed folders and files and initializes the files if they aren't already.
	 */
	private void prepareSaveStructure()
	{
		// Main folder
		File f = new File(this.savePath);

		if (!f.exists())
		{
			f.mkdir();
		}

		// Players folder
		f = new File(this.savePath + "players/");

		if (!f.exists())
		{
			f.mkdir();
		}
	}

	/**
	 * The thread run function.
	 * Listens on the port given in the constructor.
	 * Will accept connections and add them to the clients[] socket.
	 * 
	 * The socket update thread will handle all the game logic.
	 */
	@Override
	public void run()
	{
		while (this.isRunning)
		{
			try
			{
				// Accept socket
				Socket socket = this.serverSocket.accept();

				if (socket == null)
				{
					ConsoleHelper.writeLog("error", "Got null socket!", "Server");
					break;
				}
				else
					ConsoleHelper.writeLog("info", "Got connection from " + socket.getRemoteSocketAddress(), "ServerSocket");

				// Free slot?
				int freeSlot = this.findFreeSlot();
				if (freeSlot == -1)
				{
					ConsoleHelper.writeLog("info", "Connection from " + socket.getRemoteSocketAddress() + " dropped (no free slot)!", "ServerSocket");
					socket.close();
					continue;
				}

				// We got a free slot
				ConsoleHelper.writeLog("info", "Connection from " + socket.getRemoteSocketAddress() + " accepted (slot " + freeSlot + ")!", "ServerSocket");
				this.clients[freeSlot] = new CubicWorldServerClient(this, socket, freeSlot);
			}
			catch (IOException e)
			{
				ConsoleHelper.writeLog("error", "IOException in server main loop: " + e.getMessage(), "Server Main");
			}
		}
	}

	/**
	 * <pre>
	 * Spawns the entity with the given type id and sets its position to the
	 * given position.
	 * This function is only to simplify things.
	 * Spawning an entity works like this:
	 * 
	 * 1. Retrieve free id from entity manager (entityManager.getNextFreeId())
	 * 2. Instantiate entity by new EntityClass() or
	 * EntitySystem.instantiateEntity(typeId)
	 * 3. Add the instance with the given id to the entitymanager by add().
	 * 
	 * </pre>
	 * 
	 * @param entityTypeId
	 * @param position
	 */
	public void spawnEntity(int entityTypeId, Vector3 position)
	{
		synchronized (this.entityManagerLockObject)
		{
			// Get new entity id
			int entityId = this.entityManager.getNextFreeId();

			// Instantiate entity
			AEntity entity = EntitySystem.instantiateEntity(entityTypeId);
			entity.setPosition(position);

			// Add to entity manager
			this.entityManager.add(entityId, entity);
		}
	}

	/**
	 * Stops this server.
	 */
	public void stop()
	{
		// Terminate!
		this.isRunning = false;

		try
		{
			this.saveThread.join();
			this.updateThread.join();
			this.serverThread.join();
		}
		catch (Exception e)
		{
			// Ignore!
		}
	}
}
