
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

package edu.ksu.cis.indus.staticanalyses.flow.instances;

import edu.ksu.cis.indus.TestHelper;

import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;

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
	 * @see FATestSetup#setUp()
	 */
	protected void setUp()
	  throws Exception {
		super.setUp();

		final ValueAnalyzerBasedProcessingController _pc = new ValueAnalyzerBasedProcessingController();
		_pc.setAnalyzer(valueAnalyzer);
		_pc.setEnvironment(valueAnalyzer.getEnvironment());
		_pc.setProcessingFilter(new TagBasedProcessingFilter(FATestSetup.TAG_NAME));
		_pc.setStmtGraphFactory(getStmtGraphFactory());
		cgiImpl = new CallGraph(new PairManager(false, true));
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
	 * @see FATestSetup#tearDown()
	 */
	protected void tearDown()
	  throws Exception {
		cgiImpl.reset();
		cgiImpl = null;
		super.tearDown();
	}
}

// End of File
