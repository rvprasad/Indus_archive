
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

import soot.Body;
import soot.SootClass;
import soot.SootMethod;
import soot.Trap;
import soot.Value;
import soot.ValueBox;

import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.FieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.NewExpr;
import soot.jimple.ParameterRef;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;

import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.staticanalyses.AnalysesController;
import edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.Init2NewExprMapper;
import edu.ksu.cis.indus.staticanalyses.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.staticanalyses.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraph;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraphMgr;
import edu.ksu.cis.indus.staticanalyses.support.FIFOWorkBag;
import edu.ksu.cis.indus.staticanalyses.support.IWorkBag;
import edu.ksu.cis.indus.staticanalyses.support.Pair;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * This class accepts slice criterions and generates slices of the given system.
 * 
 * <p>
 * The term "immediate slice" in the context of this file implies the slice containing only entities on which the given term
 * depends on, not the transitive closure.
 * </p>
 * 
 * <p>
 * There are 2 flavours of executable slicing: forward and backward.  Backward slicing is inclusion of anything that leads to
 * the slice criterion from the given entry points to the system.  This can provide a executable system which will  simulate
 * the given system along all paths from the entry points leading to the slice criterion independent of the input.   In case
 * the input causes a divergence in this path then the simulation ends there.
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
 * Due to the above view we only support backward and complete slicing.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class SlicingEngine {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(SlicingEngine.class);

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

	static {
		Collection c = new HashSet();
		c.add(BACKWARD_SLICE);
		c.add(FORWARD_SLICE);
		c.add(COMPLETE_SLICE);
		SLICE_TYPES = Collections.unmodifiableCollection(c);
	}

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private static final int CAPACITY = 30;

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
	private final IWorkBag workbag = new FIFOWorkBag();

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
	private Map method2body =
		new LinkedHashMap(CAPACITY, 0.7f, true) {
			protected boolean removeEldestEntry(final Map.Entry eldest) {
				return size() > CAPACITY;
			}
		};

	/**
	 * This transforms the system based on the slicing decision of this object.
	 */
	private TaggingBasedSliceCollector transformer;

	/**
	 * This indicates if ready dependence should be used.
	 */
	private boolean useReady;

	/**
	 * Creates a new SlicingEngine object.
	 */
	public SlicingEngine() {
		transformer = new TaggingBasedSliceCollector(this);
	}

	/**
	 * Sets the the id's of the dependence analyses to be used for slicing.  The analyses are provided by the given
	 * controller that was used to initialize the analyses. to
	 *
	 * @param ctrl provides dependency information required for slicing.
	 * @param dependenciesToUse is the ids of the dependecies to be considered for slicing.
	 *
	 * @pre ctrl != null and dependenciesToUse !=     null
	 * @pre dependeciesToUse->forall(o | controller.getAnalysis(o) != null)
	 */
	public void setAnalysesControllerAndDependenciesToUse(final AnalysesController ctrl, final Collection dependenciesToUse) {
		controller = ctrl;
		dependencies.addAll(dependenciesToUse);
		intraProceduralDependencies.clear();

		for (Iterator i = dependencies.iterator(); i.hasNext();) {
			Object id = i.next();

			if (id.equals(DependencyAnalysis.IDENTIFIER_BASED_DATA_DA)
				  || id.equals(DependencyAnalysis.SYNCHRONIZATION_DA)
				  || id.equals(DependencyAnalysis.CONTROL_DA)
				  || id.equals(DependencyAnalysis.DIVERGENCE_DA)) {
				intraProceduralDependencies.addAll(controller.getAnalyses(id));
			}

			if (id.equals(DependencyAnalysis.READY_DA)) {
				useReady = true;
			} else {
				useReady = false;
			}
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

		for (Iterator i = sliceCriteria.iterator(); i.hasNext();) {
			Object o = i.next();

			if (!SliceCriteriaFactory.isSlicingCriterion(o)) {
				LOGGER.error("The work piece is not a subtype of AbstractSliceCriterion" + o);
				throw new IllegalStateException("The work piece is not a subtype of AbstractSliceCriterion" + o);
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
		transformer.setTagName(tagName);
	}

	/**
	 * Initializes the slicer by checking compatibility of the configured components.
	 *
	 * @throws IllegalStateException when the given slice type cannot be handled by the configured transformer.
	 */
	public void initialize() {
		if (!transformer.handleSliceType(sliceType, executableSlice)) {
			throw new IllegalStateException(
				"The given slice type cannot be handled by the configured transformer. Change the "
				+ "slice type or provide a suitable transformer.");
		}
	}

	/**
	 * Resets internal data structures and removes all references to objects provided at initialization time. For other
	 * operations to be meaningful following a call to this method, the user should call <code>initialize</code> before
	 * calling any other methods.
	 */
	public void reset() {
		cgi = null;
		transformer.reset();
		criteria.clear();

		// clear the work bag of slice criterion
		while (workbag.hasWork()) {
			AbstractSliceCriterion work = (AbstractSliceCriterion) workbag.getWork();
			work.sliced();
		}
	}

	/**
	 * Slices the system provided at initialization for the initialized criteria to generate the given type of slice..
	 */
	public void slice() {
		transformer.processSeedCriteria(criteria);
		workbag.addAllWorkNoDuplicates(criteria);

		// we are assuming the mapping will capture the past-processed information to prevent processed criteria from 
		// reappearing.  
		while (workbag.hasWork()) {
			AbstractSliceCriterion work = (AbstractSliceCriterion) workbag.getWork();

			if (work instanceof SliceExpr) {
				transformAndGenerateNewCriteriaForExpr((SliceExpr) work);
			} else if (work instanceof SliceStmt) {
				SliceStmt temp = (SliceStmt) work;
				SootMethod sm = temp.getOccurringMethod();
				transformAndGenerateNewCriteriaForStmt((Stmt) temp.getCriterion(), sm, temp.shouldConsiderExecution());
			}
			work.sliced();
		}

		transformer.completeTransformation();
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
	 * @param method DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	private Body getStmtListFor(final SootMethod method) {
		Body result;
		result = (Body) method2body.get(method);

		if (result == null) {
			result = method.getActiveBody();

			if (result != null) {
				method2body.put(method, result);
			}
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
		Collection newCriteria = new HashSet();

		if (sliceType.equals(COMPLETE_SLICE)) {
			for (Iterator i = das.iterator(); i.hasNext();) {
				DependencyAnalysis da = (DependencyAnalysis) i.next();
				newCriteria.addAll(da.getDependents(stmt, method));
				newCriteria.addAll(da.getDependees(stmt, method));
			}
		} else if (sliceType.equals(BACKWARD_SLICE)) {
			for (Iterator i = das.iterator(); i.hasNext();) {
				DependencyAnalysis da = (DependencyAnalysis) i.next();
				newCriteria.addAll(da.getDependees(stmt, method));
			}
		} else if (sliceType.equals(FORWARD_SLICE)) {
			for (Iterator i = das.iterator(); i.hasNext();) {
				DependencyAnalysis da = (DependencyAnalysis) i.next();
				newCriteria.addAll(da.getDependents(stmt, method));
			}
		}

		for (Iterator i = newCriteria.iterator(); i.hasNext();) {
			Object o = i.next();
			Stmt unslicedStmt;
			SootMethod unslicedMethod;

			if (o instanceof Pair) {
				Pair pair = (Pair) o;
				unslicedStmt = (Stmt) pair.getFirst();
				unslicedMethod = (SootMethod) pair.getSecond();
			} else {
				unslicedStmt = (Stmt) o;
				unslicedMethod = method;
			}

			if (transformer.getTransformed(unslicedStmt, unslicedMethod) == null) {
				// consider it as a slice criterion
				SliceStmt sliceCriterion = SliceStmt.getSliceStmt();
				sliceCriterion.initialize(unslicedMethod, unslicedStmt, true);
				workbag.addWorkNoDuplicates(sliceCriterion);

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Adding " + unslicedStmt + " in " + unslicedMethod.getSignature() + " to workbag.");
				}
			}
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
		Collection das = controller.getAnalyses(dependenceId);

		if (!das.isEmpty()) {
			generateNewCriteria(stmt, method, das);
		}
	}

	/**
	 * Generates new slice criteria based on what affects the given occurrence of the invoke expression.  By nature of
	 * Jimple, only one invoke expression can occur in a statement, hence, the arguments.  This treats constructors in a
	 * different way as it will generate slice criteria to consider the class initializers as well.
	 *
	 * @param stmt in which the field occurs.
	 * @param method in which <code>stmt</code> occurs.
	 *
	 * @pre stmt != null and method != null
	 * @pre stmt.containsInvokeExpr() == true
	 */
	private void generateNewCriteriaForInvocation(final Stmt stmt, final SootMethod method) {
		InvokeExpr expr = stmt.getInvokeExpr();
		Collection callees = new HashSet();

		if (expr instanceof SpecialInvokeExpr && expr.getMethod().getName().equals("<init>")) {
			callees.add(expr.getMethod());

			SootClass sc = expr.getMethod().getDeclaringClass();

			if (sc.declaresMethodByName("<clinit>")) {
				callees.add(sc.getMethodByName("<clinit>"));
			}
		} else {
			Context context = new Context();
			context.setRootMethod(method);
			context.setStmt(stmt);
			callees.addAll(cgi.getCallees(expr, context));
		}

		generateNewCriteriaForReturnPointOfMethods(callees);

		if (useReady) {
			generateNewCriteriaBasedOnDependence(stmt, method, DependencyAnalysis.READY_DA);
		}
	}

	/**
	 * Generates new slicing criteria which captures inter-procedural dependences due to call-sites.
	 *
	 * @param pBox is the parameter reference to be sliced on.
	 * @param method in which <code>pBox</code> occurs.
	 *
	 * @pre pBox != null and method != null
	 */
	private void generateNewCriteriaForParam(final ValueBox pBox, final SootMethod method) {
		/*
		 * Note that this will cause us to include all caller sites as slicing criteria which may not be what the user
		 * intended.
		 */
		ParameterRef param = (ParameterRef) pBox.getValue();
		int index = param.getIndex();

		for (Iterator i = cgi.getCallers(method).iterator(); i.hasNext();) {
			CallTriple ctrp = (CallTriple) i.next();
			SootMethod caller = ctrp.getMethod();
			Stmt stmt = ctrp.getStmt();
			ValueBox argBox = ctrp.getStmt().getInvokeExpr().getArgBox(index);

			if (transformer.getTransformed(argBox, stmt, method) == null) {
				SliceExpr critExpr = SliceExpr.getSliceExpr();
				critExpr.initialize(caller, stmt, argBox, true);
				workbag.addWorkNoDuplicates(critExpr);

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Adding expr " + argBox.getValue() + " at " + stmt + " in " + caller.getSignature()
						+ " to workbag.");
				}
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param callees DOCUMENT ME!
	 */
	private void generateNewCriteriaForReturnPointOfMethods(final Collection callees) {
		// add exit points of callees as the slice criteria
		for (Iterator i = callees.iterator(); i.hasNext();) {
			SootMethod callee = (SootMethod) i.next();
			BasicBlockGraph bbg = slicedBBGMgr.getBasicBlockGraph(callee);

			if (bbg == null) {
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("No basic block graph available for " + callee.getSignature() + ". Moving on.");
				}
				continue;
			}

			for (Iterator j = bbg.getTails().iterator(); j.hasNext();) {
				BasicBlock bb = (BasicBlock) j.next();
				Stmt trailer = bb.getTrailerStmt();

				if (transformer.getTransformed(trailer, callee) == null) {
					SliceStmt sliceCriterion = SliceStmt.getSliceStmt();
					sliceCriterion.initialize(callee, trailer, true);
					workbag.addWorkNoDuplicates(sliceCriterion);

					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Adding " + trailer + " in " + callee.getSignature() + " to workbag.");
					}
				}
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param method DOCUMENT ME!
	 */
	private void generateNewCriteriaForTheCallToThisMethod(final SootMethod method) {
		for (Iterator i = cgi.getCallers(method).iterator(); i.hasNext();) {
			CallTriple ctrp = (CallTriple) i.next();
			SootMethod caller = ctrp.getMethod();
			Stmt stmt = ctrp.getStmt();

			if (transformer.getTransformed(stmt, method) == null) {
				SliceStmt critStmt = SliceStmt.getSliceStmt();
				critStmt.initialize(caller, stmt, false);
				workbag.addWorkNoDuplicates(critStmt);

				if (method instanceof InstanceInvokeExpr) {
					ValueBox baseBox = ((InstanceInvokeExpr) stmt.getInvokeExpr()).getBaseBox();

					if (transformer.getTransformed(baseBox, stmt, method) == null) {
						SliceExpr critExpr = SliceExpr.getSliceExpr();
						critExpr.initialize(caller, stmt, baseBox, true);
						workbag.addWorkNoDuplicates(critExpr);

						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("Adding expr " + baseBox.getValue() + " at " + stmt + " in " + caller.getSignature()
								+ " to workbag.");
						}
					}
				}
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
	 */
	private void generateNewCriteriaToConsiderException(final Stmt stmt, final SootMethod method) {
		/*
		 * check if the stmt is enclosed in a try-catch block.  If so, generate a criteria for the first statment of catch
		 * clause for that exception
		 */
		Body body = getStmtListFor(method);

		if (body != null) {
			List sl = body.getAllUnitBoxes();
			int index = sl.indexOf(stmt);

			for (Iterator i = body.getTraps().iterator(); i.hasNext();) {
				Trap trap = (Trap) i.next();

				if (sl.indexOf(trap.getBeginUnit()) <= index && index < sl.indexOf(trap.getEndUnit())) {
					transformer.transform((Stmt) trap.getHandlerUnit(), method);
				}
			}
		} else {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Could not get body for method " + method.getSignature());
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
	 */
	private void processForNewExpr(final Stmt stmt, final SootMethod method) {
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
			AssignStmt as = (AssignStmt) stmt;

			if (as.getRightOp() instanceof NewExpr) {
				Stmt def = initMapper.getInitCallStmtForNewExprStmt(stmt, method);
				transformAndGenerateNewCriteriaForStmt(def, method, true);
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
		Collection das = controller.getAnalyses(DependencyAnalysis.IDENTIFIER_BASED_DATA_DA);

		for (Iterator i = vBoxes.iterator(); i.hasNext();) {
			ValueBox vBox = (ValueBox) i.next();

			if (transformer.getTransformed(vBox, stmt, method) == null) {
				transformer.transform(vBox, stmt, method);

				if (vBox.getValue() instanceof ParameterRef) {
					generateNewCriteriaForParam(vBox, method);
					generateNewCriteriaForTheCallToThisMethod(method);
				} else if (vBox.getValue() instanceof StaticFieldRef) {
					SootClass sc = ((StaticFieldRef) vBox.getValue()).getField().getDeclaringClass();

					if (sc.declaresMethodByName("<clinit>")) {
						generateNewCriteriaForReturnPointOfMethods(Collections.singleton(sc.getMethodByName("<clinit>")));
					}
				}
			}
		}

		// create new slice criteria
		generateNewCriteria(stmt, method, das);
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
		Stmt stmt = sExpr.getOccurringStmt();
		SootMethod method = sExpr.getOccurringMethod();
		ValueBox expr = (ValueBox) sExpr.getCriterion();

		Value value = expr.getValue();
		transformAndGenerateCriteriaForVBoxes(value.getUseBoxes(), stmt, method);
		// include the statement to capture control dependency
		transformAndGenerateNewCriteriaForStmt(stmt, method, false);

		if (value instanceof InvokeExpr) {
			generateNewCriteriaForInvocation(stmt, method);
		} else if (value instanceof FieldRef || value instanceof ArrayRef) {
			generateNewCriteriaBasedOnDependence(stmt, method, DependencyAnalysis.INTERFERENCE_DA);
			generateNewCriteriaBasedOnDependence(stmt, method, DependencyAnalysis.REFERENCE_BASED_DATA_DA);
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
		if (considerExecution) {
			transformAndGenerateCriteriaForVBoxes(stmt.getUseAndDefBoxes(), stmt, method);
			processForNewExpr(stmt, method);
			transformer.transform(stmt, method);

			if (stmt.containsInvokeExpr()) {
				generateNewCriteriaForInvocation(stmt, method);
			} else if (stmt.containsArrayRef() || stmt.containsFieldRef()) {
				generateNewCriteriaBasedOnDependence(stmt, method, DependencyAnalysis.INTERFERENCE_DA);
				generateNewCriteriaBasedOnDependence(stmt, method, DependencyAnalysis.REFERENCE_BASED_DATA_DA);
			} else if (useReady && (stmt instanceof EnterMonitorStmt || stmt instanceof ExitMonitorStmt)) {
				generateNewCriteriaBasedOnDependence(stmt, method, DependencyAnalysis.READY_DA);
			}
			generateNewCriteriaForTheCallToThisMethod(method);
			generateNewCriteriaToConsiderException(stmt, method);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Sliced : " + stmt + " [" + considerExecution + "] | " + method);
		}

		// create new slice criteria
		generateNewCriteria(stmt, method, intraProceduralDependencies);
	}
}

/*
   ChangeLog:
   $Log$
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
