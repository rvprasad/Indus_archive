
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

package edu.ksu.cis.bandera.staticanalyses.escape;

import soot.Local;
import soot.Modifier;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;

import soot.jimple.JimpleBody;

import edu.ksu.cis.bandera.staticanalyses.flow.AbstractAnalyzer;
import edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.processors.CallGraph;
import edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.processors.ThreadGraph;
import edu.ksu.cis.bandera.staticanalyses.interfaces.IThreadGraphInfo.NewExprTriple;
import edu.ksu.cis.bandera.staticanalyses.processing.ProcessingController;
import edu.ksu.cis.bandera.staticanalyses.support.Tester;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * This is the driver for Ruf's escape analysis.  
 * 
 * @deprecated However, this is not documented as it is not used by our framework.
 * This is rather a comparison implementation which will not be supported.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public final class RufEATester
  extends Tester {
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

		CallGraph cg = new CallGraph();
		cg.hookup(ppc);

		ThreadGraph tg = new ThreadGraph(cg);
		tg.hookup(ppc);

		RufsEscapeAnalysis ea1 = new RufsEscapeAnalysis(scm, cg, tg);
		ea1.hookup(ppc);

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: BFA postprocessing");
		}
		start = System.currentTimeMillis();

		ppc.process();
		stop = System.currentTimeMillis();
		addTimeLog(", BFA postprocessing took ", stop - start);

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: BFA postprocessing");
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
		printTimingStats();
	}
}

/*****
 ChangeLog:

$Log$
Revision 1.2  2003/07/27 21:15:22  venku
Minor:
 - arg name changes.
 - comment changes.


*****/
