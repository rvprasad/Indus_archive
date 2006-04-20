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

import edu.ksu.cis.indus.annotations.Empty;
import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;
import edu.ksu.cis.indus.annotations.NonNullContainer;

import java.util.Collection;
import java.util.Iterator;

/**
 * this is an abstract implementation of <code>IObjectDirectedGraphBuilder</code>.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <N> the type of the node in the graph build by this builder.
 * @param <O> the type of the object stored in the node in the built graph.
 * @param <G> the type of the object graph builder.
 */
public abstract class AbstractObjectDirectedGraphBuilder<N extends IObjectNode<N, O>, O, G extends IObjectDirectedGraph<N, O>>
		implements IObjectDirectedGraphBuilder<N, O> {

	/**
	 * The graph that is being built.
	 */
	protected G graph;

	/**
	 * {@inheritDoc}
	 */
	public final void addEdgeFromTo(@NonNull @NonNullContainer final Collection<O> preds, @NonNull @Immutable final O node) {
		final Iterator<O> _i = preds.iterator();
		final int _iEnd = preds.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			addEdgeFromTo(_i.next(), node);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final void addEdgeFromTo(@NonNull @Immutable final O node, @NonNull @NonNullContainer final Collection<O> succs) {
		final Iterator<O> _i = succs.iterator();
		final int _iEnd = succs.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			addEdgeFromTo(node, _i.next());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Empty public void finishBuilding() {
		// does nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional public final G getBuiltGraph() {
		return graph;
	}

}

// End of File
