package net.kennux.cubicworld.gui;

import net.kennux.cubicworld.gui.skin.AGuiSkin;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * <pre>
 * Abstract ui overlay implementation.
 * An ui overlay is for example the game settings.
 * 
 * An overlay will obscure the screen and draw itself ontop of the darkened
 * image.
 * It can contain multiple gui elements like buttons, labels, textboxes, etc.
 * </pre>
 * 
 * @author KennuX
 *
 */
public abstract class AGuiOverlay extends GuiElementContainer implements IGuiOverlay
{
	/**
	 * The texture used to obscure / darken the screen.
	 */
	public static Texture overlayObscureTexture;

	/**
	 * Constructs the overlay.
	 * In your own class you <strong>HAVE TO</strong> overload this constructor!
	 */
	public AGuiOverlay()
	{
		// Call bootstrap
		this.initialize();
	}

	/**
	 * <pre>
	 * Add all your gui elements to this overlay in this function.
	 * It will get called in the constructor.
	 * 
	 * You can use the protected functions
	 * addElement() and removeElement() for adding / removing gui elements.
	 * </pre>
	 */
	protected abstract void initialize();

	/**
	 * The main rendering routine.
	 * First it obscures / darkens the screen,
	 * then it render's all elements attached to the overlay.
	 * 
	 * @param spriteBatch
	 */
	public void render(SpriteBatch spriteBatch, BitmapFont font, AGuiSkin skin, ShapeRenderer shapeRenderer)
	{
		// Darken the screen
		spriteBatch.draw(AGuiOverlay.overlayObscureTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		// Render the overlay base (The background texture of the overlay).
		Texture overlayTexture = skin.getTexture("Overlay");
		spriteBatch.draw(overlayTexture, this.position.x, this.position.y, this.size.x, this.size.y);

		// Render the underlying element container
		super.render(spriteBatch, font, skin, shapeRenderer);
	}
}