
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import soot.ArrayType;
import soot.Modifier;
import soot.RefType;
import soot.SootField;
import soot.SootMethod;
import soot.Type;

import edu.ksu.cis.indus.staticanalyses.flow.AbstractAnalyzer;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractExprSwitch;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractIndexManager;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractStmtSwitch;
import edu.ksu.cis.indus.staticanalyses.flow.ClassManager;
import edu.ksu.cis.indus.staticanalyses.flow.ModeFactory;
import edu.ksu.cis.indus.staticanalyses.flow.modes.insensitive.IndexManager;
import edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.allocation.AllocationContext;
import edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.allocation.AllocationSiteSensitiveIndexManager;
import edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.flow.FlowSensitiveIndexManager;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;


/**
 * This  class serves as the interface to the external world for Object flow analysis information.
 * 
 * <p>
 * The values returned on querying this analysis are AST chunks corresponding to object allocation/creation sites.
 * </p>
 * 
 * <p>
 * Created: Wed Jan 30 18:49:43 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public final class OFAnalyzer
  extends AbstractAnalyzer {
	/**
	 * Creates a new <code>OFAnalyzer</code> instance.
	 *
	 * @param astim the prototype of the index manager to be used in conjunction with AST nodes.
	 * @param allocationim the prototype of the index manager to be used in conjunction with fields and arrays.
	 * @param lexpr the LHS expression visitor prototype.
	 * @param rexpr the RHS expression visitor prototype.
	 * @param stmt the statement visitor prototype.
	 *
	 * @pre astim != null and allocationim != null and lexpr != null and rexpr != null and stmt != null
	 */
	private OFAnalyzer(final AbstractIndexManager astim, final AbstractIndexManager allocationim,
		final AbstractExprSwitch lexpr, final AbstractExprSwitch rexpr, final AbstractStmtSwitch stmt) {
		super(new AllocationContext());

		ModeFactory mf = new ModeFactory();
		mf.setASTIndexManagerPrototype(astim);
		mf.setInstanceFieldIndexManagerPrototype(allocationim);
		mf.setArrayIndexManagerPrototype(allocationim);
		mf.setMethodIndexManagerPrototype(new IndexManager());
		mf.setStaticFieldIndexManagerPrototype(new IndexManager());
		mf.setNodePrototype(new OFAFGNode(null));
		mf.setStmtVisitorPrototype(stmt);
		mf.setLHSExprVisitorPrototype(lexpr);
		mf.setRHSExprVisitorPrototype(rexpr);
		mf.setClassManagerPrototype(new ClassManager(null));
		setModeFactory(mf);
	}

	/**
	 * Checks if the given type is a valid reference type.
	 *
	 * @param t is the type to checked.
	 *
	 * @return <code>true</code> if <code>t</code> is a valid reference type; <code>false</code>, otherwise.
	 *
	 * @pre t != null
	 */
	public static final boolean isReferenceType(final Type t) {
		return t instanceof RefType || t instanceof ArrayType;
	}

	/**
	 * Returns the analyzer that operates in flow insensitive and allocation-site insensitive modes.
	 *
	 * @return the instance of analyzer correponding to the given name.
	 *
	 * @post result != null
	 */
	public static OFAnalyzer getFIOIAnalyzer() {
		AbstractIndexManager temp = new IndexManager();

		return new OFAnalyzer(temp, temp,
			new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fi.ExprSwitch(null, new LHSConnector()),
			new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fi.ExprSwitch(null, new RHSConnector()),
			new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fi.StmtSwitch(null));
	}

	/**
	 * Returns the analyzer that operates in flow insensitive and allocation-site sensitive modes.
	 *
	 * @return the instance of analyzer correponding to the given name.
	 *
	 * @post result != null
	 */
	public static OFAnalyzer getFIOSAnalyzer() {
		OFAnalyzer temp =
			new OFAnalyzer(new IndexManager(), new AllocationSiteSensitiveIndexManager(),
				new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fi.ExprSwitch(null, new LHSConnector()),
				new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fi.ExprSwitch(null, new RHSConnector()),
				new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fi.StmtSwitch(null));

		return temp;
	}

	/**
	 * Returns the analyzer that operates in flow sensitive and allocation-site insensitive modes.
	 *
	 * @return the instance of analyzer correponding to the given name.
	 *
	 * @post result != null
	 */
	public static OFAnalyzer getFSOIAnalyzer() {
		return new OFAnalyzer(new FlowSensitiveIndexManager(), new IndexManager(),
			new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fs.ExprSwitch(null, new LHSConnector()),
			new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fs.RHSExprSwitch(null, new RHSConnector()),
			new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fi.StmtSwitch(null));
	}

	/**
	 * Returns the analyzer that operates in flow sensitive and allocation-site sensitive modes.
	 *
	 * @return the instance of analyzer correponding to the given name.
	 *
	 * @post result != null
	 */
	public static OFAnalyzer getFSOSAnalyzer() {
		OFAnalyzer temp =
			new OFAnalyzer(new FlowSensitiveIndexManager(), new AllocationSiteSensitiveIndexManager(),
				new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fs.ExprSwitch(null, new LHSConnector()),
				new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fs.RHSExprSwitch(null, new RHSConnector()),
				new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fi.StmtSwitch(null));
		return temp;
	}

	/**
	 * Returns values associated with the given field associated with the given allocation sites.
	 *
	 * @param f the field reqarding which information is requested.
	 * @param sites the collection of allocation sites that are of interest when extracting field information.
	 *
	 * @return a collection of values the field <code>f</code> may evaluate when associated with object created at allocation
	 * 		   sites given by <code>sites</code>.
	 *
	 * @pre f != null and sites != null
	 * @pre sites.oclIsKindOf(Collection(Object))
	 */
	public Collection getValues(final SootField f, final Collection sites) {
		Object temp = null;
		Collection retValues;
		AllocationContext ctxt = (AllocationContext) context;

		if (Modifier.isStatic(f.getModifiers())) {
			retValues = getValues(f);
		} else {
			retValues = new HashSet();
			temp = ctxt.getAllocationSite();

			for (Iterator i = sites.iterator(); i.hasNext();) {
				ctxt.setAllocationSite(i.next());
				retValues.addAll(getValues(f));
			}
			ctxt.setAllocationSite(temp);
		}
		return retValues.isEmpty() ? Collections.EMPTY_SET
								   : retValues;
	}

	/**
	 * Checks if the <code>method</code> was analyzed in the <code>context</code> in this analysis.
	 *
	 * @param method to be checked if it was analyzed.
	 * @param ctxt in which the method was analyzed.
	 *
	 * @return <code>true</code> if <code>method</code> was analyzed; <code>false</code>, otherwise.
	 *
	 * @pre method != null and ctxt != null
	 */
	public boolean wasAnalyzed(final SootMethod method, final AllocationContext ctxt) {
		return fa.queryMethodVariant(method, ctxt) != null;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.5  2003/08/26 17:53:56  venku
   Actually we can use the types to cut down the number of edges
   between the flow nodes. The current fix uses a method in OFAnalyzer
   to check for reference types, only if the type matches the given expression
   is processed.  However, this does not apply for staticfield, instancefield, and
   array access expressions.
   Revision 1.4  2003/08/17 10:48:34  venku
   Renamed BFA to FA.  Also renamed bfa variables to fa.
   Ripple effect was huge.
   Revision 1.3  2003/08/15 03:39:53  venku
   Spruced up documentation and specification.
   Tightened preconditions in the interface such that they can be loosened later on in implementaions.
   Renamed a few fields/parameter variables to avoid name confusion.
   Revision 1.2  2003/08/09 21:52:57  venku
   Change parameter names.
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
 */
