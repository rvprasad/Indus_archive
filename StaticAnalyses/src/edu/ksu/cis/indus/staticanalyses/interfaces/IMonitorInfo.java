
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

package edu.ksu.cis.indus.staticanalyses.interfaces;

import edu.ksu.cis.indus.interfaces.IStatus;

import java.util.Collection;


/**
 * This interface provides the information pertaining to Java monitors in the analyzed system.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IMonitorInfo
  extends IStatus {
	/**
	 * The id of this interface.
	 */
	String ID = "Synchronization monitor Information";

	/**
	 * Returns a collection of <code>Triple</code>s of <code>EnterMonitorStmt</code>, <code>ExitMonitorStmt</code>, and
	 * <code>SootMethod</code>. The third element is the method in which the monitor occurs.  In case the first and the
	 * second element of the triple are <code>null</code> then this means the method is a synchronized.
	 *
	 * @return collection of monitors in the analyzed system.
	 *
	 * @post result.oclIsKindOf(Collection(edu.ksu.cis.indus.staticanalyses.support.Triple(soot.jimple.EnterMonitorStmt,
	 * 		 soot.jimple.ExitMonitorStmt, soot.SootMethod)))
	 * @post result->forall(o | o.getThird() ! = null)
	 */
	Collection getMonitorTriples();
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2003/08/21 03:32:37  venku
   Incorporated IStatus interface into any interface that provides analysis information.
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
