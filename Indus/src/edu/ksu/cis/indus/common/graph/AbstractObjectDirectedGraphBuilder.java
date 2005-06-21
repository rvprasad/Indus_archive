
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
 * this is an abstract implementation of <code>IObjectDirectedGraphBuilder</code>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractObjectDirectedGraphBuilder
  implements IObjectDirectedGraphBuilder {
	/** 
	 * The graph that is being built.
	 */
	protected IObjectDirectedGraph graph;

	/**
	 * @see edu.ksu.cis.indus.common.graph.IObjectDirectedGraphBuilder#getBuiltGraph()
	 */
	public final IObjectDirectedGraph getBuiltGraph() {
		return graph;
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IObjectDirectedGraphBuilder#addEdgeFromTo(Object,
	 * 		java.util.Collection)
	 */
	public final void addEdgeFromTo(final Object node, final Collection succs) {
		final Iterator _i = succs.iterator();
		final int _iEnd = succs.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			addEdgeFromTo(node, _i.next());
		}
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IObjectDirectedGraphBuilder#addEdgeFromTo(java.util.Collection,
	 * 		Object)
	 */
	public final void addEdgeFromTo(final Collection preds, final Object node) {
		final Iterator _i = preds.iterator();
		final int _iEnd = preds.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			addEdgeFromTo(_i.next(), node);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IObjectDirectedGraphBuilder#finishBuilding()
	 */
	public void finishBuilding() {
	}

}

// End of File
