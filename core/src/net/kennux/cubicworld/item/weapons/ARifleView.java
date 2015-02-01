package net.kennux.cubicworld.item.weapons;

import net.kennux.cubicworld.item.IItemView;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;

/**
 * Item view implementation for rifles.
 * 
 * @author kennux
 *
 */
public class ARifleView implements IItemView
{
	/**
	 * Defines all possible states for this item view.
	 * 
	 * @author kennux
	 *
	 */
	enum ViewState
	{
		NORMAL, AIMING
	};

	/**
	 * The current state
	 */
	@SuppressWarnings("unused")
	private ViewState state;

	public ARifleView()
	{
		this.state = ViewState.NORMAL;
	}

	@Override
	public void render(Camera camera, ModelBatch modelBatch, DecalBatch decalBatch, SpriteBatch spriteBatch)
	{

	}

	@Override
	public void update()
	{

	}
}
