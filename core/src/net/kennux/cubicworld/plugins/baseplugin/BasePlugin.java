package net.kennux.cubicworld.plugins.baseplugin;

import java.io.IOException;
import java.util.zip.DataFormatException;

import net.kennux.cubicworld.CubicWorld;
import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.entity.EntitySystem;
import net.kennux.cubicworld.entity.ItemEntity;
import net.kennux.cubicworld.entity.PlayerEntity;
import net.kennux.cubicworld.entity.TestEntity;
import net.kennux.cubicworld.gui.GuiManager;
import net.kennux.cubicworld.gui.IGuiElement;
import net.kennux.cubicworld.gui.IGuiOverlay;
import net.kennux.cubicworld.gui.events.IClickHandler;
import net.kennux.cubicworld.gui.hud.BlockSelectorGui;
import net.kennux.cubicworld.gui.hud.Chatbox;
import net.kennux.cubicworld.gui.hud.Crosshair;
import net.kennux.cubicworld.gui.hud.HudBlockInformation;
import net.kennux.cubicworld.gui.overlay.Overlay;
import net.kennux.cubicworld.gui.overlay.OverlayData;
import net.kennux.cubicworld.gui.overlay.XMLOverlayLoader;
import net.kennux.cubicworld.gui.skin.StandardSkin;
import net.kennux.cubicworld.input.IKeyInputHandler;
import net.kennux.cubicworld.input.InputManager;
import net.kennux.cubicworld.inventory.IInventory;
import net.kennux.cubicworld.item.ItemClass;
import net.kennux.cubicworld.item.ItemStack;
import net.kennux.cubicworld.item.ItemSystem;
import net.kennux.cubicworld.networking.Protocol;
import net.kennux.cubicworld.networking.packet.ChatMessage;
import net.kennux.cubicworld.networking.packet.ClientChunkRequest;
import net.kennux.cubicworld.networking.packet.ClientLogin;
import net.kennux.cubicworld.networking.packet.ClientPlayerUpdate;
import net.kennux.cubicworld.networking.packet.ClientVoxelUpdate;
import net.kennux.cubicworld.networking.packet.KeepAlive;
import net.kennux.cubicworld.networking.packet.ServerChunkData;
import net.kennux.cubicworld.networking.packet.ServerEntityDestroy;
import net.kennux.cubicworld.networking.packet.ServerEntitySpawn;
import net.kennux.cubicworld.networking.packet.ServerEntityUpdate;
import net.kennux.cubicworld.networking.packet.ServerPlayerSpawn;
import net.kennux.cubicworld.networking.packet.ServerTimeUpdate;
import net.kennux.cubicworld.networking.packet.ServerVoxelUpdate;
import net.kennux.cubicworld.networking.packet.inventory.ClientDropItem;
import net.kennux.cubicworld.networking.packet.inventory.ClientItemMove;
import net.kennux.cubicworld.networking.packet.inventory.ClientItemTransaction;
import net.kennux.cubicworld.networking.packet.inventory.ServerBlockInventoryUpdate;
import net.kennux.cubicworld.networking.packet.inventory.ServerPlayerInventoryUpdate;
import net.kennux.cubicworld.pluginapi.APlugin;
import net.kennux.cubicworld.pluginapi.annotations.Event;
import net.kennux.cubicworld.pluginapi.annotations.PluginInfo;
import net.kennux.cubicworld.plugins.baseplugin.gui.ChatOverlay;
import net.kennux.cubicworld.plugins.baseplugin.gui.XMLButtonLoader;
import net.kennux.cubicworld.plugins.baseplugin.gui.XMLImageLoader;
import net.kennux.cubicworld.plugins.baseplugin.gui.XMLInventorySlotLoader;
import net.kennux.cubicworld.plugins.baseplugin.gui.XMLInventoryViewLoader;
import net.kennux.cubicworld.plugins.baseplugin.gui.XMLTextboxLoader;
import net.kennux.cubicworld.plugins.baseplugin.input.BlockSelectorHudHandler;
import net.kennux.cubicworld.plugins.baseplugin.input.CameraMouseHandler;
import net.kennux.cubicworld.plugins.baseplugin.input.MovementKeyHandler;
import net.kennux.cubicworld.util.ConsoleHelper;
import net.kennux.cubicworld.util.DebugHelper;
import net.kennux.cubicworld.util.VectorHelper;
import net.kennux.cubicworld.voxel.RaycastHit;
import net.kennux.cubicworld.voxel.VoxelData;
import net.kennux.cubicworld.voxel.VoxelEngine;
import net.kennux.cubicworld.voxel.VoxelRenderState;
import net.kennux.cubicworld.voxel.handlers.IVoxelActionHandler;
import net.kennux.cubicworld.voxel.handlers.MachineUpdateHandler;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

