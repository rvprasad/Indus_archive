
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

import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.Util;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.Local;
import soot.SootMethod;
import soot.ValueBox;

import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.ParameterRef;
import soot.jimple.Stmt;
import soot.toolkits.graph.CompleteUnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.toolkits.scalar.SimpleLocalUses;
import soot.toolkits.scalar.UnitValueBoxPair;


/**
 * DOCUMENT ME!
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
	 * <p>DOCUMENT ME! </p>
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
	 * @see edu.ksu.cis.indus.slicer.IDirectionSensitivePartOfSlicingEngine#generateCriteriaToIncludeCallees(java.util.Collection,
	 * 		soot.jimple.Stmt, soot.SootMethod, boolean)
	 */
	public void generateCriteriaToIncludeCallees(Collection callees, Stmt stmt, SootMethod method, boolean considerReturnValue) {
		// add exit points of callees as the slice criteria
		for (final Iterator _i = callees.iterator(); _i.hasNext();) {
			final SootMethod _callee = (SootMethod) _i.next();
			final BasicBlockGraph _bbg = engine.bbgMgr.getBasicBlockGraph(_callee);

			/*
			 * we do not want to include a dependence on return statement in java.lang.Thread.start() method
			 * as it will occur in a different thread and cannot affect the sequential flow of control in the current thread.
			 */
			if (!Util.isStartMethod(_callee) && _bbg != null) {
				generateNewCriteriaBasedOnMethodExit(stmt, method, considerReturnValue, _callee, _bbg);
			} else if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Did not process " + _callee.getSignature() + " as it may be the start() method or it has no"
					+ " basic block graph.");
			}
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
					engine.generateSliceStmtCriterion(_trailer, callee, considerReturnValue);
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

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Generating criteria for method called at " + invocationStmt + " in " + caller);
		}
	}
	
	/** 
	 * This maps methods to methods to a bitset that indicates which of the parameters of the method is required in the
	 * slice.
	 *
	 * @invariant method2params.oclIsKindOf(Map(SootMethod, BitSet))
	 * @invariant method2params->forall(o | o.getValue().size() = o.getKey().getParameterCount())
	 */
	private final Map method2params = new HashMap();

	
	/**
	 * Generates new slicing criteria which captures inter-procedural dependences due to call-sites.
	 *
	 * @param pBox is the parameter reference to be sliced on.
	 * @param callee in which<code>pBox</code> occurs.
	 *
	 * @pre pBox != null and method != null
	 */
	public void generateNewCriteriaForParam(final ValueBox pBox, final SootMethod callee) {
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

		for (final Iterator _i = engine.cgi.getCallers(callee).iterator(); _i.hasNext();) {
			final CallTriple _ctrp = (CallTriple) _i.next();
			final SootMethod _caller = _ctrp.getMethod();
			final Stmt _stmt = _ctrp.getStmt();
			final ValueBox _argBox = _ctrp.getExpr().getArgBox(_index);

			engine.generateSliceExprCriterion(_argBox, _stmt, _caller, true);
		}

		generateNewCriteriaForTheCallToMethod(callee);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Generating criteria for parameters");
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
						engine.generateSliceStmtCriterion(_stmt, callee, true);
					}
				}
			}
		}
	}

	
	
	/**
	 * @see edu.ksu.cis.indus.slicer.IDirectionSensitivePartOfSlicingEngine#generatedNewCriteriaForLocal(soot.ValueBox,
	 * 		soot.jimple.Stmt, soot.SootMethod)
	 */
	public void generatedNewCriteriaForLocal(ValueBox local, Stmt stmt, SootMethod method) {
		final Collection _analyses = engine.controller.getAnalyses(IDependencyAnalysis.IDENTIFIER_BASED_DATA_DA);

		if (_analyses.size() > 0) {
			final Collection _temp = new HashSet();
			final Iterator _i = _analyses.iterator();
			final int _iEnd = _analyses.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final IDependencyAnalysis _analysis = (IDependencyAnalysis) _i.next();
				final Object _direction = _analysis.getDirection();
				final Pair _pair = new Pair(stmt, local);

				if (_direction.equals(IDependencyAnalysis.DIRECTIONLESS)
					  || _direction.equals(IDependencyAnalysis.BI_DIRECTIONAL)
					  || _direction.equals(IDependencyAnalysis.BACKWARD_DIRECTION)) {
					_temp.addAll(_analysis.getDependees(_pair, method));
				} else if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Info from forward direction identifier based data dependence analysis ("
						+ _analysis.getClass().getName() + ") will not be used for backward slicing.");
				}
			}

			final Iterator _j = _temp.iterator();
			final int _jEnd = _temp.size();

			for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
				final Stmt _stmt = (Stmt) _j.next();
				engine.generateSliceStmtCriterion(_stmt, method, true);
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.slicer.IDirectionSensitivePartOfSlicingEngine#initializeCriterion(edu.ksu.cis.indus.slicer.ISliceCriterion)
	 */
	public void initializeCriterion(final ISliceCriterion criteria) {
	    ((AbstractSliceCriterion) criteria).setDirection(SlicingEngine.BACKWARD_SLICE);
	    ((AbstractSliceCriterion) criteria).setCallSite(null);
	}

    /** 
     * @see edu.ksu.cis.indus.slicer.IDirectionSensitivePartOfSlicingEngine#reset()
     */
    public void reset() {
        method2params.clear();
    }
}

/*
   ChangeLog:
   $Log$
 */
