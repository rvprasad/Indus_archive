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

import edu.ksu.cis.indus.annotations.AEmpty;
import edu.ksu.cis.indus.common.collections.CollectionUtils;
import edu.ksu.cis.indus.common.collections.IPredicate;
import edu.ksu.cis.indus.common.collections.MapUtils;
import edu.ksu.cis.indus.common.collections.SetUtils;
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
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Body;
import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.ValueBox;
import soot.jimple.DefinitionStmt;
import soot.jimple.Stmt;

/**
 * This class provides use-def information for local variables in a method. The analysis is performed at basic block level.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class LocalUseDefAnalysisv2
		implements IUseDefInfo<DefinitionStmt, Pair<Local, Stmt>> {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(LocalUseDefAnalysisv2.class);

	/**
	 * The control flow graph used to calculate the use-def info.
	 */
	private BasicBlockGraph bbGraph;

	/**
	 * A map from local and statement pair to a collection of def statement.
	 */
	private final Map<Pair<Local, Stmt>, Collection<DefinitionStmt>> defInfo = new HashMap<Pair<Local, Stmt>, Collection<DefinitionStmt>>();

	/**
	 * A map from definition statement to a collection of statements.
	 */
	private final Map<DefinitionStmt, Collection<Pair<Local, Stmt>>> useInfo = new HashMap<DefinitionStmt, Collection<Pair<Local, Stmt>>>();

	/**
	 * Creates a new LocalDefsAnalysis object.
	 * 
	 * @param graph is the control flow graph used to calculate the use-def info.
	 * @pre graph != null
	 */
	public LocalUseDefAnalysisv2(final BasicBlockGraph graph) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Analyzing " + graph.getStmtGraph().getBody().getMethod());
		}

		final Body _body = graph.getStmtGraph().getBody();
		final List<Local> _listOfLocals = new ArrayList<Local>();
		_listOfLocals.addAll(_body.getLocals());
		bbGraph = graph;
		analyze(_listOfLocals);
		bbGraph = null;

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Analyzing " + graph.getStmtGraph().getBody().getMethod());
		}
	}

	// / CLOVER:OFF

	/**
	 * Creates a new LocalUseDefAnalysisv2 object.
	 */
	@AEmpty private LocalUseDefAnalysisv2() {
		// does nothing
	}

	// / CLOVER:ON

	/**
	 * Retrieves the definitions of <code>local</code> that reach <code>stmt</code>.
	 * 
	 * @param local variable.
	 * @param stmt in which <code>local</code> occurs.
	 * @param method <i>ignored</i>.
	 * @return a collection of def statements.
	 * @pre local != null and stmt != null
	 * @post result != null
	 */
	public Collection<DefinitionStmt> getDefs(final Local local, final Stmt stmt,
			@SuppressWarnings("unused") final SootMethod method) {
		return Collections.unmodifiableCollection(MapUtils.queryCollection(defInfo, new Pair<Local, Stmt>(local, stmt)));
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param method <i>ignored</i>.
	 */
	public Collection<DefinitionStmt> getDefs(final Stmt useStmt, @SuppressWarnings("unused") final SootMethod method) {
		final Collection<DefinitionStmt> _result = new HashSet<DefinitionStmt>();

		for (final Iterator<ValueBox> _i = useStmt.getUseBoxes().iterator(); _i.hasNext();) {
			final ValueBox _vb = _i.next();
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
	public Collection<? extends Comparable<?>> getIds() {
		return Collections.singleton(IUseDefInfo.LOCAL_USE_DEF_ID);
	}

	/**
	 * Retrieves the uses of definitions at <code>stmt</code>.
	 * 
	 * @param stmt in which a definition occurs.
	 * @param method <i>ignored</i>.
	 * @return a collection of statements.
	 * @pre stmt != null
	 * @post result != null
	 */
	public Collection<Pair<Local, Stmt>> getUses(final DefinitionStmt stmt,
			@SuppressWarnings("unused") final SootMethod method) {
		return MapUtils.queryCollection(useInfo, stmt);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IStatus#isStable()
	 */
	public boolean isStable() {
		return true;
	}

	/**
	 * Performs the analysis to calculate the info.
	 * 
	 * @param listOfLocals is a list of the locals.
	 * @pre local2defs != null and listOfLocals != null
	 */
	private void analyze(final List<Local> listOfLocals) {
		final IWorkBag<BasicBlock> _wb = new FIFOWorkBag<BasicBlock>();
		final Map<BasicBlock, Map<Local, DefinitionStmt>> _intrabb2local2defStmt = new HashMap<BasicBlock, Map<Local, DefinitionStmt>>();
		final Map<DefinitionStmt, Local> _defStmt2local = new HashMap<DefinitionStmt, Local>();
		final Map<BasicBlock, Collection<DefinitionStmt>> _bb2reachingDefStmts = new HashMap<BasicBlock, Collection<DefinitionStmt>>();
		final Collection<DefinitionStmt> _defsExitingBB = new ArrayList<DefinitionStmt>();

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
			final BasicBlock _bb = _wb.getWork();
			_defsExitingBB.clear();
			_defsExitingBB.addAll(MapUtils.getCollectionFromMap(_bb2reachingDefStmts, _bb));

			final Map<Local, DefinitionStmt> _intraBBLocal2defStmt = _intrabb2local2defStmt.get(_bb);

			// if the basic block has definitions statement then remove definitions killed in by these definitions.
			if (_intraBBLocal2defStmt != null) {
				CollectionUtils.filter(_defsExitingBB, new IPredicate<DefinitionStmt>() {

					public boolean evaluate(final DefinitionStmt object) {
						return _intraBBLocal2defStmt.get(_defStmt2local.get(object)) == null;
					}
				});
				_defsExitingBB.addAll(_intraBBLocal2defStmt.values());
			}

			// propagate the information to successor basic blocks
			final Iterator<BasicBlock> _i = _bb.getSuccsOf().iterator();
			final int _iEnd = _bb.getSuccsOf().size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final BasicBlock _succBB = _i.next();
				final Collection<DefinitionStmt> _defsReachingSuccBB = MapUtils.getCollectionFromMap(_bb2reachingDefStmts,
						_succBB);

				if (!SetUtils.isSubCollection(_defsExitingBB, _defsReachingSuccBB)) {
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
	 * @pre defStmt2local != null and bb2reachingDefStmts != null
	 */
	private void calculateIntraBBUseDefInfo(final Map<DefinitionStmt, Local> defStmt2local,
			final Map<BasicBlock, Collection<DefinitionStmt>> bb2reachingDefStmts) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("calculating information at intra bb level and record the use-def information.");
		}

		// calculate information at intra bb level and record the use-def information.
		final PairManager _pairMgr = new PairManager(false, true);
		final Map<Local, Set<DefinitionStmt>> _local2defStmts = MapUtils.invertMap(defStmt2local);

		// process each basic block.
		final Collection<BasicBlock> _keySet = bb2reachingDefStmts.keySet();
		final Iterator<BasicBlock> _i = _keySet.iterator();
		final int _iEnd = _keySet.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final BasicBlock _bb = _i.next();
			final Collection<DefinitionStmt> _rDefs = bb2reachingDefStmts.get(_bb);
			final List<Stmt> _stmtsOf = _bb.getStmtsOf();
			final Iterator<Stmt> _j = _stmtsOf.iterator();
			final int _jEnd = _stmtsOf.size();

			// process each statement in the basic block recording reaching def information.
			for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
				final Stmt _stmt = _j.next();
				final List<ValueBox> _useBoxes = _stmt.getUseBoxes();
				final Iterator<ValueBox> _k = _useBoxes.iterator();
				final int _kEnd = _useBoxes.size();

				for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
					final ValueBox _vb = _k.next();
					final Value _value = _vb.getValue();

					if (_value instanceof Local) {
						final Pair<Local, Stmt> _pair = _pairMgr.getPair((Local) _value, _stmt);
						final Collection<DefinitionStmt> _d = SetUtils.intersection(_local2defStmts.get(_value), _rDefs);
						defInfo.put(_pair, _d);
						for (final DefinitionStmt _defStmt : _d) {
							MapUtils.putIntoCollectionInMap(useInfo, _defStmt, _pair);
						}
					}
				}

				// prune reaching def for further intra basic block processing
				if (defStmt2local.containsKey(_stmt)) {
					_rDefs.removeAll(_local2defStmts.get(defStmt2local.get(_stmt)));
					_rDefs.add((DefinitionStmt) _stmt);
				}
			}
		}
	}

	/**
	 * Captures the definitions alive at the end of each basic block.
	 * 
	 * @param listOfLocals is a list of the locals.
	 * @param intraBB2local2exitDefStmts maps a basic block to a map from locals to definitions statements alive at the end of
	 *            the basic block.
	 * @param defStmt2local maps definitions statments to the locals defined in them.
	 * @return a collection of defining basic blocks.
	 * @pre listOfLocals != null and bb2local2exitDefStmts != null and defStmt2local != null
	 * @post result != null
	 */
	private Collection<BasicBlock> seedDefInfo(final List<Local> listOfLocals,
			final Map<BasicBlock, Map<Local, DefinitionStmt>> intraBB2local2exitDefStmts,
			final Map<DefinitionStmt, Local> defStmt2local) {
		final Collection<BasicBlock> _defBBs = new HashSet<BasicBlock>();

		for (final Iterator<Stmt> _i = bbGraph.getStmtGraph().iterator(); _i.hasNext();) {
			final Stmt _stmt = _i.next();

			if (_stmt instanceof DefinitionStmt) {
				final DefinitionStmt _defStmt = (DefinitionStmt) _stmt;
				final Value _leftOp = _defStmt.getLeftOp();

				if (_leftOp instanceof Local) {
					final BasicBlock _enclosingBlock = bbGraph.getEnclosingBlock(_defStmt);
					if (_enclosingBlock != null) {
						_defBBs.add(_enclosingBlock);
						defStmt2local.put(_defStmt, (Local) _leftOp);
					} else if (LOGGER.isWarnEnabled()) {
						LOGGER.warn(_defStmt + " in " + bbGraph.getStmtGraph().getBody().getMethod() + " is not enclosed "
								+ "by a basic block!");
					}
				}
			}
		}

		final int _numOfLocals = listOfLocals.size();
		Map<Local, DefinitionStmt> _local2def = new HashMap<Local, DefinitionStmt>(_numOfLocals);
		final Iterator<BasicBlock> _i = _defBBs.iterator();
		final int _iEnd = _defBBs.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final BasicBlock _bb = _i.next();
			final List<Stmt> _stmtsOf = _bb.getStmtsOf();
			final Iterator<Stmt> _j = _stmtsOf.iterator();
			final int _jEnd = _stmtsOf.size();

			for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
				final Stmt _stmt = _j.next();

				if (_stmt instanceof DefinitionStmt) {
					final DefinitionStmt _defStmt = (DefinitionStmt) _stmt;

					if (_defStmt.getLeftOp() instanceof Local) {
						_local2def.put((Local) _defStmt.getLeftOp(), _defStmt);
					}
				}
			}
			intraBB2local2exitDefStmts.put(_bb, _local2def);
			_local2def = new HashMap<Local, DefinitionStmt>(_numOfLocals);
		}

		return intraBB2local2exitDefStmts.keySet();
	}
}

// End of File
