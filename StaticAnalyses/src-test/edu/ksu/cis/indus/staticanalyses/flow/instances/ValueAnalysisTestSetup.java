
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

package edu.ksu.cis.indus.staticanalyses.flow.instances;

import edu.ksu.cis.indus.TestHelper;

import edu.ksu.cis.indus.common.soot.IStmtGraphFactory;

import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;

import edu.ksu.cis.indus.staticanalyses.flow.FATestSetup;
import edu.ksu.cis.indus.staticanalyses.flow.IFAProcessorTest;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.CallGraph;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.CallGraphTest;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.XMLBasedCallGraphTest;
import edu.ksu.cis.indus.staticanalyses.processing.ValueAnalyzerBasedProcessingController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import junit.framework.TestSuite;


/**
 * This class sets up the data once before various tests are run on them.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class ValueAnalysisTestSetup
  extends FATestSetup {
	/**
	 * The call graph implementation to be tested.
	 */
	protected CallGraph cgiImpl;

	/**
	 * @see FATestSetup#FATestSetup(TestSuite,String,String)
	 */
	public ValueAnalysisTestSetup(final TestSuite test, final String theNameOfClasses, final String classpath) {
		super(test, theNameOfClasses, classpath);
	}

	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		super.setUp();

		final ValueAnalyzerBasedProcessingController _pc = new ValueAnalyzerBasedProcessingController();
		_pc.setAnalyzer(valueAnalyzer);
		_pc.setEnvironment(valueAnalyzer.getEnvironment());
		_pc.setProcessingFilter(new TagBasedProcessingFilter(FATestSetup.TAG_NAME));
		_pc.setStmtGraphFactory(getStmtGraphFactory());
		cgiImpl = new CallGraph();
		cgiImpl.hookup(_pc);
		_pc.process();
		cgiImpl.unhook(_pc);

		final Collection _temp =
			new ArrayList(TestHelper.getTestCasesReachableFromSuite((TestSuite) getTest(), CallGraphTest.class));
		_temp.addAll(TestHelper.getTestCasesReachableFromSuite((TestSuite) getTest(), XMLBasedCallGraphTest.class));

		for (final Iterator _i = _temp.iterator(); _i.hasNext();) {
			final IFAProcessorTest _test = (IFAProcessorTest) _i.next();
			_test.setProcessor(cgiImpl);
		}
	}

	/**
	 * @see TestCase#tearDown()
	 */
	protected void tearDown()
	  throws Exception {
		cgiImpl.reset();
		cgiImpl = null;
		super.tearDown();
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2004/03/09 18:40:03  venku
   - refactoring.
   - moved methods common to XMLBased Test into AbstractXMLBasedTest.
   Revision 1.1  2004/03/07 20:27:54  venku
   - refactoring! refactoring!
   - generalized OFA Test base to be applicable to any value flow
     analysis built on top of FA.
   Revision 1.6  2004/02/11 09:37:18  venku
   - large refactoring of code based  on testing :-)
   - processing filters can now be chained.
   - ofa xmlizer was implemented.
   - xml-based ofa tester was implemented.
   Revision 1.5  2004/02/09 01:20:10  venku
   - coding convention.
   - added a new abstract class contain the logic required for xml-based
     testing.  (AbstractXMLBasedTest)
   - added a new xml-based call graph testing class.
   Revision 1.4  2004/02/08 21:31:41  venku
   - test refactoring to enable same test case to be used as
     unit test case and regression test case
   Revision 1.3  2004/02/08 19:53:31  venku
   - it should be tearDown and not teardown. FIXED.
   Revision 1.2  2004/02/08 19:17:19  venku
   - test refactoring for regression testing.
   Revision 1.1  2004/02/08 04:53:10  venku
   - refactoring!!!
   - All regression tests implement IXMLBasedTest.
   - All test setups extends AbstractXMLBasedTestSetup.
   - coding convention.
   - all tests occur at the same package as the classes
     being tested.
   Revision 1.1  2004/02/08 02:21:21  venku
   - renamed package instances.ofa.processors to instances.ofa.
   - renamed OFAProcessorArgTestSuite to OFAProcessorTestSuite.
   Revision 1.2  2004/02/08 02:08:25  venku
   - coding conventions.
   Revision 1.1  2004/02/08 01:10:33  venku
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
