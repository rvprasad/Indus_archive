
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

import java.util.Collection;
import java.util.Iterator;


/**
 * this is an abstract implementation of <code>IGraphBuilder</code>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractGraphBuilder
  implements IGraphBuilder {
	/** 
	 * The graph that is being built.
	 */
	protected IObjectDirectedGraph graph;

	/**
	 * @see edu.ksu.cis.indus.common.graph.IGraphBuilder#getBuiltGraph()
	 */
	public final IObjectDirectedGraph getBuiltGraph() {
		return graph;
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IGraphBuilder#addEdgeFromTo(edu.ksu.cis.indus.common.graph.INode,
	 * 		java.util.Collection)
	 */
	public final void addEdgeFromTo(final INode node, final Collection succs) {
		final Iterator _i = succs.iterator();
		final int _iEnd = succs.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final INode _succ = (INode) _i.next();
			addEdgeFromTo(node, _succ);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IGraphBuilder#addEdgeFromTo(java.util.Collection,
	 * 		edu.ksu.cis.indus.common.graph.INode)
	 */
	public final void addEdgeFromTo(final Collection preds, final INode node) {
		final Iterator _i = preds.iterator();
		final int _iEnd = preds.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final INode _pred = (INode) _i.next();
			addEdgeFromTo(_pred, node);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IGraphBuilder#finishBuilding()
	 */
	public void finishBuilding() {
	}

	/**
	 * Adds an edge from the node representing <code>src</code> the node representing <code>dest</code>.
	 *
	 * @param src node in the originating graph.
	 * @param dest node in the originating graph.
	 */
	protected abstract void addEdgeFromTo(final INode src, final INode dest);
}

// End of File
