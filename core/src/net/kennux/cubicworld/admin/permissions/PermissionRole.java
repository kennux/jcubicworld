package net.kennux.cubicworld.admin.permissions;

/**
 * <pre>
 * Permission role class.
 * </pre>
 * @author KennuX
 *
 */
public class PermissionRole
{
	/**
	 * The rights string list.
	 */
	private String[] rights;
	
	/**
	 * The name of the role.
	 */
	private String roleName;
	
	/**
	 * Constructs a role object with the given name as rolename and rights array.
	 * @param name
	 * @param rights
	 */
	public PermissionRole(String roleName, String[] rights)
	{
		this.roleName = roleName;
		this.rights = rights;
	}
	
	public String getName()
	{
		return this.roleName;
	}
	
	/**
	 * <pre>
	 * Checks if this role is allowed to execute the command given in commandName.
	 * This function checks this role for the right command.[commandName]
	 * 
	 * So PermissionRole.hasRight("command."+commandName) would have the same effect.
	 * </pre>
	 * @param commandName The command name
	 * @return
	 */
	public boolean isAllowedToExecuteCommand(String commandName)
	{
		return this.hasRight("command."+commandName);
	}
	
	/**
	 * Checks this role if has the given right.
	 * @param rightName
	 * @return
	 */
	public boolean hasRight(String rightName)
	{
		for (int i = 0; i < this.rights.length; i++)
		{
			if (this.rights[i].equals(rightName))
			{
				return true;
			}
		}
		
		return false;
	}
}
