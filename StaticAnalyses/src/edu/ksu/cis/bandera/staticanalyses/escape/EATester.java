
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

package edu.ksu.cis.bandera.staticanalyses.escape;

import ca.mcgill.sable.soot.ClassFile;
import ca.mcgill.sable.soot.Modifier;
import ca.mcgill.sable.soot.SootClass;
import ca.mcgill.sable.soot.SootClassManager;
import ca.mcgill.sable.soot.SootField;
import ca.mcgill.sable.soot.SootMethod;

import ca.mcgill.sable.soot.jimple.Jimple;
import ca.mcgill.sable.soot.jimple.JimpleBody;
import ca.mcgill.sable.soot.jimple.Local;

import ca.mcgill.sable.util.Iterator;

import edu.ksu.cis.bandera.staticanalyses.flow.AbstractAnalyzer;
import edu.ksu.cis.bandera.staticanalyses.flow.PostProcessingController;
import edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.postprocessors.CallGraph;
import edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.postprocessors.ThreadGraph;
import edu.ksu.cis.bandera.staticanalyses.flow.interfaces.CallGraphInfo.CallTriple;
import edu.ksu.cis.bandera.staticanalyses.flow.interfaces.ThreadGraphInfo.NewExprTriple;
import edu.ksu.cis.bandera.staticanalyses.support.Tester;

import java.util.HashMap;
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
public class EATester extends Tester {
	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private String args[];

	/**
	 * Creates a new EATester object.
	 *
	 * @param args DOCUMENT ME!
	 */
	private EATester(String args[]) {
		this.args = args;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param args DOCUMENT ME!
	 */
	public static void main(String args[]) {
		if (args.length == 0) {
			System.out.println("Please specify a class to consider for the analysis.");
		}
		(new EATester(args)).execute();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	private void execute() {
		SootClassManager scm = loadupClassesAndCollectMains(args);
		AbstractAnalyzer aa = OFAnalyzer.getFSOSAnalyzer("EATester");
		aa.analyze(scm, rootMethods);

		PostProcessingController ppc = new PostProcessingController();
		ppc.setAnalyzer(aa);

		CallGraph cg = new CallGraph();
		cg.hookup(ppc);

		ThreadGraph tg = new ThreadGraph(cg);
		tg.hookup(ppc);

		RufsTweakedEscapeAnalysis ea = new RufsTweakedEscapeAnalysis(scm, cg, tg);
		ea.hookup(ppc);
		ppc.process();
		System.out.println("CALL GRAPH:\n" + cg.dumpGraph());
		System.out.println("THREAD GRAPH:\n" + tg.dumpGraph());

		ea.execute();

		int count = 1;
		Map threadMap = new HashMap();
		System.out.println("\nThread mapping:");

		for (java.util.Iterator j = tg.getAllocationSites().iterator(); j.hasNext();) {
			NewExprTriple element = (NewExprTriple) j.next();
			String tid = "T" + count++;
			threadMap.put(element, tid);
			if (element.getMethod() == null)
				System.out.println(tid + " -> " + element.getExpr().getType());
			else
				System.out.println(tid + " -> " + element.getStmt() + "@" + element.getMethod());
		}

		for (int i = 0; i < args.length; i++) {
			SootClass sc = scm.getClass(args[i]);
			System.out.println("Info for class " + sc.getName() + "\n");

			for (Iterator j = sc.getFields().iterator(); j.hasNext();) {
				SootField sf = (SootField) j.next();

				if (Modifier.isStatic(sf.getModifiers())) {
					System.out.println(
						"Info for static field "
							+ sf.getName()
							+ ":"
							+ sf.getType()
							+ "\n"
							+ ea.tpgetInfo(sf, threadMap)
							+ "\n");
				}
			}

			for (Iterator j = sc.getMethods().iterator(); j.hasNext();) {
				SootMethod sm = (SootMethod) j.next();
				System.out.println("Info for Method " + sm.getSignature() + "\n" + ea.tpgetInfo(sm, threadMap));

				JimpleBody body;

				try {
					body = (JimpleBody) sm.getBody(Jimple.v());
				} catch (Exception e) {
					try {
						body = new JimpleBody(sm, sm.getBody(ClassFile.v()), 0);
					} catch (Exception ee) {
						continue;
					}
				}

				for (Iterator k = body.getLocals().iterator(); k.hasNext();) {
					Local local = (Local) k.next();
					System.out.println(
						"Info for Local " + local + ":" + local.getType() + "\n" + ea.tpgetInfo(sm, local, threadMap) + "\n");
				}
				/*
				for (java.util.Iterator l = cg.getCallees(sm).iterator(); l.hasNext();) {
					CallTriple ctrp = (CallTriple) l.next();
					ctrp = new CallTriple(sm, ctrp.getStmt(), ctrp.getExpr()); 
					System.out.println("Info for call site " + ctrp.getStmt() + "\n" + ea.tpgetInfo(ctrp, threadMap));
				}
				*/
			}
		}
	}
}

/*****
 ChangeLog:

$Log$

*****/
