package net.kennux.cubicworld.admin.permissions;

public class TestBackend implements IPermissionSystemBackend
{

	@Override
	public PermissionRole[] loadRoles()
	{
		return new PermissionRole[] { new PermissionRole("Test", new String[] { "command.*" }) };
	}

	@Override
	public String[] getUserRoles(String username)
	{
		return new String[] { "Test" };
	}
}
