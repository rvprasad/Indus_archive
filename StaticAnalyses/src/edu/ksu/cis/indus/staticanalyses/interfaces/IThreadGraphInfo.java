
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

package edu.ksu.cis.indus.staticanalyses.interfaces;

import soot.SootMethod;

import soot.jimple.NewExpr;
import soot.jimple.Stmt;

import edu.ksu.cis.indus.interfaces.IStatus;
import edu.ksu.cis.indus.staticanalyses.Context;
import edu.ksu.cis.indus.staticanalyses.support.Triple;

import java.util.Collection;


/**
 * This interface provides thread graph information pertaining to the analyzed system. It is adviced that any post processor
 * which provides Thread graph information should provide it via this interface.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IThreadGraphInfo extends IStatus {
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
		 * Creates a new NewExprTriple object.  Allocation sites that do not occur in the source code of the system can be
		 * represented via a non-null <code>expr</code> coupled with a null method and statement.
		 *
		 * @param method in which the object is created.
		 * @param stmt in which the allocation occurs.
		 * @param expr is the allocation site.
		 *
		 * @pre expr != null and stmt != null and method != null
		 */
		public NewExprTriple(final SootMethod method, final Stmt stmt, final NewExpr expr) {
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
	 * @post result != null and result.oclIsKindOf(Collection(NewExprTriple))
	 */
	Collection getAllocationSites();

	/**
	 * Returns the methods called executed in the <code>Thread</code> object created by the given <code>ne</code> expression
	 * in the given context <code>ctxt</code>.
	 *
	 * @param ne is the expression in which the thread is created.
	 * @param ctxt is the context in which <code>ne</code> occurs.
	 *
	 * @return a collection of methods executed in this thread.
	 *
	 * @pre ne != null and ctxt != null
	 * @post result != null and result.oclIsKindOf(Collection(SootMethod))
	 */
	Collection getExecutedMethods(NewExpr ne, Context ctxt);

	/**
	 * Returns the threads in which the given method is executed.
	 *
	 * @param sm is the method which is executed.
	 *
	 * @return a collection of allocation sites which capture the creation of the executing thread.
	 *
	 * @pre sm != null
	 * @post result != null and result.oclIsKindOf(Collection(NewExprTriple))
	 */
	Collection getExecutionThreads(SootMethod sm);

	/**
	 * Returns the sites which start new threads, i.e., <code>java.lang.Thread.start()</code> call-sites.
	 *
	 * @return a collection of call-sites which captures the start sites for threads.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(CallTriple))
	 */
	Collection getStartSites();
}

/*
   ChangeLog:

   $Log$
   Revision 1.5  2003/08/13 08:49:10  venku
   Spruced up documentation and specification.
   Tightened preconditions in the interface such that they can be loosed later on in implementaions.

   Revision 1.4  2003/08/13 08:29:40  venku
   Spruced up documentation and specification.

   Revision 1.3  2003/08/12 01:52:00  venku
   Removed redundant final in parameter declaration in methods of interfaces.
   Revision 1.2  2003/08/11 07:46:09  venku
   Finalized the parameters.
   Spruced up Documentation and Specification.

   Revision 1.1  2003/08/07 06:42:16  venku
   Major:
    - Moved the package under indus umbrella.
    - Renamed isEmpty() to hasWork() in WorkBag.

   Revision 1.1  2003/05/22 22:16:45  venku
   All the interfaces were renamed to start with an "I".
 */
