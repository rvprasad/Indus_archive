
/*
 * Bandera, a Java(TM) analysis and transformation toolkit
 * Copyright (C) 2002, 2003, 2004.
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://www.cis.ksu.edu/santos/bandera).
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
 *                http://www.cis.ksu.edu/santos/bandera
 */

package edu.ksu.cis.bandera.staticanalyses.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * This is a simple concrete implementation of <code>DirectedGraph</code> interface.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class SimpleNodeGraph
  extends DirectedGraph {
	/**
	 * The collection of nodes in this graph.
	 */
	private List nodes = new ArrayList();

	/**
	 * This maps objects to their representative nodes.
	 */
	private Map object2nodes = new HashMap();

	/**
	 * This is a simple concrete implementation of <code>Node</code> interface.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	public static class SimpleNode
	  implements Node {
		/**
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		public final Object object;

		/**
		 * The collection of nodes which precede this node in the graph.
		 */
		private final Set predecessors = new HashSet();

		/**
		 * The collection of nodes which succeed this node in the graph.
		 */
		private final Set successors = new HashSet();

		/**
		 * Creates a new SimpleNode object.
		 *
		 * @param o DOCUMENT ME!
		 */
		SimpleNode(Object o) {
			this.object = o;
		}

		/**
		 * Returns the predecessor nodes of this node.
		 *
		 * @return the collection of predecessor nodes of this node.
		 *
		 * @post result->forall(o | o.oclIsKindOf(Node))
		 *
		 * @see edu.ksu.cis.bandera.staticanalyses.support.Node#getPredsOf()
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
		 * @see edu.ksu.cis.bandera.staticanalyses.support.Node#getSuccsNodesInDirection(boolean)
		 */
		public final Collection getSuccsNodesInDirection(boolean forward) {
			if(forward) {
				return getSuccsOf();
			} else {
				return getPredsOf();
			}
		}

		/**
		 * Returns the successors of this node.
		 *
		 * @return the collection of successor nodes of this node.
		 *
		 * @post result->forall(o | o.oclIsKindOf(Node))
		 *
		 * @see edu.ksu.cis.bandera.staticanalyses.support.Node#getSuccsOf()
		 */
		public final Collection getSuccsOf() {
			return Collections.unmodifiableCollection(successors);
		}

		/**
		 * Adds a predecessor to this node.
		 *
		 * @param node is the node to be added as the predecessor.
		 */
		public final void addPredecessors(Node node) {
			predecessors.add(node);
		}

		/**
		 * Adds a successor to this node.
		 *
		 * @param node is the node to be added as the successor.
		 */
		public final void addSuccessors(Node node) {
			successors.add(node);
		}
	}

	/**
	 * Returns a node that represents <code>o</code> in this graph.
	 *
	 * @param o is the object to be represented as a node in this graph.
	 *
	 * @return the node representing <code>o</code>.
	 *
	 * @post object2nodes\
	 * @pre.get(o) = null implies object2nodes.get(o) = result
	 */
	public Node getNode(Object o) {
		Node result = (Node) object2nodes.get(o);

		if(result == null) {
			result = new SimpleNode(o);
			object2nodes.put(o, result);
			nodes.add(result);
			heads.add(result);
			tails.add(result);
		}
		return result;
	}

	/**
	 * Returns the nodes in this graph.
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.support.DirectedGraph#getNodes()
	 */
	public List getNodes() {
		return Collections.unmodifiableList(nodes);
	}

	/**
	 * Adds an directed edge between the given nodes.  Both the nodes should have been obtained by calling
	 * <code>getNode()</code> on this object.
	 *
	 * @param src is the source of the edge.
	 * @param dest is the destination of the edge.
	 *
	 * @return <code>true</code> if an edge was added; <code>false</code>, otherwise.
	 */
	public boolean addEdgeFromTo(Node src, Node dest) {
		boolean result = false;

		if(nodes.contains(src) && nodes.contains(dest)) {
			((SimpleNode) src).addSuccessors(dest);
			((SimpleNode) dest).addPredecessors(src);
			tails.remove(src);
			heads.remove(dest);
			result = true;
		}

		return result;
	}

	/**
	 * @see edu.ksu.cis.bandera.staticanalyses.support.DirectedGraph#size()
	 */
	public int size() {
		return nodes.size();
	}
}

/*****
 ChangeLog:

$Log$

*****/
