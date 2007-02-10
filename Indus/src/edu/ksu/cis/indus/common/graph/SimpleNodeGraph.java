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
 * This is a simple concrete implementation of <code>DirectedGraph</code> interface.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <O> the type of the object stored in the nodes of this graph.
 */
public class SimpleNodeGraph<O>
		extends MutableDirectedGraph<SimpleNode<O>>
		implements IObjectDirectedGraph<SimpleNode<O>, O> {

	/**
	 * The object extractor that can be used to extract objects stored in the nodes of this graph.
	 */
	@NonNull private final ITransformer<SimpleNode<O>, O> objectExtractor;

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
	 * @post object2nodes$pre.get(o) == null implies inclusion
	 * @post inclusion: nodes->includes(result) and heads->includes(result) and tails->includes(result) and
	 *       object2nodes.get(o) == result
	 */
	@NonNull public SimpleNode<O> getNode(@Immutable final O o) {
		@SuppressWarnings("unchecked") final ObjectGraphInfo<SimpleNode<O>, O> _objectGraphInfo = (ObjectGraphInfo) graphInfo;
		final SimpleNode<O> _result = _objectGraphInfo.getNode(o);
		shapeChanged();
		return _result;
	}

	/**
	 * {@inheritDoc}
	 */
	@NonNull @Functional public ITransformer<SimpleNode<O>, O> getObjectExtractor() {
		return objectExtractor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional public SimpleNode<O> queryNode(@Immutable final O o) {
		@SuppressWarnings("unchecked") final ObjectGraphInfo<SimpleNode<O>, O> _objectGraphInfo = (ObjectGraphInfo) graphInfo;
		return _objectGraphInfo.queryNode(o);
	}
}

// End of File
