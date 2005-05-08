
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.SootMethod;


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
	 * This retrieves dependence from the given analysis.
	 */
	private final IDependenceRetriever retriever;

	/** 
	 * This analysis provides seed dependence information.
	 */
	private final IDependencyAnalysis da;

	/** 
	 * This is a cache variable for work bag.
	 */
	private final IWorkBag wbCache = new HistoryAwareLIFOWorkBag(processedCache);

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
			wbCache.addAllWork(da.getDependees(dependent, context));

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

	/**
	 * Returns a stringized representation of this analysis.  The representation includes the results of the analysis.
	 *
	 * @param methods for which the information needs to be stringized.
	 *
	 * @return a stringized representation of this object.
	 *
	 * @pre methods != null and methods.oclIsKindOf(Collection(SootMethod))
	 * @post result != null
	 */
	public String toString(final Collection methods) {
		final StringBuffer _result =
			new StringBuffer("Statistics for control dependence as calculated by " + getClass().getName() + "\n");
		int _localEdgeCount;
		int _edgeCount = 0;

		final StringBuffer _temp = new StringBuffer();

		for (final Iterator _i = methods.iterator(); _i.hasNext();) {
			final SootMethod _method = (SootMethod) _i.next();

			if (!_method.hasActiveBody()) {
				continue;
			}

			final List _stmts = new ArrayList(_method.getActiveBody().getUnits());
			_localEdgeCount = 0;

			for (int _j = 0; _j < _stmts.size(); _j++) {
				final Object _stmt = _stmts.get(_j);
				final Collection _dees = getDependees(_stmt, _method);
				_temp.append("\t\t" + _stmt + " --> " + _dees + "\n");
				_localEdgeCount += _dees.size();
			}

			_result.append("\tFor " + _method + " there are " + _localEdgeCount + " control dependence edges.\n");
			_result.append(_temp);
			_temp.delete(0, _temp.length());
			_edgeCount += _localEdgeCount;
		}
		_result.append("A total of " + _edgeCount + " control dependence edges exists.\n");
		return _result.toString();
	}
}

// End of File
