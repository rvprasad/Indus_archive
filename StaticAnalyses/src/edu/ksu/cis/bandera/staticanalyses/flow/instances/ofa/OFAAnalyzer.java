package edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa;


import edu.ksu.cis.bandera.staticanalyses.flow.AbstractAnalyzer;
import edu.ksu.cis.bandera.staticanalyses.flow.AbstractExprSwitch;
import edu.ksu.cis.bandera.staticanalyses.flow.AbstractIndexManager;
import edu.ksu.cis.bandera.staticanalyses.flow.AbstractStmtSwitch;
import edu.ksu.cis.bandera.staticanalyses.flow.ClassManager;
import edu.ksu.cis.bandera.staticanalyses.flow.Context;
import edu.ksu.cis.bandera.staticanalyses.flow.ModeFactory;
import edu.ksu.cis.bandera.staticanalyses.flow.modes.insensitive.IndexManager;
import edu.ksu.cis.bandera.staticanalyses.flow.modes.sensitive.AllocationSiteSensitiveIndexManager;
import edu.ksu.cis.bandera.staticanalyses.flow.modes.sensitive.FlowSensitiveASTIndexManager;

import ca.mcgill.sable.soot.Modifier;
import ca.mcgill.sable.soot.SootClass;
import ca.mcgill.sable.soot.SootField;
import ca.mcgill.sable.soot.SootMethod;
import ca.mcgill.sable.soot.jimple.InstanceFieldRef;
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

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

// OFAAnalyzer.java
/**
 * <p>This class serves as the interface to the external world for Object flow analysis information.</p>
 *
 * Created: Wed Jan 30 18:49:43 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ 
 */

public class OFAAnalyzer extends AbstractAnalyzer {

	/**
	 * <p>An instance of <code>Logger</code> used for logging purpose.</p>
	 *
	 */
	private static final Logger logger = LogManager.getLogger(OFAAnalyzer.class);

	/**
	 * <p>Creates a new <code>OFAAnalyzer</code> instance.</p>
	 *
	 * @param name the name of the analysis instance.
	 * @param astim the prototype of the index manager to be used in conjunction with AST nodes.
	 * @param allocationim the prototype of the index manager to be used in conjunction with fields and arrays.
	 * @param lexpr the LHS expression visitor prototype.
	 * @param rexpr the RHS expression visitor prototype.
	 * @param stmt the statement visitor prototype.
	 */
	private OFAAnalyzer (String name, AbstractIndexManager astim, AbstractIndexManager allocationim,
					  AbstractExprSwitch lexpr, AbstractExprSwitch rexpr, AbstractStmtSwitch stmt) {
		super(name, new ModeFactory(astim,
									allocationim,
									allocationim,
									new IndexManager(),
									new IndexManager(),
									new OFAFGNode(null),
									stmt, lexpr, rexpr,
									new ClassManager(null)));
	}



	/**
	 * <p>Returns the analyzer that operates in flow insensitive and allocation-site insensitive modes.</p>
	 *
	 * @param name the name of the analysis instance.
	 * @return the instance of analyzer correponding to the given name.
	 */
	public static OFAAnalyzer getFIOIAnalyzer(String name) {
		AbstractIndexManager temp = new IndexManager();
		return new OFAAnalyzer(name,
							temp,
							temp,
							new edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.fi.ExprSwitch(null, new LHSConnector()),
							new edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.fi.ExprSwitch(null, new RHSConnector()),
							new edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.fi.StmtSwitch(null));
	}


	/**
	 * <p>Returns the analyzer that operates in flow sensitive and allocation-site insensitive modes.</p>
	 *
	 * @param name the name of the analysis instance.
	 * @return the instance of analyzer correponding to the given name.
	 */
	public static OFAAnalyzer getFSOIAnalyzer(String name) {
		return new OFAAnalyzer(name,
							new FlowSensitiveASTIndexManager(),
							new IndexManager(),
							new edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.fs.ExprSwitch(null, new LHSConnector()),
							new edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.fs.RHSExprSwitch(null, new RHSConnector()),
							new edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.fi.StmtSwitch(null));
	}

	/**
	 * <p>Returns the analyzer that operates in flow insensitive and allocation-site sensitive modes.</p>
	 *
	 * @param name the name of the analysis instance.
	 * @return the instance of analyzer correponding to the given name.
	 */
	public static OFAAnalyzer getFIOSAnalyzer(String name) {
		OFAAnalyzer temp = new OFAAnalyzer(name,
									 new IndexManager(),
									 new AllocationSiteSensitiveIndexManager(),
									 new edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.fi.ExprSwitch(null, new LHSConnector()),
									 new edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.fi.ExprSwitch(null, new RHSConnector()),
									 new edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.fi.StmtSwitch(null));
		temp.context.setAllocationSite(new HashSet());
		return temp;
	}


	/**
	 * <p>Returns the analyzer that operates in flow sensitive and allocation-site sensitive modes.</p>
	 *
	 * @param name the name of the analysis instance.
	 * @return the instance of analyzer correponding to the given name.
	 */
	public static OFAAnalyzer getFSOSAnalyzer(String name) {
		OFAAnalyzer temp = new OFAAnalyzer(name,
									 new FlowSensitiveASTIndexManager(),
									 new AllocationSiteSensitiveIndexManager(),
									 new edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.fs.ExprSwitch(null, new LHSConnector()),
									 new edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.fs.RHSExprSwitch(null, new RHSConnector()),
									 new edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.fi.StmtSwitch(null));
		temp.context.setAllocationSite(new HashSet());
		return temp;
	}

	/**
	 * <p>Returns values associated with the given field associated with the given allocation sites.</p>
	 *
	 * @param f the field reqarding which information is requested.
	 * @param sites the collection of allocation sites that are of interest when extracting field information.
	 * @return a collection of values the field <code>f</code> may evaluate when associated with object created at allocation
	 * sites given by <code>sites</code>.
	 */
	public Collection getValues(SootField f, Collection sites) {
		Object temp = null;
		Collection retValues;

		if (Modifier.isStatic(f.getModifiers())) {
			retValues = getValues(f);
		} else {
			retValues = new HashSet();
			temp = context.getAllocationSite();
			for (Iterator i = sites.iterator(); i.hasNext();) {
				 context.setAllocationSite(i.next());
				 retValues.addAll(getValues(f));
			} // end of for (Iterator i = sites.iterator(); i.hasNext();)
			context.setAllocationSite(temp);
		} // end of else

		return retValues;
	}

	/**
	 * <p>Returns the set of method implementations that shall be invoked at the given callsite expression in the given
	 * method.</p>
	 *
	 * @param e the virtual method call site.
	 * @param enclosingMethod the method in which the expression occurs.
	 * @return the set of method implementations that result at the given call site.
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

}// OFAAnalyzer
