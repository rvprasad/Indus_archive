
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

import edu.ksu.cis.indus.common.collections.CollectionsUtilities;
import edu.ksu.cis.indus.common.datastructures.Pair;

import edu.ksu.cis.indus.interfaces.ICallingContextRetriever;

import edu.ksu.cis.indus.processing.Context;

import edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;
import soot.ValueBox;

import soot.jimple.Stmt;


/**
 * This class encapsulates the logic to extract dependencies from a dependence analysis based on slice direction. This  class
 * is meant for internal use only.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class DependenceExtractor
  implements Closure {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(DependenceExtractor.class);

	/** 
	 * The entity which is the trigger.
	 */
	protected Object entity;

	/** 
	 * The context in which the trigger occurs.
	 */
	protected SootMethod occurringMethod;

	/** 
	 * The collection of new criteria.
	 */
	private final Collection newCriteria;

	/** 
	 * This maps criteria bases to a collection of contexts.
	 *
	 * @invariant dependence2contexts.oclIsKindOf(Map(Object, Collection(Stack(CallTriple))))
	 */
	private final Map criteriabase2contexts = new HashMap();

	/** 
	 * This maps dependence analysis IDs to the context retriever object.
	 *
	 * @invariant depID2ctxtRetriever.oclIsKindOf(Map(Object, ICallingContextRetriever))
	 */
	private final Map depID2ctxtRetriever = new HashMap();

	/** 
	 * The object that actually retrieves the dependences from the given dependence analysis.
	 */
	private IDependenceRetriver retriever;

	/**
	 * Creates a new CriteriaClosure object.
	 */
	protected DependenceExtractor() {
		newCriteria = new HashSet();
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
		 * @param method in which <code>stmt</code> occurs.
		 *
		 * @return a collection of dependences.
		 *
		 * @pre analysis != null and entity != null and method != null
		 * @post result != null and result.oclIsKindOf(Collection)
		 */
		Collection getDependences(final IDependencyAnalysis analysis, final Object entity, final SootMethod method);
	}

	/**
	 * Retrieves the contexts for the given criteria base.
	 *
	 * @param criteriaBase of interest.
	 *
	 * @return a collection of criteria.
	 *
	 * @pre criteriaBase != null
	 * @post result != null and result.oclIsKindOf(Collection(Stack(CallTriple)))
	 */
	public Collection getContextsFor(final Object criteriaBase) {
		final Collection _result = (Collection) criteriabase2contexts.get(criteriaBase);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("getContextsFor(criteriaBase = " + criteriaBase + ") -  : _result = " + _result);
		}

		return _result;
	}

	/**
	 * Sets the information that maps dependence id's to context retriever to be used.
	 *
	 * @param map a map from dependence analysis id to context retriever to be used with it.
	 *
	 * @pre map != null and map.oclIsKindOf(Map(Object, ICallingContextRetriever))
	 * @pre map.keySet()->forall( o | o.equals(IDependencyAnalysis.IDENTIFIER_BASED_DATA_DA) or
	 * 		o.equals(IDependencyAnalysis.REFERENCE_BASED_DATA_DA) or o.equals(IDependencyAnalysis.INTERFERENCE_DA) or
	 * 		o.equals(IDependencyAnalysis.READY_DA) or o.equals(IDependencyAnalysis.CONTROL_DA) or
	 * 		o.equals(IDependencyAnalysis.DIVERGENCE_DA) or o.equals(IDependencyAnalysis.SYNCHRONIZATION_DA)
	 */
	public void setDepID2ContextRetrieverMapping(final Map map) {
		depID2ctxtRetriever.putAll(map);
	}

	/**
	 * Populates the criteria based on the provided analysis.
	 *
	 * @param analysis from which to extract the criteria.
	 *
	 * @pre analysis != null and analysis.oclIsKindOf(AbstractDependencyAnalysis)
	 */
	public void execute(final Object analysis) {
		final IDependencyAnalysis _da = (IDependencyAnalysis) analysis;
		newCriteria.addAll(retriever.getDependences(_da, entity, occurringMethod));
		populateCriteriaBaseToContextsMap(_da);

		if (LOGGER.isDebugEnabled()) {
			final StringBuffer _sb = new StringBuffer();
			_sb.append("Criteria bases for " + entity + "@" + occurringMethod + " from " + _da.getClass() + " are :\n[");

			for (final Iterator _j = retriever.getDependences(_da, entity, occurringMethod).iterator(); _j.hasNext();) {
				_sb.append("\n\t->" + _j.next());
			}
			_sb.append("\n]");
			LOGGER.debug(_sb.toString());
		}
	}

	/**
	 * Sets the value of <code>retriever</code>.
	 *
	 * @param theRetriever the new value of <code>retriever</code>.
	 */
	void setDependenceRetriever(final IDependenceRetriver theRetriever) {
		retriever = theRetriever;
	}

	/**
	 * Retrieves a collection of dependence pairs based on last trigger set.
	 *
	 * @return a collection of  criteria.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(Pair(Stmt, SootMethod)))
	 */
	Collection getDependences() {
		return Collections.unmodifiableCollection(newCriteria);
	}

	/**
	 * Sets the dependee/dependent.  It also clears information pertaining to the previous trigger.
	 *
	 * @param theEntity is the dependent/dependee.
	 * @param method in which the entity occurs.
	 *
	 * @pre theEntity != null and method != null
	 */
	void setTrigger(final Object theEntity, final SootMethod method) {
		entity = theEntity;
		occurringMethod = method;
		newCriteria.clear();
		criteriabase2contexts.clear();
	}

	/**
	 * Populates the contexts in <code>criteriabase2contexts</code> based on the given dependence analysis.
	 *
	 * @param da to be used while populating the map.
	 *
	 * @pre da != null
	 * @post criteriabase2contexts.oclIsKindOf(Map(Object, Collection(Stack(CallTriple))))
	 */
	private void populateCriteriaBaseToContextsMap(final IDependencyAnalysis da) {
		final Collection _ids = da.getIds();
		final Collection _retrievers = new HashSet();

		if (CollectionUtils.containsAny(_ids, depID2ctxtRetriever.keySet())) {
			for (final Iterator _i = CollectionUtils.intersection(_ids, depID2ctxtRetriever.keySet()).iterator();
				  _i.hasNext();) {
				_retrievers.add(depID2ctxtRetriever.get(_i.next()));
			}
		} else {
			_retrievers.add(ICallingContextRetriever.NULL_CONTEXT_RETRIEVER);
		}

		for (final Iterator _l = _retrievers.iterator(); _l.hasNext();) {
			final ICallingContextRetriever _ctxtRetriever = (ICallingContextRetriever) _l.next();
			_ctxtRetriever.setInfoFor(ICallingContextRetriever.SRC_ENTITY, entity);
			_ctxtRetriever.setInfoFor(ICallingContextRetriever.SRC_METHOD, occurringMethod);

			final Context _context = new Context();
			final Iterator _j = newCriteria.iterator();
			final int _jEnd = newCriteria.size();

			for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
				final Object _t = _j.next();
				final boolean _containsKey = criteriabase2contexts.containsKey(_t);

				if (_t instanceof Pair && !(_containsKey && ((Collection) criteriabase2contexts.get(_t)).contains(null))) {
					final Pair _pair = (Pair) _t;
					final Object _o = _pair.getFirst();
					final SootMethod _criteriabaseMethod = (SootMethod) _pair.getSecond();

					if (_o instanceof Stmt) {
						final Stmt _stmt = (Stmt) _o;
						_context.setStmt(_stmt);
						_context.setRootMethod(_criteriabaseMethod);

						final Collection _programPoints = _stmt.getUseAndDefBoxes();
						final Iterator _k = _programPoints.iterator();
						final int _kEnd = _programPoints.size();

						for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
							_context.setProgramPoint((ValueBox) _k.next());

							final Collection _ctxts = _ctxtRetriever.getCallingContextsForProgramPoint(_context);
							CollectionsUtilities.putAllIntoCollectionInMap(criteriabase2contexts, _t, _ctxts,
								CollectionsUtilities.HASH_SET_FACTORY);
						}
					} else {
						final Collection _ctxts = _ctxtRetriever.getCallingContextsForThis(_criteriabaseMethod);
						CollectionsUtilities.putAllIntoCollectionInMap(criteriabase2contexts, _t, _ctxts,
							CollectionsUtilities.HASH_SET_FACTORY);
					}
				}
			}
		}

		if (LOGGER.isDebugEnabled()) {
			final List _t = new ArrayList(_ids);
			Collections.sort(_t);
			LOGGER.debug("populateDependenceToContextsMap(da = " + _t.toString() + ") : - dependence2contexts - "
				+ criteriabase2contexts);
		}
	}
}

// End of File
