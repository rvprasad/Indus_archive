
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
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
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.LIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.Local;
import soot.Value;
import soot.ValueBox;

import soot.jimple.DefinitionStmt;
import soot.jimple.Stmt;

import soot.toolkits.graph.UnitGraph;


/**
 * This class provides use-def information for local variables in a method.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class LocalUseDefAnalysis {
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
	 * A manager of Pair objects.
	 */
	private final PairManager pairMgr = new PairManager();

	/**
	 * A list of statements in the given method.
	 *
	 * @invariant stmtList.oclIsKindOf(Sequence(Stmt))
	 */
	private List stmtList;

	/**
	 * The control flow graph used to calculate the use-def info.
	 */
	private UnitGraph unitGraph;

	/**
	 * Creates a new LocalDefsAnalysis object.
	 *
	 * @param graph is the control flow graph used to calculate the use-def info.
	 *
	 * @pre graph != null
	 */
	public LocalUseDefAnalysis(final UnitGraph graph) {
		final Body _body = graph.getBody();
		final BitSet[][] _l2defs = new BitSet[_body.getUnits().size()][_body.getLocalCount()];
		stmtList = new ArrayList();
		stmtList.addAll(graph.getBody().getUnits());

		final List _listOfLocals = new ArrayList();
		_listOfLocals.addAll(graph.getBody().getLocals());
		unitGraph = graph;
		analyze(_l2defs, _listOfLocals);
		extract(_l2defs, _listOfLocals);
		unitGraph = null;
		stmtList = null;
	}

	/**
	 * Retrieves the definitions of <code>local</code> that reach <code>stmt</code>.
	 *
	 * @param local variable.
	 * @param stmt in which <code>local</code> occurs.
	 *
	 * @return a collection of def statements.
	 *
	 * @pre local != null and stmt != null
	 * @post result != null and result.oclIsKindOf(Collection(DefinitionStmt))
	 */
	public Collection getDefsOf(final Local local, final Stmt stmt) {
		return Collections.unmodifiableCollection((Collection) CollectionsUtilities.getFromMap(defInfo,
				pairMgr.getUnOptimizedPair(local, stmt), Collections.EMPTY_LIST));
	}

	/**
	 * Retrieves the definitions of local at the statement encapsulated in <code>losalStmtPair</code>.
	 *
	 * @param localStmtPair is a pair consisting of a <code>Local</code> and a <code>Stmt</code> in which the local occurs.
	 *
	 * @return a collection of def statements.
	 *
	 * @pre localStmtPair != null
	 * @post result != null and result.oclIsKindOf(Collection(DefinitionStmt))
	 */
	public Collection getDefsOf(final Pair localStmtPair) {
		return Collections.unmodifiableCollection((Collection) CollectionsUtilities.getFromMap(defInfo, localStmtPair,
				Collections.EMPTY_LIST));
	}

	/**
	 * Retrieves the uses of definitions at <code>stmt</code>.
	 *
	 * @param stmt in which a definition occurs.
	 *
	 * @return a collection of statements.
	 *
	 * @pre stmt != null
	 * @post result != null and result.oclIsKindOf(Collection(Stmt))
	 */
	public Collection getUsesOf(final Stmt stmt) {
		return (Collection) CollectionsUtilities.getFromMap(useInfo, stmt, Collections.EMPTY_LIST);
	}

	/**
	 * Performs the analysis to calculate the info.
	 *
	 * @param local2defs maps a statment to a collection of def sets of each locals in the method.
	 * @param listOfLocals is a list of the locals.
	 *
	 * @pre local2defs != null and listOfLocals != null
	 * @pre listOfLocals.oclIsKindOf(List(Local))
	 */
	private void analyze(final BitSet[][] local2defs, final List listOfLocals) {
		final BitSet _temp = new BitSet();
		final int _size = stmtList.size();
		final IWorkBag _wb = new LIFOWorkBag();
		final Collection _defStmts = seedDefInfo(local2defs, listOfLocals);
		_wb.addAllWork(_defStmts);

		while (_wb.hasWork()) {
			final Stmt _stmt = (Stmt) _wb.getWork();
			final BitSet[] _defsAtStmt = local2defs[stmtList.indexOf(_stmt)];
			int _killIndex = -1;

			if (_stmt instanceof DefinitionStmt) {
				_killIndex = listOfLocals.indexOf(((ValueBox) _stmt.getDefBoxes().iterator().next()).getValue());
			}

			for (final Iterator _i = unitGraph.getSuccsOf(_stmt).iterator(); _i.hasNext();) {
				final Stmt _succ = (Stmt) _i.next();
				final int _succIndex = stmtList.indexOf(_succ);
				final BitSet[] _defsAtSucc = local2defs[_succIndex];
				boolean _flag = false;

				for (int _j = _defsAtStmt.length - 1; _j >= 0; _j--) {
					if (_j != _killIndex) {
						final BitSet _defsOfLocalAtStmt = _defsAtStmt[_j];

						if (_defsOfLocalAtStmt != null) {
							if (_defsAtSucc[_j] == null) {
								_defsAtSucc[_j] = new BitSet(_size);
							}
							_temp.clear();
							_temp.or(_defsOfLocalAtStmt);
							_temp.andNot(_defsAtSucc[_j]);
							_flag |= _temp.cardinality() > 0;
							_defsAtSucc[_j].or(_defsOfLocalAtStmt);
						}
					}
				}

				if (_flag) {
					_wb.addWorkNoDuplicates(_succ);
				}
			}
		}
	}

	/**
	 * Extracts the info encoded in bits and captures as occurring between entities.
	 *
	 * @param local2defs maps a statment to a collection of def sets of each locals in the method.
	 * @param localList is a list of the locals.
	 *
	 * @pre local2defs != null and listOfLocals != null
	 * @pre listOfLocals.oclIsKindOf(List(Local))
	 */
	private void extract(final BitSet[][] local2defs, final List localList) {
		final Collection _cache = new ArrayList();

		for (final Iterator _i = unitGraph.iterator(); _i.hasNext();) {
			final Stmt _stmt = (Stmt) _i.next();
			final int _stmtIndex = stmtList.indexOf(_stmt);
			final BitSet[] _stmtDefSet = local2defs[_stmtIndex];

			for (final Iterator _j = _stmt.getUseBoxes().iterator(); _j.hasNext();) {
				final ValueBox _ub = (ValueBox) _j.next();

				if (_ub.getValue() instanceof Local) {
					final Local _local = (Local) _ub.getValue();
					final int _lindex = localList.indexOf(_local);
					final BitSet _defs = _stmtDefSet[_lindex];

					if (!(_defs == null || _defs.isEmpty())) {
						_cache.clear();

						for (int _k = _defs.nextSetBit(0); _k >= 0; _k = _defs.nextSetBit(_k + 1)) {
							final Object _defStmt = stmtList.get(_k);
							_cache.add(_defStmt);
							CollectionsUtilities.getListFromMap(useInfo, _defStmt).add(_stmt);
						}
						CollectionsUtilities.putAllIntoSetInMap(defInfo, pairMgr.getUnOptimizedPair(_local, _stmt), _cache);
					}
				}
			}
		}
	}

	/**
	 * Captures definition info into <code>local2defs</code>.
	 *
	 * @param local2defs maps a statment to a collection of def sets of each locals in the method.
	 * @param listOfLocals is a list of the locals.
	 *
	 * @return a collection of definition statements.
	 *
	 * @pre local2defs != null and listOfLocals != null
	 * @pre listOfLocals.oclIsKindOf(List(Local))
	 * @post result != null and result.oclIsKindOf(Collection(DefinitionStmt))
	 */
	private Collection seedDefInfo(final BitSet[][] local2defs, final List listOfLocals) {
		final Collection _result = new ArrayList();
		final int _sizeOfStmtList = stmtList.size();

		for (final Iterator _i = unitGraph.iterator(); _i.hasNext();) {
			final Stmt _stmt = (Stmt) _i.next();

			if (_stmt instanceof DefinitionStmt) {
				final ValueBox _vb = (ValueBox) _stmt.getDefBoxes().iterator().next();
				final Value _value = _vb.getValue();

				if (_value instanceof Local) {
					final int _localIndex = listOfLocals.indexOf(_value);
					final int _stmtIndex = stmtList.indexOf(_stmt);

					for (final Iterator _j = unitGraph.getSuccsOf(_stmt).iterator(); _j.hasNext();) {
						final Stmt _succ = (Stmt) _j.next();
						BitSet _temp = local2defs[stmtList.indexOf(_succ)][_localIndex];

						if (_temp == null) {
							_temp = new BitSet(_sizeOfStmtList);
							local2defs[stmtList.indexOf(_succ)][_localIndex] = _temp;
						}
						_temp.set(_stmtIndex, true);
						_result.add(_succ);
					}
				}
			}
		}
		return _result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2004/06/15 08:54:48  venku
   - implemented method local use-def info analysis.
   - implemented identified based dependence analysis based on above analysis.
 */
