
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import soot.ArrayType;
import soot.NullType;
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
import soot.jimple.SpecialInvokeExpr;

import edu.ksu.cis.indus.staticanalyses.Context;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractExprSwitch;
import edu.ksu.cis.indus.staticanalyses.flow.FA;
import edu.ksu.cis.indus.staticanalyses.flow.IFGNode;
import edu.ksu.cis.indus.staticanalyses.flow.MethodVariant;
import edu.ksu.cis.indus.staticanalyses.flow.MethodVariantManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;


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
	 * @param invocationExpr expression in which the invocation occurs.
	 * @param callContext the context in which the invocation occurs.
	 * @param exprSwitchParam the expression visitor to be used for visiting expressions.
	 *
	 * @throws IllegalArgumentException when <code>accessExprBox</code> does not wrap an <code>InstanceInvokeExpr</code>
	 * 		   object.
	 *
	 * @pre callerMethod != null and invocationExpr != null and callContext != null
	 */
	public InvokeExprWork(final MethodVariant callerMethod, final ValueBox invocationExpr, final Context callContext,
		final AbstractExprSwitch exprSwitchParam) {
		super(callerMethod, invocationExpr, callContext);

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
			LOGGER.debug(this + "\n\tMethod:" + sm + "\n\tExpr:" + e + "\n\tValues:" + values + "\n\tNode:" + node);
		}

		for (Iterator i = values.iterator(); i.hasNext();) {
			Value v = (Value) i.next();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Value: " + v);
			}

			if (v instanceof NullConstant) {
				continue;
			}

			if (e instanceof SpecialInvokeExpr) {
				sc = e.getMethod().getDeclaringClass();
			} else {
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
