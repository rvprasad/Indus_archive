/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

package edu.ksu.cis.indus.slicer;

import edu.ksu.cis.indus.annotations.Empty;
import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.NonNull;
import edu.ksu.cis.indus.annotations.NonNullContainer;
import edu.ksu.cis.indus.common.ToStringBasedComparator;
import edu.ksu.cis.indus.common.collections.CollectionUtils;
import edu.ksu.cis.indus.common.collections.IPredicate;
import edu.ksu.cis.indus.common.collections.Stack;
import edu.ksu.cis.indus.common.datastructures.FIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.graph.SimpleNode;
import edu.ksu.cis.indus.common.graph.SimpleNodeGraph;
import edu.ksu.cis.indus.common.scoping.SpecificationBasedScopeDefinition;
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;
import edu.ksu.cis.indus.common.soot.Util;
import edu.ksu.cis.indus.interfaces.IActivePart;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.ICallingContextRetriever;
import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.interfaces.INewExpr2InitMapper;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis;
import edu.ksu.cis.indus.staticanalyses.processing.AnalysesController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.ParameterRef;
import soot.jimple.Stmt;
import soot.tagkit.Host;

/**
 * This class accepts slice criterions and generates slices of the given system.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public final class SlicingEngine {

	/**
	 * This predicate can be used to filter out java.lang.Thread.start methods from a set of methods.
	 * 
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	private static class NonStartMethodPredicate
			implements IPredicate<SootMethod> {

		/**
		 * Creates an instance of this class.
		 */
		@Empty NonStartMethodPredicate() {
			super();
		}

		/**
		 * {@inheritDoc}
		 * 
		 * @see edu.ksu.cis.indus.common.collections.IPredicate#evaluate(java.lang.Object)
		 */
		public <T1 extends SootMethod> boolean evaluate(final T1 object) {
			return !Util.isStartMethod(object);
		}
	}

	/**
	 * The logger used by instances of this class to log messages.
	 */
	static final Logger LOGGER = LoggerFactory.getLogger(SlicingEngine.class);

	/**
	 * The object used to realize the "active" part of this object.
	 */
	private final IActivePart.ActivePart activePart = new IActivePart.ActivePart();

	/**
	 * This is the basic block graph manager which manages the BB graphs corresponding to the system being sliced.
	 */
	private BasicBlockGraphMgr bbgMgr;

	/**
	 * This caches the call stack of the criteria currently being processed. It will hold the call-site of the current method
	 * in which the processing is occurring. This will NOT include the current method being processed at TOS unless there is
	 * recursion.
	 */
	private Stack<CallTriple> callStackCache;

	/**
	 * This graph stores the call strings that have been captured during slicing.
	 */
	private SimpleNodeGraph<Object> callStringGraph;

	/**
	 * This provides the call graph information in the system being sliced.
	 */
	private ICallGraphInfo cgi;

	/**
	 * This collects the parts of the system that make up the slice.
	 */
	private SliceCollector collector;

	/**
	 * The collection of control based Dependence analysis to be used during slicing. Synchronization, Divergence, and Control
	 * dependences are such dependences.
	 */
	private Collection<IDependencyAnalysis<?, ?, ?, ?, ?, ?>> controlflowBasedDAs = new ArrayList<IDependencyAnalysis<?, ?, ?, ?, ?, ?>>();

	/**
	 * The controller used to access the dependency analysis info during slicing.
	 */
	private AnalysesController controller;

	/**
	 * The list of slice criteria.
	 * 
	 * @invariant criteria != null
	 */
	private List<ISliceCriterion> criteria = new ArrayList<ISliceCriterion>();

	/**
	 * The closure used to extract dependence information based on slice direction. See <code>setSliceType()</code> for
	 * details.
	 */
	private final DependenceExtractor dependenceExtractor = new DependenceExtractor(this);

	/**
	 * This provides the logic to determine parts of the slice that are direction dependent.
	 */
	private IDirectionSensitivePartOfSlicingEngine directionSensitiveInfo;

	/**
	 * This maps new expressions to corresponding init call sites.
	 */
	private INewExpr2InitMapper initMapper;

	/**
	 * This predicate is used to filter out java.lang.Thread.start methods from a set of methods.
	 */
	private IPredicate<SootMethod> nonStartMethodPredicate = new NonStartMethodPredicate();

	/**
	 * This defines the scope of slicing. If this is <code>null</code>, then the entire system is considered as the scope.
	 */
	private SpecificationBasedScopeDefinition sliceScope;

	/**
	 * The direction of the slice. It's default value is <code>BACKWARD_SLICE</code>.
	 * 
	 * @invariant sliceTypes.contains(sliceType)
	 */
	private SliceType sliceType = SliceType.BACKWARD_SLICE;

	/**
	 * The system being sliced.
	 */
	private IEnvironment system;

	/**
	 * This caches the informaiton - is interference dependence being used in this execution?
	 */
	private boolean useInterferenceDACache;

	/**
	 * The work bag used during slicing.
	 * 
	 * @invariant workbag != null
	 */
	private final IWorkBag<ISliceCriterion> workbag = new FIFOWorkBag<ISliceCriterion>();

	/**
	 * Creates a new SlicingEngine object.
	 */
	public SlicingEngine() {
		collector = new SliceCollector(this);
		callStringGraph = new SimpleNodeGraph<Object>();
		callStringGraph.getNode(null);
	}

	/**
	 * Places the given call site on top of the call stack to simulate a call. The callsite comprises of the caller and the
	 * invocation statement in the caller.
	 * 
	 * @param callsite is the call site.
	 * @pre callsite != null
	 */
	public void enterMethod(final CallTriple callsite) {
		if (callsite != null) {
			if (callStackCache == null) {
				callStackCache = new Stack<CallTriple>();
			}
			callStackCache.push(callsite);
		}
	}

	/**
	 * Returns the active part of this object.
	 * 
	 * @return the active part.
	 */
	public IActivePart getActivePart() {
		return activePart;
	}

	/**
	 * Retrieves the slice collector being used by this instance of the slice engine.
	 * 
	 * @return the slice collector
	 * @post result != null
	 */
	public SliceCollector getCollector() {
		return collector;
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
			LOGGER.error(_temp);
			throw new IllegalStateException(_temp);
		}

		useInterferenceDACache = controller.getAnalyses(IDependencyAnalysis.DependenceSort.INTERFERENCE_DA) != null;
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
		activePart.activate();

		callStringGraph = new SimpleNodeGraph<Object>();
		callStringGraph.getNode(null);

		if (directionSensitiveInfo != null) {
			directionSensitiveInfo.reset();
		}

		workbag.clear();
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
				_result = callStackCache.pop();
			}
		}

		return _result;
	}

	/**
	 * Sets the the id's of the dependence analyses to be used for slicing. The analyses are provided by the given controller
	 * that was used to initialize the analyses. to
	 * 
	 * @param ctrl provides dependency information required for slicing.
	 * @param dependenciesToUse is the ids of the dependecies to be considered for slicing.
	 * @pre ctrl != null and dependenciesToUse != null
	 * @pre dependeciesToUse->forall(o | controller.getAnalysis(o) != null)
	 */
	public void setAnalysesControllerAndDependenciesToUse(final AnalysesController ctrl,
			final Collection<IDependencyAnalysis.DependenceSort> dependenciesToUse) {
		controller = ctrl;
		controlflowBasedDAs.clear();

		for (final Iterator<IDependencyAnalysis.DependenceSort> _i = dependenciesToUse.iterator(); _i.hasNext();) {
			final IDependencyAnalysis.DependenceSort _id = _i.next();

			if (_id.equals(IDependencyAnalysis.DependenceSort.CONTROL_DA)
					|| _id.equals(IDependencyAnalysis.DependenceSort.SYNCHRONIZATION_DA)
					|| _id.equals(IDependencyAnalysis.DependenceSort.DIVERGENCE_DA)
					|| _id.equals(IDependencyAnalysis.DependenceSort.READY_DA)) {
				controlflowBasedDAs.addAll((Collection<IDependencyAnalysis<?, ?, ?, ?, ?, ?>>) controller.getAnalyses(_id));
			}
		}
	}

	/**
	 * Sets the basic block graph manager to be used during slicing.
	 * 
	 * @param basicBlockGraphMgr is the basic block graph manager for the system being sliced.
	 * @pre bbgMgr != null
	 */
	public void setBasicBlockGraphManager(final BasicBlockGraphMgr basicBlockGraphMgr) {
		bbgMgr = basicBlockGraphMgr;
	}

	/**
	 * Sets the call graph to be used during slicing.
	 * 
	 * @param callgraph provides call graph information about the system being sliced.
	 * @pre callgraph != null
	 */
	public void setCgi(final ICallGraphInfo callgraph) {
		cgi = callgraph;
	}

	/**
	 * Sets the information that provides context retriever based on dependence analysis id.
	 * 
	 * @param map a map from dependence analysis id to context retriever to be used with it.
	 * @pre map != null
	 */
	public void setDepID2ContextRetrieverMapping(final Map<IDependencyAnalysis.DependenceSort, ICallingContextRetriever> map) {
		dependenceExtractor.setDepID2ContextRetrieverMapping(map);
	}

	/**
	 * Sets the object that maps new expressions to corresponding init invocation sites.
	 * 
	 * @param mapper maps new expressions to corresponding init invocation sites.
	 * @pre mapper != null
	 */
	public void setInitMapper(final INewExpr2InitMapper mapper) {
		initMapper = mapper;
	}

	/**
	 * Sets the given criteria as the slicing criteria over the next run.
	 * 
	 * @param sliceCriteria are ofcourse the slicing criteria
	 * @throws IllegalStateException when there are criteria which are not of type <code>ISliceCriterion</code>.
	 * @pre sliceCriteria != null
	 */
	public void setSliceCriteria(final Collection<ISliceCriterion> sliceCriteria) {
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

		for (final Iterator<?> _i = sliceCriteria.iterator(); _i.hasNext();) {
			final Object _o = _i.next();

			if (!SliceCriteriaFactory.isSlicingCriterion(_o)) {
				LOGGER.error("The work piece is not a valid slice criterion." + _o);
				throw new IllegalStateException("The work piece is not a valid slice criterion." + _o);
			}

			criteria.add(_criteriaFactory.clone((ISliceCriterion) _o));
		}

		if (LOGGER.isDebugEnabled()) {
			Collections.sort(criteria, ToStringBasedComparator.getComparator());

			final StringBuffer _sb = new StringBuffer();

			for (final Iterator<ISliceCriterion> _i = criteria.iterator(); _i.hasNext();) {
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
	 * @param theSliceType is the type of slice requested.
	 * @throws IllegalArgumentException when the given slice type is illegal.
	 * @pre theSliceType != null
	 */
	public void setSliceType(final SliceType theSliceType) {
		sliceType = theSliceType;

		if (theSliceType.equals(SliceType.BACKWARD_SLICE)) {
			directionSensitiveInfo = new BackwardSlicingPart(this);
		} else if (theSliceType.equals(SliceType.FORWARD_SLICE)) {
			directionSensitiveInfo = new ForwardSlicingPart(this);
		} else if (theSliceType.equals(SliceType.COMPLETE_SLICE)) {
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
	 * Sets the name of the tag to be used identify the elements of the AST belonging to the slice.
	 * 
	 * @param tagName is the name of the tag
	 * @pre tagName != null
	 */
	public void setTagName(final String tagName) {
		collector.setTagName(tagName);
	}

	/**
	 * Slices the system provided at initialization for the initialized criteria to generate the given type of slice..
	 */
	public void slice() {
		workbag.addAllWorkNoDuplicates(criteria);

		boolean _flag = true;

		while (_flag && activePart.canProceed()) {

			while (workbag.hasWork() && activePart.canProceed()) {
				final Object _work = workbag.getWork();

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("BEGIN - Processing criterion - " + _work);
				}

				if (_work instanceof ExprLevelSliceCriterion) {
					final ExprLevelSliceCriterion _sliceExpr = (ExprLevelSliceCriterion) _work;
					final SootMethod _sm = _sliceExpr.getOccurringMethod();

					if (sliceScope == null || sliceScope.isInScope(_sm, system)) {
						callStackCache = _sliceExpr.getCallStack();
						transformAndGenerateNewCriteriaForExpr(_sliceExpr);
					}
				} else if (_work instanceof StmtLevelSliceCriterion) {
					final StmtLevelSliceCriterion _sliceStmt = (StmtLevelSliceCriterion) _work;
					final SootMethod _sm = _sliceStmt.getOccurringMethod();

					if (sliceScope == null || sliceScope.isInScope(_sm, system)) {
						final Stmt _stmt = (Stmt) _sliceStmt.getCriterion();
						callStackCache = _sliceStmt.getCallStack();
						transformAndGenerateNewCriteriaForStmt(_stmt, _sm, _sliceStmt.isConsiderExecution());
					}
				} else if (_work instanceof MethodLevelSliceCriterion) {
					final MethodLevelSliceCriterion _sliceMethod = (MethodLevelSliceCriterion) _work;
					final SootMethod _sm = _sliceMethod.getOccurringMethod();

					if (sliceScope == null || sliceScope.isInScope(_sm, system)) {
						callStackCache = _sliceMethod.getCallStack();
						transformAndGenerateNewCriteriaForMethod(_sm);
					}
				}

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("END - Processing criterion - " + _work);
				}
			}
			_flag = directionSensitiveInfo.continueProcessing();
		}

		if (activePart.canProceed()) {
			collector.completeSlicing();
		}
	}

	/**
	 * Generates new slice criterion that captures the sites that invoke the given method.
	 * 
	 * @param method is the method whose calling sites needs to be included in the slice.
	 * @pre method != null
	 */
	void generateCriteriaForTheCallToMethod(final SootMethod method) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Generating criteria for call-sites (callee-caller) " + method);
		}

		if (isNotIncludedInSlice(method)) {
			includeMethodAndDeclaringClassInSlice(method);
		}

		if (!haveCollectedAllInvocationSites(method)) {
			if (ifInsideContext()) {
				if (callStackCache.peek() != null) {
					final CallTriple _top = callStackCache.pop();
					final SootMethod _caller = _top.getMethod();
					final Stmt _stmt = _top.getStmt();
					directionSensitiveInfo.generateCriteriaForTheCallToMethod(method, _caller, _stmt);
					callStackCache.push(_top);
				}
			} else {
				markAsCollectedAllInvocationSites(method);

				for (final Iterator<CallTriple> _i = cgi.getCallers(method).iterator(); _i.hasNext();) {
					final CallTriple _ctrp = _i.next();
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
	 * Generates slice criterion for teh given program point.
	 * 
	 * @param valueBox is the program point for which slice criterion should be generated.
	 * @param stmt is the statement containing <code>valueBox</code>.
	 * @param method is the method containing <code>stmt</code>.
	 * @param considerExecution indicates if the execution of the program point should be considered or just the control
	 *            reaching it.
	 * @return <code>true</code> if the expression was previously not in the slice; <code>false</code>, otherwise.
	 * @pre valueBox != null and stmt != null and method != null
	 */
	boolean generateExprLevelSliceCriterion(final ValueBox valueBox, final Stmt stmt, final SootMethod method,
			final boolean considerExecution) {
		final boolean _result;

		if (isNotIncludedInSlice(valueBox)) {
			final Collection<ISliceCriterion> _sliceCriteria = SliceCriteriaFactory.getFactory().getCriteria(method, stmt,
					valueBox, considerExecution);
			setContext(_sliceCriteria);
			workbag.addAllWorkNoDuplicates(_sliceCriteria);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Adding expr [" + considerExecution + "] " + valueBox.getValue() + " at " + stmt + " in "
						+ method.getSignature() + " @ " + callStackCache + " to workbag.");
			}
			_result = true;
		} else {
			if (valueBox.getValue() instanceof InvokeExpr) {
				generateCriteriaForInvokeExprIn(stmt, method);
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Already collected expr " + valueBox.getValue() + " occurring at " + stmt + " in "
						+ method.getSignature());
			}
			_result = false;
		}
		return _result;
	}

	/**
	 * Specialized form of <code>generateSliceExprCriterion</code> that generates the criterion against the given call stack
	 * instead of the current call stack.
	 * 
	 * @param valueBox is the program point for which slice criterion should be generated.
	 * @param stmt is the statement containing <code>valueBox</code>.
	 * @param method is the method containing <code>stmt</code>.
	 * @param considerExecution indicates if the execution of the program point should be considered or just the control
	 *            reaching it.
	 * @param callStack to use instead of the current call stack.
	 * @return <code>true</code> if the expression was previously not in the slice; <code>false</code>, otherwise.
	 * @pre valueBox != null and stmt != null and method != null and callstack != null
	 */
	boolean generateExprLevelSliceCriterion(final ValueBox valueBox, final Stmt stmt, final SootMethod method,
			final boolean considerExecution, final Stack<CallTriple> callStack) {
		final Stack<CallTriple> _stack = callStackCache;
		callStackCache = callStack;
		final boolean _result = generateExprLevelSliceCriterion(valueBox, stmt, method, considerExecution);
		callStackCache = _stack;
		return _result;
	}

	/**
	 * Generates slice criterion for the given statement.
	 * 
	 * @param stmt is the statement.
	 * @param method is the method containing <code>stmt</code>.
	 * @param considerExecution indicates if the execution of the statement should be considered or just the control reaching
	 *            it.
	 * @return <code>true</code> if the statement was previously not in the slice; <code>false</code>, otherwise.
	 * @pre stmt != null and method != null
	 */
	boolean generateStmtLevelSliceCriterion(final Stmt stmt, final SootMethod method, final boolean considerExecution) {
		final boolean _result = isNotIncludedInSlice(stmt);

		if (_result) {
			final Collection<ISliceCriterion> _sliceCriteria = SliceCriteriaFactory.getFactory().getCriteria(method, stmt,
					considerExecution);
			setContext(_sliceCriteria);
			workbag.addAllWorkNoDuplicates(_sliceCriteria);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Adding [" + considerExecution + "] " + stmt + " in " + method.getSignature() + " @ "
						+ callStackCache + " to workbag.");
			}
		} else {
			if (sliceType.equals(SliceType.COMPLETE_SLICE)
					|| (considerExecution && sliceType.equals(SliceType.BACKWARD_SLICE))
					|| (!considerExecution && sliceType.equals(SliceType.FORWARD_SLICE))) {
				final Collection<ValueBox> _temp = new HashSet<ValueBox>(stmt.getUseAndDefBoxes());

				// if it contains an invocation expression, we do not want to include the arguments/sub-expressions.
				if (stmt.containsInvokeExpr()) {
					final InvokeExpr _invokeExpr = stmt.getInvokeExpr();
					_temp.removeAll(_invokeExpr.getUseBoxes());

					// in case of instance invocation, we do want to include the receiver position expression.
					if (_invokeExpr instanceof InstanceInvokeExpr) {
						_temp.add(((InstanceInvokeExpr) _invokeExpr).getBaseBox());
					}
				}

				for (final Iterator<ValueBox> _i = _temp.iterator(); _i.hasNext();) {
					final ValueBox _valueBox = _i.next();
					generateExprLevelSliceCriterion(_valueBox, stmt, method, considerExecution);
				}
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Already collected stmt " + stmt + " in " + method.getSignature());
			}
		}
		return _result;
	}

	/**
	 * Retrieves the basic block graph manager used in the engine.
	 * 
	 * @return the basic block graph manager.
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
	Stack<CallTriple> getCopyOfCallStackCache() {
		final Stack<CallTriple> _result;

		if (callStackCache != null) {
			_result = callStackCache.clone();
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
	 * Includes the given host in the slice.
	 * 
	 * @param host to be included in the slice.
	 * @pre host != null
	 * @post not isNotIncludedInSlice(host)
	 */
	void includeInSlice(final Host host) {
		collector.includeInSlice(host);
	}

	/**
	 * Includes the given method and it's declaring class in the slice.
	 * 
	 * @param method to be included in the slice.
	 * @pre method != null
	 */
	void includeMethodAndDeclaringClassInSlice(final SootMethod method) {
		includeInSlice(method);

		final SootClass _sc = method.getDeclaringClass();
		includeInSlice(_sc);

		@SuppressWarnings("unchecked") final Collection<Type> _types = new HashSet<Type>(method.getParameterTypes());
		_types.add(method.getReturnType());
		includeTypesInSlice(_types);

		@SuppressWarnings("unchecked") final List<SootClass> _exceptions = method.getExceptions();
		final Iterator<SootClass> _i = _exceptions.iterator();
		final int _iEnd = _exceptions.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final SootClass _exception = _i.next();
			includeInSlice(_exception);
		}
	}

	/**
	 * Generates new criteria based on the entities that influence or are influenced by the given statement as indicated by
	 * the given dependency analyses.
	 * 
	 * @param stmt that will trigger the dependence.
	 * @param method in which <code>stmt</code> occurs.
	 * @param das is a collection of dependency analyses.
	 * @pre stmt != null and method != null and das != null
	 * @post workbag$pre.getWork() != workbag.getWork() or workbag$pre.getWork() == workbag.getWork()
	 */
	private void generateCriteriaBasedOnStmtLevelDependences(final Stmt stmt, final SootMethod method,
			final Collection<IDependencyAnalysis<?, ?, ?, ?, ?, ?>> das) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("generateCriteriaBasedOnDependences(Stmt stmt=" + stmt + ", SootMethod method=" + method
					+ ", Collection das) - BEGIN");
		}

		dependenceExtractor.setTrigger(stmt, method, getCopyOfCallStackCache());
		CollectionUtils.forAllDo(das, dependenceExtractor);

		for (final Iterator<?> _i = dependenceExtractor.getDependences().iterator(); _i.hasNext();) {
			final Object _o = _i.next();
			final Collection<Stack<CallTriple>> _contexts = dependenceExtractor.getContextsFor(_o);
			final Stmt _stmtToBeIncluded;
			final SootMethod _methodToBeIncluded;

			if (_o instanceof Pair) {
				@SuppressWarnings("unchecked") final Pair<Stmt, SootMethod> _pair = (Pair) _o;
				_stmtToBeIncluded = _pair.getFirst();
				_methodToBeIncluded = _pair.getSecond();
			} else {
				_stmtToBeIncluded = (Stmt) _o;
				_methodToBeIncluded = method;
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("generateCriteriaBasedOnDependences(Stmt, SootMethod, Collection) -  : _contexts=" + _contexts
						+ ", _stmtToBeIncluded=" + _stmtToBeIncluded + ", _methodToBeIncluded=" + _methodToBeIncluded);
			}

			final Stack<CallTriple> _temp = getCopyOfCallStackCache();
			final Iterator<Stack<CallTriple>> _i1 = _contexts.iterator();
			final int _iEnd = _contexts.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final Stack<CallTriple> _context = _i1.next();
				setCallStackCache(_context);

				if (_stmtToBeIncluded != null) {
					final boolean _b = generateStmtLevelSliceCriterion(_stmtToBeIncluded, _methodToBeIncluded, !sliceType
							.equals(SliceType.FORWARD_SLICE));

					if (!_b) {
						generateMethodLevelSliceCriteria(_methodToBeIncluded);
					}
				} else {
					generateMethodLevelSliceCriteria(_methodToBeIncluded);
				}
			}
			setCallStackCache(_temp);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("generateCriteriaBasedOnDependences(Stmt, SootMethod, Collection) - END");
		}
	}

	/**
	 * Generates new slice criteria based on what affects the given occurrence of the invoke expression (caller-callee). By
	 * nature of Jimple, only one invoke expression can occur in a statement, hence, the arguments.
	 * 
	 * @param stmt in which the field occurs.
	 * @param method in which <code>stmt</code> occurs.
	 * @pre stmt != null and method != null
	 * @pre stmt.containsInvokeExpr() == true
	 */
	private void generateCriteriaForInvokeExprIn(final Stmt stmt, final SootMethod method) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Generating criteria for invocation expressions (caller-callee)");
		}

		final InvokeExpr _expr = stmt.getInvokeExpr();
		final SootMethod _sm = _expr.getMethod();
		final Collection<SootMethod> _callees = new HashSet<SootMethod>();

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

		// include the invoked method (not the resolved method)
		includeMethodAndDeclaringClassInSlice(_sm);

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
	 * @pre locals != null
	 * @pre stmt != null and method != null
	 */
	private void generateCriteriaForLocals(final Collection<ValueBox> locals, final Stmt stmt, final SootMethod method) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("generateCriteriaForLocals(Collection locals = " + locals + ", Stmt stmt = " + stmt
					+ ", SootMethod method = " + method + ", stack =" + callStackCache + ") - BEGIN");
		}

		@SuppressWarnings("unchecked") final Collection<IDependencyAnalysis<?, ?, ?, ?, ?, ?>> _analyses = (Collection<IDependencyAnalysis<?, ?, ?, ?, ?, ?>>) controller
				.getAnalyses(IDependencyAnalysis.DependenceSort.IDENTIFIER_BASED_DATA_DA);

		if (_analyses.size() > 0) {
			final Iterator<ValueBox> _k = locals.iterator();
			final int _kEnd = locals.size();

			for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
				final Local _local = (Local) _k.next().getValue();
				dependenceExtractor.setTrigger(dependenceExtractor.getEntityForIdentifierBasedDataDA(_local, stmt), method,
						getCopyOfCallStackCache());
				CollectionUtils.forAllDo(_analyses, dependenceExtractor);

				final Collection<?> _dependences = dependenceExtractor.getDependences();
				final Iterator<?> _l = _dependences.iterator();
				final int _lEnd = _dependences.size();

				for (int _lIndex = 0; _lIndex < _lEnd; _lIndex++) {
					final Object _o = _l.next();
					final Local _depLocal;
					final Stmt _depStmt;
					if (_o instanceof Stmt) {
						_depStmt = (Stmt) _o;
						_depLocal = _local;
					} else if (_o instanceof Pair) {
						@SuppressWarnings("unchecked") final Pair<Local, Stmt> _p = (Pair) _o;
						_depLocal = _p.getFirst();
						_depStmt = _p.getSecond();
					} else {
						throw new IllegalStateException("Identifier-based Data DA returned result of type - " + _o.getClass());
					}

					directionSensitiveInfo.processLocalAt(_depLocal, _depStmt, method);
				}

				directionSensitiveInfo.processLocalAt(_local, stmt, method);
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("generateCriteriaForLocals() " + ", stack =" + callStackCache + "- END");
		}
	}

	/**
	 * Generates method level slice criteria for the given method.
	 * 
	 * @param method of interest.
	 * @pre method != null
	 */
	private void generateMethodLevelSliceCriteria(final SootMethod method) {
		final boolean _generateCriteria = shouldMethodLevelCriteriaBeGenerated(method);

		if (_generateCriteria) {
			final Collection<ISliceCriterion> _sliceCriteria = SliceCriteriaFactory.getFactory().getCriteria(method);
			setContext(_sliceCriteria);

			final Collection<ISliceCriterion> _c = workbag.addAllWorkNoDuplicates(_sliceCriteria);

			if (LOGGER.isDebugEnabled() && !_c.isEmpty()) {
				LOGGER.debug("Adding " + method.getSignature() + " @ " + callStackCache + " to workbag.");
			}
		}
	}

	/**
	 * Checks if all invocation sites of the given method have been included in the slice.
	 * 
	 * @param method of interest.
	 * @return <code>true</code> if all invocation sites of <code>method</code> have been included in the slice;
	 *         <code>false</code>, otherwise.
	 */
	@Functional private boolean haveCollectedAllInvocationSites(@NonNull final SootMethod method) {
		return callStringGraph.queryNode(method) != null && callStringGraph.queryNode(method).getSuccsOf().isEmpty();
	}

	/**
	 * Includes the class associated with the given types.
	 * 
	 * @param types to be included in the slice.
	 */
	private void includeTypesInSlice(@NonNull @NonNullContainer final Collection<Type> types) {
		for (final Iterator<Type> _i = types.iterator(); _i.hasNext();) {
			final Type _type = _i.next();

			if (_type instanceof RefType) {
				includeInSlice(((RefType) _type).getSootClass());
			} else if (_type instanceof ArrayType && ((ArrayType) _type).baseType instanceof RefType) {
				includeInSlice(((RefType) ((ArrayType) _type).baseType).getSootClass());
			}
		}
	}

	/**
	 * Checks if the given host is not included in the slice.
	 * 
	 * @param host to be checked.
	 * @return <code>true</code> if the host is not included in the slice; <code>false</code>, otherwise.
	 * @pre host != null
	 */
	private boolean isNotIncludedInSlice(final Host host) {
		return !collector.hasBeenCollected(host);
	}

	/**
	 * Records information to indicate that all invocation sites of the given method have been included in the slice.
	 * 
	 * @param method of interest.
	 */
	private void markAsCollectedAllInvocationSites(@NonNull final SootMethod method) {
		final SimpleNode<Object> _n = callStringGraph.getNode(method);
		for (final Iterator<SimpleNode<Object>> _i = new ArrayList<SimpleNode<Object>>(_n.getSuccsOf()).iterator(); _i
				.hasNext();) {
			_n.removeSuccessor(_i.next());
		}
	}

	/**
	 * Tries to record the current call stack against the given method and returns the status of the recording.
	 * 
	 * @param method of interest
	 * @return <code>true</code> if the call stack was recored; <code>false</code> if another call stack subsumed this.
	 */
	private boolean recordCallStackForMethod(@NonNull final SootMethod method) {
		boolean _result = true;

		final SimpleNode<Object> _methodNode = callStringGraph.getNode(method);
		if (callStringGraph.queryNode(method) == null) {
			if (!callStackCache.isEmpty()) {
				Object _s = callStackCache.peek();
				if (_s != null) {
					callStringGraph.addEdgeFromTo(_methodNode, callStringGraph.getNode(_s));

					for (int _i = callStackCache.size() - 2; _i >= 0; _i--) {
						final Object _t = callStackCache.get(_i);
						if (_t != null) {
							callStringGraph.addEdgeFromTo(callStringGraph.queryNode(_s), callStringGraph.getNode(_t));
							_s = _t;
						} else {
							break;
						}
					}
				}
			}
		} else if (!callStackCache.isEmpty()) {
			_result = recordCallStackForVisitedMethod(_methodNode);
		}
		return _result;
	}

	/**
	 * Records the call stack for the method stored in the given node.
	 * 
	 * @param methodNode of interest.
	 * @return <code>true</code> if new method nodes were created or if current call stack needs to be considered for
	 *         processing. In other words, <code>true</code> is returned when the call stack to the given method node
	 *         extended call stack graph; <code>false</code>, otherwise.
	 */
	private boolean recordCallStackForVisitedMethod(final SimpleNode<Object> methodNode) {
		final int _limit = callStackCache.size() - 1;
		boolean _considerCallStack = callStackCache.peek() != null;
		boolean _createdNewNodes = false;

		for (int _i = _limit; _i >= 0 && _considerCallStack; _i--) {
			final Object _o = callStackCache.get(_i);

			SimpleNode<Object> _node = callStringGraph.queryNode(_o);

			if (_node == null) {
				_node = callStringGraph.getNode(_o);
				_createdNewNodes = true;
			} else if (_o != null) {
				if (_node.getSuccsOf().isEmpty()) {
					_considerCallStack = false;
				} else if (_i == 0) {
					final Collection<SimpleNode<Object>> _reachablesFrom = callStringGraph.getReachablesFrom(_node, true);
					_reachablesFrom.add(_node);

					final Iterator<SimpleNode<Object>> _j = _reachablesFrom.iterator();
					final int _jEnd = _reachablesFrom.size();

					for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
						final SimpleNode<Object> _src = _j.next();
						final Collection<SimpleNode<Object>> _succsOfSrc = new ArrayList<SimpleNode<Object>>(_src
								.getSuccsOf());

						final Iterator<SimpleNode<Object>> _k = _succsOfSrc.iterator();
						final int _kEnd = _succsOfSrc.size();

						for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
							final SimpleNode<Object> _dest = _k.next();
							callStringGraph.removeEdgeFromTo(_src, _dest);
						}
					}
					_considerCallStack = false;
				}
			} else {
				_considerCallStack = false;
			}

			if (_limit - _i > 0 && (_considerCallStack || _createdNewNodes)) {
				final SimpleNode<Object> _prev = callStringGraph.queryNode(callStackCache.get(_i + 1));

				if (_prev != null) {
					callStringGraph.addEdgeFromTo(_prev, _node);
				}
			}
		}

		callStringGraph.addEdgeFromTo(methodNode, callStringGraph.getNode(callStackCache.peek()));
		return _considerCallStack || _createdNewNodes;
	}

	/**
	 * Sets the value of <code>callStackCache</code>.
	 * 
	 * @param callStack the new value of <code>callStackCache</code>.
	 */
	private void setCallStackCache(final Stack<CallTriple> callStack) {
		callStackCache = callStack;
	}

	/**
	 * Sets the direction and the calling context on the given criteria.
	 * 
	 * @param theCriteria a collection of criteria
	 * @pre theCriteria != null
	 */
	private void setContext(final Collection<ISliceCriterion> theCriteria) {
		if (callStackCache != null) {
			final Iterator<ISliceCriterion> _i = theCriteria.iterator();
			final int _iEnd = theCriteria.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final ISliceCriterion _criterion = _i.next();

				_criterion.setCallStack(callStackCache.clone());
			}
		}
	}

	/**
	 * Checks if method level criteria should be generated for the given method.
	 * 
	 * @param method of interest.
	 * @return <code>true</code> if criteria should be generated; <code>false</code>, otherwise.
	 * @pre method != null
	 */
	private boolean shouldMethodLevelCriteriaBeGenerated(final SootMethod method) {
		boolean _result = isNotIncludedInSlice(method);

		if (!_result) {
			if (callStackCache == null) {
				_result = !haveCollectedAllInvocationSites(method);
			} else if (!haveCollectedAllInvocationSites(method)) {
				_result = recordCallStackForMethod(method);
			}
		}

		return _result;
	}

	/**
	 * Generates immediate slice for the given expression.
	 * 
	 * @param expr is the expression-level slice criterion.
	 * @pre expr != null and expr.getOccurringStmt() != null and expr.getOccurringMethod() != null
	 * @pre expr.getCriterion() != null and expr.getCriterion().oclIsKindOf(ValueBox)
	 */
	private void transformAndGenerateNewCriteriaForExpr(final ExprLevelSliceCriterion expr) {
		final Stmt _stmt = expr.getOccurringStmt();
		final SootMethod _method = expr.getOccurringMethod();
		final ValueBox _vBox = (ValueBox) expr.getCriterion();
		final boolean _considerExecution = expr.isConsiderExecution();
		final Value _value = _vBox.getValue();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Transforming expr criteria: " + _value + "[" + _considerExecution + "] at " + _stmt + " in "
					+ _method + "   " + callStackCache);
		}

		// include the statement to capture control dependency and generate criteria from it. Remember to collect it.
		if (sliceType.equals(SliceType.FORWARD_SLICE)) {
			transformAndGenerateNewCriteriaForStmt(_stmt, _method, true);
		} else {
			transformAndGenerateNewCriteriaForStmt(_stmt, _method, false);
		}

		// generate new slice criteria
		if (sliceType.equals(SliceType.COMPLETE_SLICE) || (_considerExecution && sliceType.equals(SliceType.BACKWARD_SLICE))
				|| (!_considerExecution && sliceType.equals(SliceType.FORWARD_SLICE))) {
			final Collection<ValueBox> _valueBoxes = directionSensitiveInfo.retrieveValueBoxesToTransformExpr(_vBox, _stmt);

			// include any sub expressions and generate criteria from them
			transformAndGenerateToConsiderExecution(_stmt, _method, _valueBoxes);

			if (_value instanceof InvokeExpr) {
				generateCriteriaForInvokeExprIn(_stmt, _method);
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Transforming expr criteria: " + _value + " at " + _stmt + " in " + _method);
		}
	}

	/**
	 * Transforms the given method and generates suitable slice criteria based on various dependences.
	 * 
	 * @param method of interest.
	 * @pre method != null
	 */
	private void transformAndGenerateNewCriteriaForMethod(final SootMethod method) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("transformAndGenerateNewCriteriaForMethod(SootMethod method = " + method + ") - BEGIN");
		}

		generateCriteriaForTheCallToMethod(method);
		generateCriteriaBasedOnStmtLevelDependences(null, method, controlflowBasedDAs);

		if (callStackCache == null) {
			markAsCollectedAllInvocationSites(method);
		} else {
			recordCallStackForMethod(method);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("transformAndGenerateNewCriteriaForMethod() - END");
		}
	}

	/**
	 * Transforms the given statement and generates new criteria. The given statement is only collected if
	 * <code>considerExecution</code> is <code>true</code>.
	 * 
	 * @param stmt is the statement-level slice criterion.
	 * @param method is the method in which <code>stmt</code> occurs.
	 * @param considerExecution <code>true</code> indicates that the effect of executing this criterion should be considered
	 *            while slicing. This means all the expressions of the associated statement are also considered as slice
	 *            criteria. <code>false</code> indicates that just the mere effect of the control reaching this criterion
	 *            should be considered while slicing. This means none of the expressions of the associated statement are
	 *            considered as slice criteria.
	 */
	private void transformAndGenerateNewCriteriaForStmt(final Stmt stmt, final SootMethod method,
			final boolean considerExecution) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Transforming stmt criteria: " + stmt + "[" + considerExecution + "] in " + method + "   "
					+ callStackCache);
		}

		// transform the statement
		if (sliceType.equals(SliceType.COMPLETE_SLICE) || (considerExecution && sliceType.equals(SliceType.BACKWARD_SLICE))
				|| (!considerExecution && sliceType.equals(SliceType.FORWARD_SLICE))) {
			directionSensitiveInfo.processNewExpr(stmt, method);

			final Collection<ValueBox> _valueBoxes = directionSensitiveInfo.retrieveValueBoxesToTransformStmt(stmt);
			transformAndGenerateToConsiderExecution(stmt, method, _valueBoxes);

			if (stmt.containsInvokeExpr()) {
				generateCriteriaForInvokeExprIn(stmt, method);
			}
		}

		// capture control flow based dependences.
		if (isNotIncludedInSlice(stmt)) {
			generateCriteriaBasedOnStmtLevelDependences(stmt, method, controlflowBasedDAs);
		}

		// generate new slice criteria
		generateCriteriaForTheCallToMethod(method);

		// collect the statement
		includeInSlice(stmt);

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
	 * @pre stmt != null and method != null and vBoxes != null
	 * @pre vBoxes.oclIsKindOf(Collection(ValueBox))
	 * @pre stmt.getUseAndDefBoxes().containsAll(valueBoxes)
	 */
	private void transformAndGenerateToConsiderExecution(final Stmt stmt, final SootMethod method,
			final Collection<ValueBox> vBoxes) {
		if (LOGGER.isDebugEnabled()) {
			final StringBuffer _sb = new StringBuffer();
			_sb.append("BEGIN: Transforming value boxes [");

			for (final Iterator<ValueBox> _i = vBoxes.iterator(); _i.hasNext();) {
				final ValueBox _vBox = _i.next();
				_sb.append(_vBox.getValue());
				_sb.append("[" + _vBox + "]");
				_sb.append(", ");
			}
			_sb.append("]");
			LOGGER.debug(_sb.toString());
		}

		final Collection<Type> _types = new HashSet<Type>();
		final Collection<IDependencyAnalysis<?, ?, ?, ?, ?, ?>> _das = new ArrayList<IDependencyAnalysis<?, ?, ?, ?, ?, ?>>();
		final Collection<ValueBox> _locals = new HashSet<ValueBox>();

		for (final Iterator<ValueBox> _i = vBoxes.iterator(); _i.hasNext();) {
			final ValueBox _vBox = _i.next();

			if (isNotIncludedInSlice(_vBox)) {
				includeInSlice(_vBox);

				final Value _value = _vBox.getValue();

				if (_value instanceof ParameterRef) {
					directionSensitiveInfo.processParameterRef((IdentityStmt) stmt, method);
				} else if (_value instanceof FieldRef || _value instanceof ArrayRef) {
					_das.addAll((Collection<IDependencyAnalysis<?, ?, ?, ?, ?, ?>>) controller
							.getAnalyses(IDependencyAnalysis.DependenceSort.REFERENCE_BASED_DATA_DA));

					if (useInterferenceDACache) {
						_das.addAll((Collection<IDependencyAnalysis<?, ?, ?, ?, ?, ?>>) controller
								.getAnalyses(IDependencyAnalysis.DependenceSort.INTERFERENCE_DA));
					}

					if (_value instanceof FieldRef) {
						final SootField _field = ((FieldRef) _vBox.getValue()).getField();
						includeInSlice(_field);
						includeInSlice(_field.getDeclaringClass());
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
			generateCriteriaBasedOnStmtLevelDependences(stmt, method, _das);
		}

		// create new criteria based on program point level dependence (identifier based dependence).
		generateCriteriaForLocals(_locals, stmt, method);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Transforming value boxes");
		}
	}
}

// End of File
