
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
 * in terms of statements and method pair.  Use this in cases such as ready dependence.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
final class PairRetriever
  implements IDependenceRetriever {
	/**
	 * @see IDependenceRetriever#getDependees(IDependencyAnalysis, Object, Object)
	 */
	public Collection getDependees(final IDependencyAnalysis da, final Object dependence, final Object origContext) {
		final Pair _pair = (Pair) dependence;
		return da.getDependees(_pair.getFirst(), _pair.getSecond());
	}

	/**
	 * @see IDependenceRetriever#getDependents(IDependencyAnalysis, Object, Object)
	 */
	public Collection getDependents(final IDependencyAnalysis da, final Object dependence, final Object origContext) {
		final Pair _pair = (Pair) dependence;
		return da.getDependents(_pair.getFirst(), _pair.getSecond());
	}
}

// End of File
