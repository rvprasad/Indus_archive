package edu.ksu.cis.bandera.bfa.analysis.ofa;


import edu.ksu.cis.bandera.bfa.AbstractAnalyzer;
import edu.ksu.cis.bandera.bfa.AbstractExprSwitch;
import edu.ksu.cis.bandera.bfa.AbstractIndexManager;
import edu.ksu.cis.bandera.bfa.AbstractStmtSwitch;
import edu.ksu.cis.bandera.bfa.Context;
import edu.ksu.cis.bandera.bfa.ModeFactory;
import edu.ksu.cis.bandera.bfa.modes.insensitive.IndexManager;
import edu.ksu.cis.bandera.bfa.modes.sensitive.flow.ASTIndexManager;

import ca.mcgill.sable.soot.SootClass;
import ca.mcgill.sable.soot.SootField;
import ca.mcgill.sable.soot.SootMethod;
import ca.mcgill.sable.soot.jimple.ArrayRef;
import ca.mcgill.sable.soot.jimple.IdentityRef;
import ca.mcgill.sable.soot.jimple.InvokeExpr;
import ca.mcgill.sable.soot.jimple.Local;
import ca.mcgill.sable.soot.jimple.NewArrayExpr;
import ca.mcgill.sable.soot.jimple.NewExpr;
import ca.mcgill.sable.soot.jimple.NewMultiArrayExpr;
import ca.mcgill.sable.soot.jimple.NonStaticInvokeExpr;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Analyzer.java
 *
 *
 * Created: Wed Jan 30 18:49:43 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public class Analyzer extends AbstractAnalyzer {

	private static final Logger cat = Logger.getLogger(Analyzer.class.getName());

	private Analyzer (String name, AbstractIndexManager astim,
					  AbstractExprSwitch lexpr, AbstractExprSwitch rexpr, AbstractStmtSwitch stmt) {
		super(name, new ModeFactory(astim,
									new IndexManager(),
									new IndexManager(),
									new IndexManager(),
									new IndexManager(),
									new OFAFGNode(null),
									stmt, lexpr, rexpr));
	}

	public static Analyzer getFIAnalyzer(String name) {
		return new Analyzer(name,
							new IndexManager(),
							new edu.ksu.cis.bandera.bfa.analysis.ofa.fi.ExprSwitch(null, new LHSConnector()),
							new edu.ksu.cis.bandera.bfa.analysis.ofa.fi.ExprSwitch(null, new RHSConnector()),
							new edu.ksu.cis.bandera.bfa.analysis.ofa.fi.StmtSwitch(null));
	}


	public static Analyzer getFSAnalyzer(String name) {
		return new Analyzer(name,
							new ASTIndexManager(),
							new edu.ksu.cis.bandera.bfa.analysis.ofa.fs.ExprSwitch(null, new LHSConnector()),
							new edu.ksu.cis.bandera.bfa.analysis.ofa.fs.RHSExprSwitch(null, new RHSConnector()),
							new edu.ksu.cis.bandera.bfa.analysis.ofa.fi.StmtSwitch(null));
	}
	/*
	public Set getValues(InvokeExpr e, Context c) {
		return new HashSet();
	}

	public Set getValues(NewExpr e, Context c) {
		return new HashSet();
	}

	public Set getValues(NewArrayExpr e, Context c) {
		return new HashSet();
	}

	public Set getValues(NewMultiArrayExpr e, Context c) {
		return new HashSet();
	}

	public Set getValues(Local l, Context c) {
		return new HashSet();
	}
	*/
	public Collection invokeExprResolution (NonStaticInvokeExpr e, SootMethod enclosingMethod) {
		Collection newExprs = bfa.getMethodVariant(enclosingMethod, context).getASTVariant(e.getBase(), context).getValues();
		Set ret = new HashSet();
		for (Iterator i = newExprs.iterator(); i.hasNext();) {
			 NewExpr expr = (NewExpr)i.next();
			 SootClass sc = bfa.getClass(expr.getBaseType().className);
			 ret.add(sc.getMethod(e.getMethod().getName()));
		} // end of for (Iterator i = newExprs.iterator(); i.hasNext();)
		return ret;
	}

}// Analyzer
