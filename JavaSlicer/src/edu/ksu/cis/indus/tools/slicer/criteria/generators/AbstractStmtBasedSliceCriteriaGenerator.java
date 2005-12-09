
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
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.slicer.ISliceCriterion;
import edu.ksu.cis.indus.slicer.SliceCriteriaFactory;
import edu.ksu.cis.indus.slicer.SlicingEngine;
import edu.ksu.cis.indus.tools.slicer.SlicerConfiguration;
import edu.ksu.cis.indus.tools.slicer.SlicerTool;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootMethod;
import soot.jimple.Stmt;


/**
 * This class contains the logic to generate slice criteria based on properties of statements.  The subclasses provide the
 * logic pertaining to property-based criteria selection.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <T1> DOCUMENT ME!
 */
public abstract class AbstractStmtBasedSliceCriteriaGenerator<T1>
  extends AbstractSliceCriteriaGenerator<SootMethod, T1> {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractStmtBasedSliceCriteriaGenerator.class);

	/**
	 * Retrieves the criteria based on the information set on this generator.
	 *
	 * @return a collection of criterion.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(ISliceCriterion))
	 */
	@Override protected final Collection<ISliceCriterion> getCriteriaTemplateMethod() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: creating field criteria.");
		}

		final SlicerTool<?> _slicer = getSlicerTool();
		final String _sliceType = ((SlicerConfiguration) _slicer.getActiveConfiguration()).getSliceType();
		final boolean _considerExecution;

		if (_sliceType.equals(SlicingEngine.SliceType.FORWARD_SLICE.name())) {
			_considerExecution = false;
		} else {
			_considerExecution = true;
		}

		final Collection<ISliceCriterion> _result = new HashSet<ISliceCriterion>();
		final SliceCriteriaFactory _criteriaFactory = SliceCriteriaFactory.getFactory();
		final BasicBlockGraphMgr _bbgMgr = _slicer.getBasicBlockGraphManager();
		final ICallGraphInfo _cgi = _slicer.getCallGraph();
		final Collection<SootMethod> _reachableMethods = _cgi.getReachableMethods();
		final Iterator<SootMethod> _i = _reachableMethods.iterator();
		final int _iEnd = _reachableMethods.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final SootMethod _sm = _i.next();

			if (shouldConsiderSite(_sm)) {
				final BasicBlockGraph _bbg = _bbgMgr.getBasicBlockGraph(_sm);
				final List<BasicBlock> _nodeList = _bbg.getNodes();
				final Iterator<BasicBlock> _j = _nodeList.iterator();
				final int _jEnd = _nodeList.size();

				for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
					final BasicBlock _bb = _j.next();

					final List<Stmt> _stmtsOf = _bb.getStmtsOf();
					final Iterator<Stmt> _k = _stmtsOf.iterator();
					final int _kEnd = _stmtsOf.size();

					for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
						final Stmt _stmt = _k.next();

						if (shouldConsiderStmt(_stmt)
							  && shouldGenerateCriteriaFrom(getEntityForCriteriaFiltering(_stmt, _sm))) {
							if (_sliceType.equals(SlicingEngine.SliceType.COMPLETE_SLICE)) {
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

	/**
	 * Retrieves the entity that should be used to filter the statement for criteria generation purposes.  The returned value
	 * is used for filtering purposes only. <code>shouldConsiderStmt(stmt)</code> will be <code>true</code> when this method
	 * is executed.
	 *
	 * @param stmt of interest.
	 * @param sm containing <code>stmt</code>.
	 *
	 * @return the entity.  This implementation returns <code>stmt</code>.
	 *
	 * @pre stmt != null and sm != null
	 */
	protected T1 getEntityForCriteriaFiltering(final Stmt stmt, @SuppressWarnings("unused") final SootMethod sm) {
		return (T1) stmt;
	}

	/**
	 * Checks if the given statement should be considered as slice criteria.  The subclasses should override this method and
	 * provide the logic to check if the <code>stmt</code> has the required properties.
	 *
	 * @param stmt of interest.
	 *
	 * @return <code>true</code> if <code>stmt</code> should be considered; <code>false</code>, otherwise.  This
	 * 		   implementation always returns <code>true</code>.
	 *
	 * @pre stmt != null
	 */
	protected boolean shouldConsiderStmt(@SuppressWarnings("unused") final Stmt stmt) {
		return true;
	}
}

// End of File
