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

import edu.ksu.cis.indus.common.collections.CollectionUtils;
import edu.ksu.cis.indus.common.collections.IClosure;
import edu.ksu.cis.indus.common.collections.MapUtils;
import edu.ksu.cis.indus.common.collections.SetUtils;
import edu.ksu.cis.indus.common.collections.Stack;
import edu.ksu.cis.indus.common.datastructures.Pair;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootMethod;
import soot.ValueBox;

import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InvokeStmt;
import soot.jimple.MonitorStmt;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;

/**
 * This class encapsulates the logic to extract dependencies from a dependence analysis based on slice direction. This class
 * is meant for internal use only.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
final class DependenceExtractor
		implements IClosure<IDependencyAnalysis<?, ?, ?, ?, ?, ?>> {

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
		 * @return a collection of dependences.
		 * @pre analysis != null and entity != null and method != null
		 * @post result != null and result.oclIsKindOf(Collection)
		 */
		Collection<Object> getDependences(final IDependencyAnalysis<?, ?, ?, ?, ?, ?> analysis, final Object entity,
				final SootMethod method);
	}

	/**
	 * The interface used to extract program points of a statement for the purpose of context generation.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	private interface IProgramPointRetriever {

		/**
		 * Retrieves the program points of interest from the given statement.
		 *
		 * @param stmt of interest.
		 * @return the collection of value boxes/program points.
		 * @pre stmt != null
		 */
		Collection<ValueBox> getProgramPoints(Stmt stmt);
	}

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(DependenceExtractor.class);

	/**
	 * This retriever will return the program point containing the receiver of the wait/notify invocation or the locked object
	 * of the monitor.
	 */
	private static final IProgramPointRetriever READY_DA_PPR = new IProgramPointRetriever() {

		public Collection<ValueBox> getProgramPoints(final Stmt stmt) {
			if (stmt instanceof InvokeStmt) {
				return Collections.singleton((((VirtualInvokeExpr) ((InvokeStmt) stmt).getInvokeExpr())).getBaseBox());
			} else if (stmt instanceof MonitorStmt) {
				return Collections.singleton(((MonitorStmt) stmt).getOpBox());
			} else {
				throw new IllegalArgumentException("stmt has to be of type MonitorStmt or InvokeStmt.");
			}
		}
	};

	/**
	 * This retriever will return the program point containing the reference.
	 */
	private static final IProgramPointRetriever REFERENTIAL_DA_PPR = new IProgramPointRetriever() {

		public Collection<ValueBox> getProgramPoints(final Stmt stmt) {
			final Collection<ValueBox> _result;

			if (stmt.containsArrayRef()) {
				_result = Collections.singleton(stmt.getArrayRef().getBaseBox());
			} else if (stmt.containsFieldRef()) {
				final FieldRef _fr = stmt.getFieldRef();

				if (_fr instanceof InstanceFieldRef) {
					_result = Collections.singleton(((InstanceFieldRef) _fr).getBaseBox());
				} else {
					_result = Collections.singleton(stmt.getFieldRefBox());
				}
			} else {
				throw new IllegalArgumentException("stmt has to contain an array/field reference.");
			}
			return _result;
		}
	};

	/**
	 * This retriever will return the program point containing the locked object.
	 */
	private static final IProgramPointRetriever SYNCHRONIZATION_DA_PPR = new IProgramPointRetriever() {

		public Collection<ValueBox> getProgramPoints(final Stmt stmt) {
			if (stmt instanceof MonitorStmt) {
				return Collections.singleton(((MonitorStmt) stmt).getOpBox());
			}
			throw new IllegalArgumentException("stmt has to be of type MonitorStmt.");
		}
	};

	/**
	 * The entity which is the trigger.
	 */
	protected Object entity;

	/**
	 * The context in which the trigger occurs.
	 */
	protected SootMethod occurringMethod;

	/**
	 * This maps criteria bases to a collection of contexts.
	 */
	private final Map<Object, Collection<Stack<CallTriple>>> criteriabase2contexts;

	/**
	 * The collection of dependees/dependents that form the new criteria bases.
	 */
	private final Collection<Object> dependences;

	/**
	 * This maps dependence analysis IDs to the context retriever object.
	 */
	private final Map<IDependencyAnalysis.DependenceSort, ICallingContextRetriever> depID2ctxtRetriever;

	/**
	 * The instance of SlicingEngine that will use this instance.
	 */
	private final SlicingEngine engine;

	/**
	 * The object that actually retrieves the dependences from the given dependence analysis.
	 */
	private IDependenceRetriver retriever;

	/**
	 * Creates a new CriteriaClosure object.
	 *
	 * @param slicingEngine that will use this instance.
	 * @pre slicingEngine != null
	 */
	protected DependenceExtractor(final SlicingEngine slicingEngine) {
		dependences = new HashSet<Object>();
		engine = slicingEngine;
		criteriabase2contexts = new HashMap<Object, Collection<Stack<CallTriple>>>();
		depID2ctxtRetriever = new HashMap<IDependencyAnalysis.DependenceSort, ICallingContextRetriever>();
	}

	/**
	 * Populates the criteria based on the provided analysis.
	 *
	 * @param analysis from which to extract the criteria.
	 * @pre analysis != null
	 */
	public void execute(final IDependencyAnalysis<?, ?, ?, ?, ?, ?> analysis) {
		final IDependencyAnalysis<?, ?, ?, ?, ?, ?> _da = analysis;
		final Collection<Object> _t = retriever.getDependences(_da, entity, occurringMethod);

		if (!_t.isEmpty()) {
			dependences.addAll(_t);
			populateCriteriaBaseToContextsMap(_da, _t);
		}
	}

	/**
	 * Retrieves the contexts for the given criteria base.
	 *
	 * @param <T> DOCUMENT ME!
	 * @param criteriaBase of interest.
	 * @return a collection of criteria.
	 * @pre criteriaBase != null
	 * @post result != null and result.oclIsKindOf(Collection(Stack(CallTriple)))
	 */
	public <T> Collection<Stack<CallTriple>> getContextsFor(final T criteriaBase) {
		final Collection<Stack<CallTriple>> _result = MapUtils.queryCollection(criteriabase2contexts, criteriaBase);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("getContextsFor(criteriaBase = " + criteriaBase + ") -  : _result = " + _result);
		}

		return _result;
	}

	/**
	 * Sets the information that maps dependence id's to context retriever to be used.
	 *
	 * @param map a map from dependence analysis id to context retriever to be used with it.
	 * @pre map != null and map.oclIsKindOf(Map(Object, ICallingContextRetriever))
	 */
	public void setDepID2ContextRetrieverMapping(final Map<IDependencyAnalysis.DependenceSort, ICallingContextRetriever> map) {
		depID2ctxtRetriever.putAll(map);
	}

	/**
	 * Retrieves a collection of dependence pairs based on last trigger set.
	 *
	 * @return a collection of criteria.
	 * @post result != null and result.oclIsKindOf(Collection(Pair(Stmt, SootMethod)))
	 */
	Collection<?> getDependences() {
		return Collections.unmodifiableCollection(dependences);
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
	 * Sets the dependee/dependent. It also clears information pertaining to the previous trigger.
	 *
	 * @param theEntity is the dependent/dependee.
	 * @param method in which the entity occurs.
	 * @pre theEntity != null and method != null
	 */
	void setTrigger(final Object theEntity, final SootMethod method) {
		entity = theEntity;
		occurringMethod = method;
		dependences.clear();
		criteriabase2contexts.clear();
	}

	/**
	 * Populates the contexts in <code>criteriabase2contexts</code> based on the given interprocedural dependence analysis.
	 *
	 * @param ids of the dependence analysis from which <code>criteriaBases</code> was generated.
	 * @param criteriaBases for which contexts are required.
	 * @throws IllegalArgumentException when the preconditions are not satisfied.
	 * @pre ids != null and criteriaBases != null
	 * @pre ids.contains(IDependencyAnalysis.DependenceSort.READY_DA) or
	 *      ids.contains(IDependencyAnalysis.DependenceSort.INTERFERENCE_DA) or
	 *      ids.contains(IDependencyAnalysis.DependenceSort.REFERENCE_BASED_DATA_DA) or
	 *      ids.contains(IDependencyAnalysis.DependenceSort.SYNCHRONIZATION_DA)
	 */
	private void populateContextsForInterProceduralDependences(final Collection<IDependencyAnalysis.DependenceSort> ids,
			final Collection<?> criteriaBases) {
		final IProgramPointRetriever _result;

		if (ids.contains(IDependencyAnalysis.DependenceSort.READY_DA)) {
			_result = READY_DA_PPR;
		} else if (ids.contains(IDependencyAnalysis.DependenceSort.INTERFERENCE_DA)
				|| ids.contains(IDependencyAnalysis.DependenceSort.REFERENCE_BASED_DATA_DA)) {
			_result = REFERENTIAL_DA_PPR;
		} else if (ids.contains(IDependencyAnalysis.DependenceSort.SYNCHRONIZATION_DA)) {
			_result = SYNCHRONIZATION_DA_PPR;
		} else {
			throw new IllegalArgumentException("da has to have one of these Ids mentioned in the documentation. - " + ids);
		}

		final IProgramPointRetriever _ppr = _result;

		for (final Iterator<IDependencyAnalysis.DependenceSort> _i = SetUtils.intersection(ids, depID2ctxtRetriever.keySet())
				.iterator(); _i.hasNext();) {
			final ICallingContextRetriever _ctxtRetriever = depID2ctxtRetriever.get(_i.next());
			_ctxtRetriever.setInfoFor(ICallingContextRetriever.Identifiers.SRC_ENTITY, entity);
			_ctxtRetriever.setInfoFor(ICallingContextRetriever.Identifiers.SRC_METHOD, occurringMethod);

			final Context _context = new Context();

			for (final Iterator<?> _j = criteriaBases.iterator(); _j.hasNext();) {
				@SuppressWarnings("unchecked") final Pair<Stmt, SootMethod> _t = (Pair) _j.next();
				final boolean _containsKey = criteriabase2contexts.containsKey(_t);

				if (!(_containsKey && criteriabase2contexts.get(_t).contains(null))) {
					final Pair<Stmt, SootMethod> _pair = _t;
					final Stmt _stmt = _pair.getFirst();
					final SootMethod _criteriabaseMethod = _pair.getSecond();

					if (_stmt != null) {
						_context.setStmt(_stmt);
						_context.setRootMethod(_criteriabaseMethod);

						final Collection<ValueBox> _programPoints = _ppr.getProgramPoints(_stmt);

						for (final Iterator<ValueBox> _k = _programPoints.iterator(); _k.hasNext();) {
							_context.setProgramPoint(_k.next());

							final Collection<Stack<CallTriple>> _ctxts = _ctxtRetriever
									.getCallingContextsForProgramPoint(_context);
							MapUtils.putAllIntoCollectionInMap(criteriabase2contexts, _pair, _ctxts);
						}
					} else {
						_context.setRootMethod(_criteriabaseMethod);

						final Collection<Stack<CallTriple>> _ctxts = _ctxtRetriever.getCallingContextsForThis(_context);
						MapUtils.putAllIntoCollectionInMap(criteriabase2contexts, _pair, _ctxts);
					}
				}
			}
		}
	}

	/**
	 * Populates the contexts in <code>criteriabase2contexts</code> based on the given dependence analysis.
	 *
	 * @param da to be used while populating the map.
	 * @param criteriaBases for which contexts need to be retrieved.
	 * @pre da != null and criteriaBases != null and criteriaBases
	 */
	private void populateCriteriaBaseToContextsMap(final IDependencyAnalysis<?, ?, ?, ?, ?, ?> da,
			final Collection<?> criteriaBases) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("populateCriteriaBaseToContextsMap(IDependencyAnalysis da=" + da.getIds()
					+ ", Collection criteriaBases=" + criteriaBases + ") - BEGIN");
		}

		final Collection<IDependencyAnalysis.DependenceSort> _ids = da.getIds();

		if (_ids.contains(IDependencyAnalysis.DependenceSort.CONTROL_DA)
				|| _ids.contains(IDependencyAnalysis.DependenceSort.IDENTIFIER_BASED_DATA_DA)) {
			for (final Iterator<?> _i = criteriaBases.iterator(); _i.hasNext();) {
				MapUtils.putIntoCollectionInMap(criteriabase2contexts, _i.next(), engine.getCopyOfCallStackCache());
			}
		} else if (CollectionUtils.containsAny(_ids, depID2ctxtRetriever.keySet())) {
			populateContextsForInterProceduralDependences(_ids, criteriaBases);
		} else {
			// if there are no context retrievers for the given dependence analysis, then return a null context.
			for (Object _cb : criteriaBases) {
				MapUtils.putIntoCollectionInMap(criteriabase2contexts, _cb, (Stack<CallTriple>) null);
			}
		}

		if (LOGGER.isDebugEnabled()) {
			final List<IDependencyAnalysis.DependenceSort> _t = new ArrayList<IDependencyAnalysis.DependenceSort>(_ids);
			Collections.sort(_t);
			LOGGER.debug("populateDependenceToContextsMap(): criteriabases2contexts - " + criteriabase2contexts + " - END");
		}
	}
}

// End of File
