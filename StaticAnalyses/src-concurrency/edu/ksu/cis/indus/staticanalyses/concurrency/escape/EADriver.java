
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

package edu.ksu.cis.indus.staticanalyses.concurrency.escape;

import soot.Local;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.ValueBox;

import soot.jimple.ArrayRef;
import soot.jimple.FieldRef;
import soot.jimple.JimpleBody;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;

import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.CallGraph;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.ThreadGraph;
import edu.ksu.cis.indus.staticanalyses.interfaces.IThreadGraphInfo.NewExprTriple;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.CGBasedProcessingController;
import edu.ksu.cis.indus.staticanalyses.processing.ValueAnalyzerBasedProcessingController;
import edu.ksu.cis.indus.staticanalyses.support.SootBasedDriver;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;


/**
 * This class drives escape analysis and displays the calculated information.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public final class EADriver
  extends SootBasedDriver {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(EADriver.class);

	/**
	 * The provided command line arguments.
	 */
	private String[] args;

	/**
	 * Creates a new EADriver object.
	 *
	 * @param argsParam is the command line arguments.
	 *
	 * @post args.oclAsType(Sequence(String)) == argsParam.oclAsType(Sequence(String))
	 */
	private EADriver(final String[] argsParam) {
		this.args = argsParam;
	}

	/**
	 * Entry point to the test driver.
	 *
	 * @param argsParam is the command line arguments.
	 */
	public static void main(final String[] argsParam) {
		if (argsParam.length == 0) {
			System.out.println("Please specify a class to consider for the analysis.");
		}
		(new EADriver(argsParam)).execute();
	}

	/**
	 * Drives escape analysis. It executes FA first, followed by any post process analysis, followed by the escape analysis.
	 */
	protected void execute() {
		setClassNames(args);
		initialize();

		IValueAnalyzer aa = OFAnalyzer.getFSOSAnalyzer();
		Collection rm = new ArrayList();

		for (Iterator l = rootMethods.iterator(); l.hasNext();) {
			rm.clear();
			rm.add(l.next());

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("BEGIN: FA analysis");
			}

			long start = System.currentTimeMillis();
			aa.reset();
			aa.analyze(scene, rm);

			long stop = System.currentTimeMillis();

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("END: FA analysis");
			}
			addTimeLog("FA analysis", stop - start);

			ValueAnalyzerBasedProcessingController ppc = new ValueAnalyzerBasedProcessingController();
			ppc.setAnalyzer(aa);

			// Create call graph
			CallGraph cg = new CallGraph();
			cg.hookup(ppc);

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("BEGIN: FA postprocessing");
			}
			start = System.currentTimeMillis();
			ppc.process();
			cg.unhook(ppc);

			ppc = new CGBasedProcessingController(cg);
			ppc.setAnalyzer(aa);

			// Create Thread graph
			ThreadGraph tg = new ThreadGraph(cg, new CFGAnalysis(cg, bbm));
			tg.hookup(ppc);
			ppc.process();
			tg.unhook(ppc);

			// Perform equivalence-class-based escape analysis
			EquivalenceClassBasedEscapeAnalysis analysis = new EquivalenceClassBasedEscapeAnalysis(cg, tg, bbm);
			analysis.hookup(ppc);
			ppc.process();
			stop = System.currentTimeMillis();
			analysis.unhook(ppc);

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("END: FA postprocessing");
			}
			addTimeLog("FA postprocessing took ", stop - start);
			System.out.println("CALL GRAPH:\n" + cg.dumpGraph());
			System.out.println("THREAD GRAPH:\n" + tg.dumpGraph());

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("BEGIN: " + analysis.getClass().getName() + " processing");
			}
			start = System.currentTimeMillis();
			analysis.execute();
			stop = System.currentTimeMillis();

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("END: " + analysis.getClass().getName() + " processing");
			}
			addTimeLog(analysis.getClass().getName(), stop - start);

			int count = 1;
			Map threadMap = new HashMap();
			System.out.println("\nThread mapping:");

			for (java.util.Iterator j = tg.getAllocationSites().iterator(); j.hasNext();) {
				NewExprTriple element = (NewExprTriple) j.next();
				String tid = "T" + count++;
				threadMap.put(element, tid);

				if (element.getMethod() == null) {
					System.out.println(tid + " -> " + element.getExpr().getType());
				} else {
					System.out.println(tid + " -> " + element.getStmt() + "@" + element.getMethod());
				}
			}

			Collection abstractObjects = new HashSet();
			int accessSites = 0;
			int allocationSites = 0;

			for (int i = 0; i < args.length; i++) {
				SootClass sc = scene.getSootClass(args[i]);
				System.out.println("Info for class " + sc.getName() + "\n");

				for (Iterator j = CollectionUtils.intersection(cg.getReachableMethods(), sc.getMethods()).iterator();
					  j.hasNext();) {
					SootMethod sm = (SootMethod) j.next();
					System.out.println("\nInfo for Method " + sm.getSignature());

					if (sm.isConcrete()) {
						JimpleBody body = (JimpleBody) sm.retrieveActiveBody();

						for (Iterator k = body.getLocals().iterator(); k.hasNext();) {
							Local local = (Local) k.next();
							System.out.println(" Local " + local + ":" + local.getType() + "\n" + " escapes -> "
								+ analysis.escapes(local, sm) + "\n" + " global -> " + analysis.isGlobal(local, sm));
						}

						for (Iterator k = body.getUseAndDefBoxes().iterator(); k.hasNext();) {
							ValueBox box = (ValueBox) k.next();
							Value v = box.getValue();

							if (v instanceof ArrayRef || v instanceof FieldRef) {
								Object as = analysis.getAliasSetFor(v, sm);

								if (as != null) {
									as = ((AliasSet) as).find();

									if (!abstractObjects.contains(as)) {
										abstractObjects.add(as);
									}

									if (analysis.escapes(v, sm)) {
										accessSites++;
									}
								}
							} else if (v instanceof NewExpr || v instanceof NewArrayExpr || v instanceof NewMultiArrayExpr) {
								allocationSites++;
							}
						}
					} else {
						if (LOGGER.isInfoEnabled()) {
							LOGGER.info(sm + " is not a concrete method.  Hence, it's body could not be retrieved.");
						}
					}
				}
			}
			System.out.println("Total number of abstract objects is " + abstractObjects.size());
			System.out.println("Total numbef of allocation sites is " + allocationSites);
			System.out.println("Total number of shared accesses based on escape information is " + accessSites);
			System.out.println("Total classes loaded: " + scene.getClasses().size());
			printTimingStats();
		}
	}

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param o DOCUMENT ME!
	 */
	protected void writeInfo(final Object o) {
		System.out.println(o.toString());
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.15  2003/11/06 05:15:07  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.14  2003/11/02 22:09:57  venku
   - changed the signature of the constructor of
     EquivalenceClassBasedEscapeAnalysis.
   Revision 1.13  2003/10/31 01:02:04  venku
   - added code for extracting data for CC04 paper.
   Revision 1.12  2003/10/09 00:17:39  venku
   - changes to instrumetn statistics numbers.
   Revision 1.11  2003/10/05 06:31:35  venku
   - Things work.  The bug was the order in which the
     parameter alias sets were being accessed.  FIXED.
   Revision 1.10  2003/09/29 14:55:03  venku
   - don't use "use-orignal-names" option with Jimple.
     The variables referring to objects need to be unique if the
     results of the analyses should be meaningful.
   Revision 1.9  2003/09/29 09:04:30  venku
   - dump formatting.
   Revision 1.8  2003/09/29 07:30:51  venku
   - added support to spit out local variables names as they occur
     in the source rather than jimplified names.
   Revision 1.7  2003/09/29 06:37:31  venku
   - Each driver now handles each root method separately.
   Revision 1.6  2003/09/29 04:20:57  venku
   - coding convention.
   Revision 1.5  2003/09/28 07:32:30  venku
   - many basic block graphs were being constructed. Now, there
     is only one that will be used.
   Revision 1.4  2003/09/28 06:20:39  venku
   - made the core independent of hard code used to create unit graphs.
     The core depends on the environment to provide a factory that creates
     these unit graphs.
   Revision 1.3  2003/09/28 03:17:13  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.2  2003/09/08 02:23:13  venku
   - Ripple effect of bbm support in Driver and change of constructor
     in ThreadGraph.
   Revision 1.1  2003/08/21 01:24:25  venku
    - Renamed src-escape to src-concurrency to as to group all concurrency
      issue related analyses into a package.
    - Renamed escape package to concurrency.escape.
    - Renamed EquivalenceClassBasedAnalysis to EquivalenceClassBasedEscapeAnalysis.
   Revision 1.4  2003/08/17 10:48:34  venku
   Renamed BFA to FA.  Also renamed bfa variables to fa.
   Ripple effect was huge.
   Revision 1.3  2003/08/11 06:29:07  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.2  2003/08/10 03:43:26  venku
   Renamed Tester to Driver.
   Refactored logic to pick entry points.
   Provided for logging timing stats into any specified stream.
   Ripple effect in others.
   Revision 1.1  2003/08/07 06:39:07  venku
   Major:
    - Moved the package under indus umbrella.
   Minor:
    - changes to accomodate ripple effect from support package.
   Revision 1.1  2003/07/30 08:27:03  venku
   Renamed IATester to EADriver.
   Also, staged various analyses.
 */