@PluginInfo(pluginName = "BasePlugin", author = "KennuX")
public class BasePlugin extends APlugin
{
	// Textures
	public static Texture bedrock;
	public static Texture dirt;
	public static Texture glass;
	public static Texture grassTop;
	public static Texture grassSide;
	public static Texture leavesSpruce;
	public static Texture stone;
	public static Texture treeSpruce;
	public static Texture furnaceFrontTexture;
	public static Texture furnaceFrontLitTexture;
	public static Texture furnaceSideTexture;
	public static Texture furnaceTopTexture;
	public static Texture coal;

	// Texture ids
	public static int bedrockId = -1;
	public static int dirtId = -1;
	public static int grassTopId = -1;
	public static int glassId = -1;
	public static int grassSideId = -1;
	public static int leavesSpruceId = -1;
	public static int stoneId = -1;
	public static int treeSpruceId = -1;
	public static int furnaceFrontId = -1;
	public static int furnaceFrontLitId = -1;
	public static int furnaceSideId = -1;
	public static int furnaceTopId = -1;
	public static int coalId = -1;

	// Voxel ids
	public static short voxelBedrockId = -1;
	public static short voxelDirtId = -1;
	public static short voxelGlassId = -1;
	public static short voxelGrassId = -1;
	public static short voxelLeavesSpruceId = -1;
	public static short voxelStoneId = -1;
	public static short voxelTreeSpruceId = -1;
	public static short voxelFurnaceId = -1;

	// Overlay ids
	public static int mainMenuOverlayId = -1;
	public static int furnaceGuiOverlayId = -1;
	public static int playerInventoryOverlayId = -1;
	public static int chatOverlayId = -1;

	// Overlays
	public static Overlay playerInventoryOverlay;

	// Item ids
	public static int itemCoalId = -1;

	// Entity id
	public static int playerEntityId;
	public static int testEntityId;
	public static int itemEntityId;

	// Input movement controller
	public static InputMovementController inputMovementController;
	private static MovementKeyHandler leftMovementHandler;
	private static MovementKeyHandler rightMovementHandler;
	private static MovementKeyHandler forwardMovementHandler;
	private static MovementKeyHandler backwardMovementHandler;

	@Override
	public void defineEntityTypes()
	{
		playerEntityId = EntitySystem.registerEntity(PlayerEntity.class);
		testEntityId = EntitySystem.registerEntity(TestEntity.class);
		itemEntityId = EntitySystem.registerEntity(ItemEntity.class);
	}

