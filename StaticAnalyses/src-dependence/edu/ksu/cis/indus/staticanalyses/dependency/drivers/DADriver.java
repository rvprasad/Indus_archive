
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (C) 2003, 2004, 2005
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://indus.projects.cis.ksu.edu/).
 * It is understood that any modification not identified as such is
 * not covered by the preceding statement.
 *
 * This work is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this toolkit; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * Java is a trademark of Sun Microsystems, Inc.
 *
 * To submit a bug report, send a comment, or get the latest news on
 * this project and other SAnToS projects, please visit the web-site
 *                http://indus.projects.cis.ksu.edu/
 */

package edu.ksu.cis.indus.staticanalyses.dependency.drivers;

import soot.Scene;
import soot.SootMethod;

import soot.toolkits.graph.CompleteUnitGraph;

import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis;
import edu.ksu.cis.indus.staticanalyses.escape.EquivalenceClassBasedAnalysis;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractAnalyzer;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.CallGraph;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.ThreadGraph;
import edu.ksu.cis.indus.staticanalyses.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.staticanalyses.interfaces.IEnvironment;
import edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor;
import edu.ksu.cis.indus.staticanalyses.interfaces.IThreadGraphInfo;
import edu.ksu.cis.indus.staticanalyses.interfaces.IThreadGraphInfo.NewExprTriple;
import edu.ksu.cis.indus.staticanalyses.processing.CGBasedProcessingController;
import edu.ksu.cis.indus.staticanalyses.processing.ProcessingController;
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
	 * This is a map from interface IDs to interface implementations that are required by the analyses being driven.
	 *
	 * @invariant info.oclIsKindOf(Map(String, Object))
	 */
	protected Map info;

	/**
	 * This is a map from methods to their complete statement graph as obtained from Soot.
	 *
	 * @invariant method2cmpltStmtGraph.oclIsKindOf(Map(SootMethod, CompleteUnitGraph))
	 */
	protected final Map method2cmpltStmtGraph = new HashMap();

	/**
	 * A collection of dependence analyses.
	 *
	 * @invariant das.oclIsKindOf(Collection(DependencyAnalysis))
	 */
	protected Collection das;

	/**
	 * This is the collection of classes to be analysed.
	 */
	protected Scene scm;

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

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: BFA");
		}

		long start = System.currentTimeMillis();
		aa.analyze(scm, rootMethods);

		long stop = System.currentTimeMillis();
		addTimeLog("BFA", stop - start);

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: BFA");
		}

		ProcessingController pc = new ProcessingController();
		pc.setAnalyzer(aa);

		Collection processors = new ArrayList();
		ICallGraphInfo cgi = new CallGraph();
		processors.add(cgi);
		process(pc, processors);
		System.out.println("CALL GRAPH:\n" + ((CallGraph) cgi).dumpGraph());

		pc = new CGBasedProcessingController(cgi);
		pc.setAnalyzer(aa);

		IThreadGraphInfo tgi = new ThreadGraph(cgi);
		processors.clear();
		processors.add(tgi);
		process(pc, processors);
		System.out.println("THREAD GRAPH:\n" + ((ThreadGraph) tgi).dumpGraph());

		for (Iterator i = cgi.getReachableMethods().iterator(); i.hasNext();) {
			SootMethod method = (SootMethod) i.next();

			try {
				CompleteUnitGraph sg = new CompleteUnitGraph(method.retrieveActiveBody());
				method2cmpltStmtGraph.put(method, sg);
			} catch (RuntimeException e) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Method " + method.getSignature() + " may not have body.", e);
				}
			}
		}

		info = new HashMap();
		info.put(ICallGraphInfo.ID, cgi);
		info.put(IThreadGraphInfo.ID, tgi);

		info.put(PairManager.ID, new PairManager());
		info.put(IEnvironment.ID, aa.getEnvironment());

		setupDependencyAnalyses();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: dependency analyses");
		}

		for (Iterator i = das.iterator(); i.hasNext();) {
			DependencyAnalysis da = (DependencyAnalysis) i.next();
			start = System.currentTimeMillis();
			da.analyze();
			stop = System.currentTimeMillis();
			addTimeLog(da.getClass().getName() + " analysis", stop - start);
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
		printTimingStats(System.out);
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
	 * @pre processors.oclIsKindOf(Collection(IProcessor))
	 */
	protected void process(final ProcessingController pc, final Collection processors) {
		for (Iterator i = processors.iterator(); i.hasNext();) {
			IProcessor processor = (IProcessor) i.next();

			processor.hookup(pc);
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: BFA post processing");
		}

		long start = System.currentTimeMillis();
		pc.process();

		long stop = System.currentTimeMillis();
		addTimeLog("BFA post processing", stop - start);

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: BFA post processing");
		}

		for (Iterator i = processors.iterator(); i.hasNext();) {
			IProcessor processor = (IProcessor) i.next();

			processor.unhook(pc);
		}
	}

	/**
	 * Sets up the dependence analyses to be driven.
	 */
	protected void setupDependencyAnalyses() {
		ICallGraphInfo cgi = (ICallGraphInfo) info.get(ICallGraphInfo.ID);
		IThreadGraphInfo tgi = (IThreadGraphInfo) info.get(IThreadGraphInfo.ID);
		EquivalenceClassBasedAnalysis ecba = new EquivalenceClassBasedAnalysis(scm, cgi, tgi);
		ProcessingController ppc = new CGBasedProcessingController(cgi);
		Collection failed = new ArrayList();
		info.put(EquivalenceClassBasedAnalysis.ID, ecba);

		ppc.setAnalyzer(aa);

		for (Iterator i = das.iterator(); i.hasNext();) {
			DependencyAnalysis da = (DependencyAnalysis) i.next();

			if (da.getPreProcessor() != null) {
				da.getPreProcessor().hookup(ppc);
			}

			try {
				da.initialize(method2cmpltStmtGraph, info);
			} catch (InitializationException e) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn(da.getClass() + " failed to initialize, hence, will not be executed.", e);
					failed.add(da);
				}
			}
		}
		das.removeAll(failed);
		ecba.hookup(ppc);

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: preprocessing for dependency analyses");
		}

		long start = System.currentTimeMillis();
		ppc.process();

		long stop = System.currentTimeMillis();
		addTimeLog("Dependency preprocessing", stop - start);

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: preprocessing for dependency analyses");
		}
		ecba.unhook(ppc);
		ecba.execute();

		for (Iterator i = das.iterator(); i.hasNext();) {
			DependencyAnalysis da = (DependencyAnalysis) i.next();

			if (da.getPreProcessor() != null) {
				da.getPreProcessor().unhook(ppc);
			}
		}
	}
}

/*
   ChangeLog:

   $Log$

   Revision 1.2  2003/08/09 23:29:52  venku
   Ripple Effect of renaming Inter/Intra procedural data DAs to Aliased/NonAliased data DA.

   Revision 1.1  2003/08/07 06:38:05  venku
   Major:
    - Moved the packages under indus umbrella.
    - Renamed MethodLocalDataDA to NonAliasedDataDA.
    - Added class for AliasedDataDA.
    - Documented and specified the classes.
 */
