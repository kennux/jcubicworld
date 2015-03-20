package net.kennux.cubicworld.entity;

import net.kennux.cubicworld.voxel.VoxelWorld;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;

public class TestEntity extends AModelMobEntity
{
	private PlayerEntity playerEntity;

	public TestEntity()
	{

	}

	public TestEntity(VoxelWorld voxelWorld)
	{
		super(voxelWorld);
	}

	// Controller constructor -> Only server
	public TestEntity(VoxelWorld voxelWorld, PlayerEntity playerEntity)
	{
		super(voxelWorld);
		this.playerEntity = playerEntity;
		this.setPosition(new Vector3(0, 130, 0));
		this.interpolatePosition(true);
		this.setTargetEntity(playerEntity);
	}

	public float getEntitySpeed()
	{
		return 10;
	}

	@Override
	public ModelInstance getModelInstance()
	{
		return null;
	}

	@Override
	public void init()
	{
	}

	@Override
	public void update()
	{
		// Server logic part
		if (this.master.isServer())
		{
			if (this.playerEntity == null)
			{
				// Destroy!
				this.die();
				return;
			}
		}
		super.update();
	}

}
