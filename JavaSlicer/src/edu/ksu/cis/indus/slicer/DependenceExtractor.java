
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

package edu.ksu.cis.indus.slicer;

import edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis;
import edu.ksu.cis.indus.staticanalyses.dependency.ReadyDAv1;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.Closure;


/**
 * This class encapsulates the logic to extract dependencies from a dependence analysis based on slice direction.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
class DependenceExtractor
  implements Closure {
	/** 
	 * The context in which the trigger occurs.
	 */
	protected Object context;

	/** 
	 * The entity which is the trigger.
	 */
	protected Object entity;

	/** 
	 * The collection of criteria based on the flow of control reaching (exclusive) or leaving the statement.
	 */
	private final Collection falseCriteria;

	/** 
	 * The collection of criteria based on the flow of control reaching and leaving the statement.
	 */
	private final Collection trueCriteria;

	/** 
	 * The object that actually retrieves the dependences from the given dependence analysis.
	 */
	private IDependenceRetriver retriever;

	/** 
	 * This maps truth values (true/false) to a collection of criteria based on reachability of control flow.
	 */
	private final Map newCriteria;

	/**
	 * Creates a new CriteriaClosure object.
	 */
	protected DependenceExtractor() {
		newCriteria = new HashMap();
		trueCriteria = new HashSet();
		falseCriteria = new HashSet();
		newCriteria.put(Boolean.TRUE, trueCriteria);
		newCriteria.put(Boolean.FALSE, falseCriteria);
	}

	/**
	 * This interface enables to retrieve dependences.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	static interface IDependenceRetriver {
		/**
		 * Retrieves the dependencies.
		 *
		 * @param analysis is the analysis from which to retrieve the dependences.
		 * @param entity for which the dependences are requested.
		 * @param context in which <code>stmt</code> occurs.
		 *
		 * @return a collection of dependences.
		 *
		 * @pre analysis != null and entity != null and context != null
		 * @post result != null and result.oclIsKindOf(Collection)
		 */
		Collection getDependences(final IDependencyAnalysis analysis, final Object entity, final Object context);
	}

	/**
	 * Populates the criteria based on the provided analysis.
	 *
	 * @param analysis from which to extract the criteria.
	 *
	 * @pre analysis != null and analysis.oclIsKindOf(AbstractDependencyAnalysis)
	 */
	public final void execute(final Object analysis) {
		final IDependencyAnalysis _da = (IDependencyAnalysis) analysis;
		final Collection _criteria = retriever.getDependences(_da, entity, context);

		if (_da.getId().equals(IDependencyAnalysis.READY_DA)) {
			final Collection _specials = ((ReadyDAv1) _da).getSynchronizedMethodEntryExitPoints(_criteria);
			falseCriteria.addAll(_specials);
			_criteria.removeAll(_specials);
		}
		trueCriteria.addAll(_criteria);

		if (SlicingEngine.LOGGER.isDebugEnabled()) {
			final StringBuffer _sb = new StringBuffer();
			_sb.append("Criteria bases for " + entity + "@" + context + " from " + _da.getClass() + " are :\n[");

			for (final Iterator _j = retriever.getDependences(_da, entity, context).iterator(); _j.hasNext();) {
				_sb.append("\n\t->" + _j.next());
			}
			_sb.append("\n]");
			SlicingEngine.LOGGER.debug(_sb.toString());
		}
	}

	/**
	 * Retrieves the criteria mapping truth values (true/false indicating the execution effect of the criteria is considered)
	 * to collection of dependence pairs.
	 *
	 * @return a mapping of truth values to criteria.
	 *
	 * @post result != null and result.oclIsKindOf(Map(Boolean, Collection(Pair(Stmt, SootMethod))))
	 */
	final Map getCriteriaMap() {
		return Collections.unmodifiableMap(newCriteria);
	}

	/**
	 * Sets the value of <code>retriever</code>.
	 *
	 * @param theRetriever the new value of <code>retriever</code>.
	 */
	void setRetriever(final IDependenceRetriver theRetriever) {
		retriever = theRetriever;
	}

	/**
	 * Sets the dependee/dependent.
	 *
	 * @param theEntity is the dependent/dependee.
	 * @param theContext in which <code>dependeXX</code> occurs.
	 *
	 * @pre theEntity != null and theContext != null
	 */
	final void setTrigger(final Object theEntity, final Object theContext) {
		entity = theEntity;
		context = theContext;
		trueCriteria.clear();
		falseCriteria.clear();
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2004/08/20 02:13:05  venku
   - refactored slicer based on slicing direction.

 */
