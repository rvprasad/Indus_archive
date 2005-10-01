package edu.ksu.cis.indus.common.graph;

import java.util.Collection;

/**
 * The interface to be implemented by node objects occuring in <code>DirectedGraph</code>.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <T> the sub type of this type.
 */
public interface INode<T extends INode<T>>
		extends IDirectedGraphView.INode {

	/**
	 * Retrieves the predecessors of this node.
	 * 
	 * @return the collection of predecessors of this node.
	 * @post result != null and result->forall(o | o.getSuccsOf()->includes(this))
	 */
	Collection<T> getPredsOf();

	/**
	 * Retrieves the successors of this node.
	 * 
	 * @param forward <code>true</code> implies forward direction(successors); <code>false</code> implies backward
	 *            direction (predecessors).
	 * @return the collection of successors of this node.
	 * @post result != null and
	 * @post forward == true implies result->forall(o | o.getPredsOf()->includes(this))
	 * @post forward == false implies result->forall(o | o.getSuccsOf()->includes(this))
	 */
	Collection<T> getSuccsNodesInDirection(boolean forward);

	/**
	 * Retrieves the set of successor nodes of this node.
	 * 
	 * @return the collection of successor nodes(<code>INode</code>) of this node.
	 * @post result != null and result->forall(o | o.getPredsOf()->includes(this))
	 */
	Collection<T> getSuccsOf();
}
