
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

import edu.ksu.cis.indus.AbstractXMLBasedTestSetup;
import edu.ksu.cis.indus.TestHelper;

import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import junit.framework.TestSuite;

import soot.ArrayType;
import soot.G;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.VoidType;


/**
 * This is the setup in which various tests of flow analyses are run.  The classes to be processed during the test can be
 * configured via the command line or via specifying <code>FARegressionTestSuite.FA_TEST_PROPERTIES_FILE</code> system
 * property. The syntax for both these options is a space separated list of class names.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class FATestSetup
  extends AbstractXMLBasedTestSetup {
	/**
	 * The tag used by the flow analysis instance.
	 */
	public static final String TAG_NAME = "FATestSetup:TestTag";

	/**
	 * The value analyzer used during testing.
	 */
	protected IValueAnalyzer valueAnalyzer;

	/**
	 * The scene used during testing.
	 */
	protected Scene scene;

	/**
	 * The names of the class to analyze.
	 */
	protected final String classNames;

	/**
	 * The class path to use with soot in this setup.
	 */
	private final String sootClassPath;

	/**
	 * Creates a new FATestSetup object.
	 *
	 * @param test is the test to run in this setup.
	 * @param theNameOfClasses is the list of classes.
	 * @param classpath to be used to find the classes.
	 *
	 * @pre test != null and theNameOfClasses != null
	 */
	protected FATestSetup(final TestSuite test, final String theNameOfClasses, final String classpath) {
		super(test);
		valueAnalyzer = OFAnalyzer.getFSOSAnalyzer(FATestSetup.TAG_NAME);
		scene = Scene.v();
		sootClassPath = classpath;
		classNames = theNameOfClasses;
	}

	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		super.setUp();

		final String[] _j = classNames.toString().split(" ");
		final Collection _rootMethods = new ArrayList();

		scene.setSootClassPath(sootClassPath);

		for (int _i = _j.length - 1; _i >= 0; _i--) {
			final SootClass _sc = scene.loadClassAndSupport(_j[_i]);

			if (_sc.declaresMethod("main", Collections.singletonList(ArrayType.v(RefType.v("java.lang.String"), 1)),
					  VoidType.v())) {
				final SootMethod _sm =
					_sc.getMethod("main", Collections.singletonList(ArrayType.v(RefType.v("java.lang.String"), 1)),
						VoidType.v());

				if (_sm.isPublic() && _sm.isConcrete()) {
					_rootMethods.add(_sm);
				}
			}
		}

		valueAnalyzer.analyze(scene, _rootMethods);

		final Collection _temp = TestHelper.getTestCasesReachableFromSuite((TestSuite) getTest(), FATest.class);

		for (final Iterator _i = _temp.iterator(); _i.hasNext();) {
			final FATest _tester = (FATest) _i.next();
			_tester.setFA(((AbstractAnalyzer) valueAnalyzer).fa);
			_tester.setFATagName(TAG_NAME);
		}
	}

	/**
	 * @see TestCase#teardown()
	 */
	protected void tearDown()
	  throws Exception {
		G.reset();
		valueAnalyzer.reset();
		valueAnalyzer = null;
		scene = null;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.7  2004/02/08 19:17:19  venku
   - test refactoring for regression testing.
   Revision 1.6  2004/02/08 05:28:49  venku
   - class names was not initialized properly.
   Revision 1.5  2004/02/08 04:53:10  venku
   - refactoring!!!
   - All regression tests implement IXMLBasedTest.
   - All test setups extends AbstractXMLBasedTestSetup.
   - coding convention.
   - all tests occur at the same package as the classes
     being tested.
   Revision 1.4  2004/02/08 02:38:19  venku
   - added a new constructor for batch testing.
   Revision 1.3  2004/02/08 01:10:33  venku
   - renamed TestSuite classes to ArgTestSuite classes.
   - added DependencyArgTestSuite.
   Revision 1.2  2004/01/03 19:52:54  venku
   - renamed CallGraphInfoTest to CallGraphTest
   - all tests of a kind have to be exposed via a suite like
     FATestSuite or OFAProcessorArgTestSuite.  This is to enable
     automated testing.
   - all properties should start with indus and not edu.ksu.cis.indus...
   Revision 1.1  2003/12/31 08:48:59  venku
   - Refactoring.
   - Setup classes setup each tests by data created by a common setup.
   - Tests and Setups are structured such that if test A requires
     data that can be tested by test B then testSetup B can
     be used to drive test A as well.
 */
