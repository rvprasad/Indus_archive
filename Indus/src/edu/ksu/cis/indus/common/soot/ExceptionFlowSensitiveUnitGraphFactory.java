
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

import soot.toolkits.exceptions.ThrowAnalysis;

import soot.toolkits.graph.UnitGraph;


/**
 * This class provides <code>ExceptionFlowSensitiveUnitGraph</code>s.
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
	 * The names of the exceptions via which the control flow should be ignored.
	 *
	 * @invariant exceptionToIgnore.oclIsKindOf(Collection(String))
	 */
	private Collection exceptionsToIgnore = new ArrayList();

	/**
	 * The <code>ThrowAnalysis</code> to be used during CFG construction.  It is only used by the super class constructor.
	 * Please refer to documentation of <code>soot.toolkits.graph.ExceptionalUnitGraph</code> for more info.
	 */
	private final ThrowAnalysis throwAnalysis;

	/**
	 * Creates a new ExceptionFlowSensitiveUnitGraphFactory object.
	 *
	 * @param namesOfExceptionToIgnore are the names of the exceptions that determine the control edges to be ignored.
	 * @param analysis is the throw analysis to use during CFG construction.  If this is <code>null</code> then the throw
	 * 		  analysis as mandated by Soot is used.   Please refer to documentation of
	 * 		  <code>soot.toolkits.graph.ExceptionalUnitGraph</code> for more info.
	 *
	 * @pre namesOfExceptionToIgnore != null
	 */
	public ExceptionFlowSensitiveUnitGraphFactory(final Collection namesOfExceptionToIgnore, final ThrowAnalysis analysis) {
		exceptionsToIgnore.addAll(namesOfExceptionToIgnore);
		throwAnalysis = analysis;
	}

	/**
	 * @see edu.ksu.cis.indus.common.soot.AbstractUnitGraphFactory#getUnitGraphForBody(soot.Body)
	 */
	protected UnitGraph getUnitGraphForBody(final Body body) {
		return new ExceptionFlowSensitiveUnitGraph(body, exceptionsToIgnore);
	}

	/**
	 * @see edu.ksu.cis.indus.common.soot.AbstractUnitGraphFactory#getUnitGraphForMethod(soot.SootMethod)
	 */
	protected UnitGraph getUnitGraphForMethod(final SootMethod method) {
		UnitGraph _result = null;

		if (method.isConcrete()) {
			if (throwAnalysis == null) {
				_result = new ExceptionFlowSensitiveUnitGraph(method.retrieveActiveBody(), exceptionsToIgnore);
			} else {
				_result = new ExceptionFlowSensitiveUnitGraph(method.retrieveActiveBody(), exceptionsToIgnore, throwAnalysis);
			}
		} else if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Method " + method + " is not concrete.");
		}
		return _result;
	}
}

/*
   ChangeLog:
   $Log$
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
