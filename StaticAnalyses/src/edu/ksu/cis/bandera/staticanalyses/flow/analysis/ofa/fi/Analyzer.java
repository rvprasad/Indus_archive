package edu.ksu.cis.bandera.bfa.analysis.ofa.fi;


import edu.ksu.cis.bandera.bfa.AbstractAnalyzer;
import edu.ksu.cis.bandera.bfa.Context;
import edu.ksu.cis.bandera.bfa.ModeFactory;
import edu.ksu.cis.bandera.bfa.analysis.ofa.FGNode;
import edu.ksu.cis.bandera.bfa.analysis.ofa.LHSConnector;
import edu.ksu.cis.bandera.bfa.analysis.ofa.RHSConnector;
import edu.ksu.cis.bandera.bfa.modes.insensitive.IndexManager;

import ca.mcgill.sable.soot.SootClass;
import ca.mcgill.sable.soot.SootMethod;
import ca.mcgill.sable.soot.jimple.NewExpr;
import ca.mcgill.sable.soot.jimple.NonStaticInvokeExpr;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Category;

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

	private static final Category cat = Category.getInstance(Analyzer.class.getName());

	public final Context context;

	public Analyzer (String name){
		super(name, new ModeFactory(new IndexManager(),
									new IndexManager(),
									new IndexManager(),
									new IndexManager(),
									new IndexManager(),
									new FGNode(null),
									new StmtSwitch(null),
									new ExprSwitch(null, new LHSConnector()),
									new ExprSwitch(null, new RHSConnector())));
		context = new Context();
	}

	public Collection invokeExprResolution(NonStaticInvokeExpr e, SootMethod enclosingMethod) {
		Collection newExprs = bfa.getMethodVariant(enclosingMethod, context)
			.getASTVariant(e.getBase(), context).getValues();
		Set ret = new HashSet();
		for (Iterator i = newExprs.iterator(); i.hasNext();) {
			 NewExpr expr = (NewExpr)i.next();
			 SootClass sc = bfa.getClass(expr.getBaseType().className);
			 ret.add(sc.getMethod(e.getMethod().getName()));
		} // end of for (Iterator i = newExprs.iterator(); i.hasNext();)
		return ret;
	}

}// Analyzer
