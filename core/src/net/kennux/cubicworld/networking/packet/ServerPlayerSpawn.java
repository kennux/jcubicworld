package net.kennux.cubicworld.networking.packet;

import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.CubicWorldServer;
import net.kennux.cubicworld.entity.PlayerEntity;
import net.kennux.cubicworld.networking.APacketModel;
import net.kennux.cubicworld.networking.CubicWorldServerClient;
import net.kennux.cubicworld.serialization.BitReader;
import net.kennux.cubicworld.serialization.BitWriter;

import com.badlogic.gdx.math.Vector3;

/**
 * Gets sent after the player got spawned on the world (ClientLogin answer).
 * 
 * @author kennux
 *
 */
public class ServerPlayerSpawn extends APacketModel
{
	// Serverside model
	public PlayerEntity playerEntity;

	// Clientside model
	public Vector3 spawnPos;
	public BitReader playerInventoryData;

	@Override
	public void interpretClientSide(CubicWorldGame cubicWorld)
	{
		// Spawn entity
		cubicWorld.playerController.setPosition(this.spawnPos);
		cubicWorld.playerController.interpolatePosition(true);
		cubicWorld.playerController.getPlayerInventory().deserializeInventory(this.playerInventoryData);
	}

	@Override
	public void interpretServerSide(CubicWorldServer server, CubicWorldServerClient client)
	{
	}

	@Override
	public void readPacket(BitReader reader)
	{
		// Read spawn position
		this.spawnPos = reader.readVector3();

		// Deserialize inventory
		this.playerInventoryData = reader;
	}

	@Override
	public void writePacket(BitWriter builder)
	{
		// Write spawn position and inventory information
		builder.writeVector3(this.playerEntity.getPosition());
		this.playerEntity.playerInventory.serializeInventory(builder);
	}

}
