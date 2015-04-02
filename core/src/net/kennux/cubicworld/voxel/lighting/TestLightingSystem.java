package net.kennux.cubicworld.voxel.lighting;

/**
 * Basic lighting system.
 * 
 * @author KennuX
 *
 */
public class TestLightingSystem extends ALightingSystem
{
	@Override
	protected ILightingPass[] getPasses()
	{
		return new ILightingPass[] { new TestSunlightLightingPass(this), new TestDependencySolverPass(this) };
	}

}
