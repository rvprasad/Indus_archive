
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

import edu.ksu.cis.indus.common.graph.BasicBlockGraph;
import edu.ksu.cis.indus.common.graph.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.common.graph.BasicBlockGraphMgr;
import edu.ksu.cis.indus.common.datastructures.FIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.Triple;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;

import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.processing.AbstractValueAnalyzerBasedProcessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.Local;
import soot.Modifier;
import soot.SootClass;
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


/**
 * This class represents Equivalence Class-based analysis to calculate escape information of objects.  Escape information is
 * provided in terms of share-ability of the object bound to a given value in a given method.  This analysis is overloaded
 * as a symbolic analysis to calculate information that can be used to prune ready-dependence edges.
 * 
 * <p>
 * This analysis requires <code>local splitting</code> option of Soot framework to be enabled while generating the Jimple for
 * the system being analyzed.
 * </p>
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
public final class EquivalenceClassBasedEscapeAnalysis
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
	 * This is the collection of notify methods in the system that can lead to ready dependences.
	 */
	private Collection notifyMethods = new ArrayList();

	/**
	 * This is the collection of wait methods in the system that can lead to ready dependences.
	 */
	private Collection waitMethods = new ArrayList();

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
	final class StmtProcessor
	  extends AbstractStmtSwitch {
		/**
		 * @see soot.jimple.StmtSwitch#caseAssignStmt(soot.jimple.AssignStmt)
		 */
		public void caseAssignStmt(final AssignStmt stmt) {
			boolean _temp = valueProcessor.read;
			valueProcessor.read = true;
			valueProcessor.process(stmt.getRightOp());
			valueProcessor.read = _temp;

			final AliasSet _r = (AliasSet) valueProcessor.getResult();
			_temp = valueProcessor.read;
			valueProcessor.read = false;
			valueProcessor.process(stmt.getLeftOp());
			valueProcessor.read = _temp;

			final AliasSet _l = (AliasSet) valueProcessor.getResult();

			if ((_r != null) && (_l != null)) {
				_l.unify(_r, false);
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseEnterMonitorStmt(soot.jimple.EnterMonitorStmt)
		 */
		public void caseEnterMonitorStmt(final EnterMonitorStmt stmt) {
			valueProcessor.process(stmt.getOp());
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseExitMonitorStmt(soot.jimple.ExitMonitorStmt)
		 */
		public void caseExitMonitorStmt(final ExitMonitorStmt stmt) {
			valueProcessor.process(stmt.getOp());
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseIdentityStmt(soot.jimple.IdentityStmt)
		 */
		public void caseIdentityStmt(final IdentityStmt stmt) {
			boolean _temp = valueProcessor.read;
			valueProcessor.read = true;
			valueProcessor.process(stmt.getRightOp());
			valueProcessor.read = _temp;

			final AliasSet _r = (AliasSet) valueProcessor.getResult();
			_temp = valueProcessor.read;
			valueProcessor.read = false;
			valueProcessor.process(stmt.getLeftOp());
			valueProcessor.read = _temp;

			final AliasSet _l = (AliasSet) valueProcessor.getResult();

			if ((_r != null) && (_l != null)) {
				_l.unify(_r, false);
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseInvokeStmt(soot.jimple.InvokeStmt)
		 */
		public void caseInvokeStmt(final InvokeStmt stmt) {
			valueProcessor.process(stmt.getInvokeExpr());
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseReturnStmt(soot.jimple.ReturnStmt)
		 */
		public void caseReturnStmt(final ReturnStmt stmt) {
			valueProcessor.process(stmt.getOp());

			final AliasSet _l = (AliasSet) valueProcessor.getResult();

			if (_l != null) {
				methodCtxtCache.getReturnAS().unify(_l, false);
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseThrowStmt(soot.jimple.ThrowStmt)
		 */
		public void caseThrowStmt(final ThrowStmt stmt) {
			valueProcessor.process(stmt.getOp());
			methodCtxtCache.getThrownAS().unify((AliasSet) valueProcessor.getResult(), false);
		}

		/**
		 * Processes the given statement.
		 *
		 * @param stmt to be processed.
		 *
		 * @pre stmt != null
		 */
		void process(final Stmt stmt) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Processing statement: " + stmt);
			}
			stmt.apply(this);
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
	final class ValueProcessor
	  extends AbstractJimpleValueSwitch {
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
			boolean _temp = read;
			read = true;
			v.getBase().apply(this);
			read = _temp;

			final AliasSet _base = (AliasSet) getResult();
			AliasSet _elt = _base.getASForField(AliasSet.ARRAY_FIELD);

			if (_elt == null) {
				_elt = AliasSet.getASForType(v.getType());

				if (_elt != null) {
					_base.putASForField(AliasSet.ARRAY_FIELD, _elt);
				}
			}

			if (_elt != null) {
				_elt.setAccessedTo(true);
				setReadOrWritten(_elt);
			}

			setResult(_elt);
		}

		/**
		 * @see soot.jimple.RefSwitch#caseInstanceFieldRef(soot.jimple.InstanceFieldRef)
		 */
		public void caseInstanceFieldRef(final InstanceFieldRef v) {
			boolean _temp = read;
			read = true;
			v.getBase().apply(this);
			read = _temp;

			final AliasSet _base = (AliasSet) getResult();
			final String _fieldSig = v.getField().getSignature();
			AliasSet _field = _base.getASForField(_fieldSig);

			if (_field == null) {
				_field = AliasSet.getASForType(v.getType());

				if (_field != null) {
					_base.putASForField(_fieldSig, _field);
				}
			}

			if (_field != null) {
				_field.setAccessedTo(true);
				setReadOrWritten(_field);
			}

			setResult(_field);
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
			AliasSet _s = (AliasSet) localASsCache.get(v);

			if (_s == null) {
				_s = AliasSet.getASForType(v.getType());

				if (_s != null) {
					localASsCache.put(v, _s);
				}
			}

			if (_s != null) {
				_s.setAccessedTo(true);
				setReadOrWritten(_s);
			}

			setResult(_s);
		}

		/**
		 * @see soot.jimple.RefSwitch#caseParameterRef( soot.jimple.ParameterRef)
		 */
		public void caseParameterRef(final ParameterRef v) {
			final AliasSet _as = methodCtxtCache.getParamAS(v.getIndex());
			setReadOrWritten(_as);
			setResult(_as);
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
			final AliasSet _as = methodCtxtCache.getThisAS();
			setReadOrWritten(_as);
			setResult(_as);
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
		 * Process the given value/expression.
		 *
		 * @param value to be processed.
		 *
		 * @pre value != null
		 */
		void process(final Value value) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Processing value: " + value);
			}
			value.apply(this);
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
		 * Process the arguments of the invoke expression.
		 *
		 * @param v is the invoke expressions containing the arguments to be processed.
		 * @param method being invoked at <code>v</code>.
		 *
		 * @return the list of alias sets corresponding to the arguments.
		 *
		 * @pre v != null and method != null
		 * @post result != null and result.oclIsKindOf(Sequence(AliasSet))
		 */
		private List processArguments(final InvokeExpr v, final SootMethod method) {
			// fix up arg alias sets.
			final List _argASs;
			final int _paramCount = method.getParameterCount();

			if (_paramCount == 0) {
				_argASs = Collections.EMPTY_LIST;
			} else {
				_argASs = new ArrayList();

				for (int _i = 0; _i < _paramCount; _i++) {
					final Value _val = v.getArg(_i);
					Object _temp = null;

					if (AliasSet.canHaveAliasSet(_val.getType())) {
						v.getArg(_i).apply(valueProcessor);
						_temp = valueProcessor.getResult();
					}

					_argASs.add(_temp);
				}
			}
			return _argASs;
		}

		/**
		 * Process the callees in a caller.
		 *
		 * @param callees is the collection of methods called.
		 * @param caller is the calling method.
		 * @param primaryAliasSet is the alias set of the primary in the invocation expression.
		 * @param methodContext corresponding to the invocation expression.
		 *
		 * @throws RuntimeException when cloning fails.
		 *
		 * @pre callees != null and caller != null and primaryAliasSet != null and methodContext != null
		 * @pre callees.oclIsKindOf(Collection(SootMethod))
		 */
		private void processCallees(final Collection callees, final SootMethod caller, final AliasSet primaryAliasSet,
			final MethodContext methodContext) {
			for (final Iterator _i = callees.iterator(); _i.hasNext();) {
				final SootMethod _callee = (SootMethod) _i.next();
				final Triple _triple = (Triple) method2Triple.get(_callee);

				// This is needed when the system is not closed.
				if (_triple == null) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("NO TRIPLE.  May be due to open system. - " + _callee.getSignature());
					}
					continue;
				}

				final boolean _delayUnification = processNotifyStartWait(primaryAliasSet, _callee);

				// retrieve the method context of the callee
				MethodContext _mc = (MethodContext) _triple.getFirst();

				/*
				 * If the caller and callee occur in different SCCs then clone the callee method context and then unify it
				 * with the site context.  If not, unify the method context with site-context as precision will be lost any
				 * which way.
				 */
				if (cfgAnalysis.notInSameSCC(caller, _callee)) {
					try {
						_mc = (MethodContext) _mc.clone();
					} catch (CloneNotSupportedException _e) {
						LOGGER.error("Hell NO!  This should not happen.", _e);
						throw new RuntimeException(_e);
					}
				}

				if (_delayUnification) {
					addToDelayedUnificationSet(methodContext, _mc);
				} else {
					methodContext.unify(_mc, false);
				}
			}
		}

		/**
		 * Processes invoke expressions/call-sites.
		 *
		 * @param expr invocation expresison to be processed.
		 */
		private void processInvokeExpr(final InvokeExpr expr) {
			final Collection _callees = new ArrayList();
			final SootMethod _caller = context.getCurrentMethod();
			final SootMethod _sm = expr.getMethod();

			// fix up "return" alias set.
			AliasSet _retAS = null;

			_retAS = AliasSet.getASForType(_sm.getReturnType());

			// fix up "primary" alias set.
			AliasSet _primaryAS = null;

			if (!_sm.isStatic()) {
				((InstanceInvokeExpr) expr).getBase().apply(valueProcessor);
				_primaryAS = (AliasSet) valueProcessor.getResult();
			}

			final List _argASs = processArguments(expr, _sm);

			// create a site-context of the given expression and store it into the associated site-context cache.
			final MethodContext _sc = new MethodContext(_sm, _primaryAS, _argASs, _retAS, AliasSet.createAliasSet());
			scCache.put(new CallTriple(_caller, context.getStmt(), expr), _sc);

			if (expr instanceof StaticInvokeExpr) {
				_callees.add(_sm);
			} else if (expr instanceof InterfaceInvokeExpr
				  || expr instanceof VirtualInvokeExpr
				  || expr instanceof SpecialInvokeExpr) {
				context.setProgramPoint(((InstanceInvokeExpr) expr).getBaseBox());
				_callees.addAll(cgi.getCallees(expr, context));
			}

			processCallees(_callees, _caller, _primaryAS, _sc);

			setResult(_retAS);
		}

		/**
		 * Process the called method for <code>start(), notify(), nofityAll(),</code> and variants of <code>wait</code>
		 * methods.
		 *
		 * @param primaryAliasSet is the alias set corresponding to the primary of the invocation expression.
		 * @param callee being called.
		 *
		 * @return <code>true</code> when the called method is <code>java.lang.Thread.start()</code>.
		 *
		 * @pre primaryAliasSet != null and callee != null
		 */
		private boolean processNotifyStartWait(final AliasSet primaryAliasSet, final SootMethod callee) {
			boolean _delayUnification = false;

			if (callee.getName().equals("start")
				  && callee.getDeclaringClass().getName().equals("java.lang.Thread")
				  && callee.getReturnType() instanceof VoidType
				  && callee.getParameterCount() == 0) {
				// unify all parts of alias sets if "start" is being invoked.
				_delayUnification = true;
			} else if (callee.getDeclaringClass().getName().equals("java.lang.Object")
				  && callee.getReturnType() instanceof VoidType
				  && callee.getParameterCount() == 0) {
				final String _calleeName = callee.getName();

				// set notifies/waits flags if this is wait/notify call. 
				if (_calleeName.equals("notify") || _calleeName.equals("notifyAll")) {
					primaryAliasSet.setNotifies();
				} else if (_calleeName.equals("wait")) {
					primaryAliasSet.setWaits();
				}
			}
			return _delayUnification;
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
		final Triple _trp1 = (Triple) method2Triple.get(waitMethod);

		if (_trp1 == null) {
			throw new IllegalArgumentException(waitMethod + " was not processed.");
		}

		final Triple _trp2 = (Triple) method2Triple.get(notifyMethod);

		if (_trp2 == null) {
			throw new IllegalArgumentException(notifyMethod + " was not processed.");
		}

		final InvokeExpr _wi = wait.getInvokeExpr();
		final InvokeExpr _ni = notify.getInvokeExpr();
		boolean _result = false;

		if (_wi instanceof VirtualInvokeExpr && _ni instanceof VirtualInvokeExpr) {
			final VirtualInvokeExpr _wTemp = (VirtualInvokeExpr) _wi;
			final VirtualInvokeExpr _nTemp = (VirtualInvokeExpr) _ni;
			final SootMethod _wSM = _wTemp.getMethod();
			final SootMethod _nSM = _nTemp.getMethod();

			if (waitMethods.contains(_wSM) && notifyMethods.contains(_nSM)) {
				final AliasSet _as1 = (AliasSet) ((Map) _trp1.getSecond()).get(_wTemp.getBase());
				final AliasSet _as2 = (AliasSet) ((Map) _trp2.getSecond()).get(_nTemp.getBase());

				if ((_as1.getReadyEntity() != null) && (_as2.getReadyEntity() != null)) {
					_result = _as1.getReadyEntity() == _as2.getReadyEntity();
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

		return _result;
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
		final Triple _trp1 = (Triple) method2Triple.get(exitMethod);

		if (_trp1 == null) {
			throw new IllegalArgumentException(exitMethod + " was not processed.");
		}

		final Triple _trp2 = (Triple) method2Triple.get(enterMethod);

		if (_trp2 == null) {
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
			final AliasSet _t = AliasSet.getASForType(sf.getType());

			if (_t != null) {
				_t.setGlobal();
				globalASs.put(sf.getSignature(), _t);
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

		final SootClass _sc = sm.getDeclaringClass();

		if (_sc.getName().equals("java.lang.Object")) {
			waitMethods.add(_sc.getMethod("void wait()"));
			waitMethods.add(_sc.getMethod("void wait(long)"));
			waitMethods.add(_sc.getMethod("void wait(long,int)"));

			notifyMethods.add(_sc.getMethodByName("notify"));
			notifyMethods.add(_sc.getMethodByName("notifyAll"));
		}
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
		boolean _result = true;

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
				_result = getAliasSetFor(v, sm).escapes();
			} else {
				_result = false;
			}
		} catch (RuntimeException _e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("There is no information about " + v + " occurring in " + sm
					+ ".  So, providing pessimistic info (true).", _e);
			}
		}

		return _result;
	}

	/**
	 * Executes phase 2 and 3 as mentioned in the technical report.  It processed each methods in the call-graph bottom-up
	 * propogating the  alias set information in a collective fashion. It then propogates the information top-down in the
	 * call-graph.
	 */
	public void execute() {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: Equivalence Class-based and Symbol-based Escape Analysis");
		}

		performPhase2();

		performPhase3();

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

		waitMethods.clear();
		notifyMethods.clear();
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
		boolean _result = escapes(v1, sm1) && escapes(v2, sm2);

		if (_result && !(v1 instanceof StaticFieldRef) && !(v2 instanceof StaticFieldRef)) {
			try {
				// Ruf's analysis mandates that the allocation sites that are executed multiple times pollute escape 
				// information. But this is untrue, as all the data that can be shared across threads have been exposed and 
				// marked rightly so at allocation sites.  By equivalence class-based unification guarantees that the 
				// corresponding alias set at the caller side is unified atleast twice in case these threads are started at 
				// different sites.  In case the threads are started at the same site, then the processing of call-site during
				// phase 2 (bottom-up) will ensure that the alias sets are unified with themselves.  Hence, the program 
				// structure and the language semantics along with the rules above ensure that the escape information is 
				// polluted (pessimistic) only when necessary.
				final Collection _o1 = getAliasSetFor(v1, sm1).getShareEntities();
				final Collection _o2 = getAliasSetFor(v2, sm2).getShareEntities();
				_result = (_o1 != null) && (_o2 != null) && !(CollectionUtils.intersection(_o1, _o2).isEmpty());
			} catch (RuntimeException _e) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("There is no information about " + v1 + "/" + v2 + " occurring in " + sm1 + "/" + sm2
						+ ".  So, providing pessimistic info (true).", _e);
				}
			}
		}

		return _result;
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
		final Triple _trp = (Triple) method2Triple.get(sm);

		if (_trp == null) {
			throw new IllegalArgumentException("Method " + sm + " was not analyzed.");
		}

		final Map _local2AS = (Map) _trp.getSecond();
		AliasSet _result = null;

		if (AliasSet.canHaveAliasSet(v.getType())) {
			if (v instanceof InstanceFieldRef) {
				final InstanceFieldRef _i = (InstanceFieldRef) v;
				final AliasSet _temp = (AliasSet) _local2AS.get(_i.getBase());
				_result = _temp.getASForField(((FieldRef) v).getField().getSignature());
			} else if (v instanceof StaticFieldRef) {
				_result = (AliasSet) globalASs.get(((FieldRef) v).getField().getSignature());
			} else if (v instanceof ArrayRef) {
				final ArrayRef _a = (ArrayRef) v;
				final AliasSet _temp = (AliasSet) _local2AS.get(_a.getBase());
				_result = _temp.getASForField(AliasSet.ARRAY_FIELD);
			} else if (v instanceof Local) {
				_result = (AliasSet) _local2AS.get(v);
			}
		}
		return _result;
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
		boolean _result = true;

		try {
			if (AliasSet.canHaveAliasSet(v.getType())) {
				_result = getAliasSetFor(v, sm).isGlobal();
			} else {
				_result = false;
			}
		} catch (RuntimeException _e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("There is no information about " + v + " occurring in " + sm
					+ ".  So, providing pessimistic info (true).", _e);
			}
		}

		return _result;
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
		for (final Iterator _i = delayedSet.entrySet().iterator(); _i.hasNext();) {
			final Map.Entry _element = (Map.Entry) _i.next();
			((MethodContext) _element.getKey()).unify((MethodContext) _element.getValue(), true);
		}
		delayedSet.clear();
	}

	/**
	 * Performs phase 2 processing as described in the paper described in the documentation of this class.
	 */
	private void performPhase2() {
		final IWorkBag _wb = new FIFOWorkBag();
		final Collection _processed = new HashSet();
		final Collection _sccs = cgi.getSCCs(false);

		// Phase 2: The SCCs are ordered bottom up. 
		for (final Iterator _i = _sccs.iterator(); _i.hasNext();) {
			final List _nodes = (List) _i.next();

			for (final Iterator _j = _nodes.iterator(); _j.hasNext();) {
				final SootMethod _sm = (SootMethod) _j.next();

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Processing " + _sm);
				}

				if (!_sm.isConcrete()) {
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn("NO BODY: " + _sm.getSignature());
					}

					continue;
				}

				final Triple _triple = (Triple) method2Triple.get(_sm);

				/*
				 * NOTE: This is an kludge to fix an anamoly arising from closing open system.  However, this triple should
				 * have been created for each processed method in the callback method.
				 */
				if (_triple == null) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("NO METHOD TRIPLE: " + _sm.getSignature());
					}

					continue;
				}

				methodCtxtCache = (MethodContext) _triple.getFirst();
				localASsCache = (Map) _triple.getSecond();
				scCache = (Map) _triple.getThird();
				context.setRootMethod(_sm);

				final BasicBlockGraph _bbg = bbm.getBasicBlockGraph(_sm);
				_wb.clear();
				_processed.clear();
				_wb.addAllWork(_bbg.getHeads());

				while (_wb.hasWork()) {
					final BasicBlock _bb = (BasicBlock) _wb.getWork();
					_processed.add(_bb);

					for (final Iterator _k = _bb.getStmtsOf().iterator(); _k.hasNext();) {
						final Stmt _stmt = (Stmt) _k.next();
						context.setStmt(_stmt);
						stmtProcessor.process(_stmt);
					}

					for (final Iterator _k = _bb.getSuccsOf().iterator(); _k.hasNext();) {
						final Object _o = _k.next();

						if (!_processed.contains(_o)) {
							_wb.addWork(_o);
						}
					}
				}

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("LocalASsCache: " + localASsCache);
				}

				// unify the contexts at start call-sites.
				performDelayedUnification();
			}
		}
	}

	/**
	 * Performs phase 3 processing as described in the paper described in the documentation of this class.
	 */
	private void performPhase3() {
		// Phase 3
		final IWorkBag _wb = new FIFOWorkBag();
		final Collection _processed = new HashSet();
		_wb.addAllWork(cgi.getHeads());

		while (_wb.hasWork()) {
			final SootMethod _caller = (SootMethod) _wb.getWork();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Top-down procesing : CALLER : " + _caller);
			}

			final Collection _callees = cgi.getCallees(_caller);
			Triple _triple = (Triple) method2Triple.get(_caller);
			final Map _ctrp2sc = (Map) _triple.getThird();

			for (final Iterator _i = _callees.iterator(); _i.hasNext();) {
				final CallTriple _ctrp = (CallTriple) _i.next();
				final SootMethod _callee = _ctrp.getMethod();

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Top-down processing : CALLEE : " + _callee);
				}

				_triple = (Triple) method2Triple.get(_callee);

				/*
				 * NOTE: This is an anomaly which results from how an open system is closed.  Refer to MethodVariant.java for
				 * more info.
				 */
				if (_triple == null) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("NO CALLEE TRIPLE: " + _callee.getSignature());
					}

					continue;
				}

				final MethodContext _mc = (MethodContext) (_triple.getFirst());
				final CallTriple _callerTrp = new CallTriple(_caller, _ctrp.getStmt(), _ctrp.getExpr());
				final MethodContext _sc = (MethodContext) _ctrp2sc.get(_callerTrp);

				// It would suffice to unify the site context with it self in the case of loop enclosure
				// as this is more semantically close to what happens during execution.
				if (_callee.getName().equals("start")
					  && _callee.getDeclaringClass().getName().equals("java.lang.Thread")
					  && _callee.getReturnType() instanceof VoidType
					  && _callee.getParameterCount() == 0
					  && cfgAnalysis.executedMultipleTimes(_ctrp.getStmt(), _caller)) {
					_sc.selfUnify(true);
				}
				_sc.propogateInfoFromTo(_mc);

				if (!_processed.contains(_callee)) {
					_processed.add(_callee);
					_wb.addWork(_callee);
				}
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.38  2003/12/16 06:28:14  venku
   - removed the valueprocessor.accessed field and defaulted
     it to true always.

   Revision 1.37  2003/12/13 02:29:08  venku
   - Refactoring, documentation, coding convention, and
     formatting.

   Revision 1.36  2003/12/09 04:22:10  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.35  2003/12/08 12:20:44  venku
   - moved some classes from staticanalyses interface to indus interface package
   - ripple effect.
   Revision 1.34  2003/12/08 12:15:59  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.33  2003/12/08 10:46:45  venku
   - added logging support when processing statements and values.
   - accessed field of valueProcessor is true independent of
     the method as such setting can lead to incorrect results.
   - only Object.wait() was being considered and other variants
     were not being considered. FIXED.
   Revision 1.32  2003/12/07 08:41:32  venku
   - deleted getCallGraph() from ICallGraphInfo interface.
   - made getSCCs() direction sensitive.
   - ripple effect.
   Revision 1.31  2003/12/02 09:42:38  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.30  2003/11/26 06:57:59  venku
   - subtle error in shared.  If the values are static field references
     they will escape but their sharedEntities set will be empty.
     This leads to incorrect results.  FIXED.
   Revision 1.29  2003/11/25 21:47:30  venku
   - logging.
   Revision 1.28  2003/11/16 19:06:50  venku
   - documentation.
   Revision 1.27  2003/11/10 03:17:19  venku
   - renamed AbstractProcessor to AbstractValueAnalyzerBasedProcessor.
   - ripple effect.
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
