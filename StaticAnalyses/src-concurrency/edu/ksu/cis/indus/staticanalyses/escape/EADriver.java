
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (C) 2002, 2003, 2004.
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

package edu.ksu.cis.indus.staticanalyses.escape;

import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

import soot.jimple.JimpleBody;

import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.CallGraph;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.ThreadGraph;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.interfaces.IThreadGraphInfo.NewExprTriple;
import edu.ksu.cis.indus.staticanalyses.processing.CGBasedProcessingController;
import edu.ksu.cis.indus.staticanalyses.processing.ProcessingController;
import edu.ksu.cis.indus.staticanalyses.support.Driver;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
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
  extends Driver {
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
	 * Drives escape analysis. It executes BFA first, followed by any post process analysis, followed by the escape analysis.
	 */
	protected void execute() {
		Scene scm = loadupClassesAndCollectMains(args);
		IValueAnalyzer aa = OFAnalyzer.getFSOSAnalyzer();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: BFA analysis");
		}

		long start = System.currentTimeMillis();
		aa.analyze(scm, rootMethods);

		long stop = System.currentTimeMillis();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: BFA analysis");
		}
		addTimeLog("BFA analysis", stop - start);

		ProcessingController ppc = new ProcessingController();
		ppc.setAnalyzer(aa);

        // Create call graph
		CallGraph cg = new CallGraph();
		cg.hookup(ppc);

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: BFA postprocessing");
		}
		start = System.currentTimeMillis();
		ppc.process();
		cg.unhook(ppc);

        ppc = new CGBasedProcessingController(cg);
        ppc.setAnalyzer(aa);
        // Create Thread graph 
		ThreadGraph tg = new ThreadGraph(cg);
		tg.hookup(ppc);
		ppc.process();
        tg.unhook(ppc);
        
        // Perform equivalence-class-based escape analysis
        EquivalenceClassBasedAnalysis analysis = new EquivalenceClassBasedAnalysis(scm, cg, tg);
        analysis.hookup(ppc);
        ppc.process();
        stop = System.currentTimeMillis();
		analysis.unhook(ppc);

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: BFA postprocessing");
		}
		addTimeLog("BFA postprocessing took ", stop - start);
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

		for (int i = 0; i < args.length; i++) {
			SootClass sc = scm.getSootClass(args[i]);
			System.out.println("Info for class " + sc.getName() + "\n");

			for (Iterator j = CollectionUtils.intersection(cg.getReachableMethods(), sc.getMethods()).iterator();
				  j.hasNext();) {
				SootMethod sm = (SootMethod) j.next();
				System.out.println("\nInfo for Method " + sm.getSignature());

				if (sm.isConcrete()) {
					JimpleBody body = (JimpleBody) sm.retrieveActiveBody();

					for (Iterator k = body.getLocals().iterator(); k.hasNext();) {
						Local local = (Local) k.next();
						System.out.println(" Local " + local + ":" + local.getType() + " -> shared:"
							+ analysis.isShared(local, sm));
					}
				} else {
					if (LOGGER.isInfoEnabled()) {
						LOGGER.info(sm + " is not a concrete method.  Hence, it's body could not be retrieved.");
					}
				}
			}
		}
		printTimingStats(System.out);
	}
}

/*****
 ChangeLog:

$Log$
Revision 1.1  2003/08/07 06:39:07  venku
Major:
 - Moved the package under indus umbrella.

Minor:
 - changes to accomodate ripple effect from support package.

Revision 1.1  2003/07/30 08:27:03  venku
Renamed IATester to EADriver.
Also, staged various analyses.


*****/
