
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors;

import edu.ksu.cis.indus.common.CollectionsUtilities;
import edu.ksu.cis.indus.common.Constants;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;

import edu.ksu.cis.indus.interfaces.IIdentification;
import edu.ksu.cis.indus.interfaces.IUseDefInfo;

import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.AbstractValueAnalyzerBasedProcessor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
 * is reachable from the def via the control flow graph or via the CFG, then def and use site are related by use-def
 * relation.  The only exception for this case is when the def occurs in the class initializer.   In this case, the defs can
 * reach almost all methods even if they are executed in a different thread from the use site.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class AliasedUseDefInfo
  extends AbstractValueAnalyzerBasedProcessor
  implements IUseDefInfo,
	  IIdentification {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(AliasedUseDefInfo.class);

	/** 
	 * The basic block graph manager to use during analysis.
	 */
	protected final BasicBlockGraphMgr bbgMgr;

	/** 
	 * The object flow analyzer to be used to calculate the UD info.
	 *
	 * @invariant analyzer.oclIsKindOf(OFAnalyzer)
	 */
	private final IValueAnalyzer analyzer;

	/** 
	 * This is a map from def-sites to their corresponding to use-sites.
	 *
	 * @invariant def2usesMap.oclIsKindOf(SootField, Map(Pair(Stmt, SootMethod), Collection(Pair(Stmt, SootMethod))))
	 */
	private final Map def2usesMap = new HashMap(Constants.getNumOfFieldsInApplication());

	/** 
	 * This is a map from use-sites to their corresponding to def-sites.
	 *
	 * @invariant use2defsMap.oclIsKindOf(SootField, Map(Pair(Stmt, SootMethod), Collection(Pair(Stmt, SootMethod))))
	 */
	private final Map use2defsMap = new HashMap(Constants.getNumOfFieldsInApplication());

	/** 
	 * This manages <code>Pair</code> objects.
	 */
	private final PairManager pairMgr;

	/**
	 * Creates a new AliasedUseDefInfo object.
	 *
	 * @param iva is the object flow analyzer to be used in the analysis.
	 * @param bbgManager is the basic block graph manager to use.
	 * @param pairManager to be used.
	 *
	 * @pre analyzer != null and cg != null and bbgMgr != null and pairManager != null
	 */
	public AliasedUseDefInfo(final IValueAnalyzer iva, final BasicBlockGraphMgr bbgManager, final PairManager pairManager) {
		analyzer = iva;
		bbgMgr = bbgManager;
		pairMgr = pairManager;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param method in which the use occurs.
	 *
	 * @pre context != null
	 */
	public Collection getDefs(final Stmt useStmt, final SootMethod method) {
		Collection _result = Collections.EMPTY_LIST;

		if (useStmt.containsArrayRef() || useStmt.containsFieldRef()) {
			final Object _key;

			if (useStmt.containsArrayRef()) {
				_key = useStmt.getArrayRef().getBase().getType();
			} else {
				_key = useStmt.getFieldRef().getField();
			}

			final Map _map = (Map) MapUtils.getObject(use2defsMap, _key);
			_result = (Collection) MapUtils.getObject(_map, pairMgr.getPair(useStmt, method), Collections.EMPTY_LIST);
		}
		return _result;
	}

	/**
	 * {@inheritDoc}<i>This operation is unsupported in this implementation.</i>
	 */
	public Collection getDefs(final Local local, final Stmt useStmt, final SootMethod method) {
		throw new UnsupportedOperationException("This opertation is not supported.");
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IIdentification#getIds()
	 */
	public Collection getIds() {
		return Collections.singleton(IUseDefInfo.ALIASED_USE_DEF_ID);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param method in which the definition occurs.
	 *
	 * @pre method != null
	 */
	public Collection getUses(final DefinitionStmt defStmt, final SootMethod method) {
		Collection _result = Collections.EMPTY_LIST;

		if (defStmt.containsArrayRef() || defStmt.containsFieldRef()) {
			final Object _key;

			if (defStmt.containsArrayRef()) {
				_key = defStmt.getArrayRef().getBase().getType();
			} else {
				_key = defStmt.getFieldRef().getField();
			}

			final Map _map = (Map) MapUtils.getObject(def2usesMap, _key);
			_result = (Collection) MapUtils.getObject(_map, pairMgr.getPair(defStmt, method), Collections.EMPTY_LIST);
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzerBasedProcessor#callback(Stmt, Context)
	 */
	public void callback(final Stmt stmt, final Context context) {
		final AssignStmt _as = (AssignStmt) stmt;

		if (_as.containsArrayRef() || _as.containsFieldRef()) {
			final Object _key;

			if (_as.containsArrayRef()) {
				_key = _as.getArrayRef().getBase().getType();
			} else {
				_key = _as.getFieldRef().getField();
			}

			final Value _ref = _as.getRightOp();
			final Map _key2info;

			if (_ref instanceof ArrayRef || _ref instanceof FieldRef) {
				_key2info = CollectionsUtilities.getMapFromMap(use2defsMap, _key);
			} else {
				_key2info = CollectionsUtilities.getMapFromMap(def2usesMap, _key);
			}
			_key2info.put(pairMgr.getPair(stmt, context.getCurrentMethod()), null);
		}
	}

	/**
	 * Records naive interprocedural data dependence.  All it does it records dependence between type conformant writes and
	 * reads.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzerBasedProcessor#consolidate()
	 */
	public void consolidate() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: consolidating");
		}

		final Collection _uses = new HashSet();

		for (final Iterator _i = def2usesMap.entrySet().iterator(); _i.hasNext();) {
			final Map.Entry _entry = (Map.Entry) _i.next();
			final Object _key = _entry.getKey();
			final Map _defsite2usesites = (Map) _entry.getValue();
			final Map _usesite2defsites = (Map) use2defsMap.get(_key);

			if (_usesite2defsites != null) {
				final Iterator _k = _defsite2usesites.keySet().iterator();
				final int _kEnd = _defsite2usesites.keySet().size();

				for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
					final Pair _defSite = (Pair) _k.next();
					final Iterator _l = _usesite2defsites.keySet().iterator();
					final int _lEnd = _usesite2defsites.keySet().size();

					for (int _lIndex = 0; _lIndex < _lEnd; _lIndex++) {
						final Pair _useSite = (Pair) _l.next();

						if (areDefUseRelated(_defSite, _useSite)) {
							/*
							 * Check if the use method and the def method are the same.  If so, use CFG reachability.
							 * If not, use call graph reachability within the locality of a thread.
							 */
							if (doesDefReachUse(_defSite, _useSite)) {
								CollectionsUtilities.putIntoSetInMap(_usesite2defsites, _useSite, _defSite);
								_uses.add(_useSite);
							}
						}
					}

					if (!_uses.isEmpty()) {
						CollectionsUtilities.putAllIntoSetInMap(_defsite2usesites, _defSite, _uses);
						_uses.clear();
					}
				}
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: consolidating");
		}
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
	public void reset() {
		unstable();
		def2usesMap.clear();
		use2defsMap.clear();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		final StringBuffer _result =
			new StringBuffer("Statistics for Aliased Use Def analysis as calculated by " + getClass().getName() + "\n");
		int _edgeCount = 0;

		final StringBuffer _temp = new StringBuffer();

		for (final Iterator _i = use2defsMap.entrySet().iterator(); _i.hasNext();) {
			final Map.Entry _entry = (Map.Entry) _i.next();
			final Object _entity = _entry.getKey();
			_result.append("For " + _entity + "\n ");

			for (final Iterator _k = ((Map) _entry.getValue()).entrySet().iterator(); _k.hasNext();) {
				final Map.Entry _entry1 = (Map.Entry) _k.next();
				final Object _use = _entry1.getKey();
				final Collection _defs = (Collection) _entry1.getValue();
				int _localEdgeCount = 0;

				if (_defs != null) {
					for (final Iterator _j = (_defs).iterator(); _j.hasNext();) {
						final Object _def = _j.next();
						_temp.append("\t\t" + _use + " <== " + _def + "\n");
					}
					_localEdgeCount += (_defs).size();
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
	 * Checks if the definition can reach the use site inter-procedurally.  This implementation assumes that the definition
	 * always reaches use site.
	 *
	 * @param defMethod is the context of definition. <i>ignored</i>
	 * @param defStmt is the definition statement. <i>ignored</i>.
	 * @param useMethod is the context of use. <i>ignored</i>.
	 * @param useStmt is the use statement. <i>ignored</i>.
	 *
	 * @return <code>true</code>
	 */
	protected boolean isReachableViaInterProceduralControlFlow(final SootMethod defMethod, final Stmt defStmt,
		final SootMethod useMethod, final Stmt useStmt) {
		return true;
	}

	/**
	 * Checks if the given definition and use are related.
	 *
	 * @param defSite is the definition site.
	 * @param useSite is the use site.
	 *
	 * @return <code>true</code> if the def and use site are related; <code>false</code>, otherwise.
	 *
	 * @pre defSite != null and useSite != null
	 * @pre defSite.oclIsKindOf(Pair(Stmt, SootMethod)) and useSite.oclIsKindOf(Pair(Stmt, SootMethod))
	 */
	private boolean areDefUseRelated(final Pair defSite, final Pair useSite) {
		boolean _result = false;
		final Context _context = new Context();
		final DefinitionStmt _defStmt = (DefinitionStmt) defSite.getFirst();
		final DefinitionStmt _useStmt = (DefinitionStmt) useSite.getFirst();
		final SootMethod _defMethod = (SootMethod) defSite.getSecond();
		final SootMethod _useMethod = (SootMethod) useSite.getSecond();

		if (_defStmt.containsArrayRef()) {
			final ValueBox _vBox1 = _useStmt.getArrayRef().getBaseBox();
			_context.setRootMethod(_useMethod);
			_context.setStmt(_useStmt);
			_context.setProgramPoint(_vBox1);

			final Collection _c1 = analyzer.getValues(_vBox1.getValue(), _context);
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

				final Collection _c1 = analyzer.getValues(_vBox1.getValue(), _context);
				final ValueBox _vBox2 = ((InstanceFieldRef) _defStmt.getFieldRef()).getBaseBox();
				_context.setRootMethod(_defMethod);
				_context.setStmt(_defStmt);
				_context.setProgramPoint(_vBox2);

				// if the primaries of the access expression alias atleast one object.
				_result = CollectionUtils.containsAny(_c1, analyzer.getValues(_vBox2.getValue(), _context));
			}
		}
		return _result;
	}

	/**
	 * Checks if the def reaches the use site.  If either of the methods are class initializers, <code>true</code> is
	 * returned.
	 *
	 * @param defSite is the definition site.
	 * @param useSite is the use site.
	 *
	 * @return <code>true</code> if the def and use site are related; <code>false</code>, otherwise.
	 *
	 * @pre defSite != null and useSite != null
	 * @pre defSite.oclIsKindOf(Pair(Stmt, SootMethod)) and useSite.oclIsKindOf(Pair(Stmt, SootMethod))
	 */
	private boolean doesDefReachUse(final Pair defSite, final Pair useSite) {
		boolean _result;
		final DefinitionStmt _defStmt = (DefinitionStmt) defSite.getFirst();
		final DefinitionStmt _useStmt = (DefinitionStmt) useSite.getFirst();
		final SootMethod _defMethod = (SootMethod) defSite.getSecond();
		final SootMethod _useMethod = (SootMethod) useSite.getSecond();

		if (_defMethod.getName().equals("<clinit>") || _useMethod.getName().equals("<clinit>")) {
			_result = true;
		} else {
			if (_useMethod.equals(_defMethod)) {
				final BasicBlockGraph _bbg = bbgMgr.getBasicBlockGraph(_useMethod);
				final BasicBlock _bbUse = _bbg.getEnclosingBlock(_useStmt);
				final BasicBlock _bbDef = _bbg.getEnclosingBlock(_defStmt);

				if (_bbUse == _bbDef) {
					final List _sl = _bbUse.getStmtsOf();
					_result = _sl.indexOf(_defStmt) < _sl.indexOf(_useStmt);
				} else {
					_result = _bbg.isReachable(_bbDef, _bbUse, true);
				}
			} else {
				_result = isReachableViaInterProceduralControlFlow(_defMethod, _defStmt, _useMethod, _useStmt);
			}
		}
		return _result;
	}
}

// End of File
