
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

import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;

import edu.ksu.cis.indus.slicer.SliceCriteriaFactory;
import edu.ksu.cis.indus.slicer.SlicingEngine;

import edu.ksu.cis.indus.tools.slicer.SlicerConfiguration;
import edu.ksu.cis.indus.tools.slicer.SlicerTool;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootField;
import soot.SootMethod;

import soot.jimple.FieldRef;
import soot.jimple.Stmt;


/**
 * This class can be used to generate fields-based slice criteria guided by a specification matcher.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class FieldBasedCriteriaGenerator
  extends AbstractSliceCriteriaGenerator {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(FieldBasedCriteriaGenerator.class);

	/**
	 * Retrieves the criteria based on the information set on this generator.
	 *
	 * @return a collection of criterion.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(ISliceCriterion))
	 */
	public Collection getCriteriaTemplateMethod() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: creating field criteria.");
		}

		final SlicerTool _slicer = getSlicerTool();
		final Object _sliceType = ((SlicerConfiguration) _slicer.getActiveConfiguration()).getSliceType();
		final boolean _considerExecution;

		if (_sliceType.equals(SlicingEngine.FORWARD_SLICE)) {
			_considerExecution = false;
		} else {
			_considerExecution = true;
		}

		final Collection _result = new HashSet();
		final SliceCriteriaFactory _criteriaFactory = SliceCriteriaFactory.getFactory();
		final BasicBlockGraphMgr _bbgMgr = _slicer.getBasicBlockGraphManager();
		final ICallGraphInfo _cgi = _slicer.getCallGraph();
		final Collection _reachableMethods = _cgi.getReachableMethods();
		final Iterator _i = _reachableMethods.iterator();
		final int _iEnd = _reachableMethods.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final SootMethod _sm = (SootMethod) _i.next();
			final BasicBlockGraph _bbg = _bbgMgr.getBasicBlockGraph(_sm);
			final List _nodeList = _bbg.getNodes();
			final Iterator _j = _nodeList.iterator();
			final int _jEnd = _nodeList.size();

			for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
				final BasicBlock _bb = (BasicBlock) _j.next();

				final List _stmtsOf = _bb.getStmtsOf();
				final Iterator _k = _stmtsOf.iterator();
				final int _kEnd = _stmtsOf.size();

				for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
					final Stmt _stmt = (Stmt) _k.next();

					if (_stmt.containsFieldRef()) {
						final FieldRef _fRef = _stmt.getFieldRef();
						final SootField _field = _fRef.getField();

						if (shouldGenerateCriteriaFrom(_field)) {
							if (_sliceType.equals(SlicingEngine.COMPLETE_SLICE)) {
								_result.addAll(_criteriaFactory.getCriteria(_sm, _stmt, true));
								_result.addAll(_criteriaFactory.getCriteria(_sm, _stmt, false));
							} else {
								_result.addAll(_criteriaFactory.getCriteria(_sm, _stmt, _considerExecution));
							}
						}
					}
				}
			}
		}
		return _result;
	}
}

// End of File
