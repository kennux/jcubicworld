package net.kennux.cubicworld.admin.permissions;

import java.util.HashMap;
import java.util.Map.Entry;

/**
 * <pre>
 * A permission system backend handles permission loading.
 * An implementation of this interface MUST have a constructor without any parameters, otherwise server init will fail.
 * </pre>
 * @author KennuX
 *
 */
public interface IPermissionSystemBackend
{
	/**
	 * Load your roles in here and return the role objects.
	 * @return
	 */
	public PermissionRole[] loadRoles();
	
	/**
	 * Return all roles mapped to the given username in here.
	 * @return
	 */
	public String[] getUserRoles(String username);
}
