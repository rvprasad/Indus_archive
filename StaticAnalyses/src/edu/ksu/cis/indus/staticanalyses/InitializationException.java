
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
