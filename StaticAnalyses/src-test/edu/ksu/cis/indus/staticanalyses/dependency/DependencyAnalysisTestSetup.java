
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

import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.interfaces.IMonitorInfo;
import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;
import edu.ksu.cis.indus.interfaces.IUseDefInfo;

import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;

import edu.ksu.cis.indus.staticanalyses.AnalysesController;
import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.MonitorAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis;
import edu.ksu.cis.indus.staticanalyses.flow.FATestSetup;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ValueAnalysisTestSetup;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.AliasedUseDefInfo;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.AliasedUseDefInfov2;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.ThreadGraph;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
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
 * This is the setup in which various tests of dependence analyses are run.  The classes to be processed during the test can
 * be configured via the command line or via specifying
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
	private Collection das;

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

		final PairManager _pairManager = new PairManager(false, true);
		tgiImpl = new ThreadGraph(cgiImpl, new CFGAnalysis(cgiImpl, bbgMgr), _pairManager);
		tgiImpl.hookup(_pc);
		_pc.process();
		tgiImpl.unhook(_pc);
		aliasUD = new AliasedUseDefInfov2(valueAnalyzer, cgiImpl, tgiImpl, bbgMgr, _pairManager);
		ecba = new EquivalenceClassBasedEscapeAnalysis(cgiImpl, bbgMgr);
		monitorInfo = new MonitorAnalysis();

		//setup info        
		info = new HashMap();
		info.put(ICallGraphInfo.ID, cgiImpl);
		info.put(IThreadGraphInfo.ID, tgiImpl);
		info.put(PairManager.ID, _pairManager);
		info.put(IEnvironment.ID, valueAnalyzer.getEnvironment());
		info.put(IValueAnalyzer.ID, valueAnalyzer);
		info.put(IUseDefInfo.ALIASED_USE_DEF_ID, aliasUD);
		info.put(EquivalenceClassBasedEscapeAnalysis.ID, ecba);
		info.put(IMonitorInfo.ID, monitorInfo);

		// retrieve dependence analysis
		das = new HashSet();

		for (final Iterator _i =
				TestHelper.getTestCasesReachableFromSuite((TestSuite) getTest(), IDependencyAnalysisTest.class).iterator();
			  _i.hasNext();) {
			final IDependencyAnalysisTest _test = (IDependencyAnalysisTest) _i.next();
			_test.setEnvironment(valueAnalyzer.getEnvironment());
			das.add(_test.getDA());
		}

		for (final Iterator _i =
				TestHelper.getTestCasesReachableFromSuite((TestSuite) getTest(), XMLBasedDependencyAnalysisTest.class)
							.iterator(); _i.hasNext();) {
			final XMLBasedDependencyAnalysisTest _test = (XMLBasedDependencyAnalysisTest) _i.next();
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
		_ac.addAnalyses(EquivalenceClassBasedEscapeAnalysis.ID, Collections.singleton(ecba));

		for (final Iterator _i1 = das.iterator(); _i1.hasNext();) {
			final IDependencyAnalysis _da1 = (IDependencyAnalysis) _i1.next();
			_da1.reset();
			_ac.addAnalyses(_da1.getId(), Collections.singleton(_da1));
		}
		_ac.initialize();
		_ac.execute();
	}

	/**
	 * @see junit.extensions.TestSetup#tearDown()
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
		monitorInfo.reset();
		monitorInfo = null;
		aliasUD.reset();
		aliasUD = null;
		info.clear();
		info = null;

		// teardown the dependency analysis
		for (final Iterator _i = das.iterator(); _i.hasNext();) {
			((AbstractDependencyAnalysis) _i.next()).reset();
		}
		das.clear();
		das = null;
		super.tearDown();
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.24  2004/08/02 07:33:45  venku
   - small but significant change to the pair manager.
   - ripple effect.

   Revision 1.23  2004/08/01 21:30:15  venku
   - ECBA was made independent of ThreadGraph Analysis.
   Revision 1.22  2004/07/28 09:09:27  venku
   - changed aliased use def analysis to consider thread.
   - also fixed a bug in the same analysis.
   - ripple effect.
   - deleted entry control dependence and renamed direct entry control da as
     entry control da.
   Revision 1.21  2004/07/23 13:09:45  venku
   - Refactoring in progress.
     - Extended IMonitorInfo interface.
     - Teased apart the logic to calculate monitor info from SynchronizationDA
       into MonitorAnalysis.
     - Casted EquivalenceClassBasedEscapeAnalysis as an AbstractAnalysis.
     - ripple effect.
     - Implemented safelock analysis to handle intraprocedural processing.
   Revision 1.20  2004/07/21 11:36:26  venku
   - Extended IUseDefInfo interface to provide both local and non-local use def info.
   - ripple effect.
   - deleted ContainmentPredicate.  Instead, used CollectionUtils.containsAny() in
     ECBA and AliasedUseDefInfo analysis.
   - Added new faster implementation of LocalUseDefAnalysisv2
   - Used LocalUseDefAnalysisv2
   Revision 1.19  2004/07/17 23:32:18  venku
   - used Factory() pattern to populate values in maps and lists in CollectionsUtilities methods.
   - ripple effect.
   Revision 1.18  2004/07/16 06:38:47  venku
   - added  a more precise implementation of aliased use-def information.
   - ripple effect.
   Revision 1.17  2004/07/11 14:17:39  venku
   - added a new interface for identification purposes (IIdentification)
   - all classes that have an id implement this interface.
   Revision 1.16  2004/05/31 21:38:07  venku
   - moved BasicBlockGraph and BasicBlockGraphMgr from common.graph to common.soot.
   - ripple effect.
   Revision 1.15  2004/05/21 22:11:47  venku
   - renamed CollectionsModifier as CollectionUtilities.
   - added new specialized methods along with a method to extract
     filtered maps.
   - ripple effect.
   Revision 1.14  2004/05/14 11:25:48  venku
   - aliasUD was not reset. FIXED.
   Revision 1.13  2004/05/14 06:27:25  venku
   - renamed DependencyAnalysis as AbstractDependencyAnalysis.
   Revision 1.12  2004/04/21 04:13:20  venku
   - jimple dumping takes time.  Instead, the user can control this
     per configuration.
   Revision 1.11  2004/04/21 02:24:01  venku
   - test clean up code was added.
   Revision 1.10  2004/04/20 06:53:17  venku
   - documentation.
   Revision 1.9  2004/04/19 05:10:26  venku
   - NPE's in test setup caused by unchecked reseting.
   Revision 1.8  2004/04/18 02:05:18  venku
   - memory leak fixes.
   Revision 1.7  2004/04/18 00:42:56  venku
   - references to objects had leaked after test. FIXED.
   Revision 1.6  2004/04/18 00:17:20  venku
   - added support to dump jimple.xml while testing. (bug fix)
   Revision 1.5  2004/04/01 19:18:29  venku
   - stmtGraphFactory was not set.
   Revision 1.4  2004/03/29 09:44:41  venku
   - finished the xml-based testing framework for dependence.
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
