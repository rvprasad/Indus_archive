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

import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;
import edu.ksu.cis.indus.annotations.NonNullContainer;

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
	 * {@inheritDoc}
	 */
	public final void addEdgeFromTo(@NonNull @NonNullContainer final Collection<O> sources, @Immutable final Object label,
			final O dest) {
		final Iterator<O> _i = sources.iterator();
		final int _iEnd = sources.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final O _src = _i.next();
			addEdgeFromTo(_src, label, dest);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final void addEdgeFromTo(final O src, @Immutable final Object label,
			@NonNull @NonNullContainer final Collection<O> destinations) {
		final Iterator<O> _i = destinations.iterator();
		final int _iEnd = destinations.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final O _dest = _i.next();
			addEdgeFromTo(src, label, _dest);
		}
	}
}

// End of File
