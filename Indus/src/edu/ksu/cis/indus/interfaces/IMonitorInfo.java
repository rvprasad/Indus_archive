
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

import java.util.Collection;

import soot.SootMethod;

import soot.jimple.Stmt;


/**
 * This interface provides the information pertaining to Java monitors in the analyzed system.
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
public interface IMonitorInfo
  extends IStatus,
	  IIdentification {
	/** 
	 * The id of this interface.
	 */
	String ID = "Synchronization monitor Information";

	/**
	 * Retrieves the statements enclosed in the monitor acquired on entering the given synchronized method.
	 *
	 * @param method in which the monitor occurs.
	 * @param transitive <code>true</code> indicates transitive closure is required; <code>false</code>, otherwise.
	 *
	 * @return a collection of statements
	 *
	 * @pre method != null and method.isSychronized();
	 * @post result != null and result.oclIsKindOf(Collection(Stmt))
	 */
	Collection getEnclosedStmts(final SootMethod method, final boolean transitive);

	/**
	 * Retrieves the statements enclosed in the monitor respresented by the given monitor statement in the given method.
	 *
	 * @param monitorStmt is the monitor statement.
	 * @param method in which the monitor occurs.
	 * @param transitive <code>true</code> indicates transitive closure is required; <code>false</code>, otherwise.
	 *
	 * @return a collection of statements
	 *
	 * @pre monitorStmt != null and method != null
	 * @pre monitorStmt.oclIsKindOf(EnterMonitorStmt) or monitorStmt.oclIsKindOf(ExitMonitorStmt)
	 * @post result != null and result.oclIsKindOf(Collection(Stmt))
	 */
	Collection getEnclosedStmts(final Stmt monitorStmt, final SootMethod method, final boolean transitive);

	/**
	 * Retrieves the monitor statements enclosing in the given monitor statement in the given method.
	 *
	 * @param stmt obviously.
	 * @param method in which the monitor occurs.
	 * @param transitive <code>true</code> indicates transitive closure is required; <code>false</code>, otherwise.
	 *
	 * @return a collection of statements
	 *
	 * @pre stmt != null and method != null
	 * @post result != null and result.oclIsKindOf(Collection(Stmt))
	 * @post result->forall(o | o.oclIsKindOf(EnterMonitorStmt) or o.oclIsKindOf(ExitMonitorStmt)
	 */
	Collection getEnclosingMonitors(final Stmt stmt, final SootMethod method, final boolean transitive);

	/**
	 * Returns a collection of <code>Triple</code>s of <code>EnterMonitorStmt</code>, <code>ExitMonitorStmt</code>, and
	 * <code>SootMethod</code> in the system. The third element is the method in which the monitor occurs.  In case the
	 * first  and the second element of the triple are <code>null</code> then this means the method is a synchronized.
	 *
	 * @return collection of monitors in the analyzed system.
	 *
	 * @post result.oclIsKindOf(Collection(edu.ksu.cis.indus.common.graph.Triple(soot.jimple.EnterMonitorStmt,
	 * 		 soot.jimple.ExitMonitorStmt, soot.SootMethod)))
	 * @post result->forall(o | o.getThird() ! = null)
	 */
	Collection getMonitorTriples();

	/**
	 * Returns a collection of <code>Triple</code>s of <code>EnterMonitorStmt</code>, <code>ExitMonitorStmt</code>, and
	 * <code>SootMethod</code> corresponding to monitor represented by <code>monitorStmt</code> in <code>method</code>.
	 *
	 * @param monitorStmt obviously.
	 * @param method in which monitorStmt occurs.
	 *
	 * @return collection of monitors in the analyzed system.
	 *
	 * @pre method != null
	 * @pre monitorStmt.oclIsKindOf(EnterMonitorStmt) or monitorStmt.oclIsKindOf(ExitMonitorStmt)
	 * @post result->forall(o | o.getThird().equals(method))
	 * @post result.oclIsKindOf(Collection(edu.ksu.cis.indus.common.graph.Triple(soot.jimple.EnterMonitorStmt,
	 * 		 soot.jimple.ExitMonitorStmt, soot.SootMethod)))
	 * @post result->forall(o | o.getThird() ! = null)
	 */
	Collection getMonitorTriplesFor(final Stmt monitorStmt, final SootMethod method);

	/**
	 * Returns a collection of <code>Triple</code>s of <code>EnterMonitorStmt</code>, <code>ExitMonitorStmt</code>, and
	 * <code>SootMethod</code> corresponding to the monitors in <code>method</code>. The third element is the method in
	 * which the monitor occurs.  In case the first and the second element of the triple are <code>null</code> then this
	 * means the method is a synchronized.
	 *
	 * @param method in which the monitors occur.
	 *
	 * @return collection of monitors in the analyzed system.
	 *
	 * @pre method != null
	 * @post result->forall(o | o.getThird().equals(method))
	 * @post result.oclIsKindOf(Collection(edu.ksu.cis.indus.common.graph.Triple(soot.jimple.EnterMonitorStmt,
	 * 		 soot.jimple.ExitMonitorStmt, soot.SootMethod)))
	 * @post result->forall(o | o.getThird() ! = null)
	 */
	Collection getMonitorTriplesIn(final SootMethod method);
}

/*
   ChangeLog:
   $Log$
   Revision 1.6  2004/07/22 06:59:26  venku
   - loosened the specification to extract enclosed statements of synced methods.
   Revision 1.5  2004/07/21 02:09:44  venku
   - spruced up IMonitorInfo interface with method to extract more information.
   - updated SynchronizationDA to provide the methods introduced in IMonitorInfo.
   Revision 1.4  2004/07/11 14:17:41  venku
   - added a new interface for identification purposes (IIdentification)
   - all classes that have an id implement this interface.
   Revision 1.3  2003/12/13 02:28:54  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.2  2003/12/09 04:22:03  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.1  2003/12/08 12:20:40  venku
   - moved some classes from staticanalyses interface to indus interface package
   - ripple effect.
   Revision 1.7  2003/12/08 12:15:59  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.6  2003/12/02 09:42:39  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.5  2003/11/06 05:15:07  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.4  2003/09/28 03:08:03  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.3  2003/08/21 03:32:37  venku
   Incorporated IStatus interface into any interface that provides analysis information.
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
