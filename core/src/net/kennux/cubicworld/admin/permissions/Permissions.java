package net.kennux.cubicworld.admin.permissions;

import java.util.HashMap;

/**
 * <pre>
 * Static permissions class.
 * This class holds all permission rules.
 * 
 * The current permission system is rather simple, users can have one or multiple roles.
 * Every role can have access to commands.
 * </pre>
 * @author KennuX
 *
 */
public class Permissions
{
	/**
	 * <pre>
	 * The roles list as a hashmap.
	 * The key is the role name, value is the role object.
	 * </pre>
	 */
	private static HashMap<String, PermissionRole> roles = new HashMap<String, PermissionRole>();
	
	/**
	 * <pre>
	 * Adds the given role to the roles hashmap.
	 * This will overwrite existing role with same name.
	 * </pre>
	 * @param role
	 */
	public static void registerRole(PermissionRole role)
	{
		// Add to roles hashmap.
		roles.put(role.getName(), role);
	}
	
	/**
	 * Returns the role registered with the given name.
	 * May returns null if there is no role registered for the 
	 * @param roleName
	 * @return
	 */
	public static PermissionRole getRole(String roleName)
	{
		return roles.get(roleName);
	}
	
	/**
	 * Returns true if there is a role with the given name registered.
	 * @param roleName
	 * @return
	 */
	public static boolean hasRole(String roleName)
	{
		return roles.containsKey(roleName);
	}
	
	/**
	 * <pre>
	 * Checks if the given role has the given right.
	 * Returns false if the given role name is not registered.
	 * 
	 * </pre>
	 * @param role
	 * @param right
	 * @return
	 */
	public static boolean hasRight(String role, String right)
	{
		PermissionRole roleObject = Permissions.getRole(role);
		
		if (roleObject == null || !roleObject.hasRight(right))
			return false;
		
		return true;
	}
	
	/**
	 * Checks if one of the given roles has the given right.
	 * Returns false if the given role name(s) is not registered.
	 * @param roles
	 * @param right
	 * @return
	 */
	public static boolean hasRight(String[] roles, String right)
	{
		for (String role : roles)
		{
			// Check every rule and if one has the right, return true
			// otherwise return false
			if (Permissions.hasRight(role, right))
				return true;
		}
		
		return false;
	}
}
