package net.kennux.cubicworld.gui.hud;

import net.kennux.cubicworld.gui.GuiHelper;
import net.kennux.cubicworld.gui.IHudElement;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

/**
 * <pre>
 * Chatbox hud element implementation.
 * </pre>
 * 
 * @author KennuX
 *
 */
public class Chatbox implements IHudElement
{
	public static Chatbox getInstance()
	{
		return instance;
	}

	// Singleton
	private static Chatbox instance;

	/**
	 * The chatbox content.
	 */
	private String chatboxContent = "";

	public Chatbox()
	{
		instance = this;
	}

	/**
	 * Adds a chat message to this chatbox.
	 * 
	 * @param message
	 */
	public void addChatMessage(String message)
	{
		this.chatboxContent += message + "\r\n";
	}

	@Override
	public void render(SpriteBatch spriteBatch, BitmapFont font)
	{
		Vector2 chatboxPosition = GuiHelper.getAbsoluteFromPercentagePosition(new Vector2(0, 35));
		font.setColor(Color.WHITE);
		font.drawMultiLine(spriteBatch, this.chatboxContent, chatboxPosition.x, chatboxPosition.y);
	}

	@Override
	public void update()
	{

	}

}
