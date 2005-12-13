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

package edu.ksu.cis.indus.staticanalyses.flow;

import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;

/**
 * This class manages variants corresponding to entities that have values.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 * @param <E> DOCUMENT ME!
 * @param <SYM> DOCUMENT ME!
 * @param <T> DOCUMENT ME!
 * @param <N> DOCUMENT ME!
 * @param <R> DOCUMENT ME!
 */
class ValuedVariantManager<E, SYM, T extends ITokens<T, SYM>, N extends IFGNode<SYM, T, N>, R>
		extends AbstractVariantManager<ValuedVariant<N>, E, SYM, T, N, R> {

	/**
	 * Creates a new <code>ValuedVariantManager</code> instance.
	 *
	 * @param theAnalysis the instance of the framework in which this object is used.
	 * @param indexManager the manager of indices which map array variants to arrays.
	 * @pre theAnalysis != null and indexManager != null
	 */
	ValuedVariantManager(final FA<SYM, T, N, R> theAnalysis, final IIndexManager<? extends IIndex<?>, E> indexManager) {
		super(theAnalysis, indexManager);
	}

	/**
	 * Returns a new valued variant corresponding to the given ast object.
	 *
	 * @param o the ast object whose variant is to be returned.
	 * @return a new variant corresponding to <code>o</code>.
	 */
	@Override protected ValuedVariant<N> getNewVariant(@SuppressWarnings("unused") final E o) {
		return new ValuedVariant<N>(fa.getNewFGNode());
	}
}

// End of File
