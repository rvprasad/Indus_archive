
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

package edu.ksu.cis.indus.interfaces;

import edu.ksu.cis.indus.common.datastructures.Triple;

import edu.ksu.cis.indus.processing.Context;

import java.util.Collection;
import java.util.List;

import soot.SootMethod;

import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;


/**
 * This interface provides call graph information pertaining to the analyzed system.   It is adviced that any post processor
 * which provides Call graph information should provide it via this interface.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface ICallGraphInfo
  extends IStatus {
	/**
	 * The id of this interface.
	 */
	String ID = "Callgraph Information";

	/**
	 * This class captures in the information pertaining to a call relation.  It provides the expression, statement, and the
	 * method in which the call occurs.  However, depending on the call relation context, the method may indicate the method
	 * called at an expression and a statement.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	final class CallTriple
	  extends Triple {
		/**
		 * Creates a new CallTriple object.  It is assumed that none of the components of this triple will not change their
		 * state  in ways that will affect equality test or hash code value of the component.
		 *
		 * @param method is the caller or the callee in the relation.
		 * @param stmt in which the call occurs.
		 * @param expr is the call.
		 *
		 * @pre method != null and stmt != null and expr != null
		 */
		public CallTriple(final SootMethod method, final Stmt stmt, final InvokeExpr expr) {
			super(expr, stmt, method);
			optimize();
		}

		/**
		 * Returns the call expression.
		 *
		 * @return the call expression.
		 *
		 * @post result != null
		 */
		public InvokeExpr getExpr() {
			return (InvokeExpr) getFirst();
		}

		/**
		 * Returns the caller or the callee.
		 *
		 * @return the caller/callee.
		 *
		 * @post result != null
		 */
		public SootMethod getMethod() {
			return (SootMethod) getThird();
		}

		/**
		 * Returns the statement in which the call occurs.
		 *
		 * @return the statement containing the call.
		 *
		 * @post result != null
		 */
		public Stmt getStmt() {
			return (Stmt) getSecond();
		}

		/**
		 * Provides a stringized representation of this object.
		 *
		 * @return stringized representation of this object.
		 *
		 * @post result != null
		 */
		protected String stringize() {
			return getStmt() + "@" + getMethod();
		}
	}

	/**
	 * Returns the set of methods called in <code>caller</code>.
	 *
	 * @param caller of interest.
	 *
	 * @return a collection of call-sites.
	 *
	 * @pre caller != null
	 * @post result != null and result.oclIsKindOf(Collection(CallTriple))
	 */
	Collection getCallees(SootMethod caller);

	/**
	 * Returns the set of methods called in the given <code>expr</code> in the given <code>context</code>.
	 *
	 * @param expr that is the call.
	 * @param context in which the expr occurs.  The calling method should occurs in the call string of the context as the
	 * 		  current method.
	 *
	 * @return a collection of methods.
	 *
	 * @pre expr != null and context != null
	 * @post result != null and result.oclIsKindOf(Collection(SootMethod))
	 */
	Collection getCallees(InvokeExpr expr, Context context);

	/**
	 * Returns the set of methods that call <code>callee</code>.
	 *
	 * @param callee is the method invoked.
	 *
	 * @return a colleciton of call-sites.
	 *
	 * @pre callee != null
	 * @post result != null and result.oclIsKindOf(Collection(CallTriple))
	 */
	Collection getCallers(SootMethod callee);

	/**
	 * Returns the methods from which the system starts.
	 *
	 * @return a colleciton of <code>SootMethod</code>s.
	 *
	 * @post result != null and result->forall(o | o.oclType = SootMethod)
	 */
	Collection getHeads();

	/**
	 * Returns the methods that are reachable from the given invocation point via a call chain.
	 *
	 * @param stmt in which the method invocation occurs.
	 * @param root in which the method invocation occurs.
	 *
	 * @return a collection of reachable methods.
	 *
	 * @pre stmt != null and root != null and stmt.containsInvokeExpr() == true
	 * @post result != null and result.oclIsKindOf(Collection(SootMethod))
	 */
	Collection getMethodsReachableFrom(Stmt stmt, SootMethod root);

	/**
	 * Returns the methods that are reachable from withing the given method via a call chain.
	 *
	 * @param root in which the method invocation occurs.
	 * @param forward <code>true</code> indicates that methods reachable by following a call chain from <code>root</code> are
	 * 		  required.  <code>false</code> indicates that methods that can reach <code>root</code> by following a call
	 * 		  chain are required.
	 *
	 * @return a collection of reachable methods.
	 *
	 * @pre root != null
	 * @post result != null and result.oclIsKindOf(Collection(SootMethod))
	 */
	Collection getMethodsReachableFrom(SootMethod root, boolean forward);

	/**
	 * Checks if the <code>method</code> is reachable in the analyzed system.
	 *
	 * @param method to be checked for reachability.
	 *
	 * @return <code>true</code> if <code>method</code> can be reached in the analyzed system; <code>false</code>, otherwise.
	 *
	 * @pre method != null
	 */
	boolean isReachable(SootMethod method);

	/**
	 * Returns a collection of methods that can be reached in the analyzed system.
	 *
	 * @return a collection of methods.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(SootMethod))
	 */
	Collection getReachableMethods();

	/**
	 * Returns a sequence of strongly connected components in the given call graph.
	 *
	 * @param topDown <code>true</code> indicates returned sccs should be in the top-down order; <code>false</code>,
	 * 		  indicates bottom-up.
	 *
	 * @return a sequence of sequence of methods.
	 *
	 * @post result != null and result.oclIsKindOf(Sequence(Sequence(soot.SootMethod)))
	 */
	List getSCCs(boolean topDown);
}

