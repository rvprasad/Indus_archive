
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

import java.util.Collection;


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
  extends AbstractAutomata
  implements IDeterministicAutomata {
	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param state DOCUMENT ME!
	 * @param label DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 *
	 * @throws IllegalStateException DOCUMENT ME!
	 */
	public IState getResultingState(final IState state, final ITransitionLabel label) {
		final Collection _t = super.getResultingStates(state, label);

		if (_t.isEmpty()) {
			final String _msg = "There is no transition labelled '" + label + "' from the given state (" + state + ")";
			throw new IllegalStateException(_msg);
		}
		return (IState) _t.iterator().next();
	}

	/**
	 * @see fse05.AbstractAutomata#addLabelledTransitionFromTo(fse05.IState, fse05.IAutomata.ITransitionLabel, fse05.IState)
	 */
	protected void addLabelledTransitionFromTo(final IState src, final ITransitionLabel label, final IState dest) {
		final Collection _states = getResultingStates(src, label);

		if (!_states.isEmpty()) {
			final String _msg = "A transition labelled '" + label + "' already exists from the given source (" + src + ")";
			throw new IllegalStateException(_msg);
		} else if (label.equals(EPSILON)) {
			final String _msg = "Epsilon transitions are not allowed in Deterministic automata.";
			throw new IllegalArgumentException(_msg);
		} else {
			super.addLabelledTransitionFromTo(src, label, dest);
		}
	}
}

// End of File
