
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

import soot.Local;
import soot.Modifier;
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

import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;
import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.staticanalyses.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.staticanalyses.interfaces.IThreadGraphInfo;
import edu.ksu.cis.indus.staticanalyses.processing.AbstractValueAnalyzerBasedProcessor;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraph;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraphMgr;
import edu.ksu.cis.indus.staticanalyses.support.FIFOWorkBag;
import edu.ksu.cis.indus.staticanalyses.support.IWorkBag;
import edu.ksu.cis.indus.staticanalyses.support.SimpleNodeGraph;
import edu.ksu.cis.indus.staticanalyses.support.SimpleNodeGraph.SimpleNode;
import edu.ksu.cis.indus.staticanalyses.support.Triple;

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
  extends AbstractValueAnalyzerBasedProcessor {
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
	 * This maps a site context to a corresponding to method context.  This is used to collect contexts corresponding to
	 * start call-sites for delayed processing.
	 */
	private Map delayedSet = new HashMap();

	/**
	 * Creates a new EquivalenceClassBasedEscapeAnalysis object.
	 *
	 * @param callgraph provides call-graph information.
	 * @param tgiPrm provides thread-graph information.
	 * @param basicBlockGraphMgr provides basic block graphs required by this analysis.
	 *
	 * @pre scene != null and callgraph != null and tgi != null
	 */
	public EquivalenceClassBasedEscapeAnalysis(final ICallGraphInfo callgraph, final IThreadGraphInfo tgiPrm,
		final BasicBlockGraphMgr basicBlockGraphMgr) {
		this.cgi = callgraph;
		this.tgi = tgiPrm;

		globalASs = new HashMap();
		method2Triple = new HashMap();
		stmtProcessor = new StmtProcessor();
		valueProcessor = new ValueProcessor();
		bbm = basicBlockGraphMgr;
		context = new Context();
		cfgAnalysis = new CFGAnalysis(cgi, bbm);
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
			boolean temp = valueProcessor.read;
			valueProcessor.read = true;
			stmt.getRightOp().apply(valueProcessor);
			valueProcessor.read = temp;

			AliasSet r = (AliasSet) valueProcessor.getResult();
			temp = valueProcessor.read;
			valueProcessor.read = false;
			stmt.getLeftOp().apply(valueProcessor);
			valueProcessor.read = temp;

			AliasSet l = (AliasSet) valueProcessor.getResult();

			if ((r != null) && (l != null)) {
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
			boolean temp = valueProcessor.read;
			valueProcessor.read = true;
			stmt.getRightOp().apply(valueProcessor);
			valueProcessor.read = temp;

			AliasSet r = (AliasSet) valueProcessor.getResult();
			temp = valueProcessor.read;
			valueProcessor.read = false;
			stmt.getLeftOp().apply(valueProcessor);
			valueProcessor.read = temp;

			AliasSet l = (AliasSet) valueProcessor.getResult();

			if ((r != null) && (l != null)) {
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
		 * This indicates if the value should be marked as being read or written.  <code>true</code> indicates that the
		 * values should be marked as being read from.  <code>false</code> indicates that the values should be marked as
		 * being written into.
		 */
		boolean read = true;

		/**
		 * Provides the alias set associated with the array element being referred.  All elements in a dimension of an array
		 * are abstracted by a single alias set.
		 *
		 * @see soot.jimple.RefSwitch#caseArrayRef(soot.jimple.ArrayRef)
		 */
		public void caseArrayRef(final ArrayRef v) {
			boolean temp = read;
			read = true;
			v.getBase().apply(this);
			read = temp;

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
				setReadOrWritten(elt);
			}

			setResult(elt);
		}

		/**
		 * @see soot.jimple.RefSwitch#caseInstanceFieldRef(soot.jimple.InstanceFieldRef)
		 */
		public void caseInstanceFieldRef(final InstanceFieldRef v) {
			boolean temp = read;
			read = true;
			v.getBase().apply(this);
			read = temp;

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
				setReadOrWritten(field);
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
				setReadOrWritten(s);
			}

			setResult(s);
		}

		/**
		 * @see soot.jimple.RefSwitch#caseParameterRef( soot.jimple.ParameterRef)
		 */
		public void caseParameterRef(final ParameterRef v) {
			AliasSet as = methodCtxtCache.getParamAS(v.getIndex());
			setReadOrWritten(as);
			setResult(as);
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
			AliasSet as = methodCtxtCache.getThisAS();
			setReadOrWritten(as);
			setResult(as);
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
		 * Helper method to mark the alias set as read or written.
		 *
		 * @param as is the alias set to be marked.
		 */
		private void setReadOrWritten(final AliasSet as) {
			if (as != null) {
				if (read) {
					as.setRead();
				} else {
					as.setWritten();
				}
			}
		}

		/**
		 * Processes invoke expressions/call-sites.
		 *
		 * @param v invocation expresison to be processed.
		 *
		 * @throws RuntimeException when a cloning fails.  However, this should not happen.
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
			int paramCount = sm.getParameterCount();

			if (paramCount == 0) {
				argASs = Collections.EMPTY_LIST;
			} else {
				argASs = new ArrayList();

				for (int i = 0; i < paramCount; i++) {
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
				SootMethod callee = (SootMethod) i.next();
				Triple triple = (Triple) method2Triple.get(callee);

				// This is needed when the system is not closed.
				if (triple == null) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("NO TRIPLE.  May be due to open system. - " + callee.getSignature());
					}
					continue;
				}

				/*
				 * If the start sites are processed first then the alias sets are marked as accesssed.
				 * However, as the sharing information is only changed at "start" call-sites, the marking of the alias sets
				 * as accessed will not affect sharability, which is incorrect.
				 *
				 * As a solution, we collect all method contexts, both site- and method-contexts, for all start call-sites in
				 * the caller to be unified after all other statements in the caller have been processed.
				 */
				boolean delayUnification = false;

				if (callee.getName().equals("start")
					  && callee.getDeclaringClass().getName().equals("java.lang.Thread")
					  && callee.getReturnType() instanceof VoidType
					  && callee.getParameterCount() == 0) {
					// unify all parts of alias sets if "start" is being invoked.
					delayUnification = true;
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
				 * If the caller and callee occur in different SCCs then clone the callee method context and then unify it
				 * with the site context.  If not, unify the method context with site-context as precision will be lost any
				 * which way.
				 */
				if (cfgAnalysis.notInSameSCC(caller, callee)) {
					try {
						mc = (MethodContext) mc.clone();
					} catch (CloneNotSupportedException e) {
						LOGGER.error("Hell NO!  This should not happen.", e);
						throw new RuntimeException(e);
					}
				}

				if (delayUnification) {
					addToDelayedUnificationSet(sc, mc);
				} else {
					sc.unify(mc, false);
				}
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
				  && (wSM.getParameterCount() == 0)
				  && wSM.getName().equals("wait")
				  && nSM.getDeclaringClass().getName().equals("java.lang.Object")
				  && nSM.getReturnType() instanceof VoidType
				  && (nSM.getParameterCount() == 0)
				  && (nSM.getName().equals("notify") || nSM.getName().equals("notifyAll"))) {
				AliasSet as1 = (AliasSet) ((Map) trp1.getSecond()).get(wTemp.getBase());
				AliasSet as2 = (AliasSet) ((Map) trp2.getSecond()).get(nTemp.getBase());

				if ((as1.getReadyEntity() != null) && (as2.getReadyEntity() != null)) {
					result = as1.getReadyEntity() == as2.getReadyEntity();
				} else {
					/*
					 * This is the case where a start site has wait and notify called on a reference.
					 * In such cases, wait and notify fields are set on the alias set but there is not alias set
					 * with set values to trigger the change of Entity field.
					 * Only if the start site is loop enclosed should these cases flag dependency by setting Entity.
					 */
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn(
							"There are wait()s and/or notify()s in this program without corresponding notify()s and/or "
							+ "wait()s that occur in different threads - " + wait + " " + notify);
					}
				}
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

		return escapes(exitStmt.getOp(), exitMethod) && escapes(enterStmt.getOp(), enterMethod);
	}

	/**
	 * Creates an alias set for the static fields.  This is the creation of  global alias sets in Ruf's algorithm.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzerBasedProcessor#callback(SootField)
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
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzerBasedProcessor#callback(SootMethod)
	 */
	public void callback(final SootMethod sm) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Update method2Triple for " + sm);
		}

		method2Triple.put(sm, new Triple(new MethodContext(sm), new HashMap(), new HashMap()));
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
	public boolean escapes(final Value v, final SootMethod sm) {
		boolean result = true;

		try {
			// check if given value has an alias set and if so, check if the enclosing method executes only in threads created
			// allocation sites which are executed only once. 
			if (AliasSet.canHaveAliasSet(v.getType())) {
				// Ruf's analysis mandates that the allocation sites that are executed multiple times pollute escape 
				// information. But this is untrue, as all the data that can be shared across threads have been exposed and 
				// marked rightly so at allocation sites.  By equivalence class-based unification guarantees that the 
				// corresponding alias set at the caller side is unified atleast twice in case these threads are started at 
				// different sites.  In case the threads are started at the same site, then the processing of call-site during
				// phase 2 (bottom-up) will ensure that the alias sets are unified with themselves.  Hence, the program 
				// structure and the language semantics along with the rules above ensure that the escape information is 
				// polluted (pessimistic) only when necessary.
				result = getAliasSetFor(v, sm).escapes();
			} else {
				result = false;
			}
		} catch (RuntimeException e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("There is no information about " + v + " occurring in " + sm
					+ ".  So, providing pessimistic info (true).", e);
			}
		}

		return result;
	}

	/**
	 * Executes phase 2 and 3 as mentioned in the technical report.  It processed each methods in the call-graph bottom-up
	 * propogating the  alias set information in a collective fashion. It then propogates the information top-down in the
	 * call-graph.
	 */
	public void execute() {
		SimpleNodeGraph sng = cgi.getCallGraph();
		Collection sccs = sng.getSCCs(false);
		IWorkBag wb = new FIFOWorkBag();
		Collection processed = new HashSet();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: Equivalence Class-based and Symbol-based Escape Analysis");
		}

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

				if (sm.getName().equals("<init>")) {
					valueProcessor.accessed = false;
				} else {
					valueProcessor.accessed = true;
				}

				BasicBlockGraph bbg = bbm.getBasicBlockGraph(sm);
				wb.clear();
				processed.clear();
				wb.addAllWork(bbg.getHeads());

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

				// unify the contexts at start call-sites.
				performDelayedUnification();
			}
		}

		// Phase 3
		processed.clear();
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

				// It would suffice to unify the site context with it self in the case of loop enclosure
				// as this is more semantically close to what happens during execution.
				if (callee.getName().equals("start")
					  && callee.getDeclaringClass().getName().equals("java.lang.Thread")
					  && callee.getReturnType() instanceof VoidType
					  && callee.getParameterCount() == 0
					  && cfgAnalysis.executedMultipleTimes(ctrp.getStmt(), caller)) {
					sc.selfUnify(true);
				}
				sc.propogateInfoFromTo(mc);

				if (!processed.contains(callee)) {
					processed.add(callee);
					wb.addWork(callee);
				}
			}
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: Equivalence Class-based and Symbol-based Escape Analysis");
		}
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IProcessor#hookup(ProcessingController)
	 */
	public void hookup(final ProcessingController ppc) {
		ppc.register(this);
	}

	/**
	 * Reset internal data structures.
	 */
	public void reset() {
		globalASs.clear();
		method2Triple.clear();
	}

	/**
	 * Checks if the given values are shared.  This is more stricter than escape-ness.  This requires that the values be
	 * escaping as well as represent a common entity.
	 *
	 * @param v1 is one of the value in the check.
	 * @param sm1 is the method in which <code>v1</code> occurs.
	 * @param v2 is the other value in the check.
	 * @param sm2 is the method in which <code>v2</code> occurs.
	 *
	 * @return <code>true</code> if the given values are indeed shared across threads; <code>false</code>, otherwise.
	 *
	 * @pre v1 != null and sm1 != null and v2 != null and sm2 != null
	 */
	public boolean shared(final Value v1, final SootMethod sm1, final Value v2, final SootMethod sm2) {
		boolean result = escapes(v1, sm1) && escapes(v2, sm2);

		if (result) {
			try {
				// Ruf's analysis mandates that the allocation sites that are executed multiple times pollute escape 
				// information. But this is untrue, as all the data that can be shared across threads have been exposed and 
				// marked rightly so at allocation sites.  By equivalence class-based unification guarantees that the 
				// corresponding alias set at the caller side is unified atleast twice in case these threads are started at 
				// different sites.  In case the threads are started at the same site, then the processing of call-site during
				// phase 2 (bottom-up) will ensure that the alias sets are unified with themselves.  Hence, the program 
				// structure and the language semantics along with the rules above ensure that the escape information is 
				// polluted (pessimistic) only when necessary.
				Collection o1 = getAliasSetFor(v1, sm1).getShareEntities();
				Collection o2 = getAliasSetFor(v2, sm2).getShareEntities();
				result = (o1 != null) && (o2 != null) && !(CollectionUtils.intersection(o1, o2).isEmpty());
			} catch (RuntimeException e) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("There is no information about " + v1 + "/" + v2 + " occurring in " + sm1 + "/" + sm2
						+ ".  So, providing pessimistic info (true).", e);
				}
			}
		}

		return result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IProcessor#unhook(ProcessingController)
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregister(this);
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
	AliasSet getAliasSetFor(final Value v, final SootMethod sm) {
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

	/**
	 * Checks if the given value in the given method is global.
	 *
	 * @param v is the value to be checked for globalness.
	 * @param sm is the method in which <code>v</code> occurs.
	 *
	 * @return <code>true</code> if <code>v</code> is marked as global; <code>false</code>, otherwise.
	 *
	 * @pre v != null and sm != null
	 */
	boolean isGlobal(final Value v, final SootMethod sm) {
		boolean result = true;

		try {
			if (AliasSet.canHaveAliasSet(v.getType())) {
				result = getAliasSetFor(v, sm).isGlobal();
			} else {
				result = false;
			}
		} catch (RuntimeException e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("There is no information about " + v + " occurring in " + sm
					+ ".  So, providing pessimistic info (true).", e);
			}
		}

		return result;
	}

	/**
	 * Adds the given contexts to the set of contexts to be processed later on.  This is called by the tree walker.
	 *
	 * @param siteContext is the context associated with the site.
	 * @param methodContext is the context associated with the method.
	 *
	 * @pre siteContext != null and methodContext != null
	 * @post delayedSet.get(siteContext) == methodContext
	 */
	void addToDelayedUnificationSet(final MethodContext siteContext, final MethodContext methodContext) {
		delayedSet.put(siteContext, methodContext);
	}

	/**
	 * Performs the unification of contexts occurring at start call-sites as collected via
	 * <code>addToDelayedUnificationSet</code>.
	 *
	 * @post delayedSet.isEmpty()
	 */
	private void performDelayedUnification() {
		for (Iterator i = delayedSet.entrySet().iterator(); i.hasNext();) {
			Map.Entry element = (Map.Entry) i.next();
			((MethodContext) element.getKey()).unify((MethodContext) element.getValue(), true);
		}
		delayedSet.clear();
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.26  2003/11/07 12:20:36  venku
   - added information logging.

   Revision 1.25  2003/11/06 05:59:17  venku
   - coding convention.
   Revision 1.24  2003/11/06 05:15:07  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.23  2003/11/05 09:28:34  venku
   - ripple effect of splitting IWorkBag.
   Revision 1.22  2003/11/02 22:09:57  venku
   - changed the signature of the constructor of
     EquivalenceClassBasedEscapeAnalysis.
   Revision 1.21  2003/11/01 23:50:00  venku
   - documentation.
   Revision 1.20  2003/10/31 01:02:04  venku
   - added code for extracting data for CC04 paper.
   Revision 1.19  2003/10/21 04:29:23  venku
   - subtle bug emanated from the order in which the
     statements were processed.  As a fix, all start call-sites
     in a method are processed after all statements of the
      method have been processed.
   Revision 1.18  2003/10/09 00:17:40  venku
   - changes to instrumetn statistics numbers.
   Revision 1.17  2003/10/05 16:22:25  venku
   - Interference dependence is now symbol based.
   - Both interference and ready dependence consider
     loop information in a more sound manner.
   - ripple effect of the above.
   Revision 1.16  2003/10/05 06:31:35  venku
   - Things work.  The bug was the order in which the
     parameter alias sets were being accessed.  FIXED.
   Revision 1.15  2003/10/04 22:53:45  venku
   - backup commit.
   Revision 1.14  2003/09/29 14:54:46  venku
   - don't use "use-orignal-names" option with Jimple.
     The variables referring to objects need to be unique if the
     results of the analyses should be meaningful.
   Revision 1.13  2003/09/29 06:36:31  venku
   - added reset() method.
   Revision 1.12  2003/09/28 06:20:39  venku
   - made the core independent of hard code used to create unit graphs.
     The core depends on the environment to provide a factory that creates
     these unit graphs.
   Revision 1.11  2003/09/28 03:17:13  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.10  2003/09/27 01:27:46  venku
   - documentation.
   Revision 1.9  2003/09/12 08:13:40  venku
   - added todo item.
   Revision 1.8  2003/09/08 02:24:27  venku
   - uses IThreadGraphInfo to calculate multi executed thread
     allocation site.
   Revision 1.7  2003/09/01 12:01:30  venku
   Major:
   - Ready dependence info in ECBA was flaky as it did not consider
     impact of multiple call sites with contradicting wait/notify use of
     the primary.  FIXED.
   Revision 1.6  2003/08/27 12:40:35  venku
   It is possible that in ill balanced wait/notify lead to a situation
   where there are no entities to match them, hence, we got a
   NullPointerException.  FIXED.
   It now flags a log error indicating the source has anamolies.
   Revision 1.5  2003/08/25 11:58:43  venku
   Deleted a debug statement.
   Revision 1.4  2003/08/24 12:04:32  venku
   Removed occursInCycle() method from DirectedGraph.
   Installed occursInCycle() method in CFGAnalysis.
   Converted performTopologicalsort() and getFinishTimes() into instance methods.
   Ripple effect of the above changes.
   Revision 1.3  2003/08/24 06:06:34  venku
   logging added.
   Revision 1.2  2003/08/21 03:56:44  venku
   Formatting.
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
