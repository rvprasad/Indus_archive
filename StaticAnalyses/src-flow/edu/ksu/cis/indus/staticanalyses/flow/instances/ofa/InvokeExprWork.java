
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

import java.util.Iterator;

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
public class InvokeExprWork
  extends AbstractAccessExprWork {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(InvokeExprWork.class);

	/**
	 * This instance is used to create new virtual invoke ast nodes.
	 */
	protected static final Jimple JIMPLE = Jimple.v();

	/**
	 * The expression visitor that created this object.  This is used to plugin a new method call into the flow graph.
	 *
	 * @invariant exprSwitch != null
	 */
	protected AbstractExprSwitch exprSwitch;

	/**
	 * Indicates if the method represented by this object returns a value of with reference-like type.
	 *
	 * @invariant returnsRefLikeType != null
	 */
	protected final boolean returnsRefLikeType;

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

		ValueBox invocationExpr = callContext.getProgramPoint();

		if (!(invocationExpr.getValue() instanceof InstanceInvokeExpr)) {
			throw new IllegalArgumentException("accessExprBox has to contain a InstanceInvokeExpr object as value.");
		}
		this.exprSwitch = exprSwitchParam;

		InstanceInvokeExpr ie = (InstanceInvokeExpr) invocationExpr.getValue();
		this.returnsRefLikeType = ie.getMethod().getReturnType() instanceof RefLikeType;
	}

	/**
	 * Checks if any of the <code>values</code> provide a new method implementation.  If so, plugs in the flow graph for the
	 * new implementation at the method invocation site connecting the nodes suitably.  It plugs in call-backs resulting
	 * from native method calls.
	 */
	public synchronized void execute() {
		InstanceInvokeExpr e = (InstanceInvokeExpr) accessExprBox.getValue();
		SootMethod sm = e.getMethod();
		FA fa = caller._fa;
		SootClass sc;
		ValueBox vb = context.getProgramPoint();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(values + " values arrived at base node of " + accessExprBox.getValue() + " in " + context);
		}

		for (Iterator i = values.iterator(); i.hasNext();) {
			Value v = (Value) i.next();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Value: " + v);
			}

			if (v instanceof NullConstant) {
				continue;
			}

			Type t = v.getType();

			if (t instanceof RefType) {
				sc = fa.getClass(((RefType) v.getType()).getClassName());
			} else if (t instanceof ArrayType) {
				sc = fa.getClass("java.lang.Object");
			} else {
				RuntimeException ee = new RuntimeException("Non-reference/array type flowing into invocation site.");

				if (LOGGER.isErrorEnabled()) {
					LOGGER.error(ee);
				}
				context.setProgramPoint(vb);
				throw ee;
			}

			try {
				sm = MethodVariantManager.findDeclaringMethod(sc, e.getMethod());
			} catch (RuntimeException ee) {
				LOGGER.error(sc + ":" + context.getCurrentMethod() + "@" + e, ee);
				context.setProgramPoint(vb);
				throw ee;
			}

			MethodVariant mv = fa.getMethodVariant(sm, context);

			if (!installedVariants.contains(mv)) {
				IFGNode param;
				IFGNode arg;

				for (int j = 0; j < sm.getParameterCount(); j++) {
					if (sm.getParameterType(j) instanceof RefLikeType) {
						param = mv.queryParameterNode(j);
						context.setProgramPoint(e.getArgBox(j));
						arg = caller.queryASTNode(e.getArg(j), context);
						arg.addSucc(param);
					}
				}
				param = mv.queryThisNode();
				context.setProgramPoint(e.getBaseBox());
				arg = caller.queryASTNode(e.getBase(), context);
				arg.addSucc(param);

				if (returnsRefLikeType) {
					arg = mv.queryReturnNode();
					context.setProgramPoint(accessExprBox);
					param = caller.queryASTNode(e, context);
					arg.addSucc(param);
				}
				installedVariants.add(mv);
			}
		}
		context.setProgramPoint(vb);
	}

	/**
	 * Returns a stringized representation of this object.
	 *
	 * @return the stringized representation of this object.
	 *
	 * @post result != null
	 */
	public String toString() {
		return "InvokeExprWork: " + caller._method + "@" + accessExprBox.getValue();
	}
}

/*
   ChangeLog:
   $Log$
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