/*
   ChangeLog:
   $Log$
   Revision 1.7  2004/03/03 02:17:50  venku
   - added a new method to ICallGraphInfo interface.
   - implemented the above method in CallGraph.
   - made aliased use-def call-graph sensitive.
   Revision 1.6  2004/01/20 21:23:41  venku
   - the return value of getSCCs needs to be ordered if
     it accepts a direction parameter.  FIXED.
   Revision 1.5  2004/01/06 00:17:10  venku
   - Classes pertaining to workbag in package indus.graph were moved
     to indus.structures.
   - indus.structures was renamed to indus.datastructures.
   Revision 1.4  2003/12/30 09:16:33  venku
   - CallTriple/NewExprTriple are optimized after creation.
   Revision 1.3  2003/12/13 02:28:54  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.2  2003/12/09 04:22:03  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.1  2003/12/08 12:20:40  venku
   - moved some classes from staticanalyses interface to indus interface package
   - ripple effect.
   Revision 1.12  2003/12/08 12:15:59  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.11  2003/12/07 08:41:32  venku
   - deleted getCallGraph() from ICallGraphInfo interface.
   - made getSCCs() direction sensitive.
   - ripple effect.
   Revision 1.10  2003/11/29 09:34:59  venku
   - removed getCycles() method as it was not being used.
   Revision 1.9  2003/11/29 09:30:37  venku
   - removed getRecursionRoots() method as it was not being used.
   - modified pruning algorithmm.
   - modified getCallees(InvokeExpr,Context) method.
   Revision 1.8  2003/11/06 05:15:07  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.7  2003/09/28 03:08:03  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.6  2003/08/21 03:32:37  venku
   Incorporated IStatus interface into any interface that provides analysis information.
   Revision 1.5  2003/08/13 08:29:40  venku
   Spruced up documentation and specification.
   Revision 1.4  2003/08/11 07:46:09  venku
   Finalized the parameters.
   Spruced up Documentation and Specification.
   Revision 1.3  2003/08/11 04:20:19  venku
   - Pair and Triple were changed to work in optimized and unoptimized mode.
   - Ripple effect of the previous change.
   - Documentation and specification of other classes.
   Revision 1.2  2003/08/09 23:26:20  venku
   - Added an interface to provide use-def information.
   - Added an implementation to the above interface.
   - Extended call graph processor to retrieve call tree information rooted at arbitrary node.
   - Modified IValueAnalyzer interface such that only generic queries are possible.
     If required, this can be extended in the future.
   Revision 1.1  2003/08/07 06:42:16  venku
   Major:
    - Moved the package under indus umbrella.
    - Renamed isEmpty() to hasWork() in IWorkBag.
 */
