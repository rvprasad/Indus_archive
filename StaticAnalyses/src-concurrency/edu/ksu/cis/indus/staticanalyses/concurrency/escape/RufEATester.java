
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
import soot.Modifier;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;

import soot.jimple.JimpleBody;

import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractAnalyzer;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.CallGraph;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.ThreadGraph;
import edu.ksu.cis.indus.staticanalyses.interfaces.IThreadGraphInfo.NewExprTriple;
import edu.ksu.cis.indus.staticanalyses.processing.ProcessingController;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraphMgr;
import edu.ksu.cis.indus.staticanalyses.support.Driver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * This is the driver for Ruf's escape analysis.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 *
 * @deprecated However, this is not documented as it is not used by our framework. This is rather a comparison implementation
 * 			   which will not be supported.
 */
public final class RufEATester
  extends Driver {
	/**
	 * <p>
	 * The logger used by instances of this class to log messages.
	 * </p>
	 */
	private static final Log LOGGER = LogFactory.getLog(RufEATester.class);

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final String[] args;

	/**
	 * Creates a new RufEATester object.
	 *
	 * @param argsParam DOCUMENT ME!
	 */
	private RufEATester(final String[] argsParam) {
		this.args = argsParam;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param argsParam DOCUMENT ME!
	 */
	public static void main(final String[] argsParam) {
		if (argsParam.length == 0) {
			System.out.println("Please specify a class to consider for the analysis.");
		}
		(new RufEATester(argsParam)).execute();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	protected void execute() {
		Scene scm = loadupClassesAndCollectMains(args);
		AbstractAnalyzer aa = OFAnalyzer.getFSOSAnalyzer();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: FA analysis");
		}

		long start = System.currentTimeMillis();
		aa.analyze(scm, rootMethods);

		long stop = System.currentTimeMillis();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: FA analysis");
		}
		addTimeLog("FA analysis", stop - start);

		ProcessingController ppc = new ProcessingController();
		ppc.setAnalyzer(aa);

		CallGraph cg = new CallGraph();
		cg.hookup(ppc);

		ThreadGraph tg = new ThreadGraph(cg, new CFGAnalysis(cg, new BasicBlockGraphMgr()));
		tg.hookup(ppc);

		RufsEscapeAnalysis ea1 = new RufsEscapeAnalysis(scm, cg, tg);
		ea1.hookup(ppc);

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: FA postprocessing");
		}
		start = System.currentTimeMillis();

		ppc.process();
		stop = System.currentTimeMillis();
		addTimeLog(", FA postprocessing took ", stop - start);

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: FA postprocessing");
		}

		ea1.unhook(ppc);
		tg.unhook(ppc);
		cg.unhook(ppc);
		System.out.println("CALL GRAPH:\n" + cg.dumpGraph());
		System.out.println("THREAD GRAPH:\n" + tg.dumpGraph());

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: " + ea1.getClass().getName() + " processing");
		}
		start = System.currentTimeMillis();

		ea1.execute();
		stop = System.currentTimeMillis();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: " + ea1.getClass().getName() + " processing");
		}
		addTimeLog(ea1.getClass().getName(), stop - start);

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

		for (int i = 0; i < args.length; i++) {
			SootClass sc = scm.getSootClass(args[i]);
			System.out.println("Info for class " + sc.getName() + "\n");

			for (Iterator j = sc.getFields().iterator(); j.hasNext();) {
				SootField sf = (SootField) j.next();

				if (Modifier.isStatic(sf.getModifiers())) {
					System.out.println("Info for static field " + sf.getName() + ":" + sf.getType() + "\n"
						+ ea1.tpgetInfo(sf, threadMap) + "\n");
				}
			}

			for (Iterator j = sc.getMethods().iterator(); j.hasNext();) {
				SootMethod sm = (SootMethod) j.next();
				System.out.println("Info for Method " + sm.getSignature() + "\n" + ea1.tpgetInfo(sm, threadMap));

				JimpleBody body = (JimpleBody) sm.retrieveActiveBody();

				for (Iterator k = body.getLocals().iterator(); k.hasNext();) {
					Local local = (Local) k.next();
					System.out.println("Info for Local " + local + ":" + local.getType() + "\n"
						+ ea1.tpgetInfo(sm, local, threadMap) + "\n");
				}
			}
		}
		printTimingStats(System.out);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2003/09/08 02:23:24  venku
 *** empty log message ***
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
   Revision 1.3  2003/07/30 08:30:31  venku
   Refactoring ripple.
   Also fixed a subtle bug in isShared() which caused wrong results.
   Revision 1.2  2003/07/27 21:15:22  venku
   Minor:
    - arg name changes.
    - comment changes.
 */
