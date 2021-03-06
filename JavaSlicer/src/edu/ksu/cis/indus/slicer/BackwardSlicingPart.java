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

import edu.ksu.cis.indus.common.collections.IClosure;
import edu.ksu.cis.indus.common.collections.MapUtils;
import edu.ksu.cis.indus.common.collections.Stack;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Triple;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.staticanalyses.cfg.LocalUseDefAnalysisv2;
import edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis;
import edu.ksu.cis.indus.staticanalyses.dependency.IdentifierBasedDataDA;
import edu.ksu.cis.indus.staticanalyses.dependency.IdentifierBasedDataDAv2;
import edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis.Direction;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.NewExpr;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;

/**
 * This class provides the logic to detect parts of a backward slice.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class BackwardSlicingPart
		implements IDirectionSensitivePartOfSlicingEngine {

	/**
	 * This closure contains logic to generate criteria to include the value in the return statements.
	 * 
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private class ReturnValueInclusionClosure
			implements IClosure<Pair<Stmt, SootMethod>> {

		/**
		 * {@inheritDoc}
		 * 
		 * @see edu.ksu.cis.indus.common.collections.IClosure#execute(Object)
		 */
		public void execute(final Pair<Stmt, SootMethod> input) {
			final Stmt _trailer = input.getFirst();
			final SootMethod _callee = input.getSecond();

			if (_trailer instanceof ReturnStmt) {
				// TODO: we are considering both throws and returns as return points. This should change when we
				// consider if control-flow based on exceptions.
				engine.generateExprLevelSliceCriterion(((ReturnStmt) _trailer).getOpBox(), _trailer, _callee, true);
			}
		}
	}

	/**
	 * This closure contains logic to generate criteria to include return statements.
	 * 
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private class TailStmtInclusionClosure
			implements IClosure<Pair<Stmt, SootMethod>> {

		/**
		 * {@inheritDoc}
		 * 
		 * @see edu.ksu.cis.indus.common.collections.IClosure#execute(Object)
		 */
		public void execute(final Pair<Stmt, SootMethod> input) {
			final Stmt _stmt = input.getFirst();
			final SootMethod _callee = input.getSecond();
			engine.generateStmtLevelSliceCriterion(_stmt, _callee, false);
		}
	}

	/**
	 * This controls if the generated slice criterion either preserves the control leaving the criterion (true) or preserves
	 * the control reaching the criterion (false).
	 */
	private final boolean CONSIDER_EXECUTION = true;

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(BackwardSlicingPart.class);

	/**
	 * The engine with which this part is a part of.
	 */
	final SlicingEngine engine;

	/**
	 * this maps methods to their call sites.
	 */
	private final Map<SootMethod, Collection<Triple<Stmt, SootMethod, Stack<CallTriple>>>> callee2callsites = new HashMap<SootMethod, Collection<Triple<Stmt, SootMethod, Stack<CallTriple>>>>();

	/**
	 * This is a map from methods to transformed return statements.
	 */
	private final Map<SootMethod, Collection<Stmt>> exitTransformedMethods2exitpoints = new HashMap<SootMethod, Collection<Stmt>>();

	/**
	 * This maps methods to methods to a bitset that indicates which of the parameters of the method is required in the slice.
	 * 
	 * @invariant method2params.oclIsKindOf(Map(SootMethod, BitSet))
	 * @invariant method2params->forall(o | o.getValue().size() = o.getKey().getParameterCount())
	 */
	private final Map<SootMethod, BitSet> method2params = new HashMap<SootMethod, BitSet>();

	/**
	 * This closure is used generate criteria to include the value in return statements.
	 */
	private final IClosure<Pair<Stmt, SootMethod>> returnValueInclClosure = new ReturnValueInclusionClosure();

	/**
	 * This closure is used generate criteria to include return statements.
	 */
	private final IClosure<Pair<Stmt, SootMethod>> tailStmtInclusionClosure = new TailStmtInclusionClosure();

	/**
	 * Creates an instance of this class.
	 * 
	 * @param theEngine of which this part is a part of.
	 * @pre theEngine != null
	 */
	BackwardSlicingPart(final SlicingEngine theEngine) {
		engine = theEngine;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.slicer.IDirectionSensitivePartOfSlicingEngine#continueProcessing()
	 */
	public boolean continueProcessing() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("continueProcessing() - BEGIN");
		}

		boolean _result = false;

		for (final Iterator<Map.Entry<SootMethod, Collection<Triple<Stmt, SootMethod, Stack<CallTriple>>>>> _i = callee2callsites
				.entrySet().iterator(); _i.hasNext();) {
			final Map.Entry<SootMethod, Collection<Triple<Stmt, SootMethod, Stack<CallTriple>>>> _entry = _i.next();
			final SootMethod _callee = _entry.getKey();
			final BitSet _params = method2params.get(_callee);
			if (_params != null) {
				final Collection<Triple<Stmt, SootMethod, Stack<CallTriple>>> _temp = _entry.getValue();
				for (final Iterator<Triple<Stmt, SootMethod, Stack<CallTriple>>> _k = _temp.iterator(); _k.hasNext();) {
					final Triple<Stmt, SootMethod, Stack<CallTriple>> _triple = _k.next();
					final Stmt _stmt = _triple.getFirst();
					final SootMethod _caller = _triple.getSecond();
					final Stack<CallTriple> _stack = _triple.getThird();
					final InvokeExpr _expr = _stmt.getInvokeExpr();
					for (int _j = _params.nextSetBit(0); _j >= 0; _j = _params.nextSetBit(_j + 1)) {
						_result |= engine.generateExprLevelSliceCriterion(_expr.getArgBox(_j), _stmt, _caller,
								CONSIDER_EXECUTION, _stack);
					}
				}
				if (_params.cardinality() == _callee.getParameterCount()) {
					_i.remove();
				}
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("continueProcessing() - END");
		}

		return _result;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IDirectionSensitivePartOfSlicingEngine#generateCriteriaForTheCallToMethod(soot.SootMethod, soot.SootMethod,
	 *      soot.jimple.Stmt)
	 */
	public void generateCriteriaForTheCallToMethod(final SootMethod callee, final SootMethod caller, final Stmt callStmt) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("generateCriteriaForTheCallToMethod(Stmt callStmt = " + callStmt + "SootMethod callee = " + callee
					+ ", SootMethod caller = " + caller + ", stack = " + engine.getCopyOfCallStackCache() + ") - BEGIN");
		}

		/*
		 * _stmt may be an assignment statement. Hence, we want the control to reach the statement but not leave it. However,
		 * the execution of the invoke expression should be considered as it is requied to reach the callee. Likewise, we want
		 * to include the expression but not all arguments. We rely on the reachable parameters to suck in the arguments. So,
		 * we generate criteria only for the invocation expression and not the arguments. Refer to
		 * transformAndGenerateToNewCriteriaForXXXX for information about how invoke expressions are handled differently.
		 */
		engine.generateStmtLevelSliceCriterion(callStmt, caller, false);
		engine.includeInSlice(callStmt.getInvokeExprBox());
		generateCriteriaForReceiverOfAt(callStmt, caller);
		recordCallInfoForProcessingArgsTo(callStmt, caller, callee);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("generateCriteriaForTheCallToMethod() - END");
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.slicer.IDirectionSensitivePartOfSlicingEngine#generateCriteriaToIncludeCallees(soot.jimple.Stmt,
	 *      soot.SootMethod, java.util.Collection)
	 */
	public void generateCriteriaToIncludeCallees(final Stmt stmt, final SootMethod caller,
			final Collection<SootMethod> callees) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("generateCriteriaToIncludeCallees(Stmt stmt = " + stmt + ", Collection callees = " + callees
					+ ", SootMethod caller = " + caller + ", stack = " + engine.getCopyOfCallStackCache() + ") - BEGIN");
		}

		processTailsOf(callees, stmt, caller, tailStmtInclusionClosure);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("generateCriteriaToIncludeCallees() - END");
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see DependenceExtractor.IDependenceRetriver#getDependences(IDependencyAnalysis, Object, SootMethod )
	 */
	public Collection<Object> getDependences(final IDependencyAnalysis analysis, final Object entity, final SootMethod method) {
		final Collection<Object> _result = new HashSet<Object>();
		final IDependencyAnalysis.Direction _direction = analysis.getDirection();

		if (_direction.equals(Direction.BACKWARD_DIRECTION) || _direction.equals(Direction.BI_DIRECTIONAL)) {
			if (analysis instanceof IdentifierBasedDataDA && entity instanceof Stmt) {
				_result.addAll(((IdentifierBasedDataDA) analysis).getDependees((Stmt) entity, method));
			} else if (analysis instanceof IdentifierBasedDataDAv2  && entity instanceof Stmt) {
				_result.addAll(((IdentifierBasedDataDAv2) analysis).getDependees((Stmt) entity, method));
			} else {
				_result.addAll(analysis.getDependees(entity, method));
			}
		} else if (LOGGER.isWarnEnabled()) {
			LOGGER.warn("Trying to retrieve BACKWARD dependence from a dependence analysis that is FORWARD direction. -- "
					+ analysis.getClass() + " - " + _direction);
		}

		return _result;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see DependenceExtractor.IDependenceRetriver#getEntityForIdentifierBasedDataDA(soot.Local, soot.jimple.Stmt)
	 */
	public Object getEntityForIdentifierBasedDataDA(final Local local, final Stmt stmt) {
		return new Pair<Local, Stmt>(local, stmt);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.slicer.IDirectionSensitivePartOfSlicingEngine#processLocalAt(Local, soot.jimple.Stmt,
	 *      soot.SootMethod)
	 */
	public void processLocalAt(final Local local, final Stmt stmt, final SootMethod method) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("processLocalAt(Local local = " + local + ", Stmt stmt = " + stmt + ", SootMethod method = "
					+ method + ", stack = " + engine.getCopyOfCallStackCache() + ") - BEGIN");
		}

		boolean _flag = false;
		
		if (stmt instanceof DefinitionStmt) {
			final Iterator<ValueBox> _i = stmt.getDefBoxes().iterator();
			final int _iEnd = stmt.getDefBoxes().size();

			for (int _iIndex = 0; _iIndex < _iEnd && !_flag; _iIndex++) {
				final ValueBox _vb = _i.next();
				final Value _v = _vb.getValue();
				_flag = _v.equals(local);
			}
		}
		
		engine.generateStmtLevelSliceCriterion(stmt, method, _flag);
		
		if (stmt.containsInvokeExpr() && _flag) {
			final Context _ctxt = new Context();
			_ctxt.setRootMethod(method);
			_ctxt.setStmt(stmt);
			processTailsOf(engine.getCgi().getCallees(stmt.getInvokeExpr(), _ctxt), stmt, method,
					returnValueInclClosure);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("processLocalAt() , stack = " + engine.getCopyOfCallStackCache() + "- END");
		}
	}

	/**
	 * Processes new expressions to include corresponding init statements into the slice.
	 * 
	 * @param stmt is the statement containing the new expression.
	 * @param method contains <code>stmt</code>.
	 * @pre stmt != null and method != null
	 */
	public void processNewExpr(final Stmt stmt, final SootMethod method) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("processNewExpr(Stmt stmt = " + stmt + ", SootMethod method = " + method + ", stack = "
					+ engine.getCopyOfCallStackCache() + ") - BEGIN");
		}

		/*
		 * Here we make some assumptions. new expressions will always be assigned to a variable in order to call the
		 * constructor. Hence, they will always occur in assignment statement. It is common practice in compilers to emit code
		 * to construct the instance just after emitting for creating the instance. Hence, we should be able to find the
		 * <init> call site by following use-def chain. However, this may fail in case where the <init> call is made on a
		 * variable other than the one to which the new expression was assigned to. r1 = new <X>; r2 = r1; r2.<init>(); To
		 * handle such cases, we use OFA.
		 */
		if (stmt instanceof AssignStmt) {
			final AssignStmt _as = (AssignStmt) stmt;

			if (_as.getRightOp() instanceof NewExpr) {
				final Stmt _def = engine.getInitMapper().getInitCallStmtForNewExprStmt(stmt, method);
				if (_def != null) {
					engine.generateStmtLevelSliceCriterion(_def, method, true);
				} else if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Could not find the <init> for " + stmt + "@" + method);
				}
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("processNewExpr() - END");
		}
	}

	/**
	 * Generates new slicing criteria which captures inter-procedural dependences due to call-sites.
	 * <p>
	 * This should be called from within the callee's context (callStack containing the call to the callee).
	 * </p>
	 * 
	 * @param stmt is the statement in which <code>pBox</code> occurs.
	 * @param callee in which<code>stmt</code> occurs.
	 * @pre method != null and stmt != null
	 */
	public void processParameterRef(final IdentityStmt stmt, final SootMethod callee) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("processParameterRef(ValueBox pBox = " + stmt.getRightOpBox() + ", SootMethod callee = " + callee
					+ ", stack = " + engine.getCopyOfCallStackCache() + ") - BEGIN");
		}

		final ParameterRef _param = (ParameterRef) stmt.getRightOp();
		final int _index = _param.getIndex();

		BitSet _params = method2params.get(callee);

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

		if (engine.ifInsideContext()) {
			final CallTriple _temp = engine.returnFromMethod();
			if (_temp != null) {
				final SootMethod _caller = _temp.getMethod();
				final Stmt _stmt = _temp.getStmt();
				final ValueBox _argBox = _temp.getExpr().getArgBox(_index);
				engine.generateExprLevelSliceCriterion(_argBox, _stmt, _caller, true);
				generateCriteriaForReceiverOfAt(_stmt, _caller);
			}
			engine.enterMethod(_temp);
		} else {
			for (final Iterator<CallTriple> _i = engine.getCgi().getCallers(callee).iterator(); _i.hasNext();) {
				final CallTriple _ctrp = _i.next();
				final SootMethod _caller = _ctrp.getMethod();
				final Stmt _stmt = _ctrp.getStmt();
				final ValueBox _argBox = _ctrp.getExpr().getArgBox(_index);
				engine.generateExprLevelSliceCriterion(_argBox, _stmt, _caller, true);
				generateCriteriaForReceiverOfAt(_stmt, _caller);
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("processParameterRef() - END");
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.slicer.IDirectionSensitivePartOfSlicingEngine#reset()
	 */
	public void reset() {
		callee2callsites.clear();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IDirectionSensitivePartOfSlicingEngine#retrieveValueBoxesToTransformExpr(ValueBox, Stmt)
	 */
	public Collection<ValueBox> retrieveValueBoxesToTransformExpr(final ValueBox valueBox, final Stmt stmt) {
		final Collection<ValueBox> _valueBoxes = new HashSet<ValueBox>();
		_valueBoxes.add(valueBox);

		final Value _value = valueBox.getValue();

		// if it is an invocation expression, we do not want to include the arguments/sub-expressions.
		// in case of instance invocation, we do want to include the receiver position expression.
		if (!(_value instanceof InvokeExpr)) {
			_valueBoxes.addAll(_value.getUseBoxes());
		} else if (_value instanceof InstanceInvokeExpr) {
			_valueBoxes.add(((InstanceInvokeExpr) _value).getBaseBox());
		}

		/*
		 * Note that l-position is the lhs of an assignment statement whereas an l-value is value that occurs the l-position
		 * and is defined. In a[i] = v; a is the l-value whereas i is a r-value in the l-position and v is r-value in the
		 * r-position.
		 */

		// we include the unincluded r-values in case the given value box represents a l-value.
		if (stmt instanceof DefinitionStmt && stmt.getDefBoxes().contains(valueBox)) {
			_valueBoxes.addAll(engine.getCollector().getUncollected(stmt.getUseBoxes()));
		}

		return _valueBoxes;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IDirectionSensitivePartOfSlicingEngine#retrieveValueBoxesToTransformStmt(Stmt)
	 */
	public Collection<ValueBox> retrieveValueBoxesToTransformStmt(final Stmt stmt) {
		final Collection<ValueBox> _valueBoxes = new HashSet<ValueBox>(stmt.getUseAndDefBoxes());

		// if it contains an invocation expression, we do not want to include the arguments/sub-expressions.
		if (stmt.containsInvokeExpr()) {
			final InvokeExpr _invokeExpr = stmt.getInvokeExpr();

			_valueBoxes.removeAll(_invokeExpr.getUseBoxes());

			// in case of instance invocation, we do want to include the receiver position expression.
			if (_invokeExpr instanceof InstanceInvokeExpr) {
				_valueBoxes.add(((InstanceInvokeExpr) _invokeExpr).getBaseBox());
			}
		}

		return _valueBoxes;
	}

	/**
	 * Checks if the given called method's return points should be considered to generate new slice criterion. The callee
	 * should be marked as invoked or required before calling this method.
	 * 
	 * @param callee is the method in question.
	 * @param expr indicates if the expression in the tail statement should be checked to determine if criteria should be
	 *            generated.
	 * @return <code>true</code> if method's return points of callee should be considered to generate new slice criterion;
	 *         <code>false</code>, otherwise.
	 */
	private boolean considerMethodExitForCriteriaGeneration(final SootMethod callee, final boolean expr) {
		boolean _result = false;

		if (!exitTransformedMethods2exitpoints.containsKey(callee)) {
			_result = true;
		} else if (expr) {
			final Collection<Stmt> _temp = MapUtils.queryCollection(exitTransformedMethods2exitpoints, callee);
			final Iterator<Stmt> _i = _temp.iterator();
			final int _iEnd = _temp.size();

			for (int _iIndex = 0; _iIndex < _iEnd && !_result; _iIndex++) {
				final Stmt _tail = _i.next();

				if (_tail instanceof ReturnStmt) {
					_result |= !engine.getCollector().hasBeenCollected(((ReturnStmt) _tail).getOpBox());
				} else if (_tail instanceof ThrowStmt) {
					_result |= !engine.getCollector().hasBeenCollected(((ThrowStmt) _tail).getOpBox());
				}
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Consider Method exit for method " + callee + " is " + _result);
		}

		return _result;
	}

	/**
	 * Generates criteria to include the receiver at the given invocation statement.
	 * <p>
	 * This should be called from the caller's context (callStack containing the call to the callee).
	 * </p>
	 * 
	 * @param callStmt at which the invocation occurs.
	 * @param caller in which the invocation occurs.
	 * @pre callStmt != null and caller != null and callee != null
	 */
	private void generateCriteriaForReceiverOfAt(final Stmt callStmt, final SootMethod caller) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("generateCriteriaForReceiverOfAt(Stmt invocationStmt = " + callStmt + ", SootMethod caller = "
					+ caller + ", stack = " + engine.getCopyOfCallStackCache() + ") - BEGIN");
		}

		if (!callStmt.getInvokeExpr().getMethod().isStatic()) {
			final ValueBox _vBox = ((InstanceInvokeExpr) callStmt.getInvokeExpr()).getBaseBox();
			engine.generateExprLevelSliceCriterion(_vBox, callStmt, caller, true);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("generateCriteriaForReceiverOfAt(), stack = " + engine.getCopyOfCallStackCache() + " - END");
		}
	}

	/**
	 * Processes the init call to the super class inside init method.
	 * 
	 * @param initMethod is the init method.
	 * @param bbg is the basic block graph of <code>callee</code>.
	 * @pre initMethod != null and bbg != null and engine.getCallStackCache() != null
	 */
	private void processSuperInitInInit(final SootMethod initMethod, final BasicBlockGraph bbg) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("processSuperInitInInit(SootMethod initMethod = " + initMethod + ", BasicBlockGraph bbg = " + bbg
					+ ", stack = " + engine.getCopyOfCallStackCache() + ") - BEGIN");
		}

		/*
		 * if we are sucking in an init we better suck in the super <init> invoke expression as well. By JLS, this has to be
		 * the first statement in the constructor. However, if it accepts arguments, the arguments will be set up before the
		 * call. Hence, it is safe to suck in the first <init> invoke expression in the <init> method being sucked in.
		 * However, care must be taken to suck in the first <init> invocation that is invokes <init> from the same class as
		 * the enclosing <init> method. As we process invocation expressions, we are bound to suck in any other required
		 * <init>'s from other higher super classes.
		 */
		if (initMethod.getName().equals("<init>") && initMethod.getDeclaringClass().hasSuperclass()) {
			final LocalUseDefAnalysisv2 _udl = new LocalUseDefAnalysisv2(engine.getBasicBlockGraphManager()
					.getBasicBlockGraph(initMethod));
			final Collection<Pair<Local, Stmt>> _uses = _udl.getUses((DefinitionStmt) bbg.getHead().getLeaderStmt(),
					initMethod);

			for (final Iterator<Pair<Local, Stmt>> _i = _uses.iterator(); _i.hasNext();) {
				final Pair<Local, Stmt> _pair = _i.next();
				final Stmt _stmt = _pair.getSecond();

				if (_stmt instanceof InvokeStmt) {
					final SootMethod _called = _stmt.getInvokeExpr().getMethod();

					if (_called.getName().equals("<init>")
							&& _called.getDeclaringClass().equals(initMethod.getDeclaringClass().getSuperclass())) {
						engine.generateStmtLevelSliceCriterion(_stmt, initMethod, true);
						break;
					}
				}
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("processSuperInitInInit() - END");
		}
	}

	/**
	 * Processes the callees called at the given statements in the caller with the given closure to include tails of the
	 * callees along with any arguments at the call-site.
	 * <p>
	 * This should be called from the caller's context (callStack containing the call to the caller).
	 * </p>
	 * 
	 * @param callees are the methods called at the statement.
	 * @param stmt is the statment containing the invocation.
	 * @param caller is the method in which <code>invocationStmt</code> occurs.
	 * @param closure to be executed.
	 * @pre stmt != null and caller != null and callees != null
	 */
	private void processTailsOf(final Collection<SootMethod> callees, final Stmt stmt, final SootMethod caller,
			final IClosure<Pair<Stmt, SootMethod>> closure) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("processTailsOf(Collection callees = " + callees + ", Stmt stmt = " + stmt
					+ ", SootMethod caller = " + caller + ", IClosure closure = " + closure + ", stack = "
					+ engine.getCopyOfCallStackCache() + ") - BEGIN");
		}

		final BasicBlockGraphMgr _bbgMgr = engine.getBasicBlockGraphManager();

		for (final Iterator<SootMethod> _i = callees.iterator(); _i.hasNext();) {
			final SootMethod _callee = _i.next();

			if (considerMethodExitForCriteriaGeneration(_callee, closure == returnValueInclClosure)) {
				engine.includeInSlice(_callee);

				if (_callee.isConcrete()) {
					final BasicBlockGraph _calleeBasicBlockGraph = _bbgMgr.getBasicBlockGraph(_callee);
					final CallTriple _callTriple = new CallTriple(caller, stmt, stmt.getInvokeExpr());
					engine.enterMethod(_callTriple);
					processSuperInitInInit(_callee, _calleeBasicBlockGraph);

					final Collection<Stmt> _temp = new HashSet<Stmt>();

					for (final Iterator<BasicBlock> _j = _calleeBasicBlockGraph.getSinks().iterator(); _j.hasNext();) {
						final BasicBlock _bb = _j.next();
						final Stmt _trailer = _bb.getTrailerStmt();
						closure.execute(new Pair<Stmt, SootMethod>(_trailer, _callee));
						_temp.add(_trailer);
					}
					exitTransformedMethods2exitpoints.put(_callee, _temp);
					engine.returnFromMethod();
				} else {
					final InvokeExpr _invokeExpr = stmt.getInvokeExpr();

					for (int _j = _callee.getParameterCount() - 1; _j >= 0; _j--) {
						engine.generateExprLevelSliceCriterion(_invokeExpr.getArgBox(_j), stmt, caller, true);
					}
				}
			}
			recordCallInfoForProcessingArgsTo(stmt, caller, _callee);
		}
		generateCriteriaForReceiverOfAt(stmt, caller);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("processTailsOf() , stack = " + engine.getCopyOfCallStackCache() + "- END");
		}
	}

	/**
	 * Records the given callee was called from the given call-site in the current context.
	 * 
	 * @param stmt containing the call site.
	 * @param caller containing <code>stmt</code>
	 * @param callee being called.
	 * @pre stmt != null and caller != null and callee != null
	 */
	private void recordCallInfoForProcessingArgsTo(final Stmt stmt, final SootMethod caller, final SootMethod callee) {
		final Stack<CallTriple> _stackClone = engine.getCopyOfCallStackCache();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("recordCallInfoForParameterProcessing(Stmt stmt = " + stmt + ", SootMethod caller = " + caller
					+ ", SootMethod callee = " + callee + ", stack = " + _stackClone + ") - BEGIN");
		}

		final Triple<Stmt, SootMethod, Stack<CallTriple>> _triple = new Triple<Stmt, SootMethod, Stack<CallTriple>>(stmt,
				caller, _stackClone);
		MapUtils.putIntoSetInMap(callee2callsites, callee, _triple);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("recordCallInfoForParameterProcessing() - END");
		}
	}

}

// End of File
