
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

package edu.ksu.cis.indus.common.graph;

import java.util.Collection;


/**
 * The interface to be implemented by node objects occuring in <code>DirectedGraph</code>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface INode {
	/**
	 * Retrieves the predecessors of this node.
	 *
	 * @return the collection of predecessors of this node.
	 *
	 * @post result->forall(o | o.oclIsKindOf(INode))
	 * @post result->forall(o | o.getSuccsOf()->includes(this))
	 */
	Collection getPredsOf();

	/**
	 * Retrieves the successors of this node.
	 *
	 * @param forward <code>true</code> implies forward direction(successors); <code>false</code> implies backward direction
	 * 		  (predecessors).
	 *
	 * @return the collection of successors of this node.
	 *
	 * @post result->forall(o | o.oclIsKindOf(INode))
	 * @post forward == true implies result->forall(o | o.getPredsOf()->includes(this))
	 * @post forward == false implies result->forall(o | o.getSuccsOf()->includes(this))
	 */
	Collection getSuccsNodesInDirection(boolean forward);

	/**
	 * Retrieves the set of successor nodes of this node.
	 *
	 * @return the collection of successor nodes(<code>INode</code>) of this node.
	 *
	 * @post result->forall(o | o.oclIsKindOf(INode))
	 * @post result->forall(o | o.getPredsOf()->includes(this))
	 */
	Collection getSuccsOf();
}

// End of File
