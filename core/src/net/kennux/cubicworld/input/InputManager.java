package net.kennux.cubicworld.input;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * <pre>
 * The input manager handles assigning / managing of input event.
 * The input manager is NOT meant to get used for gui.
 * The gui has it's own input handling system, this one is only for the game controls.
 * 
 * This class is client-only!
 * </pre>
 * 
 * @author KennuX
 *
 */
public class InputManager
{
	/**
	 * Contains all key input actions mapped to their input key.
	 */
	private HashMap<Integer, IKeyInputHandler> keyInputActions;

	/**
	 * Contains all key input actions which conflicted with already existing key input actions.
	 */
	private ArrayList<IKeyInputHandler> conflictedKeyInputActions;

	/**
	 * This mouse input handler will get used by the GameInputProcessor.
	 */
	private IMouseInputHandler mouseInputHandler;

	/**
	 * Initializes the input manager.
	 * Will get called in the bootstrap.
	 */
	public InputManager()
	{
		keyInputActions = new HashMap<Integer, IKeyInputHandler>();
		conflictedKeyInputActions = new ArrayList<IKeyInputHandler>();
	}

	/**
	 * Adds a input action to the input manager.
	 * Returns true if the adding was successfull, false if there is a conflict.
	 * If the action conflicts with another one it will get saved to a seperate list and the user can remap the input keys ingame.
	 * 
	 * @param inputKey
	 * @param keyAction
	 */
	public boolean addInputAction(int inputKey, IKeyInputHandler keyAction)
	{
		if (keyInputActions.containsKey(inputKey))
		{
			conflictedKeyInputActions.add(keyAction);
			return false;
		}
		else
		{
			keyInputActions.put(new Integer(inputKey), keyAction);
			return true;
		}
	}

	/**
	 * Returns the input action for the given input key.
	 * Will return null if there is no input key action.
	 * 
	 * @param inputKey
	 * @return
	 */
	public IKeyInputHandler getInputActionForKey(int inputKey)
	{
		if (!keyInputActions.containsKey(inputKey))
			return null;
		return keyInputActions.get(inputKey);
	}

	public IMouseInputHandler getMouseInputHandler()
	{
		return this.mouseInputHandler;
	}

	public void setMouseInputHandler(IMouseInputHandler mouseInputHandler)
	{
		this.mouseInputHandler = mouseInputHandler;
	}
}
