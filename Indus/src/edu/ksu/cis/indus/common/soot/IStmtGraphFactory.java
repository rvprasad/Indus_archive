
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

import soot.SootMethod;

import soot.toolkits.graph.UnitGraph;


/**
 * This is the interface via which the user can plugin various sorts of unit graphs into the analyses and also reuse the same
 * implementation at many places.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IStmtGraphFactory {
	/** 
	 * The id of this interface.
	 */
	Object ID = "Statement Graph Factory";

	/**
	 * Retrieves the unit graph of the given method.
	 *
	 * @param method for which the unit graph is requested.
	 *
	 * @return the requested unit graph.
	 *
	 * @post result != null
	 * @post method.isConcrete() implies result != null and result.oclIsKindOf(CompleteUnitGraph)
	 */
	UnitGraph getStmtGraph(final SootMethod method);

	/**
	 * Resets all internal datastructures.
	 */
	void reset();
}

// End of File
