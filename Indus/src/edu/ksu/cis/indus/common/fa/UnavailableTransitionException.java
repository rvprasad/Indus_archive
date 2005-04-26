
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
 * This indicates an unavailable transition was attempted from the current state of  an automaton.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class UnavailableTransitionException
  extends RuntimeException {
	/** 
	 * The serial version UID.
	 */
	static final long serialVersionUID = 2850268151619025469L;

	/**
	 * Creates an instance of this class.
	 */
	public UnavailableTransitionException() {
		super();
	}

	/**
	 * Creates an instance of this class.
	 *
	 * @param message of interest.
	 */
	public UnavailableTransitionException(final String message) {
		super(message);
	}
}

// End of File
