
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

package edu.ksu.cis.indus.common.soot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;

import soot.jimple.JimpleBody;

import soot.toolkits.graph.UnitGraph;


/**
 * This class provides <code>ExceptionFlowSensitiveStmtGraph</code>s.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class ExceptionFlowSensitiveStmtGraphFactory
  extends AbstractStmtGraphFactory {
	/**
	 * The collection of exception names that are relevant while dealing with synchronization constructs.
	 */
	public static final Collection SYNC_RELATED_EXCEPTIONS = Collections.singleton("java.lang.Throwable");

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(ExceptionFlowSensitiveStmtGraphFactory.class);

	/**
	 * The names of the exceptions via which the control flow should be ignored.
	 *
	 * @invariant exceptionToIgnore.oclIsKindOf(Collection(String))
	 */
	private Collection exceptionsToIgnore = new ArrayList();

	/**
	 * This flag indicates if the unit graph should be like complete unit graph or like trap unit graph in terms considering
	 * control from the statement before the try block.
	 */
	private final boolean flag;

	/**
	 * Creates a new ExceptionFlowSensitiveStmtGraphFactory object.
	 *
	 * @param namesOfExceptionToIgnore are the names of the exceptions that determine the control edges to be ignored.
	 * @param dontAddEdgeFromStmtBeforeAreaOfProtectionToCatchBlock <code>true</code> indicates if the edge from the unit
	 * 		  before the unit that begins the trap protected region to the handler unit should be omitted;
	 * 		  <code>false</code>, otherwise.
	 *
	 * @pre namesOfExceptionToIgnore != null
	 */
	public ExceptionFlowSensitiveStmtGraphFactory(final Collection namesOfExceptionToIgnore,
		final boolean dontAddEdgeFromStmtBeforeAreaOfProtectionToCatchBlock) {
		flag = dontAddEdgeFromStmtBeforeAreaOfProtectionToCatchBlock;
		exceptionsToIgnore.addAll(namesOfExceptionToIgnore);
	}

	/**
	 * Returns a default factory instance.  This will return a factory object created by  <code>new
	 * ExceptionFlowSensitiveStmtGraphFactory(SYNC_RELATED_EXCEPTIONS, true)</code>
	 *
	 * @return a new factory instance.
	 *
	 * @post result != null
	 */
	public static ExceptionFlowSensitiveStmtGraphFactory getDefaultFactory() {
		return new ExceptionFlowSensitiveStmtGraphFactory(SYNC_RELATED_EXCEPTIONS, true);
	}

	/**
	 * @see edu.ksu.cis.indus.common.soot.AbstractStmtGraphFactory#getStmtGraphForBody(soot.jimple.JimpleBody)
	 */
	protected UnitGraph getStmtGraphForBody(final JimpleBody body) {
		return new ExceptionFlowSensitiveStmtGraph(body, exceptionsToIgnore, flag);
	}

	/**
	 * @see edu.ksu.cis.indus.common.soot.AbstractStmtGraphFactory#getStmtGraphForMethod(soot.SootMethod)
	 */
	protected UnitGraph getStmtGraphForMethod(final SootMethod method) {
		UnitGraph _result = null;

		if (method.isConcrete()) {
			_result = getStmtGraphForBody((JimpleBody) method.retrieveActiveBody());
		} else if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Method " + method + " is not concrete.");
		}
		return _result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.5  2004/05/28 21:41:58  venku
   - added a new method to create default factory instances implementation.

   Revision 1.4  2004/03/29 01:55:16  venku
   - refactoring.
     - history sensitive work list processing is a common pattern.  This
       has been captured in HistoryAwareXXXXWorkBag classes.
   - We rely on views of CFGs to process the body of the method.  Hence, it is
     required to use a particular view CFG consistently.  This requirement resulted
     in a large change.
   - ripple effect of the above changes.
   Revision 1.3  2004/03/26 00:07:26  venku
   - renamed XXXXUnitGraphFactory to XXXXStmtGraphFactory.
   - ripple effect in classes and method names.
   Revision 1.5  2004/03/07 20:30:23  venku
   - documentation.
   Revision 1.4  2004/03/05 11:59:40  venku
   - documentation.
   Revision 1.3  2004/03/04 11:56:48  venku
   - renamed a method.
   - added a valid empty body into native methods.
   Revision 1.2  2004/02/23 08:27:21  venku
   - the graphs were created as complete unit graphs. FIXED.
   Revision 1.1  2004/02/17 05:59:15  venku
   - renamed ExceptionFlowSensitiveStmtGraphXXXX to
     ExceptionFlowSensitiveUnitGraph.
   Revision 1.1  2004/02/17 05:45:34  venku
   - added the logic to create stmt graphs whose structure can be
     tuned to consider the flow of control due to certain exceptions.
 */
