
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

import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;

import edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;
import soot.Value;
import soot.ValueBox;

import soot.jimple.AssignStmt;
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
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class BackwardSlicingPart
  implements IDirectionSensitivePartOfSlicingEngine {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(BackwardSlicingPart.class);

	/** 
	 * This is the collection of methods whose exits were transformed.
	 */
	private final Collection exitTransformedMethods = new HashSet();

	/** 
	 * This maps methods to methods to a bitset that indicates which of the parameters of the method is required in the
	 * slice.
	 *
	 * @invariant method2params.oclIsKindOf(Map(SootMethod, BitSet))
	 * @invariant method2params->forall(o | o.getValue().size() = o.getKey().getParameterCount())
	 */
	private final Map method2params = new HashMap();

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final SlicingEngine engine;

	/**
	 * DOCUMENT ME!
	 *
	 * @param theEngine
	 */
	BackwardSlicingPart(final SlicingEngine theEngine) {
		engine = theEngine;
	}

	/**
	 * @see DependenceExtractor.IDependenceRetriver#getDependences(IDependencyAnalysis, Object, Object)
	 */
	public Collection getDependences(final IDependencyAnalysis analysis, final Object entity, final Object context) {
		final Collection _result = new HashSet();
		final Object _direction = analysis.getDirection();

		if (_direction.equals(IDependencyAnalysis.BACKWARD_DIRECTION)
			  || _direction.equals(IDependencyAnalysis.DIRECTIONLESS)
			  || _direction.equals(IDependencyAnalysis.BI_DIRECTIONAL)) {
			_result.addAll(analysis.getDependees(entity, context));
		} else if (LOGGER.isWarnEnabled()) {
			LOGGER.warn("Trying to retrieve BACKWARD dependence from a dependence analysis that is FORWARD direction.");
		}

		return _result;
	}

	/**
	 * @see IDirectionSensitivePartOfSlicingEngine#generateCriteriaForTheCallToMethod(soot.SootMethod,     soot.SootMethod,
	 * 		soot.jimple.Stmt)
	 */
	public void generateCriteriaForTheCallToMethod(final SootMethod callee, final SootMethod caller, final Stmt callStmt) {
		/*
		 * _stmt may be an assignment statement.  Hence, we want the control to reach the statement but not leave
		 * it.  However, the execution of the invoke expression should be considered as it is requied to reach the
		 * callee.  Likewise, we want to include the expression but not all arguments.  We rely on the reachable
		 * parameters to suck in the arguments.  So, we generate criteria only for the invocation expression and
		 * not the arguments.  Refer to transformAndGenerateToNewCriteriaForXXXX for information about how
		 * invoke expressions are handled differently.
		 */
		engine.generateSliceStmtCriterion(callStmt, caller, false);
		engine.getCollector().includeInSlice(callStmt.getInvokeExprBox());
		generateCriteriaForReceiver(callStmt, caller, callee);
	}

	/**
	 * @see edu.ksu.cis.indus.slicer.IDirectionSensitivePartOfSlicingEngine#generateCriteriaToIncludeCallees(soot.jimple.Stmt,
	 * 		soot.SootMethod, java.util.Collection)
	 */
	public void generateCriteriaToIncludeCallees(final Stmt stmt, final SootMethod caller, final Collection callees) {
		//add exit points of callees as the slice criteria
		for (final Iterator _i = callees.iterator(); _i.hasNext();) {
			final SootMethod _callee = (SootMethod) _i.next();
			final BasicBlockGraph _bbg = engine.getBasicBlockGraphManager().getBasicBlockGraph(_callee);

			/*
			 * we do not want to include a dependence on return statement in java.lang.Thread.start() method
			 * as it will occur in a different thread and cannot affect the sequential flow of control in the current thread.
			 */
			if (_bbg != null) {
				generateCriteriaForCallee(stmt, caller, stmt instanceof AssignStmt, _callee, _bbg);
			} else if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Did not process " + _callee.getSignature() + " as it may be the start() method or it has no"
					+ " basic block graph.");
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.slicer.IDirectionSensitivePartOfSlicingEngine#processLocalAt(soot.ValueBox, soot.jimple.Stmt,
	 * 		soot.SootMethod)
	 */
	public void processLocalAt(final ValueBox local, final Stmt stmt, final SootMethod method) {
		engine.generateSliceStmtCriterion(stmt, method, true);
	}

	/**
	 * Processes new expressions to include corresponding init statements into the slice.
	 *
	 * @param stmt is the statement containing the new expression.
	 * @param method contains <code>stmt</code>.
	 *
	 * @pre stmt != null and method != null
	 */
	public void processNewExpr(final Stmt stmt, final SootMethod method) {
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
				final Stmt _def = engine.getInitMapper().getInitCallStmtForNewExprStmt(stmt, method);
				engine.generateSliceStmtCriterion(_def, method, true);
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Processing for new expr");
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
	public void processParameterRef(final ValueBox pBox, final SootMethod callee) {
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

		final Stack _callStackCache = engine.getCallStackCache();

		if (_callStackCache != null && !_callStackCache.isEmpty()) {
			final CallTriple _temp = (CallTriple) _callStackCache.pop();
			final SootMethod _caller = _temp.getMethod();
			final Stmt _stmt = _temp.getStmt();
			final ValueBox _argBox = _temp.getExpr().getArgBox(_index);
			engine.generateSliceExprCriterion(_argBox, _stmt, _caller, true);
			generateCriteriaForReceiver(_stmt, _caller, callee);
			_callStackCache.push(_temp);
		} else {
			for (final Iterator _i = engine.getCgi().getCallers(callee).iterator(); _i.hasNext();) {
				final CallTriple _ctrp = (CallTriple) _i.next();
				final SootMethod _caller = _ctrp.getMethod();
				final Stmt _stmt = _ctrp.getStmt();
				final ValueBox _argBox = _ctrp.getExpr().getArgBox(_index);
				engine.generateSliceExprCriterion(_argBox, _stmt, _caller, true);
				generateCriteriaForReceiver(_stmt, _caller, callee);
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Generating criteria for parameters");
		}
	}

	/**
	 * @see IDirectionSensitivePartOfSlicingEngine#retrieveValueBoxesToTransformExpr(ValueBox)
	 */
	public Collection retrieveValueBoxesToTransformExpr(final ValueBox valueBox) {
		final Collection _valueBoxes = new HashSet();
		_valueBoxes.add(valueBox);

		final Value _value = valueBox.getValue();

		//if it is an invocation expression, we do not want to include the arguments/sub-expressions. 
		// in case of instance invocation, we do want to include the receiver position expression.
		if (!(_value instanceof InvokeExpr)) {
			_valueBoxes.addAll(_value.getUseBoxes());
		} else if (_value instanceof InstanceInvokeExpr) {
			_valueBoxes.add(((InstanceInvokeExpr) _value).getBaseBox());
		}
		return _valueBoxes;
	}

	/**
	 * @see IDirectionSensitivePartOfSlicingEngine#retrieveValueBoxesToTransformStmt(Stmt)
	 */
	public Collection retrieveValueBoxesToTransformStmt(final Stmt stmt) {
		final Collection _valueBoxes = new HashSet(stmt.getUseAndDefBoxes());

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
	private void generateCriteriaForCallee(final Stmt invocationStmt, final SootMethod caller,
		final boolean considerReturnValue, final SootMethod callee, final BasicBlockGraph calleeBasicBlockGraph) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Generating criteria for method called at " + invocationStmt + " in " + caller + "["
				+ considerReturnValue + "]");
		}

		// check if a criteria to consider the exit points of the method should be generated.
		if (considerMethodExitForCriteriaGeneration(callee)) {
			if (callee.isConcrete()) {
				if (engine.getCallStackCache() == null) {
					engine.setCallStackCache(new Stack());
				}

				final Stack _callStackCache = engine.getCallStackCache();
				_callStackCache.push(new CallTriple(caller, invocationStmt, invocationStmt.getInvokeExpr()));
				processSuperInitInInit(callee, calleeBasicBlockGraph);

				for (final Iterator _j = calleeBasicBlockGraph.getTails().iterator(); _j.hasNext();) {
					final BasicBlock _bb = (BasicBlock) _j.next();
					final Stmt _trailer = _bb.getTrailerStmt();

					// TODO: we are considering both throws and returns as return points. This should change when we consider 
					// if control-flow based on exceptions.
					engine.generateSliceStmtCriterion(_trailer, callee, considerReturnValue);
				}
				_callStackCache.pop();

				if (_callStackCache.isEmpty()) {
					engine.setCallStackCache(null);
				}
			} else {
				engine.includeMethodAndDeclaringClassInSlice(callee);

				// HACK: to suck in arguments to a native method.
				final InvokeExpr _expr = invocationStmt.getInvokeExpr();

				for (int _i = _expr.getArgCount() - 1; _i >= 0; _i--) {
					engine.generateSliceExprCriterion(_expr.getArgBox(_i), invocationStmt, caller, true);
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
					engine.generateSliceExprCriterion(_invokeExpr.getArgBox(_j), invocationStmt, caller, true);
				}
			}
		}

		generateCriteriaForReceiver(invocationStmt, caller, callee);


		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Generating criteria for method called at " + invocationStmt + " in " + caller);
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param invocationStmt
	 * @param caller
	 * @param callee
	 */
	private void generateCriteriaForReceiver(final Stmt invocationStmt, final SootMethod caller, final SootMethod callee) {
		if (!callee.isStatic()) {
			final ValueBox _vBox = ((InstanceInvokeExpr) invocationStmt.getInvokeExpr()).getBaseBox();
			engine.generateSliceExprCriterion(_vBox, invocationStmt, caller, true);
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
						final Stack _callStackCache = engine.getCallStackCache();
						_callStackCache.push(new CallTriple(callee, _stmt, _stmt.getInvokeExpr()));
						engine.generateSliceStmtCriterion(_stmt, callee, true);
						_callStackCache.pop();
						break;
					}
				}
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2004/08/18 09:54:49  venku
   - adding first cut classes from refactoring for feature 427.  This is not included in v0.3.2.
 */
