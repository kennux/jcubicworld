package net.kennux.cubicworld.fsm;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * <pre>
 * This is a very basic finite state machine implementation.
 * It can get used to implement ai or other things which could need a state machine.
 * 
 * Important: If you implement a finite state machine, don't forget to overload the FiniteStateMachine() constructor :-)
 * </pre>
 * @author kennux
 *
 */
public abstract class FiniteStateMachine
{
	/**
	 * The states.
	 * Contains all states.
	 */
	private ArrayList<IState> states;

	/**
	 * Contains all transitions.
	 * Entry's key is the transition state from, value is transition state to.
	 */
	private HashMap<Entry<IState, IState>, ITransition> transitions;

	/**
	 * Contains the current state's array list (states) index.
	 */
	private int currentState = 0;

	/**
	 * Overload this constructor!
	 * You must register your states and transitions in your constructor.
	 */
	public FiniteStateMachine()
	{
		this.states = new ArrayList<IState>();
		this.transitions = new HashMap<Entry<IState, IState>, ITransition>();
	}

	/**
	 * @return the currentState
	 */
	public int getCurrentState()
	{
		return currentState;
	}

	/**
	 * Register a state to this state machine.
	 * Returns it's state index in the states list.
	 * 
	 * @param state
	 */
	protected int registerState(IState state)
	{
		this.states.add(state);
		return this.states.indexOf(state);
	}

	/**
	 * Registers a transition from one to another state.
	 * 
	 * @param transition
	 * @param fromState
	 * @param toState
	 */
	protected void registerTransition(ITransition transition, IState fromState, IState toState)
	{
		this.transitions.put(new SimpleEntry<IState, IState>(fromState, toState), transition);
	}

	/**
	 * @param currentState
	 *            the currentState to set
	 */
	public void setCurrentState(int currentState)
	{
		this.currentState = currentState;
	}

	/**
	 * <pre>
	 * Performs one fsm cycle.
	 * One cycle can update the current state or change the fsm's state from one state to another.
	 * If changing from one state to another, first the leave method on the current state will get called, then the enter method on the next state will get called.
	 * </pre>
	 */
	public void update()
	{
		// Get current state
		IState currentState = this.states.get(this.getCurrentState());

		// Check all conditions
		for (Entry<Entry<IState, IState>, ITransition> entry : this.transitions.entrySet())
		{
			// Check?
			if (entry.getKey().getKey() == currentState)
			{
				if (entry.getValue().conditionMet())
				{
					// TRANSITION!!
					currentState.leave(entry.getKey().getValue());
					this.setCurrentState(this.states.indexOf(entry.getKey().getValue()));
					entry.getKey().getValue().enter(currentState);
					return;
				}
			}
		}

		// Update state
		currentState.update();
	}
}