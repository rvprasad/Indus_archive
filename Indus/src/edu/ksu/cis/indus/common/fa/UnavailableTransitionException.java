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

package edu.ksu.cis.indus.common.fa;

import edu.ksu.cis.indus.annotations.Immutable;

/**
 * This indicates an unavailable transition was attempted from the current state of an automaton.
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
	public UnavailableTransitionException(@Immutable final String message) {
		super(message);
	}
}

// End of File
