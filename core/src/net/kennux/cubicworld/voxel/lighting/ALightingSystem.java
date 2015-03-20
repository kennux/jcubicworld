package net.kennux.cubicworld.voxel.lighting;

import net.kennux.cubicworld.voxel.VoxelChunk;

/**
 * <pre>
 * 
 * Abstract implementation of a lighting system.
 * A lighting system can have multiple lighting passes.
 * 
 * This class only gets an array of passes from the getPasses() function which you must override and executes them one after another.
 * A lighting pass then can return a boolean in ILightingPass.executePass() which says the pass is done or not.
 * This can be used in order to wait for other voxel's lighting to complete if the data is needed for lighting calculations.
 * 
 * </pre>
 * 
 * @author KennuX
 *
 */
public abstract class ALightingSystem
{
	/**
	 * <pre>
	 * Return the lighting passes in an array.
	 * The passes will get executed in the order they are stored in the array.
	 * </pre>
	 */
	protected abstract ILightingPass[] getPasses();

	/**
	 * The values returned by executePass().
	 * 
	 * @see ILightingPass#executePass(net.kennux.cubicworld.voxel.VoxelChunk)
	 */
	private boolean[] passStates;

	/**
	 * The passes returned by getPasses().
	 * 
	 * @see ALightingSystem#passes
	 */
	private ILightingPass[] passes;

	/**
	 * The overall lighting state.
	 */
	private boolean state;

	/**
	 * You need to overload this constructor!
	 */
	public ALightingSystem()
	{
		this.passes = this.getPasses();
		this.passStates = new boolean[this.passes.length];
	}

	/**
	 * Resets all lighting states
	 * 
	 * @see ALightingSystem#passStates
	 */
	public void resetLighting()
	{
		for (int i = 0; i < this.passStates.length; i++)
		{
			this.passStates[i] = false;
		}

		this.state = false;
	}

	/**
	 * Calculates the lighting information.
	 * Executes the lighting passes.
	 * 
	 * @see ALightingSystem#passes
	 * @param chunk
	 *            The chunk to light
	 */
	public void update(VoxelChunk chunk)
	{
		// Dont run if lighting is ready
		if (this.state)
			return;

		for (int i = 0; i < this.passes.length; i++)
		{
			// Find a pass which is not done yet
			if (!this.passStates[i])
			{
				this.passStates[i] = this.passes[i].executePass(chunk);

				if (!this.passStates[i])
				{
					this.state = false;
					return;
				}
			}
		}

		this.state = true;
		return;
	}

	/**
	 * Checks if the pass with the given class is already done.
	 * Returns fals if the given pass was not found.
	 * This method may be a bit slow!
	 * 
	 * @return
	 */
	public boolean isPassDone(Class clazz)
	{
		int passIndex = -1;

		for (int i = 0; i < this.passes.length; i++)
		{
			if (this.passes[i].getClass().equals(clazz))
			{
				passIndex = i;
				break;
			}
		}

		return this.passStates[passIndex];
	}

	/**
	 * @return the state
	 */
	public boolean isReady()
	{
		return this.state;
	}
}
