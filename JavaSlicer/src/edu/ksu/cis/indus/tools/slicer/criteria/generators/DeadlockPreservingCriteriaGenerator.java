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

package edu.ksu.cis.indus.tools.slicer.criteria.generators;

import edu.ksu.cis.indus.common.collections.InstanceOfPredicate;
import edu.ksu.cis.indus.common.collections.IteratorUtils;
import edu.ksu.cis.indus.common.datastructures.Triple;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.slicer.ISliceCriterion;
import edu.ksu.cis.indus.slicer.SliceCriteriaFactory;
import edu.ksu.cis.indus.tools.slicer.SlicerTool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootMethod;
import soot.VoidType;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;

/**
 * This class generates seed slicing criteria to preserve the deadlocking property of the system being sliced.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class DeadlockPreservingCriteriaGenerator
		extends AbstractSliceCriteriaGenerator<SootMethod, Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(DeadlockPreservingCriteriaGenerator.class);

	/**
	 * {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.tools.slicer.criteria.generators.AbstractSliceCriteriaGenerator#getCriteriaTemplateMethod()
	 */
	@Override protected Collection<ISliceCriterion> getCriteriaTemplateMethod() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: creating deadlock criteria.");
		}

		final Collection<ISliceCriterion> _result = new HashSet<ISliceCriterion>();
		final Collection<ISliceCriterion> _subResult = new HashSet<ISliceCriterion>();
		final Context _context = new Context();
		final SlicerTool<?> _slicer = getSlicerTool();
		final BasicBlockGraphMgr _bbgMgr = _slicer.getBasicBlockGraphManager();
		final SliceCriteriaFactory _criteriaFactory = SliceCriteriaFactory.getFactory();

		final Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _monitorTriples = _slicer.getMonitorInfo()
				.getMonitorTriples();
		for (final Iterator<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _i = _monitorTriples.iterator(); _i
				.hasNext();) {
			final Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod> _mTriple = _i.next();
			final SootMethod _method = _mTriple.getThird();

			if (shouldConsiderSite(_method)) {
				_subResult.clear();
				_context.setRootMethod(_method);

				if (shouldGenerateCriteriaFrom(_mTriple)) {
					if (_mTriple.getFirst() == null) {
						// add all entry points and return points (including throws) of the method as the criteria
						_subResult.addAll(generateCriteriaForMethodBoundary(_method, _bbgMgr, _criteriaFactory));
						contextualizeCriteriaBasedOnThis(_method, _subResult);
					} else {
						final EnterMonitorStmt _enterStmt = _mTriple.getFirst();

						_subResult.addAll(_criteriaFactory.getCriteria(_method, _enterStmt, true, true));
						_subResult.addAll(_criteriaFactory.getCriteria(_method, _mTriple.getSecond(), true, true));
						_context.setStmt(_enterStmt);
						_context.setProgramPoint(_enterStmt.getOpBox());
						contextualizeCriteriaBasedOnProgramPoint(_context, _subResult);
					}
				}
				_result.addAll(_subResult);
			}
		}

		final InstanceOfPredicate<InvokeStmt, Stmt> _instanceOfPredicate = new InstanceOfPredicate<InvokeStmt, Stmt>(
				InvokeStmt.class);
		for (final Iterator<SootMethod> _i = _slicer.getCallGraph().getReachableMethods().iterator(); _i.hasNext();) {
			final SootMethod _caller = _i.next();
			final Iterator<Stmt> _sl = _bbgMgr.getStmtList(_caller).iterator();
			_context.setRootMethod(_caller);

			for (final Iterator<Stmt> _j = IteratorUtils.filteredIterator(_sl, _instanceOfPredicate); _j.hasNext();) {
				final InvokeStmt _stmt = (InvokeStmt) _j.next();
				final InvokeExpr _ve = _stmt.getInvokeExpr();
				final SootMethod _callee = _ve.getMethod();

				if (_callee.getName().equals("join") && _callee.getParameterCount() == 0
						&& _callee.getReturnType() == VoidType.v()
						&& _callee.getDeclaringClass().getName().equals("java.lang.Thread")) {
					_subResult.clear();
					_subResult.addAll(_criteriaFactory.getCriteria(_caller, _stmt, true, true));
					_context.setStmt(_stmt);
					_context.setProgramPoint(((VirtualInvokeExpr) _ve).getBaseBox());
					contextualizeCriteriaBasedOnProgramPoint(_context, _subResult);
					_result.addAll(_subResult);
				}
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: creating deadlock criteria. - " + _result);
		}

		return _result;
	}

	/**
	 * Generates the slice criteria to capture the boundary statements of the given method.
	 * 
	 * @param method of interest
	 * @param bbgMgr is the basic block graph manager to use.
	 * @param criteriaFactory is the slice criteria factory to use.
	 * @return the collection of slice criteria
	 * @throws IllegalStateException when the basic block graph manager is improperly initialized.
	 * @pre method != null and bbgMgr != null and criteriaFactory != null
	 */
	private Collection<ISliceCriterion> generateCriteriaForMethodBoundary(final SootMethod method,
			final BasicBlockGraphMgr bbgMgr, final SliceCriteriaFactory criteriaFactory) throws IllegalStateException {
		final Collection<ISliceCriterion> _result = new ArrayList<ISliceCriterion>();
		final BasicBlockGraph _bbg = bbgMgr.getBasicBlockGraph(method);

		if (_bbg != null) {
			final Collection<ISliceCriterion> _criteria = criteriaFactory.getCriteria(method, bbgMgr.getStmtList(method).get(
					0), false);
			_result.addAll(_criteria);

			for (final Iterator<BasicBlockGraph.BasicBlock> _j = _bbg.getSinks().iterator(); _j.hasNext();) {
				final BasicBlockGraph.BasicBlock _bb = _j.next();
				final Stmt _stmt = _bb.getTrailerStmt();
				_result.addAll(criteriaFactory.getCriteria(method, _stmt, false));
			}
		} else {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Could not retrieve the basic block graph for " + method.getSignature() + ".  Moving on.");
			}
		}
		return _result;
	}
}

// End of File
