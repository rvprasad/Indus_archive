
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(SimpleNodeGraph.class);

	/**
	 * The sequence of nodes in this graph.  They are stored in the order that the nodes are created.
	 *
	 * @invariant nodes.oclIsTypeOf(Sequence(SimpleNode))
	 */
	private List nodes = new ArrayList();

	/**
	 * This maps objects to their representative nodes.
	 *
	 * @invariant object2nodes.oclIsTypeOf(Map(Object, SimpleNode))
	 */
	private Map object2nodes = new HashMap();

	/**
	 * This is a simple concrete implementation of <code>INode</code> interface.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	public static class SimpleNode
	  implements INode {
		/**
		 * The object being represetned by this node.
		 */
		public final Object _object;

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
		 * @param o is the object to be represented by this node.
		 */
		SimpleNode(final Object o) {
			this._object = o;
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
		public final void addPredecessors(final INode node) {
			predecessors.add(node);
		}

		/**
		 * Adds a successor to this node.
		 *
		 * @param node is the node to be added as the successor.
		 *
		 * @post self.getSuccsOf()->includes(node)
		 */
		public final void addSuccessors(final INode node) {
			successors.add(node);
		}
	}

	/**
	 * Returns a node that represents <code>o</code> in this graph.  If no such node exists, then a new node is created.
	 *
	 * @param o is the object being represented by a node in this graph.
	 *
	 * @return the node representing <code>o</code>.
	 *
	 * @throws NullPointerException when the given object is <code>null</code>.
	 *
	 * @pre o != null
	 * @post object2nodes$pre.get(o) == null implies inclusion
	 * @post inclusion: nodes->includes(result) and heads->includes(result) and tails->includes(result) and
	 * 		 object2nodes.get(o) == result
	 * @post result != null
	 */
	public INode getNode(final Object o) {
		if (o == null) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("object to be represented cannot be null.");
			}
			throw new NullPointerException("object to be represented cannot be null.");
		}

		INode result = (INode) object2nodes.get(o);

		if (result == null) {
			result = new SimpleNode(o);
			object2nodes.put(o, result);
			nodes.add(result);
			heads.add(result);
			tails.add(result);
		}
		return result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.support.DirectedGraph#getNodes()
	 */
	public List getNodes() {
		return Collections.unmodifiableList(nodes);
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
	public boolean addEdgeFromTo(final INode src, final INode dest) {
		boolean result = false;

		if (nodes.contains(src) && nodes.contains(dest)) {
			((SimpleNode) src).addSuccessors(dest);
			((SimpleNode) dest).addPredecessors(src);
			tails.remove(src);
			heads.remove(dest);
			result = true;
		}

		return result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.support.DirectedGraph#size()
	 */
	public int size() {
		return nodes.size();
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2003/08/11 04:20:19  venku
   - Pair and Triple were changed to work in optimized and unoptimized mode.
   - Ripple effect of the previous change.
   - Documentation and specification of other classes.
   Revision 1.1  2003/08/07 06:42:16  venku
   Major:
    - Moved the package under indus umbrella.
    - Renamed isEmpty() to hasWork() in WorkBag.
   Revision 1.5  2003/05/22 22:18:31  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
