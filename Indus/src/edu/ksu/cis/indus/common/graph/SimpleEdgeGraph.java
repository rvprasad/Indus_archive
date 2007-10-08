/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

package edu.ksu.cis.indus.common.graph;

import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;
import edu.ksu.cis.indus.common.collections.ITransformer;

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
		@Immutable private final Object object;

		/**
		 * Creates an instance of this class.
		 * 
		 * @param obj to be contained in this label.
		 */
		public SimpleLabel(@Immutable final Object obj) {
			object = obj;
		}

		/**
		 * Retrieves the object contained in this label.
		 * 
		 * @return the contained object.
		 */
		@Functional public Object getObject() {
			return object;
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override public boolean equals(final Object obj) {
			final boolean _result;
			if (obj instanceof SimpleLabel) {
				final SimpleLabel _sl = (SimpleLabel) obj;
				_result = object == _sl.object || (object != null && object.equals(_sl.object));
			} else {
				_result = object == obj || (object != null && object.equals(obj));
			}
			return _result;
		}


		/**
		 * {@inheritDoc}
		 */
		@Override public int hashCode() {
			return object.hashCode();
		}		
	}

	/**
	 * This is a label that represents <code>null</code> object.
	 */
	@Immutable public static final IEdgeLabel NULL_LABEL = new SimpleLabel(null);

	/**
	 * Creates an instance of this class.
	 */
	public SimpleEdgeGraph() {
		super(new ObjectGraphInfo<SimpleEdgeLabelledNode<O>, O>(new ITransformer<O, SimpleEdgeLabelledNode<O>>() {

			public SimpleEdgeLabelledNode<O> transform(final O input) {
				return new SimpleEdgeLabelledNode<O>(input);
			}
		}));
	}

	/**
	 * Returns a node that represents <code>o</code> in this graph. If no such node exists, then a new node is created.
	 * 
	 * @param o is the object being represented by a node in this graph.
	 * @return the node representing <code>o</code>.
	 * @post object2nodes$pre.get(o) == null implies (nodes->includes(result) and heads->includes(result) and
	 *       tails->includes(result) and object2nodes.get(o) == result)
	 */
	@NonNull public SimpleEdgeLabelledNode<O> getNode(@Immutable final O o) {
		@SuppressWarnings("unchecked") final ObjectGraphInfo<SimpleEdgeLabelledNode<O>, O> _objectGraphInfo = (ObjectGraphInfo) graphInfo;
		final SimpleEdgeLabelledNode<O> _result = _objectGraphInfo.getNode(o);
		shapeChanged();
		return _result;
	}

	/**
	 * {@inheritDoc}
	 */
	public SimpleEdgeLabelledNode<O> queryNode(@Immutable final O o) {
		@SuppressWarnings("unchecked") final ObjectGraphInfo<SimpleEdgeLabelledNode<O>, O> _objectGraphInfo = (ObjectGraphInfo) graphInfo;
		return _objectGraphInfo.queryNode(o);
	}

	/**
	 * Retrieves the label representing/containing the given object.
	 * 
	 * @param obj to serve as the contents of the label.
	 * @return the label.
	 */
	@NonNull public IEdgeLabel getLabel(@Immutable final Object obj) {
		return new SimpleLabel(obj);
	}

	/**
	 * {@inheritDoc}
	 */
	@NonNull @Functional public ITransformer<SimpleEdgeLabelledNode<O>, O> getObjectExtractor() {
		return new ITransformer<SimpleEdgeLabelledNode<O>, O>() {

			public O transform(final SimpleEdgeLabelledNode<O> input) {
				return input.getObject();
			}
		};
	}
}

// End of File
