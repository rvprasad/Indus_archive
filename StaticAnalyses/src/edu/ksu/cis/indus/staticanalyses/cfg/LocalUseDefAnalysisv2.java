
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

package edu.ksu.cis.indus.staticanalyses.cfg;

import edu.ksu.cis.indus.common.CollectionsUtilities;
import edu.ksu.cis.indus.common.datastructures.FIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;

import edu.ksu.cis.indus.interfaces.IUseDefInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.Predicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.Body;
import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.ValueBox;

import soot.jimple.DefinitionStmt;
import soot.jimple.Stmt;


/**
 * This class provides use-def information for local variables in a method.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class LocalUseDefAnalysisv2
  implements IUseDefInfo {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(LocalUseDefAnalysisv2.class);

	/** 
	 * A map from local and statement pair to a collection of def statement.
	 *
	 * @invariant defInfo.oclIsKindOf(Map(Pair(Local, Stmt), Collection(DefinitionStmt))
	 */
	private final Map defInfo = new HashMap();

	/** 
	 * A map from definition statement to a collection of statements.
	 *
	 * @invariant defInfo.oclIsKindOf(Map(DefinitionStmt, Collection(Stmt))
	 */
	private final Map useInfo = new HashMap();

	/** 
	 * The control flow graph used to calculate the use-def info.
	 */
	private BasicBlockGraph bbGraph;

	/**
	 * Creates a new LocalDefsAnalysis object.
	 *
	 * @param graph is the control flow graph used to calculate the use-def info.
	 *
	 * @pre graph != null
	 */
	public LocalUseDefAnalysisv2(final BasicBlockGraph graph) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Analyzing " + graph.getStmtGraph().getBody().getMethod());
		}

		final Body _body = graph.getStmtGraph().getBody();
		final List _listOfLocals = new ArrayList();
		_listOfLocals.addAll(_body.getLocals());
		bbGraph = graph;
		analyze(_listOfLocals);
		bbGraph = null;

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Analyzing " + graph.getStmtGraph().getBody().getMethod());
		}
	}

	/// CLOVER:OFF

	/**
	 * Creates a new LocalUseDefAnalysisv2 object.
	 */
	private LocalUseDefAnalysisv2() {
	}

	/// CLOVER:ON

	/**
	 * Retrieves the definitions of <code>local</code> that reach <code>stmt</code>.
	 *
	 * @param local variable.
	 * @param stmt in which <code>local</code> occurs.
	 * @param method <i>ignored</i>.
	 *
	 * @return a collection of def statements.
	 *
	 * @pre local != null and stmt != null
	 * @post result != null and result.oclIsKindOf(Collection(DefinitionStmt))
	 */
	public Collection getDefs(final Local local, final Stmt stmt, final SootMethod method) {
		return Collections.unmodifiableCollection((Collection) MapUtils.getObject(defInfo, new Pair(local, stmt),
				Collections.EMPTY_LIST));
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param method <i>ignored</i>.
	 */
	public Collection getDefs(final Stmt useStmt, final SootMethod method) {
		final Collection _result = new HashSet();

		for (final Iterator _i = useStmt.getUseBoxes().iterator(); _i.hasNext();) {
			final ValueBox _vb = (ValueBox) _i.next();
			final Value _value = _vb.getValue();

			if (_value instanceof Local) {
				_result.addAll(getDefs((Local) _value, useStmt, null));
			}
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IIdentification#getIds()
	 */
	public Collection getIds() {
		return Collections.singleton(IUseDefInfo.LOCAL_USE_DEF_ID);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IStatus#isStable()
	 */
	public boolean isStable() {
		return true;
	}

	/**
	 * Retrieves the uses of definitions at <code>stmt</code>.
	 *
	 * @param stmt in which a definition occurs.
	 * @param method <i>ignored</i>.
	 *
	 * @return a collection of statements.
	 *
	 * @pre stmt != null
	 * @post result != null and result.oclIsKindOf(Collection(Stmt))
	 */
	public Collection getUses(final DefinitionStmt stmt, final SootMethod method) {
		return (Collection) MapUtils.getObject(useInfo, stmt, Collections.EMPTY_LIST);
	}

	/**
	 * Performs the analysis to calculate the info.
	 *
	 * @param listOfLocals is a list of the locals.
	 *
	 * @pre local2defs != null and listOfLocals != null
	 * @pre listOfLocals.oclIsKindOf(List(Local))
	 */
	private void analyze(final List listOfLocals) {
		final IWorkBag _wb = new FIFOWorkBag();
		final Map _intrabb2local2defStmt = new HashMap();
		final Map _defStmt2local = new HashMap();
		final Map _bb2reachingDefStmts = new HashMap();
		final Collection _defsExitingBB = new ArrayList();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("calculating definition basic blocks.");
		}

		// populate the workbag with locals defining basic blocks
		_wb.addAllWork(seedDefInfo(listOfLocals, _intrabb2local2defStmt, _defStmt2local));

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("propagating information across basic blocks.");
		}

		// propagate the inforamtion
		while (_wb.hasWork()) {
			final BasicBlock _bb = (BasicBlock) _wb.getWork();
			_defsExitingBB.clear();
			_defsExitingBB.addAll(CollectionsUtilities.getSetFromMap(_bb2reachingDefStmts, _bb));

			final Map _intraBBLocal2defStmt = (Map) _intrabb2local2defStmt.get(_bb);

			// if the basic block has definitions statement then remove definitions killed in by these definitions.
			if (_intraBBLocal2defStmt != null) {
				CollectionUtils.filter(_defsExitingBB,
					new Predicate() {
						public boolean evaluate(final Object object) {
							return _intraBBLocal2defStmt.get(_defStmt2local.get(object)) == null;
						}
					});
				_defsExitingBB.addAll(_intraBBLocal2defStmt.values());
			}

			// propagate the information to successor basic blocks
			final Iterator _i = _bb.getSuccsOf().iterator();
			final int _iEnd = _bb.getSuccsOf().size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final BasicBlock _succBB = (BasicBlock) _i.next();
				final Collection _defsReachingSuccBB = CollectionsUtilities.getSetFromMap(_bb2reachingDefStmts, _succBB);

				if (!CollectionUtils.isSubCollection(_defsExitingBB, _defsReachingSuccBB)) {
					_defsReachingSuccBB.addAll(_defsExitingBB);
					_wb.addWorkNoDuplicates(_succBB);
				}
			}
		}

		calculateIntraBBUseDefInfo(_defStmt2local, _bb2reachingDefStmts);
	}

	/**
	 * Calculates Intra basic-block use-def info while recording the use-def info.
	 *
	 * @param defStmt2local maps definition statement to the local being defined.
	 * @param bb2reachingDefStmts maps basic blocks to the definition statements that reaches the basic block.
	 *
	 * @pre defStmt2local != null and bb2reachingDefStmts != null
	 * @pre defStmt2local.oclIsKindOf(Map(DefinitionStmt, Local))
	 * @pre bb2reachingDefStmts.oclIsKindOf(Map(BasicBlock, Collection(DefinitionStmts)))
	 */
	private void calculateIntraBBUseDefInfo(final Map defStmt2local, final Map bb2reachingDefStmts) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("calculating information at intra bb level and record the use-def information.");
		}

		// calculate information at intra bb level and record the use-def information.
		final Collection _defStmts = new HashSet();
		final PairManager _pairMgr = new PairManager(false, true);
		final Map _local2defStmts = CollectionsUtilities.invertMap(defStmt2local);

		// process each basic block.
		final Collection _keySet = bb2reachingDefStmts.keySet();
		final Iterator _i = _keySet.iterator();
		final int _iEnd = _keySet.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final BasicBlock _bb = (BasicBlock) _i.next();
			final Collection _rDefs = (Collection) bb2reachingDefStmts.get(_bb);
			final List _stmtsOf = _bb.getStmtsOf();
			final Iterator _j = _stmtsOf.iterator();
			final int _jEnd = _stmtsOf.size();

			// process each statement in the basic block recording reaching def information. 
			for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
				final Stmt _stmt = (Stmt) _j.next();
				final List _useBoxes = _stmt.getUseBoxes();
				final Iterator _k = _useBoxes.iterator();
				final int _kEnd = _useBoxes.size();
				_defStmts.clear();

				for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
					final ValueBox _vb = (ValueBox) _k.next();
					final Value _value = _vb.getValue();

					if (_value instanceof Local) {
						final Collection _d = CollectionUtils.intersection((Collection) _local2defStmts.get(_value), _rDefs);
						defInfo.put(_pairMgr.getPair(_value, _stmt), _d);
						_defStmts.addAll(_d);
					}
				}

				// record use information
				final Iterator _l = _defStmts.iterator();
				final int _lEnd = _defStmts.size();

				for (int _lIndex = 0; _lIndex < _lEnd; _lIndex++) {
					final Object _defStmt = _l.next();
					CollectionsUtilities.putIntoListInMap(useInfo, _defStmt, _stmt);
				}

				// prune reaching def for further intra basic block processing
				final Local _local = (Local) defStmt2local.get(_stmt);

				if (_local != null) {
					_rDefs.removeAll((Collection) _local2defStmts.get(_local));
					_rDefs.add(_stmt);
				}
			}
		}
	}

	/**
	 * Captures the definitions alive at the end of each basic block.
	 *
	 * @param listOfLocals is a list of the locals.
	 * @param intraBB2local2exitDefStmts maps a basic block to a map from locals to definitions statements alive at the end
	 * 		  of the basic block.
	 * @param defStmt2local maps definitions statments to the locals defined in them.
	 *
	 * @return a collection of defining basic blocks.
	 *
	 * @pre listOfLocals != null and bb2local2exitDefStmts != null and defStmt2local != null
	 * @pre listOfLocals.oclIsKindOf(List(Local))
	 * @post result != null and result.oclIsKindOf(Collection(BasicBlock))
	 * @post bb2local2exitDefStmts.oclIsKindOf(Map(BasicBlock, Map(Local, DefinitionStmt)))
	 * @post defStmt2local.oclIsKindOf(Map(DefinitionStmt, Local))
	 */
	private Collection seedDefInfo(final List listOfLocals, final Map intraBB2local2exitDefStmts, final Map defStmt2local) {
		final Collection _defBBs = new HashSet();

		for (final Iterator _i = bbGraph.getStmtGraph().iterator(); _i.hasNext();) {
			final Stmt _stmt = (Stmt) _i.next();

			if (_stmt instanceof DefinitionStmt) {
				final DefinitionStmt _defStmt = (DefinitionStmt) _stmt;
				final Value _leftOp = _defStmt.getLeftOp();

				if (_leftOp instanceof Local) {
					_defBBs.add(bbGraph.getEnclosingBlock(_defStmt));
					defStmt2local.put(_defStmt, _leftOp);
				}
			}
		}

		final int _numOfLocals = listOfLocals.size();
		Map _local2def = new HashMap(_numOfLocals);
		final Iterator _i = _defBBs.iterator();
		final int _iEnd = _defBBs.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final BasicBlock _bb = (BasicBlock) _i.next();
			final List _stmtsOf = _bb.getStmtsOf();
			final Iterator _j = _stmtsOf.iterator();
			final int _jEnd = _stmtsOf.size();

			for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
				final Stmt _stmt = (Stmt) _j.next();

				if (_stmt instanceof DefinitionStmt) {
					final DefinitionStmt _defStmt = (DefinitionStmt) _stmt;

					if (_defStmt.getLeftOp() instanceof Local) {
						_local2def.put(_defStmt.getLeftOp(), _defStmt);
					}
				}
			}
			intraBB2local2exitDefStmts.put(_bb, _local2def);
			_local2def = new HashMap(_numOfLocals);
		}

		return intraBB2local2exitDefStmts.keySet();
	}
}

// End of File
