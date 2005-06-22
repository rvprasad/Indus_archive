
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

import edu.ksu.cis.indus.common.graph.IEdgeLabelledDirectedGraph.IEdgeLabelledNode;

import java.util.HashSet;

import org.apache.commons.collections.Transformer;


/**
 * This implementation caters edge labelled graphs.  Edges added vai <code>addEdgeFromTo(INode, INode)</code> will be added
 * with a <code>null</code> object based label.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SimpleEdgeGraph
  extends MutableEdgeLabelledDirectedGraph
  implements IMutableEdgeLabelledDirectedGraph,
	  IObjectDirectedGraph {
	/** 
	 * This is a label that represents <code>null</code> object.
	 */
	public static final IEdgeLabel NULL_LABEL = new SimpleLabel(null);

	/** 
	 * This transforms an object to a <code>SimpleEdgeLabelledNode</code>.
	 */
	private static final Transformer OBJECT_TO_NODE_TRANSFORMER =
		new Transformer() {
			public Object transform(final Object input) {
				return new SimpleEdgeLabelledNode(input);
			}
		};

	/**
	 * Creates an instance of this class.
	 */
	public SimpleEdgeGraph() {
		super(new ObjectGraphInfo(OBJECT_TO_NODE_TRANSFORMER));
	}

	/**
	 * This class builds a <code>SimpleNodeGraph</code>.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	public static class SimpleEdgeGraphBuilder
	  extends AbstractEdgeLabelledObjectDirectedGraphBuilder
	  implements IEdgeLabelledObjectDirectedGraphBuilder {
		/**
		 * @see IEdgeLabelledObjectDirectedGraphBuilder#addEdgeFromTo(java.lang.Object, java.lang.Object,
		 * 		java.lang.Object)
		 */
		public void addEdgeFromTo(final Object src, final Object label, final Object dest) {
			final SimpleEdgeGraph _simpleEdgeGraph = (SimpleEdgeGraph) graph;
			_simpleEdgeGraph.addEdgeFromTo(_simpleEdgeGraph.getNode(src), _simpleEdgeGraph.getLabel(label),
				_simpleEdgeGraph.getNode(dest));
		}

		/**
		 * @see edu.ksu.cis.indus.common.graph.IObjectDirectedGraphBuilder#addEdgeFromTo(INode, INode)
		 */
		public void addEdgeFromTo(final Object src, final Object dest) {
			final SimpleEdgeGraph _simpleEdgeGraph = (SimpleEdgeGraph) graph;
			_simpleEdgeGraph.addEdgeFromTo(_simpleEdgeGraph.getNode(src), NULL_LABEL, _simpleEdgeGraph.getNode(dest));
		}

		/**
		 * @see edu.ksu.cis.indus.common.graph.IObjectDirectedGraphBuilder#createGraph()
		 */
		public void createGraph() {
			graph = new SimpleEdgeGraph();
		}

		/**
		 * @see edu.ksu.cis.indus.common.graph.IObjectDirectedGraphBuilder#createNode(java.lang.Object)
		 */
		public void createNode(final Object obj) {
			((SimpleEdgeGraph) graph).getNode(obj);
		}
	}


	/**
	 * This is an object-containing label.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	public static final class SimpleLabel
	  implements IEdgeLabel {
		/** 
		 * The object contained in this label.
		 */
		private final Object object;

		/**
		 * Creates an instance of this class.
		 *
		 * @param obj to be contained in this label.
		 */
		public SimpleLabel(final Object obj) {
			object = obj;
		}

		/**
		 * Retrieves the object contained in this label.
		 *
		 * @return the contained object.
		 */
		public Object getObject() {
			return object;
		}
	}


	/**
	 * This is an implementation of an object node to be used in mutable edge-labelled graph.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	static class SimpleEdgeLabelledNode
	  extends MutableEdgeLabelledNode
	  implements IObjectNode {
		/** 
		 * The object being represetned by this node.
		 */
		private final Object object;

		/**
		 * Creates an instance of this class.
		 *
		 * @param o is the object to be represented by the node.
		 */
		SimpleEdgeLabelledNode(final Object o) {
			super(new HashSet(), new HashSet());
			object = o;
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
	public IEdgeLabelledNode getNode(final Object o) {
		final IEdgeLabelledNode _result = (IEdgeLabelledNode) ((ObjectGraphInfo) graphInfo).getNode(o);
		shapeChanged();
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IObjectDirectedGraph#queryNode(java.lang.Object)
	 */
	public IObjectNode queryNode(final Object o) {
		return ((ObjectGraphInfo) graphInfo).queryNode(o);
	}

	/**
	 * Retrieves the label representing/containing the given object.
	 *
	 * @param obj to serve as the contents of the label.
	 *
	 * @return the label.
	 *
	 * @post result != null
	 */
	protected IEdgeLabel getLabel(final Object obj) {
		return new SimpleLabel(obj);
	}
}

// End of File
