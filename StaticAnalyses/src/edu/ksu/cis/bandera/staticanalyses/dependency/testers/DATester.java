
/*
 * Bandera, a Java(TM) analysis and transformation toolkit
 * Copyright (C) 2002, 2003, 2004.
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://www.cis.ksu.edu/santos/bandera).
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
 *                http://www.cis.ksu.edu/santos/bandera
 */

package edu.ksu.cis.bandera.staticanalyses.dependency.testers;

import ca.mcgill.sable.soot.SootClassManager;
import ca.mcgill.sable.soot.SootMethod;

import ca.mcgill.sable.soot.jimple.CompleteStmtGraph;

import edu.ksu.cis.bandera.staticanalyses.ProcessingController;
import edu.ksu.cis.bandera.staticanalyses.dependency.DependencyAnalysis;
import edu.ksu.cis.bandera.staticanalyses.flow.AbstractAnalyzer;
import edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.processors.CallGraph;
import edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.processors.ThreadGraph;
import edu.ksu.cis.bandera.staticanalyses.interfaces.CallGraphInfo;
import edu.ksu.cis.bandera.staticanalyses.interfaces.Environment;
import edu.ksu.cis.bandera.staticanalyses.interfaces.ThreadGraphInfo;
import edu.ksu.cis.bandera.staticanalyses.interfaces.ThreadGraphInfo.NewExprTriple;
import edu.ksu.cis.bandera.staticanalyses.support.Pair.PairManager;
import edu.ksu.cis.bandera.staticanalyses.support.Tester;
import edu.ksu.cis.bandera.staticanalyses.support.Util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public abstract class DATester
  extends Tester {
	/**
	 * <p>
	 * The logger used by instances of this class to log messages.
	 * </p>
	 */
	private static final Log LOGGER = LogFactory.getLog(DATester.class);

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	protected AbstractAnalyzer aa;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	protected Map info;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	protected Map method2cmpltStmtGraph = new HashMap();

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	protected SootClassManager scm;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private String args[];

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param args DOCUMENT ME!
	 */
	public void run(String args[]) {
		if(args.length == 0) {
			System.out.println("Please specify a class to consider for the analysis.");
			System.exit(-1);
		}
		this.args = args;
		execute();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	protected abstract Collection getDependencyAnalyses();

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	protected void execute() {
		LOGGER.info("Loading classes....");

		scm = loadupClassesAndCollectMains(args);
		aa = OFAnalyzer.getFSOSAnalyzer("DATester");
		LOGGER.info("BEGIN: BFA");

		long start = System.currentTimeMillis();
		aa.analyze(scm, rootMethods);

		long stop = System.currentTimeMillis();
		addTimeLog("BFA", stop - start);
		LOGGER.info("END: BFA");

		ProcessingController pc = new ProcessingController();
		pc.setAnalyzer(aa);

		CallGraph cg = new CallGraph();
		cg.hookup(pc);

		ThreadGraph tg = new ThreadGraph(cg);
		tg.hookup(pc);
		LOGGER.info("BEGIN: BFA post processing");
		start = System.currentTimeMillis();
		pc.process();
		stop = System.currentTimeMillis();
		addTimeLog("BFA post processing", stop - start);
		LOGGER.info("END: BFA post processing");
		cg.unhook(pc);
		tg.unhook(pc);

		System.out.println("CALL GRAPH:\n" + cg.dumpGraph());
		System.out.println("THREAD GRAPH:\n" + tg.dumpGraph());

		Map method2cmpltStmtGraph = new HashMap();

		for(Iterator i = cg.getReachableMethods().iterator(); i.hasNext();) {
			SootMethod method = (SootMethod) i.next();

			try {
				CompleteStmtGraph sg = new CompleteStmtGraph((Util.getJimpleBody(method)).getStmtList());
				method2cmpltStmtGraph.put(method, sg);
			} catch(RuntimeException e) {
				LOGGER.warn("Method " + method.getSignature() + " may not have body.", e);
			}
		}

		info = new HashMap();
		info.put(CallGraphInfo.ID, cg);
		info.put(ThreadGraphInfo.ID, tg);
		info.put(PairManager.ID, new PairManager());
		info.put(Environment.ID, aa.getEnvironment());

		Collection das = getDependencyAnalyses();
		LOGGER.info("BEGIN: dependency analyses");

		for(Iterator i = das.iterator(); i.hasNext();) {
			DependencyAnalysis da = (DependencyAnalysis) i.next();
			start = System.currentTimeMillis();
			da.analyze();
			stop = System.currentTimeMillis();
			addTimeLog(da.getClass().getName() + " analysis", stop - start);
		}
		LOGGER.info("END: dependency analyses");

		Map threadMap = new HashMap();
		System.out.println("\nThread mapping:");

		int count = 1;

		for(java.util.Iterator j = tg.getAllocationSites().iterator(); j.hasNext();) {
			NewExprTriple element = (NewExprTriple) j.next();
			String tid = "T" + count++;
			threadMap.put(element, tid);

			if(element.getMethod() == null) {
				System.out.println(tid + " -> " + element.getExpr().getType());
			} else {
				System.out.println(tid + " -> " + element.getStmt() + "@" + element.getMethod());
			}
		}

		for(Iterator i = das.iterator(); i.hasNext();) {
			System.out.println(((DependencyAnalysis) i.next()));
		}
		printTimingStats();
	}
}

/*****
 ChangeLog:

$Log$

*****/
