
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

package edu.ksu.cis.indus.staticanalyses.cfg;

import soot.Scene;
import soot.SootMethod;

import soot.toolkits.graph.CompleteUnitGraph;
import soot.jimple.NewExpr;
import soot.jimple.Stmt;

import edu.ksu.cis.indus.staticanalyses.Context;
import edu.ksu.cis.indus.staticanalyses.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.staticanalyses.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraph;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraphMgr;
import edu.ksu.cis.indus.staticanalyses.support.Util;

import java.util.Collection;
import java.util.Iterator;


/**
 * DOCUMENT ME!
 *
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class CFGAnalysis {
	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private BasicBlockGraphMgr bbm;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private ICallGraphInfo cgi;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private Scene scm;

	/**
	 * Creates a new CFGAnalysis object.
	 *
	 * @param scm DOCUMENT ME!
	 * @param cgi DOCUMENT ME!
	 */
	public CFGAnalysis(Scene scm, ICallGraphInfo cgi) {
		this(scm, cgi, new BasicBlockGraphMgr());
	}

	/**
	 * Creates a new CFGAnalysis object.
	 *
	 * @param scm DOCUMENT ME!
	 * @param cgi DOCUMENT ME!
	 * @param bbm DOCUMENT ME!
	 */
	public CFGAnalysis(Scene scm, ICallGraphInfo cgi, BasicBlockGraphMgr bbm) {
		this.scm = scm;
		this.cgi = cgi;
		this.bbm = bbm;
	}

	/**
	 * Checks if the given new expression is enclosed in a loop.
	 *
	 * @param ne is an allocation expression.
	 * @param context in which <code>ne</code> occurs.
	 *
	 * @return DOCUMENT ME!
	 */
	public boolean checkForLoopEnclosedNewExpr(NewExpr ne, Context context) {
		String classname = ne.getBaseType().getClassName();
		SootMethod sm = context.getCurrentMethod();
		boolean result = false;

		if (Util.isDescendentOf(scm.getSootClass(classname), "java.lang.Thread")) {
			BasicBlockGraph bbg = bbm.getBasicBlockGraph(new CompleteUnitGraph(sm.retrieveActiveBody()));
			Stmt stmt = context.getStmt();

			if (bbg.occursInCycle(bbg.getEnclosingBlock(stmt))) {
				result = true;
			}
		}
		return result;
	}

	/**
	 * Checks if the given soot method 
	 *
	 * @param caller is the method which leads to a thread allocation site.
	 *
	 * @return <code>true</code> if the given method or any of it's ancestors in the call tree have multiple or
	 *            multiply-executed call sites; <code>false</code>, otherwise.
	 */
	public boolean executedMultipleTimes(SootMethod caller) {
		boolean result = false;
		Collection callers = cgi.getCallers(caller);
main_control: 
		if (callers.size() > 1) {
			result = true;
		} else if (callers.size() == 1) {
			for (Iterator i = cgi.getSCCs().iterator(); i.hasNext();) {
				Collection scc = (Collection) i.next();

				if (scc.contains(caller) && scc.size() > 1) {
					result = true;
					break main_control;
				}
			}

			CallTriple ctrp = (CallTriple) callers.iterator().next();
			SootMethod caller2 = ctrp.getMethod();
			BasicBlockGraph bbg = bbm.getBasicBlockGraph(new CompleteUnitGraph(caller2.retrieveActiveBody()));

			if (bbg.occursInCycle(bbg.getEnclosingBlock(ctrp.getStmt()))) {
				result = true;
			} else {
				result = executedMultipleTimes(caller2);
			}
		}
		return result;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * <p></p>
	 *
	 * @param m DOCUMENT ME!
	 * @param p DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public boolean notInSameSCC(SootMethod m, SootMethod p) {
		boolean result = true;
		Collection sccs = cgi.getSCCs();
		Collection scc = null;

		for (Iterator i = sccs.iterator(); i.hasNext();) {
			scc = (Collection) i.next();

			if (scc.contains(m)) {
				break;
			}
			scc = null;
		}

		if (scc != null) {
			result = !scc.contains(p);
		}
		return result;
	}
}

/*****
 ChangeLog:

$Log$
Revision 1.3  2003/05/22 22:18:32  venku
All the interfaces were renamed to start with an "I".
Optimizing changes related Strings were made.


*****/
