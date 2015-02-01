package net.kennux.cubicworld.entity;

import net.kennux.cubicworld.voxel.VoxelWorld;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

/**
 * Model rendered mob entity implementation.
 * Renders a model and extends AMobEntity.
 * 
 * Return a model instance in the getModelInstance() abstract function by overriding it.
 * 
 * @author kennux
 *
 */
public abstract class AModelMobEntity extends AMobEntity
{
	/**
	 * The current model instance
	 */
	private ModelInstance modelInstance;

	public AModelMobEntity()
	{

	}

	public AModelMobEntity(VoxelWorld voxelWorld)
	{
		super(voxelWorld);
	}

	/**
	 * Returns a new model instance of your model in here.
	 * 
	 * @return
	 */
	public abstract ModelInstance getModelInstance();

	/**
	 * Renders the current model instance.
	 */
	public void render(Camera camera, ModelBatch modelBatch, DecalBatch decalBatch, SpriteBatch spriteBatch, BitmapFont bitmapFont)
	{
		if (this.modelInstance == null)
			this.modelInstance = this.getModelInstance();

		// Set transformation
		Vector3 renderPosition = new Vector3(this.getPosition());
		Quaternion rotation = new Quaternion().setEulerAngles(this.getEulerAngles().y, this.getEulerAngles().x, this.getEulerAngles().z);
		this.modelInstance.transform.set(renderPosition, rotation);

		// Render
		modelBatch.render(this.modelInstance);

		// Project worldpos
		/*
		 * Vector3 screenPos = camera.project(renderPosition);
		 * screenPos.x -= bitmapFont.getBounds(this.getEntityName()).width / 2;
		 * bitmapFont.draw(spriteBatch, this.getEntityId() + ", " + this.getPosition(), screenPos.x, screenPos.y);
		 */
	}

}
