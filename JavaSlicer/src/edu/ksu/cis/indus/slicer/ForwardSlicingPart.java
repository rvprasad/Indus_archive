package edu.ksu.cis.indus.slicer;

import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.Util;
import edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;
import soot.ValueBox;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;


public class ForwardSlicingPart implements IDirectionSensitivePartOfSlicingEngine {

	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(ForwardSlicingPart.class);

	/** 
	 * <p>DOCUMENT ME! </p>
	 */
	private final SlicingEngine engine;

	/**
	 * DOCUMENT ME!
	 *
	 * @param theEngine
	 */
	ForwardSlicingPart(final SlicingEngine theEngine) {
		engine = theEngine;
	}

	/**
	 * @see edu.ksu.cis.indus.slicer.IDirectionSensitivePartOfSlicingEngine#generateCriteriaToIncludeCallees(java.util.Collection,
	 * 		soot.jimple.Stmt, soot.SootMethod, boolean)
	 */
	public void generateCriteriaToIncludeCallees(Collection callees, Stmt stmt, SootMethod method, boolean considerReturnValue) {
		
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
					  || _direction.equals(IDependencyAnalysis.FORWARD_DIRECTION)) {
					_temp.addAll(_analysis.getDependents(_pair, method));
				} else if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Info from backward direction identifier based data dependence analysis ("
						+ _analysis.getClass().getName() + ") will not be used for forward slicing.");
				}
			}

			final Iterator _j = _temp.iterator();
			final int _jEnd = _temp.size();

			for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
				final Stmt _stmt = (Stmt) _j.next();
				if (_stmt.containsInvokeExpr()) {
				    final InvokeExpr _expr = _stmt.getInvokeExpr();
				    final Collection _useBoxes = _expr.getUseBoxes();
				    if (_useBoxes.contains())
				}
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.slicer.IDirectionSensitivePartOfSlicingEngine#initializeCriterion(edu.ksu.cis.indus.slicer.ISliceCriterion)
	 */
	public void initializeCriterion(final ISliceCriterion criteria) {
	    ((AbstractSliceCriterion) criteria).setDirection(SlicingEngine.FORWARD_SLICE);
	    ((AbstractSliceCriterion) criteria).setCallSite(null);
	}
}


/*
ChangeLog:

$Log$
*/