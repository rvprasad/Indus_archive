
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (C) 2003, 2004, 2005
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://indus.projects.cis.ksu.edu/).
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
 *                http://indus.projects.cis.ksu.edu/
 */

package edu.ksu.cis.indus.staticanalyses.concurrency.escape;

import soot.Local;
import soot.Modifier;
import soot.Scene;
import soot.SootField;
import soot.SootMethod;
import soot.Value;
import soot.VoidType;

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
import soot.jimple.JimpleBody;
import soot.jimple.NewExpr;
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
import edu.ksu.cis.indus.staticanalyses.support.SimpleNodeGraph;
import edu.ksu.cis.indus.staticanalyses.support.SimpleNodeGraph.SimpleNode;
import edu.ksu.cis.indus.staticanalyses.support.Triple;
import edu.ksu.cis.indus.staticanalyses.support.WorkBag;

import org.apache.commons.collections.CollectionUtils;
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
 * This class represents Equivalence Class-based analysis to calculate escape information of objects.  Escape information is
 * provided in terms of share-ability of the object bound to a given value in a given method.  This analysis is overloaded
 * as  a symbolic analysis to calculate information that can be used to prune ready-dependence edges.
 * 
 * <p>
 * The implementation is based on the techreport <a
 * href="http://www.cis.ksu.edu/santos/papers/technicalReports/SAnToS-TR2003-6.pdf">Honing the  Detection of Interference
 * and Ready Dependence for Slicing Concurrent Java Programs.</a>
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class EquivalenceClassBasedEscapeAnalysis
  extends AbstractProcessor {
	/*
	 * xxxCache variables do not capture state of the object.  Rather they are used cache values across method calls.  Hence,
	 * any subclasses of this class should  not reply on these variables as they may be removed in the future.
	 */

	/**
	 * This is the unique string identifier that can be used to identify an instance of this class.
	 */
	public static final String ID = "Shared Access Information";

	/**
	 * The logger used by instances of this class to log messages.
	 */
	static final Log LOGGER = LogFactory.getLog(EquivalenceClassBasedEscapeAnalysis.class);

	/**
	 * This manages the basic block graphs corresponding to the methods in being analyzed.
	 */
	final BasicBlockGraphMgr bbm;

	/**
	 * This provides inter-procedural control-flow information.
	 */
	final CFGAnalysis cfgAnalysis;

	/**
	 * This provides context information pertaining to caller-callee relation across method calls.  The method stored in the
	 * context is the caller.  The statement is one in which invocation occurs.  The program point is at which place the
	 * invocation happens.
	 */
	final Context context;

	/**
	 * This provides call-graph information.
	 */
	final ICallGraphInfo cgi;

	/**
	 * This provides thread-graph information.
	 */
	final IThreadGraphInfo tgi;

	/**
	 * This maps global/static fields to their alias sets.
	 *
	 * @invariant globalASs->forall(o | o.oclIsKindOf(AliasSet))
	 */
	final Map globalASs;

	/**
	 * This maps a method to a triple containing the method context, the alias sets for the locals in the method (key), and
	 * the site contexts for all the call-sites in the method(key).
	 *
	 * @invariant method2Triple.oclIsKindOf(Map(SootMethod, Triple(MethodContext, Map(Local, AliasSet), Map(CallTriple,
	 * 			  MethodContext))))
	 */
	final Map method2Triple;

	/**
	 * The soot class management entity that manages the classes being analyzed.
	 */
	final Scene scm;

	/**
	 * This is the statement processor used to analyze the methods.
	 */
	final StmtProcessor stmtProcessor;

	/**
	 * This is the <code>Value</code> processor used to process Jimple pieces that make up the methods.
	 */
	final ValueProcessor valueProcessor;

	/**
	 * This is a cache variable that holds local alias set map between method calls.
	 *
	 * @invariant localASsCache.oclIsKindOf(Local, AliasSet)
	 */
	Map localASsCache;

	/**
	 * This is a cache variable that holds site context map between method calls.
	 *
	 * @invariant scCache.oclIsKindOf(CallTriple, MethodContex)
	 */
	Map scCache;

	/**
	 * This is a cache variable that holds method context map between method calls.
	 */
	MethodContext methodCtxtCache;

	/**
	 * A collection of thread allocation sites which are executed from within a loop or a SCC in the call graph.  This  also
	 * includes any allocation sites reachable from a method executed in a loop.
	 *
	 * @invariant threadAllocSitesSingle.oclIsKindOf(Triple(SootMethod, Stmt, NewExpr)))
	 */
	private final Collection threadAllocSitesMulti;

	/**
	 * A collection of thread allocation sites which are not executed from within a loop or a SCC in the call graph.  This
	 * also includes any allocation sites reachable from a method executed in a loop. This is the dual of
	 * <code>threadAllocSitesMulti.</code>
	 *
	 * @invariant threadAllocSitesSingle.oclIsKindOf(Triple(SootMethod, Stmt, NewExpr)))
	 */
	private final Collection threadAllocSitesSingle;

	/**
	 * Creates a new EquivalenceClassBasedEscapeAnalysis object.
	 *
	 * @param scene provides and manages the classes to be analysed.
	 * @param callgraph provides call-graph information.
	 * @param tgiPrm provides thread-graph information.
	 *
	 * @pre scene != null and callgraph != null and tgi != null
	 */
	public EquivalenceClassBasedEscapeAnalysis(final Scene scene, final ICallGraphInfo callgraph,
		final IThreadGraphInfo tgiPrm) {
		this.scm = scene;
		this.cgi = callgraph;
		this.tgi = tgiPrm;

		threadAllocSitesSingle = new HashSet();
		threadAllocSitesMulti = new HashSet();
		globalASs = new HashMap();
		method2Triple = new HashMap();
		stmtProcessor = new StmtProcessor();
		valueProcessor = new ValueProcessor();
		bbm = new BasicBlockGraphMgr();
		context = new Context();
		cfgAnalysis = new CFGAnalysis(scm, cgi, bbm);
	}

	/**
	 * This class encapsulates the logic to process the statements during escape analysis.  Each overridden methods in  this
	 * class will process the expressions in the statement and unify them as per to the rules associated with the
	 * statements.
	 * 
	 * <p>
	 * The arguments to any of the overridden methods cannot be <code>null</code>.
	 * </p>
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	class StmtProcessor
	  extends AbstractStmtSwitch {
		/**
		 * @see soot.jimple.StmtSwitch#caseAssignStmt(soot.jimple.AssignStmt)
		 */
		public void caseAssignStmt(final AssignStmt stmt) {
			stmt.getRightOp().apply(valueProcessor);

			AliasSet r = (AliasSet) valueProcessor.getResult();
			stmt.getLeftOp().apply(valueProcessor);

			AliasSet l = (AliasSet) valueProcessor.getResult();

			if (r != null && l != null) {
				l.unify(r, false);
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseEnterMonitorStmt(soot.jimple.EnterMonitorStmt)
		 */
		public void caseEnterMonitorStmt(final EnterMonitorStmt stmt) {
			stmt.getOp().apply(valueProcessor);
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseExitMonitorStmt(soot.jimple.ExitMonitorStmt)
		 */
		public void caseExitMonitorStmt(final ExitMonitorStmt stmt) {
			stmt.getOp().apply(valueProcessor);
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseIdentityStmt(soot.jimple.IdentityStmt)
		 */
		public void caseIdentityStmt(final IdentityStmt stmt) {
			stmt.getRightOp().apply(valueProcessor);

			AliasSet r = (AliasSet) valueProcessor.getResult();
			stmt.getLeftOp().apply(valueProcessor);

			AliasSet l = (AliasSet) valueProcessor.getResult();

			if (r != null && l != null) {
				l.unify(r, false);
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseInvokeStmt(soot.jimple.InvokeStmt)
		 */
		public void caseInvokeStmt(final InvokeStmt stmt) {
			stmt.getInvokeExpr().apply(valueProcessor);
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseReturnStmt(soot.jimple.ReturnStmt)
		 */
		public void caseReturnStmt(final ReturnStmt stmt) {
			Value v = stmt.getOp();
			v.apply(valueProcessor);

			AliasSet l = (AliasSet) valueProcessor.getResult();

			if (l != null) {
				methodCtxtCache.getReturnAS().unify(l, false);
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseThrowStmt(soot.jimple.ThrowStmt)
		 */
		public void caseThrowStmt(final ThrowStmt stmt) {
			stmt.getOp().apply(valueProcessor);
			methodCtxtCache.getThrownAS().unify((AliasSet) valueProcessor.getResult(), false);
		}
	}


	/**
	 * This class encapsulates the logic to process the expressions during escape analysis.  Alias sets are created as
	 * required.  The class relies on <code>AliasSet</code> to decide if alias set needs to be created for a type of value.
	 * 
	 * <p>
	 * The arguments to any of the overridden methods cannot be <code>null</code>.
	 * </p>
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	class ValueProcessor
	  extends AbstractJimpleValueSwitch {
		/**
		 * This provides the value to be associated with <code>accessed</code> field of any alias set created during
		 * processing.
		 */
		boolean accessed;

		/**
		 * Provides the alias set associated with the array element being referred.  All elements in a dimension of an array
		 * are abstracted by a single alias set.
		 *
		 * @see soot.jimple.RefSwitch#caseArrayRef(soot.jimple.ArrayRef)
		 */
		public void caseArrayRef(final ArrayRef v) {
			v.getBase().apply(this);

			AliasSet base = (AliasSet) getResult();
			AliasSet elt = base.getASForField(AliasSet.ARRAY_FIELD);

			if (elt == null) {
				elt = AliasSet.getASForType(v.getType());

				if (elt != null) {
					base.putASForField(AliasSet.ARRAY_FIELD, elt);
				}
			}

			if (elt != null) {
				elt.setAccessedTo(accessed);
			}
			setResult(elt);
		}

		/**
		 * @see soot.jimple.RefSwitch#caseInstanceFieldRef(soot.jimple.InstanceFieldRef)
		 */
		public void caseInstanceFieldRef(final InstanceFieldRef v) {
			v.getBase().apply(this);

			AliasSet base = (AliasSet) getResult();
			String fieldSig = v.getField().getSignature();
			AliasSet field = base.getASForField(fieldSig);

			if (field == null) {
				field = AliasSet.getASForType(v.getType());

				if (field != null) {
					base.putASForField(fieldSig, field);
				}
			}

			if (field != null) {
				field.setAccessedTo(accessed);
			}
			setResult(field);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseInterfaceInvokeExpr( soot.jimple.InterfaceInvokeExpr)
		 */
		public void caseInterfaceInvokeExpr(final InterfaceInvokeExpr v) {
			processInvokeExpr(v);
		}

		/**
		 * @see soot.jimple.JimpleValueSwitch#caseLocal(Local)
		 */
		public void caseLocal(final Local v) {
			AliasSet s = (AliasSet) localASsCache.get(v);

			if (s == null) {
				s = AliasSet.getASForType(v.getType());

				if (s != null) {
					localASsCache.put(v, s);
				}
			}

			if (s != null) {
				s.setAccessedTo(accessed);
			}
			setResult(s);
		}

		/**
		 * @see soot.jimple.RefSwitch#caseParameterRef( soot.jimple.ParameterRef)
		 */
		public void caseParameterRef(final ParameterRef v) {
			setResult(methodCtxtCache.getParamAS(v.getIndex()));
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseSpecialInvokeExpr( soot.jimple.SpecialInvokeExpr)
		 */
		public void caseSpecialInvokeExpr(final SpecialInvokeExpr v) {
			processInvokeExpr(v);
		}

		/**
		 * @see soot.jimple.RefSwitch#caseStaticFieldRef( soot.jimple.StaticFieldRef)
		 */
		public void caseStaticFieldRef(final StaticFieldRef v) {
			setResult(globalASs.get(v.getField().getSignature()));
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseStaticInvokeExpr( soot.jimple.StaticInvokeExpr)
		 */
		public void caseStaticInvokeExpr(final StaticInvokeExpr v) {
			processInvokeExpr(v);
		}

		/**
		 * @see soot.jimple.ConstantSwitch#caseStringConstant(soot.jimple.StringConstant)
		 */
		public void caseStringConstant(final StringConstant v) {
			setResult(AliasSet.getASForType(v.getType()));
		}

		/**
		 * @see soot.jimple.RefSwitch#caseThisRef(soot.jimple.ThisRef)
		 */
		public void caseThisRef(final ThisRef v) {
			setResult(methodCtxtCache.getThisAS());
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseVirtualInvokeExpr( soot.jimple.VirtualInvokeExpr)
		 */
		public void caseVirtualInvokeExpr(final VirtualInvokeExpr v) {
			processInvokeExpr(v);
		}

		/**
		 * Creates an alias set if <code>o</code> is of type <code>Value</code>.  It uses <code>AliasSet</code> to decide if
		 * the given type requires an alias set.  If not, <code>null</code> is provided   as the alias set.  This is also
		 * the  case when <code>o</code> is not of type <code>Value</code>.
		 *
		 * @param o is a piece of IR to be processed.
		 */
		public void defaultCase(final Object o) {
			if (o instanceof Value) {
				setResult(AliasSet.getASForType(((Value) o).getType()));
			} else {
				setResult(null);
			}
		}

		/**
		 * Processes invoke expressions/call-sites.
		 *
		 * @param v invocation expresison to be processed.
		 */
		private void processInvokeExpr(final InvokeExpr v) {
			Collection callees = new ArrayList();
			SootMethod caller = context.getCurrentMethod();
			SootMethod sm = v.getMethod();

			// fix up "return" alias set.
			AliasSet retAS = null;

			retAS = AliasSet.getASForType(sm.getReturnType());

			// fix up "this" alias set.
			AliasSet thisAS = null;

			if (!sm.isStatic()) {
				((InstanceInvokeExpr) v).getBase().apply(valueProcessor);
				thisAS = (AliasSet) valueProcessor.getResult();
			}

			// fix up arg alias sets.
			List argASs;

			if (sm.getParameterCount() == 0) {
				argASs = Collections.EMPTY_LIST;
			} else {
				argASs = new ArrayList();

				for (int i = 0; i < sm.getParameterCount(); i++) {
					Value val = v.getArg(i);
					Object temp = null;

					if (AliasSet.canHaveAliasSet(val.getType())) {
						v.getArg(i).apply(valueProcessor);
						temp = valueProcessor.getResult();
					}
					argASs.add(temp);
				}
			}

			// create a site-context of the given expression and store it into the associated site-context cache.
			MethodContext sc = new MethodContext(sm, thisAS, argASs, retAS, AliasSet.createAliasSet());
			scCache.put(new CallTriple(caller, context.getStmt(), v), sc);

			if (v instanceof StaticInvokeExpr || v instanceof SpecialInvokeExpr) {
				callees.add(sm);
			} else if (v instanceof InterfaceInvokeExpr || v instanceof VirtualInvokeExpr) {
				context.setProgramPoint(((InstanceInvokeExpr) v).getBaseBox());
				callees.addAll(cgi.getCallees(v, context));
			}

			for (Iterator i = callees.iterator(); i.hasNext();) {
				boolean unifyAll = false;
				SootMethod callee = (SootMethod) i.next();
				Triple triple = (Triple) method2Triple.get(callee);

				// This is needed when the system is not closed.
				if (triple == null) {
					continue;
				}

				if (callee.getName().equals("start")
					  && callee.getDeclaringClass().getName().equals("java.lang.Thread")
					  && callee.getReturnType() instanceof VoidType
					  && callee.getParameterCount() == 0) {
					// unify all parts of alias sets if "start" is being invoked.
					unifyAll = true;
				} else if (callee.getDeclaringClass().getName().equals("java.lang.Object")
					  && callee.getReturnType() instanceof VoidType
					  && callee.getParameterCount() == 0) {
					String calleeName = callee.getName();

					// set notifies/waits flags if this is wait/notify call. 
					if (calleeName.equals("notify") || calleeName.equals("notifyAll")) {
						thisAS.setNotifies();
					} else if (calleeName.equals("wait")) {
						thisAS.setWaits();
					}
				}

				// retrieve the method context of the callee
				MethodContext mc = (MethodContext) triple.getFirst();

				/*
				 * If the caller and callee occur in different SCCs clone the callee method context and then unify it with
				 * the site context.  If not, unify the method context with site-context as precision will be lost any which
				 * way.
				 */
				if (cfgAnalysis.notInSameSCC(caller, callee)) {
					try {
						mc = (MethodContext) mc.clone();
					} catch (CloneNotSupportedException e) {
						if (LOGGER.isErrorEnabled()) {
							LOGGER.error("Hell NO!  This should not happen.", e);
						}
					}
				}
				sc.unify(mc, unifyAll);
			}
			setResult(retAS);
		}
	}

	/**
	 * Checks if the given statement containing a <code>wait</code> invocation is ready dependent on the given statement
	 * containing <code>notify/All</code> invocation.
	 *
	 * @param wait is the statement containing <code>wait</code> invocation.
	 * @param waitMethod is the method in which <code>wait</code> occurs.
	 * @param notify is the statement containing <code>notify/All</code> invocation.
	 * @param notifyMethod is the method in which <code>notify</code> occurs.
	 *
	 * @return <code>true</code> if <code>wait</code> is ready dependent on <code>notify</code>; <code>false</code>,
	 * 		   otherwise.
	 *
	 * @throws IllegalArgumentException if either of the given statement was not processed in the analysis.
	 *
	 * @pre wait != null and waitMethod != null and notify != null and notifyMethod != null
	 */
	public boolean isReadyDependent(final InvokeStmt wait, final SootMethod waitMethod, final InvokeStmt notify,
		final SootMethod notifyMethod) {
		Triple trp1 = (Triple) method2Triple.get(waitMethod);

		if (trp1 == null) {
			throw new IllegalArgumentException(waitMethod + " was not processed.");
		}

		Triple trp2 = (Triple) method2Triple.get(notifyMethod);

		if (trp2 == null) {
			throw new IllegalArgumentException(notifyMethod + " was not processed.");
		}

		InvokeExpr wi = wait.getInvokeExpr();
		InvokeExpr ni = notify.getInvokeExpr();
		boolean result = false;

		if (wi instanceof VirtualInvokeExpr && ni instanceof VirtualInvokeExpr) {
			VirtualInvokeExpr wTemp = (VirtualInvokeExpr) wi;
			VirtualInvokeExpr nTemp = (VirtualInvokeExpr) ni;
			SootMethod wSM = wTemp.getMethod();
			SootMethod nSM = nTemp.getMethod();

			if (wSM.getDeclaringClass().getName().equals("java.lang.Object")
				  && wSM.getReturnType() instanceof VoidType
				  && wSM.getParameterCount() == 0
				  && wSM.getName().equals("wait")
				  && nSM.getDeclaringClass().getName().equals("java.lang.Object")
				  && nSM.getReturnType() instanceof VoidType
				  && nSM.getParameterCount() == 0
				  && (nSM.getName().equals("notify") || nSM.getName().equals("notifyAll"))) {
				AliasSet as1 = (AliasSet) ((Map) trp1.getSecond()).get(wTemp.getBase());
				AliasSet as2 = (AliasSet) ((Map) trp2.getSecond()).get(nTemp.getBase());
				result = as1.getEntity().equals(as2.getEntity());
			}
		}
		return result;
	}

	/**
	 * Checks if the given exit monitor expression is dependent on the given enter monitor expression.
	 *
	 * @param exitStmt is the statement in which exit monitor expression occurs.
	 * @param exitMethod in which <code>exitStmt</code> occurs.
	 * @param enterStmt is the statemetn in which enter monitor expression occurs.
	 * @param enterMethod in which <code>enterStmt</code> occurs.
	 *
	 * @return <code>true</code> if <code>exitStmt</code> is ready dependent on <code>enterStmt</code>; <code>false</code>,
	 * 		   otherwise.
	 *
	 * @throws IllegalArgumentException if either of the given statements were not processed.
	 *
	 * @pre exitStmt != null and exitMethod != null and enterStmt != null and enterMethod != null
	 */
	public boolean isReadyDependent(final ExitMonitorStmt exitStmt, final SootMethod exitMethod,
		final EnterMonitorStmt enterStmt, final SootMethod enterMethod) {
		Triple trp1 = (Triple) method2Triple.get(exitMethod);

		if (trp1 == null) {
			throw new IllegalArgumentException(exitMethod + " was not processed.");
		}

		Triple trp2 = (Triple) method2Triple.get(enterMethod);

		if (trp2 == null) {
			throw new IllegalArgumentException(enterMethod + " was not processed.");
		}

		return isShared(exitStmt.getOp(), exitMethod) && isShared(enterStmt.getOp(), enterMethod);
	}

	/**
	 * Checks if the object bound to the given value in the given method shared or escapes.
	 *
	 * @param v is the object value being checked for sharing.
	 * @param sm is the method in which <code>v</code> occurs.
	 *
	 * @return <code>true</code> if <code>v</code> is shared; <code>false</code>, otherwise.
	 *
	 * @pre v != null and sm != null
	 */
	public boolean isShared(final Value v, final SootMethod sm) {
		boolean result = true;

		try {
			// check if given value has an alias set and if so, check if the enclosing method executes only in threads created
			// allocation sites which are executed only once. 
			if (AliasSet.canHaveAliasSet(v.getType())
				  && CollectionUtils.intersection(threadAllocSitesMulti, tgi.getExecutionThreads(sm)).isEmpty()) {
				result = getAliasSetFor(v, sm).isShared();
			}
		} catch (RuntimeException e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("There is no information about " + v + " occurring in " + sm
					+ ".  So, providing pessimistic info (true).");
			}
		}
		return result;
	}

	/**
	 * Invoked by the controller when a <code>Value</code> is encountered.
	 *
	 * @param value that was encountered.
	 * @param contextParam in which <code>value</code> was encountered.
	 *
	 * @pre value != null and context != null
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#callback(Value, Context)
	 */
	public void callback(final Value value, final Context contextParam) {
		if (value instanceof NewExpr && cgi.isReachable(contextParam.getCurrentMethod())) {
			NewExpr e = (NewExpr) value;
			Object o = new NewExprTriple(contextParam.getCurrentMethod(), context.getStmt(), e);

			if (cfgAnalysis.checkForLoopEnclosedNewExpr(e, contextParam)) {
				threadAllocSitesMulti.add(o);
			} else {
				threadAllocSitesSingle.add(o);
			}
		}
	}

	/**
	 * Creates an alias set for the static fields.  This is the creation of  global alias sets in Ruf's algorithm.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#callback(SootField)
	 */
	public void callback(final SootField sf) {
		if (Modifier.isStatic(sf.getModifiers())) {
			AliasSet t = AliasSet.getASForType(sf.getType());

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
	public void callback(final SootMethod sm) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Update method2Triple for " + sm);
		}
		method2Triple.put(sm, new Triple(new MethodContext(sm), new HashMap(), new HashMap()));
	}

	/**
	 * Performs phase1 operation as mentioned in the technial report.  This should be called after the call graph information
	 * has been consolidated.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#consolidate()
	 */
	public void consolidate() {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: equivalence class based escape analysis consolidation");
		}

		Collection tassBak = new HashSet(threadAllocSitesSingle);

		// Mark any thread allocation site that will be executed multiple times via a loop or a SCC loop in the call graph
		// as creating multiple threads.
		for (Iterator i = tassBak.iterator(); i.hasNext();) {
			NewExprTriple trp = (NewExprTriple) i.next();
			SootMethod encloser = trp.getMethod();

			if (cfgAnalysis.executedMultipleTimes(encloser)) {
				threadAllocSitesSingle.remove(trp);
				threadAllocSitesMulti.add(trp);
			}
		}

		Context ctxt = new Context();
		Collection multiExecMethods = new HashSet();

		// Mark any thread allocation site that is reachable from methods executed in threads created at sites which
		// create more than one thread also as creating multiple threads. 
		for (Iterator i = threadAllocSitesMulti.iterator(); i.hasNext();) {
			NewExprTriple ntrp = (NewExprTriple) i.next();
			ctxt.setRootMethod(ntrp.getMethod());
			ctxt.setStmt(ntrp.getStmt());
			multiExecMethods.addAll(tgi.getExecutedMethods(ntrp.getExpr(), ctxt));
		}
		tassBak.clear();
		tassBak.addAll(threadAllocSitesSingle);

		// filter the thread allocation site sets based on multiExecMethods.
		for (Iterator i = tassBak.iterator(); i.hasNext();) {
			NewExprTriple trp = (NewExprTriple) i.next();
			SootMethod encloser = trp.getMethod();

			if (multiExecMethods.contains(encloser)) {
				threadAllocSitesSingle.remove(trp);
				threadAllocSitesMulti.add(trp);
			}
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: equivalence class based escape analysis consolidation");
		}
	}

	/**
	 * Executes phase 2 and 3 as mentioned in the technical report.  It processed each methods in the call-graph bottom-up
	 * propogating the  alias set information in a collective fashion. It then propogates the information top-down in the
	 * call-graph.
	 */
	public void execute() {
		SimpleNodeGraph sng = cgi.getCallGraph();
		Collection sccs = sng.getSCCs(false);
		WorkBag wb = new WorkBag(WorkBag.FIFO);

		// Phase 2: The SCCs are ordered bottom up. 
		for (Iterator i = sccs.iterator(); i.hasNext();) {
			List nodes = (List) i.next();

			for (Iterator j = nodes.iterator(); j.hasNext();) {
				SimpleNode node = (SimpleNode) j.next();
				SootMethod sm = (SootMethod) node._object;

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Processing " + sm);
				}

				if (!sm.isConcrete()) {
					LOGGER.warn("NO BODY: " + sm.getSignature());
					continue;
				}

				JimpleBody body = (JimpleBody) sm.retrieveActiveBody();
				Triple triple = (Triple) method2Triple.get(sm);

				/*
				 * NOTE: This is an kludge to fix an anamoly arising from closing open system.  However, this triple should
				 * have been created for each processed method in the callback method.
				 */
				if (triple == null) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("NO METHOD TRIPLE: " + sm.getSignature());
					}
					continue;
				}
				methodCtxtCache = (MethodContext) triple.getFirst();
				localASsCache = (Map) triple.getSecond();
				scCache = (Map) triple.getThird();
				context.setRootMethod(sm);

				wb.clear();

				BasicBlockGraph bbg = bbm.getBasicBlockGraph(sm);

				if (bbg == null) {
					bbg = bbm.getBasicBlockGraph(new CompleteUnitGraph(body));
				}
				wb.addAllWork(bbg.getHeads());

				Collection processed = new HashSet();

				if (sm.getName().equals("<init>")) {
					valueProcessor.accessed = false;
				} else {
					valueProcessor.accessed = true;
				}

				while (wb.hasWork()) {
					BasicBlock bb = (BasicBlock) wb.getWork();
					processed.add(bb);

					for (Iterator k = bb.getStmtsOf().iterator(); k.hasNext();) {
						Stmt stmt = (Stmt) k.next();
						context.setStmt(stmt);
						stmt.apply(stmtProcessor);
					}

					for (Iterator k = bb.getSuccsOf().iterator(); k.hasNext();) {
						Object o = k.next();

						if (!processed.contains(o)) {
							wb.addWork(o);
						}
					}
				}

				if (valueProcessor.accessed) {
					continue;
				}
				wb.clear();

				AliasSet aTemp = methodCtxtCache.getReturnAS();

				if (aTemp != null) {
					wb.addWork(aTemp);
				}
				wb.addWork(methodCtxtCache.getThrownAS());

				for (int k = sm.getParameterCount() - 1; k >= 0; k--) {
					aTemp = methodCtxtCache.getParamAS(k);

					if (aTemp != null) {
						wb.addWork(aTemp);
					}
				}
				processed.clear();

				while (wb.hasWork()) {
					AliasSet as = (AliasSet) ((AliasSet) wb.getWork()).find();

					if (processed.contains(as)) {
						continue;
					}
					processed.add(as);
					as.setAccessedTo(true);

					for (Iterator k = as.getFieldMap().values().iterator(); k.hasNext();) {
						AliasSet element = (AliasSet) k.next();

						if (!processed.contains(element.find())) {
							wb.addWork(element.find());
						}
					}
				}
			}
		}

		// Phase 3
		Collection processed = new HashSet();
		wb.addAllWork(cgi.getHeads());

		while (wb.hasWork()) {
			SootMethod caller = (SootMethod) wb.getWork();
			Collection callees = cgi.getCallees(caller);
			Triple triple = (Triple) method2Triple.get(caller);
			Map ctrp2sc = (Map) triple.getThird();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Top-down procesing : CALLER : " + caller);
			}

			for (Iterator i = callees.iterator(); i.hasNext();) {
				CallTriple ctrp = (CallTriple) i.next();
				SootMethod callee = ctrp.getMethod();

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Top-down processing : CALLEE : " + callee);
				}

				triple = (Triple) method2Triple.get(callee);

				/*
				 * NOTE: This is an anomaly which results from how an open system is closed.  Refer to MethodVariant.java for
				 * more info.
				 */
				if (triple == null) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("NO CALLEE TRIPLE: " + callee.getSignature());
					}
					continue;
				}

				MethodContext mc = (MethodContext) (triple.getFirst());
				CallTriple callerTrp = new CallTriple(caller, ctrp.getStmt(), ctrp.getExpr());
				MethodContext sc = (MethodContext) ctrp2sc.get(callerTrp);
				sc.propogateInfoFromTo(mc);

				if (!processed.contains(callee)) {
					processed.add(callee);
					wb.addWork(callee);
				}
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#hookup(ProcessingController)
	 */
	public void hookup(final ProcessingController ppc) {
		ppc.register(NewExpr.class, this);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#unhook(ProcessingController)
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregister(NewExpr.class, this);
	}

	/**
	 * Retrieves the alias set corresponding to the given value.
	 *
	 * @param v is the value for which the alias set is requested.
	 * @param sm is the method in which <code>v</code> occurs.
	 *
	 * @return the alias set corresponding to <code>v</code>.
	 *
	 * @throws IllegalArgumentException if <code>sm</code> was not analyzed.
	 *
	 * @pre v.isOclKindOf(Local) or v.isOclKindOf(ArrayRef) or v.isOclKindOf(FieldRef) or v.isOclKindOf(ArrayRef) or
	 * 		v.isOclKindOf(InstanceFieldRef) implies v.getBase().isOclKindOf(Local)
	 */
	private AliasSet getAliasSetFor(final Value v, final SootMethod sm) {
		Triple trp = (Triple) method2Triple.get(sm);

		if (trp == null) {
			throw new IllegalArgumentException("Method " + sm + " was not analyzed.");
		}

		Map local2AS = (Map) trp.getSecond();
		AliasSet result = null;

		if (AliasSet.canHaveAliasSet(v.getType())) {
			if (v instanceof InstanceFieldRef) {
				InstanceFieldRef i = (InstanceFieldRef) v;
				result = ((AliasSet) local2AS.get(i.getBase())).getASForField(((FieldRef) v).getField().getSignature());
			} else if (v instanceof StaticFieldRef) {
				result = (AliasSet) globalASs.get(((FieldRef) v).getField().getSignature());
			} else if (v instanceof ArrayRef) {
				ArrayRef a = (ArrayRef) v;
				result = ((AliasSet) local2AS.get(a.getBase())).getASForField(AliasSet.ARRAY_FIELD);
			} else if (v instanceof Local) {
				result = (AliasSet) local2AS.get(v);
			}
		}
		return result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/08/21 01:24:25  venku
    - Renamed src-escape to src-concurrency to as to group all concurrency
      issue related analyses into a package.
    - Renamed escape package to concurrency.escape.
    - Renamed EquivalenceClassBasedAnalysis to EquivalenceClassBasedEscapeAnalysis.
   Revision 1.3  2003/08/11 08:49:34  venku
   Javadoc documentation errors were fixed.
   Some classes were documented.
   Revision 1.2  2003/08/11 06:29:07  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.1  2003/08/07 06:39:07  venku
   Major:
    - Moved the package under indus umbrella.
   Minor:
    - changes to accomodate ripple effect from support package.
 */
