
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
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

import edu.ksu.cis.indus.IXMLBasedTest;

import edu.ksu.cis.indus.interfaces.IEnvironment;


/**
 * This is the interface of unit tests that test instances of dependency analysis.
 *
 * @author <a href="$user_web$">$user_name$</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IDependencyAnalysisTest
  extends IXMLBasedTest {
	/**
	 * Retrieves the analysis instance being tested.
	 *
	 * @return the instance of the analysis.
	 */
	DependencyAnalysis getDA();

	/**
	 * Retrieves the environment which the analysis analyzed.
	 *
	 * @param environment that was analyzed.
	 */
	void setEnvironment(IEnvironment environment);
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2004/03/29 09:44:41  venku
   - finished the xml-based testing framework for dependence.
   Revision 1.1  2004/03/09 19:10:40  venku
   - preliminary commit of test setup for dependency analyses.
 */
