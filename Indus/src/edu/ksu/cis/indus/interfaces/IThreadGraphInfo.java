
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

import edu.ksu.cis.indus.common.structures.Triple;

import edu.ksu.cis.indus.processing.Context;

import java.util.Collection;

import soot.SootMethod;

import soot.jimple.NewExpr;
import soot.jimple.Stmt;


/**
 * This interface provides thread graph information pertaining to the analyzed system. It is adviced that any post processor
 * which provides Thread graph information should provide it via this interface.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IThreadGraphInfo
  extends IStatus {
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
		 * represented via a non-null <code>expr</code> coupled with a null method and statement. It is assumed that none of
		 * the components of this triple will not change their state  in ways that will affect equality test or hash code
		 * value of the component.
		 *
		 * @param method in which the object is created.
		 * @param stmt in which the allocation occurs.
		 * @param expr is the allocation site.
		 *
		 * @pre expr != null and stmt != null and method != null
		 */
		public NewExprTriple(final SootMethod method, final Stmt stmt, final NewExpr expr) {
			super(expr, stmt, method);
			optimize();
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
	 * Returns the thread allocations sites which are executed multiple times.  Sites are considered to be executed multiple
	 * times if they are
	 * 
	 * <ul>
	 * <li>
	 * loop enclosed,
	 * </li>
	 * <li>
	 * reachable from a method with multiple call-sites, and
	 * </li>
	 * <li>
	 * reachable from a method that occurs in a call graph SCC (recursion loop),
	 * </li>
	 * </ul>
	 * 
	 *
	 * @return a collectio of thread allocation sites.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(NewExprTriple))
	 * @post getAllocationSites().containsAll(result)
	 */
	Collection getMultiThreadAllocSites();

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
   Revision 1.3  2003/12/13 02:28:54  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.2  2003/12/09 04:22:03  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.1  2003/12/08 12:20:40  venku
   - moved some classes from staticanalyses interface to indus interface package
   - ripple effect.
   Revision 1.11  2003/12/08 12:15:59  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.10  2003/12/02 09:42:39  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.9  2003/11/06 05:15:07  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.8  2003/09/28 03:08:03  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.7  2003/09/08 02:21:53  venku
   - supports a new method to extract thread allocation sites
     which may be executed multiple times.
   Revision 1.6  2003/08/21 03:32:37  venku
   Incorporated IStatus interface into any interface that provides analysis information.
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
    - Renamed isEmpty() to hasWork() in IWorkBag.
   Revision 1.1  2003/05/22 22:16:45  venku
   All the interfaces were renamed to start with an "I".
 */
