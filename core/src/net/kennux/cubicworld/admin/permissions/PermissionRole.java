package net.kennux.cubicworld.admin.permissions;

/**
 * <pre>
 * Permission role class.
 * </pre>
 * 
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
	 * 
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
	 * 
	 * @param commandName
	 *            The command name
	 * @return
	 */
	public boolean isAllowedToExecuteCommand(String commandName)
	{
		return this.hasRight("command." + commandName);
	}

	/**
	 * <pre>
	 * Checks this role if has the given right.
	 * If the role has a right like command.* it will act as a wildcard.
	 * </pre>
	 * 
	 * @param rightName
	 * @return
	 */
	public boolean hasRight(String rightName)
	{
		for (int i = 0; i < this.rights.length; i++)
		{
			// Check if right is a wildcard
			if (this.rights[i].contains("*"))
			{
				// Yes, it is a wildcard!
				int asteriskPosition = this.rights[i].indexOf("*");

				// Get wildcard pattern and wildcard
				String wildcardPattern = this.rights[i].substring(0, asteriskPosition);
				String wildcard = rightName.substring(0, asteriskPosition);

				// Check if they match
				if (wildcardPattern.equals(wildcard))
				{
					// User has the right!
					return true;
				}
			}
			else if (this.rights[i].equals(rightName))
			{
				return true;
			}
		}

		return false;
	}
}
