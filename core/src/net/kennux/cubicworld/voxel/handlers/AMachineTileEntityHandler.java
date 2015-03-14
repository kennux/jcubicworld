package net.kennux.cubicworld.voxel.handlers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import net.kennux.cubicworld.CubicWorld;
import net.kennux.cubicworld.CubicWorldConfiguration;
import net.kennux.cubicworld.inventory.IInventory;
import net.kennux.cubicworld.serialization.BitReader;
import net.kennux.cubicworld.serialization.BitWriter;
import net.kennux.cubicworld.util.ShaderLoader;
import net.kennux.cubicworld.voxel.VoxelChunk;
import net.kennux.cubicworld.voxel.VoxelData;
import net.kennux.cubicworld.voxel.VoxelEngine;

/**
 * <pre>
 * Abstract machine update handler.
 * You can use this to implement machines in a simple way.
 * 
 * This update handler has 2 states, working and not working.
 * Based on the state the following render states are set:
 * 
 * 0 - Not working
 * 1 - Working
 * 
 * This handler will check every tick if the machine can start working.
 * 
 * IMPORTANT: This class requires the voxel data to has a inventory attached.
 * </pre>
 * 
 * @author KennuX
 *
 */
public abstract class AMachineTileEntityHandler implements IVoxelTileEntityHandler
{
	/**
	 * Return true in here if the machine's conditions for starting working are met.
	 * 
	 * @return
	 */
	protected abstract boolean getWorkingState(IInventory inventory);
	
	/**
	 * Is true if the machine is currently in working state.
	 */
	private boolean isWorking = false;
	
	// Model getters and setters.
	/**
	 * <pre>
	 * Return your mesh for the working state in here.
	 * As texture the voxel engine map will get used.
	 * You should NOT create a new instance on every call!
	 * If null is returned in here nothing will get rendered.
	 * </pre>
	 * @return
	 */
	protected abstract Mesh getWorkingMesh();
	/**
	 * <pre>
	 * Return your mesh for the not working state in here.
	 * As texture the voxel engine map will get used.
	 * You should NOT create a new instance on every call!
	 * If null is returned in here nothing will get rendered.
	 * </pre>
	 * @return
	 */
	protected abstract Mesh getNotWorkingMesh();
	
	/**
	 * The tile entity shader.
	 * This will use the shader located in assets/shaders/tileentity as standard.
	 * Set this shader in your constructor if you want to use a custom one.
	 */
	private ShaderProgram tileEntityShader;
	
	public void handleUpdate(VoxelData voxelData, int x, int y, int z, boolean isServer)
	{
		// Only blocks with inventories are allowed!
		if (voxelData.blockInventory == null)
			return;

		// Get the current target working state
		boolean workingState = this.getWorkingState(voxelData.blockInventory);

		if (isServer)
		{
			if (workingState)
				this.workTick();
		}
		else
		{
			// Client render things
			if (!isWorking && workingState)
			{
				// Change to working
				//voxelData.setRenderStateId(1);
				// TODO
				this.isWorking = true;
			}
			else if (isWorking && !workingState)
			{
				// Change to not working
				//voxelData.setRenderStateId(0);
				// TODO
				this.isWorking = false;
			}
		}
	}
	
	@Override
	public void handleRender(Camera camera, VoxelData voxelData, int x, int y, int z)
	{
		// Check if shader is ready
		if (this.tileEntityShader == null)
		{
			this.tileEntityShader = ShaderLoader.loadShader("tileentity");
		}
		
		// Check if shader is compiled
		if (this.tileEntityShader.isCompiled())
		{
			// Determine which mesh will get rendered
			Mesh m = null;
			
			if (this.isWorking)
			{
				m = this.getWorkingMesh();
			}
			else
			{
				m = this.getNotWorkingMesh();
			}
			
			// Should it get rendered?
			if (m != null)
			{
				this.tileEntityShader.begin();
				
				// Render mesh
				// TODO: Lighting
				this.tileEntityShader.setUniformf("m_light", 1); // voxelData.getLightLevel() / (float) CubicWorldConfiguration.maxLightLevel);
				this.tileEntityShader.setUniformMatrix("m_cameraProj", camera.combined);
				this.tileEntityShader.setUniformMatrix("m_transform", new Matrix4(new Vector3(x,y,z), VoxelChunk.ROTATION_MAPPINGS_QUATERNION[voxelData.rotation], new Vector3(1,1,1)));
				VoxelEngine.textureAtlas.atlasTexture.bind(0);
				this.tileEntityShader.setUniformi("r_textureAtlas", 0);
				
				m.render(this.tileEntityShader, GL20.GL_TRIANGLES);
				this.tileEntityShader.end();
			}
		}
	}

	/**
	 * Gets called every tick the machine is working.
	 */
	protected abstract void workTick();

	public void serialize(BitWriter writer)
	{

	}

	public void deserialize(BitReader reader)
	{

	}
}
