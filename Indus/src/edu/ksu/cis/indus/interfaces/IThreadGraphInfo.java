
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

import edu.ksu.cis.indus.processing.Context;

import java.util.Collection;

import soot.SootMethod;

import soot.jimple.InvokeStmt;


/**
 * This interface provides thread graph information pertaining to the analyzed system. It is adviced that any post processor
 * which provides Thread graph information should provide it via this interface.
 * 
 * <p>
 * Subtypes of this class have to return the constant <code>ID</code> defined in this class as a result of
 * <code>getId</code>.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IThreadGraphInfo
  extends IStatus,
	  IIdentification {
	/** 
	 * The id of this interface.
	 */
	String ID = "Threadgraph Information";

	/**
	 * Returns a collection of thread allocation sites in the system.
	 *
	 * @return a collection of pairs of creation statements and their enclosing methods. The returned pairs consists of a
	 * 		   statement and the method in which it occurs.  However, system threads are represented as pairs of simple
	 * 		   objects. So, the caller is adviced to check for the types of the Pair before using them.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(Pair)
	 */
	Collection getAllocationSites();

	/**
	 * Returns the sites which create new threads, i.e., <code>java.lang.Thread.start()</code> call-sites.
	 *
	 * @return a collection of call-sites which captures the start sites for threads.  The returned pairs consists of a
	 * 		   invocation statement and the method in which it occurs.  However, system threads are represented as pairs of
	 * 		   simple objects. So, the caller is adviced to check for the types of the Pair before using them.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(Pair)
	 */
	Collection getCreationSites();

	/**
	 * Returns the methods executed in the <code>Thread</code> created at the given <code>startStmt</code> in the given
	 * context <code>ctxt</code>.
	 *
	 * @param startStmt is the statement in which the thread is created.
	 * @param ctxt is the context in which <code>ne</code> occurs.
	 *
	 * @return a collection of methods executed in this thread.
	 *
	 * @pre startStmt != null and ctxt != null
	 * @pre ctxt.getCurrentMethod() != null
	 * @post result != null and result.oclIsKindOf(Collection(SootMethod))
	 */
	Collection getExecutedMethods(InvokeStmt startStmt, Context ctxt);

	/**
	 * Returns the threads in which the given method is executed.
	 *
	 * @param sm is the method which is executed.
	 *
	 * @return a collection of thread creation sites (invocation statement and method) along with the class that provided
	 * 		   that body of the thread in which the given method executes is returned.
	 *
	 * @pre sm != null
	 * @post result != null and result.oclIsKindOf(Collection(Triple(InvokeStmt, SootMethod, SootClass)))
	 */
	Collection getExecutionThreads(SootMethod sm);

	/**
	 * Checks if the methods will always occur in the different threads.
	 *
	 * @param methodOne obviously contains the definition.
	 * @param methodTwo obviously contains the use.
	 *
	 * @return <code>true</code> if the given methods will only occur in different threads; <code>false</code>,  otherwise.
	 *
	 * @pre methodOne != null and methodTwo != null
	 */
	boolean mustOccurInDifferentThread(final SootMethod methodOne, final SootMethod methodTwo);

	/**
	 * Checks if the methods will only occur in the same thread.
	 *
	 * @param methodOne obviously contains the definition.
	 * @param methodTwo obviously contains the use.
	 *
	 * @return <code>true</code> if the given methods will only occur in the same thread; <code>false</code>,  otherwise.
	 *
	 * @pre methodOne != null and methodTwo != null
	 */
	boolean mustOccurInSameThread(final SootMethod methodOne, final SootMethod methodTwo);
}

/*
   ChangeLog:
   $Log$
   Revision 1.6  2004/07/11 14:17:41  venku
   - added a new interface for identification purposes (IIdentification)
   - all classes that have an id implement this interface.
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
