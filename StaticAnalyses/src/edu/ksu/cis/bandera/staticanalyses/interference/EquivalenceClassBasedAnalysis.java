
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
import ca.mcgill.sable.soot.Modifier;
import ca.mcgill.sable.soot.RefType;
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
import ca.mcgill.sable.soot.jimple.FieldRef;
import ca.mcgill.sable.soot.jimple.IdentityStmt;
import ca.mcgill.sable.soot.jimple.InstanceFieldRef;
import ca.mcgill.sable.soot.jimple.InterfaceInvokeExpr;
import ca.mcgill.sable.soot.jimple.InvokeExpr;
import ca.mcgill.sable.soot.jimple.InvokeStmt;
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

import edu.ksu.cis.bandera.staticanalyses.ProcessingController;
import edu.ksu.cis.bandera.staticanalyses.auxillary.ICFGAnalysis;
import edu.ksu.cis.bandera.staticanalyses.flow.Context;
import edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.processors.AbstractProcessor;
import edu.ksu.cis.bandera.staticanalyses.interfaces.CallGraphInfo;
import edu.ksu.cis.bandera.staticanalyses.interfaces.CallGraphInfo.CallTriple;
import edu.ksu.cis.bandera.staticanalyses.interfaces.ThreadGraphInfo;
import edu.ksu.cis.bandera.staticanalyses.interfaces.ThreadGraphInfo.NewExprTriple;
import edu.ksu.cis.bandera.staticanalyses.support.BasicBlockGraph;
import edu.ksu.cis.bandera.staticanalyses.support.BasicBlockGraph.BasicBlock;
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
import java.util.Collections;
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
	public static final String ID = "Shared Access Information";

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
	final BasicBlockGraphMgr bbm;

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
	final Map method2Triple;

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
	 *
	 * @pre scm != null and cgi != null and tgi != null
	 */
	public EquivalenceClassBasedAnalysis(SootClassManager scm, CallGraphInfo cgi, ThreadGraphInfo tgi) {
		this.scm = scm;
		this.cgi = cgi;
		this.tgi = tgi;

		threadAllocSitesSingle = new HashSet();
		threadAllocSitesMulti = new HashSet();
		globalASs = new HashMap();
		method2Triple = new HashMap();
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
			stmt.getRightOp().apply(valueProcessor);

			AliasSet r = (AliasSet) valueProcessor.getResult();
			stmt.getLeftOp().apply(valueProcessor);

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
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		boolean accessed;

		/**
		 * @see ca.mcgill.sable.soot.jimple.RefSwitch#caseArrayRef(ca.mcgill.sable.soot.jimple.ArrayRef)
		 */
		public void caseArrayRef(ArrayRef v) {
			v.getBase().apply(this);

			AliasSet base = (AliasSet) getResult();
			AliasSet elt = (AliasSet) base.getASForField(ARRAY_FIELD);

			if(elt == null) {
				elt = AliasSet.getASForType(v.getType());
				base.putASForField(ARRAY_FIELD, elt);
			}

			elt.setAccessedTo(accessed);

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
				field = AliasSet.getASForType(v.getType());
				base.putASForField(fieldSig, field);
			}

			field.setAccessedTo(accessed);
			setResult(field);
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.ExprSwitch#caseInterfaceInvokeExpr(
		 * 		ca.mcgill.sable.soot.jimple.InterfaceInvokeExpr)
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
				s = AliasSet.getASForType(v.getType());
				localASsCache.put(v, s);
			}
			s.setAccessedTo(accessed);
			setResult(s);
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.RefSwitch#caseParameterRef( ca.mcgill.sable.soot.jimple.ParameterRef)
		 */
		public void caseParameterRef(ParameterRef v) {
			setResult(methodCtxtCache.getParamAS(v.getIndex()));
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.ExprSwitch#caseSpecialInvokeExpr( ca.mcgill.sable.soot.jimple.SpecialInvokeExpr)
		 */
		public void caseSpecialInvokeExpr(SpecialInvokeExpr v) {
			processInvokeExpr(v);
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.RefSwitch#caseStaticFieldRef( ca.mcgill.sable.soot.jimple.StaticFieldRef)
		 */
		public void caseStaticFieldRef(StaticFieldRef v) {
			setResult(globalASs.get(v.getField().getSignature()));
		}

		/**
		 * @see ca.mcgill.sable.soot.jimple.ExprSwitch#caseStaticInvokeExpr( ca.mcgill.sable.soot.jimple.StaticInvokeExpr)
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
		 * @see ca.mcgill.sable.soot.jimple.ExprSwitch#caseVirtualInvokeExpr( ca.mcgill.sable.soot.jimple.VirtualInvokeExpr)
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
			AliasSet retAS = null;

			retAS = AliasSet.getASForType(sm.getReturnType());

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

			MethodContext sc = getSiteContext(sm, thisAS, argASs, retAS, AliasSet.getASWithFieldMap());
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
				Triple triple = (Triple) method2Triple.get(callee);

				// This is needed when the system is not closed.
				if(triple == null) {
					continue;
				}

				if(callee.getName().equals("start")
					  && callee.getDeclaringClass().getName().equals("java.lang.Thread")
					  && callee.getReturnType() instanceof VoidType
					  && callee.getParameterCount() == 0) {
					unifyAll = true;
				}

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
			setResult(retAS);
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param v DOCUMENT ME!
	 * @param sm DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public Object getEntity(Value v, SootMethod sm) {
		return getAliasSetFor(v, sm).getEntity();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param l1 DOCUMENT ME!
	 * @param sm1 DOCUMENT ME!
	 * @param l2 DOCUMENT ME!
	 * @param sm2 DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 *
	 * @throws IllegalArgumentException DOCUMENT ME!
	 */
	public boolean isReadyDependent(Local l1, SootMethod sm1, Local l2, SootMethod sm2) {
		Triple trp1 = (Triple) method2Triple.get(sm1);

		if(trp1 == null) {
			throw new IllegalArgumentException(sm1 + " was not processed.");
		}

		Triple trp2 = (Triple) method2Triple.get(sm2);

		if(trp2 == null) {
			throw new IllegalArgumentException(sm2 + " was not processed.");
		}

		AliasSet as1 = (AliasSet) ((Map) trp1.getSecond()).get(l1);
		AliasSet as2 = (AliasSet) ((Map) trp2.getSecond()).get(l2);
		boolean result = false;

		if(as1.getEntity().equals(as2.getEntity())) {
			result = true;
		}
		return result;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param v DOCUMENT ME!
	 * @param sm DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public boolean isShared(Value v, SootMethod sm) {
		try {
			return getAliasSetFor(v, sm).isShared();
		} catch(RuntimeException e) {
			e.printStackTrace();
			throw e;
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
	 * @see edu.ksu.cis.bandera.staticanalyses.interfaces.Processor#callback( ca.mcgill.sable.soot.jimple.Value,
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
	 * @see edu.ksu.cis.bandera.staticanalyses.interfaces.Processor#callback(SootField)
	 */
	public void callback(SootField sf) {
		if(Modifier.isStatic(sf.getModifiers())) {
			AliasSet t = AliasSet.getASForType(sf.getType());
			t.setGlobal();
			globalASs.put(sf.getSignature(), t);
		}
	}

	/**
	 * Creates a method context for <code>sm</code>.  This is the creation of method contexts in Ruf's algorithm.
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.interfaces.Processor#callback(SootMethod)
	 */
	public void callback(SootMethod sm) {
		LOGGER.debug("Update method2Triple for " + sm);
		method2Triple.put(sm, new Triple(new MethodContext(sm), new HashMap(), new HashMap()));
	}

	/**
	 * Performs phase1 (condition 2 and 3) operation here.  This should be called after the call graph information has been
	 * consolidated.
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.interfaces.Processor#consolidate()
	 */
	public void consolidate() {
		LOGGER.info("BEGIN: equivalence class based escape analysis consolidation");

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
		LOGGER.info("END: equivalence class based escape analysis consolidation");
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
				SootMethod sm = (SootMethod) node._OBJECT;
				LOGGER.info("Processing: " + sm);

				JimpleBody body = Util.getJimpleBody(sm);
				Triple triple = (Triple) method2Triple.get(sm);

				if(body == null) {
					LOGGER.warn("NO BODY: " + sm.getSignature());
					continue;
				}

				/*
				 * NOTE: This is an anomaly which results from how an open system is closed.  Refer to MethodVariant.java for
				 * more info.
				 */
				if(triple == null) {
					LOGGER.debug("NO CALLEE TRIPLE: " + sm.getSignature());
					continue;
				}
				methodCtxtCache = (MethodContext) triple.getFirst();
				localASsCache = (Map) triple.getSecond();
				scCache = (Map) triple.getThird();
				context.setRootMethod(sm);

				wb.clear();

				BasicBlockGraph bbg = bbm.getBasicBlockGraph(sm);

				if(bbg == null) {
					bbg = bbm.getBasicBlockGraph(new CompleteStmtGraph(body.getStmtList()));
				}
				wb.addAllWork(bbg.getHeads());

				Collection processed = new HashSet();

				if(sm.getName().equals("<init>")) {
					valueProcessor.accessed = false;
				} else {
					valueProcessor.accessed = true;
				}

				while(!wb.isEmpty()) {
					BasicBlock bb = (BasicBlock) wb.getWork();
					processed.add(bb);

					for(Iterator k = bb.getStmtsOf().iterator(); k.hasNext();) {
						Stmt stmt = (Stmt) k.next();
						context.setStmt(stmt);
						stmt.apply(stmtProcessor);
					}

					for(Iterator k = bb.getSuccsOf().iterator(); k.hasNext();) {
						Object o = k.next();

						if(!processed.contains(o)) {
							wb.addWork(o);
						}
					}
				}

				if(valueProcessor.accessed) {
					continue;
				}
				wb.clear();

				if(!(sm.getReturnType() instanceof VoidType)) {
					wb.addWork(methodCtxtCache.getReturnAS());
				}
				wb.addWork(methodCtxtCache.getThrownAS());

				for(int k = sm.getParameterCount() - 1; k >= 0; k--) {
					wb.addWork(methodCtxtCache.getParamAS(k).find());
				}
				processed.clear();

				while(!wb.isEmpty()) {
					AliasSet as = (AliasSet) ((AliasSet) wb.getWork()).find();

					if(processed.contains(as)) {
						continue;
					}
					processed.add(as);
					as.setAccessedTo(true);

					if(as.hasFieldMap()) {
						for(Iterator k = as.getFieldMap().values().iterator(); k.hasNext();) {
							AliasSet element = (AliasSet) k.next();

							if(!processed.contains(element.find())) {
								wb.addWork(element.find());
							}
						}
					}
				}
			}
		}

		Collection processed = new HashSet();
		wb.addAllWork(cgi.getHeads());

		while(!wb.isEmpty()) {
			SootMethod caller = (SootMethod) wb.getWork();
			Collection callees = cgi.getCallees(caller);
			Triple triple = (Triple) method2Triple.get(caller);
			Map ctrp2sc = (Map) triple.getThird();
			LOGGER.debug("Top-down procesing in " + caller);

			for(Iterator i = callees.iterator(); i.hasNext();) {
				CallTriple ctrp = (CallTriple) i.next();
				SootMethod callee = ctrp.getMethod();
				LOGGER.debug("Top-down processing of " + callee);
				triple = (Triple) method2Triple.get(callee);

				/*
				 * NOTE: This is an anomaly which results from how an open system is closed.  Refer to MethodVariant.java for
				 * more info.
				 */
				if(triple == null) {
					LOGGER.debug("NO CALLEE TRIPLE: " + callee.getSignature());
					continue;
				}

				MethodContext mc = (MethodContext) (triple.getFirst());
				CallTriple callerTrp = new CallTriple(caller, ctrp.getStmt(), ctrp.getExpr());
				MethodContext sc = (MethodContext) ctrp2sc.get(callerTrp);
				sc.propogateInfoFromTo(mc);

				if(!processed.contains(callee)) {
					processed.add(callee);
					wb.addWork(callee);
				}
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param global DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public boolean hasSharedAccess(SootField global) {
		AliasSet as = (AliasSet) globalASs.get(global);
		boolean result;

		if(as == null) {
			result = false;
		} else {
			result = as.isShared();
		}
		return result;
	}

	/**
	 * @see edu.ksu.cis.bandera.staticanalyses.interfaces.Processor#hookup(
	 * 		edu.ksu.cis.bandera.staticanalyses.flow.ProcessingController)
	 */
	public void hookup(ProcessingController ppc) {
		ppc.register(NewExpr.class, this);
	}

	/**
	 * @see edu.ksu.cis.bandera.staticanalyses.interfaces.Processor#unhook(
	 * 		edu.ksu.cis.bandera.staticanalyses.flow.ProcessingController)
	 */
	public void unhook(ProcessingController ppc) {
		ppc.unregister(NewExpr.class, this);
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

	/*
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
	 */

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

	/*
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
	 */

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
		FastUnionFindElement t = (FastUnionFindElement) globalASs.get(sf);
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
		Triple triple = (Triple) method2Triple.get(sm);

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
		Triple triple = (Triple) method2Triple.get(sm);

		if(triple != null) {
			MethodContext mc = (MethodContext) triple.getFirst();
			result.append(mc.toString());
		} else {
			LOGGER.warn("Method " + sm + " did not have a method context.");
		}
		return result.toString();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param v DOCUMENT ME!
	 * @param sm DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 *
	 * @throws IllegalArgumentException DOCUMENT ME!
	 *
	 * @pre v.isOclKindOf(Local) or v.isOclKindOf(ArrayRef) or v.isOclKindOf(FieldRef)
	 * @pre v.isOclKindOf(ArrayRef) or v.isOclKindOf(InstanceFieldRef) implies v.getBase().isOclKindOf(Local)
	 */
	private AliasSet getAliasSetFor(Value v, SootMethod sm) {
		Triple trp = (Triple) method2Triple.get(sm);

		if(trp == null) {
			throw new IllegalArgumentException("Method " + sm + " was not analyzed.");
		}

		Map local2AS = (Map) trp.getSecond();
		AliasSet result = null;

		if(v instanceof InstanceFieldRef) {
			InstanceFieldRef i = (InstanceFieldRef) v;
			result = ((AliasSet) local2AS.get(i.getBase())).getASForField(((FieldRef) v).getField().getSignature());
		} else if(v instanceof StaticFieldRef) {
			result = (AliasSet) globalASs.get(((FieldRef) v).getField().getSignature());
		} else if(v instanceof ArrayRef) {
			ArrayRef a = (ArrayRef) v;
			result = ((AliasSet) local2AS.get(a.getBase())).getASForField(ARRAY_FIELD);
		} else if(v instanceof Local) {
			result = (AliasSet) local2AS.get(v);
		}
		return result;
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
class AliasSet
  extends FastUnionFindElement
  implements Cloneable {
	/**
	 * <p>
	 * The logger used by instances of this class to log messages.
	 * </p>
	 */
	private static final Log LOGGER = LogFactory.getLog(AliasSet.class);

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	static int entityCount = 0;

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
	private Map fieldMap;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private Object entity;

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
	 * <p>
	 * DOCUMENT ME!
	 * </p>
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
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private boolean waits;

	/**
	 * Creates a new AliasSet object with the given map as the field map.
	 *
	 * @param fieldMap DOCUMENT ME!
	 */
	private AliasSet(Map fieldMap) {
		this.fieldMap = fieldMap;
		theClone = null;
		shared = accessed = global = propogating = stringifying = false;
		entity = null;
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
		theClone.set = null;

		if(hasFieldMap()) {
			theClone.fieldMap = new HashMap();

			for(Iterator i = fieldMap.entrySet().iterator(); i.hasNext();) {
				Map.Entry entry = (Map.Entry) i.next();
				AliasSet temp = (AliasSet) ((AliasSet) entry.getValue()).clone();
				theClone.fieldMap.put(entry.getKey(), temp);
			}
		} else {
			theClone.fieldMap = null;
		}

		Object result = theClone;
		theClone = null;

		return result;
	}

	/**
	 * Creates an alias set suitable for the given type.
	 *
	 * @param type is the type from which Alias set is requested.
	 *
	 * @return the alias set corresponding to the given type.
	 */
	static AliasSet getASForType(Type type) {
		AliasSet result = null;

		if(!(type instanceof VoidType)) {
			if(type instanceof RefType || type instanceof ArrayType) {
				result = new AliasSet(new HashMap());
			} else {
				result = new AliasSet(null);
			}
		}
		return result;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	static AliasSet getASWithFieldMap() {
		return new AliasSet(new HashMap());
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	static AliasSet getASWithoutFieldMap() {
		return new AliasSet(null);
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
	 *
	 * @param value DOCUMENT ME!
	 */
	void setAccessedTo(boolean value) {
		((AliasSet) find()).accessed = value;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	Object getEntity() {
		return ((AliasSet) find()).entity;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	Map getFieldMap() {
		Map result = null;

		if(fieldMap != null) {
			result = Collections.unmodifiableMap(fieldMap);
		}
		return result;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	void setGlobal() {
		AliasSet rep = (AliasSet) find();

		if(rep.global) {
			return;
		}
		rep.global = true;
		rep.shared = true;

		if(rep.entity == null) {
			rep.entity = "Entity" + entityCount++ + ":";
		}

		if(rep.fieldMap != null) {
			for(Iterator i = rep.fieldMap.values().iterator(); i.hasNext();) {
				AliasSet as = (AliasSet) i.next();
				as.setGlobal();
			}
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
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	void setWaits() {
		((AliasSet) find()).waits = true;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	boolean hasFieldMap() {
		return fieldMap != null;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param as DOCUMENT ME!
	 */
	void propogateInfoFromTo(AliasSet as) {
		if(propogating) {
			return;
		}
		propogating = true;

		AliasSet rep1 = (AliasSet) find();
		AliasSet rep2 = (AliasSet) as.find();
		rep2.shared = rep1.shared;
		rep2.entity = rep1.entity;

		if(rep2.hasFieldMap() && rep1.hasFieldMap()) {
			for(Iterator i = rep2.fieldMap.entrySet().iterator(); i.hasNext();) {
				Map.Entry entry = (Map.Entry) i.next();
				AliasSet to = (AliasSet) ((AliasSet) rep2.fieldMap.get(entry.getKey())).find();
				AliasSet from = (AliasSet) ((AliasSet) rep1.fieldMap.get(entry.getKey())).find();
				from.propogateInfoFromTo(to);
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

		if(rep.fieldMap == null || rep.fieldMap.isEmpty()) {
			fields.append(newTabbing + "NONE");
		} else {
			for(Iterator i = rep.fieldMap.entrySet().iterator(); i.hasNext();) {
				Map.Entry entry = (Map.Entry) i.next();
				fields.append(newTabbing + entry.getKey() + ":\n"
					+ ((AliasSet) entry.getValue()).toString(newTabbing + "    ") + "\n");
			}
		}
		result.append(tabbing + "GLOBAL: " + rep.global + "\n");
		result.append(tabbing + "ACCESSED: " + rep.accessed + "\n");
		result.append(tabbing + "SHARED: " + rep.shared + "\n");
		result.append(tabbing + "WAITS: " + rep.waits + "\n");
		result.append(tabbing + "NOTIFIES: " + rep.notifies + "\n");
		result.append(tabbing + "ENITY: " + rep.entity + "\n");
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

		if(unifyAll) {
			rep1.shared |= rep1.accessed && rep2.accessed;

			if(rep1.entity == null && (rep1.waits && rep2.notifies) || (rep1.notifies && rep2.waits)) {
				rep1.entity = new String("Entity:" + entityCount++);
			}
		}

		rep1.accessed |= rep2.accessed;
		rep1.shared |= rep2.shared;

		if(rep1.hasFieldMap() && rep2.hasFieldMap()) {
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
		}

		if(rep1.global || rep2.global) {
			rep1.setGlobal();
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
			Type type = sm.getParameterType(i);
			AliasSet t = AliasSet.getASForType(type);
			argAliasSets.add(i, t);
		}

		if(!(sm.getReturnType() instanceof VoidType)) {
			ret = AliasSet.getASForType(sm.getReturnType());
		}
		thrown = AliasSet.getASWithFieldMap();

		if(!sm.isStatic()) {
			thisAS = AliasSet.getASWithFieldMap();  //getASForClass(sm.getDeclaringClass());
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
		MethodContext result = null;

		if(set != null) {
			result = (MethodContext) ((MethodContext) find()).clone();
		} else {
			result = (MethodContext) super.clone();

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
		}
		return result;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	public String toString() {
		StringBuffer result = new StringBuffer();

		if(getThisAS() != null) {
			result.append("  Info for @this:\n" + getThisAS().toString("  ") + "\n");
		}

		for(Iterator i = argAliasSets.iterator(); i.hasNext();) {
			AliasSet t = ((AliasSet) i.next());

			if(t == null) {
				continue;
			}
			result.append("  Info for @parameter" + i + ":\n" + ((AliasSet) t.find()).toString("  ") + "\n");
		}

		if(getReturnAS() != null) {
			result.append("  Info for @return:\n" + getReturnAS().toString("  ") + "\n");
		}
		return result.toString();
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
	void propogateInfoFromTo(MethodContext mc) {
		MethodContext rep1 = (MethodContext) find();
		MethodContext rep2 = (MethodContext) mc.find();

		for(int i = method.getParameterCount() - 1; i >= 0; i--) {
			AliasSet s = (AliasSet) rep1.argAliasSets.get(i);
			AliasSet t = (AliasSet) rep2.argAliasSets.get(i);

			if(s != null && t != null) {
				s.propogateInfoFromTo(t);
			}
		}

		if(rep1.ret != null) {
			rep1.ret.propogateInfoFromTo(rep2.ret);
		}
		thrown.propogateInfoFromTo(mc.thrown);

		if(rep1.thisAS != null) {
			rep1.thisAS.propogateInfoFromTo(rep2.thisAS);
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
				AliasSet t = (AliasSet) n.argAliasSets.get(i);

				if(t != null) {
					s.unify(t, unifyAll);
				}
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

		if(rep.hasFieldMap()) {
			for(Iterator i = rep.getFieldMap().keySet().iterator(); i.hasNext();) {
				Object o = i.next();

				AliasSet a = (AliasSet) rep.getFieldMap().get(o);
				AliasSet b = (AliasSet) d.getFieldMap().get(o);

				if(!(clonee2clone.containsKey(a))) {
					buildClonee2CloneMap(a, b, clonee2clone);
				}
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

/*****
 ChangeLog:

$Log$

*****/
