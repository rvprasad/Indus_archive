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

package edu.ksu.cis.indus.staticanalyses.cfg;

import edu.ksu.cis.indus.common.collections.MapUtils;
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
 * This class provides use-def information for local variables in a method. The analysis is performed at statement level.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class LocalUseDefAnalysis
		implements IUseDefInfo<DefinitionStmt, Pair<Local, Stmt>> {

	/**
	 * A map from local and statement pair to a collection of def statement.
	 */
	private final Map<Pair<Local, Stmt>, Collection<DefinitionStmt>> defInfo = new HashMap<Pair<Local, Stmt>, Collection<DefinitionStmt>>();

	/**
	 * A list of statements in the given method.
	 */
	private List<Stmt> stmtList;

	/**
	 * The control flow graph used to calculate the use-def info.
	 */
	private UnitGraph unitGraph;

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
	public LocalUseDefAnalysis(final UnitGraph graph) {
		final Body _body = graph.getBody();
		final PatchingChain _stmts = _body.getUnits();
		final BitSet[][] _l2defs = new BitSet[_stmts.size()][_body.getLocalCount()];
		stmtList = new ArrayList<Stmt>();
		stmtList.addAll(_stmts);

		final List<Local> _listOfLocals = new ArrayList<Local>();
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
	 * {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.interfaces.IStatus#isStable()
	 */
	public boolean isStable() {
		return true;
	}

	/**
	 * Performs the analysis to calculate the info.
	 * 
	 * @param local2defs maps a statment to a collection of def sets of each locals in the method.
	 * @param listOfLocals is a list of the locals.
	 * @pre local2defs != null and listOfLocals != null
	 */
	private void analyze(final BitSet[][] local2defs, final List<Local> listOfLocals) {
		final IWorkBag<Stmt> _wb = new FIFOWorkBag<Stmt>();
		final BitSet _temp = new BitSet();
		final Map<Stmt, Collection<Integer>> _stmt2localIndices = new HashMap<Stmt, Collection<Integer>>();

		_wb.addAllWork(seedDefInfo(local2defs, listOfLocals, _stmt2localIndices));

		while (_wb.hasWork()) {
			final Stmt _stmt = _wb.getWork();
			final BitSet[] _defsAtStmt = local2defs[stmtList.indexOf(_stmt)];
			final Collection<Integer> _localIndices = _stmt2localIndices.get(_stmt);

			// if the current statement is a def stmt then remove the index of the local being killed via definition
			if (_stmt instanceof DefinitionStmt) {
				final Value _leftOp = ((DefinitionStmt) _stmt).getLeftOp();

				if (_leftOp instanceof Local) {
					_localIndices.remove(new Integer(listOfLocals.indexOf(_leftOp)));
				}
			}

			// propagate the local defs to the successors
			for (final Iterator<Integer> _i = _localIndices.iterator(); _i.hasNext();) {
				final Integer _localIndexValue = _i.next();
				final int _localIndex = _localIndexValue.intValue();
				final BitSet _defsOfLocalAtStmt = _defsAtStmt[_localIndex];

				for (final Iterator<Stmt> _j = unitGraph.getSuccsOf(_stmt).iterator(); _j.hasNext();) {
					final Stmt _succ = _j.next();
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
						_stmt2localIndices.get(_succ).add(_localIndexValue);
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
	 * @pre local2defs != null and listOfLocals != null
	 */
	private void extract(final BitSet[][] local2defs, final List<Local> localList) {
		final PairManager _pairMgr = new PairManager(false, true);

		for (final Iterator<Stmt> _i = unitGraph.iterator(); _i.hasNext();) {
			final Stmt _stmt = _i.next();
			final int _stmtIndex = stmtList.indexOf(_stmt);
			final BitSet[] _stmtDefSet = local2defs[_stmtIndex];

			for (final Iterator<ValueBox> _j = _stmt.getUseBoxes().iterator(); _j.hasNext();) {
				final ValueBox _ub = _j.next();

				if (_ub.getValue() instanceof Local) {
					final Local _local = (Local) _ub.getValue();
					final Pair<Local, Stmt> _pair = _pairMgr.getPair(_local, _stmt);
					final int _lindex = localList.indexOf(_local);
					final BitSet _defs = _stmtDefSet[_lindex];

					if (_defs != null && !_defs.isEmpty()) {

						for (int _k = _defs.nextSetBit(0); _k >= 0; _k = _defs.nextSetBit(_k + 1)) {
							final DefinitionStmt _defStmt = (DefinitionStmt) stmtList.get(_k);
							final Local _l = (Local) _defStmt.getLeftOp();
							if (_l == _local) {
								MapUtils.putIntoCollectionInMap(useInfo, _defStmt, _pair);
								MapUtils.putIntoCollectionInMap(defInfo, _pair, _defStmt);
							}
						}
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
	 * @return a collection of definition statements.
	 * @pre local2defs != null and listOfLocals != null and stmt2localIndices != null
	 * @post result != null
	 */
	private Collection<Stmt> seedDefInfo(final BitSet[][] local2defs, final List<Local> listOfLocals,
			final Map<Stmt, Collection<Integer>> stmt2localIndices) {
		final Collection<Stmt> _result = new ArrayList<Stmt>();
		final Collection<DefinitionStmt> _defStmts = new ArrayList<DefinitionStmt>();

		for (final Iterator<Stmt> _i = unitGraph.iterator(); _i.hasNext();) {
			final Stmt _stmt = _i.next();
			stmt2localIndices.put(_stmt, new HashSet<Integer>());

			if (_stmt instanceof DefinitionStmt) {
				_defStmts.add((DefinitionStmt) _stmt);
			}
		}

		for (final Iterator<DefinitionStmt> _i = _defStmts.iterator(); _i.hasNext();) {
			final DefinitionStmt _stmt = _i.next();
			final Value _value = _stmt.getLeftOp();

			if (_value instanceof Local && listOfLocals.contains(_value)) {
				final int _localIndex = listOfLocals.indexOf(_value);
				final int _stmtIndex = stmtList.indexOf(_stmt);

				for (final Iterator<Stmt> _j = unitGraph.getSuccsOf(_stmt).iterator(); _j.hasNext();) {
					final Stmt _succ = _j.next();
					final int _succIndex = stmtList.indexOf(_succ);
					BitSet _temp = local2defs[_succIndex][_localIndex];

					if (_temp == null) {
						_temp = new BitSet();
						local2defs[_succIndex][_localIndex] = _temp;
					}
					_temp.set(_stmtIndex, true);
					_result.add(_succ);
					stmt2localIndices.get(_succ).add(new Integer(_localIndex));
				}
			}
		}
		return _result;
	}
}

// End of File
