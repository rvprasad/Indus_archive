/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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

import edu.ksu.cis.indus.common.collections.IFactory;
import edu.ksu.cis.indus.common.datastructures.Triple;

import edu.ksu.cis.indus.processing.Context;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.SootMethod;

import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;

/**
 * This interface provides call graph information pertaining to the analyzed system. It is adviced that any post processor
 * which provides Call graph information should provide it via this interface.
 * <p>
 * Subtypes of this class have to return the constant <code>ID</code> defined in this class as a result of
 * <code>getId</code>.
 * </p>
 * <p>
 * A method <i>a</i> is reachable from method <i>b</i> if there is an explicit call from <i>a</i> to <i>b</i>. This is
 * also holds when <i>a=b</i>.
 * </p>
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface ICallGraphInfo
		extends IStatus, IIdentification {

	/**
	 * This class captures in the information pertaining to a call relation. It provides the expression, statement, and the
	 * method in which the call occurs. However, depending on the call relation context, the method may indicate the method
	 * called at an expression and a statement.
	 * 
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	final class CallTriple
			extends Triple<InvokeExpr, Stmt, SootMethod> {

		/**
		 * DOCUMENT ME!
		 */
		public static final IFactory<Set<CallTriple>> SET_FACTORY = new IFactory<Set<CallTriple>>() {

			public Set<CallTriple> create() {
				return new HashSet<CallTriple>();
			}

		};

		/**
		 * Creates a new CallTriple object. It is assumed that none of the components of this triple will not change their
		 * state in ways that will affect equality test or hash code value of the component.
		 * 
		 * @param method is the caller or the callee in the relation.
		 * @param stmt in which the call occurs.
		 * @param expr is the call.
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
		 * @post result != null
		 */
		public InvokeExpr getExpr() {
			return getFirst();
		}

		/**
		 * Returns the caller or the callee.
		 * 
		 * @return the caller/callee.
		 * @post result != null
		 */
		public SootMethod getMethod() {
			return getThird();
		}

		/**
		 * Returns the statement in which the call occurs.
		 * 
		 * @return the statement containing the call.
		 * @post result != null
		 */
		public Stmt getStmt() {
			return getSecond();
		}

		/**
		 * Provides a stringized representation of this object.
		 * 
		 * @return stringized representation of this object.
		 * @post result != null
		 */
		@Override protected String stringize() {
			return getStmt() + "@" + getMethod();
		}
	}

	/**
	 * The id of this interface.
	 */
	Comparable<? extends Object> ID = "Callgraph Information";

	/**
	 * Checks if any of the given methods are reachable from the given caller.
	 * 
	 * @param methods that should be reached.
	 * @param caller containing the call-site.
	 * @return <code>true</code> if any of the <code>methods</code> are reachable from <code>caller</code>;
	 *         <code>false</code>, otherwise.
	 * @pre mehtod != null and caller != null
	 * @pre stmt.containsInvokeExpr()
	 */
	boolean areAnyMethodsReachableFrom(Collection<SootMethod> methods, SootMethod caller);

	/**
	 * Checks if any of the given methods are reachable from the given call-site.
	 * 
	 * @param methods that should be reached.
	 * @param stmt containing the call-site.
	 * @param caller containing the call-site.
	 * @return <code>true</code> if any of the <code>methods</code> are reachable from call-site at <code>stmt</code> in
	 *         <code>caller</code>; <code>false</code>, otherwise.
	 * @pre mehtod != null and stmt != null and caller != null
	 * @pre stmt.containsInvokeExpr()
	 */
	boolean areAnyMethodsReachableFrom(Collection<SootMethod> methods, Stmt stmt, SootMethod caller);

	/**
	 * Returns the set of methods called in the given <code>expr</code> in the given <code>context</code>.
	 * 
	 * @param expr that is the call.
	 * @param context in which the expr occurs. The calling method should occurs in the call string of the context as the
	 *            current method.
	 * @return a collection of methods.
	 * @pre expr != null and context != null
	 * @post result != null
	 */
	Collection<SootMethod> getCallees(InvokeExpr expr, Context context);

	/**
	 * Returns the set of methods called in <code>caller</code>.
	 * 
	 * @param caller of interest.
	 * @return a collection of call-sites.
	 * @pre caller != null
	 * @post result != null
	 */
	Collection<CallTriple> getCallees(SootMethod caller);

	/**
	 * Returns the set of methods that call <code>callee</code>.
	 * 
	 * @param callee is the method invoked.
	 * @return a colleciton of call-sites.
	 * @pre callee != null
	 * @post result != null
	 */
	Collection<CallTriple> getCallers(SootMethod callee);

	/**
	 * Returns the intersection of the methods reachable from the given methods in the given direction. This is equivalent to
	 * <code>CollectionUtils.intersection(getMethodsReachableFrom(method1, forward1), getMethodsReachableFrom(method2,
	 * forward2))</code>.
	 * 
	 * @param method1 of interest.
	 * @param forward1 direction of reachability from <code>method1</code>.
	 * @param method2 of interest.
	 * @param forward2 direction of reachability from <code>method2</code>.
	 * @return a collection of methods.
	 * @pre method1 != null and method2 != null
	 * @post result != null
	 * @post getReachableFrom(method2, forward2)->forall(o | getReachableFrom(method1, forward2).contains(o) implies
	 *       result.contains(o))
	 * @post result->forall(o | getReachableFrom(method2, forward2).contains(o) and getReachableFrom(method1,
	 *       forward1).contains(o))
	 */
	Collection<SootMethod> getCommonMethodsReachableFrom(SootMethod method1, boolean forward1, SootMethod method2,
			boolean forward2);

	/**
	 * Retrieves the minimum set of callees that ensure the given methods reach common callees.
	 * 
	 * @param method1 is one method of interest.
	 * @param method2 is another method of interest.
	 * @return a collection of methods.
	 * @pre method1 != null and method2 != null
	 * @post result != null
	 * @post getReachableMethods().containsAll(result)
	 * @post
	 * @post result->forall(o | isReachable(o, method, false) and isReachable(o, method, false))
	 */
	Collection<SootMethod> getConnectivityCalleesFor(SootMethod method1, SootMethod method2);

	/**
	 * Retrieves the minimum set of callers that ensure the common callers can reach the given methods.
	 * 
	 * @param method1 is one method of interest.
	 * @param method2 is another method of interest.
	 * @return a collection of methods.
	 * @pre method1 != null and method2 != null
	 * @post result != null
	 * @post getReachableMethods().containsAll(result)
	 * @post
	 * @post result->forall(o | isReachable(o, method, true) and isReachable(o, method, true))
	 */
	Collection<SootMethod> getConnectivityCallersFor(SootMethod method1, SootMethod method2);

	/**
	 * Returns the methods from which the system starts.
	 * 
	 * @return a colleciton of <code>SootMethod</code>s.
	 * @post result != null
	 */
	Collection<SootMethod> getEntryMethods();

	/**
	 * Retrieves the methods in the call graph in topological order.
	 * 
	 * @param topdown <code>true</code> indicates in top-down order; <code>false</code> indicates bottom-up order.
	 * @return the methods in the call graph.
	 * @post result != null
	 */
	List<SootMethod> getMethodsInTopologicalOrder(boolean topdown);

	/**
	 * Returns the methods that are reachable from withing the given method via a call chain.
	 * 
	 * @param root in which the method invocation occurs.
	 * @param forward <code>true</code> indicates that methods reachable by following a call chain from <code>root</code>
	 *            are required. <code>false</code> indicates that methods that can reach <code>root</code> by following a
	 *            call chain are required.
	 * @return a collection of reachable methods.
	 * @pre root != null
	 * @post result != null
	 */
	Collection<SootMethod> getMethodsReachableFrom(SootMethod root, boolean forward);

	/**
	 * Returns the methods that are reachable from the given invocation point via a call chain.
	 * 
	 * @param stmt in which the method invocation occurs.
	 * @param root in which the method invocation occurs.
	 * @return a collection of reachable methods.
	 * @pre stmt != null and root != null and stmt.containsInvokeExpr() == true
	 * @post result != null
	 */
	Collection<SootMethod> getMethodsReachableFrom(Stmt stmt, SootMethod root);

	/**
	 * Returns a collection of methods that can be reached in the analyzed system.
	 * 
	 * @return a collection of methods.
	 * @post result != null
	 */
	Collection<SootMethod> getReachableMethods();

	/**
	 * Returns a sequence of strongly connected components in the given call graph.
	 * 
	 * @param topDown <code>true</code> indicates returned sccs should be in the top-down order; <code>false</code>,
	 *            indicates bottom-up.
	 * @return a sequence of sequence of methods.
	 * @post result != null
	 */
	List<List<SootMethod>> getSCCs(boolean topDown);

	/**
	 * Checks if the <code>callee</code> is reachable <code>caller</code>.
	 * 
	 * @param callee is the method that needs to be reached.
	 * @param caller is the method to start the search from.
	 * @return <code>true</code> if <code>callee</code> can be reached from <code>caller</code>; <code>false</code>,
	 *         otherwise.
	 * @pre callee != null and caller != null
	 * @post not (isReachable(callee) and isReachable(caller)) implies result == false
	 */
	boolean isCalleeReachableFromCaller(SootMethod callee, SootMethod caller);

	/**
	 * Checks if the <code>callee</code> is reachable <code>caller</code>.
	 * 
	 * @param callee is the method that needs to be reached.
	 * @param stmt in the <code>caller</code> at which invocation occurs.
	 * @param caller is the method to start the search from.
	 * @return <code>true</code> if <code>callee</code> can be reached from <code>caller</code> via the call at
	 *         <code>stmt</code>; <code>false</code>, otherwise.
	 * @pre callee != null and caller != null and stmt != null
	 * @post not (isReachable(callee) and isReachable(caller)) implies result == false
	 * @post result implies isCalleeReachableFromCaller(callee, caller)
	 */
	boolean isCalleeReachableFromCallSite(SootMethod callee, Stmt stmt, SootMethod caller);

	/**
	 * Checks if the <code>method</code> is reachable in the analyzed system.
	 * 
	 * @param method to be checked for reachability.
	 * @return <code>true</code> if <code>method</code> can be reached in the analyzed system; <code>false</code>,
	 *         otherwise.
	 * @pre method != null
	 */
	boolean isReachable(SootMethod method);
}

// End of File
