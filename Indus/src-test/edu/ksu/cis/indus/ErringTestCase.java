
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

package edu.ksu.cis.indus;

import junit.framework.TestCase;


/**
 * This is a test case which will always cause an error via an exception.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class ErringTestCase
  extends TestCase {
	/** 
	 * The exception to be thrown.
	 */
	final RuntimeException exception;

	/**
	 * Creates a new ErringTestCase object.
	 *
	 * @param message to be embedded in the exception to be thrown.
	 */
	public ErringTestCase(final String message) {
		exception = new RuntimeException(message);
	}

	/**
	 * This is the erring test.
	 */
	public void testError() {
		throw exception;
	}
}

// End of File
