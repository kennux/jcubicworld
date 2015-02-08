package net.kennux.cubicworld.plugins.baseplugin.gui;

import net.kennux.cubicworld.CubicWorld;
import net.kennux.cubicworld.gui.GuiElementContainer;
import net.kennux.cubicworld.gui.GuiHelper;
import net.kennux.cubicworld.gui.IGuiOverlay;
import net.kennux.cubicworld.gui.elements.ITextboxEnterHandler;
import net.kennux.cubicworld.gui.elements.Textbox;
import net.kennux.cubicworld.gui.hud.Chatbox;
import net.kennux.cubicworld.gui.overlay.OverlayData;
import net.kennux.cubicworld.gui.skin.AGuiSkin;
import net.kennux.cubicworld.networking.packet.ChatMessage;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class ChatOverlay extends GuiElementContainer implements IGuiOverlay
{
	/**
	 * The textbox
	 */
	private Textbox textbox;

	public ChatOverlay()
	{
		this.initialize();
	}

	@Override
	public OverlayData getOverlayData()
	{
		return null;
	}

	protected void initialize()
	{
		// Init position and size
		this.setPosition(GuiHelper.getAbsoluteFromPercentagePosition(new Vector2(5, 5)));
		this.setSize(GuiHelper.getAbsoluteFromPercentagePosition(new Vector2(35, 5)));

		// Init textbox
		Vector2 textboxPosition = GuiHelper.getAbsoluteFromPercentagePosition(new Vector2(0, 5));
		Vector2 textboxSize = GuiHelper.getAbsoluteFromPercentagePosition(new Vector2(40, 5));
		Rectangle relativeTextboxRect = new Rectangle(textboxPosition.x - this.getPosition().x, textboxPosition.y - this.getPosition().y, textboxSize.x, textboxSize.y);
		Rectangle absoluteTextboxRect = new Rectangle(textboxPosition.x, textboxPosition.y, textboxSize.x, textboxSize.y);
		this.textbox = new Textbox(absoluteTextboxRect, relativeTextboxRect);

		this.addElement("Textbox", this.textbox);

		// Init textbox enter handler
		this.textbox.setEnterHandler(new ITextboxEnterHandler()
		{

			@Override
			public void handleEnter(Textbox textbox)
			{
				ChatMessage chatMessage = new ChatMessage();
				chatMessage.chatMessage = textbox.getContents();
				CubicWorld.getClient().client.sendPacket(chatMessage);
				textbox.setContents("");
			}

		});
	}

	@Override
	public void render(SpriteBatch spriteBatch, BitmapFont font, AGuiSkin skin, ShapeRenderer shapeRenderer)
	{
		super.render(spriteBatch, font, skin, shapeRenderer);

		Chatbox.getInstance().render(spriteBatch, font);
	}

	@Override
	public void setOverlayData(OverlayData overlayData)
	{
	}

	@Override
	public void update()
	{
	}
}
