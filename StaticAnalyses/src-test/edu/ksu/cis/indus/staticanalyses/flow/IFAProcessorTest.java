
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

package edu.ksu.cis.indus.staticanalyses.flow;

import edu.ksu.cis.indus.processing.IProcessor;


/**
 * This is the common interface to be implemented by unit tests that test pre/post processors of flow analysis information.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public interface IFAProcessorTest
  extends IFATest {
	/**
	 * Set the processor being tested.
	 *
	 * @param processor is to be tested.
	 *
	 * @pre processor != null
	 */
	void setProcessor(IProcessor processor);
}

/*
   ChangeLog:
   $Log$
   Revision 1.4  2004/02/11 09:37:18  venku
   - large refactoring of code based  on testing :-)
   - processing filters can now be chained.
   - ofa xmlizer was implemented.
   - xml-based ofa tester was implemented.
   Revision 1.3  2004/02/08 21:31:41  venku
   - test refactoring to enable same test case to be used as
     unit test case and regression test case
   Revision 1.2  2004/02/08 19:17:19  venku
   - test refactoring for regression testing.
   Revision 1.1  2004/02/08 01:10:33  venku
   - renamed TestSuite classes to ArgTestSuite classes.
   - added DependencyArgTestSuite.
 */
