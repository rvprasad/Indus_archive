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

import edu.ksu.cis.indus.annotations.Empty;
import edu.ksu.cis.indus.common.datastructures.Pair;

import java.util.Collection;
import java.util.HashSet;

/**
 * This implementation of <code>IDependenceRetriever</code> is used in instance when the dependence information is provided
 * in terms of statements and method pair. Use this in cases such as ready dependence.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <T1> DOCUMENT ME!
 * @param <C1> DOCUMENT ME!
 * @param <E1> DOCUMENT ME!
 * @param <E2> DOCUMENT ME!
 * @param <C2> DOCUMENT ME!
 * @param <T2> DOCUMENT ME!
 */
final class PairRetriever<T1, C1, E1, E2, C2, T2>
		extends AbstractDependenceRetriever<T1, C1, Pair<E1, C1>, E2, C2, Pair<T2, C2>> {

	/**
	 * Creates an instance of this class.
	 */
	@Empty public PairRetriever() {
		// does nothing
	}

	/**
	 * @see IDependenceRetriever#convertToConformantDependees(java.util.Collection, java.lang.Object, java.lang.Object)
	 */
	public Collection<Pair<E2, C2>> convertToConformantDependees(Collection<Pair<T2, C2>> dependents, final E2 base,
			final C2 context) {
		final Collection<Pair<E2, C2>> _result = new HashSet<Pair<E2, C2>>();
		for (final Pair<T2, C2> _pair : dependents) {
			_result.add((Pair<E2, C2>) _pair);
		}
		return _result;
	}

	/**
	 * @see IDependenceRetriever#convertToConformantDependents(java.util.Collection, java.lang.Object, java.lang.Object)
	 */
	public Collection<Pair<T1, C1>> convertToConformantDependents(final Collection<Pair<E1, C1>> dependees, final T1 base,
			final C1 context) {
		final Collection<Pair<T1, C1>> _result = new HashSet<Pair<T1, C1>>();
		for (final Pair<E1, C1> _pair : dependees) {
			_result.add((Pair<T1, C1>) _pair);
		}
		return _result;
	}
}

// End of File
