
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

import edu.ksu.cis.indus.common.datastructures.Triple;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;

import edu.ksu.cis.indus.processing.Context;

import edu.ksu.cis.indus.slicer.SliceCriteriaFactory;

import edu.ksu.cis.indus.tools.slicer.SlicerTool;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;

import soot.jimple.EnterMonitorStmt;
import soot.jimple.Stmt;


/**
 * This class generates seed slicing criteria to preserve the deadlocking property of the system being sliced.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class DeadlockPreservingCriteriaGenerator
  extends AbstractSliceCriteriaGenerator {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(DeadlockPreservingCriteriaGenerator.class);

	/**
	 * @see edu.ksu.cis.indus.tools.slicer.criteria.generators.AbstractSliceCriteriaGenerator#getCriteriaTemplateMethod()
	 */
	protected Collection getCriteriaTemplateMethod() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: creating deadlock criteria.");
		}

		final Collection _result = new HashSet();
		final Collection _subResult = new HashSet();
		final Context _context = new Context();
		final SlicerTool _slicer = getSlicerTool();
		final BasicBlockGraphMgr _bbgMgr = _slicer.getBasicBlockGraphManager();
		final SliceCriteriaFactory _criteriaFactory = SliceCriteriaFactory.getFactory();

		for (final Iterator _i = _slicer.getMonitorInfo().getMonitorTriples().iterator(); _i.hasNext();) {
			final Triple _mTriple = (Triple) _i.next();
			final SootMethod _method = (SootMethod) _mTriple.getThird();

			if (shouldConsiderSite(_method)) {
				_subResult.clear();
				_context.setRootMethod(_method);

				if (_method.getDeclaringClass().isApplicationClass() && shouldGenerateCriteriaFrom(_mTriple)) {
					if (_mTriple.getFirst() == null) {
						// add all entry points and return points (including throws) of the method as the criteria
						final BasicBlockGraph _bbg = _bbgMgr.getBasicBlockGraph(_method);

						if (_bbg != null) {
							final Collection _criteria =
								_criteriaFactory.getCriteria(_method, (Stmt) _bbgMgr.getStmtList(_method).get(0), false);
							_subResult.addAll(_criteria);

							for (final Iterator _j = _bbg.getTails().iterator(); _j.hasNext();) {
								final BasicBlock _bb = (BasicBlock) _j.next();
								final Stmt _stmt = _bb.getTrailerStmt();
								_subResult.addAll(_criteriaFactory.getCriteria(_method, _stmt, false));
							}
							contextualizeCriteriaBasedOnThis(_method, _subResult);
						} else {
							if (LOGGER.isWarnEnabled()) {
								LOGGER.warn("Could not retrieve the basic block graph for " + _method.getSignature()
									+ ".  Moving on.");
							}
						}
					} else {
						final EnterMonitorStmt _enterStmt = (EnterMonitorStmt) _mTriple.getFirst();

						_subResult.addAll(_criteriaFactory.getCriteria(_method, _enterStmt, true, true));
						_subResult.addAll(_criteriaFactory.getCriteria(_method, (Stmt) _mTriple.getSecond(), true, true));
						_context.setStmt(_enterStmt);
						_context.setProgramPoint(_enterStmt.getOpBox());
						contextualizeCriteriaBasedOnProgramPoint(_context, _subResult);
					}
				}
				_result.addAll(_subResult);
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: creating deadlock criteria. - " + _result);
		}

		return _result;
	}
}

// End of File
