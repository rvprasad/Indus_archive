
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
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

import edu.ksu.cis.indus.common.ToStringBasedComparator;
import edu.ksu.cis.indus.common.datastructures.FIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.PoolAwareWorkBag;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.interfaces.INewExpr2InitMapper;
import edu.ksu.cis.indus.interfaces.IPoolable;

import edu.ksu.cis.indus.processing.Context;

import edu.ksu.cis.indus.staticanalyses.AnalysesController;
import edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis;
import edu.ksu.cis.indus.staticanalyses.dependency.ReadyDAv1;

import java.util.ArrayList;
import java.util.BitSet;
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

import soot.ArrayType;
import soot.Local;
import soot.RefType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.ValueBox;
import soot.VoidType;

import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.NewExpr;
import soot.jimple.ParameterRef;
import soot.jimple.Stmt;

import soot.toolkits.graph.CompleteUnitGraph;

import soot.toolkits.scalar.SimpleLocalDefs;
import soot.toolkits.scalar.SimpleLocalUses;
import soot.toolkits.scalar.UnitValueBoxPair;


/**
 * This class accepts slice criterions and generates slices of the given system.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public final class SlicingEngine {
	/** 
	 * Backward slice request.
	 */
	public static final Object BACKWARD_SLICE = "BACKWARD_SLICE";

	/** 
	 * Complete slice request.
	 */
	public static final Object COMPLETE_SLICE = "COMPLETE_SLICE";

	/** 
	 * Forward slice request.
	 */
	public static final Object FORWARD_SLICE = "FORWARD_SLICE";

	/** 
	 * This just a convenience collection of the types of slices supported by this class.
	 *
	 * @invariant sliceTypes.contains(FORWARD_SLICE) and sliceTypes.contains(BACKWARD_SLICE) and sliceTypes.contains
	 * 			  (COMPLETE_SLICE)
	 */
	public static final Collection SLICE_TYPES;

	/** 
	 * The logger used by instances of this class to log messages.
	 */
	static final Log LOGGER = LogFactory.getLog(SlicingEngine.class);

	static {
		final Collection _c = new HashSet();
		_c.add(BACKWARD_SLICE);
		_c.add(FORWARD_SLICE);
		_c.add(COMPLETE_SLICE);
		SLICE_TYPES = Collections.unmodifiableCollection(_c);
	}

	/** 
	 * This is the basic block graph manager which manages the BB graphs corresponding to the system being sliced.
	 */
	BasicBlockGraphMgr bbgMgr;

	/** 
	 * The direction of the slice.  It's default value is <code>BACKWARD_SLICE</code>.
	 *
	 * @invariant sliceTypes.contains(sliceType)
	 */
	Object sliceType = BACKWARD_SLICE;

	/** 
	 * The controller used to access the dependency analysis info during slicing.
	 */
	private AnalysesController controller;

	/** 
	 * This is the collection of methods whose exits were transformed.
	 */
	private final Collection exitTransformedMethods = new HashSet();

	/** 
	 * The work bag used during slicing.
	 *
	 * @invariant workbag != null and workbag.oclIsKindOf(Bag)
	 * @invariant workbag->forall(o | o.oclIsKindOf(AbstractSliceCriterion))
	 */
	private final IWorkBag workbag = new PoolAwareWorkBag(new FIFOWorkBag());

	/** 
	 * The collection of control based Dependence analysis to be used during slicing.  Synchronization, Divergence, and
	 * Control dependences are such dependences.
	 *
	 * @invariant controlflowBasedDAs->forall(o | o.oclIsKindOf(AbstractDependencyAnalysis) and
	 * 			  o.getId().equals(AbstractDependencyAnalysis.CONTROL_DA))
	 */
	private Collection controlflowBasedDAs = new ArrayList();

	/** 
	 * This provides the call graph information in the system being sliced.
	 */
	private ICallGraphInfo cgi;

	/** 
	 * The list of slice criteria.
	 *
	 * @invariant criteria != null and criteria->forall(o | o.oclIsKindOf(AbstractSliceCriterion))
	 */
	private List criteria = new ArrayList();

	/** 
	 * This maps methods to methods to a bitset that indicates which of the parameters of the method is required in the
	 * slice.
	 *
	 * @invariant method2params.oclIsKindOf(Map(SootMethod, BitSet))
	 * @invariant method2params->forall(o | o.getValue().size() = o.getKey().getParameterCount())
	 */
	private final Map method2params = new HashMap();

	/** 
	 * The closure used to generate criteria based on slice direction. See <code>setSliceType()</code> for details.
	 */
	private DependenceClosure criteriaClosure;

	/** 
	 * This maps new expressions to corresponding init call sites.
	 */
	private INewExpr2InitMapper initMapper;

	/** 
	 * This collects the parts of the system that make up the slice.
	 */
	private SliceCollector collector;

	/**
	 * Creates a new SlicingEngine object.
	 */
	public SlicingEngine() {
		collector = new SliceCollector(this);
	}

	/**
	 * This class encapsulates the logic to extract dependencies from a dependence analysis based on slice direction.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private static class DependenceClosure
	  implements Closure {
		/** 
		 * The method in which the trigger occurs.
		 */
		protected SootMethod method;

		/** 
		 * The statement which is the trigger.
		 */
		protected Stmt stmt;

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
		private final IDependenceRetriver retriever;

		/** 
		 * This maps truth values (true/false) to a collection of criteria based on reachability of control flow.
		 */
		private final Map newCriteria;

		/**
		 * Creates a new CriteriaClosure object.
		 *
		 * @param dependenceRetriever to be used to extract dependence from the dependence analysis.
		 *
		 * @pre dependenceRetriever != null
		 */
		protected DependenceClosure(final IDependenceRetriver dependenceRetriever) {
			newCriteria = new HashMap();
			trueCriteria = new HashSet();
			falseCriteria = new HashSet();
			retriever = dependenceRetriever;
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
			 * @param stmt for which the dependences are requested.
			 * @param method in which <code>stmt</code> occurs.
			 *
			 * @return a collection of dependences.
			 *
			 * @pre analysis != null and stmt != null and method != null
			 * @post result != null and result.oclIsKindOf(Collection(Pair(Stmt, SootMethod)))
			 */
			Collection getDependences(final IDependencyAnalysis analysis, final Stmt stmt, final SootMethod method);
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
			final Collection _criteria = retriever.getDependences(_da, stmt, method);

			if (_da.getId().equals(IDependencyAnalysis.READY_DA)) {
				final Collection _specials = ((ReadyDAv1) _da).getSynchronizedMethodEntryExitPoints(_criteria);
				falseCriteria.addAll(_specials);
				_criteria.removeAll(_specials);
			}
			trueCriteria.addAll(_criteria);

			if (LOGGER.isDebugEnabled()) {
				final StringBuffer _sb = new StringBuffer();
				_sb.append("Criteria bases for " + stmt + "@" + method + " from " + _da.getClass() + " are :\n[");

				for (final Iterator _j = retriever.getDependences(_da, stmt, method).iterator(); _j.hasNext();) {
					_sb.append("\n\t->" + _j.next());
				}
				_sb.append("\n]");
				LOGGER.debug(_sb.toString());
			}
		}

		/**
		 * Retrieves the criteria mapping truth values (true/false indicating the execution effect of the criteria is
		 * considered) to collection of dependence pairs.
		 *
		 * @return a mapping of truth values to criteria.
		 *
		 * @post result != null and result.oclIsKindOf(Map(Boolean, Collection(Pair(Stmt, SootMethod))))
		 */
		final Map getCriteriaMap() {
			return Collections.unmodifiableMap(newCriteria);
		}

		/**
		 * Sets the dependee/dependent.
		 *
		 * @param dependeXX is the dependent/dependee.
		 * @param theMethod in which <code>dependeXX</code> occurs.
		 *
		 * @pre dependeXX != null and theMethod != null
		 */
		final void setTrigger(final Stmt dependeXX, final SootMethod theMethod) {
			stmt = dependeXX;
			method = theMethod;
			trueCriteria.clear();
			falseCriteria.clear();
		}
	}


	/**
	 * This class provides implementation to retrieve criteria required for calculating backward slice.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private class BackwardSliceDependenceClosure
	  implements DependenceClosure.IDependenceRetriver {
		/**
		 * @see DependenceClosure.IDependenceRetriver#getDependences(IDependencyAnalysis,Stmt,SootMethod)
		 */
		public Collection getDependences(final IDependencyAnalysis da, final Stmt stmt, final SootMethod method) {
			final Collection _result = new HashSet();

			if (da.getDirection().equals(IDependencyAnalysis.BACKWARD_DIRECTION)) {
				_result.addAll(da.getDependees(stmt, method));
			} else if (da.getDirection().equals(IDependencyAnalysis.FORWARD_DIRECTION)) {
				_result.addAll(da.getDependents(stmt, method));
			}
			return _result;
		}
	}


	/**
	 * This class provides implementation to retrieve criteria required for calculating complete slice.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private class CompleteSliceDependenceClosure
	  implements DependenceClosure.IDependenceRetriver {
		/**
		 * @see DependenceClosure.IDependenceRetriver#getDependences(IDependencyAnalysis,Stmt,SootMethod)
		 */
		public Collection getDependences(final IDependencyAnalysis da, final Stmt stmt, final SootMethod method) {
			final Collection _result = new HashSet();
			_result.addAll(da.getDependees(stmt, method));
			_result.addAll(da.getDependents(stmt, method));
			return _result;
		}
	}


	/**
	 * This class provides implementation to retrieve criteria required for calculating forrward slice.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private class ForwardSliceDependenceClosure
	  implements DependenceClosure.IDependenceRetriver {
		/**
		 * @see DependenceClosure.IDependenceRetriver#getDependences(IDependencyAnalysis,Stmt,SootMethod)
		 */
		public Collection getDependences(final IDependencyAnalysis da, final Stmt stmt, final SootMethod method) {
			final Collection _result = new HashSet();

			if (da.getDirection().equals(IDependencyAnalysis.BACKWARD_DIRECTION)) {
				_result.addAll(da.getDependents(stmt, method));
			} else if (da.getDirection().equals(IDependencyAnalysis.FORWARD_DIRECTION)) {
				_result.addAll(da.getDependees(stmt, method));
			}
			return _result;
		}
	}

	/**
	 * Sets the the id's of the dependence analyses to be used for slicing.  The analyses are provided by the given
	 * controller that was used to initialize the analyses. to
	 *
	 * @param ctrl provides dependency information required for slicing.
	 * @param dependenciesToUse is the ids of the dependecies to be considered for slicing.
	 *
	 * @pre ctrl != null and dependenciesToUse != null
	 * @pre dependeciesToUse->forall(o | controller.getAnalysis(o) != null)
	 */
	public void setAnalysesControllerAndDependenciesToUse(final AnalysesController ctrl, final Collection dependenciesToUse) {
		controller = ctrl;
		controlflowBasedDAs.clear();

		for (final Iterator _i = dependenciesToUse.iterator(); _i.hasNext();) {
			final Object _id = _i.next();

			if (_id.equals(IDependencyAnalysis.CONTROL_DA)
				  || _id.equals(IDependencyAnalysis.SYNCHRONIZATION_DA)
				  || _id.equals(IDependencyAnalysis.DIVERGENCE_DA)) {
				controlflowBasedDAs.addAll(controller.getAnalyses(_id));
			}
		}

		if (dependenciesToUse.contains(IDependencyAnalysis.READY_DA)) {
			controlflowBasedDAs.addAll(controller.getAnalyses(IDependencyAnalysis.READY_DA));
		}
	}

	/**
	 * Sets the basic block graph manager to be used during slicing.
	 *
	 * @param basicBlockGraphMgr is the basic block graph manager for the system being sliced.
	 *
	 * @pre bbgMgr != null
	 */
	public void setBasicBlockGraphManager(final BasicBlockGraphMgr basicBlockGraphMgr) {
		bbgMgr = basicBlockGraphMgr;
	}

	/**
	 * Sets the call graph to be used during slicing.
	 *
	 * @param callgraph provides call graph information about the system being sliced.
	 *
	 * @pre callgraph != null
	 */
	public void setCgi(final ICallGraphInfo callgraph) {
		cgi = callgraph;
	}

	/**
	 * Retrieves the slice collector being used by this instance of the slice engine.
	 *
	 * @return the slice collector
	 *
	 * @post result != null
	 */
	public SliceCollector getCollector() {
		return collector;
	}

	/**
	 * Sets the object that maps new expressions to corresponding init invocation sites.
	 *
	 * @param mapper maps new expressions to corresponding init invocation sites.
	 *
	 * @pre mapper != null
	 */
	public void setInitMapper(final INewExpr2InitMapper mapper) {
		initMapper = mapper;
	}

	/**
	 * Sets the given criteria as the slicing criteria over the next run.
	 *
	 * @param sliceCriteria are ofcourse the slicing criteria
	 *
	 * @throws IllegalStateException when there are criteria which are not of type <code>AbstractSliceCriterion</code>.
	 *
	 * @pre sliceCriteria != null and sliceCriteria.oclIsKindOf(Collection(AbstractSliceCriterion))
	 */
	public void setSliceCriteria(final Collection sliceCriteria) {
		if (sliceCriteria == null || sliceCriteria.size() == 0) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Slice criteria is unspecified.");
			}
			throw new IllegalStateException("Slice criteria is unspecified.");
		} else if (controller == null) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Class Manager and/or Controller is unspecified.");
			}
			throw new IllegalStateException("Class Manager and/or Controller is unspecified.");
		}

		for (final Iterator _i = sliceCriteria.iterator(); _i.hasNext();) {
			final Object _o = _i.next();

			if (!SliceCriteriaFactory.isSlicingCriterion(_o)) {
				LOGGER.error("The work piece is not a subtype of AbstractSliceCriterion" + _o);
				throw new IllegalStateException("The work piece is not a subtype of AbstractSliceCriterion" + _o);
			}

			try {
				criteria.add(((AbstractSliceCriterion) _o).clone());
			} catch (final CloneNotSupportedException _e) {
				LOGGER.error("The work piece could not be cloned - " + _o, _e);
				throw new IllegalStateException("The work piece could not be cloned - " + _o);
			}
		}
		Collections.sort(criteria, ToStringBasedComparator.SINGLETON);

		if (LOGGER.isDebugEnabled()) {
			final StringBuffer _sb = new StringBuffer();

			for (final Iterator _i = criteria.iterator(); _i.hasNext();) {
				_sb.append("\n\t");
				_sb.append(_i.next());
			}
			LOGGER.debug("Criteria:\n" + _sb.toString());
			LOGGER.debug("END: Populating deadlock criteria.");
		}
	}

	/**
	 * Sets the type of slice to be generated by the slicer.
	 *
	 * @param theSliceType is the type of slice requested.  This has to be one of<code>XXX_SLICE</code> values defined in
	 * 		  this class.
	 *
	 * @throws IllegalArgumentException when the given slice type is illegal.
	 *
	 * @pre theSliceType != null
	 * @pre SLICE_TYPES.contains(theSliceType)
	 */
	public void setSliceType(final Object theSliceType) {
		if (!SLICE_TYPES.contains(sliceType)) {
			throw new IllegalArgumentException("The given slice type is not one of XXX_SLICE values defined in this class.");
		}

		sliceType = theSliceType;

		if (sliceType.equals(SlicingEngine.BACKWARD_SLICE)) {
			criteriaClosure = new DependenceClosure(new BackwardSliceDependenceClosure());
		} else if (sliceType.equals(SlicingEngine.FORWARD_SLICE)) {
			criteriaClosure = new DependenceClosure(new ForwardSliceDependenceClosure());
		} else if (sliceType.equals(SlicingEngine.COMPLETE_SLICE)) {
			criteriaClosure = new DependenceClosure(new CompleteSliceDependenceClosure());
		}
	}

	/**
	 * Sets the name of the tag to be used identify the elements of the AST belonging to the slice.
	 *
	 * @param tagName is the name of the tag
	 *
	 * @pre tagName != null
	 */
	public void setTagName(final String tagName) {
		collector.setTagName(tagName);
	}

	/**
	 * Initializes the slicing engine.
	 *
	 * @throws IllegalStateException when the tag name is not set.
	 */
	public void initialize() {
		if (collector.getTagName() == null) {
			final String _temp = "Please set the tag name before executing the engine.";
			LOGGER.fatal(_temp);
			throw new IllegalStateException(_temp);
		}
	}

	/**
	 * Resets internal data structures and removes all references to objects provided at initialization time. For other
	 * operations to be meaningful following a call to this method, the user should call <code>initialize</code> before
	 * calling any other methods.
	 */
	public void reset() {
		cgi = null;
		collector.reset();
		criteria.clear();
		method2params.clear();
		exitTransformedMethods.clear();

		// clear the work bag of slice criterion
		while (workbag.hasWork()) {
			final Object _work = workbag.getWork();
			((IPoolable) _work).returnToPool();
		}
	}

	/**
	 * Slices the system provided at initialization for the initialized criteria to generate the given type of slice..
	 */
	public void slice() {
		for (final Iterator _i = criteria.iterator(); _i.hasNext();) {
			final Object _crit = _i.next();
			workbag.addWorkNoDuplicates(_crit);
		}

		// we are assuming the mapping will capture the past-processed information to prevent processed criteria from 
		// reappearing.  
		while (workbag.hasWork()) {
			final Object _work = workbag.getWork();

			if (_work instanceof SliceExpr) {
				transformAndGenerateNewCriteriaForExpr((SliceExpr) _work);
			} else if (_work instanceof SliceStmt) {
				final SliceStmt _temp = (SliceStmt) _work;
				final SootMethod _sm = _temp.getOccurringMethod();
				transformAndGenerateNewCriteriaForStmt((Stmt) _temp.getCriterion(), _sm, _temp.isConsiderExecution());
			}
			((IPoolable) _work).returnToPool();
		}

		collector.completeSlicing();
	}

	/**
	 * Retrieves the basic block graph manager used in the engine.
	 *
	 * @return the basic block graph manager.
	 *
	 * @post result != null
	 */
	BasicBlockGraphMgr getBasicBlockGraphManager() {
		return bbgMgr;
	}

	/**
	 * Checks if the given called method's return points should be considered to generate new slice criterion. The callee
	 * should be marked as invoked or required before calling this method.
	 *
	 * @param callee is the method in question.
	 *
	 * @return <code>true</code> if method's return points of callee should be considered to generate new slice criterion;
	 * 		   <code>false</code>, otherwise.
	 */
	private boolean considerMethodExitForCriteriaGeneration(final SootMethod callee) {
		boolean _result = false;

		if (!exitTransformedMethods.contains(callee)) {
			exitTransformedMethods.add(callee);
			_result = true;
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Consider Method exit for method " + callee + " is " + _result);
		}

		return _result;
	}

	/**
	 * Generates new criteria based on the entities that influence or are influenced by the given statement as indicated by
	 * the given dependency analyses.
	 *
	 * @param stmt that will trigger the dependence.
	 * @param method in which <code>stmt</code> occurs.
	 * @param das is a collection of dependency analyses.
	 *
	 * @pre stmt != null and method != null and das != null
	 * @pre das.oclIsKindOf(Collection(AbstractDependencyAnalysis))
	 * @post workbag$pre.getWork() != workbag.getWork() or workbag$pre.getWork() == workbag.getWork()
	 */
	private void generateNewCriteria(final Stmt stmt, final SootMethod method, final Collection das) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Generating Criteria based on dependences");
		}

		criteriaClosure.setTrigger(stmt, method);
		CollectionUtils.forAllDo(das, criteriaClosure);

		for (final Iterator _i = criteriaClosure.getCriteriaMap().entrySet().iterator(); _i.hasNext();) {
			final Map.Entry _entry = (Map.Entry) _i.next();
			final boolean _considerExecution = ((Boolean) _entry.getKey()).booleanValue();

			for (final Iterator _j = ((Collection) _entry.getValue()).iterator(); _j.hasNext();) {
				final Object _o = _j.next();
				final Stmt _stmtToBeIncluded;
				final SootMethod _methodToBeIncluded;

				if (_o instanceof Pair) {
					final Pair _pair = (Pair) _o;
					_stmtToBeIncluded = (Stmt) _pair.getFirst();
					_methodToBeIncluded = (SootMethod) _pair.getSecond();
				} else {
					_stmtToBeIncluded = (Stmt) _o;
					_methodToBeIncluded = method;
				}
				generateSliceStmtCriterion(_stmtToBeIncluded, _methodToBeIncluded, _considerExecution);
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Generating Criteria based on dependences");
		}
	}

	/**
	 * Generates new slice criteria for return points of the method called at the given expression.
	 *
	 * @param invocationStmt is the statment containing the invocation.
	 * @param caller is the method in which <code>invocationStmt</code> occurs.
	 * @param considerReturnValue indicates if the return value of the statement should be considered to generate slice
	 * 		  criterion.
	 * @param callee is the method being invoked.
	 * @param calleeBasicBlockGraph is the basic block graph of the callee.
	 *
	 * @pre invocationStmt !=  null and caller != null and callee != null and calleeBasicBlockGraph != null
	 */
	private void generateNewCriteriaBasedOnMethodExit(final Stmt invocationStmt, final SootMethod caller,
		final boolean considerReturnValue, final SootMethod callee, final BasicBlockGraph calleeBasicBlockGraph) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Generating criteria for method called at " + invocationStmt + " in " + caller + "["
				+ considerReturnValue + "]");
		}

		// check if a criteria to consider the exit points of the method should be generated.
		if (considerMethodExitForCriteriaGeneration(callee)) {
			if (callee.isConcrete()) {
				processSuperInitInInit(callee, calleeBasicBlockGraph);

				for (final Iterator _j = calleeBasicBlockGraph.getTails().iterator(); _j.hasNext();) {
					final BasicBlock _bb = (BasicBlock) _j.next();
					final Stmt _trailer = _bb.getTrailerStmt();

					// TODO: we are considering both throws and returns as return points. This should change when we consider 
					// if control-flow based on exceptions.
					generateSliceStmtCriterion(_trailer, callee, considerReturnValue);
				}
			} else {
				includeMethodAndDeclaringClassInSlice(callee);

				// HACK: to suck in arguments to a native method.
				final InvokeExpr _expr = invocationStmt.getInvokeExpr();

				for (int _i = _expr.getArgCount() - 1; _i >= 0; _i--) {
					generateSliceExprCriterion(_expr.getArgBox(_i), invocationStmt, caller, true);
				}
			}
		} else {
			/*
			 * if not, then check if any of the method parameters are marked as required.  If so, include them.
			 * It is possible that the return statements are not affected by the parameters in which case _params will be
			 * null.  On the other hand, may be the return statements have been included but not yet processed in which case
			 * _params will be null again.  In the latter case, we postpone for callee-caller propogation to generate
			 * criteria to consider suitable argument expressions.
			 */
			final BitSet _params = (BitSet) method2params.get(callee);
			final InvokeExpr _invokeExpr = invocationStmt.getInvokeExpr();

			if (_params != null && callee.getParameterCount() > 0) {
				for (int _j = _params.nextSetBit(0); _j >= 0; _j = _params.nextSetBit(_j + 1)) {
					generateSliceExprCriterion(_invokeExpr.getArgBox(_j), invocationStmt, caller, true);
				}
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Generating criteria for method called at " + invocationStmt + " in " + caller);
		}
	}

	/**
	 * Generates new slice criteria based on what affects the given occurrence of the invoke expression (caller-callee).  By
	 * nature of Jimple, only one invoke expression can occur in a statement, hence, the arguments.
	 *
	 * @param stmt in which the field occurs.
	 * @param method in which <code>stmt</code> occurs.
	 * @param considerReturnValue indicates if the return value expression should be considered for the slice.
	 *
	 * @pre stmt != null and method != null
	 * @pre stmt.containsInvokeExpr() == true
	 */
	private void generateNewCriteriaForInvokeExprIn(final Stmt stmt, final SootMethod method,
		final boolean considerReturnValue) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Generating criteria for invocation expressions (caller-callee)");
		}

		final InvokeExpr _expr = stmt.getInvokeExpr();
		final SootMethod _sm = _expr.getMethod();

		if (_sm.isStatic()) {
			generateNewCriteriaForReturnPointOfMethods(Collections.singleton(_sm), stmt, method, considerReturnValue);
		} else {
			final Context _context = new Context();
			_context.setRootMethod(method);
			_context.setStmt(stmt);

			final Collection _callees = cgi.getCallees(_expr, _context);
			generateNewCriteriaForReturnPointOfMethods(_callees, stmt, method, considerReturnValue);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Generating criteria for invocation expressions (caller-callee)");
		}
	}

	/**
	 * Generates criteria for locals based on identifier based data dependence.
	 *
	 * @param locals for which criteria should be generated.
	 * @param stmt in which <code>locals</code> occurs.
	 * @param method in which <code>stmt</code> occurs.
	 *
	 * @pre locals != null and locals.oclIsKindOf(Collection(Local))
	 * @pre stmt != null and method != null
	 */
	private void generateNewCriteriaForLocal(final Collection locals, final Stmt stmt, final SootMethod method) {
		final Collection _analyses = controller.getAnalyses(IDependencyAnalysis.IDENTIFIER_BASED_DATA_DA);

		if (_analyses.size() > 0) {
			final Collection _temp = new HashSet();
			final IDependencyAnalysis _analysis = (IDependencyAnalysis) _analyses.iterator().next();
			final Iterator _k = locals.iterator();
			final int _kEnd = locals.size();

			for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
				final Local _local = (Local) _k.next();
				final Pair _pair = new Pair(stmt, _local);

				if (sliceType.equals(BACKWARD_SLICE)) {
					_temp.addAll(_analysis.getDependees(_pair, method));
				} else if (sliceType.equals(FORWARD_SLICE)) {
					_temp.addAll(_analysis.getDependents(_pair, method));
				} else if (sliceType.equals(COMPLETE_SLICE)) {
					_temp.addAll(_analysis.getDependees(_pair, method));
					_temp.addAll(_analysis.getDependents(_pair, method));
				}
			}

			final Iterator _j = _temp.iterator();
			final int _jEnd = _temp.size();

			for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
				final Stmt _stmt = (Stmt) _j.next();
				generateSliceStmtCriterion(_stmt, method, true);
			}
		}
	}

	/**
	 * Generates new slicing criteria which captures inter-procedural dependences due to call-sites.
	 *
	 * @param pBox is the parameter reference to be sliced on.
	 * @param callee in which<code>pBox</code> occurs.
	 *
	 * @pre pBox != null and method != null
	 */
	private void generateNewCriteriaForParam(final ValueBox pBox, final SootMethod callee) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Generating criteria for parameters");
		}

		/*
		 * Note that this will cause us to include all caller sites as slicing criteria and this is not desired.
		 */
		final ParameterRef _param = (ParameterRef) pBox.getValue();
		final int _index = _param.getIndex();

		BitSet _params = (BitSet) method2params.get(callee);

		if (_params == null) {
			// we size the bitset on a maximum sane value for arguments to improve efficiency. 
			final int _maxArguments = 8;
			_params = new BitSet(_maxArguments);
			method2params.put(callee, _params);
		}
		_params.set(_param.getIndex());

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Parameters required for " + callee + " are " + _params);
		}

		for (final Iterator _i = cgi.getCallers(callee).iterator(); _i.hasNext();) {
			final CallTriple _ctrp = (CallTriple) _i.next();
			final SootMethod _caller = _ctrp.getMethod();
			final Stmt _stmt = _ctrp.getStmt();
			final ValueBox _argBox = _ctrp.getExpr().getArgBox(_index);

			generateSliceExprCriterion(_argBox, _stmt, _caller, true);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Generating criteria for parameters");
		}
	}

	/**
	 * Generates new slice criterion for the return points of methods.
	 *
	 * @param callees are the set of methods being called at <code>invocationStmt</code>.
	 * @param invocationStmt contains the invocation site.
	 * @param caller contains <code>invocationStmt</code>.
	 * @param considerReturnValue indicates if the return value expression should be considered for the slice.
	 *
	 * @pre callees != null and invocationStmt != null and caller != null
	 */
	private void generateNewCriteriaForReturnPointOfMethods(final Collection callees, final Stmt invocationStmt,
		final SootMethod caller, final boolean considerReturnValue) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Generating criteria for return points " + considerReturnValue);
		}

		// add exit points of callees as the slice criteria
		for (final Iterator _i = callees.iterator(); _i.hasNext();) {
			final SootMethod _callee = (SootMethod) _i.next();
			final BasicBlockGraph _bbg = bbgMgr.getBasicBlockGraph(_callee);

			/*
			 * we do not want to include a dependence on return statement in java.lang.Thread.start() method
			 * as it will occur in a different thread and cannot affect the sequential flow of control in the current thread.
			 */
			if (_callee.getName().equals("start")
				  && _callee.getDeclaringClass().getName().equals("java.lang.Thread")
				  && _callee.getReturnType().equals(VoidType.v())
				  && _callee.getParameterCount() == 0) {
				continue;
			}

			if (_bbg == null) {
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("No basic block graph available for " + _callee.getSignature() + ". Moving on.");
				}
				continue;
			}

			generateNewCriteriaBasedOnMethodExit(invocationStmt, caller, considerReturnValue, _callee, _bbg);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Generating criteria for return points");
		}
	}

	/**
	 * Generates new slice criterion that captures the sites that invoke the given method.
	 *
	 * @param callee is the method whose calling sites needs to be included in the slice.
	 *
	 * @pre callee != null
	 */
	private void generateNewCriteriaForTheCallToMethod(final SootMethod callee) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Generating criteria for call-sites (callee-caller) " + callee);
		}

		// generate criteria to include invocation sites only if the method has not been collected.
		if (!collector.hasBeenCollected(callee)) {
			includeMethodAndDeclaringClassInSlice(callee);

			final boolean _notStatic = !callee.isStatic();

			for (final Iterator _i = cgi.getCallers(callee).iterator(); _i.hasNext();) {
				final CallTriple _ctrp = (CallTriple) _i.next();
				final SootMethod _caller = _ctrp.getMethod();
				final Stmt _stmt = _ctrp.getStmt();

				/*
				 * _stmt may be an assignment statement.  Hence, we want the control to reach the statement but not leave
				 * it.  However, the execution of the invoke expression should be considered as it is requied to reach the
				 * callee.  Likewise, we want to include the expression but not all arguments.  We rely on the reachable
				 * parameters to suck in the arguments.  So, we generate criteria only for the invocation expression and
				 * not the arguments.  Refer to transformAndGenerateToNewCriteriaForXXXX for information about how
				 * invoke expressions are handled differently.
				 */
				generateSliceStmtCriterion(_stmt, _caller, false);
				collector.includeInSlice(_stmt.getInvokeExprBox());

				if (_notStatic) {
					final ValueBox _vBox = ((InstanceInvokeExpr) _stmt.getInvokeExpr()).getBaseBox();
					generateSliceExprCriterion(_vBox, _stmt, _caller, true);
				}
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Generating criteria for call-sites (callee-caller)");
		}
	}

	/**
	 * Generates slice criterion for teh given program point.
	 *
	 * @param valueBox is the program point for which slice criterion should be generated.
	 * @param stmt is the statement containing <code>valueBox</code>.
	 * @param method is the method containing <code>stmt</code>.
	 * @param considerExecution indicates if the execution of the program point should be considered or just the control
	 * 		  reaching it.
	 *
	 * @pre valueBox != null and stmt != null and method != null
	 */
	private void generateSliceExprCriterion(final ValueBox valueBox, final Stmt stmt, final SootMethod method,
		final boolean considerExecution) {
		if (!collector.hasBeenCollected(valueBox)) {
			final Collection _sliceCriteria =
				SliceCriteriaFactory.getFactory().getCriteria(method, stmt, valueBox, considerExecution);
			workbag.addAllWorkNoDuplicates(_sliceCriteria);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Adding expr [" + considerExecution + "] " + valueBox.getValue() + " at " + stmt + " in "
					+ method.getSignature() + " to workbag.");
			}
		} else {
			if (valueBox.getValue() instanceof InvokeExpr) {
				generateNewCriteriaForInvokeExprIn(stmt, method, considerExecution);
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Already collected expr " + valueBox.getValue() + " occurring at " + stmt + " in "
					+ method.getSignature());
			}
		}
	}

	/**
	 * Generates slice criterion for the given statement.
	 *
	 * @param stmt is the statement.
	 * @param method is the method containing <code>stmt</code>.
	 * @param considerExecution indicates if the execution of the statement should be considered or just the control reaching
	 * 		  it.
	 *
	 * @pre stmt != null and method != null
	 */
	private void generateSliceStmtCriterion(final Stmt stmt, final SootMethod method, final boolean considerExecution) {
		if (!collector.hasBeenCollected(stmt)) {
			final Collection _sliceCriteria = SliceCriteriaFactory.getFactory().getCriteria(method, stmt, considerExecution);
			workbag.addAllWorkNoDuplicates(_sliceCriteria);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Adding [" + considerExecution + "] " + stmt + " in " + method.getSignature() + " to workbag.");
			}
		} else {
			if (considerExecution) {
				final Collection _temp = new HashSet(stmt.getUseAndDefBoxes());

				// if it contains an invocation expression, we do not want to include the arguments/sub-expressions.
				if (stmt.containsInvokeExpr()) {
					final InvokeExpr _invokeExpr = stmt.getInvokeExpr();
					_temp.removeAll(_invokeExpr.getUseBoxes());

					// in case of instance invocation, we do want to include the receiver position expression.
					if (_invokeExpr instanceof InstanceInvokeExpr) {
						_temp.add(((InstanceInvokeExpr) _invokeExpr).getBaseBox());
					}
				}

				for (final Iterator _i = _temp.iterator(); _i.hasNext();) {
					final ValueBox _valueBox = (ValueBox) _i.next();
					generateSliceExprCriterion(_valueBox, stmt, method, considerExecution);
				}
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Already collected stmt " + stmt + " in " + method.getSignature());
			}
		}
	}

	/**
	 * Includes the given class in the slice.
	 *
	 * @param clazz to be included in the slice.
	 *
	 * @pre clazz != null
	 */
	private void includeClassInSlice(final SootClass clazz) {
		collector.includeInSlice(clazz);
	}

	/**
	 * Includes the given method and it's declaring class in the slice.
	 *
	 * @param method to be included in the slice.
	 *
	 * @pre method != null
	 */
	private void includeMethodAndDeclaringClassInSlice(final SootMethod method) {
		collector.includeInSlice(method);

		final SootClass _sc = method.getDeclaringClass();
		includeClassInSlice(_sc);

		final Collection _types = new HashSet(method.getParameterTypes());
		_types.add(method.getReturnType());
		includeTypesInSlice(_types);
	}

	/**
	 * Includes the class associated with the given types.
	 *
	 * @param types to be included in the slice.
	 *
	 * @pre types != null and types.oclIsKindOf(Collection(Type))
	 */
	private void includeTypesInSlice(final Collection types) {
		for (final Iterator _i = types.iterator(); _i.hasNext();) {
			final Type _type = (Type) _i.next();

			if (_type instanceof RefType) {
				includeClassInSlice(((RefType) _type).getSootClass());
			} else if (_type instanceof ArrayType && ((ArrayType) _type).baseType instanceof RefType) {
				includeClassInSlice(((RefType) ((ArrayType) _type).baseType).getSootClass());
			}
		}
	}

	/**
	 * Processes new expressions to include corresponding init statements into the slice.
	 *
	 * @param stmt is the statement containing the new expression.
	 * @param method contains <code>stmt</code>.
	 *
	 * @pre stmt != null and method != null
	 */
	private void processForNewExpr(final Stmt stmt, final SootMethod method) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Processing for new expr");
		}

		/*
		 * Here we make some assumptions.  new expressions will always be assigned to a variable in order to call the
		 * constructor. Hence, they will always occur in assignment statement.  It is common practice in compilers to emit
		 * code to construct the instance just after emitting for creating the instance.  Hence, we should be able to find the
		 * <init> call site by following use-def chain. However, this may fail in case where the <init> call is made on
		 * a variable other than the one to which the new expression was assigned to.
		 *     r1 = new <X>;
		 *     r2 = r1;
		 *     r2.<init>();
		 * To handle such cases, we use OFA.
		 *
		 */
		if (stmt instanceof AssignStmt) {
			final AssignStmt _as = (AssignStmt) stmt;

			if (_as.getRightOp() instanceof NewExpr) {
				final Stmt _def = initMapper.getInitCallStmtForNewExprStmt(stmt, method);
				transformAndGenerateNewCriteriaForStmt(_def, method, true);
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Processing for new expr");
		}
	}

	/**
	 * Processes the init call to the super class inside init method.
	 *
	 * @param callee is the init method.
	 * @param bbg is the basic block graph of <code>callee</code>.
	 *
	 * @pre callee != null and bbg != null
	 */
	private void processSuperInitInInit(final SootMethod callee, final BasicBlockGraph bbg) {
		/*
		 * if we are sucking in an init we better suck in the super <init> invoke expression as well. By JLS, this has to
		 * be the first statement in the constructor.  However, if it accepts arguments, the arguments will be set up
		 * before the call.  Hence, it is safe to suck in the first <init> invoke expression in the <init> method being
		 * sucked in.  However, care must be taken to suck in the first <init> invocation that is invokes <init> from the same
		 * class as the enclosing <init> method. As we process invocation expressions, we are bound to suck in any other
		 * required <init>'s from other higher super classes.
		 */
		if (callee.getName().equals("<init>") && callee.getDeclaringClass().hasSuperclass()) {
			final CompleteUnitGraph _ug = new CompleteUnitGraph(callee.getActiveBody());
			final SimpleLocalUses _sul = new SimpleLocalUses(_ug, new SimpleLocalDefs(_ug));
			final List _uses = _sul.getUsesOf(bbg.getHead().getLeaderStmt());

			for (final Iterator _i = _uses.iterator(); _i.hasNext();) {
				final UnitValueBoxPair _ubp = (UnitValueBoxPair) _i.next();
				final Stmt _stmt = (Stmt) _ubp.getUnit();

				if (_stmt instanceof InvokeStmt) {
					final SootMethod _called = _stmt.getInvokeExpr().getMethod();

					if (_called.getName().equals("<init>")
						  && _called.getDeclaringClass().equals(callee.getDeclaringClass().getSuperclass())) {
						generateSliceStmtCriterion(_stmt, callee, true);
					}
				}
			}
		}
	}

	/**
	 * Generates immediate slice for the given expression.
	 *
	 * @param expr is the expression-level slice criterion.
	 *
	 * @pre expr != null and expr.getOccurringStmt() != null and expr.getOccurringMethod() != null
	 * @pre expr.getCriterion() != null and expr.getCriterion().oclIsKindOf(ValueBox)
	 */
	private void transformAndGenerateNewCriteriaForExpr(final SliceExpr expr) {
		final Stmt _stmt = expr.getOccurringStmt();
		final SootMethod _method = expr.getOccurringMethod();
		final ValueBox _vBox = (ValueBox) expr.getCriterion();
		final Value _value = _vBox.getValue();
		final boolean _considerExecution = expr.isConsiderExecution();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Transforming expr criteria: " + _vBox.getValue() + "[" + _considerExecution + "] at "
				+ _stmt + " in " + _method);
		}

		// include the statement to capture control dependency and generate criteria from it. Remember to collect it.
		transformAndGenerateNewCriteriaForStmt(_stmt, _method, false);

		// generate new slice criteria
		if (_considerExecution) {
			final Collection _temp = new HashSet();
			_temp.add(_vBox);

			// if it is an invocation expression, we do not want to include the arguments/sub-expressions. 
			// in case of instance invocation, we do want to include the receiver position expression.
			if (!(_value instanceof InvokeExpr)) {
				_temp.addAll(_value.getUseBoxes());
			} else if (_value instanceof InstanceInvokeExpr) {
				_temp.add(((InstanceInvokeExpr) _value).getBaseBox());
			}

			// include any sub expressions and generate criteria from them
			transformAndGenerateToConsiderExecution(_stmt, _method, _temp);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Transforming expr criteria: " + _vBox.getValue() + " at " + _stmt + " in " + _method);
		}
	}

	/**
	 * Transforms the given statement and generates new criteria.  The given statement is only collected if
	 * <code>considerExecution</code> is <code>true</code>.
	 *
	 * @param stmt is the statement-level slice criterion.
	 * @param method is the method in which <code>stmt</code> occurs.
	 * @param considerExecution <code>true</code> indicates that the effect of executing this criterion should be considered
	 * 		  while slicing.  This means all the expressions of the associated statement are also considered as slice
	 * 		  criteria. <code>false</code> indicates that just the mere effect of the control reaching this criterion should
	 * 		  be considered while slicing.  This means none of the expressions of the associated statement are considered as
	 * 		  slice criteria.
	 */
	private void transformAndGenerateNewCriteriaForStmt(final Stmt stmt, final SootMethod method,
		final boolean considerExecution) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Transforming stmt criteria: " + stmt + "[" + considerExecution + "] in " + method);
		}

		// transform the statement
		if (considerExecution) {
			// generate new slice criteria
			processForNewExpr(stmt, method);

			final Collection _temp = new HashSet(stmt.getUseAndDefBoxes());

			// if it contains an invocation expression, we do not want to include the arguments/sub-expressions.
			if (stmt.containsInvokeExpr()) {
				final InvokeExpr _invokeExpr = stmt.getInvokeExpr();
				_temp.removeAll(_invokeExpr.getUseBoxes());

				// in case of instance invocation, we do want to include the receiver position expression.
				if (_invokeExpr instanceof InstanceInvokeExpr) {
					_temp.add(((InstanceInvokeExpr) _invokeExpr).getBaseBox());
				}
			}
			transformAndGenerateToConsiderExecution(stmt, method, _temp);

			if (stmt.containsInvokeExpr()) {
				generateNewCriteriaForInvokeExprIn(stmt, method, stmt instanceof AssignStmt);
			}
		}

		// generate new slice criteria
		generateNewCriteriaForTheCallToMethod(method);
		generateNewCriteria(stmt, method, controlflowBasedDAs);
		includeMethodAndDeclaringClassInSlice(method);

		// collect the statement
		collector.includeInSlice(stmt);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Transforming stmt criteria: " + stmt + "[" + considerExecution + "] in " + method);
		}
	}

	/**
	 * Transforms the given value boxes and generates criteria that are required to consider execution.
	 *
	 * @param stmt in which the value boxes occur.
	 * @param method in which <code>stmt</code> occurs.
	 * @param vBoxes are the ValueBoxesto be transformed.
	 *
	 * @pre stmt != null and method != null and vBoxes != null
	 * @pre vBoxes.oclIsKindOf(Collection(ValueBox))
	 * @pre stmt.getUseAndDefBoxes().containsAll(valueBoxes)
	 */
	private void transformAndGenerateToConsiderExecution(final Stmt stmt, final SootMethod method, final Collection vBoxes) {
		if (LOGGER.isDebugEnabled()) {
			final StringBuffer _sb = new StringBuffer();
			_sb.append("BEGIN: Transforming value boxes [");

			for (final Iterator _i = vBoxes.iterator(); _i.hasNext();) {
				final ValueBox _vBox = (ValueBox) _i.next();
				_sb.append(_vBox.getValue());
				_sb.append("[" + _vBox + "]");
				_sb.append(", ");
			}
			_sb.append("]");
			LOGGER.debug(_sb.toString());
		}

		final Collection _types = new HashSet();
		final Collection _das = new ArrayList();
		final Collection _locals = new HashSet();

		for (final Iterator _i = vBoxes.iterator(); _i.hasNext();) {
			final ValueBox _vBox = (ValueBox) _i.next();

			if (!collector.hasBeenCollected(_vBox)) {
				collector.includeInSlice(_vBox);

				final Value _value = _vBox.getValue();

				if (_value instanceof ParameterRef) {
					generateNewCriteriaForParam(_vBox, method);
					generateNewCriteriaForTheCallToMethod(method);
				} else if (_value instanceof FieldRef || _value instanceof ArrayRef) {
					_das.addAll(controller.getAnalyses(IDependencyAnalysis.REFERENCE_BASED_DATA_DA));
					_das.addAll(controller.getAnalyses(IDependencyAnalysis.INTERFERENCE_DA));

					if (_value instanceof FieldRef) {
						final SootField _field = ((FieldRef) _vBox.getValue()).getField();
						collector.includeInSlice(_field);
						includeClassInSlice(_field.getDeclaringClass());
					}
				} else if (_value instanceof Local) {
					_locals.add(_value);
				}
				_types.add(_value.getType());
			}
		}
		includeTypesInSlice(_types);

		// create new slice criteria based on statement level dependence.
		generateNewCriteria(stmt, method, _das);

		// create new criteria based on program point level dependence (identifier based dependence).
		generateNewCriteriaForLocal(_locals, stmt, method);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Transforming value boxes");
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.87  2004/07/25 01:36:44  venku
   - changed the way invoke expression arguments are handled when transforming
     statements and expressions while considering execution.
   Revision 1.86  2004/07/23 13:10:06  venku
   - Refactoring in progress.
     - Extended IMonitorInfo interface.
     - Teased apart the logic to calculate monitor info from SynchronizationDA
       into MonitorAnalysis.
     - Casted EquivalenceClassBasedEscapeAnalysis as an AbstractAnalysis.
     - ripple effect.
     - Implemented safelock analysis to handle intraprocedural processing.
   Revision 1.85  2004/07/21 07:26:56  venku
   - statements were not tagged due to execution consideration. FIXED.
   Revision 1.84  2004/07/20 07:04:36  venku
   - changes to accomodate changes to directions of dependence analyses.
   Revision 1.83  2004/07/09 05:05:25  venku
   - refactored the code to enable the criteria creation to be completely hidden
     from the user.
   - exposed the setting of the considerExecution flag of the criteria in the factory.
   - made SliceCriteriaFactory a singleton.
   Revision 1.82  2004/07/08 11:08:13  venku
   - refactoring to remove redundance.
   Revision 1.81  2004/06/26 10:16:35  venku
   - bug #389. FIXED.
   Revision 1.80  2004/06/14 04:32:11  venku
   - removed an unnecessary method invocation.
   Revision 1.79  2004/06/14 02:57:23  venku
   - documentation.
   Revision 1.78  2004/06/13 07:31:22  venku
   - documentation.
   Revision 1.77  2004/06/12 06:47:27  venku
   - documentation.
   - refactoring.
   - coding conventions.
   - catered feature request 384, 385, and 386.
   Revision 1.76  2004/05/31 21:38:10  venku
   - moved BasicBlockGraph and BasicBlockGraphMgr from common.graph to common.soot.
   - ripple effect.
   Revision 1.75  2004/05/14 09:02:57  venku
   - refactored:
     - The ids are available in IDependencyAnalysis, but their collection is
       available via a utility class, DependencyAnalysisUtil.
     - DependencyAnalysis will have a sanity check via Unit Tests.
   - ripple effect.
   Revision 1.74  2004/05/14 06:27:21  venku
   - renamed DependencyAnalysis as AbstractDependencyAnalysis.
   Revision 1.73  2004/03/03 10:09:41  venku
   - refactored code in ExecutableSlicePostProcessor and TagBasedSliceResidualizer.
   Revision 1.72  2004/02/23 04:40:51  venku
   - does a naive tracking of the call chain back to the roots.
   Revision 1.71  2004/02/13 08:39:38  venku
   - reafactored code related to marking to facilitate
     ease of tracking updates to required and invoked.
   Revision 1.70  2004/02/06 00:10:11  venku
   - optimization and logging.
   Revision 1.69  2004/02/04 02:02:35  venku
   - coding convention and formatting.
   - the logic to include the return values in return statement was
     being determined while generating the criteria. This is bad design. FIXED.
   Revision 1.68  2004/02/01 23:36:59  venku
   - used INewExpr2InitMapper instead of NewExpr2InitMapper
     to improve configrurability.
   Revision 1.67  2004/02/01 22:16:16  venku
   - renamed set/getSlicedBBGMgr to set/getBasicBlockGraphManager
     in SlicingEngine.
   - ripple effect.
   Revision 1.66  2004/01/31 01:50:21  venku
   - logging.
   Revision 1.65  2004/01/30 18:25:58  venku
   - logging.
   Revision 1.64  2004/01/27 01:46:50  venku
   - coding convention.
   Revision 1.63  2004/01/25 08:53:37  venku
   - changed generateNewCritieria to be more modularized.
   - method only trigger the inclusion of the base
     and the invoke expressions at the call site and rely on the
     parameter ref handling to suck in the arguments.
   - optimized criteria generation by not including values of
     exit points of in synchronized methods.
   Revision 1.62  2004/01/22 12:42:21  venku
   - logging.
   Revision 1.61  2004/01/22 01:01:40  venku
   - coding convention.
   Revision 1.60  2004/01/21 02:37:51  venku
   - logging.
   Revision 1.59  2004/01/20 17:32:28  venku
   - logging.
   Revision 1.58  2004/01/20 17:16:45  venku
   - coding convention.
   - renamed includeClassHierarchyInSlice to includeClassInSlice.
   - similarly, renamed includeMethodAndClassHierarchyInSlice.
   Revision 1.57  2004/01/20 16:49:39  venku
   - ready dependence was added to controlbased da's and useReady
     was deleted.
   Revision 1.56  2004/01/20 00:46:36  venku
   - criteria are sorted in SlicingEngine instead of SlicerTool.
   - formatting and logging.
   Revision 1.55  2004/01/19 23:54:21  venku
   - coding convention.
   Revision 1.54  2004/01/19 23:53:44  venku
   - moved the logic to order criteria to enforce pseudo-determinism
     during slicing into SlicingEngine.
   Revision 1.53  2004/01/19 22:55:11  venku
   - formatting and coding convention.
   Revision 1.52  2004/01/19 22:52:49  venku
   - inclusion of method declaration with identical signature in the
     super classes/interfaces is a matter of executability.  Hence,
     this is now deferred to ExecutableSlicePostProcessor.  The
     ramifications are each method is processed in
     ExecutablePostProcessor to include methods in super classes;
     only the called methods, it's declaring class
     are included in the slice in SlicingEngine.
   - renamed generateNewCriteriaForCallToEnclosingMethod() to
     generateNewCriteriaForCallToMethod()
   -
   Revision 1.51  2004/01/19 12:23:09  venku
   - optimized includeClassHierarchy() and includeMethodAndClassHierarchy() methods.
   Revision 1.50  2004/01/14 12:04:14  venku
   - we check if the statement is collected to optimized.  However,
     a statement may be collected but not all parts of it and a later
     request to collect all of it will not include unincluded parts
     into the slice due to check. FIXED.
   Revision 1.49  2004/01/13 23:25:04  venku
   - documentation.
   Revision 1.48  2004/01/13 10:03:31  venku
   - documentation.
   Revision 1.47  2004/01/13 04:33:39  venku
   - Renamed TaggingBasedSliceCollector to SliceCollector.
   - Ripple effect in the engine.
   - SlicingEngine does not handle issues such as executability
     as they do not affect the generated slice.  The slice can be
     transformed independent of the slice via postprocessing to
     adhere to such properties.
   Revision 1.46  2004/01/11 03:42:22  venku
   - synchronization, control, and divergence are now considered as
     control based DAs and are used instead of just control.
   - renamed controlDAs to controlBasedDAs.
   - while considering invocations,
     - static invocations do not use call graph.
     - for executable slices, we tag methods in declaring
       class to keep the type hierarchy correct.
   Revision 1.45  2004/01/06 00:17:05  venku
   - Classes pertaining to workbag in package indus.graph were moved
     to indus.structures.
   - indus.structures was renamed to indus.datastructures.
   Revision 1.44  2003/12/16 12:46:03  venku
   - changed the way return points of native method are
     handled.
   - changed the way class hierarchies are included into the slice.
   Revision 1.43  2003/12/16 00:48:14  venku
   - optimization.
   Revision 1.42  2003/12/16 00:46:23  venku
   - included super classes/interfaces and methods in them
     when a method with the same signature in the subclass
     is included.
   Revision 1.41  2003/12/16 00:22:53  venku
   - when we include invoke expressions leading to the
     enclosed method (callee-caller), we need to consider
     their execution to reach the callee.
   - logging.
   Revision 1.40  2003/12/15 16:34:12  venku
   - teased out the logic to suck in the class initializers.
   - removed includeInSlice as it was not used.
   - added method includeClassHierarchyInSlice.
   Revision 1.39  2003/12/15 08:09:53  venku
   - safety check.
   Revision 1.38  2003/12/15 08:09:09  venku
   - incorrect way to detect super calls in init. FIXED.
   - incorrect approximation when generating criteria
     based on return points of method.  FIXED.
   Revision 1.37  2003/12/13 20:54:27  venku
   - it is possible that none of the parameters are used.
     In such cases, _params will be null in generateNewCriteriaForMethodExit()
     which is incorrect.  FIXED.
   Revision 1.36  2003/12/13 20:52:33  venku
   - documentation.
   Revision 1.35  2003/12/13 19:52:41  venku
   - renamed Init2NewExprMapper to NewExpr2InitMapper.
   - ripple effect.
   Revision 1.34  2003/12/13 19:46:33  venku
   - documentation of SliceCollector.
   - renamed collect() to includeInSlice().
   Revision 1.33  2003/12/13 02:29:16  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.32  2003/12/09 04:22:14  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.31  2003/12/08 12:20:48  venku
   - moved some classes from staticanalyses interface to indus interface package
   - ripple effect.
   Revision 1.30  2003/12/08 12:16:05  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.29  2003/12/07 22:13:12  venku
   - renamed methods in SliceCollector.
   Revision 1.28  2003/12/07 15:16:01  venku
   - the order of collecting the slice had an impact on the
     generation of the slice (due to optimization).  For this
     reason the enclosing method should be collected after
     the criteria are generated.
   Revision 1.27  2003/12/05 15:33:35  venku
   - more logging and logic to handle inter procedural slicing.
     Getting there.
   Revision 1.26  2003/12/04 12:10:12  venku
   - changes that take a stab at interprocedural slicing.
   Revision 1.25  2003/12/02 09:42:17  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.24  2003/12/02 01:30:50  venku
   - coding conventions and formatting.
   Revision 1.23  2003/12/01 13:52:21  venku
   - utilizes pooling support.
   Revision 1.22  2003/12/01 12:22:09  venku
   - lots of changes to get it working.  Not yet there, but getting there.
   Revision 1.21  2003/11/30 13:24:30  venku
   - amidst changes.
    - control da is used while slicing statements only.
    - logging.
    - corrected logic to suck in return points, call-sites,
      new-<init> pairs, and <clinit>s.
   Revision 1.20  2003/11/28 18:16:58  venku
   - a hack and an optimization.
   Revision 1.19  2003/11/26 10:14:56  venku
   - refactored method to suck in <clinit> method dependence.
   - included logic to suck in <clinit> dependences.
   Revision 1.18  2003/11/25 00:00:45  venku
   - added support to include gotos in the slice.
   - added logic to include all tail points in the slice after slicing
     and only in case of backward executable slice.
   - added logic to include exceptions in a limited way.
   Revision 1.17  2003/11/24 18:20:25  venku
   - added partial support to handle exceptions during slicing.
   Revision 1.16  2003/11/24 16:46:15  venku
   - exposed certain variables which are part of the package
     rather than the class.
   Revision 1.15  2003/11/24 10:11:32  venku
   - there are no residualizers now.  There is a very precise
     slice collector which will collect the slice via tags.
   - architectural change. The slicer is hard-wired wrt to
     slice collection.  Residualization is outside the slicer.
   Revision 1.14  2003/11/24 09:46:49  venku
   - moved ISliceCollector and SliceCollector
     into slicer package.
   - The idea is to collect the slice based on annotation which
     can be as precise as we require and then layer on
     top of that the slicer residualization logic, either constructive or destructive.
   Revision 1.13  2003/11/24 01:21:00  venku
   - coding convention.
   Revision 1.12  2003/11/24 00:01:14  venku
   - moved the residualizers/transformers into transformation
     package.
   - Also, renamed the transformers as residualizers.
   - opened some methods and classes in slicer to be public
     so that they can be used by the residualizers.  This is where
     published interface annotation is required.
   - ripple effect of the above refactoring.
   Revision 1.11  2003/11/22 00:43:34  venku
   - split initialize() into many setter methods.
   - initialize() now just does sanity check on the runtime configuration
     of the engine.
   Revision 1.10  2003/11/20 08:22:37  venku
   - added support to include calls to <init> based on new expressions.
   - need to implement the class that provides this information.
   Revision 1.9  2003/11/17 02:23:52  venku
   - documentation.
   - xmlizers require streams/writers to be provided to them
     rather than they constructing them.
   Revision 1.8  2003/11/16 23:01:44  venku
   - exercises the support to process seed criteria.
   Revision 1.7  2003/11/13 14:08:08  venku
   - added a new tag class for the purpose of recording branching information.
   - renamed fixReturnStmts() to makeExecutable() and raised it
     into ISliceCollector interface.
   - ripple effect.
   Revision 1.6  2003/11/06 05:15:05  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.5  2003/11/05 09:33:39  venku
   - ripple effect of splitting Workbag.
   Revision 1.4  2003/11/05 08:34:40  venku
   - Slicing on single threaded and single procedure
     program works but does not terminate.  Now the
     slicing algorithm is just tracing dependence and
     creating new criteria along the way.
   Revision 1.3  2003/11/03 08:19:56  venku
   - Major changes
     value boxes are the atomic entities in a slice.
     code restructuring.
   Revision 1.2  2003/10/21 06:00:19  venku
   - Split slicing type into 2 sets:
        b/w, f/w, and complete
        executable and non-executable.
   - Extended transformer classes to handle these
     classification.
   - Added a new class to house the logic for fixing
     return statements in case of backward executable slice.
   Revision 1.1  2003/10/13 00:58:03  venku
   - empty log message
   Revision 1.16  2003/09/28 06:20:38  venku
   - made the core independent of hard code used to create unit graphs.
     The core depends on the environment to provide a factory that creates
     these unit graphs.
   Revision 1.15  2003/09/27 22:38:30  venku
   - package documentation.
   - formatting.
   Revision 1.14  2003/09/15 08:09:17  venku
   - fixed param dependency.  However, this needs to be addressed
     in a generic setting.  Also, the theoretics concerned to inclusion
     should be dealt appropriately.
   Revision 1.13  2003/08/21 10:25:08  venku
   - Now here is where things get interesting.  Inclusion has 2 meanings.
   - One is should the entity be included in the transformed system.
   - Two is shoudl the enclosed entities be considered for slicing.
   - In the process of setting this right.  Not fixed yet.  In fact fails to compile.
   Revision 1.12  2003/08/19 12:44:39  venku
   - Changed the signature of ITransformer.getLocal()
   - Introduced reset() in ITransformer.
   - Ripple effect of the above changes.
   Revision 1.11  2003/08/19 11:52:25  venku
   - The following renaming have occurred ITransformMap to ITransformer, SliceMapImpl to SliceTransformer,
     and  Slicer to SliceEngine.
   - Ripple effect of the above.
   Revision 1.10  2003/08/19 11:37:41  venku
   Major changes:
    - Changed ITransformMap extensively such that it now provides
      interface to perform the actual transformation.
    - Extended ITransformMap as AbstractTransformer to provide common
      functionalities.
    - Ripple effect of the above change in SlicerMapImpl.
    - Ripple effect of the above changes in Slicer.
    - The slicer now actually detects what needs to be included in the slice.
      Hence, it is more of an analysis/driver/engine that drives the transformation
      and SliceMapImpl is the engine that does or captures the transformation.
   - The immediate following change will be to rename ITransformMap to ITransformer,
     SliceMapImpl to SliceTransformer, and Slicer to SliceEngine.
   Revision 1.9  2003/08/18 12:14:13  venku
   - Well, to start with the slicer implementation is complete.
     Although not necessarily bug free, hoping to stabilize it quickly.
   Revision 1.8  2003/08/18 05:01:45  venku
   - Committing package name change in source after they were moved.
   Revision 1.7  2003/08/18 04:56:47  venku
   - Spruced up Documentation and specification.
    But committing before moving slicer under transformation umbrella of Indus.
   Revision 1.6  2003/05/22 22:23:49  venku
   - Changed interface names to start with a "I".
     Formatting.
 */
