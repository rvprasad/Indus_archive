
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

package edu.ksu.cis.indus.staticanalyses.concurrency.escape;

import soot.ArrayType;
import soot.Local;
import soot.Modifier;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Value;

import soot.jimple.AbstractJimpleValueSwitch;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.FieldRef;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.NewExpr;
import soot.jimple.NullConstant;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.ThisRef;
import soot.jimple.ThrowStmt;
import soot.jimple.VirtualInvokeExpr;

import soot.toolkits.graph.CompleteUnitGraph;

import edu.ksu.cis.indus.staticanalyses.Context;
import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.staticanalyses.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.staticanalyses.interfaces.IThreadGraphInfo;
import edu.ksu.cis.indus.staticanalyses.interfaces.IThreadGraphInfo.NewExprTriple;
import edu.ksu.cis.indus.staticanalyses.processing.AbstractProcessor;
import edu.ksu.cis.indus.staticanalyses.processing.ProcessingController;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraph;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraphMgr;
import edu.ksu.cis.indus.staticanalyses.support.FastUnionFindElement;
import edu.ksu.cis.indus.staticanalyses.support.LIFOWorkBag;
import edu.ksu.cis.indus.staticanalyses.support.SimpleNodeGraph;
import edu.ksu.cis.indus.staticanalyses.support.SimpleNodeGraph.SimpleNode;
import edu.ksu.cis.indus.staticanalyses.support.Triple;
import edu.ksu.cis.indus.staticanalyses.support.Util;
import edu.ksu.cis.indus.staticanalyses.support.WorkBag;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * This is an implementation of Ruf's escape analysis.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 *
 * @deprecated This is rather a comparison implementation which will not be supported.
 */
