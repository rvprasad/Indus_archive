
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

package edu.ksu.cis.indus.staticanalyses.cfg;

import soot.Scene;
import soot.SootMethod;

import soot.jimple.NewExpr;
import soot.jimple.Stmt;

import soot.toolkits.graph.CompleteUnitGraph;

import edu.ksu.cis.indus.staticanalyses.Context;
import edu.ksu.cis.indus.staticanalyses.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.staticanalyses.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraph;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraphMgr;
import edu.ksu.cis.indus.staticanalyses.support.DirectedGraph;
import edu.ksu.cis.indus.staticanalyses.support.Util;

import java.util.Collection;
import java.util.Iterator;


/**
 * This class performs generic control flow analysis.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class CFGAnalysis {
	/**
	 * This manages the basic block graphs of the methods being analyzed.
	 */
	private BasicBlockGraphMgr bbm;

	/**
	 * This provides call-graph information.
	 */
	private ICallGraphInfo cgi;

	/**
	 * This manages the classes being analyzed.
	 */
	private Scene scm;

	/**
	 * Creates a new CFGAnalysis object.
	 *
	 * @param scmParam manages the classes being analyzed.
	 * @param cgiParam provides the call-graph information.
	 *
	 * @pre scmParam != null and cgiParam != null
	 */
	public CFGAnalysis(final Scene scmParam, final ICallGraphInfo cgiParam) {
		this(scmParam, cgiParam, new BasicBlockGraphMgr());
	}

	/**
	 * Creates a new CFGAnalysis object.
	 *
	 * @param scmParam manages the classes being analyzed.
	 * @param cgiParam provides the call-graph information.
	 * @param bbmParam manages the basic block graphs of the methods being analyzed.
	 *
	 * @pre scmParam != null and cgiParam != null and bbmParam != null
	 */
	public CFGAnalysis(final Scene scmParam, final ICallGraphInfo cgiParam, final BasicBlockGraphMgr bbmParam) {
		this.scm = scmParam;
		this.cgi = cgiParam;
		this.bbm = bbmParam;
	}

	/**
	 * Checks if the given new expression is enclosed in a loop.
	 *
	 * @param ne is an allocation expression.
	 * @param context in which <code>ne</code> occurs.
	 *
	 * @return <code>true</code> if the given allocation site is loop enclosed; <code>false</code>, otherwise.
	 *
	 * @pre ne != null and context != null and context.getCurrentMethod() != null
	 */
	public boolean checkForLoopEnclosedNewExpr(final NewExpr ne, final Context context) {
		String classname = ne.getBaseType().getClassName();
		SootMethod sm = context.getCurrentMethod();
		boolean result = false;

		if (Util.isDescendentOf(scm.getSootClass(classname), "java.lang.Thread")) {
			BasicBlockGraph bbg = bbm.getBasicBlockGraph(new CompleteUnitGraph(sm.retrieveActiveBody()));
			Stmt stmt = context.getStmt();

			if (occursInCycle(bbg, bbg.getEnclosingBlock(stmt))) {
				result = true;
			}
		}
		return result;
	}

	/**
	 * Checks if the given node occurs in a cycle in the graph.  This may be sufficient in some cases rather than capturing
	 * the cycle itself.
	 *
	 * @param graph in which <code>node</code> occurs.
	 * @param node which may occur in a cycle.
	 *
	 * @return <code>true</code> if <code>node</code> occurs in cycle; <code>false</code>, otherwise.
	 */
	public final boolean occursInCycle(final DirectedGraph graph, final BasicBlock node) {
		return graph.isReachable(node, node, true);
	}

	/**
	 * Checks if the given statement is executed multiple times as a result of being loop-enclosed or occuring in a  method
	 * that is executed multiple times.
	 *
	 * @param stmt is the statement.
	 * @param caller is the method in which <code>stmt</code> occurs.
	 *
	 * @return <code>true</code> if <code>stmt</code> is executed multiple times; <code>false</code>, otherwise.
	 *
	 * @pre stmt != null and caller != null
	 */
	public boolean executedMultipleTimes(final Stmt stmt, final SootMethod caller) {
		BasicBlockGraph bbg = bbm.getBasicBlockGraph(new CompleteUnitGraph(caller.retrieveActiveBody()));
		return occursInCycle(bbg, bbg.getEnclosingBlock(stmt)) || executedMultipleTimes(caller);
	}

	/**
	 * Checks if the given soot method
	 *
	 * @param caller is the method which leads to a thread allocation site.
	 *
	 * @return <code>true</code> if the given method or any of it's ancestors in the call tree have multiple or
	 * 		   multiply-executed call sites; <code>false</code>, otherwise.
	 *
	 * @pre caller != null
	 */
	public boolean executedMultipleTimes(final SootMethod caller) {
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

			if (occursInCycle(bbg, bbg.getEnclosingBlock(ctrp.getStmt()))) {
				result = true;
			} else {
				result = executedMultipleTimes(caller2);
			}
		}
		return result;
	}

	/**
	 * Checks if the given methods occur in the same SCC in the call graph of the system.
	 *
	 * @param m is one of the methods.
	 * @param p is another method.
	 *
	 * @return <code>true</code> if the given methods occur in the same SCC; <code>false</code>, otherwise.
	 *
	 * @pre m != null and p != null
	 */
	public boolean notInSameSCC(final SootMethod m, final SootMethod p) {
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

/*
   ChangeLog:
   $Log$
   Revision 1.4  2003/08/24 12:04:32  venku
   Removed occursInCycle() method from DirectedGraph.
   Installed occursInCycle() method in CFGAnalysis.
   Converted performTopologicalsort() and getFinishTimes() into instance methods.
   Ripple effect of the above changes.
   Revision 1.3  2003/08/14 05:00:48  venku
   Spruced up specification.
   Revision 1.2  2003/08/11 08:49:34  venku
   Javadoc documentation errors were fixed.
   Some classes were documented.
   Revision 1.1  2003/08/07 06:42:16  venku
   Major:
    - Moved the package under indus umbrella.
    - Renamed isEmpty() to hasWork() in WorkBag.
   Revision 1.3  2003/05/22 22:18:32  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
