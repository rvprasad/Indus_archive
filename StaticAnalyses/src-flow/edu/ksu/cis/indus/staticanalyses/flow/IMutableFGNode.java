
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

package edu.ksu.cis.indus.staticanalyses.flow;

import edu.ksu.cis.indus.common.graph.SCCRelatedData;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;

import java.util.Collection;


/**
 * This interface enables modification of the internal data structures of the flow graph nodes.
 * 
 * <p>
 * Although, this interface is public, it are not intended for public consumption.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IMutableFGNode
  extends IFGNode {
	/**
	 * Sets the given data as the SCC related data of this node.
	 *
	 * @param data to be used.
	 *
	 * @pre data != null
	 */
	void setSCCRelatedData(SCCRelatedData data);

	/**
	 * Retrieves SCC related data of this node.
	 *
	 * @return SCC related data.
	 *
	 * @post result != null
	 */
	SCCRelatedData getSCCRelatedData();

	/**
	 * Sets the successor collection to be used to store successors.
	 *
	 * @param successors the new collection.
	 *
	 * @pre successors != null and successors.oclIsKindOf(Collection(IFGNode))
	 */
	void setSuccessorSet(final Collection successors);

	/**
	 * Sets the token set to be used.
	 *
	 * @param newTokenSet to be used.
	 *
	 * @pre newTokenSet != null
	 */
	void setTokenSet(final ITokens newTokenSet);
}

// End of File
