
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
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class DependencyAnalysisTestSetup
  extends ValueAnalysisTestSetup {
	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private AliasedUseDefInfo aliasUD;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private BasicBlockGraphMgr bbgMgr;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private EquivalenceClassBasedEscapeAnalysis ecba;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private Map info;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private ThreadGraph tgiImpl;

	/**
	 * DOCUMENT ME!
	 *
	 * @param test
	 * @param theNameOfClasses
	 * @param classpath
	 */
	protected DependencyAnalysisTestSetup(final TestSuite test, final String theNameOfClasses, final String classpath) {
		super(test, theNameOfClasses, classpath);
	}

	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		super.setUp();
		bbgMgr = new BasicBlockGraphMgr();
		bbgMgr.setUnitGraphFactory(new ExceptionFlowSensitiveStmtGraphFactory(ExceptionFlowSensitiveStmtGraphFactory.SYNC_RELATED_EXCEPTIONS, true));

		// setup level 1 analysis here.
		final ValueAnalyzerBasedProcessingController _pc = new ValueAnalyzerBasedProcessingController();
		_pc.setAnalyzer(valueAnalyzer);
		_pc.setEnvironment(valueAnalyzer.getEnvironment());
		_pc.setProcessingFilter(new TagBasedProcessingFilter(FATestSetup.TAG_NAME));
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
			_das.add(_test.getDA());
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
	 * @param cgipc DOCUMENT ME!
	 * @param das DOCUMENT ME!
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
   Revision 1.1  2004/03/09 19:10:40  venku
   - preliminary commit of test setup for dependency analyses.

 */
