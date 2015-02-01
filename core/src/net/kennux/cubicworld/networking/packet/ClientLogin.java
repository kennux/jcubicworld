package net.kennux.cubicworld.networking.packet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import net.kennux.cubicworld.CubicWorld;
import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.CubicWorldServer;
import net.kennux.cubicworld.entity.PlayerEntity;
import net.kennux.cubicworld.inventory.IInventory;
import net.kennux.cubicworld.inventory.IInventoryUpdateHandler;
import net.kennux.cubicworld.networking.APacketModel;
import net.kennux.cubicworld.networking.CubicWorldServerClient;
import net.kennux.cubicworld.networking.packet.inventory.ServerPlayerInventoryUpdate;
import net.kennux.cubicworld.serialization.BitReader;
import net.kennux.cubicworld.serialization.BitWriter;
import net.kennux.cubicworld.util.ConsoleHelper;

import com.badlogic.gdx.math.Vector3;

/**
 * Packet data: [String username]
 * 
 * Server -> Client packet
 * 
 * @author KennuX
 *
 */
public class ClientLogin extends APacketModel
{
	// Chunk coordinates
	public String username;

	@Override
	public void interpretClientSide(CubicWorldGame cubicWorld)
	{
	}

	@Override
	public void interpretServerSide(CubicWorldServer server, CubicWorldServerClient client)
	{
		client.playerEntity = new PlayerEntity();
		client.playerEntity.setEntityId(client.getClientId());
		client.playerEntity.setEntityName(this.username);
		client.playerEntity.setPosition(new Vector3(0, 130, 0));
		client.playerEntity.interpolatePosition(true);

		// Load already available data if there is any
		File playerDataFile = new File(server.savePath + client.playerEntity.getSaveFilePath());

		if (playerDataFile.exists())
		{
			// Read data from the player file
			byte[] data = new byte[(int) playerDataFile.length()];
			try
			{
				FileInputStream playerDataInputStream = new FileInputStream(playerDataFile);
				playerDataInputStream.read(data);

				// Deserialize player data
				BitReader reader = new BitReader(data);
				client.playerEntity.deserializeFull(reader);

				playerDataInputStream.close();
			}
			catch (IOException e)
			{
				ConsoleHelper.writeLog("ERROR", "Error while reading player data file: " + playerDataFile.getAbsolutePath(), "ClientLogin");
				ConsoleHelper.logError(e);
			}
		}

		// Set player inventory update handler
		final PlayerEntity playerEntity = client.playerEntity;

		client.playerEntity.playerInventory.setUpdateHandler(new IInventoryUpdateHandler()
		{
			@Override
			public void inventoryGotUpdated(IInventory inventory)
			{
				// Init update packet
				ServerPlayerInventoryUpdate model = new ServerPlayerInventoryUpdate();
				model.setPlayerId(playerEntity.getEntityId());
				model.inventory = playerEntity.playerInventory;

				CubicWorld.getServer().addPacket(model);
			}
		});

		// Send player spawn packet
		ServerPlayerSpawn spawnPacket = new ServerPlayerSpawn();
		spawnPacket.playerEntity = client.playerEntity;
		client.sendPacket(spawnPacket);

		// Send login message
		ChatMessage.serverSendChatMessage(server, "User " + client.playerEntity.getEntityName() + " loggedin!");

		// server.entityManager.add(server.entityManager.getNextFreeId(), new TestEntity(server.voxelWorld, client.playerEntity));
		/*
		 * ItemEntity itemEntity = new ItemEntity(BasePlugin.itemCoalId);
		 * itemEntity.setPosition(new Vector3(10,150,10));
		 * itemEntity.interpolatePosition(true);
		 * server.entityManager.add(server.entityManager.getNextFreeId(), itemEntity);
		 */

		// Log
		ConsoleHelper.writeLog("info", "User loggedin: " + this.username, "ServerClient");
	}

	@Override
	public void readPacket(BitReader reader)
	{
		this.username = reader.readString();
	}

	@Override
	public void writePacket(BitWriter builder)
	{
		builder.writeString(this.username);
	}

}
