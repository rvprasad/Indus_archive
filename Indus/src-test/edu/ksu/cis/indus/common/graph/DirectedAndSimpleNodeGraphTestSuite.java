
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

package edu.ksu.cis.indus.common.graph;

import junit.framework.Test;
import junit.framework.TestSuite;

import junit.swingui.TestRunner;


/**
 * This is the test suite for <code>DirectedGraph</code> and <code>SimpleNodeGraph</code>.  Any new test cases should add to
 * this suite.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class DirectedAndSimpleNodeGraphTestSuite {
	/**
	 * Creates a new DirectedAndSimpleNodeGraphTestSuite object.
	 */
	private DirectedAndSimpleNodeGraphTestSuite() {
	}

	/**
	 * Executes the test case.
	 *
	 * @param s is ignored.
	 */
	public static void main(final String[] s) {
		final TestRunner _runner = new TestRunner();
		_runner.setLoading(false);
		_runner.start(new String[0]);
		_runner.startTest(suite());
		_runner.runSuite();
	}

	/**
	 * Creates the test suite.
	 *
	 * @return the created test suite.
	 *
	 * @post result != null
	 */
	public static Test suite() {
		final TestSuite _suite = new TestSuite("Test for edu.ksu.cis.indus.common.graph");

		//$JUnit-BEGIN$
		_suite.addTestSuite(DirectedAndSimpleNodeGraphTest.class);
		_suite.addTestSuite(JikesBasedDirectedAndSimpleNodeGraphTest.class);
		_suite.addTestSuite(JavacBasedDirectedAndSimpleNodeGraphTest.class);
		//$JUnit-END$
		return _suite;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/12/09 04:22:03  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.

   Revision 1.1  2003/12/08 12:15:48  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.

   Revision 1.6  2003/12/02 09:42:34  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2

   Revision 1.5  2003/11/29 08:13:38  venku
   - added support to execute the test suite from command line.
   Revision 1.4  2003/11/10 03:40:50  venku
   - renamed DirectedAndSimpleNodeGraphTest1 to
     DirectedAndSimpleNodeGraphTest.
   Revision 1.3  2003/11/10 03:39:53  venku
   - renamed test2 and test3 to JikesBased and JavacBased tests.
   Revision 1.2  2003/09/28 23:19:36  venku
 *** empty log message ***
     Revision 1.1  2003/09/11 02:37:12  venku
     - added a test case for javac compilation of Divergent04 test.
     - created test suite to test directed and simple node graph.
 */
