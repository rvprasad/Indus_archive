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

package edu.ksu.cis.indus.staticanalyses.dependency;

import edu.ksu.cis.indus.common.datastructures.Pair;

import java.util.Collection;

/**
 * This implementation of <code>IDependenceRetriever</code> is used in instance when the dependence information is provided
 * in terms of statements and method pair. Use this in cases such as ready dependence.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <T1> DOCUMENT ME!
 * @param <C1> DOCUMENT ME!
 * @param <E2> DOCUMENT ME!
 * @param <C2> DOCUMENT ME!
 */
final class PairRetriever<T1, C1, E2, C2>
		implements IDependenceRetriever<T1, C1, Pair<T1, C1>, E2, C2, Pair<E2, C2>> {

	/**
	 * @see IDependenceRetriever#getDependees(IDependencyAnalysis, Object, Object)
	 */
	public Collection<Pair<T1, C1>> getDependees(final IDependencyAnalysis<T1, C1, Pair<T1, C1>, E2, C2, Pair<E2, C2>> da,
			final Pair<T1, C1> dependence, @SuppressWarnings("unused") final C1 origContext) {
		return da.getDependees(dependence.getFirst(), dependence.getSecond());
	}

	/**
	 * @see IDependenceRetriever#getDependents(IDependencyAnalysis, Object, Object)
	 */
	public Collection<Pair<E2, C2>> getDependents(final IDependencyAnalysis<T1, C1, Pair<T1, C1>, E2, C2, Pair<E2, C2>> da,
			final Pair<E2, C2> dependence, @SuppressWarnings("unused") final C2 origContext) {
		return da.getDependents(dependence.getFirst(), dependence.getSecond());
	}
}

// End of File
