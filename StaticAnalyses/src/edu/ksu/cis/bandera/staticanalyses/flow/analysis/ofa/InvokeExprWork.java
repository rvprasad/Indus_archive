package edu.ksu.cis.bandera.bfa.analysis.ofa;


import edu.ksu.cis.bandera.bfa.AbstractExprSwitch;
import edu.ksu.cis.bandera.bfa.BFA;
import edu.ksu.cis.bandera.bfa.Context;
import edu.ksu.cis.bandera.bfa.FGNode;
import edu.ksu.cis.bandera.bfa.MethodVariant;
import edu.ksu.cis.bandera.bfa.MethodVariantManager;
import edu.ksu.cis.bandera.bfa.Util;

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
import ca.mcgill.sable.soot.jimple.VirtualInvokeExpr;
import ca.mcgill.sable.util.List;
import ca.mcgill.sable.util.VectorList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

// InvokeExprWork.java
/**
 * <p>This class represents a peice of work that plugin new fragments of flow graph as new types which provide new
 * implementations flow into the receiver at the associated call-site.</p>
 *
 * Created: Mon Jan 28 12:36:18 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */

public class InvokeExprWork extends AbstractAccessExprWork {

	/**
	 * <p>An instance of <code>Logger</code> used for logging purpose.</p>
	 *
	 */
	private static final Logger logger = LogManager.getLogger(InvokeExprWork.class.getName());

	/**
	 * <p>A collection of predefined call sequence similar to Thread.start() and Thread.run() pair.  It usually contains
	 * instances of <code>NativeMethodCall</code> which encapsulates the information such as  <code>Thread.run()</code> is
	 * called from the native method <code>Thread.start()</code>.</p>
	 *
	 */
	protected static final Collection knownCallChains = new ArrayList(1);

	/**
	 * <p>The expression visitor that created this object.  This is used to plugin a new method call into the flow graph.</p>
	 *
	 */
	protected AbstractExprSwitch exprSwitch;

	/**
	 * <p>This instance is used to create new virtual invoke ast nodes.</p>
	 *
	 */
	protected static final Jimple jimple = Jimple.v();

	/**
	 * <p>This represents an empty parameter list.</p>
	 *
	 */
	private static final List emptyParamList = new VectorList();

	static {
		knownCallChains.add(new NativeMethodCall("java.lang.Thread",
												 "start", emptyParamList, VoidType.v(),
												 "run", emptyParamList, VoidType.v()));
	}

	/**
	 * <p>Creates a new <code>InvokeExprWork</code> instance.</p>
	 *
	 * @param caller the method in which the call occurs.
	 * @param accessExpr the expression in which the invocation occurs.
	 * @param context the context in which the invocation occurs.
	 * @param exprSwitch the expression visitor to be used for visiting expressions.
	 */
	public InvokeExprWork (MethodVariant caller, NonStaticInvokeExpr accessExpr, Context context,
						   AbstractExprSwitch exprSwitch) {
		super(caller, accessExpr, context);
		this.exprSwitch = exprSwitch;
	}

	/**
	 * <p>Checks if any of the <code>values</code> provide a new method implementation.  If so, plugs in the flow graph for
	 * the new implementation at the method invocation site connecting the nodes suitably.  It plugs in call-backs resulting
	 * from native method calls. </p>
	 *
	 */
	public synchronized void execute() {
		NonStaticInvokeExpr e = (NonStaticInvokeExpr)accessExpr;
		SootMethod sm = e.getMethod();
		BFA bfa = caller.bfa;
		SootClass sc;
		SootClassManager scm = bfa.getSootClassManager();

		logger.debug("Expr:" + accessExpr + "   Values:" + values + "   Method:" + sm);

		for (Iterator i = values.iterator(); i.hasNext();) {
			 Value v = (Value)i.next();

			 if (v instanceof NullConstant) {
				 continue;
			 } // end of if (v instanceof NullConstant)


			 if (e instanceof SpecialInvokeExpr) {
				 sc = e.getMethod().getDeclaringClass();
			 } else {
				 sc = bfa.getClass(((RefType)v.getType()).className);
			 } // end of else

			 sm = MethodVariantManager.findDeclaringMethod(sc, e.getMethod());
			 MethodVariant mv = bfa.getMethodVariant(sm, context);

			 if (!installedVariants.contains(mv)) {
				 FGNode param, arg;

				 for (int j = 0; j < sm.getParameterCount(); j++) {
					 param = mv.getParameterNode(j);
					 arg = caller.getASTNode(e.getArg(j), context);
					 arg.addSucc(param);
				 } // end of for (int i = 0; i < sm.getArgCount(); i++)

				 param = mv.getThisNode();
				 arg = caller.getASTNode(e.getBase(), context);
				 arg.addSucc(param);

				 if (AbstractExprSwitch.isNonVoid(sm)) {
					 arg = mv.getReturnNode();
					 param = caller.getASTNode(e, context);
					 arg.addSucc(param);
				 } // end of if (isNonVoid(sm))

				 installedVariants.add(mv);
			 } // end of if (!installedVariants.contains(mv))

			 for (Iterator j = knownCallChains.iterator(); j.hasNext();) {
				 NativeMethodCall temp = (NativeMethodCall)j.next();
				 logger.debug("Need to check if " + temp.nativeMethodName + " is same as " + sm.getSignature() +
							  " declared in " + sm.getDeclaringClass());
				 if (temp.isThisTheMethod(sm)) {
					 SootMethod methodToCall = temp.getMethod(scm.getClass(e.getBase().getType().toString()));
					 VirtualInvokeExpr v1 = jimple.newVirtualInvokeExpr((Local)e.getBase(), methodToCall, new VectorList());
					 exprSwitch.caseVirtualInvokeExpr(v1);
					 logger.debug("Plugging a call to run method of class" + e.getBase().getType().toString() + ".");
				 } // end of for (Iterator j = values.iterator(); j.hasNext();)
			 } // end of if
		} // end of for (Iterator i = knownCallChains.iterator(); i.hasNext();)
	}

