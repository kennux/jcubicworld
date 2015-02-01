package net.kennux.cubicworld.gui.skin;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

/**
 * The standard skin implementation.
 * 
 * @author kennux
 *
 */
public class StandardSkin extends AGuiSkin
{

	@Override
	public void bootstrap()
	{
		this.primaryColor = Color.WHITE;
		this.secondaryColor = Color.BLACK;
		this.fontColor = Color.WHITE;
		this.addTexture("Button", new Texture(Gdx.files.internal("textures\\gui\\standard\\button.png")));
		this.addTexture("Button_Clicked", new Texture(Gdx.files.internal("textures\\gui\\standard\\button_clicked.png")));
		this.addTexture("Overlay", new Texture(Gdx.files.internal("textures\\gui\\standard\\overlay.png")));
		this.addTexture("InventorySlot", new Texture("textures\\gui\\standard\\inventory_slot.png"));
		this.addTexture("Textbox", new Texture("textures\\gui\\standard\\textbox.png"));
	}

}