public class RufsEscapeAnalysis
  extends AbstractProcessor {
	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	static final String ARRAY_FIELD = "$ELT";

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	static final String THIS = "$THIS";

	/**
	 * <p>
	 * The logger used by instances of this class to log messages.
	 * </p>
	 */
	static final Log LOGGER = LogFactory.getLog(RufsEscapeAnalysis.class);

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	final Context context;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	final ICallGraphInfo cgi;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	final IThreadGraphInfo tgi;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	final Map globalASs;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	final Map methodCtxt2triple;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	final Scene scm;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	final StmtProcessor stmtProcessor;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	final ValueProcessor valueProcessor;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	Jimple jimple = Jimple.v();

	// Cache variables do not capture state of the object.  Rather they are used cache values across method calls.

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	Map localASsCache;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	Map scCache;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	MethodContext methodCtxtCache;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final BasicBlockGraphMgr bbm;

	/**
	 * DOCUMENT ME!
	 *
	 * @invariant threadAllocSitesSingle->forall(o | o.isOclKindOf(Triple(SootMethod, Stmt, NewExpr)))
	 */
	private final Collection threadAllocSitesMulti;

	/**
	 * DOCUMENT ME!
	 *
	 * @invariant threadAllocSitesSingle->forall(o | o.isOclKindOf(Triple(SootMethod, Stmt, NewExpr)))
	 */
	private final Collection threadAllocSitesSingle;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private CFGAnalysis cfg;

	/**
	 * Creates a new EquivalenceClassBasedEscapeAnalysis object.
	 *
	 * @param scm DOCUMENT ME!
	 * @param cgi DOCUMENT ME!
	 * @param tgi DOCUMENT ME!
	 */
	public RufsEscapeAnalysis(Scene scm, ICallGraphInfo cgi, IThreadGraphInfo tgi) {
		this.scm = scm;
		this.cgi = cgi;
		this.tgi = tgi;
		bbm = new BasicBlockGraphMgr();
		threadAllocSitesSingle = new HashSet();
		threadAllocSitesMulti = new HashSet();
		globalASs = new HashMap();
		methodCtxt2triple = new HashMap();
		stmtProcessor = new StmtProcessor();
		valueProcessor = new ValueProcessor();
		context = new Context();
		cfg = new CFGAnalysis(cgi, new BasicBlockGraphMgr());
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	class AliasSet
	  extends FastUnionFindElement
	  implements Cloneable {        
		/**
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		Map fieldMap;

		/**
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		private Collection syncThreads;

		/**
		 * <p>
		 * The logger used by instances of this class to log messages.
		 * </p>
		 */
		private final Log logger = LogFactory.getLog(AliasSet.class);

		/**
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		private Object theClone = null;

		/**
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		private boolean global;

		/**
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		private boolean propogating = false;

		/**
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		private boolean stringifying = false;

		/**
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		private boolean synced;

		/**
		 * Creates a new AliasSet object.
		 */
		AliasSet() {
			fieldMap = new HashMap();
			syncThreads = new HashSet();
			synced = false;
			global = false;
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 *
		 * @return DOCUMENT ME!
		 *
		 * @throws CloneNotSupportedException DOCUMENT ME!
		 */
		public Object clone()
		  throws CloneNotSupportedException {
			if (theClone != null) {
				return theClone;
			}

			if (isGlobal()) {
				return find();
			}

			if (set != null) {
				return (AliasSet) ((AliasSet) find()).clone();
			}

			AliasSet result = (AliasSet) super.clone();
			theClone = result;
			result.fieldMap = new HashMap();

			result.set = null;

			if (isSynced()) {
				result.setSynced();
			}

			AliasSet rep = (AliasSet) find();

			for (Iterator i = rep.fieldMap.entrySet().iterator(); i.hasNext();) {
				Map.Entry entry = (Map.Entry) i.next();
				AliasSet temp = (AliasSet) entry.getValue();

				if (!temp.isGlobal()) {
					temp = (AliasSet) temp.clone();
					temp.set = null;
				}
				result.fieldMap.put(entry.getKey(), temp);
			}
			theClone = null;
			result.syncThreads = new HashSet(rep.syncThreads);
			return result;
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 *
		 * @param field DOCUMENT ME!
		 *
		 * @return DOCUMENT ME!
		 */
		AliasSet getASForField(String field) {
			return (AliasSet) ((AliasSet) find()).fieldMap.get(field);
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 */
		void setGlobal() {
			if (isGlobal()) {
				return;
			}

			AliasSet rep = (AliasSet) find();
			rep.global = true;

			for (Iterator i = rep.fieldMap.values().iterator(); i.hasNext();) {
				AliasSet as = (AliasSet) i.next();
				as.setGlobal();
			}
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 *
		 * @return DOCUMENT ME!
		 */
		boolean isGlobal() {
			return ((AliasSet) find()).global;
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 */
		void setSynced() {
			AliasSet rep = (AliasSet) find();
			rep.synced = true;
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 *
		 * @return DOCUMENT ME!
		 */
		boolean isSynced() {
			return ((AliasSet) find()).synced;
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 *
		 * @param threads DOCUMENT ME!
		 */
		void addSyncThreads(Collection threads) {
			((AliasSet) find()).syncThreads.addAll(threads);
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 *
		 * @param as DOCUMENT ME!
		 */
		void propogateSyncInfoFromTo(AliasSet as) {
			if (propogating) {
				return;
			}

			AliasSet rep1 = (AliasSet) find();
			AliasSet rep2 = (AliasSet) as.find();

			if (rep2.isSynced()) {
				rep2.addSyncThreads(syncThreads);
			}
			propogating = true;

			for (Iterator i = rep2.fieldMap.entrySet().iterator(); i.hasNext();) {
				Map.Entry entry = (Map.Entry) i.next();
				AliasSet to = (AliasSet) ((AliasSet) rep2.fieldMap.get(entry.getKey())).find();

				if (to.isSynced()) {
					AliasSet from = (AliasSet) ((AliasSet) rep1.fieldMap.get(entry.getKey())).find();
					from.propogateSyncInfoFromTo(to);
				}
			}
			propogating = false;
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 *
		 * @param field DOCUMENT ME!
		 * @param as DOCUMENT ME!
		 */
		void putASForField(String field, AliasSet as) {
			((AliasSet) find()).fieldMap.put(field, as);

			if (isGlobal()) {
				as.setGlobal();
			}
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 *
		 * @param tabbing DOCUMENT ME!
		 * @param threadMap DOCUMENT ME!
		 *
		 * @return DOCUMENT ME!
		 */
		String toString(String tabbing, Map threadMap) {
			if (stringifying) {
				return tabbing + "Cycle from here on.";
			}
			stringifying = true;

			StringBuffer result = new StringBuffer();
			StringBuffer threads = new StringBuffer();
			AliasSet rep = (AliasSet) find();

			if (rep.syncThreads.isEmpty()) {
				threads.append("NONE");
			} else {
				for (Iterator i = rep.syncThreads.iterator(); i.hasNext();) {
					Object o = i.next();

					if (threadMap.get(o) == null) {
						threads.append(o + ",");
					} else {
						threads.append(threadMap.get(o) + ",");
					}
				}
			}

			StringBuffer fields = new StringBuffer();
			String newTabbing = tabbing + "  ";

			if (rep.fieldMap.isEmpty()) {
				fields.append(newTabbing + "NONE");
			} else {
				for (Iterator i = rep.fieldMap.entrySet().iterator(); i.hasNext();) {
					Map.Entry entry = (Map.Entry) i.next();
					fields.append(newTabbing + entry.getKey() + ":\n"
						+ ((AliasSet) entry.getValue()).toString(newTabbing + "    ", threadMap) + "\n");
				}
			}
			result.append(tabbing + "GLOBAL: " + rep.isGlobal() + "\n");
			result.append(tabbing + "SYNCHRONIZED: " + rep.isSynced() + "\n");
			result.append(tabbing + "SYNCHRONIZED THREADS: " + threads + "\n");
			result.append(tabbing + "FIELD MAPS:\n" + fields);
			stringifying = false;
			return result.toString();
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 *
		 * @param a DOCUMENT ME!
		 *
		 * @throws NullPointerException DOCUMENT ME!
		 */
		void unify(AliasSet a)
		  throws NullPointerException {
			if (a == null) {
				if (logger.isWarnEnabled()) {
					logger.warn("Unification with null requested.");
				}
				throw new NullPointerException("Trying to unify with null.");
			}

			boolean flag = false;

			if ((isGlobal() && !a.isGlobal() && a.isSynced()) || (a.isGlobal() && !isGlobal() && isSynced())) {
				flag = true;
			}

			AliasSet m = (AliasSet) find();
			AliasSet n = (AliasSet) a.find();

			if (m == n) {
				return;
			}
			m.union(n);

			AliasSet rep1 = (AliasSet) m.find();
			AliasSet rep2;

			if (rep1 == m) {
				rep2 = n;
			} else {
				rep2 = m;
			}
			rep1.synced = rep1.synced || rep2.synced;
			rep1.addSyncThreads(rep2.syncThreads);

			Collection toBeProcessed = new HashSet();
			toBeProcessed.addAll(rep2.fieldMap.keySet());
			Collection keySet = rep1.fieldMap.keySet();
			for (Iterator i = keySet.iterator(); i.hasNext();) {
				String field = (String) i.next();
				AliasSet repAS = (AliasSet) ((FastUnionFindElement) rep1.fieldMap.get(field)).find();
				toBeProcessed.remove(field);

				FastUnionFindElement temp = (FastUnionFindElement) rep2.fieldMap.get(field);

				if (temp != null) {
					repAS.unify((AliasSet) temp.find());
				}
			}

			for (Iterator i = toBeProcessed.iterator(); i.hasNext();) {
				String field = (String) i.next();
				AliasSet repAS = (AliasSet) ((FastUnionFindElement) rep2.fieldMap.get(field)).find();
				rep1.putASForField(field, repAS);
			}

			if (rep1.global || rep2.global) {
				rep1.setGlobal();
			}

			if (flag) {
				rep1.addSyncThreads(tgi.getExecutionThreads(context.getCurrentMethod()));
			}
		}
	}


	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	class MethodContext
	  extends FastUnionFindElement
	  implements Cloneable {
		/**
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		AliasSet ret;

		/**
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		AliasSet thisAS;

		/**
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		AliasSet thrown;

		/**
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		List argAlSets;

		/**
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		SootMethod method;

		/**
		 * Creates a new MethodContext object.
		 */
		MethodContext() {
		}

		/**
		 * Creates a new MethodContext object.
		 *
		 * @param sm DOCUMENT ME!
		 */
		MethodContext(SootMethod sm) {
			method = sm;
			argAlSets = new ArrayList(sm.getParameterCount());

			for (int i = 0; i < sm.getParameterCount(); i++) {
				Type type = sm.getParameterType(i);
				AliasSet t = null;

				if (type instanceof RefType || type instanceof ArrayType) {
					t = getASForType(type);
				}
				argAlSets.add(i, t);
			}

			if (sm.getReturnType() instanceof RefType || sm.getReturnType() instanceof ArrayType) {
				ret = new AliasSet();
			}
			thrown = new AliasSet();

			if (!sm.isStatic()) {
				thisAS = getASForClass(sm.getDeclaringClass());
			}
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 *
		 * @return DOCUMENT ME!
		 *
		 * @throws CloneNotSupportedException DOCUMENT ME!
		 */
		public Object clone()
		  throws CloneNotSupportedException {
			if (set != null) {
				return (MethodContext) ((MethodContext) find()).clone();
			}

			MethodContext result = (MethodContext) super.clone();

			Map clonee2clone = new HashMap();
			result.set = null;

			if (thisAS != null) {
				result.thisAS = (AliasSet) thisAS.clone();
				buildClonee2CloneMap(thisAS, result.thisAS, clonee2clone);
			}
			result.argAlSets = new ArrayList();

			for (Iterator i = argAlSets.iterator(); i.hasNext();) {
				AliasSet tmp = (AliasSet) i.next();

				if (tmp != null) {
					Object o = tmp.clone();
					result.argAlSets.add(o);
					buildClonee2CloneMap(tmp, (AliasSet) o, clonee2clone);
				} else {
					result.argAlSets.add(null);
				}
			}

			if (ret != null) {
				result.ret = (AliasSet) ret.clone();
				buildClonee2CloneMap(ret, result.ret, clonee2clone);
			}
			result.thrown = (AliasSet) thrown.clone();
			buildClonee2CloneMap(thrown, result.thrown, clonee2clone);
			unionclones(clonee2clone);

			return result;
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 *
		 * @param index DOCUMENT ME!
		 *
		 * @return DOCUMENT ME!
		 */
		AliasSet getParamAS(int index) {
			return (AliasSet) ((MethodContext) find()).argAlSets.get(index);
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 *
		 * @return DOCUMENT ME!
		 */
		AliasSet getReturnAS() {
			return (AliasSet) ((MethodContext) find()).ret;
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 *
		 * @return DOCUMENT ME!
		 */
		AliasSet getThisAS() {
			return (AliasSet) ((MethodContext) find()).thisAS;
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 *
		 * @return DOCUMENT ME!
		 */
		AliasSet getThrownAS() {
			return (AliasSet) ((MethodContext) find()).thrown;
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 *
		 * @param mc DOCUMENT ME!
		 */
		void propogateSyncInfoFromTo(MethodContext mc) {
			MethodContext rep1 = (MethodContext) find();
			MethodContext rep2 = (MethodContext) mc.find();

			for (int i = method.getParameterCount() - 1; i >= 0; i--) {
				AliasSet s = (AliasSet) rep1.argAlSets.get(i);

				if (s != null) {
					s.propogateSyncInfoFromTo((AliasSet) rep2.argAlSets.get(i));
				}
			}

			if (rep1.ret != null) {
				rep1.ret.propogateSyncInfoFromTo(rep2.ret);
			}
			thrown.propogateSyncInfoFromTo(mc.thrown);

			if (rep1.thisAS != null) {
				rep1.thisAS.propogateSyncInfoFromTo(rep2.thisAS);
			}
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 *
		 * @param p DOCUMENT ME!
		 *
		 * @throws IllegalStateException DOCUMENT ME!
		 */
		void unify(MethodContext p)
		  throws IllegalStateException {
			if (p == null) {
				return;
			}

			MethodContext m = (MethodContext) find();
			MethodContext n = (MethodContext) p.find();

			if (m == n) {
				return;
			}

			for (int i = method.getParameterCount() - 1; i >= 0; i--) {
				AliasSet s = (AliasSet) m.argAlSets.get(i);
				AliasSet t = (AliasSet) n.argAlSets.get(i);

				if (s != null && t == null || s == null && t != null) {
					throw new IllegalStateException("Incompatible method contexts being unified - argument position " + i
						+ " of " + method);
				}

				if (s != null) {
					s.unify((AliasSet) n.argAlSets.get(i));
				}
			}

			if ((m.ret == null && n.ret != null) || (m.ret != null && n.ret == null)) {
				throw new IllegalStateException("Incompatible method contexts being unified - return value of " + method);
			}

			if (m.ret != null) {
				m.ret.unify(n.ret);
			}
			m.thrown.unify(n.thrown);

			if ((m.thisAS == null && n.thisAS != null) || (m.thisAS != null && n.thisAS == null)) {
				throw new IllegalStateException("Incompatible method contexts being unified - staticness of " + method);
			}

			if (m.thisAS != null) {
				m.thisAS.unify(n.thisAS);
			}
			m.union(n);
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 *
		 * @param s DOCUMENT ME!
		 * @param d DOCUMENT ME!
		 * @param clonee2clone DOCUMENT ME!
		 */
		private void buildClonee2CloneMap(AliasSet s, AliasSet d, Map clonee2clone) {
			clonee2clone.put(s, d);

			AliasSet rep1 = (AliasSet) s.find();
			AliasSet rep2 = (AliasSet) d.find();

			for (Iterator i = rep1.fieldMap.keySet().iterator(); i.hasNext();) {
				Object o = i.next();
				AliasSet a = (AliasSet) rep1.fieldMap.get(o);
				AliasSet b = (AliasSet) rep2.fieldMap.get(o);

				if (!(clonee2clone.containsKey(a))) {
					buildClonee2CloneMap(a, b, clonee2clone);
				}
			}
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 *
		 * @param clonee2clone DOCUMENT ME!
		 */
		private void unionclones(Map clonee2clone) {
			Collection processed = new HashSet();

			for (Iterator i = clonee2clone.keySet().iterator(); i.hasNext();) {
				FastUnionFindElement k1 = (FastUnionFindElement) i.next();

				if (processed.contains(k1)) {
					continue;
				}

				for (Iterator j = clonee2clone.keySet().iterator(); j.hasNext();) {
					FastUnionFindElement k2 = (FastUnionFindElement) j.next();

					if (k1 == k2 || processed.contains(k2)) {
						continue;
					}

					if (k1.find() == k2.find()) {
						FastUnionFindElement v1 = (FastUnionFindElement) clonee2clone.get(k1);
						FastUnionFindElement v2 = (FastUnionFindElement) clonee2clone.get(k2);
						v1.find().union(v2.find());
					}
				}
				processed.add(k1);
			}
		}
	}


	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	class StmtProcessor
	  extends AbstractStmtSwitch {
		/*
		   void unifyy(AliasSet l, AliasSet r) {
		
		      boolean flag = false;
		      if ((l.isGlobal() && !r.isGlobal() && r.isSynced()) || (r.isGlobal() && !l.isGlobal() && l.isSynced()))
		          flag = true;
		      l.unify(r);
		      if (flag)
		           ((AliasSet) l.find()).addSyncThreads(tgi.getExecutionThreads(context.getCurrentMethod()));
		   }
		
		   void unify(AliasSet l, AliasSet r) {
		       l.unify(r);
		   }
		 */

		/**
		 * @see soot.jimple.StmtSwitch#caseAssignStmt(soot.jimple.AssignStmt)
		 */
		public void caseAssignStmt(AssignStmt stmt) {
			stmt.getRightOp().apply(valueProcessor);

			AliasSet r = (AliasSet) valueProcessor.getResult();
			stmt.getLeftOp().apply(valueProcessor);

			AliasSet l = (AliasSet) valueProcessor.getResult();

			if (r != null && l != null) {
				l.unify(r);
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseEnterMonitorStmt(soot.jimple.EnterMonitorStmt)
		 */
		public void caseEnterMonitorStmt(EnterMonitorStmt stmt) {
			stmt.getOp().apply(valueProcessor);

			AliasSet v = (AliasSet) valueProcessor.getResult();
			//v.setSynced();

			if (v.isGlobal()) {
				v.addSyncThreads(tgi.getExecutionThreads(context.getCurrentMethod()));
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseExitMonitorStmt(soot.jimple.ExitMonitorStmt)
		 */
		public void caseExitMonitorStmt(ExitMonitorStmt stmt) {
			stmt.getOp().apply(valueProcessor);

			AliasSet v = (AliasSet) valueProcessor.getResult();
			//v.setSynced();

			if (v.isGlobal()) {
				v.addSyncThreads(tgi.getExecutionThreads(context.getCurrentMethod()));
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseIdentityStmt(soot.jimple.IdentityStmt)
		 */
		public void caseIdentityStmt(IdentityStmt stmt) {
			Value v = stmt.getLeftOp();
			stmt.getRightOp().apply(valueProcessor);

			AliasSet r = (AliasSet) valueProcessor.getResult();
			v.apply(valueProcessor);

			AliasSet l = (AliasSet) valueProcessor.getResult();

			if (r != null && l != null) {
				l.unify(r);
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseInvokeStmt(soot.jimple.InvokeStmt)
		 */
		public void caseInvokeStmt(InvokeStmt stmt) {
			stmt.getInvokeExpr().apply(valueProcessor);
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseReturnStmt(soot.jimple.ReturnStmt)
		 */
		public void caseReturnStmt(ReturnStmt stmt) {
			Value v = stmt.getOp();
			v.apply(valueProcessor);

			AliasSet l = (AliasSet) valueProcessor.getResult();

			if (l != null) {
				methodCtxtCache.getReturnAS().unify(l);
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseThrowStmt(soot.jimple.ThrowStmt)
		 */
		public void caseThrowStmt(ThrowStmt stmt) {
			stmt.getOp().apply(valueProcessor);
			methodCtxtCache.getThrownAS().unify((AliasSet) valueProcessor.getResult());
		}
	}


	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	class ValueProcessor
	  extends AbstractJimpleValueSwitch {
		/**
		 * @see soot.jimple.RefSwitch#caseArrayRef(soot.jimple.ArrayRef)
		 */
		public void caseArrayRef(ArrayRef v) {
			AliasSet elt = null;

			if (v.getType() instanceof RefType || v.getType() instanceof ArrayType) {
				v.getBase().apply(this);

				AliasSet base = (AliasSet) getResult();
				elt = base.getASForField(ARRAY_FIELD);

				if (elt == null) {
					elt = new AliasSet();
					base.putASForField(ARRAY_FIELD, elt);
				}
                elt.setSynced();
			}
			setResult(elt);
		}

		/**
		 * @see soot.jimple.RefSwitch#caseInstanceFieldRef(soot.jimple.InstanceFieldRef)
		 */
		public void caseInstanceFieldRef(InstanceFieldRef v) {
			SootField sf = v.getField();
			AliasSet field = null;

			if (sf.getType() instanceof RefType || sf.getType() instanceof ArrayType) {
				v.getBase().apply(this);

				AliasSet base = (AliasSet) getResult();
				String fieldSig = v.getField().getSignature();
				field = base.getASForField(fieldSig);

				if (field == null) {
					field = new AliasSet();
					base.putASForField(fieldSig, field);
				}
                field.setSynced();
			}
			setResult(field);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseInterfaceInvokeExpr( soot.jimple.InterfaceInvokeExpr)
		 */
		public void caseInterfaceInvokeExpr(InterfaceInvokeExpr v) {
			processInvokeExpr(v);
		}

		/**
		 * @see soot.jimple.JimpleValueSwitch#caseLocal(soot.jimple.Local)
		 */
		public void caseLocal(Local v) {
			AliasSet s = null;

			if (v.getType() instanceof RefType || v.getType() instanceof ArrayType) {
				s = (AliasSet) localASsCache.get(v);

				if (s == null) {
					s = new AliasSet();
					localASsCache.put(v, s);
				}
                s.setSynced();
			}
			setResult(s);
		}

		/**
		 * @see soot.jimple.RefSwitch#caseParameterRef( soot.jimple.ParameterRef)
		 */
		public void caseParameterRef(ParameterRef v) {
			if (v.getType() instanceof RefType || v.getType() instanceof ArrayType) {
				setResult(methodCtxtCache.getParamAS(v.getIndex()));
			} else {
				setResult(null);
			}
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseSpecialInvokeExpr( soot.jimple.SpecialInvokeExpr)
		 */
		public void caseSpecialInvokeExpr(SpecialInvokeExpr v) {
			processInvokeExpr(v);
		}

		/**
		 * @see soot.jimple.RefSwitch#caseStaticFieldRef( soot.jimple.StaticFieldRef)
		 */
		public void caseStaticFieldRef(StaticFieldRef v) {
			if (v.getType() instanceof RefType || v.getType() instanceof ArrayType) {
				setResult(globalASs.get(v.getField().getSignature()));
			} else {
				setResult(null);
			}
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseStaticInvokeExpr( soot.jimple.StaticInvokeExpr)
		 */
		public void caseStaticInvokeExpr(StaticInvokeExpr v) {
			processInvokeExpr(v);
		}

		/**
		 * @see soot.jimple.RefSwitch#caseThisRef( soot.jimple.ThisRef)
		 */
		public void caseThisRef(ThisRef v) {
			setResult(methodCtxtCache.getThisAS());
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseVirtualInvokeExpr( soot.jimple.VirtualInvokeExpr)
		 */
		public void caseVirtualInvokeExpr(VirtualInvokeExpr v) {
			processInvokeExpr(v);
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 *
		 * @param o DOCUMENT ME!
		 */
		public void defaultCase(Object o) {
			setResult(null);
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 *
		 * @param m DOCUMENT ME!
		 * @param p DOCUMENT ME!
		 *
		 * @return DOCUMENT ME!
		 */
		private boolean notInSameSCC(SootMethod m, SootMethod p) {
			boolean result = true;
			Collection sccs = cgi.getSCCs();
			Collection scc = null;

			for (Iterator i = sccs.iterator(); i.hasNext();) {
				scc = (Collection) i.next();

				if (scc.contains(m)) {
					break;
				}
				scc = null;
			}

			if (scc != null) {
				result = !scc.contains(p);
			}
			return result;
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 *
		 * @param v DOCUMENT ME!
		 */
		private void processInvokeExpr(InvokeExpr v) {
			Collection callees = new ArrayList();
			SootMethod caller = context.getCurrentMethod();
			SootMethod sm = v.getMethod();

			// fix up "return" alias set.
			Stmt stmt = context.getStmt();
			AliasSet retAS =
				(sm.getReturnType() instanceof RefType || sm.getReturnType() instanceof ArrayType) ? new AliasSet()
																								   : null;

			if (stmt instanceof AssignStmt && ((AssignStmt) stmt).getLeftOp().getType() instanceof RefType) {
				((AssignStmt) stmt).getLeftOp().apply(valueProcessor);
				retAS = (AliasSet) valueProcessor.getResult();
			}

			// fix up "this" alias set.
			AliasSet thisAS = null;

			if (!sm.isStatic()) {
				((InstanceInvokeExpr) v).getBase().apply(valueProcessor);
				thisAS = (AliasSet) valueProcessor.getResult();
			}

			// fix up arg alias sets.
			List argASs = new ArrayList();

			for (int i = 0; i < sm.getParameterCount(); i++) {
				Value val = v.getArg(i);
				Object temp;

				/*
				 * We process the constant arguments here instead of the valueProcessor as we do not want to create
				 * AliasSets for each constant in the method.  Rather we just want to setup the method context depending on
				 * the type of the constants when they occur at an invocation site.
				 */
				if (val instanceof StringConstant || val instanceof NullConstant) {
					temp = new AliasSet();
				} else {
					v.getArg(i).apply(valueProcessor);
					temp = valueProcessor.getResult();
				}
				argASs.add(temp);
			}

			MethodContext sc = getSiteContext(sm, thisAS, argASs, retAS, new AliasSet());
			scCache.put(new CallTriple(caller, context.getStmt(), v), sc);

			if (v instanceof StaticInvokeExpr || v instanceof SpecialInvokeExpr) {
				callees.add(sm);
			} else if (v instanceof InterfaceInvokeExpr || v instanceof VirtualInvokeExpr) {
				context.setProgramPoint(((InstanceInvokeExpr) v).getBaseBox());
				callees.addAll(cgi.getCallees(v, context));
			}

			for (Iterator i = callees.iterator(); i.hasNext();) {
				SootMethod callee = (SootMethod) i.next();
				Triple triple = (Triple) methodCtxt2triple.get(callee);

				// This is needed when the system is not closed.
				if (triple == null) {
					continue;
				}

				MethodContext mc = (MethodContext) triple.getFirst();

				if (notInSameSCC(caller, callee)) {
					try {
						mc = (MethodContext) mc.clone();
					} catch (CloneNotSupportedException e) {
						LOGGER.error("Hell NO!  This should not happen.", e);
					}
				}
				sc.unify(mc);
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param sm DOCUMENT ME!
	 * @param l DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public boolean isGlobal(SootMethod sm, Local l) {
		Triple triple = (Triple) methodCtxt2triple.get(sm);
		boolean result = true;

		// This is needed in cases when the system is not closed.
		if (triple != null) {
			Map localASs = (Map) triple.getSecond();

			FastUnionFindElement s = (FastUnionFindElement) localASs.get(l);

			if (s != null) {
				result = ((AliasSet) s).isGlobal();
			} else {
				result = false;
			}
		}
		return result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.concurrency.escape.IEscapeAnalysis#isMethodEscaping( soot.jimple.NewExpr)
	 */
	public boolean isMethodEscaping(NewExpr allocSite) {
		return true;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.concurrency.escape.IEscapeAnalysis#isSingleThreadSynchronized( soot.jimple.Stmt)
	 */
	public boolean isSingleThreadSynchronized(Stmt stmt) {
		return false;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.concurrency.escape.IEscapeAnalysis#isThreadEscaping( soot.jimple.NewExpr)
	 */
	public boolean isThreadEscaping(NewExpr allocSite) {
		return false;
	}

	/**
	 * Invoked by the controller when a <code>Value</code> is encountered.
	 *
	 * @param value DOCUMENT ME!
	 * @param context DOCUMENT ME!
	 *
	 * @pre value.isOclKindOf(NewExpr)
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#callback( soot.jimple.Value,
	 * 		edu.ksu.cis.indus.staticanalyses.flow.Context)
	 */
	public void callback(Value value, Context context) {
		if (value instanceof NewExpr) {
			processNewExpr((NewExpr) value, context);
		}
	}

	/**
	 * Creates an alias set for the static fields.  This is the creation of  global alias sets in Ruf's algorithm.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#callback(SootField)
	 */
	public void callback(SootField sf) {
		if (Modifier.isStatic(sf.getModifiers())) {
			AliasSet t = getASForType(sf.getType());

			if (t != null) {
				t.setGlobal();
				globalASs.put(sf.getSignature(), t);
			}
		}
	}

	/**
	 * Creates a method context for <code>sm</code>.  This is the creation of method contexts in Ruf's algorithm.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#callback(SootMethod)
	 */
	public void callback(SootMethod sm) {
		methodCtxt2triple.put(sm, new Triple(new MethodContext(sm), new HashMap(), new HashMap()));
	}

	/**
	 * Performs phase1 (condition 2 and 3) operation here.  This should be called after the call graph information has been
	 * consolidated.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#consolidate()
	 */
	public void consolidate() {
		Collection tassBak = new HashSet(threadAllocSitesSingle);

		for (Iterator i = tassBak.iterator(); i.hasNext();) {
			NewExprTriple trp = (NewExprTriple) i.next();
			SootMethod encloser = trp.getMethod();

			if (executedMultipleTimes(encloser)) {
				threadAllocSitesSingle.remove(trp);
				threadAllocSitesMulti.add(trp);
			}
		}

		Context ctxt = new Context();
		Collection multiExecMethods = new HashSet();

		// This is for third condition of phase 1 in Ruf's algorithm, i.e., "thread allocation sites reachable from run 
		// methods associated with multiply executed thread allocation sites".
		// We just collect 
		for (Iterator i = threadAllocSitesMulti.iterator(); i.hasNext();) {
			NewExprTriple ntrp = (NewExprTriple) i.next();
			ctxt.setRootMethod(ntrp.getMethod());
			ctxt.setStmt(ntrp.getStmt());
			multiExecMethods.addAll(tgi.getExecutedMethods(ntrp.getExpr(), ctxt));
		}
		tassBak.clear();
		tassBak.addAll(threadAllocSitesSingle);

		for (Iterator i = tassBak.iterator(); i.hasNext();) {
			NewExprTriple trp = (NewExprTriple) i.next();
			SootMethod encloser = trp.getMethod();

			if (multiExecMethods.contains(encloser)) {
				threadAllocSitesSingle.remove(trp);
				threadAllocSitesMulti.add(trp);
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param sm DOCUMENT ME!
	 * @param l DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public boolean escapes(SootMethod sm, Local l) {
		Triple triple = (Triple) methodCtxt2triple.get(sm);
		boolean result = true;

		// This is needed in cases when the system is not closed.
		if (triple != null) {
			Map localASs = (Map) triple.getSecond();

			FastUnionFindElement s = (FastUnionFindElement) localASs.get(l);

			if (s != null) {
				result =
					((AliasSet) s).isSynced()
					  || !CollectionUtils.intersection(tgi.getMultiThreadAllocSites(), tgi.getExecutionThreads(sm)).isEmpty();
			} else {
				result = false;
			}
		}
		return result;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	public void execute() {
		SimpleNodeGraph sng = cgi.getCallGraph();

		// phase 2 of Ruf's algorithm
		Collection sccs = sng.getSCCs(false);

		for (Iterator i = sccs.iterator(); i.hasNext();) {
			List nodes = (List) i.next();

			for (Iterator j = nodes.iterator(); j.hasNext();) {
				SimpleNode node = (SimpleNode) j.next();
				SootMethod sm = (SootMethod) node._object;

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Processing method " + sm);
				}

				if (!sm.isConcrete()) {
					LOGGER.warn("Punting ?????????????");
					continue;
				}

				JimpleBody body = (JimpleBody) sm.retrieveActiveBody();
				Triple triple = (Triple) methodCtxt2triple.get(sm);

				methodCtxtCache = (MethodContext) triple.getFirst();
				localASsCache = (Map) triple.getSecond();
				scCache = (Map) triple.getThird();
				context.setRootMethod(sm);

				if (Modifier.isSynchronized(sm.getModifiers()) && !sm.isStatic()) {
					methodCtxtCache.getThisAS().setSynced();
				}

				for (Iterator k = body.getUnits().iterator(); k.hasNext();) {
					Stmt stmt = (Stmt) k.next();
					context.setStmt(stmt);
					stmt.apply(stmtProcessor);
				}
			}
		}

		WorkBag wb = new LIFOWorkBag();
		Collection processed = new HashSet();
		wb.addAllWork(cgi.getHeads());

		while (wb.hasWork()) {
			SootMethod caller = (SootMethod) wb.getWork();
			Collection callees = cgi.getCallees(caller);
			Triple triple = (Triple) methodCtxt2triple.get(caller);
			Map ctrp2sc = (Map) triple.getThird();

			for (Iterator i = callees.iterator(); i.hasNext();) {
				CallTriple ctrp = (CallTriple) i.next();
				SootMethod callee = ctrp.getMethod();
				triple = (Triple) methodCtxt2triple.get(callee);

				MethodContext mc = (MethodContext) (triple.getFirst());
				CallTriple callerTrp = new CallTriple(caller, ctrp.getStmt(), ctrp.getExpr());
				MethodContext sc = (MethodContext) ctrp2sc.get(callerTrp);
				sc.propogateSyncInfoFromTo(mc);

				if (!processed.contains(callee)) {
					processed.add(callee);
					wb.addWork(callee);
				}
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#hookup(
	 * 		edu.ksu.cis.indus.staticanalyses.flow.ProcessingController)
	 */
	public void hookup(ProcessingController ppc) {
		ppc.register(NewExpr.class, this);
		ppc.register(this);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#unhook(
	 * 		edu.ksu.cis.indus.staticanalyses.flow.ProcessingController)
	 */
	public void unhook(ProcessingController ppc) {
		ppc.unregister(NewExpr.class, this);
		ppc.unregister(this);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param sc DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	AliasSet getASForClass(SootClass sc) {
		AliasSet result = new AliasSet();

		for (Iterator i = sc.getFields().iterator(); i.hasNext();) {
			SootField sf = (SootField) i.next();

			if (Modifier.isStatic(sf.getModifiers())
				  || (!(sf.getType() instanceof RefType) || sf.getType() instanceof ArrayType)) {
				continue;
			}
			result.putASForField(sf.getSignature(), new AliasSet());
		}
		return result;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param type DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	AliasSet getASForType(Type type) {
		AliasSet result = null;

		if (type instanceof ArrayType) {
			ArrayType at = (ArrayType) type;
			AliasSet s = new AliasSet();
			result = new AliasSet();

			AliasSet st = result;

			for (int i = at.numDimensions; i >= 1; i--) {
				s = new AliasSet();
				st.putASForField(ARRAY_FIELD, s);
				st = s;
			}
		} else if (type instanceof RefType) {
			result = getASForClass(scm.getSootClass(((RefType) type).getClassName()));
		}
		return result;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param sm DOCUMENT ME!
	 * @param thisAS DOCUMENT ME!
	 * @param argASs DOCUMENT ME!
	 * @param retAS DOCUMENT ME!
	 * @param thrownAS DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	MethodContext getSiteContext(SootMethod sm, AliasSet thisAS, List argASs, AliasSet retAS, AliasSet thrownAS) {
		MethodContext result = new MethodContext();
		result.method = sm;
		result.thisAS = thisAS;
		result.argAlSets = argASs;
		result.ret = retAS;
		result.thrown = thrownAS;
		return result;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param sf DOCUMENT ME!
	 * @param threadMap DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	String tpgetInfo(SootField sf, Map threadMap) {
		FastUnionFindElement t = (FastUnionFindElement) globalASs.get(sf.getSignature());
		AliasSet s = (AliasSet) t;

		if (s != null) {
			return (((AliasSet) t).toString("  ", threadMap));
		} else {
			return "";
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param sm DOCUMENT ME!
	 * @param l DOCUMENT ME!
	 * @param threadMap DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	String tpgetInfo(SootMethod sm, Local l, Map threadMap) {
		Triple triple = (Triple) methodCtxt2triple.get(sm);

		// This is needed in cases when the system is not closed.
		if (triple == null) {
			return "";
		}

		Map localASs = (Map) triple.getSecond();

		FastUnionFindElement s = (FastUnionFindElement) localASs.get(l);

		if (s != null) {
			return ((AliasSet) s).toString("  ", threadMap);
		} else {
			return "";
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param sm DOCUMENT ME!
	 * @param threadMap DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	String tpgetInfo(SootMethod sm, Map threadMap) {
		StringBuffer result = new StringBuffer();
		Triple triple = (Triple) methodCtxt2triple.get(sm);

		if (triple != null) {
			MethodContext mc = (MethodContext) triple.getFirst();

			if (mc.getThisAS() != null) {
				result.append("  Info for @this:\n" + mc.getThisAS().toString("    ", threadMap) + "\n");
			}

			for (int i = 0; i < sm.getParameterCount(); i++) {
				AliasSet t = mc.getParamAS(i);

				if (t == null) {
					continue;
				}
				result.append("  Info for @parameter" + i + ":\n" + t.toString("    ", threadMap) + "\n");
			}

			if (sm.getReturnType() instanceof RefType || sm.getReturnType() instanceof ArrayType) {
				result.append("  Info for @return:\n" + mc.getReturnAS().toString("    ", threadMap) + "\n");
			}
		} else {
			LOGGER.warn("Method " + sm + " did not have a method context.");
		}
		return result.toString();
	}

	/**
	 * Captures the second condition in phase 1 of Ruf's algorithm, i.e., "thread allocation sites reachable from methods
	 * having multiple or multipy-executed call sites".
	 *
	 * @param caller is the method which leads to a thread allocation site.
	 *
	 * @return <code>true</code> if the given method or any of the methods in it's transitive caller closure have multiple or
	 * 		   multiply-executed call sites; <code>false</code>, otherwise.
	 */
	private boolean executedMultipleTimes(SootMethod caller) {
		boolean result = false;
		Collection callers = cgi.getCallers(caller);
main_control: 
		if (callers.size() > 1) {
			result = true;
		} else if (callers.size() == 1) {
			for (Iterator i = cgi.getSCCs().iterator(); i.hasNext();) {
				Collection scc = (Collection) i.next();

				if (scc.contains(caller)) {
					result = true;
					break main_control;
				}
			}

			CallTriple ctrp = (CallTriple) callers.iterator().next();
			SootMethod caller2 = ctrp.getMethod();
			BasicBlockGraph bbg = bbm.getBasicBlockGraph(new CompleteUnitGraph(caller2.retrieveActiveBody()));

			if (cfg.occursInCycle(bbg, bbg.getEnclosingBlock(ctrp.getStmt()))) {
				result = true;
			} else {
				result = executedMultipleTimes(caller2);
			}
		}
		return result;
	}

	/**
	 * This checks for the first condition of phase 1 in Ruf's algorithm, i.e., "thread allocation sites occuring in loops".
	 *
	 * @param ne is an allocation expression.
	 * @param context in which <code>ne</code> occurs.
	 */
	private void processNewExpr(NewExpr ne, Context context) {
		String classname = ne.getBaseType().getClassName();
		SootMethod sm = context.getCurrentMethod();

		if (Util.isDescendentOf(scm.getSootClass(classname), "java.lang.Thread")) {
			BasicBlockGraph bbg = bbm.getBasicBlockGraph(new CompleteUnitGraph(sm.retrieveActiveBody()));
			Stmt stmt = context.getStmt();

			if (bbg.isReachable(bbg.getEnclosingBlock(stmt), bbg.getEnclosingBlock(stmt), true)) {
				threadAllocSitesMulti.add(new NewExprTriple(sm, stmt, ne));
			} else {
				threadAllocSitesSingle.add(new NewExprTriple(sm, stmt, ne));
			}
		}
	}
    
    AliasSet getAliasSetFor(final Value v, final SootMethod sm) {
        Triple trp = (Triple) methodCtxt2triple.get(sm);

        if (trp == null) {
            throw new IllegalArgumentException("Method " + sm + " was not analyzed.");
        }

        Map local2AS = (Map) trp.getSecond();
        AliasSet result = null;

        if (canHaveAliasSet(v.getType())) {
            if (v instanceof InstanceFieldRef) {
                InstanceFieldRef i = (InstanceFieldRef) v;
                result = ((AliasSet) local2AS.get(i.getBase())).getASForField(((FieldRef) v).getField().getSignature());
            } else if (v instanceof StaticFieldRef) {
                result = (AliasSet) globalASs.get(((FieldRef) v).getField().getSignature());
            } else if (v instanceof ArrayRef) {
                ArrayRef a = (ArrayRef) v;
                result = ((AliasSet) local2AS.get(a.getBase())).getASForField(ARRAY_FIELD);
            } else if (v instanceof Local) {
                result = (AliasSet) local2AS.get(v);
            }
        }
        return result;
    }
    
    public boolean canHaveAliasSet(final Type type) {
        return type instanceof RefType || type instanceof ArrayType;
    }
    
    
}

/*
   ChangeLog:
   $Log$
   Revision 1.6  2003/10/31 01:02:04  venku
   - added code for extracting data for CC04 paper.

   Revision 1.5  2003/09/28 03:17:13  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.4  2003/09/08 02:23:24  venku
 *** empty log message ***
         Revision 1.3  2003/09/01 11:57:30  venku
         - Ripple effect of changes in CFGAnalysis.
         Revision 1.2  2003/08/24 12:42:33  venku
         Removed occursInCycle() method from DirectedGraph.
         Installed occursInCycle() method in CFGAnalysis.
         Converted performTopologicalsort() and getFinishTimes() into instance methods.
         Ripple effect of the above changes.
         Revision 1.1  2003/08/21 01:24:25  venku
          - Renamed src-escape to src-concurrency to as to group all concurrency
            issue related analyses into a package.
          - Renamed escape package to concurrency.escape.
          - Renamed EquivalenceClassBasedAnalysis to EquivalenceClassBasedEscapeAnalysis.
         Revision 1.2  2003/08/11 06:29:07  venku
         Changed format of change log accumulation at the end of the file
         Revision 1.1  2003/08/07 06:39:07  venku
         Major:
          - Moved the package under indus umbrella.
         Minor:
          - changes to accomodate ripple effect from support package.
         Revision 1.3  2003/07/30 08:30:31  venku
         Refactoring ripple.
         Also fixed a subtle bug in isShared() which caused wrong results.
         Revision 1.2  2003/07/27 21:22:14  venku
         Minor:
          - removed unnecessary casts.
         Revision 1.1  2003/07/27 20:52:39  venku
         First of the many refactoring while building towards slicer release.
         This is the escape analysis refactored and implemented as per to tech report.
 */
