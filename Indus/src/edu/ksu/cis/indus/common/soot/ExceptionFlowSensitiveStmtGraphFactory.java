
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
 * This class provides <code>ExceptionFlowSensitiveUnitGraph</code>s.
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
	 * Creates a new ExceptionFlowSensitiveStmtGraphFactory object.
	 */
	public ExceptionFlowSensitiveStmtGraphFactory() {
		flag = true;
		exceptionsToIgnore.add("java.lang.Throwable");
	}

	/**
	 * @see edu.ksu.cis.indus.common.soot.AbstractStmtGraphFactory#getUnitGraphForBody(soot.jimple.JimpleBody)
	 */
	protected UnitGraph getUnitGraphForBody(final JimpleBody body) {
		return new ExceptionFlowSensitiveStmtGraph(body, exceptionsToIgnore, flag);
	}

	/**
	 * @see edu.ksu.cis.indus.common.soot.AbstractStmtGraphFactory#getUnitGraphForMethod(soot.SootMethod)
	 */
	protected UnitGraph getUnitGraphForMethod(final SootMethod method) {
		UnitGraph _result = null;

		if (method.isConcrete()) {
			_result = getUnitGraphForBody((JimpleBody) method.retrieveActiveBody());
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