	@Override
	public void defineInputHandlers(InputManager inputManager)
	{
		// Init movement controller
		inputMovementController = new InputMovementController();
		final InputMovementController movementController = inputMovementController;

		// Constants
		final float movementSpeed = 7.5f;

		// WASD Controls
		leftMovementHandler = new MovementKeyHandler()
		{
			@Override
			public void move()
			{
				Vector3 forward = CubicWorld.getClient().cam.direction;
				Vector3 left = new Vector3(CubicWorld.getClient().cam.up.x, CubicWorld.getClient().cam.up.y, CubicWorld.getClient().cam.up.z);
				left.crs(forward);

				movementController.move(VectorHelper.mulVectorScalar(left, movementSpeed));
			}
		};

		rightMovementHandler = new MovementKeyHandler()
		{

			@Override
			public void move()
			{
				Vector3 forward = CubicWorld.getClient().cam.direction;
				Vector3 left = new Vector3(CubicWorld.getClient().cam.up.x, CubicWorld.getClient().cam.up.y, CubicWorld.getClient().cam.up.z);
				left.crs(forward);

				movementController.move(VectorHelper.mulVectorScalar(left, -1.0f * movementSpeed));
			}
		};

		forwardMovementHandler = new MovementKeyHandler()
		{

			@Override
			public void move()
			{
				Vector3 forward = CubicWorld.getClient().cam.direction;

				movementController.move(VectorHelper.mulVectorScalar(forward, movementSpeed));
			}
		};

		backwardMovementHandler = new MovementKeyHandler()
		{

			@Override
			public void move()
			{
				Vector3 forward = CubicWorld.getClient().cam.direction;

				movementController.move(VectorHelper.mulVectorScalar(forward, -1.0f * movementSpeed));
			}
		};

		// Register the handlers to the input manager
		inputManager.addInputAction(Input.Keys.A, leftMovementHandler);
		inputManager.addInputAction(Input.Keys.D, rightMovementHandler);
		inputManager.addInputAction(Input.Keys.W, forwardMovementHandler);
		inputManager.addInputAction(Input.Keys.S, backwardMovementHandler);

		// register mouse input handler
		inputManager.setMouseInputHandler(new CameraMouseHandler());

		// Register the block gui handlers
		inputManager.addInputAction(Input.Keys.NUM_1, new BlockSelectorHudHandler(0));
		inputManager.addInputAction(Input.Keys.NUM_2, new BlockSelectorHudHandler(1));
		inputManager.addInputAction(Input.Keys.NUM_3, new BlockSelectorHudHandler(2));
		inputManager.addInputAction(Input.Keys.NUM_4, new BlockSelectorHudHandler(3));
		inputManager.addInputAction(Input.Keys.NUM_5, new BlockSelectorHudHandler(4));
		inputManager.addInputAction(Input.Keys.NUM_6, new BlockSelectorHudHandler(5));
		inputManager.addInputAction(Input.Keys.NUM_7, new BlockSelectorHudHandler(6));
		inputManager.addInputAction(Input.Keys.NUM_8, new BlockSelectorHudHandler(7));

		// Register player inventory handler
		inputManager.addInputAction(Input.Keys.I, new IKeyInputHandler()
		{

			@Override
			public void keyPressed(CubicWorldGame cubicWorld)
			{
				// Open inventory gui
				// Prepare overlay data
				OverlayData playerInventoryOverlayData = new OverlayData();
				playerInventoryOverlayData.put("playerInventory", cubicWorld.playerController.getPlayerInventory());

				// Set the overlay data
				BasePlugin.playerInventoryOverlay.setOverlayData(playerInventoryOverlayData);

				// Open the overlay
				cubicWorld.guiManager.openOverlay(BasePlugin.playerInventoryOverlayId);
			}

			@Override
			public void keyReleased(CubicWorldGame cubicWorld)
			{
			}
		});

		// Post processing test
		inputManager.addInputAction(Input.Keys.H, new IKeyInputHandler()
		{

			@Override
			public void keyPressed(CubicWorldGame cubicWorld)
			{
				cubicWorld.postProcessing = !cubicWorld.postProcessing;
			}

			@Override
			public void keyReleased(CubicWorldGame cubicWorld)
			{
			}
		});

		// Jumping
		inputManager.addInputAction(Input.Keys.SPACE, new IKeyInputHandler()
		{

			@Override
			public void keyPressed(CubicWorldGame cubicWorld)
			{
				if (cubicWorld.playerController.isGrounded())
					cubicWorld.playerController.impulse(new Vector3(0, 1, 0), 1.5f);
			}

			@Override
			public void keyReleased(CubicWorldGame cubicWorld)
			{
			}
		});

		// Debugging input handlers
		inputManager.addInputAction(Input.Keys.F3, new IKeyInputHandler()
		{

			@Override
			public void keyPressed(CubicWorldGame cubicWorld)
			{
				cubicWorld.debugActive = !cubicWorld.debugActive;
			}

			@Override
			public void keyReleased(CubicWorldGame cubicWorld)
			{
			}
		});

		inputManager.addInputAction(Input.Keys.F4, new IKeyInputHandler()
		{

			@Override
			public void keyPressed(CubicWorldGame cubicWorld)
			{
				DebugHelper.drawDiagrams = !DebugHelper.drawDiagrams;
			}

			@Override
			public void keyReleased(CubicWorldGame cubicWorld)
			{
			}
		});

		inputManager.addInputAction(Input.Keys.F5, new IKeyInputHandler()
		{

			@Override
			public void keyPressed(CubicWorldGame cubicWorld)
			{
				DebugHelper.drawNetworkDiagrams = !DebugHelper.drawNetworkDiagrams;
			}

			@Override
			public void keyReleased(CubicWorldGame cubicWorld)
			{
			}
		});

		inputManager.addInputAction(Input.Keys.ESCAPE, new IKeyInputHandler()
		{

			@Override
			public void keyPressed(CubicWorldGame cubicWorld)
			{
				cubicWorld.guiManager.openOverlay(BasePlugin.mainMenuOverlayId);
			}

			@Override
			public void keyReleased(CubicWorldGame cubicWorld)
			{
			}
		});

		inputManager.addInputAction(Input.Keys.F9, new IKeyInputHandler()
		{

			@Override
			public void keyPressed(CubicWorldGame cubicWorld)
			{
				DebugHelper.renderChunkBoundingBoxes = !DebugHelper.renderChunkBoundingBoxes;
			}

			@Override
			public void keyReleased(CubicWorldGame cubicWorld)
			{
			}
		});

		// CHAT
		inputManager.addInputAction(Input.Keys.C, new IKeyInputHandler()
		{

			@Override
			public void keyPressed(CubicWorldGame cubicWorld)
			{
				cubicWorld.guiManager.openOverlay(BasePlugin.chatOverlayId);
			}

			@Override
			public void keyReleased(CubicWorldGame cubicWorld)
			{
			}
		});

		// Open block inventory input handler
		inputManager.addInputAction(Input.Keys.T, new IKeyInputHandler()
		{

			@Override
			public void keyPressed(CubicWorldGame cubicWorld)
			{
				RaycastHit hitInfo = cubicWorld.currentBlockHit;
				Vector3 blockPos = hitInfo.hitVoxelPosition;

				VoxelData voxelData = cubicWorld.voxelWorld.getVoxel((int) blockPos.x, (int) blockPos.y, (int) blockPos.z);

				// Execute voxel action handler if there is one
				if (voxelData != null)
				{
					IVoxelActionHandler voxelActionHandler = voxelData.voxelType.getActionHandler();

					if (voxelActionHandler != null)
					{
						voxelActionHandler.handleAction(voxelData, blockPos);
					}
				}
			}

			@Override
			public void keyReleased(CubicWorldGame cubicWorld)
			{
			}
		});
	}

