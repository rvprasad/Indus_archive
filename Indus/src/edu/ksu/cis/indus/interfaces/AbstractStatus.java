
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
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

package edu.ksu.cis.indus.interfaces;

/**
 * An abstract implementation of <code>IStatus</code>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractStatus
  implements IStatus {
	/**
	 * Thi captures the status.
	 */
	private boolean status;

	/**
	 * @see edu.ksu.cis.indus.interfaces.IStatus#isStable()
	 */
	public final synchronized boolean isStable() {
		return status;
	}

	/**
	 * Sets the status of this object as <i>stable</i>.
	 */
	protected final synchronized void stable() {
		status = true;
	}

	/**
	 * Sets the status of this object as <i>unstable</i>.
	 */
	protected final synchronized void unstable() {
		status = false;
	}
}

/*
   ChangeLog:
   $Log$
 */
