
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

import java.util.HashSet;

import org.apache.commons.collections.Transformer;


/**
 * This is a simple concrete implementation of <code>DirectedGraph</code> interface.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class SimpleNodeGraph
  extends MutableDirectedGraph
  implements IObjectDirectedGraph {
	/** 
	 * This transforms an object to a <code>SimpleNode</code>.
	 */
	private static final Transformer OBJECT_TO_NODE_TRANSFORMER =
		new Transformer() {
			public Object transform(final Object input) {
				return new SimpleNode(input);
			}
		};

	/**
	 * Creates an instance of this class.
	 */
	public SimpleNodeGraph() {
		super(new ObjectGraphInfo(OBJECT_TO_NODE_TRANSFORMER));
	}

	/**
	 * This class builds a <code>SimpleNodeGraph</code>.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	public static class SimpleNodeGraphBuilder
	  extends AbstractObjectDirectedGraphBuilder {
		/**
		 * @see edu.ksu.cis.indus.common.graph.IObjectDirectedGraphBuilder#addEdgeFromTo(Object, Object)
		 */
		public void addEdgeFromTo(final Object src, final Object dest) {
			final INode _s = ((SimpleNodeGraph) graph).getNode(src);
			final INode _d = ((SimpleNodeGraph) graph).getNode(dest);
			((SimpleNodeGraph) graph).addEdgeFromTo(_s, _d);
		}

		/**
		 * @see edu.ksu.cis.indus.common.graph.IObjectDirectedGraphBuilder#createGraph()
		 */
		public void createGraph() {
			graph = new SimpleNodeGraph();
		}

		/**
		 * @see edu.ksu.cis.indus.common.graph.IObjectDirectedGraphBuilder#createNode(java.lang.Object)
		 */
		public void createNode(final Object obj) {
			((SimpleNodeGraph) graph).getNode(obj);
		}
	}


	/**
	 * This is a simple concrete implementation of <code>INode</code> interface.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	static class SimpleNode
	  extends MutableDirectedGraph.MutableNode
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
		final INode _result = ((ObjectGraphInfo) graphInfo).getNode(o);
		shapeChanged();
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IObjectDirectedGraph#queryNode(java.lang.Object)
	 */
	public IObjectNode queryNode(final Object o) {
		return ((ObjectGraphInfo) graphInfo).queryNode(o);
	}
}

// End of File
