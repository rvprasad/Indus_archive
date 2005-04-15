
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
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class NFA
  extends AbstractAutomata
  implements INonDeterministicAutomata {
	/**
	 * @see fse05.AbstractAutomata#getResultingStates(fse05.IState, fse05.IAutomata.ITransitionLabel)
	 */
	public Collection getResultingStates(final IState state, final ITransitionLabel label) {
		return super.getResultingStates(state, label);
	}
}

// End of File
