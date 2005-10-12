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

import edu.ksu.cis.indus.common.collections.ITransformer;

/**
 * This is a simple concrete implementation of <code>DirectedGraph</code> interface.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <O> the type of the object stored in the nodes of this graph.
 */
public class SimpleNodeGraph<O>
		extends MutableDirectedGraph<SimpleNode<O>>
		implements IObjectDirectedGraph<SimpleNode<O>, O> {

	private final ITransformer<SimpleNode<O>, O> objectExtractor;

	/**
	 * Creates an instance of this class.
	 */
	public SimpleNodeGraph() {
		super(new ObjectGraphInfo<SimpleNode<O>, O>(new ITransformer<O, SimpleNode<O>>() {

			public SimpleNode<O> transform(final O input) {
				return new SimpleNode<O>(input);
			}
		}));
		objectExtractor = new ITransformer<SimpleNode<O>, O>() {

			public O transform(final SimpleNode<O> input) {
				return input.getObject();
			}
		};

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
	public SimpleNode<O> getNode(final O o) {
		@SuppressWarnings("unchecked") final ObjectGraphInfo<SimpleNode<O>, O> _objectGraphInfo = (ObjectGraphInfo) graphInfo;
		final SimpleNode<O> _result = _objectGraphInfo.getNode(o);
		shapeChanged();
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IObjectDirectedGraph#getObjectExtractor()
	 */
	public ITransformer<SimpleNode<O>, O> getObjectExtractor() {
		return objectExtractor;
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IObjectDirectedGraph#queryNode(java.lang.Object)
	 */
	public SimpleNode<O> queryNode(final O o) {
		@SuppressWarnings("unchecked") final ObjectGraphInfo<SimpleNode<O>, O> _objectGraphInfo = (ObjectGraphInfo) graphInfo;
		return _objectGraphInfo.queryNode(o);
	}
}

// End of File
