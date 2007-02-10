/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

package edu.ksu.cis.indus.common.fa;

import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;

import java.util.Collection;

/**
 * This is the interface to a finite automaton.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <S> the type of the states.
 * @param <L> the type of the labels.
 */
public interface IAutomaton<S extends IState<S>, L extends ITransitionLabel<L>>
		extends Cloneable {

	/**
	 * Checks if the automaton can perform a transition labelled with the given label in the current state.
	 * 
	 * @param label of the requested transition.
	 * @return <code>true</code> if there is a transition from the current state of the automaton; <code>false</code>,
	 *         otherwise.
	 */
	boolean canPerformTransition(@Immutable L label);

	/**
	 * Clones this automaton.
	 * 
	 * @return the clone.
	 * @post result != null
	 */
	IAutomaton<S, L> clone();

	/**
	 * Retrieves the current state of the automaton. This method will return <code>null</code> if invoked before invoking
	 * <code>initialize</code> for the first time or between the invocation of <code>reset()</code> and
	 * <code>initialize()</code> with no intermediate invocations of <code>initialize()</code>.
	 * 
	 * @return the current state.
	 */
	S getCurrentState();

	/**
	 * Retrieves the final states of this automaton.
	 * 
	 * @return a collection of states.
	 */
	@NonNull Collection<S> getFinalStates();

	/**
	 * Retrieves the start state of the automaton.
	 * 
	 * @return the start state.
	 */
	S getStartState();

	/**
	 * Checks if the automaton is deterministic.
	 * 
	 * @return <code>true</code> if it is deterministic; <code>false</code>, otherwise.
	 */
	boolean isDeterministic();

	/**
	 * Checks if the automaton is in it's final state.
	 * 
	 * @return <code>true</code> if it is in a final state; <code>false</code>, otherwise.
	 */
	boolean isInFinalState();

	/**
	 * Performs a transition with the given label from the current state. This method will change the current state of the
	 * automaton.
	 * 
	 * @param label of the outgoing transition from the current state.
	 * @throws UnavailableTransitionException when there are no transitions of the given label from the current state.
	 * @throws StoppedAutomatonException when a transition is attempted on an stopped automaton.
	 */
	void performTransitionOn(@Immutable L label) throws UnavailableTransitionException, StoppedAutomatonException;

	/**
	 * Starts the automaton. This sets up the current state to the initial state.
	 */
	void start();

	/**
	 * Halts the automaton. This clears the current state.
	 */
	void stop();
}

// End of File
