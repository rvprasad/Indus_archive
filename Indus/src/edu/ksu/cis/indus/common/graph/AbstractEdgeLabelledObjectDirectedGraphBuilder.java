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
 * This is an abstract implementation of <code>IEdgeLabelledObjectDirectedGraphBuilder</code>.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <N> the type of nodes in the built graph.
 * @param <O> the type of objects stored in the build graph.
 * @param <G> the type of the build graph.
 */
public abstract class AbstractEdgeLabelledObjectDirectedGraphBuilder<N extends IObjectNode<N, O>, O, G extends IObjectDirectedGraph<N, O>>
		extends AbstractObjectDirectedGraphBuilder<N, O, G>
		implements IEdgeLabelledObjectDirectedGraphBuilder<O> {

	/**
	 * Creates an instance of this class.
	 */
	public AbstractEdgeLabelledObjectDirectedGraphBuilder() {
		super();
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IEdgeLabelledObjectDirectedGraphBuilder#addEdgeFromTo(java.util.Collection,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void addEdgeFromTo(final Collection<O> sources, final Object label, final O dest) {
		final Iterator<O> _i = sources.iterator();
		final int _iEnd = sources.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final O _src = _i.next();
			addEdgeFromTo(_src, label, dest);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IEdgeLabelledObjectDirectedGraphBuilder#addEdgeFromTo(java.lang.Object,
	 *      java.lang.Object, java.util.Collection)
	 */
	public void addEdgeFromTo(final O src, final Object label, final Collection<O> destinations) {
		final Iterator<O> _i = destinations.iterator();
		final int _iEnd = destinations.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final O _dest = _i.next();
			addEdgeFromTo(src, label, _dest);
		}
	}
}

// End of File
