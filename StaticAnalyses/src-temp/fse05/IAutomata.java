
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
 * @author <a href="$user_web$">$user_name$</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IAutomata
  extends Cloneable {
	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	Collection getFinalStates();

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	boolean isInFinalState();

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	IState getStartState();

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param label DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	boolean canPerformTransition(ILabel label);

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	Object clone();

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param label DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	void performTransitionOn(ILabel label);
}

// End of File
