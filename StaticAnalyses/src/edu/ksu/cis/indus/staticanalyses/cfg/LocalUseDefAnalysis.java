
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

import edu.ksu.cis.indus.interfaces.IUseDefInfo;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;

import soot.Body;
import soot.Local;
import soot.PatchingChain;
import soot.SootMethod;
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
public final class LocalUseDefAnalysis
  implements IUseDefInfo {
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
		final PatchingChain _stmts = _body.getUnits();
		final BitSet[][] _l2defs = new BitSet[_stmts.size()][_body.getLocalCount()];
		stmtList = new ArrayList();
		stmtList.addAll(_stmts);

		final List _listOfLocals = new ArrayList();
		_listOfLocals.addAll(_body.getLocals());
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
	 * @param local2defs maps a statment to a collection of def sets of each locals in the method.
	 * @param listOfLocals is a list of the locals.
	 *
	 * @pre local2defs != null and listOfLocals != null
	 * @pre listOfLocals.oclIsKindOf(List(Local))
	 */
	private void analyze(final BitSet[][] local2defs, final List listOfLocals) {
		final IWorkBag _wb = new FIFOWorkBag();
		final BitSet _temp = new BitSet();
		final Map _stmt2localIndices = new HashMap();

		_wb.addAllWork(seedDefInfo(local2defs, listOfLocals, _stmt2localIndices));

		while (_wb.hasWork()) {
			final Stmt _stmt = (Stmt) _wb.getWork();
			final BitSet[] _defsAtStmt = local2defs[stmtList.indexOf(_stmt)];
			final Collection _localIndices = (Collection) _stmt2localIndices.get(_stmt);

			// if the current statement is a def stmt then remove the index of the local being killed via definition 
			if (_stmt instanceof DefinitionStmt) {
				final Value _leftOp = ((DefinitionStmt) _stmt).getLeftOp();

				if (_leftOp instanceof Local) {
					_localIndices.remove(new Integer(listOfLocals.indexOf(_leftOp)));
				}
			}

			// propagate the local defs to the successors
			for (final Iterator _i = _localIndices.iterator(); _i.hasNext();) {
				final Integer _localIndexValue = (Integer) _i.next();
				final int _localIndex = _localIndexValue.intValue();
				final BitSet _defsOfLocalAtStmt = _defsAtStmt[_localIndex];

				for (final Iterator _j = unitGraph.getSuccsOf(_stmt).iterator(); _j.hasNext();) {
					final Stmt _succ = (Stmt) _j.next();
					final int _succIndex = stmtList.indexOf(_succ);
					final BitSet[] _defsAtSucc = local2defs[_succIndex];

					if (_defsAtSucc[_localIndex] == null) {
						_defsAtSucc[_localIndex] = new BitSet();
					}

					_temp.clear();
					_temp.or(_defsOfLocalAtStmt);
					_temp.andNot(_defsAtSucc[_localIndex]);

					if (_temp.cardinality() > 0) {
						_defsAtSucc[_localIndex].or(_defsOfLocalAtStmt);
						_wb.addWorkNoDuplicates(_succ);
						((Collection) _stmt2localIndices.get(_succ)).add(_localIndexValue);
					}
				}
			}
			_localIndices.clear();
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
		final PairManager _pairMgr = new PairManager(false, true);

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
						CollectionsUtilities.putAllIntoSetInMap(defInfo, _pairMgr.getPair(_local, _stmt), _cache);
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
	 * @param stmt2localIndices maps a stmt to the set of local indices
	 *
	 * @return a collection of definition statements.
	 *
	 * @pre local2defs != null and listOfLocals != null and stmt2localIndices != null
	 * @pre listOfLocals.oclIsKindOf(List(Local))
	 * @post result != null and result.oclIsKindOf(Collection(DefinitionStmt))
	 * @post stmt2localIndices.oclIsKindOf(Stmt, Collection(Integer))
	 */
	private Collection seedDefInfo(final BitSet[][] local2defs, final List listOfLocals, final Map stmt2localIndices) {
		final Collection _result = new ArrayList();
		final Collection _defStmts = new ArrayList();

		for (final Iterator _i = unitGraph.iterator(); _i.hasNext();) {
			final Stmt _stmt = (Stmt) _i.next();
			stmt2localIndices.put(_stmt, new HashSet());

			if (_stmt instanceof DefinitionStmt) {
				_defStmts.add(_stmt);
			}
		}

		for (final Iterator _i = _defStmts.iterator(); _i.hasNext();) {
			final Stmt _stmt = (Stmt) _i.next();
			final Value _value = ((DefinitionStmt) _stmt).getLeftOp();

			if (_value instanceof Local) {
				final int _localIndex = listOfLocals.indexOf(_value);
				final int _stmtIndex = stmtList.indexOf(_stmt);

				for (final Iterator _j = unitGraph.getSuccsOf(_stmt).iterator(); _j.hasNext();) {
					final Stmt _succ = (Stmt) _j.next();
					final int _succIndex = stmtList.indexOf(_succ);
					BitSet _temp = local2defs[_succIndex][_localIndex];

					if (_temp == null) {
						_temp = new BitSet();
						local2defs[_succIndex][_localIndex] = _temp;
					}
					_temp.set(_stmtIndex, true);
					_result.add(_succ);
					((Collection) stmt2localIndices.get(_succ)).add(new Integer(_localIndex));
				}
			}
		}
		return _result;
	}
}

// End of File
