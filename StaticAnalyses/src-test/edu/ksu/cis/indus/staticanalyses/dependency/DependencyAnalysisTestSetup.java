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

package edu.ksu.cis.indus.staticanalyses.dependency;

import edu.ksu.cis.indus.TestHelper;

import edu.ksu.cis.indus.common.collections.MapUtils;
import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.interfaces.IEscapeInfo;
import edu.ksu.cis.indus.interfaces.IMonitorInfo;
import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;
import edu.ksu.cis.indus.interfaces.IUseDefInfo;

import edu.ksu.cis.indus.processing.OneAllStmtSequenceRetriever;
import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;

import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.MonitorAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis;
import edu.ksu.cis.indus.staticanalyses.flow.FATestSetup;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ValueAnalysisTestSetup;
import edu.ksu.cis.indus.staticanalyses.flow.processors.AliasedUseDefInfo;
import edu.ksu.cis.indus.staticanalyses.flow.processors.AliasedUseDefInfov2;
import edu.ksu.cis.indus.staticanalyses.flow.processors.ThreadGraph;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.AnalysesController;
import edu.ksu.cis.indus.staticanalyses.processing.CGBasedProcessingFilter;
import edu.ksu.cis.indus.staticanalyses.processing.ValueAnalyzerBasedProcessingController;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import junit.framework.TestSuite;

