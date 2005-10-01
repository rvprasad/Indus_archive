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

import org.apache.commons.collections.Transformer;

/**
 * This implementation caters edge labelled graphs. Edges added vai <code>addEdgeFromTo(INode, INode)</code> will be added
 * with a <code>null</code> object based label.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <O> the type of the objects in the nodes in this graph.
 */
public class SimpleEdgeGraph<O>
		extends MutableEdgeLabelledDirectedGraph<SimpleEdgeLabelledNode<O>>
		implements IMutableEdgeLabelledDirectedGraph<SimpleEdgeLabelledNode<O>>,
		IObjectDirectedGraph<SimpleEdgeLabelledNode<O>, O> {

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
	 * This is a label that represents <code>null</code> object.
	 */
	public static final IEdgeLabel NULL_LABEL = new SimpleLabel(null);

	/**
	 * This transforms an object to a <code>SimpleEdgeLabelledNode</code>.
	 */
	private static final Transformer OBJECT_TO_NODE_TRANSFORMER = new Transformer() {

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
	 * Returns a node that represents <code>o</code> in this graph. If no such node exists, then a new node is created.
	 * 
	 * @param o is the object being represented by a node in this graph.
	 * @return the node representing <code>o</code>.
	 * @pre o != null
	 * @post object2nodes$pre.get(o) == null implies inclusion
	 * @post inclusion: nodes->includes(result) and heads->includes(result) and tails->includes(result) and
	 *       object2nodes.get(o) == result
	 * @post result != null
	 */
	public SimpleEdgeLabelledNode<O> getNode(final O o) {
		@SuppressWarnings("unchecked") final ObjectGraphInfo<SimpleEdgeLabelledNode<O>, O> _objectGraphInfo = (ObjectGraphInfo) graphInfo;
		final SimpleEdgeLabelledNode<O> _result = _objectGraphInfo.getNode(o);
		shapeChanged();
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IObjectDirectedGraph#queryNode(java.lang.Object)
	 */
	public SimpleEdgeLabelledNode<O> queryNode(final O o) {
		@SuppressWarnings("unchecked") final ObjectGraphInfo<SimpleEdgeLabelledNode<O>, O> _objectGraphInfo = (ObjectGraphInfo) graphInfo;
		return _objectGraphInfo.queryNode(o);
	}

	/**
	 * Retrieves the label representing/containing the given object.
	 * 
	 * @param obj to serve as the contents of the label.
	 * @return the label.
	 * @post result != null
	 */
	protected IEdgeLabel getLabel(final Object obj) {
		return new SimpleLabel(obj);
	}
}

// End of File
