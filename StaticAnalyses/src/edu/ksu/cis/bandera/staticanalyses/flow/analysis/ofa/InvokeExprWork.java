package edu.ksu.cis.bandera.bfa.analysis.ofa;

import edu.ksu.cis.bandera.bfa.Context;
import edu.ksu.cis.bandera.bfa.AbstractExprSwitch;
import edu.ksu.cis.bandera.bfa.AbstractFGNode;
import edu.ksu.cis.bandera.bfa.MethodVariant;
import edu.ksu.cis.bandera.bfa.MethodVariantManager;
import edu.ksu.cis.bandera.bfa.BFA;

import ca.mcgill.sable.soot.RefType;
import ca.mcgill.sable.soot.SootClass;
import ca.mcgill.sable.soot.SootMethod;
import ca.mcgill.sable.soot.jimple.NonStaticInvokeExpr;
import ca.mcgill.sable.soot.jimple.SpecialInvokeExpr;
import ca.mcgill.sable.soot.jimple.Value;

import java.util.Iterator;

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

	private static final Logger logger = Logger.getLogger(InvokeExprWork.class.getName());

	public InvokeExprWork (MethodVariant caller, Value accessExpr, Context context) {
		super(caller, accessExpr, context);
	}

	public void execute() {
		NonStaticInvokeExpr e = (NonStaticInvokeExpr)accessExpr;
		SootMethod sm = e.getMethod();
		BFA bfa = caller.bfa;
		SootClass sc;

		logger.debug("Expr:" + accessExpr + "   Values:" + values + "   Method:" + sm);

		for (Iterator i = values.iterator(); i.hasNext();) {
			 Value v = (Value)i.next();

			 if (e instanceof SpecialInvokeExpr) {
				 sc = e.getMethod().getDeclaringClass();
			 } else {
				 sc = bfa.getClass(((RefType)v.getType()).className);
			 } // end of else

			 sm = MethodVariantManager.findDeclaringMethod(sc, e.getMethod().getName());
			 MethodVariant mv = bfa.getMethodVariant(sm, context);

			 if (!installedVariants.contains(mv)) {
				 AbstractFGNode param, arg;

				 for (int j = 0; j < sm.getParameterCount(); j++) {
					 param = mv.getParameterNode(j);
					 arg = caller.getASTNode(e.getArg(j));
					 arg.addSucc(param);
				 } // end of for (int i = 0; i < sm.getArgCount(); i++)

				 param = mv.getThisNode();
				 arg = caller.getASTNode(e.getBase());
				 arg.addSucc(param);

				 if (AbstractExprSwitch.isNonVoid(sm)) {
					 arg = mv.getReturnNode();
					 param = caller.getASTNode(e);
					 arg.addSucc(param);
				 } // end of if (isNonVoid(sm))

				 installedVariants.add(mv);
			 } // end of if (!installedVariants.contains(mv))

		} // end of for (Iterator i = values.iterator(); i.hasNext();)
	}

}// InvokeExprWork
