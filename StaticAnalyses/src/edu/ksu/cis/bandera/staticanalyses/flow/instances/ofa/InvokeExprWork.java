
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

package edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa;

import ca.mcgill.sable.soot.ArrayType;
import ca.mcgill.sable.soot.Modifier;
import ca.mcgill.sable.soot.RefType;
import ca.mcgill.sable.soot.SootClass;
import ca.mcgill.sable.soot.SootClassManager;
import ca.mcgill.sable.soot.SootMethod;
import ca.mcgill.sable.soot.Type;
import ca.mcgill.sable.soot.VoidType;

import ca.mcgill.sable.soot.jimple.Jimple;
import ca.mcgill.sable.soot.jimple.Local;
import ca.mcgill.sable.soot.jimple.NonStaticInvokeExpr;
import ca.mcgill.sable.soot.jimple.NullConstant;
import ca.mcgill.sable.soot.jimple.SpecialInvokeExpr;
import ca.mcgill.sable.soot.jimple.Value;
import ca.mcgill.sable.soot.jimple.ValueBox;
import ca.mcgill.sable.soot.jimple.VirtualInvokeExpr;

import ca.mcgill.sable.util.List;
import ca.mcgill.sable.util.VectorList;

import edu.ksu.cis.bandera.staticanalyses.flow.AbstractExprSwitch;
import edu.ksu.cis.bandera.staticanalyses.flow.BFA;
import edu.ksu.cis.bandera.staticanalyses.flow.Context;
import edu.ksu.cis.bandera.staticanalyses.flow.IFGNode;
import edu.ksu.cis.bandera.staticanalyses.flow.MethodVariant;
import edu.ksu.cis.bandera.staticanalyses.flow.MethodVariantManager;
import edu.ksu.cis.bandera.staticanalyses.support.Util;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