	@Override
	public void defineItemTypes()
	{
		// Fuels
		itemCoalId = ItemSystem.registerItemType("coal").setStackSize((byte) 64).setItemTexture(coalId).setType(ItemClass.ITEM).getItemId();
	}

	@Override
	public void defineProtocol()
	{
		Protocol.addPacket(new ServerChunkData());
		Protocol.addPacket(new ServerEntitySpawn());
		Protocol.addPacket(new ServerEntityDestroy());
		Protocol.addPacket(new ServerEntityUpdate());
		Protocol.addPacket(new ServerTimeUpdate());
		Protocol.addPacket(new ServerVoxelUpdate());
		Protocol.addPacket(new ServerPlayerSpawn());
		Protocol.addPacket(new KeepAlive());
		Protocol.addPacket(new ChatMessage());
		Protocol.addPacket(new ClientChunkRequest());
		Protocol.addPacket(new ClientLogin());
		Protocol.addPacket(new ClientPlayerUpdate());
		Protocol.addPacket(new ClientVoxelUpdate());
		Protocol.addPacket(new ClientDropItem());
		Protocol.addPacket(new ServerPlayerInventoryUpdate());
		Protocol.addPacket(new ServerBlockInventoryUpdate());
		Protocol.addPacket(new ClientItemTransaction());
		Protocol.addPacket(new ClientItemMove());
	}

