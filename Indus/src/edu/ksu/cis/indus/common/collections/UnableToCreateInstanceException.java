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

package edu.ksu.cis.indus.common.collections;

import edu.ksu.cis.indus.annotations.Immutable;

/**
 * This exception is thrown by the factory when object creation fails.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class UnableToCreateInstanceException
		extends RuntimeException {

	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = -5452536480248170725L;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param message to be conveyed.
	 * @param cause to be conveyed.
	 */
	public UnableToCreateInstanceException(@Immutable final String message, @Immutable final Throwable cause) {
		super(message, cause);
	}
}
