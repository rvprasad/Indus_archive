
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

import edu.ksu.cis.indus.common.soot.SootBasedDriver;

import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;

import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.tokens.BitSetTokenManager;
import edu.ksu.cis.indus.staticanalyses.tokens.SootValueTypeManager;

import edu.ksu.cis.indus.xmlizer.JimpleXMLizerCLI;

import java.util.Collection;
import java.util.Iterator;

import junit.framework.TestSuite;

import soot.G;


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
	public static final String TAG_NAME = "indus.staticanalyses.flow.FATestSetup:FA";

	/**
	 * The value analyzer used during testing.
	 */
	protected IValueAnalyzer valueAnalyzer;

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
		valueAnalyzer = OFAnalyzer.getFSOIAnalyzer(FATestSetup.TAG_NAME, new BitSetTokenManager(new SootValueTypeManager()));
		sootClassPath = classpath;
		classNames = theNameOfClasses;
	}

	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		super.setUp();

		final SootBasedDriver _driver = new SootBasedDriver();
		_driver.addToSootClassPath(sootClassPath);
		_driver.setClassNames(classNames.toString().split(" "));
		_driver.initialize();
		valueAnalyzer.analyze(_driver.getScene(), _driver.getRootMethods());

		final Collection _temp = TestHelper.getTestCasesReachableFromSuite((TestSuite) getTest(), IFATest.class);

		for (final Iterator _i = _temp.iterator(); _i.hasNext();) {
			final IFATest _test = (IFATest) _i.next();
			_test.setFA(((AbstractAnalyzer) valueAnalyzer).fa);
			_test.setAnalyzer(valueAnalyzer);
			_test.setFATagName(TAG_NAME);
		}

		if (dumpLocation != null) {
			JimpleXMLizerCLI.writeJimpleAsXML(_driver.getScene(), dumpLocation, null, idGenerator,
				new TagBasedProcessingFilter(TAG_NAME));
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
		idGenerator.reset();
		super.tearDown();
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.22  2004/05/10 08:12:03  venku
   - streamlined the names of tags that are used.
   - deleted SlicingTag class.  NamedTag is used instead.
   - ripple effect.
   - SliceCriteriaFactory's interface is enhanced to generate individual
     slice criterion as well as criteria set for all nodes in the given AST chunk.
   Revision 1.21  2004/04/25 23:18:18  venku
   - coding conventions.
   Revision 1.20  2004/04/25 21:18:37  venku
   - refactoring.
     - created new classes from previously embedded classes.
     - xmlized jimple is fragmented at class level to ease comparison.
     - id generation is embedded into the testing framework.
     - many more tiny stuff.
   Revision 1.19  2004/04/22 22:12:06  venku
   - made changes to jimple xmlizer to dump each class into a separate file.
   - ripple effect.
   Revision 1.18  2004/04/21 04:13:20  venku
   - jimple dumping takes time.  Instead, the user can control this
     per configuration.
   Revision 1.17  2004/04/20 06:53:17  venku
   - documentation.
   Revision 1.16  2004/04/19 05:10:26  venku
   - NPE's in test setup caused by unchecked reseting.
   Revision 1.15  2004/04/18 02:05:18  venku
   - memory leak fixes.
   Revision 1.14  2004/04/18 00:42:56  venku
   - references to objects had leaked after test. FIXED.
   Revision 1.13  2004/04/18 00:02:19  venku
   - added support to dump jimple.xml while testing.
   Revision 1.12  2004/04/16 20:10:39  venku
   - refactoring
    - enabled bit-encoding support in indus.
    - ripple effect.
    - moved classes to related packages.
   Revision 1.11  2004/04/02 09:58:28  venku
   - refactoring.
     - collapsed flow insensitive and sensitive parts into common classes.
     - coding convention
     - documentation.
   Revision 1.10  2004/02/11 09:37:18  venku
   - large refactoring of code based  on testing :-)
   - processing filters can now be chained.
   - ofa xmlizer was implemented.
   - xml-based ofa tester was implemented.
   Revision 1.9  2004/02/08 21:31:41  venku
   - test refactoring to enable same test case to be used as
     unit test case and regression test case
   Revision 1.8  2004/02/08 19:53:31  venku
   - it should be tearDown and not teardown. FIXED.
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
