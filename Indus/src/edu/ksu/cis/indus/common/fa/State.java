
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

/**
 * This represents an object as a state in a finite state automata.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class State
  implements IState {
	/** 
	 * The object being represented by this state.
	 */
	final Object object;

	/**
	 * Creates a new State object.
	 *
	 * @param obj is the object to be represented by this state.
	 */
	public State(final Object obj) {
		object = obj;
	}
}

// End of File
