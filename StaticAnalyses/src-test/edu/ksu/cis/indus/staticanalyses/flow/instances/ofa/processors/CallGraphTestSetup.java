
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors;

import edu.ksu.cis.indus.TestHelper;

import edu.ksu.cis.indus.common.graph.SimpleNodeGraph;

import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;

import edu.ksu.cis.indus.staticanalyses.flow.FATestSetup;
import edu.ksu.cis.indus.staticanalyses.processing.ValueAnalyzerBasedProcessingController;

import java.util.Collection;
import java.util.Iterator;

import junit.framework.TestSuite;

import soot.G;


/**
 * This class sets up the call graph once before various tests are run on the call graph.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class CallGraphTestSetup
  extends FATestSetup {
	/**
	 * The call graph implementation to be tested.
	 */
	protected CallGraph cgiImpl;

	/**
	 * Creates a new CallGraphTestSetup object.
	 *
	 * @param test to be run in this set up.
	 */
	CallGraphTestSetup(final TestSuite test) {
		super(test);
		cgiImpl = new CallGraph();
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
		cgiImpl.hookup(_pc);
		_pc.process();
		cgiImpl.unhook(_pc);

        Collection _temp = TestHelper.getTestCasesReachableFromSuite((TestSuite) getTest(), CallGraphTest.class);

        for (final Iterator _i = _temp.iterator(); _i.hasNext();) {
            final CallGraphTest _tester = (CallGraphTest) _i.next();
            _tester.setOFA(valueAnalyzer);
			_tester.setScene(scene);
            _tester.setCallGraphInfo(cgiImpl);
            _tester.setCallGraph((SimpleNodeGraph) cgiImpl.getCallGraph());
		}
	}

	/**
	 * @see TestCase#tearDown()
	 */
	protected void tearDown()
	  throws Exception {
		G.reset();
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/12/31 08:48:59  venku
   - Refactoring.
   - Setup classes setup each tests by data created by a common setup.
   - Tests and Setups are structured such that if test A requires
     data that can be tested by test B then testSetup B can
     be used to drive test A as well.

 */
