
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

package edu.ksu.cis.bandera.staticanalyses.flow.interfaces;

import ca.mcgill.sable.soot.SootMethod;

import ca.mcgill.sable.soot.jimple.InvokeExpr;
import ca.mcgill.sable.soot.jimple.Stmt;

import edu.ksu.cis.bandera.staticanalyses.flow.Context;
import edu.ksu.cis.bandera.staticanalyses.support.SimpleNodeGraph;
import edu.ksu.cis.bandera.staticanalyses.support.Triple;

import java.util.Collection;


/**
 * This interface provides call graph information pertaining to the analyzed system.   It is adviced that any post processor
 * which provides Call graph information should provide it via this interface.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface CallGraphInfo {
	/**
	 * The id of this interface.
	 */
	public String ID = "Callgraph Information";

	/**
	 * This class captures in the information pertaining to a call relation.  It provides the expression, statement, and the
	 * method in which the call occurs.  However, depending on the call relation context, the method may indicate the method
	 * called at an expression and a statement.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	public final class CallTriple
	  extends Triple {
		/**
		 * Creates a new CallTriple object.
		 *
		 * @param method is the caller or the callee in the relation.
		 * @param stmt in which the call occurs.
		 * @param expr is the call.
		 */
		public CallTriple(SootMethod method, Stmt stmt, InvokeExpr expr) {
			super(expr, stmt, method);
		}

		/**
		 * Returns the call expression.
		 *
		 * @return the call expression.
		 */
		public InvokeExpr getExpr() {
			return (InvokeExpr) getFirst();
		}

		/**
		 * Returns the caller or the callee.
		 *
		 * @return the caller/callee.
		 */
		public SootMethod getMethod() {
			return (SootMethod) getThird();
		}

		/**
		 * Returns the statement in which the call occurs.
		 *
		 * @return the statement containing the call.
		 */
		public Stmt getStmt() {
			return (Stmt) getSecond();
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return SimpleNodeGraph
	 *
	 * @post result->getNodes()->forall(o.oclType = SimpleNodeGraph.SimpleNode and o.object.oclType = SootMethod)
	 */
	public SimpleNodeGraph getCallGraph();

	/**
	 * Returns the set of methods called in <code>caller</code>.
	 *
	 * @param caller of interest.
	 *
	 * @return a collection of <code>CallTriple</code>s.
	 *
	 * @post result->forall(o | o.oclType = CallTriple)
	 */
	public Collection getCallees(SootMethod caller);

	/**
	 * Returns the set of methods called in the given <code>expr</code> in the given <code>context</code>.
	 *
	 * @param expr that is the call.
	 * @param context in which the expr occurs.  The calling method should occurs in the call string of the context as the
	 * 		  current method.
	 *
	 * @return a collection of <code>SootMethod</code>s.
	 *
	 * @post result->forall(o | o.oclType = SootMethod)
	 */
	public Collection getCallees(InvokeExpr expr, Context context);

	/**
	 * Returns the set of methods that call <code>callee</code>.
	 *
	 * @param callee is the method invoked.
	 *
	 * @return a colleciton of <code>CallTriple</code>s.
	 *
	 * @post result->forall(o | o.oclType = CallTriple)
	 */
	public Collection getCallers(SootMethod callee);

	/**
	 * Returns a collection of method lists.  The methods in the list form cycles.
	 *
	 * @return a collection of <code>List</code>s of <code>SootMethod</code>s.
	 *
	 * @post result->forall(o | o.oclType = java.util.List(SootMethod))
	 */
	public Collection getCycles();

	/**
	 * Returns the methods from which the system starts.
	 *
	 * @return a colleciton of <code>SootMethod</code>s.
	 *
	 * @post result->forall(o | o.oclType = SootMethod)
	 */
	public Collection getHeads();

	/**
	 * Checks if the <code>method</code> is reachable in the analyzed system.
	 *
	 * @param method to be checked for reachability.
	 *
	 * @return <code>true</code> if <code>method</code> can be reached in the analyzed system; <code>false</code>, otherwise.
	 */
	public boolean isReachable(SootMethod method);

	/**
	 * Returns a collection of methods that can be reached in the analyzed system.
	 *
	 * @return a collection of <code>SootMethod</code>.
	 *
	 * @post result->forall(o | o.oclType = SootMethod)
	 */
	public Collection getReachableMethods();

	/**
	 * Returns a collection of methods that are recursion roots in the analyzed system.
	 *
	 * @return a collection of <code>SootMethod</code>.
	 *
	 * @post result->forall(o | o.oclType = SootMethod)
	 */
	public Collection getRecursionRoots();

	/**
	 * Returns a collection of collection of nodes which represent the strongly connected components in the given call graph.
	 *
	 * @return a collection of <code>Collection</code> of <code>SootMethod</code>s.
	 *
	 * @post result->forall(o | o.oclType = Collection(ca.mcgill.sable.soot.SootMethod))
	 */
	public Collection getSCCs();
}

/*****
 ChangeLog:

$Log$

*****/
