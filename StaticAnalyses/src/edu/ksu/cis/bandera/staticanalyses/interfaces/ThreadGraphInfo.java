
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

package edu.ksu.cis.bandera.staticanalyses.interfaces;

import ca.mcgill.sable.soot.SootMethod;

import ca.mcgill.sable.soot.jimple.NewExpr;
import ca.mcgill.sable.soot.jimple.Stmt;

import edu.ksu.cis.bandera.staticanalyses.flow.Context;
import edu.ksu.cis.bandera.staticanalyses.support.Triple;

import java.util.Collection;


/**
 * This interface provides thread graph information pertaining to the analyzed system. It is adviced that any post processor
 * which provides Thread graph information should provide it via this interface.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface ThreadGraphInfo {
	/**
	 * The id of this interface.
	 */
	String ID = "Threadgraph Information";

	/**
	 * This class captures in the information pertaining to object allocation.  It provides the expression, statement, and
	 * the method in which the allocation happens.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	public final class NewExprTriple
	  extends Triple {
		/**
		 * Creates a new NewExprTriple object.
		 *
		 * @param method in which the object is created.
		 * @param stmt in which the allocation occurs.
		 * @param expr is the allocation site.
		 */
		public NewExprTriple(SootMethod method, Stmt stmt, NewExpr expr) {
			super(expr, stmt, method);
		}

		/**
		 * Returns the allocation site.
		 *
		 * @return the allocation site.
		 */
		public NewExpr getExpr() {
			return (NewExpr) getFirst();
		}

		/**
		 * Returns the allocating method.
		 *
		 * @return the allocating method.
		 */
		public SootMethod getMethod() {
			return (SootMethod) getThird();
		}

		/**
		 * Returns the allocating statement.
		 *
		 * @return the allocating statement.
		 */
		public Stmt getStmt() {
			return (Stmt) getSecond();
		}
	}

	/**
	 * Returns a collection of thread allocation sites in the system.
	 *
	 * @return a collection of <code>NewExprTriple</code>s.
	 *
	 * @post result->forall(o | o.isOclKindOf(NewExprTriple))
	 */
	Collection getAllocationSites();

	/**
	 * Returns the methods called executed in the <code>Thread</code> object created by the given <code>ne</code> expression
	 * in the given context <code>ctxt</code>.
	 *
	 * @param ne is the expression in which the thread is created.
	 * @param ctxt is the context in which <code>ne</code> occurs.
	 *
	 * @return a collection of <code>SootMethod</code>s executed in this thread.
	 */
	Collection getExecutedMethods(NewExpr ne, Context ctxt);

	/**
	 * Returns the threads in which the given method is executed.
	 *
	 * @param sm is the method which is executed.
	 *
	 * @return a collection of <code>NewExprTriple</code>s which capture the creation of the executing thread.
	 */
	Collection getExecutionThreads(SootMethod sm);

	/**
	 * Returns the sites which start new threads.
	 *
	 * @return a collection of <code>CallTriple</code>s which captures the start sites for threads. 
	 */
	Collection getStartSites();
}

/*****
 ChangeLog:

$Log$

*****/
