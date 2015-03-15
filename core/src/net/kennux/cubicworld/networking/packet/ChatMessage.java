package net.kennux.cubicworld.networking.packet;

import net.kennux.cubicworld.CubicWorldGame;
import net.kennux.cubicworld.CubicWorldServer;
import net.kennux.cubicworld.admin.AdminSystem;
import net.kennux.cubicworld.admin.IChatCommand;
import net.kennux.cubicworld.admin.permissions.Permissions;
import net.kennux.cubicworld.gui.hud.Chatbox;
import net.kennux.cubicworld.networking.APacketModel;
import net.kennux.cubicworld.networking.CubicWorldServerClient;
import net.kennux.cubicworld.serialization.BitReader;
import net.kennux.cubicworld.serialization.BitWriter;

public class ChatMessage extends APacketModel
{
	// STATIC HELPERS
	/**
	 * Sends a message from the server to all clients.
	 * 
	 * @param server
	 * @param message
	 */
	public static void serverSendChatMessage(CubicWorldServer server, String message)
	{
		ChatMessage messageModel = new ChatMessage();

		messageModel.chatMessage = "Server: " + message;

		// Broadcast
		server.sendPacket(messageModel);
	}

	public String chatMessage;

	@Override
	public int getPlayerId()
	{
		return -1;
	}

	@Override
	public void interpretClientSide(CubicWorldGame cubicWorld)
	{
		// Get chatbox instance
		Chatbox chatbox = Chatbox.getInstance();

		if (chatbox != null)
		{
			// Add to chatbox
			chatbox.addChatMessage(this.chatMessage);
		}
	}

	@Override
	public void interpretServerSide(CubicWorldServer server, CubicWorldServerClient client)
	{
		// Check if this message was a command
		if (this.chatMessage.startsWith("/"))
		{
			// Split command into arguments
			String[] command = this.chatMessage.substring(1).split(" ");

			// Get command
			IChatCommand commandInstance = AdminSystem.getCommand(command[0]);

			// Execute command if available and the sender got appropriate rights
			if (commandInstance != null)
			{
				if (Permissions.hasRight(client.roles, "command."+command[0]))
				{
					commandInstance.executeCommand(client, command);
				}
				else
				{
					ChatMessage messageModel = new ChatMessage();
					messageModel.chatMessage = "You don't have the rights to access this command!";
					messageModel.setPlayerId(client.playerEntity.getEntityId());
					server.sendPacket(messageModel);
				}
			}
			else
			{
				ChatMessage messageModel = new ChatMessage();
				messageModel.chatMessage = "Command not found!";
				messageModel.setPlayerId(client.playerEntity.getEntityId());
				server.sendPacket(messageModel);
			}
		}
		else
		{
			// Instantiate chat message model
			ChatMessage messageModel = new ChatMessage();

			if (client.playerEntity != null)
			{
				messageModel.chatMessage = client.playerEntity.getEntityName() + ": " + this.chatMessage;

				// Broadcast
				server.sendPacket(messageModel);
			}
		}
	}

	@Override
	public void readPacket(BitReader reader)
	{
		this.chatMessage = reader.readString();
	}

	@Override
	public void writePacket(BitWriter builder)
	{
		builder.writeString(this.chatMessage);
	}

}
