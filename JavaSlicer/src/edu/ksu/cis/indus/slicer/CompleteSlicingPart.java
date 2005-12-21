
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

import edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis;
import edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis.Direction;

import java.util.Collection;
import java.util.HashSet;

import soot.Local;
import soot.SootMethod;
import soot.ValueBox;

import soot.jimple.IdentityStmt;
import soot.jimple.Stmt;


/**
 * This class provides the logic to detect parts of a complete (backward and forward) slice.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class CompleteSlicingPart
  implements IDirectionSensitivePartOfSlicingEngine {
	/** 
	 * This part provides the backward slice creation logic.
	 */
	private final BackwardSlicingPart backwardPart;

	/** 
	 * This part provides the forward slice creation logic.
	 */
	private final ForwardSlicingPart forwardPart;

	/** 
	 * The engine with which this part is a part of.
	 */
	private final SlicingEngine engine;

	/**
	 * Creates an instance of this class.
	 *
	 * @param theEngine of which this part is a part of.
	 *
	 * @pre theEngine != null
	 */
	public CompleteSlicingPart(final SlicingEngine theEngine) {
		engine = theEngine;
		backwardPart = new BackwardSlicingPart(engine);
		forwardPart = new ForwardSlicingPart(engine);
	}

	/**
	 * @see DependenceExtractor.IDependenceRetriver#getDependences(IDependencyAnalysis, Object, SootMethod)
	 */
	public Collection<Object> getDependences(final IDependencyAnalysis analysis, final Object entity, final SootMethod method) {
		final Collection<Object> _result = new HashSet<Object>();
		final Object _direction = analysis.getDirection();

		/*
		 * We need getDependees() for forward, backward, and bidirectional.
		 */ 
		_result.addAll(analysis.getDependees(entity, method));

		if (_direction.equals(Direction.BI_DIRECTIONAL)) {
			_result.addAll(analysis.getDependents(entity, method));
		}
		return _result;
	}

	/**
	 * @see IDirectionSensitivePartOfSlicingEngine#generateCriteriaForTheCallToMethod(soot.SootMethod,     soot.SootMethod,
	 * 		soot.jimple.Stmt)
	 */
	public void generateCriteriaForTheCallToMethod(final SootMethod callee, final SootMethod caller, final Stmt callStmt) {
		backwardPart.generateCriteriaForTheCallToMethod(callee, caller, callStmt);
		forwardPart.generateCriteriaForTheCallToMethod(callee, caller, callStmt);
	}

	/**
	 * @see edu.ksu.cis.indus.slicer.IDirectionSensitivePartOfSlicingEngine#generateCriteriaToIncludeCallees(soot.jimple.Stmt,
	 * 		soot.SootMethod, java.util.Collection)
	 */
	public void generateCriteriaToIncludeCallees(final Stmt stmt, final SootMethod caller, final Collection callees) {
		backwardPart.generateCriteriaToIncludeCallees(stmt, caller, callees);
		forwardPart.generateCriteriaToIncludeCallees(stmt, caller, callees);
	}

	/**
	 * @see IDirectionSensitivePartOfSlicingEngine#processLocalAt(Local, Stmt, SootMethod)
	 */
	public void processLocalAt(final Local local, final Stmt stmt, final SootMethod method) {
		backwardPart.processLocalAt(local, stmt, method);
		forwardPart.processLocalAt(local, stmt, method);
	}

	/**
	 * @see IDirectionSensitivePartOfSlicingEngine#processNewExpr(Stmt, SootMethod)
	 */
	public void processNewExpr(final Stmt stmt, final SootMethod method) {
		backwardPart.processNewExpr(stmt, method);
		forwardPart.processNewExpr(stmt, method);
	}

	/**
	 * @see IDirectionSensitivePartOfSlicingEngine#processParameterRef(IdentityStmt, SootMethod)
	 */
	public void processParameterRef(final IdentityStmt stmt, final SootMethod method) {
		backwardPart.processParameterRef(stmt, method);
		forwardPart.processParameterRef(stmt, method);
	}

	/**
	 * @see edu.ksu.cis.indus.slicer.IDirectionSensitivePartOfSlicingEngine#reset()
	 */
	public void reset() {
		backwardPart.reset();
		forwardPart.reset();
	}

	/**
	 * @see IDirectionSensitivePartOfSlicingEngine#retrieveValueBoxesToTransformExpr(ValueBox, Stmt)
	 */
	public Collection<ValueBox> retrieveValueBoxesToTransformExpr(final ValueBox valueBox, final Stmt stmt) {
		final Collection<ValueBox> _result = new HashSet<ValueBox>();
		_result.addAll(backwardPart.retrieveValueBoxesToTransformExpr(valueBox, stmt));
		_result.addAll(forwardPart.retrieveValueBoxesToTransformExpr(valueBox, stmt));
		return _result;
	}

	/**
	 * @see IDirectionSensitivePartOfSlicingEngine#retrieveValueBoxesToTransformStmt(Stmt)
	 */
	public Collection<ValueBox> retrieveValueBoxesToTransformStmt(final Stmt stmt) {
		final Collection<ValueBox> _result = new HashSet<ValueBox>();
		_result.addAll(backwardPart.retrieveValueBoxesToTransformStmt(stmt));
		_result.addAll(forwardPart.retrieveValueBoxesToTransformStmt(stmt));
		return _result;
	}

	/** 
	 * @see edu.ksu.cis.indus.slicer.IDirectionSensitivePartOfSlicingEngine#continueProcessing()
	 */
	public boolean continueProcessing() {
		return backwardPart.continueProcessing() || forwardPart.continueProcessing();
	}
}

// End of File
