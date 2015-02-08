package net.kennux.cubicworld.plugins.baseplugin.admin;

import net.kennux.cubicworld.admin.IChatCommand;
import net.kennux.cubicworld.networking.CubicWorldServerClient;
import net.kennux.cubicworld.networking.packet.ChatMessage;

public class TestCommand implements IChatCommand
{
	@Override
	public void executeCommand(CubicWorldServerClient sender, String[] args)
	{
		ChatMessage message = new ChatMessage();
		message.chatMessage = "Test command successfully executed!";
		message.setPlayerId(sender.playerEntity.getEntityId());
		sender.getMaster().sendPacket(message);
	}
}
