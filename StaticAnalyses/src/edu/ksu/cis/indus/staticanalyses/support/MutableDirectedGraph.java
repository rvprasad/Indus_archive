
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (C) 2003, 2004, 2005
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://indus.projects.cis.ksu.edu/).
 * It is understood that any modification not identified as such is
 * not covered by the preceding statement.
 *
 * This work is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this toolkit; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * Java is a trademark of Sun Microsystems, Inc.
 *
 * To submit a bug report, send a comment, or get the latest news on
 * this project and other SAnToS projects, please visit the web-site
 *                http://indus.projects.cis.ksu.edu/
 */

package edu.ksu.cis.indus.staticanalyses.support;

import java.util.Collection;
import java.util.Collections;


/**
 * This class provides abstract implementation and methods to mutate a directed graph.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class MutableDirectedGraph
  extends DirectedGraph {
	/**
	 * This class extends <code>INode</code> such that the resulting node can mutated.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	public abstract class MutableNode
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
		 * Creates a new MutableNode object.
		 *
		 * @param preds is the reference to the collection of predecessors.
		 * @param succs is the reference to the collection of successors.
         * @pre preds != null and succs != null
		 */
		protected MutableNode(final Collection preds, final Collection succs) {
			predecessors = preds;
			successors = succs;
		}

		/**
		 * Returns the immediate predecessors of this node.
		 *
		 * @return the immediate predecessors of this node.
		 *
		 * @see edu.ksu.cis.indus.staticanalyses.support.INode#getPredsOf()
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
		 * @see edu.ksu.cis.indus.staticanalyses.support.INode#getSuccsNodesInDirection(boolean)
		 */
		public final Collection getSuccsNodesInDirection(final boolean forward) {
			if (forward) {
				return getSuccsOf();
			} else {
				return getPredsOf();
			}
		}

		/**
		 * Returns the immediate successors of this node.
		 *
		 * @return the immediate successors of this node.
		 *
		 * @see edu.ksu.cis.indus.staticanalyses.support.INode#getSuccsOf()
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
	public final boolean addEdgeFromTo(final MutableNode src, final MutableNode dest) {
		boolean result = false;

		if (containsNodes(src) && containsNodes(dest)) {
			((MutableNode) src).addSuccessors(dest);
			((MutableNode) dest).addPredecessors(src);
			tails.remove(src);
			heads.remove(dest);
			result = true;
		}

		return result;
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
 */
