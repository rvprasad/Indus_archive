
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

import edu.ksu.cis.indus.common.datastructures.HistoryAwareLIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.soot.Constants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


/**
 * This class provides dependence information closure.  In other words, given a dependence analysis, it provides the indirect
 * version of it.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
final class IndirectDependenceAnalysis
  implements IDependencyAnalysis {
	/** 
	 * This is a cache variable for work bag processed elements.
	 */
	private final Collection processedCache = new HashSet();

	/** 
	 * This is a cache variable for work bag.
	 */
	private final IWorkBag wbCache = new HistoryAwareLIFOWorkBag(processedCache);

	/** 
	 * This retrieves dependence from the given analysis.
	 */
	private final IDependenceRetriever retriever;

	/** 
	 * This analysis provides seed dependence information.
	 */
	private final IDependencyAnalysis da;

	/** 
	 * This is similar to <code>dependent2dependee</code> except the direction is dependee->dependent. Hence, it is
	 * recommended that the subclass use this store dependence information.
	 *
	 * @invariant dependee2dependent != null
	 */
	private final Map dependee2dependent = new HashMap(Constants.getNumOfMethodsInApplication());

	/** 
	 * This can used to store dependent->dependee direction of dependence information.  Hence, it is recommended that the
	 * subclass use this store dependence information.
	 *
	 * @invariant dependent2dependee != null
	 */
	private final Map dependent2dependee = new HashMap(Constants.getNumOfMethodsInApplication());

	/**
	 * Creates an instance of this class.
	 *
	 * @param dependenceAnalysis for which indirect dependence info (or dependence closure) is to be provided.
	 * @param daRetriever should be used to retrieve dependence information from the given analysis.
	 *
	 * @pre dependenceAnalysis != null and daRetriever != null
	 */
	IndirectDependenceAnalysis(final AbstractDependencyAnalysis dependenceAnalysis, final IDependenceRetriever daRetriever) {
		da = dependenceAnalysis;
		retriever = daRetriever;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis#getDependees(java.lang.Object, java.lang.Object)
	 */
	public Collection getDependees(final Object dependent, final Object context) {
		Collection _result =
			(Collection) dependent2dependee.get(((AbstractDependencyAnalysis) da).getKeyFor(dependent, context));

		if (_result == null) {
			processedCache.clear();
			wbCache.clear();
			wbCache.addAllWork(da.getDependents(dependent, context));

			while (wbCache.hasWork()) {
				final Object _dependence = wbCache.getWork();
				wbCache.addAllWorkNoDuplicates(retriever.getDependees(da, _dependence, context));
			}
			_result = new ArrayList(processedCache);
			dependent2dependee.put(((AbstractDependencyAnalysis) da).getKeyFor(dependent, context), _result);
		}
		return Collections.unmodifiableCollection(_result);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis#getDependents(java.lang.Object, java.lang.Object)
	 */
	public Collection getDependents(final Object dependee, final Object context) {
		Collection _result =
			(Collection) dependee2dependent.get(((AbstractDependencyAnalysis) da).getKeyFor(dependee, context));

		if (_result == null) {
			processedCache.clear();
			wbCache.clear();
			wbCache.addAllWork(da.getDependents(dependee, context));

			while (wbCache.hasWork()) {
				final Object _dependence = wbCache.getWork();
				wbCache.addAllWorkNoDuplicates(retriever.getDependents(da, _dependence, context));
			}
			_result = new ArrayList(processedCache);
			dependee2dependent.put(((AbstractDependencyAnalysis) da).getKeyFor(dependee, context), _result);
		}
		return Collections.unmodifiableCollection(_result);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis#getDirection()
	 */
	public Object getDirection() {
		return da.getDirection();
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IIdentification#getIds()
	 */
	public Collection getIds() {
		return da.getIds();
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis#getIndirectVersionOfDependence()
	 */
	public IDependencyAnalysis getIndirectVersionOfDependence() {
		return this;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis#isStable()
	 */
	public boolean isStable() {
		return da.isStable();
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis#reset()
	 */
	public void reset() {
		dependee2dependent.clear();
		dependent2dependee.clear();
	}
}

// End of File
