
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

package edu.ksu.cis.indus.staticanalyses.dependency.drivers;

import soot.Scene;

import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.interfaces.IProcessor;
import edu.ksu.cis.indus.processing.ProcessingController;
import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis;
import edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractAnalyzer;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.AliasedUseDefInfo;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.CallGraph;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.ThreadGraph;
import edu.ksu.cis.indus.staticanalyses.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.staticanalyses.interfaces.IThreadGraphInfo;
import edu.ksu.cis.indus.staticanalyses.interfaces.IThreadGraphInfo.NewExprTriple;
import edu.ksu.cis.indus.staticanalyses.interfaces.IUseDefInfo;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzerBasedProcessor;
import edu.ksu.cis.indus.staticanalyses.processing.CGBasedProcessingController;
import edu.ksu.cis.indus.staticanalyses.processing.ValueAnalyzerBasedProcessingController;
import edu.ksu.cis.indus.staticanalyses.support.Driver;
import edu.ksu.cis.indus.staticanalyses.support.Pair.PairManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * This class provides basic driver facilities for driving dependency analyses.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public abstract class DADriver
  extends Driver {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(DADriver.class);

	/**
	 * This is the flow analyser used by the analyses being tested.
	 */
	protected AbstractAnalyzer aa;

	/**
	 * A collection of dependence analyses.
	 *
	 * @invariant das.oclIsKindOf(Collection(DependencyAnalysis))
	 */
	protected Collection das;

	/**
	 * This is a map from interface IDs to interface implementations that are required by the analyses being driven.
	 *
	 * @invariant info.oclIsKindOf(Map(String, Object))
	 */
	protected final Map info = new HashMap();

	/**
	 * This is the collection of classes to be analysed.
	 */
	protected Scene scm;

	/**
	 * This indicates if EquivalenceClassBasedAnalysis should be executed.  Subclasses should set this appropriately.
	 */
	protected boolean ecbaRequired;

	/**
	 * This provides equivalence class based escape analysis.
	 */
	EquivalenceClassBasedEscapeAnalysis ecba = null;

	/**
	 * This provides call-graph based processing controller.
	 */
	ValueAnalyzerBasedProcessingController cgipc;

	/**
	 * The command line arguments.
	 */
	private final String[] args;

	/**
	 * Creates a new DADriver object.  This is a dummy do-nothing constructor.
	 *
	 * @throws RuntimeException if this constructor is executed.
	 */
	protected DADriver() {
		throw new RuntimeException("Default constructor should never be called.");
	}

	/**
	 * Creates a new instance of this class.
	 *
	 * @param argsParam is the command line argument.
	 *
	 * @post args.oclAsType(Sequence(String)) == argsParam.oclAsType(Sequence(String))
	 */
	protected DADriver(final String[] argsParam) {
		if (argsParam.length == 0) {
			System.out.println("Please specify a class to consider for the analysis.");
			System.exit(-1);
		}
		this.args = argsParam;
	}

	/**
	 * Drives the analysis.
	 */
	public void run() {
		initialize();
		execute();
	}

	/**
	 * Drives the analyses.
	 *
	 * @post scm != null and aa != null
	 */
	protected void execute() {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Loading classes....");
		}

		scm = loadupClassesAndCollectMains(args);
		aa = OFAnalyzer.getFSOSAnalyzer();

		ValueAnalyzerBasedProcessingController pc = new ValueAnalyzerBasedProcessingController();
		Collection processors = new ArrayList();
		ICallGraphInfo cgi = new CallGraph();
		IThreadGraphInfo tgi = new ThreadGraph(cgi, new CFGAnalysis(cgi, bbm));
		Collection rm = new ArrayList();
		cgipc = new CGBasedProcessingController(cgi);

		pc.setAnalyzer(aa);
		cgipc.setAnalyzer(aa);

		info.put(ICallGraphInfo.ID, cgi);
		info.put(IThreadGraphInfo.ID, tgi);
		info.put(PairManager.ID, new PairManager());
		info.put(IEnvironment.ID, aa.getEnvironment());
		info.put(IValueAnalyzer.ID, aa);
		info.put(IUseDefInfo.ID, new AliasedUseDefInfo(aa));

		if (ecbaRequired) {
			ecba = new EquivalenceClassBasedEscapeAnalysis(cgi, tgi, bbm);
			info.put(EquivalenceClassBasedEscapeAnalysis.ID, ecba);
		}

		for (Iterator k = rootMethods.iterator(); k.hasNext();) {
			rm.clear();
			rm.add(k.next());

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("BEGIN: FA");
			}

			long start = System.currentTimeMillis();
			aa.reset();
			bbm.reset();

			if (ecbaRequired) {
				ecba.reset();
			}
			aa.analyze(scm, rm);

			long stop = System.currentTimeMillis();
			addTimeLog("FA", stop - start);

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("END: FA");
			}

			((CallGraph) cgi).reset();
			processors.clear();
			processors.add(cgi);
			process(pc, processors);
			System.out.println("CALL GRAPH:\n" + ((CallGraph) cgi).dumpGraph());

			processors.clear();
			((ThreadGraph) tgi).reset();
			processors.add(tgi);
			process(cgipc, processors);
			System.out.println("THREAD GRAPH:\n" + ((ThreadGraph) tgi).dumpGraph());

			setupDependencyAnalyses();

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("BEGIN: dependency analyses");
			}

			for (Iterator i = das.iterator(); i.hasNext();) {
				DependencyAnalysis da = (DependencyAnalysis) i.next();
				start = System.currentTimeMillis();
				da.analyze();
				stop = System.currentTimeMillis();
				addTimeLog(da.getClass().getName() + "[" + da.hashCode() + "] analysis", stop - start);
			}

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("END: dependency analyses");
			}

			Map threadMap = new HashMap();
			System.out.println("\nThread mapping:");

			int count = 1;

			for (java.util.Iterator j = tgi.getAllocationSites().iterator(); j.hasNext();) {
				NewExprTriple element = (NewExprTriple) j.next();
				String tid = "T" + count++;
				threadMap.put(element, tid);

				if (element.getMethod() == null) {
					System.out.println(tid + " -> " + element.getExpr().getType());
				} else {
					System.out.println(tid + " -> " + element.getStmt() + "@" + element.getMethod());
				}
			}

			for (Iterator i = das.iterator(); i.hasNext();) {
				System.out.println(i.next());
			}
			System.out.println("Total classes loaded: " + scm.getClasses().size());
			printTimingStats(System.out);
		}
	}

	/**
	 * Initialize the collection of dependence analyses with the analyses to be driven.
	 *
	 * @post das.size() > 0
	 */
	protected abstract void initialize();

	/**
	 * Drive the given processors by the given controller.  This is helpful to batch pre/post-processors.
	 *
	 * @param pc controls the processing activity.
	 * @param processors is the collection of processors.
	 *
	 * @pre processors.oclIsKindOf(Collection(IValueAnalyzerBasedProcessor))
	 */
	protected void process(final ProcessingController pc, final Collection processors) {
		for (Iterator i = processors.iterator(); i.hasNext();) {
			IProcessor processor = (IValueAnalyzerBasedProcessor) i.next();

			processor.hookup(pc);
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: FA post processing");
		}

		long start = System.currentTimeMillis();
		pc.process();

		long stop = System.currentTimeMillis();
		addTimeLog("FA post processing", stop - start);

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: FA post processing");
		}

		for (Iterator i = processors.iterator(); i.hasNext();) {
			IProcessor processor = (IValueAnalyzerBasedProcessor) i.next();

			processor.unhook(pc);
		}
	}

	/**
	 * Sets up the dependence analyses to be driven.
	 */
	protected void setupDependencyAnalyses() {
		Collection failed = new ArrayList();

		for (Iterator i = das.iterator(); i.hasNext();) {
			DependencyAnalysis da = (DependencyAnalysis) i.next();
			da.reset();
			da.setBasicBlockGraphManager(bbm);

			if (da.doesPreProcessing()) {
				da.getPreProcessor().hookup(cgipc);
			}

			try {
				da.initialize(info);
			} catch (InitializationException e) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn(da.getClass() + " failed to initialize, hence, will not be executed.", e);
					failed.add(da);
				}
			}
		}
		das.removeAll(failed);

		if (ecbaRequired) {
			ecba.hookup(cgipc);
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: preprocessing for dependency analyses");
		}

		long start = System.currentTimeMillis();
		cgipc.process();

		long stop = System.currentTimeMillis();
		addTimeLog("Dependency preprocessing", stop - start);

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: preprocessing for dependency analyses");
		}

		if (ecbaRequired) {
			ecba.unhook(cgipc);
			ecba.execute();
		}

		for (Iterator i = das.iterator(); i.hasNext();) {
			DependencyAnalysis da = (DependencyAnalysis) i.next();

			if (da.getPreProcessor() != null) {
				da.getPreProcessor().unhook(cgipc);
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.25  2003/11/02 22:10:08  venku
   - changed the signature of the constructor of
     EquivalenceClassBasedEscapeAnalysis.
   Revision 1.24  2003/10/05 16:24:01  venku
   - coding convention.
   Revision 1.23  2003/10/05 16:21:21  venku
   - removed option to use local names.
   Revision 1.22  2003/09/29 07:30:51  venku
   - added support to spit out local variables names as they occur
     in the source rather than jimplified names.
   Revision 1.21  2003/09/29 06:37:31  venku
   - Each driver now handles each root method separately.
   Revision 1.20  2003/09/29 04:20:30  venku
   - coding convention.
   Revision 1.19  2003/09/28 07:32:30  venku
   - many basic block graphs were being constructed. Now, there
     is only one that will be used.
   Revision 1.18  2003/09/28 06:46:49  venku
   - Some more changes to extract unit graphs from the enviroment.
   Revision 1.17  2003/09/28 06:20:38  venku
   - made the core independent of hard code used to create unit graphs.
     The core depends on the environment to provide a factory that creates
     these unit graphs.
   Revision 1.16  2003/09/28 03:16:48  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.15  2003/09/15 00:02:48  venku
   - TrapUnitGraphs are generated instead of CompleteUnitGraphs
   Revision 1.14  2003/09/15 00:02:05  venku
   - added support for ReferenceBasedDataDA.
   Revision 1.13  2003/09/12 01:49:30  venku
   - prints hashcode to differentiate between instances of same analysis.
   Revision 1.12  2003/09/09 01:13:58  venku
   - made basic block graph manager configurable in AbstractAnalysis
   - ripple effect of the above change in DADriver.  This should also affect Slicer.
   Revision 1.11  2003/09/09 00:45:53  venku
   - minor refactoring.
   Revision 1.10  2003/09/08 02:23:13  venku
   - Ripple effect of bbm support in Driver and change of constructor
     in ThreadGraph.
   Revision 1.9  2003/09/07 09:02:13  venku
   - Synchronization dependence now handles exception based
     sync dep edges.  This requires a Value Flow analysis which can
     provides value binding information for a local at a program point.
   - Ripple effect of the above change.
   Revision 1.8  2003/09/02 11:30:56  venku
   - Enabled toggling ECBA instance.
   Revision 1.7  2003/08/25 11:47:37  venku
   Fixed minor glitches.
   Revision 1.6  2003/08/21 01:25:21  venku
    - Renamed src-escape to src-concurrency to as to group all concurrency
      issue related analyses into a package.
    - Renamed escape package to concurrency.escape.
    - Renamed EquivalenceClassBasedAnalysis to EquivalenceClassBasedEscapeAnalysis.
   Changes due to the ripple effect of the above changes are being committed.
   Revision 1.5  2003/08/17 10:48:34  venku
   Renamed BFA to FA.  Also renamed bfa variables to fa.
   Ripple effect was huge.
   Revision 1.4  2003/08/11 06:34:52  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.3  2003/08/10 03:43:26  venku
   Renamed Tester to Driver.
   Refactored logic to pick entry points.
   Provided for logging timing stats into any specified stream.
   Ripple effect in others.
   Revision 1.2  2003/08/09 23:29:52  venku
   Ripple Effect of renaming Inter/Intra procedural data DAs to Aliased/NonAliased data DA.
   Revision 1.1  2003/08/07 06:38:05  venku
   Major:
    - Moved the packages under indus umbrella.
    - Renamed MethodLocalDataDA to NonAliasedDataDA.
    - Added class for AliasedDataDA.
    - Documented and specified the classes.
 */
