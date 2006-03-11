package edu.ksu.cis.indus.staticanalyses.dependency;

import java.util.Collection;

/**
 * DOCUMENT ME!
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <T1> DOCUMENT ME!
 * @param <C1> DOCUMENT ME!
 * @param <E1> DOCUMENT ME!
 * @param <E2> DOCUMENT ME!
 * @param <C2> DOCUMENT ME!
 * @param <T2> DOCUMENT ME!
 */
public abstract class AbstractDependenceRetriever<T1, C1, E1, E2, C2, T2>
		implements IDependenceRetriever<T1, C1, E1, E2, C2, T2> {

	/**
	 * {@inheritDoc}
	 * 
	 * @see IDependenceRetriever#getDependees(edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis,
	 *      java.lang.Object, java.lang.Object)
	 */
	public Collection<E1> getDependees(final IDependencyAnalysis<T1, C1, E1, E2, C2, T2> da, final T1 base, final C1 context) {
		return da.getDependees(base, context);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IDependenceRetriever#getDependents(edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis,
	 *      java.lang.Object, java.lang.Object)
	 */
	public Collection<T2> getDependents(final IDependencyAnalysis<T1, C1, E1, E2, C2, T2> da, final E2 base, final C2 context) {
		return da.getDependents(base, context);
	}
}

// End of File
