
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

import edu.ksu.cis.indus.common.CollectionsModifier;
import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.graph.BasicBlockGraph;
import edu.ksu.cis.indus.common.graph.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.common.graph.BasicBlockGraphMgr;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IUseDefInfo;

import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.AbstractValueAnalyzerBasedProcessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.Predicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
 * This class provides aliased use-def inforamtion which is based on types, points-to information, and call graph. If the use
 * is reachable from the def via the control flow graph or via the CFG and the call graph,  then def and use site are
 * related by use-def relation.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public final class AliasedUseDefInfo
  extends AbstractValueAnalyzerBasedProcessor
  implements IUseDefInfo {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(AliasedUseDefInfo.class);

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final BasicBlockGraphMgr bbgMgr;

	/**
	 * This provides the call graph of the system.
	 */
	private final ICallGraphInfo cgi;

	/**
	 * The object flow analyzer to be used to calculate the UD info.
	 *
	 * @invariant analyzer.oclIsKindOf(OFAnalyzer)
	 */
	private final IValueAnalyzer analyzer;

	/**
	 * This is a map from def-sites to their corresponding to use-sites.
	 *
	 * @invariant usesMap != null
	 */
	private final Map def2usesMap;

	/**
	 * This is a map from use-sites to their corresponding to def-sites.
	 *
	 * @invariant defsMap != null
	 */
	private final Map use2defsMap;

	/**
	 * This manages <code>Pair</code> objects.
	 */
	private final PairManager pairMgr = new PairManager();

	/**
	 * This indicates if the analysis has stabilized.  If so, it is safe to query this object for information.
	 */
	private boolean stable;

	/**
	 * Creates a new AliasedUseDefInfo object.
	 *
	 * @param iva is the object flow analyzer to be used in the analysis.
	 * @param cg is the call graph of the system.
	 * @param bbgManager DOCUMENT ME!
	 *
	 * @pre analyzer != null and cg != null
	 */
	public AliasedUseDefInfo(final IValueAnalyzer iva, final ICallGraphInfo cg, final BasicBlockGraphMgr bbgManager) {
		use2defsMap = new HashMap();
		def2usesMap = new HashMap();
		analyzer = iva;
		cgi = cg;
		bbgMgr = bbgManager;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IUseDefInfo#getDefs(Stmt, Context)
	 */
	public Collection getDefs(final Stmt useStmt, final Context context) {
		Collection _result = null;

		if (useStmt.containsArrayRef() || useStmt.containsFieldRef()) {
			final Map _stmt2defs = (Map) use2defsMap.get(context.getCurrentMethod());

			if (_stmt2defs != null) {
				_result = (Collection) _stmt2defs.get(useStmt);
			}
		}

		if (_result == null) {
			_result = Collections.EMPTY_LIST;
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IStatus#isStable()
	 */
	public boolean isStable() {
		return stable;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IUseDefInfo#getUses(DefinitionStmt, Context)
	 */
	public Collection getUses(final DefinitionStmt defStmt, final Context context) {
		Collection _result = null;

		if (defStmt.containsArrayRef() || defStmt.containsFieldRef()) {
			Map _stmt2uses = (Map) def2usesMap.get(context.getCurrentMethod());

			if (_stmt2uses != null) {
				_result = (Collection) _stmt2uses.get(defStmt);
			}
		}

		if (_result == null) {
			_result = Collections.EMPTY_LIST;
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzerBasedProcessor#callback(Stmt, Context)
	 */
	public void callback(final Stmt stmt, final Context context) {
		final AssignStmt _as = (AssignStmt) stmt;
		final SootMethod _method = context.getCurrentMethod();

		if (_as.containsArrayRef() || _as.containsFieldRef()) {
			final Value _ref = _as.getRightOp();

			if (_ref instanceof ArrayRef || _ref instanceof FieldRef) {
				Map _stmt2ddees = (Map) CollectionsModifier.getFromMap(use2defsMap, _method, new HashMap());
				_stmt2ddees.put(stmt, Collections.EMPTY_SET);
			} else {
				Map _stmt2ddents = (Map) CollectionsModifier.getFromMap(def2usesMap, _method, new HashMap());
				_stmt2ddents.put(stmt, Collections.EMPTY_SET);
			}
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
		final Context _contextDef = new Context();
		final Context _contextUse = new Context();

		for (final Iterator _i = def2usesMap.entrySet().iterator(); _i.hasNext();) {
			final Map.Entry _method2defuses = (Map.Entry) _i.next();
			final SootMethod _defMethod = (SootMethod) _method2defuses.getKey();
			final Collection _reachables = cgi.getMethodsReachableFrom(_defMethod);
			_reachables.add(_defMethod);
			_contextDef.setRootMethod(_defMethod);

			// extract a map that contains information only for use sites in reachable methods.
			final Map _temp =
				MapUtils.predicatedMap(use2defsMap,
					new Predicate() {
						public boolean evaluate(final Object o) {
							return _reachables.contains(o);
						}
					}, null);

			for (final Iterator _j = ((Map) _method2defuses.getValue()).entrySet().iterator(); _j.hasNext();) {
				final Map.Entry _defStmt2useStmts = (Map.Entry) _j.next();
				final Stmt _defStmt = (Stmt) _defStmt2useStmts.getKey();

				// in each reachable method, determine if the definition affects a use site.
				for (final Iterator _k = _temp.entrySet().iterator(); _k.hasNext();) {
					final Map.Entry _entry = (Map.Entry) _k.next();
					final SootMethod _useMethod = (SootMethod) _entry.getKey();
					_contextUse.setRootMethod(_useMethod);

					for (final Iterator _l = ((Map) _entry.getValue()).entrySet().iterator(); _l.hasNext();) {
						final Map.Entry _useStmt2defStmts = (Map.Entry) _l.next();
						final Stmt _useStmt = (Stmt) _useStmt2defStmts.getKey();

						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("consolidate() - Processing the following for use def info. - _useStmt = "
								+ _useStmt + ", _useMethod = " + _useMethod + ", _defStmt = " + _defStmt + ", _defMethod = "
								+ _defMethod);
						}

						boolean _related = areDefUseRelated(_contextDef, _contextUse, _defStmt, _useStmt);

						if (_related) {
							/*
							 * Check if the use method and the def method are the same.  If so, use CFG reachability.
							 * If not, use call graph reachability within the locality of a thread.
							 */
							boolean _flag = _related;

							if (_useMethod.equals(_defMethod)) {
								final BasicBlockGraph _bbg = bbgMgr.getBasicBlockGraph(_useMethod);
								final BasicBlock _bbUse = _bbg.getEnclosingBlock(_useStmt);
								final BasicBlock _bbDef = _bbg.getEnclosingBlock(_defStmt);
								_flag = _bbUse == _bbDef || _bbg.isReachable(_bbDef, _bbUse, true);
							} else {
								// TODO: determine if the use method is reachable via a call-site in the 
								// def method that is reachable from the def site in the def method.  
								// If so, set _flag to true
							}

							if (_flag) {
								Collection _defs = (Collection) _useStmt2defStmts.getValue();

								if (_defs.equals(Collections.EMPTY_SET)) {
									_defs = new HashSet();
									_useStmt2defStmts.setValue(_defs);
								}
								_defs.add(pairMgr.getOptimizedPair(_defStmt, _defMethod));
								_uses.add(pairMgr.getOptimizedPair(_useStmt, _useMethod));
							}
						}
					}
				}

				if (!_uses.isEmpty()) {
					_defStmt2useStmts.setValue(new ArrayList(_uses));
					_uses.clear();
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
		stable = false;
		ppc.register(AssignStmt.class, this);
	}

	/**
	 * Reset internal data structures.
	 */
	public void reset() {
		def2usesMap.clear();
		use2defsMap.clear();
		pairMgr.reset();
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzerBasedProcessor#unhook(ProcessingController)
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregister(AssignStmt.class, this);
		stable = true;
	}

	/**
	 * Checks if the given definition and use are related.
	 *
	 * @param defContext is the context of definition.
	 * @param useContext is the context of use.
	 * @param defStmt is the definition statement.
	 * @param useStmt is the use statement.
	 *
	 * @return <code>true</code> if the def and use site are related; <code>false</code>, otherwise.
	 *
	 * @pre defContext != null and useContext != null and defStmt != null and useStmt != null
	 */
	private boolean areDefUseRelated(final Context defContext, final Context useContext, final Stmt defStmt,
		final Stmt useStmt) {
		boolean _result = false;

		if (defStmt.containsArrayRef()
			  && useStmt.containsArrayRef()
			  && defStmt.getArrayRef().getType().equals(useStmt.getArrayRef().getType())) {
			final ValueBox _vBox1 = useStmt.getArrayRef().getBaseBox();
			useContext.setStmt(useStmt);
			useContext.setProgramPoint(_vBox1);

			final Collection _c1 = analyzer.getValues(_vBox1.getValue(), useContext);

			final ValueBox _vBox2 = defStmt.getArrayRef().getBaseBox();
			defContext.setStmt(defStmt);
			defContext.setProgramPoint(_vBox2);

			final Collection _c2 = analyzer.getValues(_vBox2.getValue(), defContext);

			// if the primaries of the access expression alias atleast one object
			_result =
				CollectionUtils.exists(_c1,
					new Predicate() {
						public boolean evaluate(final Object o) {
							return _c2.contains(o);
						}
					});
		} else if (defStmt.containsFieldRef()
			  && useStmt.containsFieldRef()
			  && defStmt.getFieldRef().getField().equals(useStmt.getFieldRef().getField())) {
			final FieldRef _fr = useStmt.getFieldRef();

			// set the initial value to true assuming dependency in case of static field ref
			_result = true;

			if (_fr instanceof InstanceFieldRef) {
				ValueBox _vBox = ((InstanceFieldRef) useStmt.getFieldRef()).getBaseBox();
				useContext.setStmt(useStmt);
				useContext.setProgramPoint(_vBox);

				final Collection _c1 = analyzer.getValues(_vBox.getValue(), useContext);

				_vBox = ((InstanceFieldRef) defStmt.getFieldRef()).getBaseBox();
				defContext.setStmt(defStmt);
				defContext.setProgramPoint(_vBox);

				final Collection _c2 = analyzer.getValues(_vBox.getValue(), defContext);

				// if the primaries of the access expression alias atleast one object.
				_result =
					CollectionUtils.exists(_c1,
						new Predicate() {
							public boolean evaluate(final Object o) {
								return _c2.contains(o);
							}
						});
			}
		}
		return _result;
	}
}

/*
   ChangeLog:
   $Log$
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
