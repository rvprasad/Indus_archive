
/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

package edu.ksu.cis.indus.staticanalyses.flow.instances;

import edu.ksu.cis.indus.TestHelper;

import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;

import edu.ksu.cis.indus.processing.OneAllStmtSequenceRetriever;
import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;

import edu.ksu.cis.indus.staticanalyses.callgraphs.CallGraphInfo;
import edu.ksu.cis.indus.staticanalyses.callgraphs.ICallGraphTest;
import edu.ksu.cis.indus.staticanalyses.callgraphs.OFABasedCallInfoCollector;
import edu.ksu.cis.indus.staticanalyses.callgraphs.XMLBasedCallGraphTest;
import edu.ksu.cis.indus.staticanalyses.flow.FATestSetup;
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
	protected CallGraphInfo cgiImpl;

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
		final OneAllStmtSequenceRetriever _ssr = new OneAllStmtSequenceRetriever();
		_ssr.setStmtGraphFactory(getStmtGraphFactory());
		_pc.setStmtSequencesRetriever(_ssr);
		_pc.setAnalyzer(valueAnalyzer);
		_pc.setEnvironment(valueAnalyzer.getEnvironment());
		_pc.setProcessingFilter(new TagBasedProcessingFilter(FATestSetup.TAG_NAME));
		cgiImpl = new CallGraphInfo(new PairManager(false, true));

		final OFABasedCallInfoCollector _ofaci = new OFABasedCallInfoCollector();
		_ofaci.hookup(_pc);
		_pc.process();
		_ofaci.unhook(_pc);
		cgiImpl.createCallGraphInfo(_ofaci.getCallInfo());

		final Collection _temp =
			new ArrayList(TestHelper.getTestCasesReachableFromSuite((TestSuite) getTest(), ICallGraphTest.class));
		_temp.addAll(TestHelper.getTestCasesReachableFromSuite((TestSuite) getTest(), XMLBasedCallGraphTest.class));

		for (final Iterator _i = _temp.iterator(); _i.hasNext();) {
			final ICallGraphTest _test = (ICallGraphTest) _i.next();
			_test.setCallGraph(cgiImpl);
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
