
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

package edu.ksu.cis.indus.staticanalyses.dependency;

import edu.ksu.cis.indus.interfaces.IEnvironment;

import junit.framework.Test;


/**
 * This is the interface of unit tests that test instances of dependency analysis.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
interface IDependencyAnalysisTest
  extends Test {
	/**
	 * Retrieves the analysis instance being tested.
	 *
	 * @return the instance of the analysis.
	 */
	IDependencyAnalysis getDA();

	/**
	 * Retrieves the environment which the analysis analyzed.
	 *
	 * @param environment that was analyzed.
	 */
	void setEnvironment(IEnvironment environment);
}

// End of File
