
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors;

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

import soot.jimple.NewExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class Init2NewExprMapper
  extends AbstractValueAnalyzerBasedProcessor {
	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private Context contextCache = new Context();

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private IValueAnalyzer ofa;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private Map method2map = new HashMap();

	/**
	 * @see IValueAnalyzerBasedProcessor#setAnalyzer(IValueAnalyzer)
	 */
	public void setAnalyzer(final IValueAnalyzer analyzer) {
		ofa = analyzer;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param newExprStmt DOCUMENT ME!
	 * @param method DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public Stmt getInitCallStmtForNewExprStmt(final Stmt newExprStmt, final SootMethod method) {
		Map ne2init = (Map) method2map.get(method);
		Stmt result = null;

		if (ne2init != null) {
			result = (Stmt) ne2init.get(newExprStmt);
		}
		return result;
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.ValueBox, edu.ksu.cis.indus.processing.Context)
	 */
	public void callback(final ValueBox vBox, final Context context) {
		Value value = vBox.getValue();

		if (value instanceof NewExpr) {
			Stmt stmt = context.getStmt();
			SootMethod method = context.getCurrentMethod();

			Map ne2init = getMapFor(method);
			ne2init.put(value, stmt);
		} else if (value instanceof SpecialInvokeExpr) {
			Stmt stmt = context.getStmt();
			SootMethod method = context.getCurrentMethod();
			SpecialInvokeExpr expr = (SpecialInvokeExpr) value;
			SootMethod sm = expr.getMethod();

			if (sm.getName().equals("<init>")) {
				Map ne2init = getMapFor(method);
				contextCache.setRootMethod(method);
				contextCache.setStmt(stmt);
				contextCache.setProgramPoint(expr.getBaseBox());

				Collection values = ofa.getValues(expr.getBase(), contextCache);

				for (Iterator i = values.iterator(); i.hasNext();) {
					NewExpr e = (NewExpr) i.next();
					Stmt newStmt = (Stmt) ne2init.remove(e);
					ne2init.put(newStmt, stmt);
				}
			}
		}
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
	public void processingBegins() {
		method2map.clear();
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregister(NewExpr.class, this);
		ppc.unregister(SpecialInvokeExpr.class, this);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param method DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	private Map getMapFor(final SootMethod method) {
		Map result = (Map) method2map.get(method);

		if (result == null) {
			result = new HashMap();
			method2map.put(method, result);
		}
		return result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/11/22 00:42:22  venku
   - renamed InitResolved to Init2NewExprMapper.
   - added logic to realize the functionality.
   Revision 1.1  2003/11/20 08:22:33  venku
   - added support to include calls to <init> based on new expressions.
   - need to implement the class that provides this information.
 */
