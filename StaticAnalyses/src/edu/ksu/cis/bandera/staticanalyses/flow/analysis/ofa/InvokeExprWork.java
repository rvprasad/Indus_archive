package edu.ksu.cis.bandera.bfa.analysis.ofa;





import ca.mcgill.sable.soot.RefType;
import ca.mcgill.sable.soot.SootClass;
import ca.mcgill.sable.soot.SootClassManager;
import ca.mcgill.sable.soot.SootMethod;
import ca.mcgill.sable.soot.jimple.Jimple;
import ca.mcgill.sable.soot.jimple.Local;
import ca.mcgill.sable.soot.jimple.NonStaticInvokeExpr;
import ca.mcgill.sable.soot.jimple.NullConstant;
import ca.mcgill.sable.soot.jimple.SpecialInvokeExpr;
import ca.mcgill.sable.soot.jimple.Value;
import ca.mcgill.sable.soot.jimple.VirtualInvokeExpr;
import ca.mcgill.sable.util.VectorList;
import edu.ksu.cis.bandera.bfa.AbstractExprSwitch;
import edu.ksu.cis.bandera.bfa.BFA;
import edu.ksu.cis.bandera.bfa.Context;
import edu.ksu.cis.bandera.bfa.FGNode;
import edu.ksu.cis.bandera.bfa.MethodVariant;
import edu.ksu.cis.bandera.bfa.MethodVariantManager;
import edu.ksu.cis.bandera.bfa.Util;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * InvokeExprWork.java
 *
 *
 * Created: Mon Jan 28 12:36:18 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public class InvokeExprWork extends AbstractAccessExprWork {

	private static final Logger logger = LogManager.getLogger(InvokeExprWork.class.getName());

	protected static final Collection knownCallChains = new ArrayList(1);


	protected AbstractExprSwitch exprSwitch;

	protected static final Jimple jimple = Jimple.v();

	public InvokeExprWork (MethodVariant caller, NonStaticInvokeExpr accessExpr, Context context,
						   AbstractExprSwitch exprSwitch) {
		super(caller, accessExpr, context);
		this.exprSwitch = exprSwitch;
		knownCallChains.add(new NativeMethodCall("java.lang.Thread", "start", "run"));
	}

	public void execute() {
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

			 sm = MethodVariantManager.findDeclaringMethod(sc, e.getMethod().getName());
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
				 if (Util.isAncestorOf(sm.getDeclaringClass(), temp.declClassName) &&
					 sm.getName().equals(temp.nativeMethodName)) {
					  SootClass runClass =
						  Util.getDeclaringClass(scm.getClass(e.getBase().getType().toString()), temp.calledJavaMethodName);
					  VirtualInvokeExpr v1 = jimple.newVirtualInvokeExpr((Local)e.getBase(),
																		 runClass.getMethod(temp.calledJavaMethodName),
																		 new VectorList());
					  exprSwitch.caseVirtualInvokeExpr(v1);
					  logger.debug("Plugging a call to run method of class" + e.getBase().getType().toString() + ".");
				 } // end of for (Iterator j = values.iterator(); j.hasNext();)
			 } // end of if
		} // end of for (Iterator i = knownCallChains.iterator(); i.hasNext();)
	}

	protected static class NativeMethodCall {

		String declClassName;

		String nativeMethodName;

		String calledJavaMethodName;

		public NativeMethodCall(String declClassName, String nativeMethodName, String calledJavaMethodName) {
			this.declClassName = declClassName;
			this.nativeMethodName = nativeMethodName;
			this.calledJavaMethodName = calledJavaMethodName;
		}
	}

}// InvokeExprWork
