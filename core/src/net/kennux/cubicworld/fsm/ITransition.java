package net.kennux.cubicworld.fsm;

/**
 * The interface used to implement FSM transitions.
 * 
 * @author kennux
 *
 */
public interface ITransition
{
	/**
	 * Should return true if your transition condition is met.
	 * Causes the state machine to change it's state if true.
	 * 
	 * @return
	 */
	public boolean conditionMet();
}
