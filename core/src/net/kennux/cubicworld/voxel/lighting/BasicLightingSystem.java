package net.kennux.cubicworld.voxel.lighting;

/**
 * Basic lighting system.
 * 
 * @author KennuX
 *
 */
public class BasicLightingSystem extends ALightingSystem
{
	@Override
	protected ILightingPass[] getPasses()
	{
		return new ILightingPass[] { new LocalLightingPass(this), new GlobalLightingPass() };
	}

}