	/**
	 * <p>This class captures the call back relation to the system from the environment via native method calls such as
	 * <code>Thread.start()</code>. </p>
	 *
	 */
	protected static class NativeMethodCall {

		/**
		 * <p>The class declaring the native method.</p>
		 *
		 */
		String declClassName;

		/**
		 * <p>The name of the native method.</p>
		 *
		 */
		String nativeMethodName;

		/**
		 * <p>The return type of the native method.</p>
		 *
		 */
		Type nativeMethodRetType;

		/**
		 * <p>The list of types of native method parameters.</p>
		 *
		 */
		List nativeMethodParamTypes;

		/**
		 * <p>The name of the Java method.</p>
		 *
		 */
		String calledJavaMethodName;

		/**
		 * <p>The return type of the Java method.</p>
		 *
		 */
		Type calledJavaMethodRetType;

		/**
		 * <p>The list of types of Java method parameters.</p>
		 *
		 */
		List calledJavaMethodParamTypes;

		/**
		 * <p>Creates a new <code>NativeMethodCall</code> instance.</p>
		 *
		 * @param declClassName the class declaring the native method.
		 * @param nativeMethodName the name of the native method.
		 * @param nativeMethodParamTypes the list of types of native method parameters.
		 * @param nativeMethodRetType the return type of the native method.
		 * @param calledJavaMethodName the name of the Java method.
		 * @param calledJavaMethodParamTypes the list of types of Java method parameters.
		 * @param calledJavaMethodRetType the return type of the Java method.
		 */
		protected NativeMethodCall(String declClassName,
								   String nativeMethodName, List nativeMethodParamTypes, Type nativeMethodRetType,
								   String calledJavaMethodName, List calledJavaMethodParamTypes,
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
		 * <p>Checks if the given method is the native method for which the Java method needs to be plugged in.</p>
		 *
		 * @param sm the method which may result in the call-back.
		 * @return <code>true</code> if <code>sm</code> is the method that will result in the call-back.
		 */
		protected boolean isThisTheMethod(SootMethod sm) {
			boolean temp = false;

			if (sm.getName().equals(nativeMethodName) && sm.getReturnType().equals(nativeMethodRetType) &&
				Util.isAncestorOf(sm.getDeclaringClass(), declClassName) && Modifier.isNative(sm.getModifiers()) &&
				sm.getParameterTypes().equals(nativeMethodParamTypes)) {
				temp = true;
			} // end of if (sm.getName() == nativeMethodName)

			return temp;
		}

		/**
		 * <p>Returns the method corresponding to the called-back method in the given class, if any.</p>
		 *
		 * @param sc the class which may implement the called-back method.
		 * @return the called-back method, if one exists.  <code>null</code> if none exists.
		 */
		protected SootMethod getMethod(SootClass sc) {
			SootMethod temp = null;
			SootClass decl = Util.getDeclaringClass(sc, calledJavaMethodName, calledJavaMethodParamTypes,
													calledJavaMethodRetType);
			temp = decl.getMethod(calledJavaMethodName, calledJavaMethodParamTypes, calledJavaMethodRetType);
			return temp;
		}
	}

}// InvokeExprWork
