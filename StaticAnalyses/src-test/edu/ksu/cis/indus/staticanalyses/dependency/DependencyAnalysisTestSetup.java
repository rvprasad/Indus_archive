
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

package edu.ksu.cis.indus.staticanalyses.dependency;

import edu.ksu.cis.indus.TestHelper;

import edu.ksu.cis.indus.common.CollectionsModifier;
import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.graph.BasicBlockGraphMgr;
import edu.ksu.cis.indus.common.soot.ExceptionFlowSensitiveStmtGraphFactory;
import edu.ksu.cis.indus.common.soot.IStmtGraphFactory;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;
import edu.ksu.cis.indus.interfaces.IUseDefInfo;

import edu.ksu.cis.indus.processing.ProcessingController;
import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;

import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis;
import edu.ksu.cis.indus.staticanalyses.flow.FATestSetup;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ValueAnalysisTestSetup;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.AliasedUseDefInfo;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.ThreadGraph;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.CGBasedProcessingFilter;
import edu.ksu.cis.indus.staticanalyses.processing.ValueAnalyzerBasedProcessingController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import junit.framework.TestSuite;


/**
 * This is the setup in which various tests of flow analyses are run.  The classes to be processed during the test can be
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
	 * The instance of equivalence class based escape analysis to use.
	 */
	private EquivalenceClassBasedEscapeAnalysis ecba;

	/**
	 * A map used to communicate arguments to various analyses.
	 */
	private Map info;

	/**
	 * The thread graph to be used.
	 */
	private ThreadGraph tgiImpl;

	/**
	 * Creates a new DependencyAnalysisTestSetup object.
	 *
	 * @param test is the test to run in this setup.
	 * @param theNameOfClasses is the list of classes.
	 * @param classpath to be used to find the classes.
	 * @param cfgFactory to be used to process method bodies.
	 *
	 * @pre test != null and theNameOfClasses != null and cfgFactory != null
	 */
	protected DependencyAnalysisTestSetup(final TestSuite test, final String theNameOfClasses, final String classpath,
		final IStmtGraphFactory cfgFactory) {
		super(test, theNameOfClasses, classpath);
		setStmtGraphFactory(cfgFactory);
	}

	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		super.setUp();
		bbgMgr = new BasicBlockGraphMgr();
		bbgMgr.setUnitGraphFactory(getStmtGraphFactory());

		// setup level 1 analysis here.
		final ValueAnalyzerBasedProcessingController _pc = new ValueAnalyzerBasedProcessingController();
		_pc.setAnalyzer(valueAnalyzer);
		_pc.setEnvironment(valueAnalyzer.getEnvironment());
		_pc.setProcessingFilter(new TagBasedProcessingFilter(FATestSetup.TAG_NAME));
		_pc.setStmtGraphFactory(getStmtGraphFactory());
		tgiImpl = new ThreadGraph(cgiImpl, new CFGAnalysis(cgiImpl, bbgMgr));
		tgiImpl.hookup(_pc);
		_pc.process();
		tgiImpl.unhook(_pc);
		aliasUD = new AliasedUseDefInfo(valueAnalyzer, cgiImpl, bbgMgr);
		ecba = new EquivalenceClassBasedEscapeAnalysis(cgiImpl, tgiImpl, bbgMgr);
		ecba.setAnalyzer(valueAnalyzer);

		//setup info        
		info = new HashMap();
		info.put(ICallGraphInfo.ID, cgiImpl);
		info.put(IThreadGraphInfo.ID, tgiImpl);
		info.put(PairManager.ID, new PairManager());
		info.put(IEnvironment.ID, valueAnalyzer.getEnvironment());
		info.put(IValueAnalyzer.ID, valueAnalyzer);
		info.put(IUseDefInfo.ID, aliasUD);
		info.put(EquivalenceClassBasedEscapeAnalysis.ID, ecba);

		// retrieve dependence analysis
		final Collection _das = new ArrayList();

		for (final Iterator _i =
				TestHelper.getTestCasesReachableFromSuite((TestSuite) getTest(), IDependencyAnalysisTest.class).iterator();
			  _i.hasNext();) {
			final IDependencyAnalysisTest _test = (IDependencyAnalysisTest) _i.next();
			_test.setEnvironment(valueAnalyzer.getEnvironment());
			_das.add(_test.getDA());
		}

		for (final Iterator _i =
				TestHelper.getTestCasesReachableFromSuite((TestSuite) getTest(), XMLBasedDependencyAnalysisTest.class)
							.iterator(); _i.hasNext();) {
			final XMLBasedDependencyAnalysisTest _test = (XMLBasedDependencyAnalysisTest) _i.next();
			_test.setCallGraph(cgiImpl);
		}

		// drive the analysis.
		_pc.setProcessingFilter(new CGBasedProcessingFilter(cgiImpl));
		setupDependencyAnalyses(_pc, _das);

		for (final Iterator _i = _das.iterator(); _i.hasNext();) {
			final DependencyAnalysis _da = (DependencyAnalysis) _i.next();
			_da.analyze();
			CollectionsModifier.putIntoCollectionInMap(info, _da.getId(), _da, new ArrayList());
		}
	}

	/**
	 * @see TestCase#tearDown()
	 */
	protected void tearDown()
	  throws Exception {
		bbgMgr.reset();
		bbgMgr = null;
		// teardown TGI and ECBA here
		tgiImpl.reset();
		tgiImpl = null;
		ecba.reset();
		ecba = null;
		info.clear();
		info = null;
		// teardown the dependency analysis
		super.tearDown();
	}

	/**
	 * Sets up the dependence analyses to be driven.
	 *
	 * @param cgipc is the controller to be used to setup analyses.
	 * @param das is the collection of analyses.
	 *
	 * @pre cgipc != null and das != null
	 */
	private void setupDependencyAnalyses(final ProcessingController cgipc, final Collection das) {
		final Collection _failed = new ArrayList();

		for (final Iterator _i = das.iterator(); _i.hasNext();) {
			final DependencyAnalysis _da = (DependencyAnalysis) _i.next();
			_da.reset();
			_da.setBasicBlockGraphManager(bbgMgr);

			try {
				_da.initialize(info);
			} catch (InitializationException _e) {
				System.err.println(_da.getClass() + " failed to initialize, hence, will not be executed." + _e.getMessage());
				_failed.add(_da);
			}

			if (!_failed.contains(_da) && _da.doesPreProcessing()) {
				_da.getPreProcessor().hookup(cgipc);
			}
		}
		das.removeAll(_failed);
		aliasUD.hookup(cgipc);
		ecba.hookup(cgipc);
		cgipc.process();
		ecba.unhook(cgipc);
		aliasUD.unhook(cgipc);

		ecba.execute();

		for (final Iterator _i = das.iterator(); _i.hasNext();) {
			final DependencyAnalysis _da = (DependencyAnalysis) _i.next();

			if (_da.getPreProcessor() != null) {
				_da.getPreProcessor().unhook(cgipc);
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2004/03/29 01:55:03  venku
   - refactoring.
     - history sensitive work list processing is a common pattern.  This
       has been captured in HistoryAwareXXXXWorkBag classes.
   - We rely on views of CFGs to process the body of the method.  Hence, it is
     required to use a particular view CFG consistently.  This requirement resulted
     in a large change.
   - ripple effect of the above changes.
   Revision 1.2  2004/03/26 00:26:40  venku
   - ripple effect of refactoring soot package in Indus.
   Revision 1.1  2004/03/09 19:10:40  venku
   - preliminary commit of test setup for dependency analyses.
 */
