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
 */
public class SimpleEdgeGraphBuilder<O>
		extends AbstractEdgeLabelledObjectDirectedGraphBuilder<SimpleEdgeLabelledNode<O>, O, SimpleEdgeGraph<O>>
		implements IEdgeLabelledObjectDirectedGraphBuilder<O> {

	/**
	 * @see edu.ksu.cis.indus.common.graph.IObjectDirectedGraphBuilder#addEdgeFromTo(Object, Object)
	 */
	public void addEdgeFromTo(final O src, final O dest) {
		final SimpleEdgeGraph<O> _simpleEdgeGraph = graph;
		_simpleEdgeGraph.addEdgeFromTo(_simpleEdgeGraph.getNode(src), SimpleEdgeGraph.NULL_LABEL, _simpleEdgeGraph
				.getNode(dest));
	}

	/**
	 * @see IEdgeLabelledObjectDirectedGraphBuilder#addEdgeFromTo(java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	public void addEdgeFromTo(final O src, final Object label, final O dest) {
		final SimpleEdgeGraph<O> _simpleEdgeGraph = graph;
		_simpleEdgeGraph.addEdgeFromTo(_simpleEdgeGraph.getNode(src), _simpleEdgeGraph.getLabel(label), _simpleEdgeGraph
				.getNode(dest));
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IObjectDirectedGraphBuilder#createGraph()
	 */
	public void createGraph() {
		graph = new SimpleEdgeGraph<O>();
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IObjectDirectedGraphBuilder#createNode(java.lang.Object)
	 */
	public void createNode(final O obj) {
		graph.getNode(obj);
	}
}