/**
 * <p>
 * This class represents a peice of work that plugin new fragments of flow graph as new types which provide new
 * implementations flow into the receiver at the associated call-site.
 * </p>
 * Created: Mon Jan 28 12:36:18 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class InvokeExprWork
  extends AbstractAccessExprWork {
	/**
	 * <p>
	 * An instance of <code>Logger</code> used for logging purpose.
	 * </p>
	 */
	private static final Logger LOGGER = LogManager.getLogger(InvokeExprWork.class);

	/**
	 * <p>
	 * A collection of predefined call sequence similar to Thread.start() and Thread.run() pair.  It usually contains
	 * instances of <code>NativeMethodCall</code> which encapsulates the information such as  <code>Thread.run()</code> is
	 * called from the native method <code>Thread.start()</code>.
	 * </p>
	 */
	protected static final Collection KNOWN_CALL_CHAINS = new ArrayList(1);

	/**
	 * <p>
	 * This instance is used to create new virtual invoke ast nodes.
	 * </p>
	 */
	protected static final Jimple JIMPLE = Jimple.v();

	/**
	 * <p>
	 * This represents an empty parameter list.
	 * </p>
	 */
	private static final List EMPTY_PARAM_LIST = new VectorList();

	static {
		KNOWN_CALL_CHAINS.add(new NativeMethodCall("java.lang.Thread", "start", EMPTY_PARAM_LIST, VoidType.v(), "run",
				EMPTY_PARAM_LIST, VoidType.v()));
	}

	/**
	 * <p>
	 * The expression visitor that created this object.  This is used to plugin a new method call into the flow graph.
	 * </p>
	 */
	protected AbstractExprSwitch exprSwitch;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	protected final boolean nonVoid;

	/**
	 * <p>
	 * Creates a new <code>InvokeExprWork</code> instance.
	 * </p>
	 *
	 * @param caller the method in which the call occurs.
	 * @param accessExprBox the expression in which the invocation occurs.
	 * @param context the context in which the invocation occurs.
	 * @param exprSwitch the expression visitor to be used for visiting expressions.
	 *
	 * @throws IllegalArgumentException when <code>accessExprBox</code> does not wrap an <code>NonStaticInvokeExpr</code>
	 *            object.
	 */
	public InvokeExprWork(MethodVariant caller, ValueBox accessExprBox, Context context, AbstractExprSwitch exprSwitch)
	  throws IllegalArgumentException {
		super(caller, accessExprBox, context);

		if (!(accessExprBox.getValue() instanceof NonStaticInvokeExpr)) {
			throw new IllegalArgumentException("accessExprBox has to contain a NonStaticInvokeExpr object as value.");
		}
		this.exprSwitch = exprSwitch;
		this.nonVoid = !(((NonStaticInvokeExpr) accessExprBox.getValue()).getMethod().getReturnType() instanceof VoidType);
	}

	/**
	 * <p>
	 * This class captures the call back relation to the system from the environment via native method calls such as
	 * <code>Thread.start()</code>.
	 * </p>
	 */
	protected static class NativeMethodCall {
		/**
		 * <p>
		 * The list of types of Java method parameters.
		 * </p>
		 */
		List calledJavaMethodParamTypes;

		/**
		 * <p>
		 * The list of types of native method parameters.
		 * </p>
		 */
		List nativeMethodParamTypes;

		/**
		 * <p>
		 * The name of the Java method.
		 * </p>
		 */
		String calledJavaMethodName;

		/**
		 * <p>
		 * The class declaring the native method.
		 * </p>
		 */
		String declClassName;

		/**
		 * <p>
		 * The name of the native method.
		 * </p>
		 */
		String nativeMethodName;

		/**
		 * <p>
		 * The return type of the Java method.
		 * </p>
		 */
		Type calledJavaMethodRetType;

		/**
		 * <p>
		 * The return type of the native method.
		 * </p>
		 */
		Type nativeMethodRetType;

		/**
		 * <p>
		 * Creates a new <code>NativeMethodCall</code> instance.
		 * </p>
		 *
		 * @param declClassName the class declaring the native method.
		 * @param nativeMethodName the name of the native method.
		 * @param nativeMethodParamTypes the list of types of native method parameters.
		 * @param nativeMethodRetType the return type of the native method.
		 * @param calledJavaMethodName the name of the Java method.
		 * @param calledJavaMethodParamTypes the list of types of Java method parameters.
		 * @param calledJavaMethodRetType the return type of the Java method.
		 */
		protected NativeMethodCall(String declClassName, String nativeMethodName, List nativeMethodParamTypes,
			Type nativeMethodRetType, String calledJavaMethodName, List calledJavaMethodParamTypes,
			Type calledJavaMethodRetType) {
			this.declClassName = declClassName;
			this.nativeMethodName = nativeMethodName;
			this.nativeMethodRetType = nativeMethodRetType;
			this.nativeMethodParamTypes = nativeMethodParamTypes;
			this.calledJavaMethodName = calledJavaMethodName;
			this.calledJavaMethodRetType = calledJavaMethodRetType;
			this.calledJavaMethodParamTypes = calledJavaMethodParamTypes;
		}

		/**
		 * <p>
		 * Returns the method corresponding to the called-back method in the given class, if any.
		 * </p>
		 *
		 * @param sc the class which may implement the called-back method.
		 *
		 * @return the called-back method, if one exists.  <code>null</code> if none exists.
		 */
		protected SootMethod getMethod(SootClass sc) {
			SootMethod temp = null;
			SootClass decl =
				Util.getDeclaringClass(sc, calledJavaMethodName, calledJavaMethodParamTypes, calledJavaMethodRetType);
			temp = decl.getMethod(calledJavaMethodName, calledJavaMethodParamTypes, calledJavaMethodRetType);

			return temp;
		}

		/**
		 * <p>
		 * Checks if the given method is the native method for which the Java method needs to be plugged in.
		 * </p>
		 *
		 * @param sm the method which may result in the call-back.
		 *
		 * @return <code>true</code> if <code>sm</code> is the method that will result in the call-back.
		 */
		protected boolean isThisTheMethod(SootMethod sm) {
			boolean temp = false;

			if (sm.getName().equals(nativeMethodName)
				  && sm.getReturnType().equals(nativeMethodRetType)
				  && Util.isDescendentOf(sm.getDeclaringClass(), declClassName)
				  && Modifier.isNative(sm.getModifiers())
				  && sm.getParameterTypes().equals(nativeMethodParamTypes)) {
				temp = true;
			}
			return temp;
		}
	}

	/**
	 * <p>
	 * Checks if any of the <code>values</code> provide a new method implementation.  If so, plugs in the flow graph for the
	 * new implementation at the method invocation site connecting the nodes suitably.  It plugs in call-backs resulting
	 * from native method calls.
	 * </p>
	 */
	public synchronized void execute() {
		NonStaticInvokeExpr e = (NonStaticInvokeExpr) accessExprBox.getValue();
		SootMethod sm = e.getMethod();
		BFA bfa = caller._BFA;
		SootClass sc;
		SootClassManager scm = bfa.getSootClassManager();
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
					sc = bfa.getClass(((RefType) v.getType()).className);
				} else if (t instanceof ArrayType) {
					sc = bfa.getClass("java.lang.Object");
				} else {
					RuntimeException ee = new RuntimeException("Non-reference/array type flowing into invocation site.");
					LOGGER.error(ee);
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

			MethodVariant mv = bfa.getMethodVariant(sm, context);

			if (!installedVariants.contains(mv)) {
				IFGNode param;
				IFGNode arg;

				//System.out.println(this.hashCode() + ": " + sm + " " + sm.getParameterCount() + " " + mv._METHOD);
				for (int j = 0; j < sm.getParameterCount(); j++) {
					param = mv.queryParameterNode(j);
					context.setProgramPoint(e.getArgBox(j));
					arg = caller.queryASTNode(e.getArg(j), context);
					arg.addSucc(param);
				}
				param = mv.queryThisNode();
				context.setProgramPoint(e.getBaseBox());
				arg = caller.queryASTNode(e.getBase(), context);
				arg.addSucc(param);

				if (nonVoid) {
					arg = mv.queryReturnNode();
					context.setProgramPoint(accessExprBox);
					param = caller.queryASTNode(e, context);
					arg.addSucc(param);
				}
				installedVariants.add(mv);
			}

			for (Iterator j = KNOWN_CALL_CHAINS.iterator(); j.hasNext();) {
				NativeMethodCall temp = (NativeMethodCall) j.next();

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Need to check if " + temp.nativeMethodName + " is same as " + sm.getSignature()
						+ " declared in " + sm.getDeclaringClass());
				}

				if (temp.isThisTheMethod(sm)) {
					SootMethod methodToCall = temp.getMethod(scm.getClass(e.getBase().getType().toString()));
					VirtualInvokeExpr v1 = JIMPLE.newVirtualInvokeExpr((Local) e.getBase(), methodToCall, new VectorList());
					exprSwitch.process(JIMPLE.newInvokeExprBox(v1));
					context.setProgramPoint(e.getBaseBox());

					IFGNode src = caller.queryASTNode(e.getBase(), context);
					context.setProgramPoint(v1.getBaseBox());
					src.addSucc(caller.queryASTNode(v1.getBase(), context));

					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Plugging a call to run method of class" + e.getBase().getType().toString() + ".");
					}
				}
			}
		}
		context.setProgramPoint(vb);
	}

	/**
	 * <p>
	 * Returns a stringized representation of this object.
	 * </p>
	 *
	 * @return the stringized representation of this object.
	 */
	public String toString() {
		return "InvokeExprWork: " + caller._METHOD + "@" + accessExprBox.getValue();
	}
}

/*****
 ChangeLog:

$Log$

*****/
