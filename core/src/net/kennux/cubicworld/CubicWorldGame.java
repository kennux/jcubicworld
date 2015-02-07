package net.kennux.cubicworld;

import java.io.IOException;

import net.kennux.cubicworld.entity.EntityManager;
import net.kennux.cubicworld.entity.PlayerController;
import net.kennux.cubicworld.environment.DayNightCycle;
import net.kennux.cubicworld.environment.Skybox;
import net.kennux.cubicworld.gui.GuiManager;
import net.kennux.cubicworld.input.GameInputProcessor;
import net.kennux.cubicworld.input.InputManager;
import net.kennux.cubicworld.networking.CubicWorldClient;
import net.kennux.cubicworld.networking.packet.ClientChunkRequest;
import net.kennux.cubicworld.profiler.Profiler;
import net.kennux.cubicworld.profiler.ProfilerResult;
import net.kennux.cubicworld.util.ConsoleHelper;
import net.kennux.cubicworld.util.DebugHelper;
import net.kennux.cubicworld.util.ShaderLoader;
import net.kennux.cubicworld.voxel.RaycastHit;
import net.kennux.cubicworld.voxel.VoxelWorld;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.Quaternion;

/**
 * The game's main class.
 * 
 * @author kennux
 *
 */
public class CubicWorldGame implements ApplicationListener
{
	/**
	 * The perspective camera used for rendering.
	 */
	public PerspectiveCamera cam;

	/**
	 * The input processor
	 */
	private InputMultiplexer inputMultiplexer;

	/**
	 * The input processor
	 */
	private GameInputProcessor gameInputProcessor;

	/**
	 * The voxel world instance.
	 */
	public VoxelWorld voxelWorld;

	/**
	 * The entity manager
	 */
	public EntityManager entityManager;

	/**
	 * Contains the last rendered final frame buffer.
	 */
	public FrameBuffer finalFrameTexture;

	/**
	 * The framebuffer which will get used in the main rendering pass.
	 */
	private FrameBuffer frameBuffer;

	/**
	 * Gets used to draw the framebuffer after the main rendering (before the gui rendering pass) pass.
	 */
	private SpriteBatch frameBufferBatch;

	/**
	 * The shader which gets used to blit textures on the screen.
	 */
	private ShaderProgram blitShader;

	/**
	 * The client socket.
	 */
	public CubicWorldClient client;

	/**
	 * The player controller.
	 */
	public PlayerController playerController;

	/**
	 * The skybox.
	 */
	private Skybox skybox;

	/**
	 * The day night cycle instance.
	 */
	public DayNightCycle dayNightCycle;

	/**
	 * The gui manager
	 */
	public GuiManager guiManager;

	/**
	 * When this is set to true a current test (post processing AA) will be
	 * active.
	 */
	public boolean postProcessing = false;

	/**
	 * The decal batch for entity rendering.
	 */
	private DecalBatch decalBatch;

	/**
	 * The model batch used to draw the entities
	 */
	private ModelBatch entityBatch;

	/**
	 * The sprite batch used for entity rendering
	 */
	private SpriteBatch entitySpriteBatch;

	/**
	 * Font used to draw strings in entity rendering.
	 */
	private BitmapFont entityFont;

	/**
	 * Contains the current block hit by raycasting from screen's center towards
	 * forward axis of the camera.
	 * Raycast will be performed in the update block in the render() method.
	 */
	public RaycastHit currentBlockHit;

	/**
	 * If this is set to true DebugHelper.renderDebugInformation() will get called.
	 */
	public boolean debugActive = true;

	/**
	 * The profiler used by this instance.
	 */
	public Profiler profiler;

	/**
	 * The plugin manager of the game.
	 */
	public PluginManager pluginManager;

	/**
	 * The input manager for this instance.
	 */
	public InputManager inputManager;

	public CubicWorldGame()
	{
		if (CubicWorld.getClient() != null)
			System.exit(-1);

		CubicWorld.setClient(this);
	}

