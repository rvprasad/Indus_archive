
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

import edu.ksu.cis.indus.common.graph.BasicBlockGraph;
import edu.ksu.cis.indus.common.graph.BasicBlockGraphMgr;
import edu.ksu.cis.indus.common.graph.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.common.structures.Pair;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IPoolable;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;

import edu.ksu.cis.indus.processing.Context;

import edu.ksu.cis.indus.staticanalyses.AnalysesController;
import edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.Init2NewExprMapper;
import edu.ksu.cis.indus.common.graph.IWorkBag;
import edu.ksu.cis.indus.common.graph.PoolAwareFIFOWorkBag;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Value;
import soot.ValueBox;
import soot.VoidType;

import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.FieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.NewExpr;
import soot.jimple.ParameterRef;
import soot.jimple.Stmt;

import soot.tagkit.AbstractHost;
import soot.tagkit.Host;


/**
 * This class accepts slice criterions and generates slices of the given system.
 * 
 * <p>
 * The term "immediate slice" in the context of this file implies the slice containing only entities on which the given term
 * depends on, not the transitive closure.
 * </p>
 * 
 * <p>
 * There are 3 types of slices: forward, backward, and complete(forward and backward).  Also, there are 2  flavours of
 * slices: executable and non-executable.
 * </p>
 * 
 * <p>
 * Backward slicing is inclusion of anything that leads to the slice criterion from the given entry points to the system.
 * This can provide a executable system which will  simulate the given system along all paths from the entry points leading
 * to the slice criterion independent of the input. In case the input causes a divergence in this path then the simulation
 * ends there.
 * </p>
 * 
 * <p>
 * However, in case of forward slicing, one would include everything that is affected by the slice criterion.  This  will
 * never lead to an semantically meaningful executable slice as the part of the system that leads to the slice criterion is
 * not captured. Rather a more meaningful notion is that of a complete slice. This includes everything that affects the
 * given slice criterion and  everything affected by the slice criterion.
 * </p>
 * 
 * <p>
 * Due to the above view we only support non-executable slices of all types and only executable slices of backward and
 * complete type.
 * </p>
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
	private static final Log LOGGER = LogFactory.getLog(SlicingEngine.class);

	static {
		final Collection _c = new HashSet();
		_c.add(BACKWARD_SLICE);
		_c.add(FORWARD_SLICE);
		_c.add(COMPLETE_SLICE);
		SLICE_TYPES = Collections.unmodifiableCollection(_c);
	}

	/**
	 * This is the basic block graph manager which manages the BB graphs corresponding to the system being sliced/cloned.
	 */
	BasicBlockGraphMgr slicedBBGMgr;

	/**
	 * The direction of the slice.  It's default value is <code>BACKWARD_SLICE</code>.
	 *
	 * @invariant sliceTypes.contains(sliceType)
	 */
	Object sliceType = BACKWARD_SLICE;

	/**
	 * This indicates if executable slices should be generated.
	 */
	boolean executableSlice;

	/**
	 * The controller used to access the dependency analysis info during slicing.
	 */
	private AnalysesController controller;

	/**
	 * The collection of slice criteria.
	 *
	 * @invariant criteria != null and criteria->forall(o | o.oclIsKindOf(AbstractSliceCriterion))
	 */
	private Collection criteria = new HashSet();

	/**
	 * The ids of the dependencies to be considered for slicing.
	 */
	private final Collection dependencies = new HashSet();

	/**
	 * The dependency analyses to be considered for intra-procedural slicing.
	 */
	private final Collection intraProceduralDependencies = new ArrayList();

	/**
	 * The work bag used during slicing.
	 *
	 * @invariant workbag != null and workbag.oclIsKindOf(Bag)
	 * @invariant workbag->forall(o | o.oclIsKindOf(AbstractSliceCriterion))
	 */
	private final IWorkBag workbag = new PoolAwareFIFOWorkBag();

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private Collection controlDAs = new ArrayList();

	/**
	 * This provides the call graph information in the system being sliced.
	 */
	private ICallGraphInfo cgi;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private Init2NewExprMapper initMapper;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private Map method2params = new HashMap();

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private Set invoked = new HashSet();

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private Set required = new HashSet();

	/**
	 * This collects the parts of the system that make up the slice.
	 */
	private TaggingBasedSliceCollector collector;

	/**
	 * This indicates if ready dependence should be used.
	 */
	private boolean useReady;

	/**
	 * Creates a new SlicingEngine object.
	 */
	public SlicingEngine() {
		collector = new TaggingBasedSliceCollector(this);
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
		dependencies.addAll(dependenciesToUse);
		intraProceduralDependencies.clear();
		controlDAs.clear();

		for (final Iterator _i = dependencies.iterator(); _i.hasNext();) {
			final Object _id = _i.next();

			if (_id.equals(DependencyAnalysis.IDENTIFIER_BASED_DATA_DA)
				  || _id.equals(DependencyAnalysis.SYNCHRONIZATION_DA)
				  || _id.equals(DependencyAnalysis.DIVERGENCE_DA)) {
				intraProceduralDependencies.addAll(controller.getAnalyses(_id));
			} else if (_id.equals(DependencyAnalysis.CONTROL_DA)) {
				controlDAs.addAll(controller.getAnalyses(_id));
			}
		}

		if (dependenciesToUse.contains(DependencyAnalysis.READY_DA)) {
			useReady = true;
		} else {
			useReady = false;
		}
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
	 * Indicates to the slicer if executable slice should be generated.
	 *
	 * @param executable is <code>true</code> if executable slice is requested; <code>false</code>, otherwise.
	 */
	public void setExecutableSlice(final boolean executable) {
		executableSlice = executable;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param mapper DOCUMENT ME!
	 */
	public void setInitMapper(final Init2NewExprMapper mapper) {
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
		}
		criteria.addAll(sliceCriteria);
	}

	/**
	 * Sets the type of slice to be generated by the slicer.
	 *
	 * @param theSliceType is the type of slice requested.  This has to be one of<code>XXX_SLICE</code> values defined in
	 * 		  this class.
	 *
	 * @pre theSliceType != null
	 * @pre SLICE_TYPES.contains(theSliceType)
	 */
	public void setSliceType(final Object theSliceType) {
		sliceType = theSliceType;
	}

	/**
	 * Sets the basic block graph manager to be used during slicing.
	 *
	 * @param bbgMgr is the basic block graph manager for the system being sliced.
	 *
	 * @throws IllegalArgumentException DOCUMENT ME!
	 *
	 * @pre bbgMgr != null
	 */
	public void setSlicedBBGMgr(final BasicBlockGraphMgr bbgMgr) {
		if (!SLICE_TYPES.contains(sliceType)) {
			throw new IllegalArgumentException("The given slice type is not one of XXX_SLICE values defined in this class.");
		}

		slicedBBGMgr = bbgMgr;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param tagName DOCUMENT ME!
	 */
	public void setTagName(final String tagName) {
		collector.setTagName(tagName);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @throws IllegalStateException DOCUMENT ME!
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
		required.clear();
		invoked.clear();

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
			required.add(((AbstractSliceCriterion) _crit).getOccurringMethod());
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

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Required methods: " + required);
			LOGGER.debug("Invoked methods: " + invoked);
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	BasicBlockGraphMgr getSlicedBasicBlockGraphMgr() {
		return slicedBBGMgr;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param host1 DOCUMENT ME!
	 * @param host2 DOCUMENT ME!
	 */
	private void collect(final Host host1, final Host host2) {
		collector.collect(host1);

		if (host1 instanceof SootMethod && !marked((SootMethod) host1)) {
			invoked.add(host1);
		}
		collector.collect(host2);

		if (host2 instanceof SootMethod && !marked((SootMethod) host2)) {
			invoked.add(host2);
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param callee DOCUMENT ME!
	 * @param caller DOCUMENT ME!
	 * @param stmt DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 *
	 * @throws RuntimeException DOCUMENT ME!
	 */
	private boolean considerMethodEntranceForCriteriaGeneration(final SootMethod callee, final SootMethod caller,
		final Stmt stmt) {
		boolean result = false;

		if (markedAsRequired(callee)) {
			invoked.remove(caller);
			required.add(caller);
			result = true;
		} else {
			if (invoked.contains(callee)) {
				result = marked(caller) && collector.hasBeenCollected(stmt.getInvokeExprBox());
			} else {
				throw new RuntimeException("How can this happen?" + callee + " was unmarked but was being processed.");
			}
		}
		return result;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param callee DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	private boolean considerMethodExitForCriteriaGeneration(final SootMethod callee) {
		boolean result = false;

		if (!marked(callee)) {
			invoked.add(callee);
			result = true;
		}
		return result;
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
	 * @pre das.oclIsKindOf(Collection(DependencyAnalysis))
	 * @post workbag$pre.getWork() != workbag.getWork() or workbag$pre.getWork() == workbag.getWork()
	 */
	private void generateNewCriteria(final Stmt stmt, final SootMethod method, final Collection das) {
		final Collection _newCriteria = new HashSet();

		if (sliceType.equals(COMPLETE_SLICE)) {
			for (final Iterator _i = das.iterator(); _i.hasNext();) {
				final DependencyAnalysis _da = (DependencyAnalysis) _i.next();
				_newCriteria.addAll(_da.getDependents(stmt, method));
				_newCriteria.addAll(_da.getDependees(stmt, method));
			}
		} else if (sliceType.equals(BACKWARD_SLICE)) {
			for (final Iterator _i = das.iterator(); _i.hasNext();) {
				final DependencyAnalysis _da = (DependencyAnalysis) _i.next();
				_newCriteria.addAll(_da.getDependees(stmt, method));
			}
		} else if (sliceType.equals(FORWARD_SLICE)) {
			for (final Iterator _i = das.iterator(); _i.hasNext();) {
				final DependencyAnalysis _da = (DependencyAnalysis) _i.next();
				_newCriteria.addAll(_da.getDependents(stmt, method));
			}
		}

		for (final Iterator _i = _newCriteria.iterator(); _i.hasNext();) {
			final Object _o = _i.next();
			Stmt stmtToBeIncluded;
			SootMethod methodToBeIncluded;

			if (_o instanceof Pair) {
				final Pair _pair = (Pair) _o;
				stmtToBeIncluded = (Stmt) _pair.getFirst();
				methodToBeIncluded = (SootMethod) _pair.getSecond();
			} else {
				stmtToBeIncluded = (Stmt) _o;
				methodToBeIncluded = method;
			}
			generateSliceStmtCriterion(stmtToBeIncluded, methodToBeIncluded, true);
		}
	}

	/**
	 * Generates new slice criteria based on the specified dependence.
	 *
	 * @param stmt in which expression which leads to dependence occurs.
	 * @param method in which <code>stmt</code> occurs.
	 * @param dependenceId identifies the dependence based on which the slice criteria should be generated.
	 *
	 * @pre stmt != null and method != null and dependenceId != null
	 */
	private void generateNewCriteriaBasedOnDependence(final Stmt stmt, final SootMethod method, final Object dependenceId) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Generating criteria for based on dependence:" + dependenceId);
		}

		final Collection _das = controller.getAnalyses(dependenceId);

		if (!_das.isEmpty()) {
			generateNewCriteria(stmt, method, _das);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Generating criteria for based on dependence:" + dependenceId);
		}
	}

	/**
	 * Generates new slice criteria based on what affects the given occurrence of the invoke expression.  By nature of
	 * Jimple, only one invoke expression can occur in a statement, hence, the arguments.
	 *
	 * @param stmt in which the field occurs.
	 * @param method in which <code>stmt</code> occurs.
	 *
	 * @pre stmt != null and method != null
	 * @pre stmt.containsInvokeExpr() == true
	 */
	private void generateNewCriteriaForInvocation(final Stmt stmt, final SootMethod method) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Generating criteria for invocation expressions (caller-callee)");
		}

		final InvokeExpr _expr = stmt.getInvokeExpr();
		final Context _context = new Context();
		_context.setRootMethod(method);
		_context.setStmt(stmt);

		final Collection _callees = cgi.getCallees(_expr, _context);
		final Collection _temp = new HashSet(_callees);

		for (final Iterator _i = _callees.iterator(); _i.hasNext();) {
			final SootMethod _sm = (SootMethod) _i.next();
			final SootClass _sc = _sm.getDeclaringClass();

			if (_sc.declaresMethodByName("<clinit>") && !collector.hasBeenCollected(_sc.getMethodByName("<clinit>"))) {
				_temp.add(_sc.getMethodByName("<clinit>"));
			}
		}

		generateNewCriteriaForReturnPointOfMethods(_callees, stmt, method);

		if (useReady) {
			generateNewCriteriaBasedOnDependence(stmt, method, DependencyAnalysis.READY_DA);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Generating criteria for invocation expressions (caller-callee)");
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

		BitSet params = (BitSet) method2params.get(callee);

		if (params == null) {
			final int _maxArguments = 8;
			params = new BitSet(_maxArguments);
			method2params.put(callee, params);
		}
		params.set(_param.getIndex());

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Parameters required for " + callee + " are " + params);
		}

		for (final Iterator _i = cgi.getCallers(callee).iterator(); _i.hasNext();) {
			final CallTriple _ctrp = (CallTriple) _i.next();
			final SootMethod _caller = _ctrp.getMethod();
			final Stmt _stmt = _ctrp.getStmt();

			if (considerMethodEntranceForCriteriaGeneration(callee, _caller, _stmt)) {
				final ValueBox _argBox = _ctrp.getExpr().getArgBox(_index);

				generateSliceExprCriterion(_argBox, _stmt, _caller, true);
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Generating criteria for parameters");
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param callees DOCUMENT ME!
	 * @param stmt DOCUMENT ME!
	 * @param caller DOCUMENT ME!
	 */
	private void generateNewCriteriaForReturnPointOfMethods(final Collection callees, final Stmt stmt, final SootMethod caller) {
		final boolean _considerReturnValue = !(stmt instanceof InvokeStmt);
		final InvokeExpr _expr = stmt.getInvokeExpr();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Generating criteria for return points " + _considerReturnValue);
		}

		// add exit points of callees as the slice criteria
		for (final Iterator _i = callees.iterator(); _i.hasNext();) {
			final SootMethod _callee = (SootMethod) _i.next();
			final BasicBlockGraph _bbg = slicedBBGMgr.getBasicBlockGraph(_callee);

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

			final BitSet _params = (BitSet) method2params.get(_callee);

			if (considerMethodExitForCriteriaGeneration(_callee) || _params == null) {
				processReturnStmtInInit(_callee, _bbg);

				for (final Iterator _j = _bbg.getTails().iterator(); _j.hasNext();) {
					final BasicBlock _bb = (BasicBlock) _j.next();
					final Stmt _trailer = _bb.getTrailerStmt();
					generateSliceStmtCriterion(_trailer, _callee, _considerReturnValue);
				}
			} else if (_callee.getParameterCount() > 0) {
				System.err.println("------" + _params + " ---- " + caller);

				for (int i = _params.nextSetBit(0); i >= 0; i = _params.nextSetBit(i + 1)) {
					generateSliceExprCriterion(_expr.getArgBox(i), stmt, caller, true);
				}
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Generating criteria for return points");
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param callee DOCUMENT ME!
	 */
	private void generateNewCriteriaForTheCallToEnclosingMethod(final SootMethod callee) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Generating criteria for call-sites (callee-caller)");
		}

		// generate criteria to include invocation sites only if the method has not been collected.
		if (!collector.hasBeenCollected(callee)) {
			collect(callee, callee.getDeclaringClass());

			final boolean _notStatic = !callee.isStatic();

			for (final Iterator _i = cgi.getCallers(callee).iterator(); _i.hasNext();) {
				final CallTriple _ctrp = (CallTriple) _i.next();
				final SootMethod _caller = _ctrp.getMethod();
				final Stmt _stmt = _ctrp.getStmt();

				if (considerMethodEntranceForCriteriaGeneration(callee, _caller, _stmt)) {
					generateSliceStmtCriterion(_stmt, _caller, false);
					generateSliceExprCriterion(_stmt.getInvokeExprBox(), _stmt, _caller, false);

					if (_notStatic) {
						final ValueBox _vBox = ((InstanceInvokeExpr) _stmt.getInvokeExpr()).getBaseBox();
						generateSliceExprCriterion(_vBox, _stmt, _caller, true);
					}
				}
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Generating criteria for call-sites (callee-caller)");
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param valueBox DOCUMENT ME!
	 * @param stmt DOCUMENT ME!
	 * @param method DOCUMENT ME!
	 * @param considerExecution DOCUMENT ME!
	 */
	private void generateSliceExprCriterion(final ValueBox valueBox, final Stmt stmt, final SootMethod method,
		final boolean considerExecution) {
		if (!marked(method)) {
			invoked.add(method);
		}

		if (!collector.hasBeenCollected(valueBox)) {
			final SliceExpr _sliceCriterion = SliceExpr.getSliceExpr();
			_sliceCriterion.initialize(method, stmt, valueBox);
			_sliceCriterion.setConsiderExecution(considerExecution);
			workbag.addWorkNoDuplicates(_sliceCriterion);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Adding expr " + valueBox.getValue() + " at " + stmt + " in " + method.getSignature()
					+ " to workbag.");
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param stmt DOCUMENT ME!
	 * @param method DOCUMENT ME!
	 * @param considerExecution DOCUMENT ME!
	 */
	private void generateSliceStmtCriterion(final Stmt stmt, final SootMethod method, final boolean considerExecution) {
		if (!marked(method)) {
			invoked.add(method);
		}

		if (!collector.hasBeenCollected(stmt)) {
			final SliceStmt _sliceCriterion = SliceStmt.getSliceStmt();
			_sliceCriterion.initialize(method, stmt);
			_sliceCriterion.setConsiderExecution(considerExecution);
			workbag.addWorkNoDuplicates(_sliceCriterion);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Adding " + stmt + " in " + method.getSignature() + " to workbag.");
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param method DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	private boolean marked(final SootMethod method) {
		return required.contains(method) || invoked.contains(method);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param method DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	private boolean markedAsRequired(final SootMethod method) {
		return required.contains(method);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param stmt DOCUMENT ME!
	 * @param method DOCUMENT ME!
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
	 * DOCUMENT ME!
	 *
	 * @param callee DOCUMENT ME!
	 * @param bbg DOCUMENT ME!
	 */
	private void processReturnStmtInInit(final SootMethod callee, final BasicBlockGraph bbg) {
		/*
		 * if we are sucking in an init we better suck in the super <init> invoke expression as well. By JLS, this has to
		 * be the first statement in the constructor.  However, if it accepts arguments, the arguments will be set up
		 * before the call.  Hence, it is safe to suck in the first <init> invoke expression in the <init> method being
		 * sucked in.  However, care must be taken to suck in the first <init> invocation that is invokes <init> from the same
		 * class as the enclosing <init> method. As we process invocation expressions, we are bound to suck in any other
		 * required <init>'s from other higher super classes.
		 */
		if (callee.getName().equals("<init>")) {
			final SootClass _sc1 = callee.getDeclaringClass();

			for (final Iterator _j = bbg.getHead().getStmtsOf().iterator(); _j.hasNext();) {
				final Stmt _stmt = (Stmt) _j.next();

				if (_stmt instanceof InvokeStmt) {
					final SootMethod _invokedMethod = _stmt.getInvokeExpr().getMethod();

					if (_invokedMethod.getName().equals("<init>")) {
						final SootClass _sc2 = _invokedMethod.getDeclaringClass();

						if (_sc1 == _sc2 || (_sc1.hasSuperclass() && _sc1.getSuperclass() == _sc2)) {
							generateSliceStmtCriterion(_stmt, callee, true);
							break;
						}
					}
				}
			}
		}
	}

	/**
	 * Transforms the given value boxes and generates new slice criteria based on what affects the given occurrence of the
	 * value boxes.
	 *
	 * @param vBoxes is collection of value boxes.
	 * @param stmt is the statement in which <code>vBoxes</code> occur.
	 * @param method is the method in which <code>stmt</code> occurs.
	 *
	 * @pre vBoxes != null and stmt != null and method != null
	 * @pre vBoxes.oclIsKindOf(Collection(ValueBoxes)) and stmt.getUseAndDefBoxes().containsAll(vBoxes)
	 */
	private void transformAndGenerateCriteriaForVBoxes(final Collection vBoxes, final Stmt stmt, final SootMethod method) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Transforming value boxes" + vBoxes);
		}

		final Collection _das = new ArrayList();
		_das.addAll(controller.getAnalyses(DependencyAnalysis.IDENTIFIER_BASED_DATA_DA));

		for (final Iterator _i = vBoxes.iterator(); _i.hasNext();) {
			final ValueBox _vBox = (ValueBox) _i.next();

			if (!collector.hasBeenCollected(_vBox)) {
				collector.collect(_vBox);

				final Value _value = _vBox.getValue();

				if (_value instanceof ParameterRef) {
					generateNewCriteriaForParam(_vBox, method);
					generateNewCriteriaForTheCallToEnclosingMethod(method);
				} else if (_value instanceof FieldRef || _value instanceof ArrayRef) {
					_das.addAll(controller.getAnalyses(DependencyAnalysis.REFERENCE_BASED_DATA_DA));
					_das.addAll(controller.getAnalyses(DependencyAnalysis.INTERFERENCE_DA));

					if (_value instanceof FieldRef) {
						final SootField _field = ((FieldRef) _vBox.getValue()).getField();
						collect(_field, _field.getDeclaringClass());
					}
				}
			}
		}

		// create new slice criteria
		generateNewCriteria(stmt, method, _das);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Transforming value boxes");
		}
	}

	/**
	 * Generates immediate slice for the given expression.
	 *
	 * @param sExpr is the expression-level slice criterion.
	 *
	 * @pre sExpr != null and sExpr.getOccurringStmt() != null and sExpr.getOccurringMethod() != null
	 * @pre sExpr.getCriterion() != null and sExpr.getCriterion().oclIsKindOf(ValueBox)
	 */
	private void transformAndGenerateNewCriteriaForExpr(final SliceExpr sExpr) {
		final Stmt _stmt = sExpr.getOccurringStmt();
		final SootMethod _method = sExpr.getOccurringMethod();
		final ValueBox _vBox = (ValueBox) sExpr.getCriterion();
		final Value _value = _vBox.getValue();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Transforming expr criteria: " + _vBox + " at " + _stmt + " in " + _method);
		}

		// collect the expresssion
		collector.collect(_vBox);

		// include any sub expressions and generate criteria from them
		transformAndGenerateCriteriaForVBoxes(_value.getUseBoxes(), _stmt, _method);

		// include the statement to capture control dependency and generate criteria from it
		transformAndGenerateNewCriteriaForStmt(_stmt, _method, false);

		// generate new slice criteria
		if (sExpr.isConsiderExecution()) {
			if (_value instanceof InvokeExpr) {
				generateNewCriteriaForInvocation(_stmt, _method);
			} else if (_value instanceof FieldRef || _value instanceof ArrayRef) {
				generateNewCriteriaBasedOnDependence(_stmt, _method, DependencyAnalysis.INTERFERENCE_DA);
				generateNewCriteriaBasedOnDependence(_stmt, _method, DependencyAnalysis.REFERENCE_BASED_DATA_DA);
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Transforming expr criteria: " + _vBox + " at " + _stmt + " in " + _method);
		}
	}

	/**
	 * Transforms the given statement and Generates new criteria.
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

		// collect the statement
		collector.collect(stmt);

		// transform the statement
		if (considerExecution) {
			transformAndGenerateCriteriaForVBoxes(stmt.getUseAndDefBoxes(), stmt, method);

			// generate new slice criteria
			processForNewExpr(stmt, method);

			if (stmt.containsInvokeExpr()) {
				generateNewCriteriaForInvocation(stmt, method);
			} else if (stmt.containsArrayRef() || stmt.containsFieldRef()) {
				generateNewCriteriaBasedOnDependence(stmt, method, DependencyAnalysis.INTERFERENCE_DA);
				generateNewCriteriaBasedOnDependence(stmt, method, DependencyAnalysis.REFERENCE_BASED_DATA_DA);
			} else if (useReady && (stmt instanceof EnterMonitorStmt || stmt instanceof ExitMonitorStmt)) {
				generateNewCriteriaBasedOnDependence(stmt, method, DependencyAnalysis.READY_DA);
			}
		}

		// generate new slice criteria
		generateNewCriteriaForTheCallToEnclosingMethod(method);
		generateNewCriteria(stmt, method, controlDAs);

		collector.collect(method);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Transforming stmt criteria: " + stmt + "[" + considerExecution + "] in " + method);
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.31  2003/12/08 12:20:48  venku
   - moved some classes from staticanalyses interface to indus interface package
   - ripple effect.

   Revision 1.30  2003/12/08 12:16:05  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.

   Revision 1.29  2003/12/07 22:13:12  venku
   - renamed methods in TaggingBasedSliceCollector.

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
   - moved ISliceCollector and TaggingBasedSliceCollector
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
