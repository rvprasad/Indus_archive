
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

package edu.ksu.cis.indus.common.graph;

import java.util.Collection;
import java.util.Collections;


/**
 * This class provides abstract implementation and methods to mutate a directed graph.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractMutableDirectedGraph
  extends AbstractDirectedGraph {
	/**
	 * This class extends <code>INode</code> such that the resulting node can mutated.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	public abstract class AbstractMutableNode
	  implements INode {
		/**
		 * The collection of nodes which precede this node in the graph.
		 *
		 * @invariant predecessors != null
		 */
		protected Collection predecessors;

		/**
		 * The collection of nodes which succeed this node in the graph.
		 *
		 * @invariant successors != null
		 */
		protected Collection successors;

		/**
		 * Creates a new AbstractMutableNode object.
		 *
		 * @param preds is the reference to the collection of predecessors.
		 * @param succs is the reference to the collection of successors.
		 *
		 * @pre preds != null and succs != null
		 */
		protected AbstractMutableNode(final Collection preds, final Collection succs) {
			predecessors = preds;
			successors = succs;
		}

		/**
		 * Returns the immediate predecessors of this node.
		 *
		 * @return the immediate predecessors of this node.
		 *
		 * @see edu.ksu.cis.indus.common.graph.INode#getPredsOf()
		 */
		public final Collection getPredsOf() {
			return Collections.unmodifiableCollection(predecessors);
		}

		/**
		 * Returns the nodes immediately reachable from this node by following the edges as indicated by
		 * <code>forward</code>.
		 *
		 * @param forward <code>true</code> indicates following outgoing edges.  <code>false</code> indicates following
		 * 		  incoming edges.
		 *
		 * @return the collection of successor nodes(<code>BasicBlock</code>) of this node.
		 *
		 * @see edu.ksu.cis.indus.common.graph.INode#getSuccsNodesInDirection(boolean)
		 */
		public final Collection getSuccsNodesInDirection(final boolean forward) {
			Collection _result;

			if (forward) {
				_result = getSuccsOf();
			} else {
				_result = getPredsOf();
			}
			return _result;
		}

		/**
		 * Returns the immediate successors of this node.
		 *
		 * @return the immediate successors of this node.
		 *
		 * @see edu.ksu.cis.indus.common.graph.INode#getSuccsOf()
		 */
		public final Collection getSuccsOf() {
			return Collections.unmodifiableCollection(successors);
		}

		/**
		 * Adds a predecessor to this node.
		 *
		 * @param node is the node to be added as the predecessor.
		 *
		 * @post self.getPredsOf()->includes(node)
		 */
		protected final void addPredecessors(final INode node) {
			predecessors.add(node);
		}

		/**
		 * Adds a successor to this node.
		 *
		 * @param node is the node to be added as the successor.
		 *
		 * @post self.getSuccsOf()->includes(node)
		 */
		protected final void addSuccessors(final INode node) {
			successors.add(node);
		}
	}

	/**
	 * Adds a directed edge between the given nodes.  Both the nodes should have been obtained by calling
	 * <code>getNode()</code> on this object.
	 *
	 * @param src is the source of the edge.
	 * @param dest is the destination of the edge.
	 *
	 * @return <code>true</code> if an edge was added; <code>false</code>, otherwise.
	 *
	 * @pre src != null and dest != null
	 * @post src.getSuccsOf()->includes(dest) and dest.getPredsOf()->includes(src)
	 */
	public final boolean addEdgeFromTo(final INode src, final INode dest) {
		boolean _result = false;

		if (containsNodes(src) && containsNodes(dest)) {
			((AbstractMutableNode) src).addSuccessors(dest);
			((AbstractMutableNode) dest).addPredecessors(src);
			tails.remove(src);
			heads.remove(dest);
			_result = true;
		}

		return _result;
	}

	/**
	 * Checks if the given node exists in the graph.
	 *
	 * @param node to be checked for containment.
	 *
	 * @return <code>true</code> if <code>node</code> is contained in this graph; <code>false</code>, otherwise.
	 *
	 * @pre node != null
	 */
	protected abstract boolean containsNodes(final INode node);
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/12/09 04:22:03  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.1  2003/12/08 12:15:48  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.4  2003/12/02 09:42:37  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.3  2003/10/20 00:43:05  venku
    - coding convention.
   Revision 1.2  2003/09/28 03:16:20  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.1  2003/08/24 08:13:11  venku
   Major refactoring.
    - The methods to modify the graphs were exposed.
    - The above anamoly was fixed by supporting a new class AbstractMutableDirectedGraph.
    - Each Mutable graph extends this graph and exposes itself via
      suitable interface to restrict access.
    - Ripple effect of the above changes.
 */
