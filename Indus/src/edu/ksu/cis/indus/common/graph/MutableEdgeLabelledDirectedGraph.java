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

import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;

import java.util.Collection;
import java.util.Iterator;

/**
 * This is abstract implementation of mutable edge labelled directed graphs.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <N> the type of nodes in this graph.
 */
public class MutableEdgeLabelledDirectedGraph<N extends MutableEdgeLabelledNode<N>>
		extends MutableDirectedGraph<N>
		implements IMutableEdgeLabelledDirectedGraph<N> {

	/**
	 * Creates an instance of this class.
	 * 
	 * @param info maintains the information of this graph.
	 */
	protected MutableEdgeLabelledDirectedGraph(@NonNull @Immutable final GraphInfo<N> info) {
		super(info);
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean addEdgeFromTo(@NonNull final N src, @NonNull final IEdgeLabel label, @NonNull final N dest) {
		final boolean _result = super.addEdgeFromTo(src, dest);

		if (_result) {
			src.addOutgoingEdgeLabelledTo(label, dest);
			dest.addIncomingEdgeLabelledFrom(label, src);
		}
		return _result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public final boolean addEdgeFromTo(@NonNull final N src, @NonNull final N dest) {
		return addEdgeFromTo(src, IEdgeLabelledDirectedGraph.DUMMY_LABEL, dest);
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean removeAllEdgesLabelled(@NonNull @Immutable final IEdgeLabel label) {
		boolean _result = true;
		final Iterator<N> _i = getNodes().iterator();
		final int _iEnd = getNodes().size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final N _node = _i.next();
			_result &= _node.removeOutgoingEdgesLabelled(label);
			_result &= _node.removeIncomingEdgesLabelled(label);
		}
		return _result;
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean removeAllEdgesLabelledFrom(@NonNull @Immutable final IEdgeLabel label, @NonNull final N node) {
		final Collection<N> _dests = node.getSuccsViaEdgesLabelled(label);
		final Iterator<N> _i = _dests.iterator();
		final int _iEnd = _dests.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final N _dest = _i.next();
			_dest.removeIncomingEdgeLabelledFrom(label, node);
		}
		return node.removeOutgoingEdgesLabelled(label);
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean removeAllEdgesLabelledTo(@NonNull @Immutable final IEdgeLabel label, @NonNull final N node) {
		final Collection<N> _srcs = node.getPredsViaEdgesLabelled(label);
		final Iterator<N> _i = _srcs.iterator();
		final int _iEnd = _srcs.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final N _src = _i.next();
			_src.removeOutgoingEdgeLabelledTo(label, node);
		}
		return node.removeIncomingEdgesLabelled(label);
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean removeEdgeFromTo(@NonNull @Immutable final N src, @NonNull @Immutable final IEdgeLabel label,
			@Immutable @NonNull final N dest) {
		return src.removeOutgoingEdgeLabelledTo(label, dest) && dest.removeIncomingEdgeLabelledFrom(label, src);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public final boolean removeEdgeFromTo(@Immutable @NonNull final N src, @Immutable @NonNull final N dest) {
		return removeEdgeFromTo(src, IEdgeLabelledDirectedGraph.DUMMY_LABEL, dest);
	}
}

// End of File
