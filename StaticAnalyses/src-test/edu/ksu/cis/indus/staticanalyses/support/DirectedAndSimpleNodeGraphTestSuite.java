
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

package edu.ksu.cis.indus.staticanalyses.support;

import junit.framework.Test;
import junit.framework.TestSuite;


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
	 * Creates the test suite.
	 *
	 * @return the created test suite.
	 *
	 * @post result != null
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for edu.ksu.cis.indus.staticanalyses.support");

		//$JUnit-BEGIN$
		suite.addTestSuite(DirectedAndSimpleNodeGraphTest.class);
		suite.addTestSuite(JikesBasedDirectedAndSimpleNodeGraphTest.class);
		suite.addTestSuite(JavacBasedDirectedAndSimpleNodeGraphTest.class);
		//$JUnit-END$
		return suite;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2003/11/10 03:39:53  venku
   - renamed test2 and test3 to JikesBased and JavacBased tests.

   Revision 1.2  2003/09/28 23:19:36  venku
   *** empty log message ***

   Revision 1.1  2003/09/11 02:37:12  venku
   - added a test case for javac compilation of Divergent04 test.
   - created test suite to test directed and simple node graph.
 */
