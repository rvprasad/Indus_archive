
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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

import edu.ksu.cis.indus.common.datastructures.HistoryAwareFIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.Triple;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;
import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;

import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.ArrayType;
import soot.Local;
import soot.Modifier;
import soot.RefType;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Value;

import soot.jimple.AbstractJimpleValueSwitch;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
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
  extends AbstractAnalysis {
	/*
	 * xxxCache variables do not capture state of the object.  Rather they are used cache values across method calls.  Hence,
	 * any subclasses of this class should  not reply on these variables as they may be removed in the future.
	 */

	/** 
	 * This is the unique string identifier that can be used to identify an instance of this class.
	 */
	public static final String ID = "Shared Access Information";

	/** 
	 * The logger used by instances of <code>ValueProcessor</code> class to log messages.
	 */
	static final Log VALUE_PROCESSOR_LOGGER = LogFactory.getLog(EquivalenceClassBasedEscapeAnalysis.ValueProcessor.class);

	/** 
	 * The logger used by instances of <code>StmtProcessor</code> class to log messages.
	 */
	static final Log STMT_PROCESSOR_LOGGER = LogFactory.getLog(EquivalenceClassBasedEscapeAnalysis.StmtProcessor.class);

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
	 * Creates a new EquivalenceClassBasedEscapeAnalysis object.
	 *
	 * @param callgraph provides call-graph information.
	 * @param basicBlockGraphMgr provides basic block graphs required by this analysis.
	 *
	 * @pre scene != null and callgraph != null and tgi != null
	 */
	public EquivalenceClassBasedEscapeAnalysis(final ICallGraphInfo callgraph, final BasicBlockGraphMgr basicBlockGraphMgr) {
		cgi = callgraph;
		globalASs = new HashMap();
		method2Triple = new HashMap();
		stmtProcessor = new StmtProcessor();
		valueProcessor = new ValueProcessor();
		bbm = basicBlockGraphMgr;
		context = new Context();
		cfgAnalysis = new CFGAnalysis(cgi, bbm);
		preprocessor = new PreProcessor();
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
				_l.unifyAliasSet(_r);
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
				_l.unifyAliasSet(_r);
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
				methodCtxtCache.getReturnAS().unifyAliasSet(_l);
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseThrowStmt(soot.jimple.ThrowStmt)
		 */
		public void caseThrowStmt(final ThrowStmt stmt) {
			valueProcessor.process(stmt.getOp());

			final AliasSet _l = (AliasSet) valueProcessor.getResult();

			if (_l != null) {
				methodCtxtCache.getThrownAS().unifyAliasSet(_l);
			}
		}

		/**
		 * Processes the given statement.
		 *
		 * @param stmt to be processed.
		 *
		 * @pre stmt != null
		 */
		void process(final Stmt stmt) {
			if (STMT_PROCESSOR_LOGGER.isTraceEnabled()) {
				STMT_PROCESSOR_LOGGER.trace("Processing statement: " + stmt);
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
			process(v.getBase());
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
		 * @see soot.jimple.ExprSwitch#caseCastExpr(soot.jimple.CastExpr)
		 */
		public void caseCastExpr(final CastExpr v) {
			process(v.getOp());
		}

		/**
		 * @see soot.jimple.RefSwitch#caseInstanceFieldRef(soot.jimple.InstanceFieldRef)
		 */
		public void caseInstanceFieldRef(final InstanceFieldRef v) {
			boolean _temp = read;
			read = true;
			process(v.getBase());
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
			setResult(null);
		}

		/**
		 * Process the given value/expression.
		 *
		 * @param value to be processed.
		 *
		 * @pre value != null
		 */
		void process(final Value value) {
			if (VALUE_PROCESSOR_LOGGER.isTraceEnabled()) {
				VALUE_PROCESSOR_LOGGER.trace("Processing value: " + value);
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

					if (EquivalenceClassBasedEscapeAnalysis.canHaveAliasSet(_val.getType())) {
						process(v.getArg(_i));
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
		 * @param siteContext corresponding to the invocation expression.
		 *
		 * @throws RuntimeException when cloning fails.
		 *
		 * @pre callees != null and caller != null and primaryAliasSet != null and MethodContext != null
		 * @pre callees.oclIsKindOf(Collection(SootMethod))
		 */
		private void processCallees(final Collection callees, final SootMethod caller, final AliasSet primaryAliasSet,
			final MethodContext siteContext) {
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

				final boolean _isThreadBoundary = processNotifyStartWait(primaryAliasSet, _callee);

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

				if (_isThreadBoundary) {
					_mc.markAsCrossingThreadBoundary();
				}

				// Ruf's analysis mandates that the allocation sites that are executed multiple times pollute escape 
				// information. But this is untrue, as all the data that can be shared across threads have been exposed and 
				// marked rightly so at allocation sites.  By equivalence class-based unification, it is guaranteed that the 
				// corresponding alias set at the caller side is unified atleast twice in case these threads are started at 
				// different sites.  In case the threads are started at the same site, then the processing of call-site during
				// phase 2 (bottom-up) will ensure that the alias sets are unified with themselves.  Hence, the program 
				// structure and the language semantics along with the rules above ensure that the escape information is 
				// polluted (pessimistic) only when necessary.
				//
				// It would suffice to unify the method context with it self in the case of loop enclosure
				// as this is more semantically close to what happens during execution.
				if (Util.isStartMethod(_callee) && cfgAnalysis.executedMultipleTimes(context.getStmt(), caller)) {
					_mc.selfUnify();
				}
				siteContext.unifyMethodContext(_mc);
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
				process(((InstanceInvokeExpr) expr).getBase());
				_primaryAS = (AliasSet) getResult();
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

			if (Util.isStartMethod(callee)) {
				// unify alias sets after all statements are processed if "start" is being invoked.
				_delayUnification = true;
			} else if (Util.isWaitMethod(callee)) {
				primaryAliasSet.setWaits();
			} else if (Util.isNotifyMethod(callee)) {
				primaryAliasSet.setNotifies();
			}
			return _delayUnification;
		}
	}


	/**
	 * This class is used to create alias sets for global variables.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private final class PreProcessor
	  extends AbstractProcessor {
		/**
		 * {@inheritDoc}  Creates an alias set for the static fields.  This is the creation of global alias sets in Ruf's
		 * algorithm.
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
		 * {@inheritDoc}  Creates a method context for <code>sm</code>.  This is the creation of method contexts in Ruf's
		 * algorithm.
		 */
		public void callback(final SootMethod sm) {
			method2Triple.put(sm, new Triple(new MethodContext(sm), new HashMap(), new HashMap()));
		}

		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(ProcessingController)
		 */
		public void hookup(final ProcessingController ppc) {
			ppc.register(this);
		}

		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(ProcessingController)
		 */
		public void unhook(final ProcessingController ppc) {
			ppc.unregister(this);
		}
	}

	/**
	 * Executes phase 2 and 3 as mentioned in the technical report.  It processed each methods in the call-graph bottom-up
	 * propogating the  alias set information in a collective fashion. It then propogates the information top-down in the
	 * call-graph.
	 */
	public void analyze() {
		unstable();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: Equivalence Class-based and Symbol-based Escape Analysis");
		}

		performPhase2();

		performPhase3();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("analyze() - " + toString());
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: Equivalence Class-based and Symbol-based Escape Analysis");
		}
		stable();
	}

	/**
	 * Checks if the given statement containing a <code>wait</code> invocation is coupled to the given statement containing
	 * <code>notify/All</code> invocation.  By coupling we mean that the notification via the given notify  invocation may
	 * reach the given wait invocation.
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
	public boolean areWaitAndNotifyCoupled(final InvokeStmt wait, final SootMethod waitMethod, final InvokeStmt notify,
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

			if (Util.isWaitMethod(_wSM) && Util.isNotifyMethod(_nSM)) {
				final AliasSet _as1 = (AliasSet) ((Map) _trp1.getSecond()).get(_wTemp.getBase());
				final AliasSet _as2 = (AliasSet) ((Map) _trp2.getSecond()).get(_nTemp.getBase());

				if ((_as1.getReadyEntities() != null) && (_as2.getReadyEntities() != null)) {
					_result = CollectionUtils.containsAny(_as1.getReadyEntities(), _as2.getReadyEntities());
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
							+ "wait()s that occur in different threads - " + wait + "@" + waitMethod + " " + notify + "@"
							+ notifyMethod);
					}
				}
			}
		}

		return _result;
	}

	/**
	 * Checks if the given type can contribute to aliasing.  Only reference and array types can lead to aliasing.
	 *
	 * @param type to be checked for aliasing support.
	 *
	 * @return <code>true</code> if <code>type</code> can contribute aliasing; <code>false</code>, otherwise.
	 *
	 * @pre type != null
	 */
	public static boolean canHaveAliasSet(final Type type) {
		return type instanceof RefType || type instanceof ArrayType;
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
			if (EquivalenceClassBasedEscapeAnalysis.canHaveAliasSet(v.getType())) {
				/*
				 *  Ruf's analysis mandates that the allocation sites that are executed multiple times pollute escape
				 * information. But this is untrue, as all the data that can be shared across threads have been exposed and
				 * marked rightly so at allocation sites.  By equivalence class-based unification guarantees that the
				 * corresponding alias set at the caller side is unified atleast twice in case these threads are started at
				 * different sites.  In case the threads are started at the same site, then the processing of call-site during
				 * phase 2 (bottom-up) will ensure that the alias sets are unified with themselves.  Hence, the program
				 * structure and the language semantics along with the rules above ensure that the escape information is
				 * polluted (pessimistic) only when necessary.
				 */
				_result = getAliasSetFor(v, sm).escapes();
			} else {
				_result = false;
			}
		} catch (final NullPointerException _e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("There is no information about " + v + " occurring in " + sm
					+ ".  So, providing pessimistic info (true).", _e);
			}
		}

		return _result;
	}

	/**
	 * Flushes the site contexts.
	 */
	public void flushSiteContexts() {
		// delete references to site caches as they will not be used hereon.
		for (final Iterator _i = cgi.getReachableMethods().iterator(); _i.hasNext();) {
			final SootMethod _sm = (SootMethod) _i.next();
			final Triple _triple = (Triple) method2Triple.get(_sm);
			method2Triple.put(_sm, new Triple(_triple.getFirst(), _triple.getSecond(), null));
		}
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
		boolean _result = escapes(v1, sm1) && escapes(v2, sm2);

		if (_result && !(v1 instanceof StaticFieldRef) && !(v2 instanceof StaticFieldRef)) {
			try {
				final Collection _o1 = getAliasSetFor(v1, sm1).getShareEntities();
				final Collection _o2 = getAliasSetFor(v2, sm2).getShareEntities();
				_result = (_o1 != null) && (_o2 != null) && CollectionUtils.containsAny(_o1, _o2);
			} catch (final NullPointerException _e) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("There is no information about " + v1 + "/" + v2 + " occurring in " + sm1 + "/" + sm2
						+ ".  So, providing pessimistic info (true).", _e);
				}
			}
		}

		return _result;
	}

	/**
	 * Checks if "this" variable of the given method escapes.  If the method is static then the result is pessimistic, hence,
	 * <code>true</code> is returned.
	 *
	 * @param method in which "this" occurs.
	 *
	 * @return <code>true</code> if "this" escapes; <code>false</code>, otherwise.
	 *
	 * @pre method != null
	 */
	public boolean thisEscapes(final SootMethod method) {
		boolean _result = true;
		final Triple _triple = (Triple) method2Triple.get(method);

		if (_triple == null && LOGGER.isWarnEnabled()) {
			LOGGER.warn("There is no information about " + method + ".  So, providing pessimistic info (true).");
		} else {
			final AliasSet _as1 = ((MethodContext) _triple.getFirst()).getThisAS();

			// if non-static query the alias set of "this" variable.  If static, just return true assuming that the 
			// application to decide wisely :-)
			if (_as1 != null) {
				_result = _as1.escapes();
			}
		}
		return _result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		final StringBuffer _result = new StringBuffer("\n");
		final Set _entrySet1 = method2Triple.entrySet();
		final Iterator _i = _entrySet1.iterator();
		final int _iEnd = _entrySet1.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Map.Entry _entry1 = (Map.Entry) _i.next();
			_result.append(_entry1.getKey());
			_result.append(":\n");

			final Map _local2AS = (Map) ((Triple) _entry1.getValue()).getSecond();
			_result.append(_local2AS.toString());
			_result.append("\n");
		}
		return _result.toString();
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

		if (EquivalenceClassBasedEscapeAnalysis.canHaveAliasSet(v.getType())) {
			if (v instanceof InstanceFieldRef) {
				final InstanceFieldRef _i = (InstanceFieldRef) v;
				final AliasSet _temp = (AliasSet) _local2AS.get(_i.getBase());
				_result = _temp.getASForField(_i.getField().getSignature());
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
	 * Retrieves the alias set for "this" variable of the given method.
	 *
	 * @param method of interest.
	 *
	 * @return the alias set corresponding to the "this" variable of the given method.
	 *
	 * @pre method != null and method.isStatic()
	 */
	AliasSet getAliasSetForThis(final SootMethod method) {
		return ((MethodContext) ((Triple) method2Triple.get(method)).getFirst()).thisAS;
	}

	/**
	 * Retrieves the alias set on the caller side that corresponds to the given alias set on the callee side at the given
	 * call site in the caller.
	 *
	 * @param ref the reference alias set.
	 * @param callee the method in which <code>ref</code> occurs.
	 * @param site the call site at which <code>callee</code> is called.
	 *
	 * @return the caller site alias set that corresponds to <code>ref</code>.  This will be <code>null</code> if there is no
	 * 		   such alias set.
	 *
	 * @pre ref != null and callee != null and site != null
	 */
	AliasSet getCallerSideAliasSet(final AliasSet ref, final SootMethod callee, final CallTriple site) {
		final Triple _triple = (Triple) method2Triple.get(site.getMethod());
		final Map _callsite2mc = (Map) _triple.getThird();
		final MethodContext _callingContext = (MethodContext) _callsite2mc.get(site);
		final MethodContext _calleeContext = (MethodContext) ((Triple) method2Triple.get(callee)).getFirst();
		return _calleeContext.getASCorrespondingToGivenASInGivenContext(ref, _callingContext);
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
			if (EquivalenceClassBasedEscapeAnalysis.canHaveAliasSet(v.getType())) {
				_result = getAliasSetFor(v, sm).isGlobal();
			} else {
				_result = false;
			}
		} catch (final NullPointerException _e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("There is no information about " + v + " occurring in " + sm
					+ ".  So, providing pessimistic info (true).", _e);
			}
		}

		return _result;
	}

	/**
	 * Rewires the method context, local variable alias sets, and site contexts such that they contain only representative
	 * alias sets and no the nominal(indirectional) alias sets.
	 *
	 * @param method for which this processing should occur.
	 *
	 * @pre method != null
	 */
	private void discardReferentialAliasSets(final SootMethod method) {
		if (localASsCache.isEmpty()) {
			localASsCache = Collections.EMPTY_MAP;
		} else {
			for (final Iterator _i = localASsCache.entrySet().iterator(); _i.hasNext();) {
				final Map.Entry _entry = (Map.Entry) _i.next();
				final AliasSet _as = (AliasSet) _entry.getValue();
				final AliasSet _equiv = (AliasSet) _as.find();

				if (_equiv != _as) {
					_entry.setValue(_equiv);
				}
			}
		}

		if (scCache.isEmpty()) {
			scCache = Collections.EMPTY_MAP;
		} else {
			for (final Iterator _i = scCache.entrySet().iterator(); _i.hasNext();) {
				final Map.Entry _entry = (Map.Entry) _i.next();
				final MethodContext _mc = (MethodContext) _entry.getValue();
				final MethodContext _mcRep = (MethodContext) _mc.find();

				if (_mcRep != _mc) {
					_entry.setValue(_mcRep);
				}
				_mcRep.discardReferentialAliasSets();
			}
		}
		methodCtxtCache.discardReferentialAliasSets();
		method2Triple.put(method, new Triple(methodCtxtCache, localASsCache, scCache));
	}

	/**
	 * Performs phase 2 processing as described in the paper described in the documentation of this class.
	 */
	private void performPhase2() {
		final Collection _processed = new HashSet();
		final IWorkBag _wb = new HistoryAwareFIFOWorkBag(_processed);
		final Collection _sccs = cgi.getSCCs(false);

		// Phase 2: The SCCs are ordered bottom up. 
		for (final Iterator _i = _sccs.iterator(); _i.hasNext();) {
			final List _nodes = (List) _i.next();

			for (final Iterator _j = _nodes.iterator(); _j.hasNext();) {
				final SootMethod _sm = (SootMethod) _j.next();

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Bottom-up processing method " + _sm);
				}

				if (!_sm.isConcrete()) {
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn("NO BODY: " + _sm.getSignature());
					}

					continue;
				}

				final Triple _triple = (Triple) method2Triple.get(_sm);

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

					for (final Iterator _k = _bb.getStmtsOf().iterator(); _k.hasNext();) {
						final Stmt _stmt = (Stmt) _k.next();
						context.setStmt(_stmt);
						stmtProcessor.process(_stmt);
					}
					_wb.addAllWorkNoDuplicates(_bb.getSuccsOf());
				}

				// discard alias sets that serve as a mere indirection level. 
				discardReferentialAliasSets(_sm);
			}
		}
	}

	/**
	 * Performs phase 3 processing as described in the paper described in the documentation of this class.
	 */
	private void performPhase3() {
		// Phase 3
		final List _methodsInTopologicalOrder = cgi.getMethodsInTopologicalOrder(true);

		for (final Iterator _cgiIterator = _methodsInTopologicalOrder.iterator(); _cgiIterator.hasNext();) {
			final SootMethod _caller = (SootMethod) _cgiIterator.next();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Top-down processing method : CALLER : " + _caller);
			}

			final Collection _callees = cgi.getCallees(_caller);
			final Triple _callerTriple = (Triple) method2Triple.get(_caller);
			final Map _ctrp2sc = (Map) _callerTriple.getThird();

			for (final Iterator _i = _callees.iterator(); _i.hasNext();) {
				final CallTriple _ctrp = (CallTriple) _i.next();
				final SootMethod _callee = _ctrp.getMethod();

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Top-down processing : CALLEE : " + _callee);
				}

				final Triple _calleeTriple = (Triple) method2Triple.get(_callee);

				/*
				 * NOTE: This is an anomaly which results from how an open system is closed.  Refer to MethodVariant.java for
				 * more info.
				 */
				if (_calleeTriple == null) {
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn("NO CALLEE TRIPLE: " + _callee.getSignature());
					}

					continue;
				}

				final MethodContext _calleeMethodContext = (MethodContext) (_calleeTriple.getFirst());
				final CallTriple _callerTrp = new CallTriple(_caller, _ctrp.getStmt(), _ctrp.getExpr());
				final MethodContext _calleeSiteContext = (MethodContext) _ctrp2sc.get(_callerTrp);
				_calleeSiteContext.propogateInfoFromTo(_calleeMethodContext);
			}
		}
	}
}

// End of File
