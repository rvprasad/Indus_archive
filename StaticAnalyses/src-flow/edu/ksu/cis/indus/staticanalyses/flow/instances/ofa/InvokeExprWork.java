
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import edu.ksu.cis.indus.processing.Context;

import edu.ksu.cis.indus.staticanalyses.flow.AbstractExprSwitch;
import edu.ksu.cis.indus.staticanalyses.flow.FA;
import edu.ksu.cis.indus.staticanalyses.flow.IFGNode;
import edu.ksu.cis.indus.staticanalyses.flow.MethodVariant;
import edu.ksu.cis.indus.staticanalyses.flow.MethodVariantManager;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.ArrayType;
import soot.RefLikeType;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.ValueBox;

import soot.jimple.InstanceInvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.JimpleValueSwitch;
import soot.jimple.NullConstant;


/**
 * This class represents a peice of work that plugin new fragments of flow graph as new types which provide new
 * implementations flow into the receiver at the associated call-site.
 * 
 * <p>
 * Created: Mon Jan 28 12:36:18 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
class InvokeExprWork
  extends AbstractAccessExprWork {
	/**
	 * This instance is used to create new virtual invoke ast nodes.
	 */
	protected static final Jimple JIMPLE = Jimple.v();

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(InvokeExprWork.class);

	/**
	 * The expression visitor that created this object.  This is used to plugin a new method call into the flow graph.
	 *
	 * @invariant exprSwitch != null
	 */
	protected JimpleValueSwitch exprSwitch;

	/**
	 * Indicates if the method represented by this object returns a value of with reference-like type.
	 *
	 * @invariant returnsRefLikeType != null
	 */
	protected final boolean returnsRefLikeType;

	/**
	 * The collection of variants already processed/installed at the given access expression.  We do not want to process
	 * variants again and again.
	 *
	 * @invariant installedVariants != null
	 */
	private final Set installedVariants = new HashSet();

	/**
	 * Creates a new <code>InvokeExprWork</code> instance.
	 *
	 * @param callerMethod the method in which the call occurs.
	 * @param callContext the context in which the invocation occurs.
	 * @param exprSwitchParam the expression visitor to be used for visiting expressions.
	 *
	 * @throws IllegalArgumentException when <code>accessExprBox</code> does not wrap an <code>InstanceInvokeExpr</code>
	 * 		   object.
	 *
	 * @pre callerMethod != null and invocationExpr != null and callContext != null
	 */
	public InvokeExprWork(final MethodVariant callerMethod, final Context callContext,
		final AbstractExprSwitch exprSwitchParam) {
		super(callerMethod, callContext);

		final ValueBox _invocationExpr = callContext.getProgramPoint();

		if (!(_invocationExpr.getValue() instanceof InstanceInvokeExpr)) {
			throw new IllegalArgumentException("accessExprBox has to contain a InstanceInvokeExpr object as value.");
		}
		this.exprSwitch = exprSwitchParam;

		final InstanceInvokeExpr _ie = (InstanceInvokeExpr) _invocationExpr.getValue();
		this.returnsRefLikeType = _ie.getMethod().getReturnType() instanceof RefLikeType;
	}

	/**
	 * Checks if any of the <code>values</code> provide a new method implementation.  If so, plugs in the flow graph for the
	 * new implementation at the method invocation site connecting the nodes suitably.  It plugs in call-backs resulting
	 * from native method calls.
	 */
	public synchronized void execute() {
		final InstanceInvokeExpr _e = (InstanceInvokeExpr) accessExprBox.getValue();
		final ValueBox _vb = context.getProgramPoint();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(values + " values arrived at base node of " + accessExprBox.getValue() + " in " + context);
		}

		for (final Iterator _i = values.iterator(); _i.hasNext();) {
			final Value _v = (Value) _i.next();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Value: " + _v);
			}

			if (_v instanceof NullConstant) {
				continue;
			}

			try {
				processExprAgainstValue(_e, _v);
			} catch (final IllegalStateException _excp) {
				context.setProgramPoint(_vb);
				throw _excp;
			}
		}
		context.setProgramPoint(_vb);
	}

	/**
	 * Returns a stringized representation of this object.
	 *
	 * @return the stringized representation of this object.
	 *
	 * @post result != null
	 */
	public String toString() {
		return "InvokeExprWork: " + caller.getMethod() + "@" + accessExprBox.getValue();
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param expr DOCUMENT ME!
	 * @param value DOCUMENT ME!
	 */
	private void processExprAgainstValue(final InstanceInvokeExpr expr, final Value value) {
		final Type _t = value.getType();
		final SootClass _sc;
		final FA _fa = caller.getFA();

		if (_t instanceof RefType) {
			_sc = _fa.getClass(((RefType) value.getType()).getClassName());
		} else if (_t instanceof ArrayType) {
			_sc = _fa.getClass("java.lang.Object");
		} else {
			final IllegalStateException _excp =
				new IllegalStateException("Non-reference/array type flowing into invocation site.");

			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(expr);
			}
			throw _excp;
		}

		final SootMethod _sm;

		try {
			_sm = MethodVariantManager.findDeclaringMethod(_sc, expr.getMethod());
		} catch (final IllegalStateException _excp) {
			LOGGER.error(_sc + ":" + context.getCurrentMethod() + "@" + expr, _excp);
			throw _excp;
		}

		final MethodVariant _mv = _fa.getMethodVariant(_sm, context);

		if (!installedVariants.contains(_mv)) {
			IFGNode _param;
			IFGNode _arg;

			for (int _j = 0; _j < _sm.getParameterCount(); _j++) {
				if (_sm.getParameterType(_j) instanceof RefLikeType) {
					_param = _mv.queryParameterNode(_j);
					context.setProgramPoint(expr.getArgBox(_j));
					_arg = caller.queryASTNode(expr.getArg(_j), context);
					_arg.addSucc(_param);
				}
			}
			_param = _mv.queryThisNode();
			context.setProgramPoint(expr.getBaseBox());
			_arg = caller.queryASTNode(expr.getBase(), context);
			_arg.addSucc(_param);

			if (returnsRefLikeType) {
				_arg = _mv.queryReturnNode();
				context.setProgramPoint(accessExprBox);
				_param = caller.queryASTNode(expr, context);
				_arg.addSucc(_param);
			}
			installedVariants.add(_mv);
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.14  2004/04/02 09:58:28  venku
   - refactoring.
     - collapsed flow insensitive and sensitive parts into common classes.
     - coding convention
     - documentation.
   Revision 1.13  2003/12/07 05:02:34  venku
   - formatting.
   Revision 1.12  2003/12/05 21:13:56  venku
   - special invokes are treated just like virtual invoke.
   Revision 1.11  2003/12/05 02:27:20  venku
   - unnecessary methods and fields were removed. Like
       getCurrentProgramPoint()
       getCurrentStmt()
   - context holds current information and only it must be used
     to retrieve this information.  No auxiliary arguments. FIXED.
   Revision 1.10  2003/12/02 09:42:37  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.9  2003/11/06 05:15:07  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.8  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.7  2003/08/25 11:24:22  venku
   Coding convention.
   Revision 1.6  2003/08/24 12:58:11  venku
   Formatting.
   Revision 1.5  2003/08/20 18:14:38  venku
   Log4j was used instead of logging.  That is fixed.
   Revision 1.4  2003/08/17 10:48:34  venku
   Renamed BFA to FA.  Also renamed bfa variables to fa.
   Ripple effect was huge.
   Revision 1.3  2003/08/16 21:55:14  venku
   Ripple effect of changing FA._FA to FA._fa
   Revision 1.2  2003/08/15 03:39:53  venku
   Spruced up documentation and specification.
   Tightened preconditions in the interface such that they can be loosened later on in implementaions.
   Renamed a few fields/parameter variables to avoid name confusion.
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
 */
