
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

package fse05;

import edu.ksu.cis.indus.common.graph.IObjectDirectedGraph.IObjectNode;

import java.util.Collection;
import java.util.HashSet;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class DFA
  implements IAutomata {
	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final Collection finalStates = new HashSet();

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final SimpleEdgeGraph sng = new SimpleEdgeGraph();

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private IState currentState;

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private IState startState;

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	public Collection getFinalStates() {
		return finalStates;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	public boolean isInFinalState() {
		return finalStates.contains(currentState);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param state DOCUMENT ME!
	 */
	public void setStartState(final IState state) {
		assert startState == null;
		sng.getNode(state);
		startState = state;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	public IState getStartState() {
		return startState;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param state DOCUMENT ME!
	 */
	public void addFinalState(final IState state) {
		sng.getNode(state);
		finalStates.add(state);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param src DOCUMENT ME!
	 * @param label DOCUMENT ME!
	 * @param dest DOCUMENT ME!
	 */
	public void addLabelledTransitionFromTo(final IState src, final ILabel label, final IState dest) {
		sng.addEdgeFromTo(sng.getNode(src), label, sng.getNode(dest));
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param label DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public boolean canPerformTransition(final ILabel label) {
		return sng.hasOutgoingEdgeLabelled(sng.queryNode(currentState), label);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 *
	 * @throws RuntimeException DOCUMENT ME!
	 */
	public Object clone() {
		try {
			return (DFA) super.clone();
		} catch (final CloneNotSupportedException _e) {
			throw new RuntimeException(_e);
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	public void initialize() {
		currentState = startState;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param label DOCUMENT ME!
	 */
	public void performTransitionOn(final ILabel label) {
		final Collection _dests = sng.getDestOfOutgoingEdgeLabelled(sng.queryNode(currentState), label);
		currentState = (IState) ((IObjectNode) _dests.iterator().next()).getObject();
	}
}

// End of File
