
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

import java.util.Collection;
import java.util.HashSet;

import soot.SootMethod;
import soot.ValueBox;

import soot.jimple.Stmt;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class CompleteSlicingPart
  implements IDirectionSensitivePartOfSlicingEngine {
	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final BackwardSlicingPart backwardPart;

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final ForwardSlicingPart forwardPart;

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
	public CompleteSlicingPart(final SlicingEngine theEngine) {
		engine = theEngine;
		backwardPart = new BackwardSlicingPart(engine);
		forwardPart = new ForwardSlicingPart(engine);
	}

	/**
	 * @see DependenceExtractor.IDependenceRetriver#getDependences(IDependencyAnalysis, Object, Object)
	 */
	public Collection getDependences(final IDependencyAnalysis analysis, final Object entity, final Object context) {
		final Collection _result = new HashSet();
		final Object _direction = analysis.getDirection();

		if (_direction.equals(IDependencyAnalysis.DIRECTIONLESS)) {
			_result.addAll(analysis.getDependees(entity, context));
		} else if (_direction.equals(IDependencyAnalysis.FORWARD_DIRECTION)
			  || _direction.equals(IDependencyAnalysis.BACKWARD_DIRECTION)) {
			_result.addAll(analysis.getDependees(entity, context));
		} else if (_direction.equals(IDependencyAnalysis.BI_DIRECTIONAL)) {
			_result.addAll(analysis.getDependees(entity, context));
			_result.addAll(analysis.getDependents(entity, context));
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
	 * DOCUMENT ME!
	 *
	 * @param local
	 * @param stmt
	 * @param method
	 */
	public void processLocalAt(final ValueBox local, final Stmt stmt, final SootMethod method) {
		backwardPart.processLocalAt(local, stmt, method);
		forwardPart.processLocalAt(local, stmt, method);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param stmt
	 * @param method
	 */
	public void processNewExpr(final Stmt stmt, final SootMethod method) {
		backwardPart.processNewExpr(stmt, method);
		forwardPart.processNewExpr(stmt, method);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param box
	 * @param method
	 */
	public void processParameterRef(final ValueBox box, final SootMethod method) {
		backwardPart.processParameterRef(box, method);
		forwardPart.processParameterRef(box, method);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param valueBox
	 *
	 * @return
	 */
	public Collection retrieveValueBoxesToTransformExpr(final ValueBox valueBox) {
		final Collection _result = new HashSet();
		_result.addAll(backwardPart.retrieveValueBoxesToTransformExpr(valueBox));
		_result.addAll(forwardPart.retrieveValueBoxesToTransformExpr(valueBox));
		return _result;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param stmt
	 *
	 * @return
	 */
	public Collection retrieveValueBoxesToTransformStmt(final Stmt stmt) {
		final Collection _result = new HashSet();
		_result.addAll(backwardPart.retrieveValueBoxesToTransformStmt(stmt));
		_result.addAll(forwardPart.retrieveValueBoxesToTransformStmt(stmt));
		return _result;
	}
}

/*
   ChangeLog:
   $Log$
 */
