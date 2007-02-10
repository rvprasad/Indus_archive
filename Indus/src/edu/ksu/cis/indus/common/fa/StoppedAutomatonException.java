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
 * This indicates a transition was attempted on a stopped automaton.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class StoppedAutomatonException
		extends RuntimeException {

	/**
	 * The serial version UID.
	 */
	static final long serialVersionUID = -1694743911300127567L;

	/**
	 * Creates a new StoppedAutomatonException object.
	 */
	public StoppedAutomatonException() {
		super();
	}

	/**
	 * Creates a new StoppedAutomatonException object.
	 * 
	 * @param message of interest.
	 */
	public StoppedAutomatonException(@Immutable final String message) {
		super(message);
	}
}

// End of File
