
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

import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
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
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

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
	 * This provides the call graph of the system.
	 */
	private ICallGraphInfo cgi;

	/**
	 * This indicates if the analysis has stabilized.  If so, it is safe to query this object for information.
	 */
	private boolean stable;

	/**
	 * Creates a new AliasedUseDefInfo object.
	 *
	 * @param iva is the object flow analyzer to be used in the analysis.
	 * @param cg is the call graph of the system.
	 *
	 * @pre analyzer != null and cg != null
	 */
	public AliasedUseDefInfo(final IValueAnalyzer iva, final ICallGraphInfo cg) {
		use2defsMap = new HashMap();
		def2usesMap = new HashMap();
		analyzer = iva;
		cgi = cg;
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

		return _result == null ? Collections.EMPTY_SET
							   : _result;
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

		return _result == null ? Collections.EMPTY_SET
							   : _result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzerBasedProcessor#callback(Stmt, Context)
	 */
	public void callback(final Stmt stmt, final Context context) {
		final AssignStmt _as = (AssignStmt) stmt;
		final SootMethod _method = context.getCurrentMethod();

		if (_as.containsArrayRef() || _as.containsFieldRef()) {
			Value _ref = _as.getRightOp();

			if (_ref instanceof ArrayRef || _ref instanceof FieldRef) {
				Map _stmt2ddees = (Map) use2defsMap.get(_method);

				if (_stmt2ddees == null) {
					_stmt2ddees = new HashMap();
					use2defsMap.put(_method, _stmt2ddees);
				}
				_stmt2ddees.put(stmt, Collections.EMPTY_SET);
			} else {
				_ref = _as.getLeftOp();

				Map _stmt2ddents = (Map) def2usesMap.get(_method);

				if (_stmt2ddents == null) {
					_stmt2ddents = new HashMap();
					def2usesMap.put(_method, _stmt2ddents);
				}
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
			_contextDef.setRootMethod(_defMethod);

			for (final Iterator _j = ((Map) _method2defuses.getValue()).entrySet().iterator(); _j.hasNext();) {
				final Map.Entry _defStmt2useStmts = (Map.Entry) _j.next();
				final Stmt _defStmt = (Stmt) _defStmt2useStmts.getKey();

				/*
				 * Check if the use method and the def method are the same.  If so, use CFG reachability.  If not, use
				 * call graph reachability within the locality of a thread.
				 */
				final Collection _reachables = cgi.getMethodsReachableFrom(_defMethod);
				_reachables.add(_defMethod);
				_reachables.retainAll(use2defsMap.keySet());

				for (final Iterator _k = _reachables.iterator(); _k.hasNext();) {
					final SootMethod _useMethod = (SootMethod) _k.next();
					_contextUse.setRootMethod(_useMethod);

					for (final Iterator _l = ((Map) use2defsMap.get(_useMethod)).entrySet().iterator(); _l.hasNext();) {
						final Map.Entry _useStmt2defStmts = (Map.Entry) _l.next();
						final Stmt _useStmt = (Stmt) _useStmt2defStmts.getKey();

						// initially host no dependence
						boolean _useDef = false;

						if (_defStmt.containsArrayRef()
							  && _useStmt.containsArrayRef()
							  && _defStmt.getArrayRef().getType().equals(_useStmt.getArrayRef().getType())) {
							ValueBox _vBox = _useStmt.getArrayRef().getBaseBox();
							_contextUse.setStmt(_useStmt);
							_contextUse.setProgramPoint(_vBox);

							final Collection _c1 = analyzer.getValues(_vBox.getValue(), _contextUse);

							_vBox = _defStmt.getArrayRef().getBaseBox();
							_contextDef.setStmt(_defStmt);
							_contextDef.setProgramPoint(_vBox);

							final Collection _c2 = analyzer.getValues(_vBox.getValue(), _contextDef);

							// if the primaries of the access expression alias atleast one object
							_useDef = !CollectionUtils.intersection(_c1, _c2).isEmpty();
						} else if (_defStmt.containsFieldRef()
							  && _useStmt.containsFieldRef()
							  && _defStmt.getFieldRef().getField().equals(_useStmt.getFieldRef().getField())) {
							FieldRef _fr = _useStmt.getFieldRef();

							// set the initial value to true assuming dependency in case of static field ref
							_useDef = true;

							if (_fr instanceof InstanceFieldRef) {
								ValueBox _vBox = ((InstanceFieldRef) _useStmt.getFieldRef()).getBaseBox();
								_contextUse.setStmt(_useStmt);
								_contextUse.setProgramPoint(_vBox);

								final Collection _c1 = analyzer.getValues(_vBox.getValue(), _contextUse);

								_vBox = ((InstanceFieldRef) _defStmt.getFieldRef()).getBaseBox();
								_contextDef.setStmt(_defStmt);
								_contextDef.setProgramPoint(_vBox);

								final Collection _c2 = analyzer.getValues(_vBox.getValue(), _contextDef);

								// if the primaries of the access expression do not alias even one object.
								_useDef = CollectionUtils.intersection(_c1, _c2).isEmpty();
							}
						}

						if (_useDef) {
							if (_useMethod.equals(_defMethod)) {
								// TODO:
								// check if there is a path from _defStmt to _useStmt in the CFG of _useMethod.  
							}

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

				if (_uses.size() != 0) {
					_defStmt2useStmts.setValue(new HashSet(_uses));
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
}

/*
   ChangeLog:
   $Log$
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
