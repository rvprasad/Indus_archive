
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
	 * @see edu.ksu.cis.indus.interfaces.IIdentification#getId()
	 */
	public Object getId() {
		return IUseDefInfo.ALIASED_USE_DEF_ID;
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
			final Object _entity = _entry.getKey();
			final Map _defsite2usesites = (Map) _entry.getValue();

			final Map _usesite2defsites = (Map) use2defsMap.get(_entity);

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
			new StringBuffer("Statistics for Alised Use Def analysis as calculated by " + getClass().getName() + "\n");
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
	protected boolean isReachableViaInterProceduralFlow(final SootMethod defMethod, final Stmt defStmt,
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
				_result = isReachableViaInterProceduralFlow(_defMethod, _defStmt, _useMethod, _useStmt);
			}
		}
		return _result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.45  2004/08/08 08:50:03  venku
   - aspectized profiling/statistics logic.
   - used a cache in CallGraph for reachable methods.
   - required a pair manager in Call graph. Ripple effect.
   - used a first-element based lookup followed by pair search algorithm in PairManager.

   Revision 1.44  2004/08/02 07:33:45  venku
   - small but significant change to the pair manager.
   - ripple effect.
   Revision 1.43  2004/07/25 00:19:24  venku
   - result for class initializers was being overwritten in isReachableViaInterProceduralFlow().  FIXED.
   Revision 1.42  2004/07/22 09:42:40  venku
   - altered IUseDefInfo to use tighter types.
   - ripple effect.
   Revision 1.41  2004/07/21 11:36:26  venku
   - Extended IUseDefInfo interface to provide both local and non-local use def info.
   - ripple effect.
   - deleted ContainmentPredicate.  Instead, used CollectionUtils.containsAny() in
     ECBA and AliasedUseDefInfo analysis.
   - Added new faster implementation of LocalUseDefAnalysisv2
   - Used LocalUseDefAnalysisv2
   Revision 1.40  2004/07/17 23:32:18  venku
   - used Factory() pattern to populate values in maps and lists in CollectionsUtilities methods.
   - ripple effect.
   Revision 1.39  2004/07/17 19:35:46  venku
   - added a new predicate class that can be used to checking existential
     containment relation between two collections.
   - ripple effect.
   Revision 1.38  2004/07/16 06:38:47  venku
   - added  a more precise implementation of aliased use-def information.
   - ripple effect.
   Revision 1.37  2004/07/11 14:17:39  venku
   - added a new interface for identification purposes (IIdentification)
   - all classes that have an id implement this interface.
   Revision 1.36  2004/07/11 09:42:14  venku
   - Changed the way status information was handled the library.
     - Added class AbstractStatus to handle status related issues while
       the implementations just announce their status.
   Revision 1.35  2004/07/10 07:58:35  venku
   - used useMethod instead of useStmt when trying to determine
     ordering of statements in doesDefReachUse(). FIXED.
   Revision 1.34  2004/07/08 11:07:46  venku
   - INTERIM COMMIT.
   Revision 1.33  2004/06/28 22:44:26  venku
   - null was returned when no info was available. FIXED.
   Revision 1.32  2004/06/28 08:07:19  venku
   - documentation.
   - refactoring.
   Revision 1.31  2004/05/31 21:38:09  venku
   - moved BasicBlockGraph and BasicBlockGraphMgr from common.graph to common.soot.
   - ripple effect.
   Revision 1.30  2004/05/21 22:30:54  venku
   - documentation.
   Revision 1.29  2004/05/21 22:11:47  venku
   - renamed CollectionsModifier as CollectionUtilities.
   - added new specialized methods along with a method to extract
     filtered maps.
   - ripple effect.
   Revision 1.28  2004/05/21 10:25:47  venku
   - logic to determine if instance field references were related was incorrect.  FIXED.
   - refactoring.
   Revision 1.27  2004/03/03 05:59:33  venku
   - made aliased use-def info intraprocedural control flow reachability aware.
   Revision 1.26  2004/03/03 02:17:46  venku
   - added a new method to ICallGraphInfo interface.
   - implemented the above method in CallGraph.
   - made aliased use-def call-graph sensitive.
   Revision 1.25  2004/02/25 00:04:02  venku
   - documenation.
   Revision 1.24  2004/01/06 00:17:01  venku
   - Classes pertaining to workbag in package indus.graph were moved
     to indus.structures.
   - indus.structures was renamed to indus.datastructures.
   Revision 1.23  2003/12/13 02:29:08  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.22  2003/12/09 04:22:10  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.21  2003/12/08 12:20:44  venku
   - moved some classes from staticanalyses interface to indus interface package
   - ripple effect.
   Revision 1.20  2003/12/08 12:15:59  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.19  2003/12/08 09:45:04  venku
   - added reset method.
   Revision 1.18  2003/12/05 15:32:29  venku
   - coding convention.
   Revision 1.17  2003/12/05 02:19:01  venku
   - naming scheme was screwed up, hence, caused subtle bugs.  FIXED.
   Revision 1.16  2003/12/04 08:57:45  venku
   - added logic to attempt to provide information only if
     the query contains an array ref or field ref.
   Revision 1.15  2003/12/02 09:42:38  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.14  2003/12/01 13:33:53  venku
   - program point was not set before querying the analyzer. FIXED.
   Revision 1.13  2003/11/26 08:15:06  venku
   - incorrect map used in getDependees(). FIXED.
   Revision 1.12  2003/11/25 19:02:20  venku
   - bugs during info calculations.  FIXED.
   Revision 1.11  2003/11/12 03:51:12  venku
   - getDefs operates on statements and
     getUses operates on Def statements.
   Revision 1.10  2003/11/10 03:17:19  venku
   - renamed AbstractProcessor to AbstractValueAnalyzerBasedProcessor.
   - ripple effect.
   Revision 1.9  2003/11/06 05:15:07  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.8  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.7  2003/08/25 09:57:35  venku
   Exposed the constructor to the public.
   Revision 1.6  2003/08/21 03:43:56  venku
   Ripple effect of adding IStatus.
   Revision 1.5  2003/08/20 18:14:38  venku
   Log4j was used instead of logging.  That is fixed.
   Revision 1.4  2003/08/13 08:51:52  venku
   Fixed Checkstyle formatting errors.
   Revision 1.3  2003/08/13 08:49:10  venku
   Spruced up documentation and specification.
   Tightened preconditions in the interface such that they can be loosed later on in implementaions.
   Revision 1.2  2003/08/11 04:27:33  venku
   - Ripple effect of changes to Pair
   - Ripple effect of changes to _content in Marker
   - Changes of how thread start sites are tracked in ThreadGraphInfo
   Revision 1.1  2003/08/09 23:26:20  venku
   - Added an interface to provide use-def information.
   - Added an implementation to the above interface.
   - Extended call graph processor to retrieve call tree information rooted at arbitrary node.
   - Modified IValueAnalyzer interface such that only generic queries are possible.
     If required, this can be extended in the future.
 */
