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

import edu.ksu.cis.indus.annotations.Empty;
import edu.ksu.cis.indus.annotations.Immutable;

/**
 * This class builds a <code>SimpleNodeGraph</code>.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <O> the type of the object stored in the nodes in the built graph.
 */
public class SimpleNodeGraphBuilder<O>
		extends AbstractObjectDirectedGraphBuilder<SimpleNode<O>, O, SimpleNodeGraph<O>> {

	/**
	 * Creates an instance of this class.
	 */
	@Empty public SimpleNodeGraphBuilder() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	public void addEdgeFromTo(@Immutable final O src, @Immutable final O dest) {
		final SimpleNodeGraph<O> _simpleNodeGraph = graph;
		final SimpleNode<O> _s = _simpleNodeGraph.getNode(src);
		final SimpleNode<O> _d = _simpleNodeGraph.getNode(dest);
		_simpleNodeGraph.addEdgeFromTo(_s, _d);
	}

	/**
	 * {@inheritDoc}
	 */
	public void createGraph() {
		graph = new SimpleNodeGraph<O>();
	}

	/**
	 * {@inheritDoc}
	 */
	public void createNode(@Immutable final O obj) {
		final SimpleNodeGraph<O> _simpleNodeGraph = graph;
		_simpleNodeGraph.getNode(obj);
	}
}
