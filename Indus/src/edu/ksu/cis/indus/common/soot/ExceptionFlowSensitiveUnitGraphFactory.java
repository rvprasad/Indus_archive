
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.Body;
import soot.SootMethod;

import soot.toolkits.graph.UnitGraph;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class ExceptionFlowSensitiveUnitGraphFactory
  extends AbstractUnitGraphFactory {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(ExceptionFlowSensitiveUnitGraphFactory.class);

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private Collection exceptionsToIgnore = new ArrayList();

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final boolean flag;

	/**
	 * Creates a new ExceptionFlowSensitiveUnitGraphFactory object.
	 *
	 * @param namesOfExceptionToIgnore DOCUMENT ME!
	 * @param dontAddEdgeFromStmtBeforeAreaOfProtectionToCatchBlock <code>true</code> indicates if the edge from the unit
	 * 		  before the unit that begins the trap protected region to the handler unit should be omitted;
	 * 		  <code>false</code>, otherwise.
	 *
	 * @pre namesOfExceptionToIgnore != null
	 */
	public ExceptionFlowSensitiveUnitGraphFactory(final Collection namesOfExceptionToIgnore,
		final boolean dontAddEdgeFromStmtBeforeAreaOfProtectionToCatchBlock) {
		flag = dontAddEdgeFromStmtBeforeAreaOfProtectionToCatchBlock;
		exceptionsToIgnore.addAll(namesOfExceptionToIgnore);
	}

	/**
	 * Creates a new ExceptionFlowSensitiveUnitGraphFactory object.
	 */
	public ExceptionFlowSensitiveUnitGraphFactory() {
		flag = true;
		exceptionsToIgnore.add("java.lang.Throwable");
	}

	/**
	 * @see edu.ksu.cis.indus.common.soot.AbstractUnitGraphFactory#getMethodForBody(soot.Body)
	 */
	protected UnitGraph getMethodForBody(final Body body) {
		return new ExceptionFlowSensitiveUnitGraph(body, exceptionsToIgnore, flag);
	}

	/**
	 * @see edu.ksu.cis.indus.common.soot.AbstractUnitGraphFactory#getUnitGraphForMethod(soot.SootMethod)
	 */
	protected UnitGraph getUnitGraphForMethod(final SootMethod method) {
		UnitGraph _result = null;

		if (method.isConcrete()) {
			_result = new ExceptionFlowSensitiveUnitGraph(method.retrieveActiveBody(), exceptionsToIgnore, flag);
		} else if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Method " + method + " is not concrete.");
		}
		return _result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2004/02/17 05:59:15  venku
   - renamed ExceptionFlowSensitiveStmtGraphXXXX to
     ExceptionFlowSensitiveUnitGraph.
   Revision 1.1  2004/02/17 05:45:34  venku
   - added the logic to create stmt graphs whose structure can be
     tuned to consider the flow of control due to certain exceptions.
 */
