
/*
 * Bandera, a Java(TM) analysis and transformation toolkit
 * Copyright (C) 2002, 2003, 2004.
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://www.cis.ksu.edu/santos/bandera).
 * It is understood that any modification not identified as such is
 * not covered by the preceding statement.
 *
 * This work is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this toolkit; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * Java is a trademark of Sun Microsystems, Inc.
 *
 * To submit a bug report, send a comment, or get the latest news on
 * this project and other SAnToS projects, please visit the web-site
 *                http://www.cis.ksu.edu/santos/bandera
 */

package edu.ksu.cis.bandera.staticanalyses.escape;

import ca.mcgill.sable.soot.ArrayType;
import ca.mcgill.sable.soot.ClassFile;
import ca.mcgill.sable.soot.Modifier;
import ca.mcgill.sable.soot.RefType;
import ca.mcgill.sable.soot.SootClass;
import ca.mcgill.sable.soot.SootClassManager;
import ca.mcgill.sable.soot.SootField;
import ca.mcgill.sable.soot.SootMethod;
import ca.mcgill.sable.soot.Type;

import ca.mcgill.sable.soot.jimple.AbstractJimpleValueSwitch;
import ca.mcgill.sable.soot.jimple.AbstractStmtSwitch;
import ca.mcgill.sable.soot.jimple.ArrayRef;
import ca.mcgill.sable.soot.jimple.AssignStmt;
import ca.mcgill.sable.soot.jimple.CompleteStmtGraph;
import ca.mcgill.sable.soot.jimple.EnterMonitorStmt;
import ca.mcgill.sable.soot.jimple.ExitMonitorStmt;
import ca.mcgill.sable.soot.jimple.IdentityStmt;
import ca.mcgill.sable.soot.jimple.InstanceFieldRef;
import ca.mcgill.sable.soot.jimple.InterfaceInvokeExpr;
import ca.mcgill.sable.soot.jimple.InvokeExpr;
import ca.mcgill.sable.soot.jimple.InvokeStmt;
import ca.mcgill.sable.soot.jimple.Jimple;
import ca.mcgill.sable.soot.jimple.JimpleBody;
import ca.mcgill.sable.soot.jimple.Local;
import ca.mcgill.sable.soot.jimple.NewExpr;
import ca.mcgill.sable.soot.jimple.NonStaticInvokeExpr;
import ca.mcgill.sable.soot.jimple.ParameterRef;
import ca.mcgill.sable.soot.jimple.ReturnStmt;
import ca.mcgill.sable.soot.jimple.SpecialInvokeExpr;
import ca.mcgill.sable.soot.jimple.StaticFieldRef;
import ca.mcgill.sable.soot.jimple.StaticInvokeExpr;
import ca.mcgill.sable.soot.jimple.Stmt;
import ca.mcgill.sable.soot.jimple.StmtBody;
import ca.mcgill.sable.soot.jimple.ThisRef;
import ca.mcgill.sable.soot.jimple.ThrowStmt;
import ca.mcgill.sable.soot.jimple.Value;
import ca.mcgill.sable.soot.jimple.VirtualInvokeExpr;

