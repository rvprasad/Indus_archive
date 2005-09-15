
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

import edu.ksu.cis.indus.common.datastructures.Triple;
import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.interfaces.IReadWriteInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Local;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Value;

import soot.jimple.AbstractJimpleValueSwitch;
import soot.jimple.ArrayRef;
import soot.jimple.CastExpr;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.ParameterRef;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.ThisRef;
import soot.jimple.VirtualInvokeExpr;


/**
 * This class encapsulates the logic to process the expressions during escape analysis.  Alias sets are created as required.
 * The class relies on <code>AliasSet</code> to decide if alias set needs to be created for a type of value.
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
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ValueProcessor.class);

	/** 
	 * The associated escape analysis.
	 */
	private final EquivalenceClassBasedEscapeAnalysis ecba;

	/** 
	 * This indicates if locals should be marked as read and written.
	 */
	private boolean markLocals = true;

	/** 
	 * This indicates if the value occurs as a rhs-value or a lhs-value in an assignment statement. <code>true</code>
	 * indicates that it value occurs as a rhs-value in an assignment statement.  <code>false</code> indicates that the
	 * value occurs as a lhs-value in an assignment statement.  This is used to mark alias sets of primaries in access
	 * expressions in a manner appropriate to the analysis.  For example, in side-effect analysis, the primaries of array
	 * expressions are read as rhs-value and are written to as lhs-value.
	 */
	private boolean rhs = true;

	/**
	 * Creates an instance of this class.
	 *
	 * @param analysis associated with this instance.
	 *
	 * @pre analysis != null
	 */
	ValueProcessor(final EquivalenceClassBasedEscapeAnalysis analysis) {
		ecba = analysis;
	}

	/**
	 * Provides the alias set associated with the array element being referred.  All elements in a dimension of an array are
	 * abstracted by a single alias set.
	 *
	 * @see soot.jimple.RefSwitch#caseArrayRef(soot.jimple.ArrayRef)
	 */
	public void caseArrayRef(final ArrayRef v) {
		boolean _temp = rhs;
		rhs = true;
		process(v.getBase());
		rhs = _temp;

		final AliasSet _base = (AliasSet) getResult();
		final AliasSet _elt = processField(v.getType(), _base, IReadWriteInfo.ARRAY_FIELD);

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
		boolean _temp = rhs;
		rhs = true;
		process(v.getBase());
		rhs = _temp;

		final AliasSet _base = (AliasSet) getResult();
		final String _fieldSig = v.getField().getSignature();
		final AliasSet _field = processField(v.getType(), _base, _fieldSig);

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
		AliasSet _s = (AliasSet) ecba.localASsCache.get(v);

		if (_s == null) {
			_s = AliasSet.getASForType(v.getType());

			if (_s != null) {
				ecba.localASsCache.put(v, _s);
			}
		}

		if (_s != null && markLocals) {
			recordAccessInfo(_s);
		}

		setResult(_s);
	}

	/**
	 * @see soot.jimple.RefSwitch#caseParameterRef( soot.jimple.ParameterRef)
	 */
	public void caseParameterRef(final ParameterRef v) {
		final AliasSet _as = ecba.methodCtxtCache.getParamAS(v.getIndex());
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
		final SootField _field = v.getField();
		final AliasSet _base = ecba.getASForClass(_field.getDeclaringClass());
		final AliasSet _fieldAS = processField(v.getType(), _base, _field.getSignature());
		_base.setGlobal();
		setResult(_fieldAS);

		if (rhs) {
			ecba.methodCtxtCache.globalDataWasRead();
		} else {
			ecba.methodCtxtCache.globalDataWasWritten();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseStaticInvokeExpr( soot.jimple.StaticInvokeExpr)
	 */
	public void caseStaticInvokeExpr(final StaticInvokeExpr v) {
		processInvokeExpr(v);
	}

	/**
	 * @see soot.jimple.RefSwitch#caseThisRef(soot.jimple.ThisRef)
	 */
	public void caseThisRef(final ThisRef v) {
		final AliasSet _as = ecba.methodCtxtCache.getThisAS();
		setResult(_as);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseVirtualInvokeExpr( soot.jimple.VirtualInvokeExpr)
	 */
	public void caseVirtualInvokeExpr(final VirtualInvokeExpr v) {
		processInvokeExpr(v);
	}

	/**
	 * Creates an alias set if <code>o</code> is of type <code>Value</code>.  It uses <code>AliasSet</code> to decide if the
	 * given type requires an alias set.  If not, <code>null</code> is provided   as the alias set.  This is also the  case
	 * when <code>o</code> is not of type <code>Value</code>.
	 *
	 * @param o is a piece of IR to be processed.
	 */
	public void defaultCase(final Object o) {
		setResult(null);
	}

	/**
	 * Sets the value of <code>markLocals</code>.
	 *
	 * @param value the new value of <code>markLocals</code>.
	 *
	 * @return the previous value of markLocals.
	 */
	boolean setMarkLocals(final boolean value) {
		final boolean _result = markLocals;
		this.markLocals = value;
		return _result;
	}

	/**
	 * Sets the value of <code>rhs</code>.
	 *
	 * @param b the new value of <code>rhs</code>.
	 *
	 * @return the previous value of rhs.
	 */
	boolean setRHS(final boolean b) {
		final boolean _r = rhs;
		rhs = b;
		return _r;
	}

	/**
	 * Process the given value/expression.
	 *
	 * @param value to be processed.
	 *
	 * @pre value != null
	 */
	void process(final Value value) {
		if (EquivalenceClassBasedEscapeAnalysis.VALUE_PROCESSOR_LOGGER.isDebugEnabled()) {
			EquivalenceClassBasedEscapeAnalysis.VALUE_PROCESSOR_LOGGER.debug("Processing value: " + value);
		}
		value.apply(this);
	}

	/**
	 * Process the arguments of the invoke expression.
	 *
	 * @param v is the invoke expressions containing the arguments to be processed.
	 *
	 * @return the list of alias sets corresponding to the arguments.
	 *
	 * @pre v != null
	 * @post result != null and result.oclIsKindOf(Sequence(AliasSet))
	 */
	private List processArguments(final InvokeExpr v) {
		// fix up arg alias sets.
		final List _argASs;
		final int _paramCount = v.getMethod().getParameterCount();

		if (_paramCount == 0) {
			_argASs = Collections.EMPTY_LIST;
		} else {
			_argASs = new ArrayList();

			for (int _i = 0; _i < _paramCount; _i++) {
				final Value _val = v.getArg(_i);
				Object _temp = null;

				if (EquivalenceClassBasedEscapeAnalysis.canHaveAliasSet(_val.getType())) {
					process(v.getArg(_i));
					_temp = ecba.valueProcessor.getResult();
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
			final Triple _triple = (Triple) ecba.method2Triple.get(_callee);

			// This is needed when the system is not closed.
			if (_triple == null) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("NO TRIPLE.  May be due to open system. - " + _callee.getSignature());
				}
				continue;
			}

			// retrieve the method context of the callee
			MethodContext _mc = (MethodContext) _triple.getFirst();

			/*
			 * If the caller and callee occur in different SCCs then clone the callee method context and then unify it
			 * with the site context.  If not, unify the method context with site-context as precision will be lost any
			 * which way.
			 */
			if (ecba.cfgAnalysis.notInSameSCC(caller, _callee)) {
				try {
					_mc = (MethodContext) _mc.clone();
				} catch (final CloneNotSupportedException _e) {
					LOGGER.error("Hell NO!  This should not happen.", _e);
					throw new RuntimeException(_e);
				}
			}

			final boolean _isThreadBoundary = processNotifyStartWaitSync(primaryAliasSet, _callee);

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
			if (Util.isStartMethod(_callee) && ecba.cfgAnalysis.executedMultipleTimes(ecba.context.getStmt(), caller)) {
				_mc.selfUnify();
			}
			siteContext.unifyMethodContext(_mc);
		}
	}

	/**
	 * Processes the fields.
	 *
	 * @param t type of the access expression.
	 * @param base is the alias set of the primary.
	 * @param fieldSig is the signature of the accessed field.
	 *
	 * @return the alias set for the field.
	 */
	private AliasSet processField(final Type t, final AliasSet base, final String fieldSig) {
		AliasSet _field = base.getASForField(fieldSig);

		if (_field == null) {
			_field = AliasSet.getASForType(t);

			if (_field != null) {
				base.putASForField(fieldSig, _field);
			}
		}

		if (_field != null) {
			recordAccessInfo(_field);
		}

		if (rhs) {
			base.addReadField(fieldSig);
		} else {
			base.addWrittenField(fieldSig);
		}
		return _field;
	}

	/**
	 * Processes invoke expressions/call-sites.
	 *
	 * @param expr invocation expresison to be processed.
	 */
	private void processInvokeExpr(final InvokeExpr expr) {
		final Collection _callees = new ArrayList();
		final SootMethod _caller = ecba.context.getCurrentMethod();
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

		final List _argASs = processArguments(expr);

		// create a site-context of the given expression and store it into the associated site-context cache.
		final MethodContext _sc = new MethodContext(_sm, _primaryAS, _argASs, _retAS, AliasSet.createAliasSet(), ecba);
		ecba.scCache.put(new CallTriple(_caller, ecba.context.getStmt(), expr), _sc);

		if (expr instanceof StaticInvokeExpr) {
			_callees.add(_sm);
		} else if (expr instanceof InterfaceInvokeExpr
			  || expr instanceof VirtualInvokeExpr
			  || expr instanceof SpecialInvokeExpr) {
			_callees.addAll(ecba.cgi.getCallees(expr, ecba.context));
		}

		processCallees(_callees, _caller, _primaryAS, _sc);

		setResult(_retAS);
	}

	/**
	 * Process the called method for <code>start(), notify(), nofityAll(),</code>, and variants of <code>wait</code> methods.
	 *
	 * @param primaryAliasSet is the alias set corresponding to the primary of the invocation expression.
	 * @param callee being called.
	 *
	 * @return <code>true</code> when the called method is <code>java.lang.Thread.start()</code>.
	 *
	 * @pre primaryAliasSet != null and callee != null
	 */
	private boolean processNotifyStartWaitSync(final AliasSet primaryAliasSet, final SootMethod callee) {
		boolean _startWasInvoked = false;

		if (Util.isStartMethod(callee)) {
			// unify alias sets after all statements are processed if "start" is being invoked.
			_startWasInvoked = true;
		} else if (Util.isWaitMethod(callee)) {
			primaryAliasSet.setWaits();
			primaryAliasSet.setLocked();
		} else if (Util.isNotifyMethod(callee)) {
			primaryAliasSet.setNotifies();
		}

		return _startWasInvoked;
	}

	/**
	 * Helper method to record threads in which alias is accessed.
	 *
	 * @param as is the alias set to be marked.
	 *
	 * @pre as != null
	 */
	private void recordAccessInfo(final AliasSet as) {
		as.setAccessed();

		if (ecba.tgi != null) {
			if (rhs) {
				as.addReadThreads(ecba.tgi.getExecutionThreads(ecba.context.getCurrentMethod()));
			} else {
				as.addWriteThreads(ecba.tgi.getExecutionThreads(ecba.context.getCurrentMethod()));
			}
		}
	}
}

// End of File
