/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
 *
 * This software is licensed under the KSU Open Academic License.
 * You should have received a copy of the license with the distribution.
 * A copy can be found at
 *     http://www.cis.ksu.edu/santos/license.html
 * or you can contact the lab at:
 *     SAnToS Laboratory
 *     234 Nichols Hall
 *     Manhattan, KS 66506, USA
 */

package edu.ksu.cis.indus.common.fa;

import edu.ksu.cis.indus.common.graph.IEdgeLabelledNode;
import edu.ksu.cis.indus.common.graph.IObjectDirectedGraph;
import edu.ksu.cis.indus.common.graph.SimpleEdgeGraph;
import edu.ksu.cis.indus.common.graph.SimpleEdgeLabelledNode;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.apache.commons.collections.CollectionUtils;

/**
 * This is an implementation of non-deterministic finite automaton.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$ *
 * @param <S> the type of the implementation of this interface.
 * @param <L> the type of the implementation of this interface.
 */
public class NFA<S extends IState<S>, L extends ITransitionLabel<L>>
		implements IAutomaton<S, L> {

	/**
	 * The current state of the NFA. This is <code>null</code> if the automaton is not running.
	 */
	private S currentState;

	/**
	 * The final states of this automaton.
	 * 
	 * @invariant finalStates.oclIsKindOf(Set(IState))
	 */
	private final Collection<S> finalStates = new HashSet<S>();

	/**
	 * This graph is used to represent the shape of the automaton.
	 */
	private final SimpleEdgeGraph<S> seg = new SimpleEdgeGraph<S>();

	/**
	 * The start state of the the automaton.
	 */
	private S startState;

	/**
	 * Creates an instance of this class.
	 */
	public NFA() {
		super();
	}

	/**
	 * Adds a final state to the automaton.
	 * 
	 * @param state to be added as a final state.
	 * @pre state != null
	 */
	public void addFinalState(final S state) {
		assert state != null;

		seg.getNode(state);
		finalStates.add(state);
	}

	/**
	 * Adds a transition labelled with the given label from the given source state to the destination state.
	 * 
	 * @param src is the source state.
	 * @param label is the label of the transition.
	 * @param dest is the destination state.
	 * @throws IllegalStateException if the automaton is altered while it's running, i.e. <code>start()</code> has been
	 *             called but <code>stop()</code> has not been called.
	 */
	public void addLabelledTransitionFromTo(final S src, final L label, final S dest) {
		if (currentState != null) {
			throw new IllegalStateException(
					"The automata should be altered when it is not not running (prior to starting it or"
							+ "after it is stopped).");
		}
		seg.addEdgeFromTo(seg.getNode(src), label, seg.getNode(dest));
	}

	/**
	 * @see IAutomaton#canPerformTransition(ITransitionLabel)
	 */
	public boolean canPerformTransition(final L label) {
		final IEdgeLabelledNode _node = seg.queryNode(currentState);
		final boolean _result;

		if (_node != null) {
			_result = _node.hasOutgoingEdgeLabelled(label);
		} else {
			_result = false;
		}
		return _result;
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	@Override public NFA<S, L> clone() {
		try {
			return (NFA) super.clone();
		} catch (final CloneNotSupportedException _e) {
			throw new RuntimeException(_e);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.common.fa.IAutomaton#getCurrentState()
	 */
	public S getCurrentState() {
		return currentState;
	}

	/**
	 * @see edu.ksu.cis.indus.common.fa.IAutomaton#getFinalStates()
	 */
	public Collection<S> getFinalStates() {
		return finalStates;
	}

	/**
	 * Retrieves the states that can be reached by taking a transition labelled with the given label from the given state of
	 * the automaton.
	 * 
	 * @param state of interest.
	 * @param label of the outgoing transitions from <code>state</code>.
	 * @return the collection of states.
	 * @pre state != null and label != null
	 * @post result != null
	 */
	public Collection<S> getResultingStates(final S state, final L label) {
		assert state != null && label != null;

		final SimpleEdgeLabelledNode<S> _node = seg.queryNode(state);
		final Collection<S> _result;

		if (_node != null) {
			final Collection<SimpleEdgeLabelledNode<S>> _dests = _node.getSuccsViaEdgesLabelled(label);
			_result = CollectionUtils.collect(_dests, IObjectDirectedGraph.OBJECT_EXTRACTOR);
		} else {
			_result = Collections.emptySet();
		}
		return _result;
	}

	/**
	 * Retrieves the start state of the automaton.
	 * 
	 * @return the start state.
	 */
	public S getStartState() {
		return startState;
	}

	/**
	 * @see edu.ksu.cis.indus.common.fa.IAutomaton#isDeterministic()
	 */
	public boolean isDeterministic() {
		return false;
	}

	/**
	 * @see edu.ksu.cis.indus.common.fa.IAutomaton#isInFinalState()
	 */
	public boolean isInFinalState() {
		return finalStates.contains(currentState);
	}

	/**
	 * @see IAutomaton#performTransitionOn(ITransitionLabel)
	 */
	public void performTransitionOn(final L label) {
		final SimpleEdgeLabelledNode<S> _node = seg.queryNode(currentState);

		if (_node != null) {
			final Collection<SimpleEdgeLabelledNode<S>> _dests = _node.getSuccsViaEdgesLabelled(label);
			currentState = _dests.iterator().next().getObject();
		} else {
			throw new IllegalStateException("There are no transition with the label " + label + " from the current state.");
		}
	}

	/**
	 * Removes the transition labelled with the given label from the given source state to the destination state.
	 * 
	 * @param src is the source state.
	 * @param label of the transition to be removed.
	 * @param dest is the destination state.
	 * @return <code>true</code> if such a transition existed and it was removed; <code>false</code>, otherwise.
	 * @throws IllegalStateException if the automaton is altered while it's running, i.e. <code>start()</code> has been
	 *             called but <code>stop()</code> has not been called.
	 */
	public boolean removeLabelledTransitionFromTo(final S src, final L label, final S dest) {
		if (currentState != null) {
			throw new IllegalStateException("The automata should be altered when it is not active (prior to starting it or"
					+ "after it is stopped).");
		}
		return seg.removeEdgeFromTo(seg.queryNode(src), label, seg.queryNode(dest));
	}

	/**
	 * Sets the start state of the automaton.
	 * 
	 * @param state to be the start state.
	 * @pre state != null
	 */
	public void setStartState(final S state) {
		assert state != null;

		seg.getNode(state);
		startState = state;
	}

	/**
	 * @see edu.ksu.cis.indus.common.fa.IAutomaton#start()
	 */
	public void start() {
		currentState = startState;
	}

	/**
	 * @see edu.ksu.cis.indus.common.fa.IAutomaton#stop()
	 */
	public void stop() {
		currentState = null;
	}
}

// End of File
