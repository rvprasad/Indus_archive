
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
