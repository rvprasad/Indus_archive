
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
	 * Checks if there is a class initializing thread in the given collection of threads.
	 *
	 * @param executionThreads to be tested.
	 *
	 * @return <code>true</code> if one of the threads in <code>executionThreads</code> did initialize a class;
	 * 		   <code>false</code>, otherwise.
	 *
	 * @pre executionThreads != null and executionThreads.oclIsKindOf(Collection(Triple))
	 */
	boolean containsClassInitThread(Collection executionThreads);

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
	boolean mustOccurInDifferentThread(SootMethod methodOne, SootMethod methodTwo);

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
	boolean mustOccurInSameThread(SootMethod methodOne, SootMethod methodTwo);
}

// End of File
