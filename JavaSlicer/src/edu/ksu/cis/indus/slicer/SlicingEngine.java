
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

import edu.ksu.cis.indus.common.ToStringBasedComparator;
import edu.ksu.cis.indus.common.datastructures.FIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.PoolAwareWorkBag;
import edu.ksu.cis.indus.common.scoping.SpecificationBasedScopeDefinition;
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;
import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.interfaces.INewExpr2InitMapper;
import edu.ksu.cis.indus.interfaces.IPoolable;

import edu.ksu.cis.indus.processing.Context;

import edu.ksu.cis.indus.staticanalyses.AnalysesController;
import edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

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

import soot.jimple.ArrayRef;
import soot.jimple.FieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.ParameterRef;
import soot.jimple.Stmt;


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
	 * This collects the parts of the system that make up the slice.
	 */
	SliceCollector collector;

	/** 
	 * The controller used to access the dependency analysis info during slicing.
	 */
	private AnalysesController controller;

	/** 
	 * This is the basic block graph manager which manages the BB graphs corresponding to the system being sliced.
	 */
	private BasicBlockGraphMgr bbgMgr;

	/** 
	 * A collection of methods for which all call-sites have been collected.
	 *
	 * @invariant collectedAllInvocationSites.oclIsKindOf(Collection(SootMethod))
	 */
	private final Collection collectedAllInvocationSites = new HashSet();

	/** 
	 * The closure used to extract dependence information based on slice direction.  See <code>setSliceType()</code> for
	 * details.
	 */
	private final DependenceExtractor dependenceExtractor = new DependenceExtractor();

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
	 * This provides the logic to determine parts of the slice that are direction dependent.
	 */
	private IDirectionSensitivePartOfSlicingEngine directionSensitiveInfo;

	/** 
	 * The system being sliced.
	 */
	private IEnvironment system;

	/** 
	 * This maps new expressions to corresponding init call sites.
	 */
	private INewExpr2InitMapper initMapper;

	/** 
	 * The list of slice criteria.
	 *
	 * @invariant criteria != null and criteria->forall(o | o.oclIsKindOf(AbstractSliceCriterion))
	 */
	private List criteria = new ArrayList();

	/** 
	 * The direction of the slice.  It's default value is <code>BACKWARD_SLICE</code>.
	 *
	 * @invariant sliceTypes.contains(sliceType)
	 */
	private Object sliceType = BACKWARD_SLICE;

	/** 
	 * This predicate is used to filter out java.lang.Thread.start methods from a set of methods.
	 */
	private Predicate nonStartMethodPredicate = new NonStartMethodPredicate();

	/** 
	 * This defines the scope of slicing.  If this is <code>null</code>, then the entire system is considered as the scope.
	 */
	private SpecificationBasedScopeDefinition sliceScope;

	/** 
	 * This caches the call stack of the criteria currently being processed.  It will hold the call-site of the current
	 * method in which the processing is occurring.  This will NOT include the current method being processed at TOS unless
	 * there is recursion.
	 */
	private Stack callStackCache;

	/** 
	 * This caches the informaiton - is interference dependence being used in this execution?
	 */
	private boolean useInterferenceDACache;

	/**
	 * Creates a new SlicingEngine object.
	 */
	public SlicingEngine() {
		collector = new SliceCollector(this);
	}

	/**
	 * This predicate can be used to filter out java.lang.Thread.start methods from a set of methods.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	private static class NonStartMethodPredicate
	  implements Predicate {
		/**
		 * @see org.apache.commons.collections.Predicate#evaluate(java.lang.Object)
		 */
		public boolean evaluate(final Object object) {
			return !Util.isStartMethod((SootMethod) object);
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
	 * Sets the information that provides context retriever based on dependence analysis id.
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
		dependenceExtractor.setDepID2ContextRetrieverMapping(map);
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

		final SliceCriteriaFactory _criteriaFactory = SliceCriteriaFactory.getFactory();

		for (final Iterator _i = sliceCriteria.iterator(); _i.hasNext();) {
			final Object _o = _i.next();

			if (!SliceCriteriaFactory.isSlicingCriterion(_o)) {
				LOGGER.error("The work piece is not a valid slice criterion." + _o);
				throw new IllegalStateException("The work piece is not a valid slice criterion." + _o);
			}

			criteria.add(_criteriaFactory.clone((ISliceCriterion) _o));
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
	 * Sets the scope for slicing.
	 *
	 * @param scope to be used.
	 */
	public void setSliceScopeDefinition(final SpecificationBasedScopeDefinition scope) {
		sliceScope = scope;
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

		if (theSliceType.equals(BACKWARD_SLICE)) {
			directionSensitiveInfo = new BackwardSlicingPart(this);
		} else if (theSliceType.equals(FORWARD_SLICE)) {
			directionSensitiveInfo = new ForwardSlicingPart(this);
		} else if (theSliceType.equals(COMPLETE_SLICE)) {
			directionSensitiveInfo = new CompleteSlicingPart(this);
		}
		dependenceExtractor.setDependenceRetriever(directionSensitiveInfo);
	}

	/**
	 * Sets the value of <code>system</code>.
	 *
	 * @param theSystem the new value of <code>system</code>.
	 */
	public void setSystem(final IEnvironment theSystem) {
		system = theSystem;
	}

	/**
	 * Retrieves the value in <code>system</code>.
	 *
	 * @return the value in <code>system</code>.
	 */
	public IEnvironment getSystem() {
		return system;
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
	 * Places the given call site on top of the call stack to simulate a call.  The callsite comprises of the caller and the
	 * invocation statement in the caller.
	 *
	 * @param callsite is the call site.
	 *
	 * @pre callsite != null
	 */
	public void enterMethod(final CallTriple callsite) {
		if (callsite != null) {
			if (callStackCache == null) {
				callStackCache = new Stack();
			}
			callStackCache.push(callsite);
		}
	}

	/**
	 * Checks if the processing is embedded in a calling context.
	 *
	 * @return <code>true</code> if the processing is embedded in a calling context; <code>false</code>, otherwise.
	 */
	public boolean ifInsideContext() {
		return callStackCache != null && !callStackCache.isEmpty();
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

		useInterferenceDACache = controller.getAnalyses(IDependencyAnalysis.INTERFERENCE_DA) != null;
	}

	/**
	 * Resets internal data structures and removes all references to objects provided at initialization time. For other
	 * operations to be meaningful following a call to this method, the user should call <code>initialize</code> before
	 * calling any other methods.
	 */
	public void reset() {
		cgi = null;
		collector.reset();
		callStackCache = null;
		criteria.clear();
		collectedAllInvocationSites.clear();

		if (directionSensitiveInfo != null) {
			directionSensitiveInfo.reset();
		}

		// clear the work bag of slice criterion
		while (workbag.hasWork()) {
			final Object _work = workbag.getWork();
			((IPoolable) _work).returnToPool();
		}
	}

	/**
	 * Pops the TOS of the call stack.
	 *
	 * @return the callsite from which the processing returned.
	 */
	public CallTriple returnFromMethod() {
		CallTriple _result = null;

		if (callStackCache != null) {
			if (ifInsideContext()) {
				_result = (CallTriple) callStackCache.pop();
			}

			if (callStackCache.isEmpty()) {
				callStackCache = null;
			}
		}

		return _result;
	}

	/**
	 * Slices the system provided at initialization for the initialized criteria to generate the given type of slice..
	 */
	public void slice() {
		workbag.addAllWorkNoDuplicates(criteria);

		while (workbag.hasWork()) {
			final Object _work = workbag.getWork();

			if (_work instanceof SliceExpr) {
				final SliceExpr _sliceExpr = (SliceExpr) _work;

				if (sliceScope == null || sliceScope.isInScope(_sliceExpr.method, system)) {
					callStackCache = _sliceExpr.getCallStack();
					transformAndGenerateNewCriteriaForExpr(_sliceExpr);
				}
			} else if (_work instanceof SliceStmt) {
				final SliceStmt _sliceStmt = (SliceStmt) _work;
				final SootMethod _sm = _sliceStmt.getOccurringMethod();

				if (sliceScope == null || sliceScope.isInScope(_sm, system)) {
					final Stmt _stmt = (Stmt) _sliceStmt.getCriterion();
					callStackCache = _sliceStmt.getCallStack();
					transformAndGenerateNewCriteriaForStmt(_stmt, _sm, _sliceStmt.isConsiderExecution());
				}
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
	 * Retrieves the value in <code>cgi</code>.
	 *
	 * @return the value in <code>cgi</code>.
	 */
	ICallGraphInfo getCgi() {
		return cgi;
	}

	/**
	 * Retrieves a copy of <code>callStackCache</code>.
	 *
	 * @return a copy of <code>callStackCache</code>.
	 */
	Stack getCopyOfCallStackCache() {
		final Stack _result;

		if (callStackCache != null) {
			_result = (Stack) callStackCache.clone();
		} else {
			_result = null;
		}
		return _result;
	}

	/**
	 * Retrieves the value in <code>initMapper</code>.
	 *
	 * @return the value in <code>initMapper</code>.
	 */
	INewExpr2InitMapper getInitMapper() {
		return initMapper;
	}

	/**
	 * Generates new slice criterion that captures the sites that invoke the given method.
	 *
	 * @param method is the method whose calling sites needs to be included in the slice.
	 *
	 * @pre method != null
	 */
	void generateCriteriaForTheCallToMethod(final SootMethod method) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Generating criteria for call-sites (callee-caller) " + method);
		}

		if (!collector.hasBeenCollected(method)) {
			includeMethodAndDeclaringClassInSlice(method);
		}

		if (!collectedAllInvocationSites.contains(method)) {
			final Stack _stack = callStackCache;

			if (_stack != null && !_stack.isEmpty()) {
				final CallTriple _top = (CallTriple) _stack.pop();
				final SootMethod _caller = _top.getMethod();
				final Stmt _stmt = _top.getStmt();
				directionSensitiveInfo.generateCriteriaForTheCallToMethod(method, _caller, _stmt);
				_stack.push(_top);
			} else {
				collectedAllInvocationSites.add(method);

				for (final Iterator _i = cgi.getCallers(method).iterator(); _i.hasNext();) {
					final CallTriple _ctrp = (CallTriple) _i.next();
					final SootMethod _caller = _ctrp.getMethod();
					final Stmt _stmt = _ctrp.getStmt();
					directionSensitiveInfo.generateCriteriaForTheCallToMethod(method, _caller, _stmt);
				}
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Generating criteria for call-sites (callee-caller)");
		}
	}

	/**
	 * Specialized form of <code>generateSliceExprCriterion</code> that generates the criterion against the given call stack
	 * instead of the current call stack.
	 *
	 * @param valueBox is the program point for which slice criterion should be generated.
	 * @param stmt is the statement containing <code>valueBox</code>.
	 * @param method is the method containing <code>stmt</code>.
	 * @param considerExecution indicates if the execution of the program point should be considered or just the control
	 * 		  reaching it.
	 * @param callStack to use instead of the current call stack.
	 *
	 * @pre valueBox != null and stmt != null and method != null and callstack != null
	 */
	void generateSliceExprCriterion(final ValueBox valueBox, final Stmt stmt, final SootMethod method,
		final boolean considerExecution, final Stack callStack) {
		final Stack _stack = callStackCache;
		callStackCache = callStack;
		generateSliceExprCriterion(valueBox, stmt, method, considerExecution);
		callStackCache = _stack;
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
	void generateSliceExprCriterion(final ValueBox valueBox, final Stmt stmt, final SootMethod method,
		final boolean considerExecution) {
		if (!collector.hasBeenCollected(valueBox)) {
			final Collection _sliceCriteria =
				SliceCriteriaFactory.getFactory().getCriteria(method, stmt, valueBox, considerExecution);
			setContext(_sliceCriteria);
			workbag.addAllWorkNoDuplicates(_sliceCriteria);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Adding expr [" + considerExecution + "] " + valueBox.getValue() + " at " + stmt + " in "
					+ method.getSignature() + " @ " + callStackCache + " to workbag.");
			}
		} else {
			if (valueBox.getValue() instanceof InvokeExpr) {
				generateCriteriaForInvokeExprIn(stmt, method);
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
	void generateSliceStmtCriterion(final Stmt stmt, final SootMethod method, final boolean considerExecution) {
		if (!collector.hasBeenCollected(stmt)) {
			final Collection _sliceCriteria = SliceCriteriaFactory.getFactory().getCriteria(method, stmt, considerExecution);
			setContext(_sliceCriteria);
			workbag.addAllWorkNoDuplicates(_sliceCriteria);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Adding [" + considerExecution + "] " + stmt + " in " + method.getSignature() + " @ "
					+ callStackCache + " to workbag.");
			}
		} else {
			if (sliceType.equals(COMPLETE_SLICE)
				  || (considerExecution && sliceType.equals(BACKWARD_SLICE))
				  || (!considerExecution && sliceType.equals(FORWARD_SLICE))) {
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
	 * Includes the given method and it's declaring class in the slice.
	 *
	 * @param method to be included in the slice.
	 *
	 * @pre method != null
	 */
	void includeMethodAndDeclaringClassInSlice(final SootMethod method) {
		collector.includeInSlice(method);

		final SootClass _sc = method.getDeclaringClass();
		includeClassInSlice(_sc);

		final Collection _types = new HashSet(method.getParameterTypes());
		_types.add(method.getReturnType());
		includeTypesInSlice(_types);

		final List _exceptions = method.getExceptions();
		final Iterator _i = _exceptions.iterator();
		final int _iEnd = _exceptions.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final SootClass _exception = (SootClass) _i.next();
			includeClassInSlice(_exception);
		}
	}

	/**
	 * Sets the value of <code>callStackCache</code>.
	 *
	 * @param callStack the new value of <code>callStackCache</code>.
	 */
	private void setCallStackCache(final Stack callStack) {
		callStackCache = callStack;
	}

	/**
	 * Sets the direction and the calling context on the given criteria.
	 *
	 * @param theCriteria a collection of criteria
	 *
	 * @pre theCriteria != null and theCriteria.oclIsKindOf(Collection(ISliceCriterion))
	 */
	private void setContext(final Collection theCriteria) {
		if (callStackCache != null) {
			final Iterator _i = theCriteria.iterator();
			final int _iEnd = theCriteria.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final ISliceCriterion _criterion = (ISliceCriterion) _i.next();

				((AbstractSliceCriterion) _criterion).setCallStack((Stack) callStackCache.clone());
			}
		}
	}

	/**
	 * Generates new slice criteria based on what affects the given occurrence of the invoke expression (caller-callee).  By
	 * nature of Jimple, only one invoke expression can occur in a statement, hence, the arguments.
	 *
	 * @param stmt in which the field occurs.
	 * @param method in which <code>stmt</code> occurs.
	 *
	 * @pre stmt != null and method != null
	 * @pre stmt.containsInvokeExpr() == true
	 */
	private void generateCriteriaForInvokeExprIn(final Stmt stmt, final SootMethod method) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Generating criteria for invocation expressions (caller-callee)");
		}

		final InvokeExpr _expr = stmt.getInvokeExpr();
		final SootMethod _sm = _expr.getMethod();
		final Collection _callees = new HashSet();

		if (_sm.isStatic()) {
			_callees.add(_sm);
		} else {
			final Context _context = new Context();
			_context.setRootMethod(method);
			_context.setStmt(stmt);
			_callees.addAll(cgi.getCallees(_expr, _context));
		}

		CollectionUtils.filter(_callees, nonStartMethodPredicate);
		directionSensitiveInfo.generateCriteriaToIncludeCallees(stmt, method, _callees);

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
	private void generateCriteriaForLocals(final Collection locals, final Stmt stmt, final SootMethod method) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("generateCriteriaForLocals(Collection locals = " + locals + ", Stmt stmt = " + stmt
				+ ", SootMethod method = " + method + ", stack =" + callStackCache + ") - BEGIN");
		}

		final Collection _analyses = controller.getAnalyses(IDependencyAnalysis.IDENTIFIER_BASED_DATA_DA);

		if (_analyses.size() > 0) {
			final Iterator _k = locals.iterator();
			final int _kEnd = locals.size();

			for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
				final ValueBox _vBox = (ValueBox) _k.next();
				final Local _local = (Local) _vBox.getValue();
				final Pair _pair = new Pair(stmt, _local);
				dependenceExtractor.setTrigger(_pair, method);
				CollectionUtils.forAllDo(_analyses, dependenceExtractor);

				final Collection _valuesSet = dependenceExtractor.getDependenceMap().values();
				final Iterator _l = _valuesSet.iterator();
				final int _lEnd = _valuesSet.size();

				for (int _lIndex = 0; _lIndex < _lEnd; _lIndex++) {
					final Collection _values = (Collection) _l.next();
					final Iterator _j = _values.iterator();
					final int _jEnd = _values.size();

					for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
						final Stmt _depStmt = (Stmt) _j.next();

						directionSensitiveInfo.processLocalAt(_local, _depStmt, method);
					}
				}
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("generateCriteriaForLocals() " + ", stack =" + callStackCache + "- END");
		}
	}

	/**
	 * Generates contextualized criteria based on inter-procedural dependence.
	 *
	 * @param criteriaBase a reference that is required to generate contexts.  This is not used by this method. Rather it is
	 * 		  provided to the context generator.
	 * @param stmtToBeIncluded the criteria needs to be generated to include this statement into the slice.
	 * @param methodToBeIncluded this method contains <code>stmtToBeIncluded</code>.
	 * @param considerExecution indicates if the execution of the statement should be considered or just the control reaching
	 * 		  it.
	 *
	 * @pre criteriaBase != null and stmtToBeIncluded != null and methodToBeIncluded != null
	 */
	private void generateInterProceduralContextualizedCriteria(final Object criteriaBase, final Stmt stmtToBeIncluded,
		final SootMethod methodToBeIncluded, final boolean considerExecution) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("generateInterProceduralContextualizedCriteria(Object criteriaBase = " + criteriaBase
				+ ", Stmt stmtToBeIncluded = " + stmtToBeIncluded + ", SootMethod methodToBeIncluded = " + methodToBeIncluded
				+ ", boolean considerExecution = " + considerExecution + ") - BEGIN");
		}

		final Stack _temp = getCopyOfCallStackCache();
		final Collection _contexts = dependenceExtractor.getContextsFor(criteriaBase);
		final Iterator _i = _contexts.iterator();
		final int _iEnd = _contexts.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Stack _context = (Stack) _i.next();
			setCallStackCache(_context);
			generateSliceStmtCriterion(stmtToBeIncluded, methodToBeIncluded, considerExecution);
		}
		setCallStackCache(_temp);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("generateInterProceduralContextualizedCriteria() - END");
		}
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
	private void generateStmtMethodCriteria(final Stmt stmt, final SootMethod method, final Collection das) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Generating Criteria based on dependences");
		}

		dependenceExtractor.setTrigger(stmt, method);
		CollectionUtils.forAllDo(das, dependenceExtractor);

		for (final Iterator _i = dependenceExtractor.getDependenceMap().entrySet().iterator(); _i.hasNext();) {
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

				if (_methodToBeIncluded.equals(method)) {
					generateSliceStmtCriterion(_stmtToBeIncluded, _methodToBeIncluded, _considerExecution);
				} else {
					generateInterProceduralContextualizedCriteria(_o, _stmtToBeIncluded, _methodToBeIncluded,
						_considerExecution);
				}
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Generating Criteria based on dependences");
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
		final boolean _considerExecution = expr.isConsiderExecution();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Transforming expr criteria: " + _vBox.getValue() + "[" + _considerExecution + "] at "
				+ _stmt + " in " + _method + "   " + callStackCache);
		}

		// include the statement to capture control dependency and generate criteria from it. Remember to collect it.
		transformAndGenerateNewCriteriaForStmt(_stmt, _method, false);

		// generate new slice criteria
		if (sliceType.equals(COMPLETE_SLICE)
			  || (_considerExecution && sliceType.equals(BACKWARD_SLICE))
			  || (!_considerExecution && sliceType.equals(FORWARD_SLICE))) {
			final Collection _valueBoxes = directionSensitiveInfo.retrieveValueBoxesToTransformExpr(_vBox, _stmt);

			// include any sub expressions and generate criteria from them
			transformAndGenerateToConsiderExecution(_stmt, _method, _valueBoxes);

			if (_vBox.getValue() instanceof InvokeExpr) {
				generateCriteriaForInvokeExprIn(_stmt, _method);
			}
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
			LOGGER.debug("BEGIN: Transforming stmt criteria: " + stmt + "[" + considerExecution + "] in " + method + "   "
				+ callStackCache);
		}

		// transform the statement
		if (sliceType.equals(COMPLETE_SLICE)
			  || (considerExecution && sliceType.equals(BACKWARD_SLICE))
			  || (!considerExecution && sliceType.equals(FORWARD_SLICE))) {
			directionSensitiveInfo.processNewExpr(stmt, method);

			final Collection _valueBoxes = directionSensitiveInfo.retrieveValueBoxesToTransformStmt(stmt);
			transformAndGenerateToConsiderExecution(stmt, method, _valueBoxes);

			if (stmt.containsInvokeExpr()) {
				generateCriteriaForInvokeExprIn(stmt, method);
			}
		}

		// capture control flow based dependences.
		if (!collector.hasBeenCollected(stmt)) {
			generateStmtMethodCriteria(stmt, method, controlflowBasedDAs);
		}

		// generate new slice criteria
		generateCriteriaForTheCallToMethod(method);

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
					directionSensitiveInfo.processParameterRef(_vBox, method);
				} else if (_value instanceof FieldRef || _value instanceof ArrayRef) {
					_das.addAll(controller.getAnalyses(IDependencyAnalysis.REFERENCE_BASED_DATA_DA));

					if (useInterferenceDACache) {
						_das.addAll(controller.getAnalyses(IDependencyAnalysis.INTERFERENCE_DA));
					}

					if (_value instanceof FieldRef) {
						final SootField _field = ((FieldRef) _vBox.getValue()).getField();
						collector.includeInSlice(_field);
						includeClassInSlice(_field.getDeclaringClass());
					}
				} else if (_value instanceof Local) {
					_locals.add(_vBox);
				}
				_types.add(_value.getType());
			}
		}
		includeTypesInSlice(_types);

		// create new slice criteria based on statement level dependence.
		if (!_das.isEmpty()) {
			generateStmtMethodCriteria(stmt, method, _das);
		}

		// create new criteria based on program point level dependence (identifier based dependence).
		generateCriteriaForLocals(_locals, stmt, method);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Transforming value boxes");
		}
	}
}

// End of File
