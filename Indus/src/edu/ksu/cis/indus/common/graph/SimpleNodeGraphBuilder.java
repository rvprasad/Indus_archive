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
	 * @see edu.ksu.cis.indus.common.graph.IObjectDirectedGraphBuilder#addEdgeFromTo(Object, Object)
	 */
	public void addEdgeFromTo(final O src, final O dest) {
		final SimpleNodeGraph<O> _simpleNodeGraph = graph;
		final SimpleNode<O> _s = _simpleNodeGraph.getNode(src);
		final SimpleNode<O> _d = _simpleNodeGraph.getNode(dest);
		_simpleNodeGraph.addEdgeFromTo(_s, _d);
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IObjectDirectedGraphBuilder#createGraph()
	 */
	public void createGraph() {
		graph = new SimpleNodeGraph<O>();
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IObjectDirectedGraphBuilder#createNode(java.lang.Object)
	 */
	public void createNode(final O obj) {
		final SimpleNodeGraph<O> _simpleNodeGraph = graph;
		_simpleNodeGraph.getNode(obj);
	}
}
