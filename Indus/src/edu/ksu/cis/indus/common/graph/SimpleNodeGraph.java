
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This is a simple concrete implementation of <code>DirectedGraph</code> interface.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class SimpleNodeGraph
  extends AbstractMutableDirectedGraph
  implements IObjectDirectedGraph {
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
	 * This class builds a <code>SimpleNodeGraph</code>.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	public static class SimpleNodeGraphBuilder
	  extends AbstractGraphBuilder {
		/**
		 * @see edu.ksu.cis.indus.common.graph.IGraphBuilder#createGraph()
		 */
		public void createGraph() {
			graph = new SimpleNodeGraph();
		}

		/**
		 * @see edu.ksu.cis.indus.common.graph.IGraphBuilder#createNode(java.lang.Object)
		 */
		public void createNode(final Object element) {
			((SimpleNodeGraph) graph).getNode(element);
		}

		/**
		 * @see edu.ksu.cis.indus.common.graph.AbstractGraphBuilder#addEdgeFromTo(INode, INode)
		 */
		protected void addEdgeFromTo(final INode src, final INode dest) {
			final INode _s = ((SimpleNodeGraph) graph).getNode(src);
			final INode _d = ((SimpleNodeGraph) graph).getNode(dest);
			((SimpleNodeGraph) graph).addEdgeFromTo(_s, _d);
		}
	}


	/**
	 * This is a simple concrete implementation of <code>INode</code> interface.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	public final class SimpleNode
	  extends AbstractMutableDirectedGraph.AbstractMutableNode
	  implements IObjectNode {
		/** 
		 * The object being represetned by this node.
		 */
		private final Object object;

		/**
		 * Creates a new SimpleNode object.
		 *
		 * @param o is the object to be represented by this node.
		 */
		SimpleNode(final Object o) {
			super(new HashSet(), new HashSet());
			this.object = o;
		}

		/**
		 * Retrieves the associated object.
		 *
		 * @return the associated object.
		 */
		public Object getObject() {
			return object;
		}

		/**
		 * Returns the stringized representation of this object.
		 *
		 * @return stringized representation.
		 *
		 * @post result != null
		 */
		public String toString() {
			return object + "";
		}
	}

	/**
	 * Returns a node that represents <code>o</code> in this graph.  If no such node exists, then a new node is created.
	 *
	 * @param o is the object being represented by a node in this graph.
	 *
	 * @return the node representing <code>o</code>.
	 *
	 * @pre o != null
	 * @post object2nodes$pre.get(o) == null implies inclusion
	 * @post inclusion: nodes->includes(result) and heads->includes(result) and tails->includes(result) and
	 * 		 object2nodes.get(o) == result
	 * @post result != null
	 */
	public INode getNode(final Object o) {
		INode _result = queryNode(o);

		if (_result == null) {
			_result = new SimpleNode(o);
			object2nodes.put(o, _result);
			nodes.add(_result);
			heads.add(_result);
			tails.add(_result);
			hasSpanningForest = false;
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IDirectedGraph#getNodes()
	 */
	public List getNodes() {
		return Collections.unmodifiableList(nodes);
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IObjectDirectedGraph#queryNode(java.lang.Object)
	 */
	public IObjectNode queryNode(final Object o) {
		if (o == null) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("object to be represented cannot be null.");
			}
			throw new NullPointerException("object to be represented cannot be null.");
		}

		final IObjectNode _result = (IObjectNode) object2nodes.get(o);
		return _result;
	}

	/**
	 * @see AbstractMutableDirectedGraph#containsNode(edu.ksu.cis.indus.common.graph.INode)
	 */
	protected boolean containsNode(final INode node) {
		return nodes.contains(node);
	}
}

// End of File
