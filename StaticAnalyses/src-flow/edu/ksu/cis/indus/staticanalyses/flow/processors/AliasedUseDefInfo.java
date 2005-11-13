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

package edu.ksu.cis.indus.staticanalyses.flow.processors;

import edu.ksu.cis.indus.common.collections.CollectionUtils;
import edu.ksu.cis.indus.common.collections.MapUtils;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;
import edu.ksu.cis.indus.common.soot.Constants;

import edu.ksu.cis.indus.interfaces.IIdentification;
import edu.ksu.cis.indus.interfaces.IUseDefInfo;

import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.AbstractValueAnalyzerBasedProcessor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.ValueBox;

import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.Stmt;

/**
 * This class provides intra-thread aliased use-def information which is based on types and points-to information. If the use
 * is reachable from the def via the control flow graph or via the CFG, then def and use site are related by use-def relation.
 * The only exception for this case is when the def occurs in the class initializer. In this case, the defs can reach almost
 * all methods even if they are executed in a different thread from the use site.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class AliasedUseDefInfo
		extends AbstractValueAnalyzerBasedProcessor
		implements IUseDefInfo, IIdentification {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AliasedUseDefInfo.class);

	/**
	 * The basic block graph manager to use during analysis.
	 */
	protected final BasicBlockGraphMgr bbgMgr;

	/**
	 * This provide control flow analysis.
	 */
	protected final CFGAnalysis cfgAnalysis;

	/**
	 * The object flow analyzer to be used to calculate the UD info.
	 */
	private final IValueAnalyzer<Value> analyzer;

	/**
	 * This is a map from def-sites to their corresponding to use-sites.
	 * 
	 * @invariant def2usesMap.keySet()->forall(o | o.oclIsKindOf(SootField) or o.oclIsKindOf(Type))
	 */
	private final Map<Object, Map<Pair<DefinitionStmt, SootMethod>, Collection<Pair<DefinitionStmt, SootMethod>>>> def2usesMap;

	/**
	 * This manages <code>Pair</code> objects.
	 */
	private final PairManager pairMgr;

	/**
	 * This is a map from use-sites to their corresponding to def-sites.
	 * 
	 * @invariant use2defsMap.keySet()->forall(o | o.oclIsKindOf(SootField) or o.oclIsKindOf(Type))
	 */
	private final Map<Object, Map<Pair<DefinitionStmt, SootMethod>, Collection<Pair<DefinitionStmt, SootMethod>>>> use2defsMap;

	/**
	 * Creates a new AliasedUseDefInfo object.
	 * 
	 * @param iva is the object flow analyzer to be used in the analysis.
	 * @param bbgManager is the basic block graph manager to use.
	 * @param pairManager to be used.
	 * @param analysis to be used.
	 * @pre analyzer != null and cg != null and bbgMgr != null and pairManager != null
	 */
	public AliasedUseDefInfo(final IValueAnalyzer iva, final BasicBlockGraphMgr bbgManager, final PairManager pairManager,
			final CFGAnalysis analysis) {
		cfgAnalysis = analysis;
		analyzer = iva;
		bbgMgr = bbgManager;
		pairMgr = pairManager;
		def2usesMap = new HashMap<Object, Map<Pair<DefinitionStmt, SootMethod>, Collection<Pair<DefinitionStmt, SootMethod>>>>(
				Constants.getNumOfFieldsInApplication());
		use2defsMap = new HashMap<Object, Map<Pair<DefinitionStmt, SootMethod>, Collection<Pair<DefinitionStmt, SootMethod>>>>(
				Constants.getNumOfFieldsInApplication());
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzerBasedProcessor#callback(Stmt, Context)
	 */
	@Override public void callback(final Stmt stmt, final Context context) {
		final DefinitionStmt _as = (DefinitionStmt) stmt;

		if (_as.containsArrayRef() || _as.containsFieldRef()) {
			final Object _key;

			if (_as.containsArrayRef()) {
				_key = _as.getArrayRef().getBase().getType();
			} else {
				_key = _as.getFieldRef().getField();
			}

			final Value _ref = _as.getRightOp();
			final Map<Object, Map<Pair<DefinitionStmt, SootMethod>, Collection<Pair<DefinitionStmt, SootMethod>>>> _map;
			if (_ref instanceof ArrayRef || _ref instanceof FieldRef) {
				_map = use2defsMap;
			} else {
				_map = def2usesMap;
			}
			final Map<Pair<DefinitionStmt, SootMethod>, Collection<Pair<DefinitionStmt, SootMethod>>> _key2info = MapUtils
					.getMapFromMap(_map, _key);
			_key2info.put(pairMgr.getPair(_as, context.getCurrentMethod()), null);
		}
	}

	/**
	 * Records naive interprocedural data dependence. All it does it records dependence between type conformant writes and
	 * reads.
	 * 
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzerBasedProcessor#consolidate()
	 */
	@Override public void consolidate() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: consolidating");
		}

		final Collection<Pair<DefinitionStmt, SootMethod>> _uses = new HashSet<Pair<DefinitionStmt, SootMethod>>();

		for (final Iterator<Map.Entry<Object, Map<Pair<DefinitionStmt, SootMethod>, Collection<Pair<DefinitionStmt, SootMethod>>>>> _i = def2usesMap
				.entrySet().iterator(); _i.hasNext();) {
			final Map.Entry<Object, Map<Pair<DefinitionStmt, SootMethod>, Collection<Pair<DefinitionStmt, SootMethod>>>> _entry = _i
					.next();
			final Object _key = _entry.getKey();
			final Map<Pair<DefinitionStmt, SootMethod>, Collection<Pair<DefinitionStmt, SootMethod>>> _defsite2usesites = _entry
					.getValue();
			final Map<Pair<DefinitionStmt, SootMethod>, Collection<Pair<DefinitionStmt, SootMethod>>> _usesite2defsites = use2defsMap
					.get(_key);

			if (_usesite2defsites != null) {
				final Iterator<Pair<DefinitionStmt, SootMethod>> _k = _defsite2usesites.keySet().iterator();
				final int _kEnd = _defsite2usesites.keySet().size();

				for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
					final Pair<DefinitionStmt, SootMethod> _defSite = _k.next();
					final Iterator<Pair<DefinitionStmt, SootMethod>> _l = _usesite2defsites.keySet().iterator();
					final int _lEnd = _usesite2defsites.keySet().size();

					for (int _lIndex = 0; _lIndex < _lEnd; _lIndex++) {
						final Pair<DefinitionStmt, SootMethod> _useSite = _l.next();

						if (areDefUseRelated(_defSite, _useSite)) {
							/*
							 * Check if the use method and the def method are the same. If so, use CFG reachability. If not,
							 * use call graph reachability within the locality of a thread.
							 */
							if (doesDefReachUse(_defSite, _useSite)) {
								MapUtils.putIntoCollectionInMap(_usesite2defsites, _useSite, _defSite);
								_uses.add(_useSite);
							}
						}
					}

					if (!_uses.isEmpty()) {
						MapUtils.putAllIntoCollectionInMap(_defsite2usesites, _defSite, _uses);
						_uses.clear();
					}
				}
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: consolidating - " + toString());
		}
	}

	/**
	 * {@inheritDoc}<i>This operation is unsupported in this implementation.</i>
	 */
	public Collection<Object> getDefs(@SuppressWarnings("unused") final Local local,
			@SuppressWarnings("unused") final Stmt useStmt, @SuppressWarnings("unused") final SootMethod method) {
		throw new UnsupportedOperationException("This opertation is not supported.");
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param method in which the use occurs.
	 * @pre context != null
	 */
	public Collection<Pair<DefinitionStmt, SootMethod>> getDefs(final Stmt useStmt, final SootMethod method) {
		Collection<Pair<DefinitionStmt, SootMethod>> _result = Collections.emptyList();

		if (useStmt.containsArrayRef() || useStmt.containsFieldRef()) {
			final Object _key;

			if (useStmt.containsArrayRef()) {
				_key = useStmt.getArrayRef().getBase().getType();
			} else {
				_key = useStmt.getFieldRef().getField();
			}

			final Map<Pair<DefinitionStmt, SootMethod>, Collection<Pair<DefinitionStmt, SootMethod>>> _map = MapUtils
					.queryObject(use2defsMap, _key);
			_result = MapUtils.queryObject(_map, pairMgr.getPair((DefinitionStmt) useStmt, method), Collections.<Pair<DefinitionStmt, SootMethod>>emptySet());
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IIdentification#getIds()
	 */
	public Collection<Comparable> getIds() {
		return Collections.singleton(IUseDefInfo.ALIASED_USE_DEF_ID);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param method in which the definition occurs.
	 * @pre method != null
	 */
	public Collection<Pair<DefinitionStmt, SootMethod>> getUses(final DefinitionStmt defStmt, final SootMethod method) {
		Collection<Pair<DefinitionStmt, SootMethod>> _result = Collections.emptyList();

		if (defStmt.containsArrayRef() || defStmt.containsFieldRef()) {
			final Object _key;

			if (defStmt.containsArrayRef()) {
				_key = defStmt.getArrayRef().getBase().getType();
			} else {
				_key = defStmt.getFieldRef().getField();
			}

			final Map<Pair<DefinitionStmt, SootMethod>, Collection<Pair<DefinitionStmt, SootMethod>>> _map = MapUtils
					.queryObject(def2usesMap, _key);
			_result = MapUtils.queryObject(_map, pairMgr.getPair(defStmt, method), Collections.<Pair<DefinitionStmt, SootMethod>>emptySet());
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzerBasedProcessor#hookup(ProcessingController)
	 */
	public void hookup(final ProcessingController ppc) {
		unstable();
		ppc.register(AssignStmt.class, this);
	}

	/**
	 * Reset internal data structures.
	 */
	@Override public void reset() {
		unstable();
		def2usesMap.clear();
		use2defsMap.clear();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override public String toString() {
		final StringBuffer _result = new StringBuffer("Statistics for Aliased Use Def analysis as calculated by "
				+ getClass().getName() + "\n");
		int _edgeCount = 0;

		final StringBuilder _temp = new StringBuilder();

		for (final Iterator<Map.Entry<Object, Map<Pair<DefinitionStmt, SootMethod>, Collection<Pair<DefinitionStmt, SootMethod>>>>> _i = use2defsMap
				.entrySet().iterator(); _i.hasNext();) {
			final Map.Entry<Object, Map<Pair<DefinitionStmt, SootMethod>, Collection<Pair<DefinitionStmt, SootMethod>>>> _entry = _i
					.next();
			final Object _entity = _entry.getKey();
			_result.append("For " + _entity + "\n ");

			for (final Iterator<Map.Entry<Pair<DefinitionStmt, SootMethod>, Collection<Pair<DefinitionStmt, SootMethod>>>> _k = _entry
					.getValue().entrySet().iterator(); _k.hasNext();) {
				final Map.Entry<Pair<DefinitionStmt, SootMethod>, Collection<Pair<DefinitionStmt, SootMethod>>> _entry1 = _k
						.next();
				final Object _use = _entry1.getKey();
				final Collection _defs = _entry1.getValue();
				int _localEdgeCount = 0;

				if (_defs != null) {
					for (final Iterator _j = _defs.iterator(); _j.hasNext();) {
						final Object _def = _j.next();
						_temp.append("\t\t" + _use + " <== " + _def + "\n");
					}
					_localEdgeCount += _defs.size();
				}

				final Object _key = _entry1.getKey();
				_result.append("\tFor " + _key + "[");

				if (_key != null) {
					_result.append(_key.hashCode());
				} else {
					_result.append(0);
				}
				_result.append("] there are " + _localEdgeCount + " use-defs.\n");
				_result.append(_temp);
				_temp.delete(0, _temp.length());
				_edgeCount += _localEdgeCount;
			}
		}
		_result.append("A total of " + _edgeCount + " use-defs.");
		return _result.toString();
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzerBasedProcessor#unhook(ProcessingController)
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregister(AssignStmt.class, this);
		stable();
	}

	/**
	 * Checks if the definition can reach the use site inter-procedurally. This implementation assumes that the definition
	 * always reaches use site.
	 * 
	 * @param defMethod is the context of definition. <i>ignored</i>
	 * @param defStmt is the definition statement. <i>ignored</i>.
	 * @param useMethod is the context of use. <i>ignored</i>.
	 * @param useStmt is the use statement. <i>ignored</i>.
	 * @return <code>true</code>
	 */
	protected boolean isReachableViaInterProceduralControlFlow(@SuppressWarnings("unused") final SootMethod defMethod,
			@SuppressWarnings("unused") final Stmt defStmt, @SuppressWarnings("unused") final SootMethod useMethod,
			@SuppressWarnings("unused") final Stmt useStmt) {
		return true;
	}

	/**
	 * Checks if the given definition and use are related.
	 * 
	 * @param defSite is the definition site.
	 * @param useSite is the use site.
	 * @return <code>true</code> if the def and use site are related; <code>false</code>, otherwise.
	 * @pre defSite != null and useSite != null
	 * @pre defSite.oclIsKindOf(Pair(Stmt, SootMethod)) and useSite.oclIsKindOf(Pair(Stmt, SootMethod))
	 */
	private boolean areDefUseRelated(final Pair<DefinitionStmt, SootMethod> defSite,
			final Pair<DefinitionStmt, SootMethod> useSite) {
		boolean _result = false;
		final Context _context = new Context();
		final DefinitionStmt _defStmt = defSite.getFirst();
		final DefinitionStmt _useStmt = useSite.getFirst();
		final SootMethod _defMethod = defSite.getSecond();
		final SootMethod _useMethod = useSite.getSecond();

		if (_defStmt.containsArrayRef()) {
			final ValueBox _vBox1 = _useStmt.getArrayRef().getBaseBox();
			_context.setRootMethod(_useMethod);
			_context.setStmt(_useStmt);
			_context.setProgramPoint(_vBox1);

			final Collection<Value> _c1 = analyzer.getValues(_vBox1.getValue(), _context);
			final ValueBox _vBox2 = _defStmt.getArrayRef().getBaseBox();
			_context.setRootMethod(_defMethod);
			_context.setStmt(_defStmt);
			_context.setProgramPoint(_vBox2);

			// if the primaries of the access expression alias atleast one object
			_result = CollectionUtils.containsAny(_c1, analyzer.getValues(_vBox2.getValue(), _context));
		} else if (_defStmt.containsFieldRef()) {
			final FieldRef _fr = _useStmt.getFieldRef();

			// set the initial value to true assuming dependency in case of static field ref
			_result = true;

			if (_fr instanceof InstanceFieldRef) {
				final ValueBox _vBox1 = ((InstanceFieldRef) _useStmt.getFieldRef()).getBaseBox();
				_context.setRootMethod(_useMethod);
				_context.setStmt(_useStmt);
				_context.setProgramPoint(_vBox1);

				final Collection<Value> _c1 = analyzer.getValues(_vBox1.getValue(), _context);
				final ValueBox _vBox2 = ((InstanceFieldRef) _defStmt.getFieldRef()).getBaseBox();
				_context.setRootMethod(_defMethod);
				_context.setStmt(_defStmt);
				_context.setProgramPoint(_vBox2);

				// if the primaries of the access expression alias atleast one object.
				_result = CollectionUtils.containsAny(_c1, analyzer.getValues(_vBox2.getValue(), _context));
			}
		}		
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Def: " + defSite + " / Use: " + useSite + " --> related:" + _result);
		}
		return _result;
	}

	/**
	 * Checks if the def reaches the use site. If either of the methods are class initializers, <code>true</code> is
	 * returned.
	 * 
	 * @param defSite is the definition site.
	 * @param useSite is the use site.
	 * @return <code>true</code> if the def and use site are related; <code>false</code>, otherwise.
	 * @pre defSite != null and useSite != null
	 * @pre defSite.oclIsKindOf(Pair(Stmt, SootMethod)) and useSite.oclIsKindOf(Pair(Stmt, SootMethod))
	 */
	private boolean doesDefReachUse(final Pair<DefinitionStmt, SootMethod> defSite,
			final Pair<DefinitionStmt, SootMethod> useSite) {
		boolean _result;
		final DefinitionStmt _defStmt = defSite.getFirst();
		final DefinitionStmt _useStmt = useSite.getFirst();
		final SootMethod _defMethod = defSite.getSecond();
		final SootMethod _useMethod = useSite.getSecond();

		if (_defMethod.getName().equals("<clinit>") || _useMethod.getName().equals("<clinit>")) {
			_result = true;
		} else {
			boolean _r = false;
			if (_useMethod.equals(_defMethod)) {
				_r = cfgAnalysis.doesControlFlowPathExistsBetween(_defStmt, _useStmt, _useMethod);
			}
			_result = _r || isReachableViaInterProceduralControlFlow(_defMethod, _defStmt, _useMethod, _useStmt);
		}
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Def: " + defSite + " / Use: " + useSite + " --> related:" + _result);
		}
		return _result;
	}
}

// End of File