	/**
	 * <pre>
	 * Creates this instance.
	 * Will initialize things in the following order:
	 * 
	 * - Profiler
	 * - Gui manager
	 * - Bootstrap initialization
	 * - Day night cycle
	 * - voxel world
	 * - player controller
	 * - client socket
	 * - input processor
	 * - post processing
	 * - rendering batches
	 * 
	 * This function gets profiled. It will be in the frame -1 on the client's
	 * profiler log.
	 * </pre>
	 */
	@Override
	public void create()
	{
		// Init Profiler
		GLProfiler.enable();
		this.profiler = new Profiler();
		try
		{
			this.profiler.openProfilingFile("client_profiling.txt", Profiler.FileFormat.PLAINTEXT);
		}
		catch (IOException e)
		{
			ConsoleHelper.writeLog("error", "Profiler init failed!", "CubicWorld Main");
			ConsoleHelper.logError(e);
		}

		this.profiler.startProfiling("Init()", "None");

		// Init gui
		this.guiManager = new GuiManager(null);

		// Bootstrap
		this.pluginManager = new PluginManager();
		this.inputManager = new InputManager();
		Bootstrap.bootstrap(this, this.pluginManager);

		// Create cam
		this.setupCamera();

		// Create day night cycle instance
		this.dayNightCycle = new DayNightCycle();

		// Create voxel world
		this.voxelWorld = new VoxelWorld(ShaderLoader.loadShader("world"), this.cam);

		// Init player controller, initial position will get set in the
		// ServerPlayerSpawn packet
		this.playerController = new PlayerController(this.voxelWorld, this.cam);

		// Connect socket
		try
		{
			this.client = new CubicWorldClient(this, "127.0.0.1", (short) 13371);
			this.client.update(this.playerController.getPosition());
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			ConsoleHelper.logError(e);
			System.exit(-1);
		}
		
		// Wait till all chunk requests are processed
		while(ClientChunkRequest.areRequestsPending())
		{
			this.client.waitForChunkPackets();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// Generate all chunk meshes
		int generationPerFrameLimit = CubicWorldConfiguration.meshGenerationsPerFrameLimit;
		int creationPerFrameLimit = CubicWorldConfiguration.meshCreationsPerFrameLimit;
		CubicWorldConfiguration.meshGenerationsPerFrameLimit = -1;
		CubicWorldConfiguration.meshCreationsPerFrameLimit = -1;
		
		// Wait till all chunks are ready
		while(!this.voxelWorld.allChunksReady())
		{
			this.voxelWorld.update();
			this.voxelWorld.render(this.cam);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		CubicWorldConfiguration.meshGenerationsPerFrameLimit = generationPerFrameLimit;
		CubicWorldConfiguration.meshGenerationsPerFrameLimit = creationPerFrameLimit;
		
		// System.exit(1);
		
		// Start update thread AFTER the client requested all chunks
		this.voxelWorld.initUpdateThread();

		// Setup input multiplexer
		this.inputMultiplexer = new InputMultiplexer();
		Gdx.input.setInputProcessor(this.inputMultiplexer);

		// Add input processors
		this.gameInputProcessor = new GameInputProcessor(this);
		this.inputMultiplexer.addProcessor(this.gameInputProcessor);
		this.inputMultiplexer.addProcessor(this.guiManager.getInputProcessor());

		// Create framebuffer
		this.frameBuffer = new FrameBuffer(Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
		this.frameBuffer.getColorBufferTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		this.finalFrameTexture = new FrameBuffer(Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
		this.finalFrameTexture.getColorBufferTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
		this.frameBufferBatch = new SpriteBatch();
		this.entityManager = new EntityManager(this.voxelWorld);

		// Init skybox
		this.skybox = new Skybox(Gdx.files.internal("textures/skybox/left.png"), Gdx.files.internal("textures/skybox/right.png"), Gdx.files.internal("textures/skybox/top.png"), Gdx.files.internal("textures/skybox/bottom.png"), Gdx.files.internal("textures/skybox/front.png"), Gdx.files.internal("textures/skybox/back.png"));

		// Init batches
		this.entityBatch = new ModelBatch();
		this.entitySpriteBatch = new SpriteBatch();
		this.decalBatch = new DecalBatch(new CameraGroupStrategy(this.cam));
		this.entityFont = new BitmapFont();

		// Init shader
		this.blitShader = ShaderLoader.loadShader("blit");

		this.profiler.stopProfiling("Init()");
		this.profiler.reset();
	}
	
	private float lastTime;
	
	/**
	 * <pre>
	 * This is the cubic world main routine.
	 * First it will update everything in this order:
	 * 
	 * - update this.currentBlockHit by raycasting
	 * - Cleanup the entity manager
	 * - Call update() on all entities in range
	 * - Update the gui manager
	 * - Update the client socket (mainly interprets read packets and send the ones who are enquened).
	 * - World cleanup (Removes all chunks too far away from the player).
	 * - Player update (Handles the player movement)
	 * 
	 * Then it will render the following to the frambuffer used for
	 * post-processing:
	 * - Skybox with camera's rotation
	 * - World render
	 * - Entity render
	 * - Postprocessing if activated
	 * - Gui rendering pass
	 * - Profiler reset / update
	 * 
	 * Every section of this function gets profiled with the Profiler class.
	 * </pre>
	 */
	@Override
	public void render()
	{
		long nanos = System.nanoTime();
		this.profiler.startProfiling("Update", "The whole update part of the render() routine");

		// Raycast
		this.profiler.startProfiling("CurrentLookAtRaycast", this.cam.position.toString() + ", dir: " + this.cam.direction.toString() + ", Distance: " + 5);
		this.currentBlockHit = this.voxelWorld.raycast(this.cam.position, this.cam.direction, 5);
		this.profiler.stopProfiling("CurrentLookAtRaycast");

		// Update
		this.gameInputProcessor.update();

		this.profiler.startProfiling("Client Socket Update", "");
		this.client.update(this.playerController.getPosition());
		this.profiler.stopProfiling("Client Socket Update");

		this.profiler.startProfiling("Entity Update", "Cleanup and Update call");
		this.entityManager.cleanupUpdateTimeout(CubicWorldConfiguration.entityUpdateTimeout);
		this.entityManager.update(this.playerController.getPosition());
		this.profiler.stopProfiling("Entity Update");

		this.profiler.startProfiling("Gui Update", "");
		this.guiManager.update();
		this.profiler.stopProfiling("Gui Update");

		this.profiler.startProfiling("World cleanup", "");
		// Chunk cleanup
		this.voxelWorld.cleanup(this.cam.position, CubicWorldConfiguration.chunkLoadDistance);
		this.profiler.stopProfiling("World cleanup");

		this.profiler.startProfiling("Player update", "");
		// Player controller
		this.playerController.update();
		this.profiler.stopProfiling("Player update");

		this.profiler.startProfiling("Plugin updates", "");
		this.pluginManager.fireEvent("update", false);
		this.profiler.stopProfiling("Plugin updates");
		
		this.profiler.stopProfiling("Update");

		this.profiler.startProfiling("Render", "The whole rendering part of the render() routine");
		Gdx.gl20.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClearColor(1, 1, 0, 1);

		// Render to framebuffer
		this.frameBuffer.begin();

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		// Skybox pass
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);

		Quaternion rotation = new Quaternion();
		this.cam.view.inv().getRotation(rotation);

		this.profiler.startProfiling("Skybox render", "");
		this.skybox.render(rotation);
		this.profiler.stopProfiling("Skybox render");

		// World rendering pass
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

		// Debug rendering
		DebugHelper.renderDebug(this.cam, this);

		// Render world
		this.profiler.startProfiling("World render", "");
		this.voxelWorld.render(this.cam);
		this.profiler.stopProfiling("World render");

		// Render entities
		this.profiler.startProfiling("Entity render", "");
		this.entityBatch.begin(this.cam);
		this.entitySpriteBatch.begin();
		this.entityManager.render(this.cam, this.entityBatch, this.decalBatch, this.entitySpriteBatch, this.entityFont);
		this.entitySpriteBatch.end();
		this.entityBatch.end();
		this.decalBatch.flush();
		this.profiler.stopProfiling("Entity render");

		// Render world pass done

		// Stop rendering to the framebuffer
		this.frameBuffer.end();

		// INSERT POST-PROCESSING HERE

		// Render to final texture
		this.finalFrameTexture.begin();

		// Draw framebuffer
		this.frameBufferBatch.setShader(this.blitShader);
		this.frameBufferBatch.begin();
		this.frameBufferBatch.draw(this.frameBuffer.getColorBufferTexture(), 0, 0);
		this.frameBufferBatch.end();

		// World rendering done
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);

		this.profiler.startProfiling("Gui render", "");

		// GUI Rendering pass
		Gdx.gl.glEnable(GL20.GL_BLEND);
		this.guiManager.render();

		this.profiler.stopProfiling("Gui render");
		this.profiler.stopProfiling("Render");

		this.profiler.startProfiling("Other", "");
		// Render debug info
		if (this.debugActive)
		{
			this.profiler.startProfiling("Debug Render", "");
			DebugHelper.renderDebugInformation(this.cam, this.client);
			this.profiler.stopProfiling("Debug Render");
		}

		Gdx.gl.glDisable(GL20.GL_BLEND);

		// Rendering done
		this.finalFrameTexture.end();

		// Draw to screen
		this.frameBufferBatch.begin();
		this.frameBufferBatch.draw(this.finalFrameTexture.getColorBufferTexture(), 0, 0);
		this.frameBufferBatch.end();

		this.profiler.stopProfiling("Other");
		
		float frameResult = ((System.nanoTime() - nanos) / 1000000.0f);
		
		if (frameResult > 50 || Gdx.graphics.getDeltaTime() > 1)
		{
			ProfilerResult[] results = this.profiler.getResults();
			System.out.println("Delta time: " + Gdx.graphics.getDeltaTime());
			System.out.println("frame time: " + frameResult);
			System.out.println("last frame time: " + this.lastTime);
			System.out.println("Profiler trace: ");
			
			for (ProfilerResult result : results)
			{
				if (result.getMilliseconds() > 20)
					System.out.println(result.getName() + " - " + result.getMilliseconds() + " ms");
			}
			
			System.out.println("");
		}
		
		this.lastTime = frameResult;
		
		// Reset profiler
		this.profiler.reset();
		GLProfiler.reset();
	}

	/**
	 * Initializes the perspective camera used for rendering.
	 */
	private void setupCamera()
	{
		this.cam = new PerspectiveCamera(70, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		this.cam.near = 0.05f;
		this.cam.far = 600.0f;
		this.cam.fieldOfView = 100.0f;

		// this.cam.translate(new Vector3(0, 130, 10));
		this.cam.update(true);
	}

	@Override
	public void resize(int width, int height)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause()
	{
		
	}

	@Override
	public void resume()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose()
	{
		// TODO Auto-generated method stub
		
	}
}
