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

import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis;

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
	 * The engine with which this part is a part of.
	 */
	private final SlicingEngine engine;

	/**
	 * This part provides the forward slice creation logic.
	 */
	private final ForwardSlicingPart forwardPart;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param theEngine of which this part is a part of.
	 * @pre theEngine != null
	 */
	public CompleteSlicingPart(final SlicingEngine theEngine) {
		engine = theEngine;
		backwardPart = new BackwardSlicingPart(engine);
		forwardPart = new ForwardSlicingPart(engine);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.slicer.IDirectionSensitivePartOfSlicingEngine#continueProcessing()
	 */
	public boolean continueProcessing() {
		return backwardPart.continueProcessing() || forwardPart.continueProcessing();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IDirectionSensitivePartOfSlicingEngine#generateCriteriaForTheCallToMethod(soot.SootMethod, soot.SootMethod,
	 *      soot.jimple.Stmt)
	 */
	public void generateCriteriaForTheCallToMethod(final SootMethod callee, final SootMethod caller, final Stmt callStmt) {
		backwardPart.generateCriteriaForTheCallToMethod(callee, caller, callStmt);
		forwardPart.generateCriteriaForTheCallToMethod(callee, caller, callStmt);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.slicer.IDirectionSensitivePartOfSlicingEngine#generateCriteriaToIncludeCallees(soot.jimple.Stmt,
	 *      soot.SootMethod, java.util.Collection)
	 */
	public void generateCriteriaToIncludeCallees(final Stmt stmt, final SootMethod caller, final Collection callees) {
		backwardPart.generateCriteriaToIncludeCallees(stmt, caller, callees);
		forwardPart.generateCriteriaToIncludeCallees(stmt, caller, callees);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see DependenceExtractor.IDependenceRetriver#getDependences(IDependencyAnalysis, Object, SootMethod)
	 */
	public Collection<Object> getDependences(final IDependencyAnalysis analysis, final Object entity, final SootMethod method) {
		final Collection<Object> _result = new HashSet<Object>();
		if (entity instanceof Pair) {
			_result.addAll(backwardPart.getDependences(analysis, ((Pair) entity).getSecond(), method));
			_result.addAll(forwardPart.getDependences(analysis, ((Pair) entity).getSecond(), method));
		} else {
			_result.addAll(backwardPart.getDependences(analysis, entity, method));
			_result.addAll(forwardPart.getDependences(analysis, entity, method));			
		}
		return _result;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see DependenceExtractor.IDependenceRetriver#getEntityForIdentifierBasedDataDA(soot.Local, soot.jimple.Stmt)
	 */
	public Object getEntityForIdentifierBasedDataDA(final Local local, final Stmt stmt) {
		return new Pair<Object, Object>(backwardPart.getEntityForIdentifierBasedDataDA(local, stmt), forwardPart
				.getEntityForIdentifierBasedDataDA(local, stmt));
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IDirectionSensitivePartOfSlicingEngine#processLocalAt(Local, Stmt, SootMethod)
	 */
	public void processLocalAt(final Local local, final Stmt stmt, final SootMethod method) {
		backwardPart.processLocalAt(local, stmt, method);
		forwardPart.processLocalAt(local, stmt, method);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IDirectionSensitivePartOfSlicingEngine#processNewExpr(Stmt, SootMethod)
	 */
	public void processNewExpr(final Stmt stmt, final SootMethod method) {
		backwardPart.processNewExpr(stmt, method);
		forwardPart.processNewExpr(stmt, method);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IDirectionSensitivePartOfSlicingEngine#processParameterRef(IdentityStmt, SootMethod)
	 */
	public void processParameterRef(final IdentityStmt stmt, final SootMethod method) {
		backwardPart.processParameterRef(stmt, method);
		forwardPart.processParameterRef(stmt, method);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.slicer.IDirectionSensitivePartOfSlicingEngine#reset()
	 */
	public void reset() {
		backwardPart.reset();
		forwardPart.reset();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IDirectionSensitivePartOfSlicingEngine#retrieveValueBoxesToTransformExpr(ValueBox, Stmt)
	 */
	public Collection<ValueBox> retrieveValueBoxesToTransformExpr(final ValueBox valueBox, final Stmt stmt) {
		final Collection<ValueBox> _result = new HashSet<ValueBox>();
		_result.addAll(backwardPart.retrieveValueBoxesToTransformExpr(valueBox, stmt));
		_result.addAll(forwardPart.retrieveValueBoxesToTransformExpr(valueBox, stmt));
		return _result;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IDirectionSensitivePartOfSlicingEngine#retrieveValueBoxesToTransformStmt(Stmt)
	 */
	public Collection<ValueBox> retrieveValueBoxesToTransformStmt(final Stmt stmt) {
		final Collection<ValueBox> _result = new HashSet<ValueBox>();
		_result.addAll(backwardPart.retrieveValueBoxesToTransformStmt(stmt));
		_result.addAll(forwardPart.retrieveValueBoxesToTransformStmt(stmt));
		return _result;
	}
}

// End of File
