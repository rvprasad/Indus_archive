
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

package edu.ksu.cis.indus.staticanalyses.cfg;

import edu.ksu.cis.indus.common.graph.IDirectedGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;

import java.util.Collection;
import java.util.Iterator;

import soot.SootMethod;

import soot.jimple.Stmt;


/**
 * This class performs generic control flow analysis.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public final class CFGAnalysis {
	/**
	 * This manages the basic block graphs of the methods being analyzed.
	 */
	private final BasicBlockGraphMgr bbm;

	/**
	 * This provides call-graph information.
	 */
	private final ICallGraphInfo cgi;

	/**
	 * Creates a new CFGAnalysis object.
	 *
	 * @param cgiParam provides the call-graph information.
	 * @param bbmParam manages the basic block graphs of the methods being analyzed.
	 *
	 * @pre scmParam != null and cgiParam != null and bbmParam != null
	 */
	public CFGAnalysis(final ICallGraphInfo cgiParam, final BasicBlockGraphMgr bbmParam) {
		this.cgi = cgiParam;
		this.bbm = bbmParam;
	}

	/**
	 * Checks if the given new expression is enclosed in a loop.
	 *
	 * @param newStmt is the statement of the allocation site.
	 * @param method in which<code>newStmt</code> occurs.
	 *
	 * @return <code>true</code> if the given allocation site is loop enclosed; <code>false</code>, otherwise.
	 *
	 * @pre ne != null and context != null and context.getCurrentMethod() != null
	 */
	public boolean checkForLoopEnclosedNewExpr(final Stmt newStmt, final SootMethod method) {
		boolean result = false;

		BasicBlockGraph bbg = bbm.getBasicBlockGraph(method);

		if (occursInCycle(bbg, bbg.getEnclosingBlock(newStmt))) {
			result = true;
		}
		return result;
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
		BasicBlockGraph bbg = bbm.getBasicBlockGraph(caller);
		return occursInCycle(bbg, bbg.getEnclosingBlock(stmt)) || executedMultipleTimes(caller);
	}

	/**
	 * Checks if the given soot method is executed multiple times in the system.  It may be due to loop enclosed call-sites,
	 * multiple call sites, or call-sites in call graph SCCs (with more than one element).
	 *
	 * @param caller is the method.
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
			for (Iterator i = cgi.getSCCs(true).iterator(); i.hasNext();) {
				Collection scc = (Collection) i.next();

				if (scc.contains(caller) && scc.size() > 1) {
					result = true;
					break main_control;
				}
			}

			CallTriple ctrp = (CallTriple) callers.iterator().next();
			SootMethod caller2 = ctrp.getMethod();
			BasicBlockGraph bbg = bbm.getBasicBlockGraph(caller2);

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
		Collection sccs = cgi.getSCCs(true);
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

	/**
	 * Checks if the given node occurs in a cycle in the graph.  This may be sufficient in some cases rather than capturing
	 * the cycle itself.
	 *
	 * @param graph in which <code>node</code> occurs.
	 * @param node which may occur in a cycle.
	 *
	 * @return <code>true</code> if <code>node</code> occurs in cycle; <code>false</code>, otherwise.
	 */
	public boolean occursInCycle(final IDirectedGraph graph, final BasicBlock node) {
		return graph.isReachable(node, node, true);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.16  2004/01/21 02:36:41  venku
   - coding convention.
   Revision 1.15  2003/12/13 02:29:08  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.14  2003/12/09 04:22:10  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.13  2003/12/08 12:19:47  venku
   - coding convention.
   Revision 1.12  2003/12/08 12:16:00  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.11  2003/12/07 08:41:45  venku
   - deleted getCallGraph() from ICallGraphInfo interface.
   - made getSCCs() direction sensitive.
   - ripple effect.
   Revision 1.10  2003/12/02 09:42:38  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.9  2003/11/06 05:15:07  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.8  2003/09/28 06:46:49  venku
   - Some more changes to extract unit graphs from the enviroment.
   Revision 1.7  2003/09/08 02:20:12  venku
   - it now only requires call graph info and basic block graph manager
   - checkForLoopEnclosedNewExpr() is now applicable to any allocation sites
   - added a new method to extract basic block graph
   Revision 1.6  2003/09/07 21:59:31  venku
   - missing documentation.  FIXED.
   Revision 1.5  2003/09/01 11:56:20  venku
   - instantiated occursInCycle()
   - added executedMultipleTimes() for Stmt and SootMethod.
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
    - Renamed isEmpty() to hasWork() in IWorkBag.
   Revision 1.3  2003/05/22 22:18:32  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
