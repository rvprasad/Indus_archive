
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
import edu.ksu.cis.bandera.staticanalyses.flow.FGNode;
import edu.ksu.cis.bandera.staticanalyses.flow.MethodVariant;
import edu.ksu.cis.bandera.staticanalyses.flow.MethodVariantManager;
import edu.ksu.cis.bandera.staticanalyses.support.Util;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


// InvokeExprWork.java

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
	private static final Logger logger = LogManager.getLogger(InvokeExprWork.class);

	/**
	 * <p>
	 * A collection of predefined call sequence similar to Thread.start() and Thread.run() pair.  It usually contains
	 * instances of <code>NativeMethodCall</code> which encapsulates the information such as  <code>Thread.run()</code> is
	 * called from the native method <code>Thread.start()</code>.
	 * </p>
	 */
	protected static final Collection knownCallChains = new ArrayList(1);

	/**
	 * <p>
	 * This instance is used to create new virtual invoke ast nodes.
	 * </p>
	 */
	protected static final Jimple jimple = Jimple.v();

	/**
	 * <p>
	 * This represents an empty parameter list.
	 * </p>
	 */
	private static final List emptyParamList = new VectorList();

	static {
		knownCallChains.add(new NativeMethodCall("java.lang.Thread", "start", emptyParamList, VoidType.v(), "run",
				emptyParamList, VoidType.v()));
	}

	/**
	 * <p>
	 * The expression visitor that created this object.  This is used to plugin a new method call into the flow graph.
	 * </p>
	 */
	protected AbstractExprSwitch exprSwitch;

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
	 * 		   object.
	 */
	public InvokeExprWork(MethodVariant caller, ValueBox accessExprBox, Context context, AbstractExprSwitch exprSwitch) {
		super(caller, accessExprBox, context);

		if(!(accessExprBox.getValue() instanceof NonStaticInvokeExpr)) {
			throw new IllegalArgumentException("accessExprBox has contain a NonStaticInvokeExpr object as value.");
		}

		// end of if (!(accessExprBox.getValue() instanceof NonStaticInvokeExpr))
		this.exprSwitch = exprSwitch;
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

			if(sm.getName().equals(nativeMethodName)
					&& sm.getReturnType().equals(nativeMethodRetType)
					&& Util.isDescendentOf(sm.getDeclaringClass(), declClassName)
					&& Modifier.isNative(sm.getModifiers())
					&& sm.getParameterTypes().equals(nativeMethodParamTypes)) {
				temp = true;
			}

			// end of if (sm.getName() == nativeMethodName)
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
		BFA bfa = caller.bfa;
		SootClass sc;
		SootClassManager scm = bfa.getSootClassManager();
		logger.debug("Expr:" + e + "   Values:" + values + "   Method:" + sm);

		ValueBox vb = context.getProgramPoint();

		for(Iterator i = values.iterator(); i.hasNext();) {
			Value v = (Value) i.next();

			if(v instanceof NullConstant) {
				continue;
			}

			if(e instanceof SpecialInvokeExpr) {
				sc = e.getMethod().getDeclaringClass();
			} else {
				sc = bfa.getClass(((RefType) v.getType()).className);
			}

			// HACK 1: This try wrapper is to address scenarios in which values of incorrect type may flow into invocation
			// sites as a result of using array types of Object and also as the object flow in array domain is based on array
			// types rather than array allocation sites.  This needs to be addressed after SAS03.
			try {
				sm = MethodVariantManager.findDeclaringMethod(sc, e.getMethod());
			} catch(IllegalStateException ee) {
				continue;
			}

			MethodVariant mv = bfa.getMethodVariant(sm, context);

			if(!installedVariants.contains(mv)) {
				FGNode param;
				FGNode arg;

				for(int j = 0; j < sm.getParameterCount(); j++) {
					param = mv.queryParameterNode(j);
					context.setProgramPoint(e.getArgBox(j));
					arg = caller.queryASTNode(e.getArg(j), context);
					arg.addSucc(param);
				}

				// end of for (int i = 0; i < sm.getArgCount(); i++)
				param = mv.queryThisNode();
				context.setProgramPoint(e.getBaseBox());
				arg = caller.queryASTNode(e.getBase(), context);
				arg.addSucc(param);

				if(AbstractExprSwitch.isNonVoid(sm)) {
					arg = mv.queryReturnNode();
					context.setProgramPoint(accessExprBox);
					param = caller.queryASTNode(e, context);
					arg.addSucc(param);
				}

				// end of if (isNonVoid(sm))
				installedVariants.add(mv);
			}

			// end of if (!installedVariants.contains(mv))
			for(Iterator j = knownCallChains.iterator(); j.hasNext();) {
				NativeMethodCall temp = (NativeMethodCall) j.next();
				logger.debug("Need to check if " + temp.nativeMethodName + " is same as " + sm.getSignature()
					+ " declared in " + sm.getDeclaringClass());

				if(temp.isThisTheMethod(sm)) {
					SootMethod methodToCall = temp.getMethod(scm.getClass(e.getBase().getType().toString()));
					VirtualInvokeExpr v1 = jimple.newVirtualInvokeExpr((Local) e.getBase(), methodToCall, new VectorList());
					exprSwitch.process(jimple.newInvokeExprBox(v1));
					context.setProgramPoint(e.getBaseBox());

					FGNode src = caller.queryASTNode(e.getBase(), context);
					context.setProgramPoint(v1.getBaseBox());
					src.addSucc(caller.queryASTNode(v1.getBase(), context));
					logger.debug("Plugging a call to run method of class" + e.getBase().getType().toString() + ".");
				}

				// end of for (Iterator j = values.iterator(); j.hasNext();)
			}

			// end of if
		}

		// end of for (Iterator i = knownCallChains.iterator(); i.hasNext();)
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
		return "InvokeExprWork: " + caller.sm + "@" + accessExprBox.getValue();
	}
}

/*****
 ChangeLog:

$Log$

*****/