/**
 * This is the setup in which various tests of dependence analyses are run. The classes to be processed during the test can be
 * configured via the command line or via specifying
 * <code>DepedencyAnalysisRegressionTestSuite.DEPENDENCYANALYSIS_TEST_PROPERTIES_FILE</code> system property. The syntax for
 * both these options is a space separated list of class names.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class DependencyAnalysisTestSetup
		extends ValueAnalysisTestSetup {

	/**
	 * The instance of aliased use-def info to use.
	 */
	private AliasedUseDefInfo aliasUD;

	/**
	 * The instance of basic block graph manager to use.
	 */
	private BasicBlockGraphMgr bbgMgr;

	/**
	 * The collection of dependency analyses being tested.
	 */
	private Collection<IDependencyAnalysis<?, ?, ?, ?, ?, ?>> das;

	/**
	 * The instance of equivalence class based escape analysis to use.
	 */
	private EquivalenceClassBasedEscapeAnalysis ecba;

	/**
	 * A map used to communicate arguments to various analyses.
	 */
	private Map info;

	/**
	 * This provides monitor information.
	 */
	private MonitorAnalysis monitorInfo;

	/**
	 * The thread graph to be used.
	 */
	private ThreadGraph tgiImpl;

	/**
	 * @see ValueAnalysisTestSetup#ValueAnalysisTestSetup(TestSuite,String,String)
	 */
	protected DependencyAnalysisTestSetup(final TestSuite test, final String theNameOfClasses, final String classpath) {
		super(test, theNameOfClasses, classpath);
	}

	/**
	 * @see junit.extensions.TestSetup#setUp()
	 */
	@Override protected void setUp() throws Exception {
		super.setUp();
		bbgMgr = new BasicBlockGraphMgr();
		bbgMgr.setStmtGraphFactory(getStmtGraphFactory());

		// setup level 1 analysis here.
		final ValueAnalyzerBasedProcessingController _pc = new ValueAnalyzerBasedProcessingController();
		final OneAllStmtSequenceRetriever _ssr = new OneAllStmtSequenceRetriever();
		_ssr.setStmtGraphFactory(getStmtGraphFactory());
		_pc.setStmtSequencesRetriever(_ssr);
		_pc.setAnalyzer(valueAnalyzer);
		_pc.setEnvironment(valueAnalyzer.getEnvironment());
		_pc.setProcessingFilter(new TagBasedProcessingFilter(FATestSetup.TAG_NAME));

		final PairManager _pairManager = new PairManager(false, true);
		tgiImpl = new ThreadGraph(cgiImpl, new CFGAnalysis(cgiImpl, bbgMgr), _pairManager);
		tgiImpl.hookup(_pc);
		_pc.process();
		tgiImpl.unhook(_pc);
		aliasUD = new AliasedUseDefInfov2(valueAnalyzer, cgiImpl, tgiImpl, bbgMgr, _pairManager);
		ecba = new EquivalenceClassBasedEscapeAnalysis(cgiImpl, null, bbgMgr);
		monitorInfo = new MonitorAnalysis();

		// setup info
		info = new HashMap();
		info.put(ICallGraphInfo.ID, cgiImpl);
		info.put(IThreadGraphInfo.ID, tgiImpl);
		info.put(PairManager.ID, _pairManager);
		info.put(IEnvironment.ID, valueAnalyzer.getEnvironment());
		info.put(IValueAnalyzer.ID, valueAnalyzer);
		info.put(IUseDefInfo.ALIASED_USE_DEF_ID, aliasUD);
		info.put(IEscapeInfo.ID, ecba.getEscapeInfo());
		info.put(IMonitorInfo.ID, monitorInfo);

		// retrieve dependence analysis
		das = new HashSet<IDependencyAnalysis<?, ?, ?, ?, ?, ?>>();

		for (final Iterator<IDependencyAnalysisTest> _i = TestHelper.getTestCasesReachableFromSuite((TestSuite) getTest(),
				IDependencyAnalysisTest.class).iterator(); _i.hasNext();) {
			final IDependencyAnalysisTest _test = _i.next();
			_test.setEnvironment(valueAnalyzer.getEnvironment());

			final IDependencyAnalysis<?, ?, ?, ?, ?, ?> _da = _test.getDA();
			das.add(_da);

			if (_da.getIds().contains(IDependencyAnalysis.DependenceSort.CONTROL_DA)
					&& (_da.getDirection().equals(IDependencyAnalysis.Direction.BI_DIRECTIONAL) || _da.getDirection().equals(
							IDependencyAnalysis.Direction.BACKWARD_DIRECTION))) {
				MapUtils.putIntoSetInMap(info, IDependencyAnalysis.DependenceSort.CONTROL_DA, _da);
			}
		}

		for (final Iterator<XMLBasedDependencyAnalysisTest> _i = TestHelper.getTestCasesReachableFromSuite(
				(TestSuite) getTest(), XMLBasedDependencyAnalysisTest.class).iterator(); _i.hasNext();) {
			final XMLBasedDependencyAnalysisTest _test = _i.next();
			_test.setCallGraph(cgiImpl);
		}

		// drive the analysis.
		_pc.setProcessingFilter(new CGBasedProcessingFilter(cgiImpl));
		aliasUD.hookup(_pc);
		_pc.process();
		aliasUD.unhook(_pc);

		// drive dependency analyses
		final AnalysesController _ac = new AnalysesController(info, _pc, bbgMgr);
		_ac.addAnalyses(IMonitorInfo.ID, Collections.singleton(monitorInfo));
		_ac.addAnalyses(IEscapeInfo.ID, Collections.singleton(ecba));

		for (final Iterator<IDependencyAnalysis<?, ?, ?, ?, ?, ?>> _i1 = das.iterator(); _i1.hasNext();) {
			final IDependencyAnalysis<?, ?, ?, ?, ?, ?> _da1 = _i1.next();
			_da1.reset();

			for (final Iterator<? extends Comparable<?>> _i2 = _da1.getIds().iterator(); _i2.hasNext();) {
				final Comparable<?> _id = _i2.next();
				_ac.addAnalyses(_id, Collections.singleton(_da1));
			}
		}
		_ac.initialize();
		_ac.execute();
	}

	/**
	 * @see junit.extensions.TestSetup#tearDown()
	 */
	@Override protected void tearDown() throws Exception {
		bbgMgr.reset();
		bbgMgr = null;
		// teardown TGI and ECBA here
		tgiImpl.reset();
		tgiImpl = null;
		ecba.reset();
		ecba = null;
		monitorInfo.reset();
		monitorInfo = null;
		aliasUD.reset();
		aliasUD = null;
		info.clear();
		info = null;

		// teardown the dependency analysis
		for (final Iterator<IDependencyAnalysis<?, ?, ?, ?, ?, ?>> _i = das.iterator(); _i.hasNext();) {
			_i.next().reset();
		}
		das.clear();
		das = null;
		super.tearDown();
	}
}

// End of File
