
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

package edu.ksu.cis.bandera.staticanalyses.interference;

import ca.mcgill.sable.soot.ArrayType;
import ca.mcgill.sable.soot.ClassFile;
import ca.mcgill.sable.soot.Modifier;
import ca.mcgill.sable.soot.RefType;
import ca.mcgill.sable.soot.SootClass;
import ca.mcgill.sable.soot.SootClassManager;
import ca.mcgill.sable.soot.SootField;
import ca.mcgill.sable.soot.SootMethod;
import ca.mcgill.sable.soot.Type;
import ca.mcgill.sable.soot.VoidType;

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
import ca.mcgill.sable.soot.jimple.ThisRef;
import ca.mcgill.sable.soot.jimple.ThrowStmt;
import ca.mcgill.sable.soot.jimple.Value;
import ca.mcgill.sable.soot.jimple.VirtualInvokeExpr;

import edu.ksu.cis.bandera.staticanalyses.auxillary.ICFGAnalysis;
import edu.ksu.cis.bandera.staticanalyses.flow.Context;
import edu.ksu.cis.bandera.staticanalyses.flow.ProcessingController;
import edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.processors.AbstractProcessor;
import edu.ksu.cis.bandera.staticanalyses.flow.interfaces.CallGraphInfo;
import edu.ksu.cis.bandera.staticanalyses.flow.interfaces.CallGraphInfo.CallTriple;
import edu.ksu.cis.bandera.staticanalyses.flow.interfaces.ThreadGraphInfo;
import edu.ksu.cis.bandera.staticanalyses.flow.interfaces.ThreadGraphInfo.NewExprTriple;
import edu.ksu.cis.bandera.staticanalyses.support.BasicBlockGraph;
import edu.ksu.cis.bandera.staticanalyses.support.BasicBlockGraphMgr;
import edu.ksu.cis.bandera.staticanalyses.support.FastUnionFindElement;
import edu.ksu.cis.bandera.staticanalyses.support.SimpleNodeGraph;
import edu.ksu.cis.bandera.staticanalyses.support.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.bandera.staticanalyses.support.SimpleNodeGraph.SimpleNode;
import edu.ksu.cis.bandera.staticanalyses.support.Triple;
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
public class EquivalenceClassBasedAnalysis
  extends AbstractProcessor {
	// Cache variables do not capture state of the object.  Rather they are used cache values across method calls.

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
	static final Log LOGGER = LogFactory.getLog(EquivalenceClassBasedAnalysis.class);

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
	final ICFGAnalysis icfgAnalysis;

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

	final BasicBlockGraphMgr bbm;

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
	 * Creates a new EquivalenceClassBasedAnalysis object.
	 *
	 * @param scm DOCUMENT ME!
	 * @param cgi DOCUMENT ME!
	 * @param tgi DOCUMENT ME!
	 */
	public EquivalenceClassBasedAnalysis(SootClassManager scm, CallGraphInfo cgi, ThreadGraphInfo tgi) {
		this.scm = scm;
		this.cgi = cgi;
		this.tgi = tgi;

		threadAllocSitesSingle = new HashSet();
		threadAllocSitesMulti = new HashSet();
		globalASs = new HashMap();
		methodCtxt2triple = new HashMap();
		stmtProcessor = new StmtProcessor();
		valueProcessor = new ValueProcessor();
		context = new Context();
		bbm = new BasicBlockGraphMgr();
		icfgAnalysis = new ICFGAnalysis(scm, cgi, bbm);
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
		 * The logger used by instances of this class to log messages.
		 * </p>
		 */
		private final Log LOGGER = LogFactory.getLog(AliasSet.class);

		/**
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		private AliasSet theClone;

		/**
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		private boolean accessed;

		/**
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		private boolean global;

		/** 
		 * <p>DOCUMENT ME! </p>
		 */
		private boolean inReadyDA;

		/** 
		 * <p>DOCUMENT ME! </p>
		 */
		private boolean notifies;

		/**
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		private boolean propogating;

		/**
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		private boolean shared;

		/**
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		private boolean stringifying;

		/** 
		 * <p>DOCUMENT ME! </p>
		 */
		private boolean waits;

		/**
		 * Creates a new AliasSet object.
		 */
		AliasSet() {
			fieldMap = new HashMap();
			theClone = null;
			inReadyDA = shared = accessed = global = propogating = stringifying = false;
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

			//optimization
			if(isGlobal()) {
				return find();
			}

			//just work on the representative of the class
			if(set != null) {
				return (AliasSet) ((AliasSet) find()).clone();
			}

			theClone = (AliasSet) super.clone();
			theClone.fieldMap = new HashMap();

			for(Iterator i = fieldMap.entrySet().iterator(); i.hasNext();) {
				Map.Entry entry = (Map.Entry) i.next();
				AliasSet temp = (AliasSet) ((AliasSet) entry.getValue()).clone();

				if(!isGlobal()) {
					temp.set = null;
				}
				theClone.fieldMap.put(entry.getKey(), temp);
			}

			Object result = theClone;
			theClone = null;

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
		void setAccessed() {
			((AliasSet) find()).accessed = true;
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 *
		 * @return DOCUMENT ME!
		 */
		boolean isAccessed() {
			return ((AliasSet) find()).accessed;
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
			rep.shared = true;
			rep.inReadyDA = true;

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
		 * DOCUMENT ME! <p></p>
		 *
		 * @return DOCUMENT ME!
		 */
		boolean isInReadyDA() {
			return ((AliasSet) find()).inReadyDA;
		}

		/**
		 * DOCUMENT ME! <p></p>
		 */
		void setNotifies() {
			((AliasSet) find()).notifies = true;
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 *
		 * @return DOCUMENT ME!
		 */
		boolean isShared() {
			return ((AliasSet) find()).shared;
		}

		/**
		 * DOCUMENT ME! <p></p>
		 */
		void setWaits() {
			((AliasSet) find()).waits = true;
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
			propogating = true;

			AliasSet rep1 = (AliasSet) find();
			AliasSet rep2 = (AliasSet) as.find();

			for(Iterator i = rep2.fieldMap.entrySet().iterator(); i.hasNext();) {
				Map.Entry entry = (Map.Entry) i.next();
				AliasSet to = (AliasSet) ((AliasSet) rep2.fieldMap.get(entry.getKey())).find();

				if(to.isShared()) {
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
		 *
		 * @return DOCUMENT ME!
		 */
		String toString(String tabbing) {
			if(stringifying) {
				return tabbing + "Cycle from here on.";
			}
			stringifying = true;

			StringBuffer result = new StringBuffer();
			AliasSet rep = (AliasSet) find();

			StringBuffer fields = new StringBuffer();
			String newTabbing = tabbing + "  ";

			if(rep.fieldMap.isEmpty()) {
				fields.append(newTabbing + "NONE");
			} else {
				for(Iterator i = rep.fieldMap.entrySet().iterator(); i.hasNext();) {
					Map.Entry entry = (Map.Entry) i.next();
					fields.append(newTabbing + entry.getKey() + ":\n"
						+ ((AliasSet) entry.getValue()).toString(newTabbing + "    ") + "\n");
				}
			}
			result.append(tabbing + "GLOBAL: " + rep.isGlobal() + "\n");
			result.append(tabbing + "SHARED: " + rep.isShared() + "\n");
			result.append(tabbing + "ACCESSED: " + rep.isAccessed() + "\n");
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
		 * @param unifyAll DOCUMENT ME!
		 */
		void unify(AliasSet a, boolean unifyAll) {
			if(a == null) {
				LOGGER.warn("Unification with null requested.");
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
			rep1.accessed |= rep2.accessed;
			rep1.shared |= rep2.shared;

			Collection toBeProcessed = new HashSet();
			toBeProcessed.addAll(rep2.fieldMap.keySet());

			for(Iterator i = rep1.fieldMap.keySet().iterator(); i.hasNext();) {
				String field = (String) i.next();
				AliasSet repAS = (AliasSet) ((FastUnionFindElement) rep1.fieldMap.get(field)).find();
				toBeProcessed.remove(field);

				FastUnionFindElement temp = (FastUnionFindElement) rep2.fieldMap.get(field);

				if(temp != null) {
					repAS.unify((AliasSet) temp.find(), unifyAll);
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

			if(unifyAll) {
				rep1.shared |= rep1.accessed && rep2.accessed;
				rep1.inReadyDA |= (rep1.waits && rep2.notifies) || (rep1.notifies && rep2.waits);
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
		List argAliasSets;

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
			argAliasSets = new ArrayList(sm.getParameterCount());

			for(int i = 0; i < sm.getParameterCount(); i++) {
				/*
				   Type type = sm.getParameterType(i);
				   AliasSet t = getASForType(type);
				   argAliasSets.add(i, t);
				 */
				argAliasSets.add(i, new AliasSet());
			}

			if(!(sm.getReturnType() instanceof VoidType)) {
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
			result.argAliasSets = new ArrayList();

			for(Iterator i = argAliasSets.iterator(); i.hasNext();) {
				AliasSet tmp = (AliasSet) i.next();

				if(tmp != null) {
					Object o = tmp.clone();
					result.argAliasSets.add(o);
					buildClonee2CloneMap(tmp, (AliasSet) o, clonee2clone);
				} else {
					result.argAliasSets.add(null);
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
			return (AliasSet) ((MethodContext) find()).argAliasSets.get(index);
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
				AliasSet s = (AliasSet) rep1.argAliasSets.get(i);

				if(s != null) {
					s.propogateSyncInfoFromTo((AliasSet) rep2.argAliasSets.get(i));
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
		 * @param unifyAll DOCUMENT ME!
		 *
		 * @throws IllegalStateException DOCUMENT ME!
		 */
		void unify(MethodContext p, boolean unifyAll) {
			if(p == null) {
				return;
			}

			MethodContext m = (MethodContext) find();
			MethodContext n = (MethodContext) p.find();

			if(m == n) {
				return;
			}

			for(int i = method.getParameterCount() - 1; i >= 0; i--) {
				AliasSet s = (AliasSet) m.argAliasSets.get(i);

				if(s != null) {
					s.unify((AliasSet) n.argAliasSets.get(i), unifyAll);
				}
			}

			if((m.ret == null && n.ret != null) || (m.ret != null && n.ret == null)) {
				throw new IllegalStateException("Incompatible method contexts being unified - return value.");
			}

			if(m.ret != null) {
				m.ret.unify(n.ret, unifyAll);
			}
			m.thrown.unify(n.thrown, unifyAll);

			if((m.thisAS == null && n.thisAS != null) || (m.thisAS != null && n.thisAS == null)) {
				throw new IllegalStateException("Incompatible method contexts being unified - staticness");
			}

			if(m.thisAS != null) {
				m.thisAS.unify(n.thisAS, unifyAll);
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
		/**
		 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseAssignStmt(ca.mcgill.sable.soot.jimple.AssignStmt)
		 */
		public void caseAssignStmt(AssignStmt stmt) {
			stmt.getRightOp().apply(valueProcessor);

			AliasSet r = (AliasSet) valueProcessor.getResult();
			stmt.getLeftOp().apply(valueProcessor);

			AliasSet l = (AliasSet) valueProcessor.getResult();

			if(r != null && l != null) {
				l.unify(r, false);
			}
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseEnterMonitorStmt(ca.mcgill.sable.soot.jimple.EnterMonitorStmt)
		 */
		public void caseEnterMonitorStmt(EnterMonitorStmt stmt) {
			stmt.getOp().apply(valueProcessor);
		}
		

		/**
		 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseExitMonitorStmt(ca.mcgill.sable.soot.jimple.ExitMonitorStmt)
		 */
		public void caseExitMonitorStmt(ExitMonitorStmt stmt) {
			stmt.getOp().apply(valueProcessor);
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
				l.unify(r, false);
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
				methodCtxtCache.getReturnAS().unify(l, false);
			}
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseThrowStmt(ca.mcgill.sable.soot.jimple.ThrowStmt)
		 */
		public void caseThrowStmt(ThrowStmt stmt) {
			stmt.getOp().apply(valueProcessor);
			methodCtxtCache.getThrownAS().unify((AliasSet) valueProcessor.getResult(), false);
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
			v.getBase().apply(this);

			AliasSet base = (AliasSet) getResult();
			AliasSet elt = (AliasSet) base.getASForField(ARRAY_FIELD);

			if(elt == null) {
				elt = new AliasSet();
				base.putASForField(ARRAY_FIELD, elt);
			}

			// Think about this
			elt.setAccessed();

			setResult(elt);
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.RefSwitch#caseInstanceFieldRef(ca.mcgill.sable.soot.jimple.InstanceFieldRef)
		 */
		public void caseInstanceFieldRef(InstanceFieldRef v) {
			v.getBase().apply(this);

			AliasSet base = (AliasSet) getResult();
			String fieldSig = v.getField().getSignature();
			AliasSet field = (AliasSet) base.getASForField(fieldSig);

			if(field == null) {
				field = new AliasSet();
				base.putASForField(fieldSig, field);
			}

			// Think about this
			field.setAccessed();

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
			AliasSet s = (AliasSet) localASsCache.get(v);

			if(s == null) {
				s = new AliasSet();
				localASsCache.put(v, s);
			}

			// Think about this
			s.setAccessed();

			setResult(s);
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.RefSwitch#caseParameterRef(ca.mcgill.sable.soot.jimple.ParameterRef)
		 */
		public void caseParameterRef(ParameterRef v) {
			setResult(methodCtxtCache.getParamAS(v.getIndex()));
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
			setResult(globalASs.get(v.getField().getSignature()));
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
		 * @param v DOCUMENT ME!
		 */
		private void processInvokeExpr(InvokeExpr v) {
			Collection callees = new ArrayList();
			SootMethod caller = context.getCurrentMethod();
			SootMethod sm = v.getMethod();

			// fix up "return" alias set.
			Stmt stmt = context.getStmt();
			AliasSet retAS = (sm.getReturnType() instanceof VoidType) ? null : new AliasSet();

			if(stmt instanceof AssignStmt) {
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
				callees.addAll(cgi.getCallees(v, context));
			}

			for(Iterator i = callees.iterator(); i.hasNext();) {
				boolean unifyAll = false;
				SootMethod callee = (SootMethod) i.next();
				Triple triple = (Triple) methodCtxt2triple.get(callee);

				// This is needed when the system is not closed.
				if(triple == null) {
					continue;
				}
				
				if (callee.getName().equals("start") && callee.getDeclaringClass().getName().equals("java.lang.Thread") &&
					callee.getReturnType() instanceof VoidType && callee.getParameterCount() == 0)
					unifyAll = true;

				MethodContext mc = (MethodContext) triple.getFirst();

				if(icfgAnalysis.notInSameSCC(caller, callee)) {
					try {
						mc = (MethodContext) mc.clone();
					} catch(CloneNotSupportedException e) {
						LOGGER.error("Hell NO!  This should not happen.", e);
					}
				}
				sc.unify(mc, unifyAll);
			}
		}
	}

	/**
	 * Invoked by the controller when a <code>Value</code> is encountered.
	 *
	 * @param value DOCUMENT ME!
	 * @param context DOCUMENT ME!
	 *
	 * @pre value.isOclKindOf(NewExpr)
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.flow.interfaces.Processor#callback(ca.mcgill.sable.soot.jimple.Value,
	 * 		edu.ksu.cis.bandera.staticanalyses.flow.Context)
	 */
	public void callback(Value value, Context context) {
		if(value instanceof NewExpr) {
			NewExpr e = (NewExpr) value;
			Object o = new NewExprTriple(context.getCurrentMethod(), context.getStmt(), e);

			if(icfgAnalysis.checkForLoopEnclosedNewExpr(e, context)) {
				threadAllocSitesMulti.add(o);
			} else {
				threadAllocSitesSingle.add(o);
			}
		}
	}

	/**
	 * Creates an alias set for the static fields.  This is the creation of  global alias sets in Ruf's algorithm.
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.flow.interfaces.Processor#callback(SootField)
	 */
	public void callback(SootField sf) {
		if(Modifier.isStatic(sf.getModifiers())) {
			AliasSet t = getASForType(sf.getType());
			t.setGlobal();
			globalASs.put(sf.getSignature(), t);
			LOGGER.info("Adding alias set for " + sf.getSignature());
		}
	}

	/**
	 * Creates a method context for <code>sm</code>.  This is the creation of method contexts in Ruf's algorithm.
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.flow.interfaces.Processor#callback(SootMethod)
	 */
	public void callback(SootMethod sm) {
		methodCtxt2triple.put(sm, new Triple(new MethodContext(sm), new HashMap(), new HashMap()));
	}

	/**
	 * Performs phase1 (condition 2 and 3) operation here.  This should be called after the call graph information has been
	 * consolidated.
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.flow.interfaces.Processor#consolidate()
	 */
	public void consolidate() {
		Collection tassBak = new HashSet(threadAllocSitesSingle);

		for(Iterator i = tassBak.iterator(); i.hasNext();) {
			NewExprTriple trp = (NewExprTriple) i.next();
			SootMethod encloser = (SootMethod) trp.getMethod();

			if(icfgAnalysis.executedMultipleTimes(encloser)) {
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
		Collection sccs = sng.getSCCs(false);
		WorkBag wb = new WorkBag(WorkBag.FIFO);
		
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
					body = new JimpleBody(sm, sm.getBody(ClassFile.v()), 0);
				}

				Triple triple = (Triple) methodCtxt2triple.get(sm);

				if(body == null || triple == null) {
					LOGGER.warn("Punting while processing " + sm);
					continue;
				}
				methodCtxtCache = (MethodContext) triple.getFirst();
				localASsCache = (Map) triple.getSecond();
				scCache = (Map) triple.getThird();
				context.setRootMethod(sm);

				
				wb.clear();
				BasicBlockGraph bbg = bbm.getBasicBlockGraph(sm);
				if (bbg == null)
					bbg = bbm.getBasicBlockGraph(new CompleteStmtGraph(body.getStmtList()));
				wb.addAllWork(bbg.getHeads());
				Collection processed = new HashSet();
				while(!wb.isEmpty()) {
					BasicBlock bb = (BasicBlock)wb.getWork();
					processed.add(bb);
					for (Iterator k = bb.getStmtsOf().iterator(); k.hasNext();) {
						Stmt stmt = (Stmt) k.next();
						context.setStmt(stmt);
						stmt.apply(stmtProcessor);
					}
					for (Iterator k = bb.getSuccsOf().iterator(); k.hasNext();) {
						Object o = k.next();
						if (!processed.contains(o))
							wb.addWork(o);
					}
				}
			}
		}

		if (true) return;

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
	 * @see edu.ksu.cis.bandera.staticanalyses.flow.interfaces.Processor#hookup(edu.ksu.cis.bandera.staticanalyses.flow.ProcessingController)
	 */
	public void hookup(ProcessingController ppc) {
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

			if(Modifier.isStatic(sf.getModifiers())) {
				globalASs.put(sf.getSignature(), new AliasSet());
			} else {
				result.putASForField(sf.getSignature(), new AliasSet());
			}
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
		AliasSet result;

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
		} else {
			result = new AliasSet();
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
		result.argAliasSets = argASs;
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
	 *
	 * @return DOCUMENT ME!
	 */
	String toString(SootField sf) {
		FastUnionFindElement t = (FastUnionFindElement) globalASs.get(sf.getSignature());
		return (((AliasSet) t).toString("  "));
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
	String toString(SootMethod sm, Local l) {
		Triple triple = (Triple) methodCtxt2triple.get(sm);

		// This is needed in cases when the system is not closed.
		if(triple == null) {
			return "";
		}

		Map localASs = (Map) triple.getSecond();
		FastUnionFindElement s = (FastUnionFindElement) localASs.get(l);

		if(s != null) {
			return ((AliasSet) s).toString("  ");
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
	 *
	 * @return DOCUMENT ME!
	 */
	String toString(SootMethod sm) {
		StringBuffer result = new StringBuffer();
		Triple triple = (Triple) methodCtxt2triple.get(sm);

		if(triple != null) {
			MethodContext mc = (MethodContext) triple.getFirst();

			if(mc.getThisAS() != null) {
				result.append("  Info for @this:\n" + mc.getThisAS().toString("  ") + "\n");
			}

			for(int i = 0; i < sm.getParameterCount(); i++) {
				AliasSet t = mc.getParamAS(i);

				if(t == null) {
					continue;
				}
				result.append("  Info for @parameter" + i + ":\n" + t.toString("  ") + "\n");
			}

			if(!(sm.getReturnType() instanceof VoidType)) {
				result.append("  Info for @return:\n" + mc.getReturnAS().toString("  ") + "\n");
			}
		} else {
			LOGGER.warn("Method " + sm + " did not have a method context.");
		}
		return result.toString();
	}
}

/*****
 ChangeLog:

$Log$

*****/
