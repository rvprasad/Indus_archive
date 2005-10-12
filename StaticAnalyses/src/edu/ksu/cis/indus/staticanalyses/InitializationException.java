
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

package edu.ksu.cis.indus.staticanalyses;

/**
 * This exception can be thrown when initialization fails.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class InitializationException
  extends Exception {
	/**
	 * Serialization version id.
	 */
	private static final long serialVersionUID = -1718070913188121533L;

	/**
	 * Creates a new InitializationException object.
	 *
	 * @param message the detailed message.
	 *
	 * @pre message != null
	 */
	public InitializationException(final String message) {
		super(message);
	}

	/**
	 * Creates a new InitializationException object. >
	 *
	 * @param message the detailed message.
	 * @param e the cause for the exception.
	 *
	 * @pre message != null and e != null
	 */
	public InitializationException(final String message, final Throwable e) {
		super(message, e);
	}

	/**
	 * Creates a new InitializationException object.
	 *
	 * @param e the cause for the exception.
	 *
	 * @pre e != null
	 */
	public InitializationException(final Throwable e) {
		super(e);
	}
}

// End of File
