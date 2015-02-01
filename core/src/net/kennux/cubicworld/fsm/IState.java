package net.kennux.cubicworld.fsm;

/**
 * The interface used for implementing anonymous or real classes to get used as a state.
 * 
 * @author kennux
 *
 */
public interface IState
{
	/**
	 * <pre>
	 * Gets called if a transition changed the state machine's state to this state.
	 * If the FSM switches for example from idle to work (this), enter will get called with idle as from state.
	 * After enter() was called, NO update() is called in this fsm cycle.
	 * In the next update / cycle, update() will get called.
	 * </pre>
	 * 
	 * @param fromState
	 */
	public void enter(IState fromState);

	/**
	 * <pre>
	 * Gets called if the FSM leaves thi state.
	 * The FSM will first call leave on this state and then enter on the next state (toState) in the same update / cycle.
	 * </pre>
	 * 
	 * @param toState
	 */
	public void leave(IState toState);

	/**
	 * Gets called if this state is currently active in the FSM and enter() was already called a update / cycle before.
	 */
	public void update();
}
