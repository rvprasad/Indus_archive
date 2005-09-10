
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

package edu.ksu.cis.indus.common.soot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootMethod;

import soot.jimple.JimpleBody;

import soot.toolkits.graph.CompleteUnitGraph;
import soot.toolkits.graph.UnitGraph;


/**
 * This class provides <code>soot.toolkits.graph.CompleteUnitGraph</code>s.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class CompleteStmtGraphFactory
  extends AbstractStmtGraphFactory {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(CompleteStmtGraphFactory.class);

	/**
	 * @see edu.ksu.cis.indus.common.soot.AbstractStmtGraphFactory#getStmtGraphForBody(soot.jimple.JimpleBody)
	 */
	protected UnitGraph getStmtGraphForBody(final JimpleBody body) {
		return new CompleteUnitGraph(body);
	}

	/**
	 * {@inheritDoc}
	 */
	protected UnitGraph getStmtGraphForMethod(final SootMethod method) {
		UnitGraph _result = null;

		if (method.isConcrete()) {
			_result = new CompleteUnitGraph(method.retrieveActiveBody());
		} else if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Method " + method + " is not concrete.");
		}
		return _result;
	}
}

// End of File
