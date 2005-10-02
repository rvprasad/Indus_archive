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

import soot.jimple.JimpleBody;

import soot.toolkits.graph.TrapUnitGraph;

/**
 * This class provides <code>soot.toolkits.graph.TrapUnitGraph</code>s.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class TrapStmtGraphFactory
		extends AbstractStmtGraphFactory<TrapUnitGraph> {

	/**
	 * @see edu.ksu.cis.indus.common.soot.AbstractStmtGraphFactory#getStmtGraphForBody(soot.jimple.JimpleBody)
	 */
	@Override protected TrapUnitGraph getStmtGraphForBody(final JimpleBody body) {
		return new TrapUnitGraph(body);
	}
}

// End of File
