/*
 * Created on Apr 28, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.ksu.cis.indus.peq.fsm;

import edu.ksu.cis.peq.fsm.interfaces.IFSM;
import edu.ksu.cis.peq.fsm.interfaces.IState;

/**
 * @author ganeshan
 *
 * This is a basic container for an fsm. 
 * Used while performing processing to remove eps transitions, conversion to dfa .. etc.
 */
public class BasicFSM implements IFSM {

	private State initialState;	
	
	/** Get the initial state.
	 * @see edu.ksu.cis.peq.fsm.interfaces.IFSM#getInitialState()
	 */
	public IState getInitialState() {
		return initialState;
	}

	/**
	 * Set the initial state.
	 * @param initialState The initialState to set.
	 */
	public void setInitialState(State initialState) {
		this.initialState = initialState;
	}
}