import edu.ksu.cis.bandera.staticanalyses.flow.AbstractAnalyzer;
import edu.ksu.cis.bandera.staticanalyses.flow.Context;
import edu.ksu.cis.bandera.staticanalyses.flow.PostProcessingController;
import edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.postprocessors.AbstractPostProcessor;
import edu.ksu.cis.bandera.staticanalyses.flow.interfaces.CallGraphInfo;
import edu.ksu.cis.bandera.staticanalyses.flow.interfaces.CallGraphInfo.CallTriple;
import edu.ksu.cis.bandera.staticanalyses.flow.interfaces.ThreadGraphInfo;
import edu.ksu.cis.bandera.staticanalyses.flow.interfaces.ThreadGraphInfo.NewExprTriple;
import edu.ksu.cis.bandera.staticanalyses.support.BasicBlockGraph;
import edu.ksu.cis.bandera.staticanalyses.support.BasicBlockGraphMgr;
import edu.ksu.cis.bandera.staticanalyses.support.FastUnionFindElement;
import edu.ksu.cis.bandera.staticanalyses.support.SimpleNodeGraph;
import edu.ksu.cis.bandera.staticanalyses.support.SimpleNodeGraph.SimpleNode;
import edu.ksu.cis.bandera.staticanalyses.support.Triple;
import edu.ksu.cis.bandera.staticanalyses.support.Util;
import edu.ksu.cis.bandera.staticanalyses.support.WorkBag;

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
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class RufsEscapeAnalysis
  extends AbstractPostProcessor
  implements EscapeAnalysis {
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
	final CallGraphInfo cgi;

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
	final SootClassManager scm;

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
	final ThreadGraphInfo tgi;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	final ValueProcessor valueProcessor;

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
	 * @invariant threadAllocSitesSingle->forall(o | o.oclType = Triple(SootMethod, Stmt, NewExpr))
	 */
	private final Collection threadAllocSitesMulti;

	/**
	 * DOCUMENT ME!
	 *
	 * @invariant threadAllocSitesSingle->forall(o | o.oclType = Triple(SootMethod, Stmt, NewExpr))
	 */
	private final Collection threadAllocSitesSingle;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private OFAnalyzer ofa;

	/**
	 * Creates a new RufsEscapeAnalysis object.
	 *
	 * @param scm DOCUMENT ME!
	 * @param cgi DOCUMENT ME!
	 * @param tgi DOCUMENT ME!
	 */
	public RufsEscapeAnalysis(SootClassManager scm, CallGraphInfo cgi, ThreadGraphInfo tgi) {
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
		private final Log LOGGER = LogFactory.getLog(AliasSet.class);

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
			synced = global = false;
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
			if(theClone != null) {
				return theClone;
			}

			if(isGlobal()) {
				return find();
			}

			if(set != null) {
				return (AliasSet) ((AliasSet) find()).clone();
			}

			AliasSet result = (AliasSet) super.clone();
			result.fieldMap = new HashMap();

			if(!isGlobal()) {
				result.set = null;

				if(isSynced()) {
					result.setSynced();
				}
			}

			AliasSet rep = (AliasSet) find();
			theClone = result;

			for(Iterator i = rep.fieldMap.entrySet().iterator(); i.hasNext();) {
				Map.Entry entry = (Map.Entry) i.next();
				AliasSet temp = (AliasSet) ((AliasSet) entry.getValue()).clone();

				if(!isGlobal()) {
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
			if(isGlobal()) {
				return;
			}

			AliasSet rep = (AliasSet) find();
			rep.global = true;

			for(Iterator i = rep.fieldMap.values().iterator(); i.hasNext();) {
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
			if(propogating) {
				return;
			}

			AliasSet rep1 = (AliasSet) find();
			AliasSet rep2 = (AliasSet) as.find();

			if(rep2.isSynced()) {
				rep2.addSyncThreads(syncThreads);
			}
			propogating = true;

			for(Iterator i = rep2.fieldMap.entrySet().iterator(); i.hasNext();) {
				Map.Entry entry = (Map.Entry) i.next();
				AliasSet to = (AliasSet) ((AliasSet) rep2.fieldMap.get(entry.getKey())).find();

				if(to.isSynced()) {
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

			if(isGlobal()) {
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
			if(stringifying) {
				return tabbing + "Cycle from here on.";
			}
			stringifying = true;

			StringBuffer result = new StringBuffer();
			StringBuffer threads = new StringBuffer();
			AliasSet rep = (AliasSet) find();

			if(rep.syncThreads.isEmpty()) {
				threads.append("NONE");
			} else {
				for(Iterator i = rep.syncThreads.iterator(); i.hasNext();) {
					Object o = i.next();

					if(threadMap.get(o) == null) {
						threads.append(o + ",");
					} else {
						threads.append(threadMap.get(o) + ",");
					}
				}
			}

			StringBuffer fields = new StringBuffer();
			String newTabbing = tabbing + "  ";

			if(rep.fieldMap.isEmpty()) {
				fields.append(newTabbing + "NONE");
			} else {
				for(Iterator i = rep.fieldMap.entrySet().iterator(); i.hasNext();) {
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
		 */
		void unify(AliasSet a) {
			if(a == null) {
				LOGGER.warn("Unification with null requested.");
			}

			boolean flag = false;

			if((isGlobal() && !a.isGlobal() && a.isSynced()) || (a.isGlobal() && !isGlobal() && isSynced())) {
				flag = true;
			}

			AliasSet m = (AliasSet) find();
			AliasSet n = (AliasSet) a.find();

			if(m == n) {
				return;
			}
			m.union(n);

			AliasSet rep1 = (AliasSet) m.find();
			AliasSet rep2;

			if(rep1 == m) {
				rep2 = n;
			} else {
				rep2 = m;
			}
			rep1.synced = rep1.synced || rep2.synced;
			rep1.addSyncThreads(rep2.syncThreads);

			Collection toBeProcessed = new HashSet();
			toBeProcessed.addAll(rep2.fieldMap.keySet());

			for(Iterator i = rep1.fieldMap.keySet().iterator(); i.hasNext();) {
				String field = (String) i.next();
				AliasSet repAS = (AliasSet) ((FastUnionFindElement) rep1.fieldMap.get(field)).find();
				toBeProcessed.remove(field);

				FastUnionFindElement temp = (FastUnionFindElement) rep2.fieldMap.get(field);

				if(temp != null) {
					repAS.unify((AliasSet) temp.find());
				}
			}

			for(Iterator i = toBeProcessed.iterator(); i.hasNext();) {
				String field = (String) i.next();
				AliasSet rep2AS = (AliasSet) ((FastUnionFindElement) rep2.fieldMap.get(field)).find();
				rep1.putASForField(field, rep2AS);
			}

			if(rep1.global || rep2.global) {
				rep1.setGlobal();
			}

			if(flag) {
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

			for(int i = 0; i < sm.getParameterCount(); i++) {
				Type type = sm.getParameterType(i);
				AliasSet t = null;

				if(type instanceof RefType || type instanceof ArrayType) {
					t = getASForType(type);
				}
				argAlSets.add(i, t);
			}

			if(sm.getReturnType() instanceof RefType || sm.getReturnType() instanceof ArrayType) {
				ret = new AliasSet();
			}
			thrown = new AliasSet();

			if(!sm.isStatic()) {
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
			if(set != null) {
				return (MethodContext) ((MethodContext) find()).clone();
			}

			MethodContext result = (MethodContext) super.clone();
			Map clonee2clone = new HashMap();
			result.set = null;

			if(thisAS != null) {
				result.thisAS = (AliasSet) thisAS.clone();
				buildClonee2CloneMap(thisAS, result.thisAS, clonee2clone);
			}
			result.argAlSets = new ArrayList();

			for(Iterator i = argAlSets.iterator(); i.hasNext();) {
				AliasSet tmp = (AliasSet) i.next();

				if(tmp != null) {
					Object o = tmp.clone();
					result.argAlSets.add(o);
					buildClonee2CloneMap(tmp, (AliasSet) o, clonee2clone);
				} else {
					result.argAlSets.add(null);
				}
			}

			if(ret != null) {
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

			for(int i = method.getParameterCount() - 1; i >= 0; i--) {
				AliasSet s = (AliasSet) rep1.argAlSets.get(i);

				if(s != null) {
					s.propogateSyncInfoFromTo((AliasSet) rep2.argAlSets.get(i));
				}
			}

			if(rep1.ret != null) {
				rep1.ret.propogateSyncInfoFromTo(rep2.ret);
			}
			thrown.propogateSyncInfoFromTo(mc.thrown);

			if(rep1.thisAS != null) {
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
		void unify(MethodContext p) {
			if(p == null) {
				return;
			}

			MethodContext m = (MethodContext) find();
			MethodContext n = (MethodContext) p.find();

			if(m == n) {
				return;
			}

			for(int i = method.getParameterCount() - 1; i >= 0; i--) {
				AliasSet s = (AliasSet) m.argAlSets.get(i);

				if(s != null) {
					s.unify((AliasSet) n.argAlSets.get(i));
				}
			}

			if((m.ret == null && n.ret != null) || (m.ret != null && n.ret == null)) {
				throw new IllegalStateException("Incompatible method contexts being unified - return value.");
			}

			if(m.ret != null) {
				m.ret.unify(n.ret);
			}
			m.thrown.unify(n.thrown);

			if((m.thisAS == null && n.thisAS != null) || (m.thisAS != null && n.thisAS == null)) {
				throw new IllegalStateException("Incompatible method contexts being unified - staticness");
			}

			if(m.thisAS != null) {
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

			AliasSet rep = (AliasSet) s.find();

			for(Iterator i = rep.fieldMap.keySet().iterator(); i.hasNext();) {
				Object o = i.next();
				AliasSet a = (AliasSet) rep.fieldMap.get(o);
				AliasSet b = (AliasSet) d.fieldMap.get(o);

				if(!(clonee2clone.containsKey(a))) {
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

			for(Iterator i = clonee2clone.keySet().iterator(); i.hasNext();) {
				FastUnionFindElement k1 = (FastUnionFindElement) i.next();

				if(processed.contains(k1)) {
					continue;
				}

				for(Iterator j = clonee2clone.keySet().iterator(); j.hasNext();) {
					FastUnionFindElement k2 = (FastUnionFindElement) j.next();

					if(k1 == k2 || processed.contains(k2)) {
						continue;
					}

					if(k1.find() == k2.find()) {
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
		 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseAssignStmt(ca.mcgill.sable.soot.jimple.AssignStmt)
		 */
		public void caseAssignStmt(AssignStmt stmt) {
			stmt.getRightOp().apply(valueProcessor);

			AliasSet r = (AliasSet) valueProcessor.getResult();
			stmt.getLeftOp().apply(valueProcessor);

			AliasSet l = (AliasSet) valueProcessor.getResult();

			if(r != null && l != null) {
				l.unify(r);
			}
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseEnterMonitorStmt(ca.mcgill.sable.soot.jimple.EnterMonitorStmt)
		 */
		public void caseEnterMonitorStmt(EnterMonitorStmt stmt) {
			stmt.getOp().apply(valueProcessor);

			AliasSet v = (AliasSet) valueProcessor.getResult();
			v.setSynced();

			if(v.isGlobal()) {
				v.addSyncThreads(tgi.getExecutionThreads(context.getCurrentMethod()));
			}
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseExitMonitorStmt(ca.mcgill.sable.soot.jimple.ExitMonitorStmt)
		 */
		public void caseExitMonitorStmt(ExitMonitorStmt stmt) {
			stmt.getOp().apply(valueProcessor);

			AliasSet v = (AliasSet) valueProcessor.getResult();
			v.setSynced();

			if(v.isGlobal()) {
				v.addSyncThreads(tgi.getExecutionThreads(context.getCurrentMethod()));
			}
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseIdentityStmt(ca.mcgill.sable.soot.jimple.IdentityStmt)
		 */
		public void caseIdentityStmt(IdentityStmt stmt) {
			Value v = stmt.getLeftOp();
			stmt.getRightOp().apply(valueProcessor);

			AliasSet r = (AliasSet) valueProcessor.getResult();
			v.apply(valueProcessor);

			AliasSet l = (AliasSet) valueProcessor.getResult();

			if(r != null && l != null) {
				l.unify(r);
			}
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseInvokeStmt(ca.mcgill.sable.soot.jimple.InvokeStmt)
		 */
		public void caseInvokeStmt(InvokeStmt stmt) {
			stmt.getInvokeExpr().apply(valueProcessor);
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseReturnStmt(ca.mcgill.sable.soot.jimple.ReturnStmt)
		 */
		public void caseReturnStmt(ReturnStmt stmt) {
			Value v = stmt.getReturnValue();
			v.apply(valueProcessor);

			AliasSet l = (AliasSet) valueProcessor.getResult();

			if(l != null) {
				methodCtxtCache.getReturnAS().unify(l);
			}
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseThrowStmt(ca.mcgill.sable.soot.jimple.ThrowStmt)
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
		 * @see ca.mcgill.sable.soot.jimple.RefSwitch#caseArrayRef(ca.mcgill.sable.soot.jimple.ArrayRef)
		 */
		public void caseArrayRef(ArrayRef v) {
			AliasSet elt = null;

			if(v.getType() instanceof RefType || v.getType() instanceof ArrayType) {
				v.getBase().apply(this);

				AliasSet base = (AliasSet) getResult();
				elt = (AliasSet) base.getASForField(ARRAY_FIELD);

				if(elt == null) {
					elt = new AliasSet();
					base.putASForField(ARRAY_FIELD, elt);
				}
			}
			setResult(elt);
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.RefSwitch#caseInstanceFieldRef(ca.mcgill.sable.soot.jimple.InstanceFieldRef)
		 */
		public void caseInstanceFieldRef(InstanceFieldRef v) {
			SootField sf = v.getField();
			AliasSet field = null;

			if(sf.getType() instanceof RefType || sf.getType() instanceof ArrayType) {
				v.getBase().apply(this);

				AliasSet base = (AliasSet) getResult();
				String fieldSig = v.getField().getSignature();
				field = (AliasSet) base.getASForField(fieldSig);

				if(field == null) {
					field = new AliasSet();
					base.putASForField(fieldSig, field);
				}
			}
			setResult(field);
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.ExprSwitch#caseInterfaceInvokeExpr(ca.mcgill.sable.soot.jimple.InterfaceInvokeExpr)
		 */
		public void caseInterfaceInvokeExpr(InterfaceInvokeExpr v) {
			processInvokeExpr(v);
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.JimpleValueSwitch#caseLocal(ca.mcgill.sable.soot.jimple.Local)
		 */
		public void caseLocal(Local v) {
			AliasSet s = null;

			if(v.getType() instanceof RefType || v.getType() instanceof ArrayType) {
				s = (AliasSet) localASsCache.get(v);

				if(s == null) {
					s = new AliasSet();
					localASsCache.put(v, s);
				}
			}
			setResult(s);
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.RefSwitch#caseParameterRef(ca.mcgill.sable.soot.jimple.ParameterRef)
		 */
		public void caseParameterRef(ParameterRef v) {
			if(v.getType() instanceof RefType || v.getType() instanceof ArrayType) {
				setResult(methodCtxtCache.getParamAS(v.getIndex()));
			} else {
				setResult(null);
			}
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.ExprSwitch#caseSpecialInvokeExpr(ca.mcgill.sable.soot.jimple.SpecialInvokeExpr)
		 */
		public void caseSpecialInvokeExpr(SpecialInvokeExpr v) {
			processInvokeExpr(v);
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.RefSwitch#caseStaticFieldRef(ca.mcgill.sable.soot.jimple.StaticFieldRef)
		 */
		public void caseStaticFieldRef(StaticFieldRef v) {
			if(v.getType() instanceof RefType || v.getType() instanceof ArrayType) {
				setResult(globalASs.get(v.getField().getSignature()));
			} else {
				setResult(null);
			}
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.ExprSwitch#caseStaticInvokeExpr(ca.mcgill.sable.soot.jimple.StaticInvokeExpr)
		 */
		public void caseStaticInvokeExpr(StaticInvokeExpr v) {
			processInvokeExpr(v);
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.RefSwitch#caseThisRef(ca.mcgill.sable.soot.jimple.ThisRef)
		 */
		public void caseThisRef(ThisRef v) {
			setResult(methodCtxtCache.getThisAS());
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.ExprSwitch#caseVirtualInvokeExpr(ca.mcgill.sable.soot.jimple.VirtualInvokeExpr)
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

			for(Iterator i = sccs.iterator(); i.hasNext();) {
				scc = (Collection) i.next();

				if(scc.contains(m)) {
					break;
				}
				scc = null;
			}

			if(scc != null) {
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
			AliasSet retAS = (sm.getReturnType() instanceof RefType) ? new AliasSet() : null;

			if(stmt instanceof AssignStmt && ((AssignStmt) stmt).getLeftOp().getType() instanceof RefType) {
				((AssignStmt) stmt).getLeftOp().apply(valueProcessor);
				retAS = (AliasSet) valueProcessor.getResult();
			}

			// fix up "this" alias set.
			AliasSet thisAS = null;

			if(!sm.isStatic()) {
				((NonStaticInvokeExpr) v).getBase().apply(valueProcessor);
				thisAS = (AliasSet) valueProcessor.getResult();
			}

			// fix up arg alias sets.
			List argASs = new ArrayList();

			for(int i = 0; i < sm.getParameterCount(); i++) {
				v.getArg(i).apply(valueProcessor);
				argASs.add(valueProcessor.getResult());
			}

			MethodContext sc = getSiteContext(sm, thisAS, argASs, retAS, new AliasSet());
			scCache.put(new CallTriple(caller, context.getStmt(), v), sc);

			if(v instanceof StaticInvokeExpr || v instanceof SpecialInvokeExpr) {
				callees.add(sm);
			} else if(v instanceof InterfaceInvokeExpr || v instanceof VirtualInvokeExpr) {
				context.setProgramPoint(((NonStaticInvokeExpr) v).getBaseBox());

				Collection temp = cgi.getCallees(v, context);

				for(Iterator i = temp.iterator(); i.hasNext();) {
					callees.add((SootMethod) i.next());
				}
			}

			for(Iterator i = callees.iterator(); i.hasNext();) {
				SootMethod callee = (SootMethod) i.next();
				Triple triple = (Triple) methodCtxt2triple.get(callee);

				// This is needed when the system is not closed.
				if(triple == null) {
					continue;
				}

				MethodContext mc = (MethodContext) triple.getFirst();

				if(notInSameSCC(caller, callee)) {
					try {
						mc = (MethodContext) mc.clone();
					} catch(CloneNotSupportedException e) {
						LOGGER.error("Hell NO!  This should not happen.", e);
					}
				}
				sc.unify(mc);
			}
		}
	}

	/**
	 * Stores the reference to the <code>OFAnalyzer</code>.
	 *
	 * @param analyzer DOCUMENT ME!
	 *
	 * @pre analyzer.oclType = edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.OFAnalyzer
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.flow.interfaces.PostProcessor#setAnalyzer(edu.ksu.cis.bandera.staticanalyses.flow.AbstractAnalyzer)
	 */
	public void setAnalyzer(AbstractAnalyzer analyzer) {
		this.ofa = (OFAnalyzer) analyzer;
	}

	/**
	 * @see edu.ksu.cis.bandera.staticanalyses.escape.EscapeAnalysis#isMethodEscaping(ca.mcgill.sable.soot.jimple.NewExpr)
	 */
	public boolean isMethodEscaping(NewExpr allocSite) {
		return true;
	}

	/**
	 * @see edu.ksu.cis.bandera.staticanalyses.escape.EscapeAnalysis#isSingleThreadSynchronized(ca.mcgill.sable.soot.jimple.Stmt)
	 */
	public boolean isSingleThreadSynchronized(Stmt stmt) {
		return false;
	}

	/**
	 * @see edu.ksu.cis.bandera.staticanalyses.escape.EscapeAnalysis#isThreadEscaping(ca.mcgill.sable.soot.jimple.NewExpr)
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
	 * @pre value.oclType = NewExpr
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.flow.interfaces.PostProcessor#callback(ca.mcgill.sable.soot.jimple.Value,
	 * 		edu.ksu.cis.bandera.staticanalyses.flow.Context)
	 */
	public void callback(Value value, Context context) {
		if(value instanceof NewExpr) {
			processNewExpr((NewExpr) value, context);
		}
	}

	/**
	 * Creates an alias set for the static fields.  This is the creation of  global alias sets in Ruf's algorithm.
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.flow.interfaces.PostProcessor#callback(SootField)
	 */
	public void callback(SootField sf) {
		if(Modifier.isStatic(sf.getModifiers())) {
			AliasSet t;

			if(sf.getType() instanceof ArrayType) {
				t = getASForType(sf.getType());
			} else {
				t = new AliasSet();
			}
			t.setGlobal();
			globalASs.put(sf.getSignature(), t);
		}
	}

	/**
	 * Creates a method context for <code>sm</code>.  This is the creation of method contexts in Ruf's algorithm.
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.flow.interfaces.PostProcessor#callback(SootMethod)
	 */
	public void callback(SootMethod sm) {
		methodCtxt2triple.put(sm, new Triple(new MethodContext(sm), new HashMap(), new HashMap()));
	}

	/**
	 * Performs phase1 (condition 2 and 3) operation here.  This should be called after the call graph information has been
	 * consolidated.
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.flow.interfaces.PostProcessor#consolidate()
	 */
	public void consolidate() {
		Collection tassBak = new HashSet(threadAllocSitesSingle);

		for(Iterator i = tassBak.iterator(); i.hasNext();) {
			NewExprTriple trp = (NewExprTriple) i.next();
			SootMethod encloser = (SootMethod) trp.getMethod();

			if(executedMultipleTimes(encloser)) {
				threadAllocSitesSingle.remove(trp);
				threadAllocSitesMulti.add(trp);
			}
		}

		Context ctxt = new Context();
		Collection multiExecMethods = new HashSet();

		// This is for third condition of phase 1 in Ruf's algorithm, i.e., "thread allocation sites reachable from run 
		// methods associated with multiply executed thread allocation sites".
		// We just collect 
		for(Iterator i = threadAllocSitesMulti.iterator(); i.hasNext();) {
			NewExprTriple ntrp = (NewExprTriple) i.next();
			ctxt.setRootMethod(ntrp.getMethod());
			ctxt.setStmt(ntrp.getStmt());
			multiExecMethods.addAll(tgi.getExecutedMethods(ntrp.getExpr(), ctxt));
		}
		tassBak.clear();
		tassBak.addAll(threadAllocSitesSingle);

		for(Iterator i = tassBak.iterator(); i.hasNext();) {
			NewExprTriple trp = (NewExprTriple) i.next();
			SootMethod encloser = (SootMethod) trp.getMethod();

			if(multiExecMethods.contains(encloser)) {
				threadAllocSitesSingle.remove(trp);
				threadAllocSitesMulti.add(trp);
			}
		}
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

		for(Iterator i = sccs.iterator(); i.hasNext();) {
			List nodes = (List) i.next();

			for(Iterator j = nodes.iterator(); j.hasNext();) {
				SimpleNode node = (SimpleNode) j.next();
				SootMethod sm = (SootMethod) node.object;
				LOGGER.info("Processing method " + sm);

				JimpleBody body;

				try {
					body = (JimpleBody) sm.getBody(Jimple.v());
				} catch(Exception e) {
					e.printStackTrace();
					System.out.println(sm.getSignature());
					body = new JimpleBody(sm, sm.getBody(ClassFile.v()), 0);
				}

				Triple triple = (Triple) methodCtxt2triple.get(sm);

				if(body == null) {
					LOGGER.warn("Punting ?????????????");
					continue;
				}
				methodCtxtCache = (MethodContext) triple.getFirst();
				localASsCache = (Map) triple.getSecond();
				scCache = (Map) triple.getThird();
				context.setRootMethod(sm);

				if(Modifier.isSynchronized(sm.getModifiers()) && !sm.isStatic()) {
					methodCtxtCache.getThisAS().setSynced();
				}

				for(ca.mcgill.sable.util.Iterator k = body.getStmtList().iterator(); k.hasNext();) {
					Stmt stmt = (Stmt) k.next();
					context.setStmt(stmt);
					stmt.apply(stmtProcessor);
				}
			}
		}

		WorkBag wb = new WorkBag(WorkBag.LIFO);
		Collection processed = new HashSet();
		wb.addAllWork(cgi.getHeads());

		while(!wb.isEmpty()) {
			SootMethod caller = (SootMethod) wb.getWork();
			Collection callees = cgi.getCallees(caller);
			Triple triple = (Triple) methodCtxt2triple.get(caller);
			Map ctrp2sc = (Map) triple.getThird();

			for(Iterator i = callees.iterator(); i.hasNext();) {
				CallTriple ctrp = (CallTriple) i.next();
				SootMethod callee = ctrp.getMethod();
				triple = (Triple) methodCtxt2triple.get(callee);

				MethodContext mc = (MethodContext) (triple.getFirst());
				CallTriple callerTrp = new CallTriple(caller, ctrp.getStmt(), ctrp.getExpr());
				MethodContext sc = (MethodContext) ctrp2sc.get(callerTrp);
				sc.propogateSyncInfoFromTo(mc);

				if(!processed.contains(callee)) {
					processed.add(callee);
					wb.addWork(callee);
				}
			}
		}
	}

	/**
	 * @see edu.ksu.cis.bandera.staticanalyses.flow.interfaces.PostProcessor#hookup(edu.ksu.cis.bandera.staticanalyses.flow.PostProcessingController)
	 */
	public void hookup(PostProcessingController ppc) {
		ppc.register(NewExpr.class, this);
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

		for(ca.mcgill.sable.util.Iterator i = sc.getFields().iterator(); i.hasNext();) {
			SootField sf = (SootField) i.next();

			if(Modifier.isStatic(sf.getModifiers())
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

		if(type instanceof ArrayType) {
			ArrayType at = (ArrayType) type;
			AliasSet s = new AliasSet();
			result = new AliasSet();

			AliasSet st = result;

			for(int i = at.numDimensions; i >= 1; i--) {
				s = new AliasSet();
				st.putASForField(ARRAY_FIELD, s);
				st = s;
			}
		} else if(type instanceof RefType) {
			result = getASForClass(scm.getClass(((RefType) type).className));
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
		return (((AliasSet) t).toString("  ", threadMap));
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
		if(triple == null) {
			return "";
		}

		Map localASs = (Map) triple.getSecond();
		FastUnionFindElement s = (FastUnionFindElement) localASs.get(l);

		if(s != null) {
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

		if(triple != null) {
			MethodContext mc = (MethodContext) triple.getFirst();

			if(mc.getThisAS() != null) {
				result.append("  Info for @this:\n" + mc.getThisAS().toString("    ", threadMap) + "\n");
			}

			for(int i = 0; i < sm.getParameterCount(); i++) {
				AliasSet t = mc.getParamAS(i);

				if(t == null) {
					continue;
				}
				result.append("  Info for @parameter" + i + ":\n" + t.toString("    ", threadMap) + "\n");
			}

			if(sm.getReturnType() instanceof RefType || sm.getReturnType() instanceof ArrayType) {
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

		if(callers.size() > 1) {
			result = true;
		} else if(callers.size() == 1) {
			CallTriple ctrp = (CallTriple) callers.iterator().next();
			SootMethod caller2 = ctrp.getMethod();
			BasicBlockGraph bbg =
				bbm.getBasicBlockGraph(new CompleteStmtGraph(((StmtBody) caller2.getBody(Jimple.v())).getStmtList()));

			if(bbg.occursInCycle(bbg.getEnclosingBlock(ctrp.getStmt()))) {
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
		String classname = ne.getBaseType().className;
		SootMethod sm = context.getCurrentMethod();

		if(Util.isDescendentOf(scm.getClass(classname), "java.lang.Thread")) {
			BasicBlockGraph bbg =
				bbm.getBasicBlockGraph(new CompleteStmtGraph(((StmtBody) sm.getBody(Jimple.v())).getStmtList()));
			Stmt stmt = context.getStmt();

			if(bbg.occursInCycle(bbg.getEnclosingBlock(stmt))) {
				threadAllocSitesMulti.add(new NewExprTriple(sm, stmt, ne));
			} else {
				threadAllocSitesSingle.add(new NewExprTriple(sm, stmt, ne));
			}
		}
	}
}

/*****
 ChangeLog:

$Log$

*****/
