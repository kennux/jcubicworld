package net.kennux.cubicworld.item;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;

/**
 * <pre>
 * The IItemView interface is used for implementing a player's view for an item.
 * 
 * View means it's rendering, so this interface is used to abstract away the difference between tool, weapon and normal item rendering.
 * For example a Gun would implement this interface and render it's model, muzzle fire, etc. if fired.
 * </pre>
 * 
 * @author kennux
 *
 */
public interface IItemView
{
	/**
	 * Render the itemview.
	 * An implementation for an assault rifle would render the rifle model in here.
	 * 
	 * @param camera The camera used for rendering-
	 * @param modelBatch The model batch you should render to.
	 * @param decalBatch The decal batch you should render to if needed.
	 * @param spriteBatch The sprite batch you should render to.
	 */
	public void render(Camera camera, ModelBatch modelBatch, DecalBatch decalBatch, SpriteBatch spriteBatch);

	/**
	 * Handle your state transitions in here.
	 * Example: Transition from normal carrying an assault rifle to aimed.
	 */
	public void update();
}
