
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
 * This is a naive implementation of aliased use-def analysis which calculates pessimistic may-be information.
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
     * 
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
	 * This indicates if the analysis has stabilized.  If so, it is safe to query this object for information.
	 */
	private boolean stable;

	/**
	 * Creates a new AliasedUseDefInfo object.
	 *
	 * @param iva is the object flow analyzer to be used in the analysis.
	 *
	 * @pre analyzer != null
	 */
	public AliasedUseDefInfo(final IValueAnalyzer iva) {
		use2defsMap = new HashMap();
		def2usesMap = new HashMap();
		analyzer = iva;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IUseDefInfo#getDefs(Stmt, Context)
	 */
	public Collection getDefs(final Stmt useStmt, final Context context) {
		Collection result = null;

		if (useStmt.containsArrayRef() || useStmt.containsFieldRef()) {
			Map stmt2defs = (Map) use2defsMap.get(context.getCurrentMethod());

			if (stmt2defs != null) {
				result = (Collection) stmt2defs.get(useStmt);
			}
		}

		return result == null ? Collections.EMPTY_SET
							  : result;
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
		Collection result = null;

		if (defStmt.containsArrayRef() || defStmt.containsFieldRef()) {
			Map stmt2uses = (Map) def2usesMap.get(context.getCurrentMethod());

			if (stmt2uses != null) {
				result = (Collection) stmt2uses.get(defStmt);
			}
		}

		return result == null ? Collections.EMPTY_SET
							  : result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzerBasedProcessor#callback(Stmt, Context)
	 */
	public void callback(final Stmt stmt, final Context context) {
		AssignStmt as = (AssignStmt) stmt;
		SootMethod method = context.getCurrentMethod();

		if (as.containsArrayRef() || as.containsFieldRef()) {
			Value ref = as.getRightOp();

			if (ref instanceof ArrayRef || ref instanceof FieldRef) {
				Map stmt2ddees = (Map) use2defsMap.get(method);

				if (stmt2ddees == null) {
					stmt2ddees = new HashMap();
					use2defsMap.put(method, stmt2ddees);
				}
				stmt2ddees.put(stmt, Collections.EMPTY_SET);
			} else {
				ref = as.getLeftOp();

				Map stmt2ddents = (Map) def2usesMap.get(method);

				if (stmt2ddents == null) {
					stmt2ddents = new HashMap();
					def2usesMap.put(method, stmt2ddents);
				}
				stmt2ddents.put(stmt, Collections.EMPTY_SET);
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

		Collection uses = new HashSet();
		Context contextDef = new Context();
		Context contextUse = new Context();

		for (Iterator i = def2usesMap.entrySet().iterator(); i.hasNext();) {
			Map.Entry method2info1 = (Map.Entry) i.next();
			SootMethod defMethod = (SootMethod) method2info1.getKey();
			contextDef.setRootMethod(defMethod);

			for (Iterator j = ((Map) method2info1.getValue()).entrySet().iterator(); j.hasNext();) {
				Map.Entry defStmt2useStmts = (Map.Entry) j.next();
				Stmt defStmt = (Stmt) defStmt2useStmts.getKey();

				for (Iterator k = use2defsMap.entrySet().iterator(); k.hasNext();) {
					Map.Entry method2info2 = (Map.Entry) k.next();
					SootMethod useMethod = (SootMethod) method2info2.getKey();
					contextUse.setRootMethod(useMethod);

					for (Iterator l = ((Map) method2info2.getValue()).entrySet().iterator(); l.hasNext();) {
						Map.Entry useStmt2defStmts = (Map.Entry) l.next();
						Stmt useStmt = (Stmt) useStmt2defStmts.getKey();

						// initially host no dependence
						boolean useDef = false;

						if (defStmt.containsArrayRef()
							  && useStmt.containsArrayRef()
							  && defStmt.getArrayRef().getType().equals(useStmt.getArrayRef().getType())) {
							ValueBox vBox = useStmt.getArrayRef().getBaseBox();
							contextUse.setStmt(useStmt);
							contextUse.setProgramPoint(vBox);

							Collection c1 = analyzer.getValues(vBox.getValue(), contextUse);

							vBox = defStmt.getArrayRef().getBaseBox();
							contextDef.setStmt(defStmt);
							contextDef.setProgramPoint(vBox);

							Collection c2 = analyzer.getValues(vBox.getValue(), contextDef);

							// if the primaries of the access expression alias atleast one object
							if (!CollectionUtils.intersection(c1, c2).isEmpty()) {
								useDef = true;
							}
						} else if (defStmt.containsFieldRef()
							  && useStmt.containsFieldRef()
							  && defStmt.getFieldRef().getField().equals(useStmt.getFieldRef().getField())) {
							FieldRef fr = useStmt.getFieldRef();

							// set the initial value to true assuming dependency in case of static field ref
							useDef = true;

							if (fr instanceof InstanceFieldRef) {
								ValueBox vBox = ((InstanceFieldRef) useStmt.getFieldRef()).getBaseBox();
								contextUse.setStmt(useStmt);
								contextUse.setProgramPoint(vBox);

								Collection c1 = analyzer.getValues(vBox.getValue(), contextUse);

								vBox = ((InstanceFieldRef) defStmt.getFieldRef()).getBaseBox();
								contextDef.setStmt(defStmt);
								contextDef.setProgramPoint(vBox);

								Collection c2 = analyzer.getValues(vBox.getValue(), contextDef);

								// if the primaries of the access expression do not alias even one object.
								if (CollectionUtils.intersection(c1, c2).isEmpty()) {
									useDef = false;
								}
							}
						}

						if (useDef) {
							Collection defs = (Collection) useStmt2defStmts.getValue();

							if (defs.equals(Collections.EMPTY_SET)) {
								defs = new HashSet();
								useStmt2defStmts.setValue(defs);
							}
							defs.add(pairMgr.getOptimizedPair(defStmt, defMethod));
							uses.add(pairMgr.getOptimizedPair(useStmt, useMethod));
						}
					}
				}

				if (uses.size() != 0) {
					defStmt2useStmts.setValue(new HashSet(uses));
					uses.clear();
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
