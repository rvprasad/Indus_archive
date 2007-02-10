
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

import edu.ksu.cis.indus.interfaces.IEnvironment;


/**
 * This is an abstract implementation of <code>IEnvironmentBasedTest</code>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractEnvironmentBasedTest
  extends IndusTestCase
  implements IEnvironmentBasedTest {
	/** 
	 * Enviroment that serves as the basis for the test.
	 */
	protected IEnvironment env;

	/**
	 * Sets the enviroment that serves as the basis for the test.
	 *
	 * @param environment of interest.
	 *
	 * @pre environment != null
	 */
	public void setEnvironment(final IEnvironment environment) {
		env = environment;
	}
}

// End of File
