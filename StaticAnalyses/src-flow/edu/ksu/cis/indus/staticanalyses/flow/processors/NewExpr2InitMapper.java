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

package edu.ksu.cis.indus.staticanalyses.flow.processors;

import edu.ksu.cis.indus.common.collections.MapUtils;
import edu.ksu.cis.indus.common.soot.Constants;

import edu.ksu.cis.indus.interfaces.INewExpr2InitMapper;

import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.AbstractValueAnalyzerBasedProcessor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import soot.SootMethod;
import soot.Value;
import soot.ValueBox;

import soot.jimple.InvokeStmt;
import soot.jimple.NewExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;

/**
 * This class provides an implementation of <code>INewExpr2InitMapper</code> based on object flow information. The approach
 * pairs the instance creation expression and the invocation expression only when the object created at the creation
 * expression flows into the primary at the invocation site.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class NewExpr2InitMapper
		extends AbstractValueAnalyzerBasedProcessor<Value>
		implements INewExpr2InitMapper {

	/**
	 * This is a cache of the context.
	 */
	private Context contextCache = new Context();

	/**
	 * This maps methods to a map from new expression occurring statement to init invocation expression occurring statement.
	 * 
	 * @invariant method2map != null
	 * @invariant method2map.keySet()->forall(o | method2map.get(o)->forall(p | (
	 *            p.getValue().getInvokeExpr().oclIsKindOf(SpecialInvokeExpr))))
	 */
	private final Map<SootMethod, Map<Stmt, InvokeStmt>> method2initStmt = new HashMap<SootMethod, Map<Stmt, InvokeStmt>>(
			Constants.getNumOfMethodsInApplication());

	/**
	 * DOCUMENT ME!
	 */
	private final Map<SootMethod, Map<NewExpr, Stmt>> method2newExprStmt = new HashMap<SootMethod, Map<NewExpr, Stmt>>(
			Constants.getNumOfMethodsInApplication());

	/**
	 * This is the object flow information to be used to improve precision.
	 */
	private IValueAnalyzer<Value> ofa;

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.ValueBox, edu.ksu.cis.indus.processing.Context)
	 */
	@Override public void callback(final ValueBox vBox, final Context context) {
		final Value _value = vBox.getValue();

		if (_value instanceof NewExpr) {
			final Stmt _stmt = context.getStmt();
			final SootMethod _method = context.getCurrentMethod();

			final Map<NewExpr, Stmt> _ne2init = MapUtils.getMapFromMap(method2newExprStmt, _method);
			_ne2init.put((NewExpr) _value, _stmt);
		} else if (_value instanceof SpecialInvokeExpr && context.getStmt() instanceof InvokeStmt) {
			final InvokeStmt _stmt = (InvokeStmt) context.getStmt();
			final SootMethod _method = context.getCurrentMethod();
			final SpecialInvokeExpr _expr = (SpecialInvokeExpr) _value;
			final SootMethod _sm = _expr.getMethod();

			if (_sm.getName().equals("<init>")) {
				final Map<Stmt, InvokeStmt> _ne2initStmt = MapUtils.getMapFromMap(method2initStmt, _method);
				final Map<NewExpr, Stmt> _ne2newStmt = MapUtils.getMapFromMap(method2newExprStmt, _method);
				contextCache.setRootMethod(_method);
				contextCache.setStmt(_stmt);
				contextCache.setProgramPoint(_expr.getBaseBox());

				final Collection<Value> _values = ofa.getValues(_expr.getBase(), contextCache);

				for (final Iterator<Value> _i = _values.iterator(); _i.hasNext();) {
					final NewExpr _e = (NewExpr) _i.next();
					final Stmt _newStmt = _ne2newStmt.remove(_e);
					_ne2initStmt.put(_newStmt, _stmt);
				}
			}
		}
	}

	/**
	 * @see INewExpr2InitMapper#getInitCallStmtForNewExprStmt(Stmt,SootMethod)
	 */
	public InvokeStmt getInitCallStmtForNewExprStmt(final Stmt newExprStmt, final SootMethod method) {
		final Map<Stmt, InvokeStmt> _ne2init = method2initStmt.get(method);
		InvokeStmt _result = null;

		if (_ne2init != null) {
			_result = _ne2init.get(newExprStmt);
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void hookup(final ProcessingController ppc) {
		ppc.register(NewExpr.class, this);
		ppc.register(SpecialInvokeExpr.class, this);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#processingBegins()
	 */
	@Override public void processingBegins() {
		method2initStmt.clear();
	}

	/**
	 * Resets the data structures.
	 */
	@Override public final void reset() {
		method2initStmt.clear();
		contextCache.setStmt(null);
		contextCache.setProgramPoint(null);
		contextCache.setRootMethod(null);
	}

	/**
	 * @see AbstractValueAnalyzerBasedProcessor#setAnalyzer(IValueAnalyzer)
	 */
	@Override public void setAnalyzer(final IValueAnalyzer<Value> analyzer) {
		ofa = analyzer;
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregister(NewExpr.class, this);
		ppc.unregister(SpecialInvokeExpr.class, this);
	}
}

// End of File