	@Override
	public void defineVoxelTypes()
	{
		// Create action handlers
		IVoxelActionHandler blockInventoryHandler = new IVoxelActionHandler()
		{

			@Override
			public void handleAction(VoxelData voxelData, Vector3 voxelPosition)
			{
				// Get cubic world game instance.
				CubicWorldGame cubicWorld = CubicWorld.getClient();

				// Init overlay data
				OverlayData overlayData = new OverlayData();
				overlayData.put("inventory", voxelData.blockInventory);
				overlayData.put("playerInventory", CubicWorld.getClient().playerController.getPlayerInventory());
				overlayData.put("voxelPos", voxelPosition);

				// Activate overlay
				IGuiOverlay blockOverlay = cubicWorld.guiManager.getOverlayById(BasePlugin.furnaceGuiOverlayId);

				blockOverlay.setOverlayData(overlayData);

				// Now open the overlay
				cubicWorld.guiManager.openOverlay(BasePlugin.furnaceGuiOverlayId);
			}

		};

		// Furnace action handler
		MachineUpdateHandler furnaceUpdateHandler = new MachineUpdateHandler()
		{
			@Override
			protected boolean getWorkingState(IInventory inventory)
			{
				ItemStack fuelStack = inventory.getItemStackInSlot(0);
				return fuelStack != null && fuelStack.getType().getItemId() == BasePlugin.itemCoalId;
			}

			@Override
			protected void workTick()
			{
				// System.out.println("Work tick!");
			}

		};

		// Register voxels
		voxelGrassId = VoxelEngine.registerType("Grass").setRenderState(0, new VoxelRenderState(grassTopId, dirtId, grassSideId, grassSideId, grassSideId, grassSideId)).setGuiTexture(grassTop).voxelId;

		voxelDirtId = VoxelEngine.registerType("Dirt").setRenderState(0, new VoxelRenderState(dirtId, dirtId, dirtId, dirtId, dirtId, dirtId)).setGuiTexture(dirt).voxelId;

		voxelBedrockId = VoxelEngine.registerType("Bedrock").setRenderState(0, new VoxelRenderState(bedrockId, bedrockId, bedrockId, bedrockId, bedrockId, bedrockId)).setGuiTexture(bedrock).voxelId;

		voxelLeavesSpruceId = VoxelEngine.registerType("LeavesSpruce").setRenderState(0, new VoxelRenderState(leavesSpruceId, leavesSpruceId, leavesSpruceId, leavesSpruceId, leavesSpruceId, leavesSpruceId)).setTransparent(true).setGuiTexture(leavesSpruce).voxelId;

		voxelTreeSpruceId = VoxelEngine.registerType("TreeSpruce").setRenderState(0, new VoxelRenderState(treeSpruceId, treeSpruceId, treeSpruceId, treeSpruceId, treeSpruceId, treeSpruceId)).setGuiTexture(treeSpruce).voxelId;

		voxelStoneId = VoxelEngine.registerType("Stone").setRenderState(0, new VoxelRenderState(stoneId, stoneId, stoneId, stoneId, stoneId, stoneId)).setGuiTexture(stone).voxelId;

		voxelGlassId = VoxelEngine.registerType("Glass").setRenderState(0, new VoxelRenderState(glassId, glassId, glassId, glassId, glassId, glassId)).setTransparent(true).setGuiTexture(glass).voxelId;

		// Create furnace render states
		VoxelRenderState furnaceNormalState = new VoxelRenderState(furnaceTopId, furnaceTopId, furnaceSideId, furnaceSideId, furnaceFrontId, furnaceSideId);
		VoxelRenderState furnaceWorkingState = new VoxelRenderState(furnaceTopId, furnaceTopId, furnaceSideId, furnaceSideId, furnaceFrontLitId, furnaceSideId);

		voxelFurnaceId = VoxelEngine.registerType("Furnace").setUpdateHandler(furnaceUpdateHandler).setRenderState(0, furnaceNormalState).setRenderState(1, furnaceWorkingState).setActionHandler(blockInventoryHandler).setInventorySize(2).setGuiTexture(furnaceFrontTexture).voxelId;
	}

	public void initializeGuiManager(GuiManager guiManager)
	{
		// Set skin
		guiManager.setSkin(new StandardSkin());

		// Init HUD
		guiManager.registerHudElement(new HudBlockInformation());
		guiManager.registerHudElement(new BlockSelectorGui());
		guiManager.registerHudElement(new Crosshair());
		guiManager.registerHudElement(new Chatbox());

		// Init overlays
		Overlay furnaceOverlay = null;
		chatOverlayId = guiManager.registerOverlay(new ChatOverlay());

		try
		{
			// Initialize main menu overlay
			Overlay mainMenuOverlay = XMLOverlayLoader.loadOverlay(Gdx.files.internal("gui\\MainMenuOverlay.xml"));
			furnaceOverlay = XMLOverlayLoader.loadOverlay(Gdx.files.internal("gui\\FurnaceGui.xml"));
			playerInventoryOverlay = XMLOverlayLoader.loadOverlay(Gdx.files.internal("gui\\PlayerInventoryGui.xml"));

			// Attach main menu events
			// Resume game event, only closes the currently active overlay.
			IGuiElement element = mainMenuOverlay.getElementById("btn_resume");
			element.setClickHandler(new IClickHandler()
			{

				@Override
				public void handleClick(Vector2 mousePosition)
				{
					CubicWorld.getClient().guiManager.closeOverlay();
				}

			});

			// End game event, calls system.exit()
			element = mainMenuOverlay.getElementById("btn_end");
			element.setClickHandler(new IClickHandler()
			{

				@Override
				public void handleClick(Vector2 mousePosition)
				{
					System.exit(0);
				}

			});

			// Options event. Does nothing yet, will open the options overlay later
			element = mainMenuOverlay.getElementById("btn_options");
			element.setClickHandler(new IClickHandler()
			{

				@Override
				public void handleClick(Vector2 mousePosition)
				{
					// TODO
				}

			});

			furnaceGuiOverlayId = guiManager.registerOverlay(furnaceOverlay);
			mainMenuOverlayId = guiManager.registerOverlay(mainMenuOverlay);
			playerInventoryOverlayId = guiManager.registerOverlay(playerInventoryOverlay);
		}
		catch (IOException | DataFormatException e)
		{
			ConsoleHelper.writeLog("error", "Base plugin failed to load overlays!", "BasePlugin");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	@Override
	public void initializeGuiSystem()
	{
		XMLOverlayLoader.registerObjectLoader("button", new XMLButtonLoader());
		XMLOverlayLoader.registerObjectLoader("image", new XMLImageLoader());
		XMLOverlayLoader.registerObjectLoader("inventoryslot", new XMLInventorySlotLoader());
		XMLOverlayLoader.registerObjectLoader("inventoryview", new XMLInventoryViewLoader());
		XMLOverlayLoader.registerObjectLoader("textbox", new XMLTextboxLoader());
	}

	@Override
	public void loadModels()
	{
	}

	public void loadSounds()
	{
		// Load standard footstep sound
		VoxelData.standardFootstepSound = Gdx.audio.newSound(Gdx.files.internal("sounds\\player\\footsteps\\Fantozzi-StoneR3.ogg"));
	}

	@Override
	public void loadTextures()
	{
		// Load textures
		bedrock = new Texture("textures\\bedrock.png");
		dirt = new Texture("textures\\dirt.png");
		glass = new Texture("textures\\glass.png");
		grassTop = new Texture("textures\\grass_top.png");
		grassSide = new Texture("textures\\grass_side.png");
		leavesSpruce = new Texture("textures\\leaves_spruce.png");
		stone = new Texture("textures\\stone.png");
		treeSpruce = new Texture("textures\\tree_spruce.png");
		furnaceFrontTexture = new Texture("textures\\furnace_front.png");
		furnaceFrontLitTexture = new Texture("textures\\furnace_front_lit.png");
		furnaceSideTexture = new Texture("textures\\furnace_side.png");
		furnaceTopTexture = new Texture("textures\\furnace_top.png");
		coal = new Texture("textures\\items\\coal.png");

		// Register textures
		bedrockId = VoxelEngine.registerTexture("Bedrock", bedrock);
		dirtId = VoxelEngine.registerTexture("Dirt", dirt);
		grassTopId = VoxelEngine.registerTexture("GrassTop", grassTop);
		grassSideId = VoxelEngine.registerTexture("GrassSide", grassSide);
		leavesSpruceId = VoxelEngine.registerTexture("LeavesSpruce", leavesSpruce);
		stoneId = VoxelEngine.registerTexture("Stone", stone);
		treeSpruceId = VoxelEngine.registerTexture("TreeSpruce", treeSpruce);
		glassId = VoxelEngine.registerTexture("Glass", glass);
		furnaceFrontId = VoxelEngine.registerTexture("FurnaceFront", furnaceFrontTexture);
		furnaceFrontLitId = VoxelEngine.registerTexture("FurnaceFrontLit", furnaceFrontLitTexture);
		furnaceSideId = VoxelEngine.registerTexture("FurnaceSide", furnaceSideTexture);
		furnaceTopId = VoxelEngine.registerTexture("FurnaceTop", furnaceTopTexture);
		coalId = ItemSystem.registerTexture("Coal", coal);
	}

	@Event(eventType = "update")
	public void update(boolean isServer)
	{
		if (!isServer)
		{
			// Update handlers
			leftMovementHandler.update();
			rightMovementHandler.update();
			forwardMovementHandler.update();
			backwardMovementHandler.update();

			// Update movement controller
			inputMovementController.update();
		}
	}

}
