
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import soot.Modifier;
import soot.SootField;
import soot.SootMethod;

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
import java.util.HashSet;
import java.util.Iterator;


/**
 * This  class serves as the interface to the external world for Object flow analysis information.
 * 
 * <p>
 * The values returned on querying this analysis are AST chunks corresponding to object allocation/creation sites.
 * </p>
 * Created: Wed Jan 30 18:49:43 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public final class OFAnalyzer
  extends AbstractAnalyzer {
	/**
	 * <p>
	 * Creates a new <code>OFAnalyzer</code> instance.
	 * </p>
	 *
	 * @param astim the prototype of the index manager to be used in conjunction with AST nodes.
	 * @param allocationim the prototype of the index manager to be used in conjunction with fields and arrays.
	 * @param lexpr the LHS expression visitor prototype.
	 * @param rexpr the RHS expression visitor prototype.
	 * @param stmt the statement visitor prototype.
	 */
	private OFAnalyzer(AbstractIndexManager astim, AbstractIndexManager allocationim, AbstractExprSwitch lexpr,
		AbstractExprSwitch rexpr, AbstractStmtSwitch stmt) {
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
	 * <p>
	 * Returns the analyzer that operates in flow insensitive and allocation-site insensitive modes.
	 * </p>
	 *
	 * @return the instance of analyzer correponding to the given name.
	 */
	public static OFAnalyzer getFIOIAnalyzer() {
		AbstractIndexManager temp = new IndexManager();

		return new OFAnalyzer(temp, temp,
			new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fi.ExprSwitch(null, new LHSConnector()),
			new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fi.ExprSwitch(null, new RHSConnector()),
			new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fi.StmtSwitch(null));
	}

	/**
	 * <p>
	 * Returns the analyzer that operates in flow insensitive and allocation-site sensitive modes.
	 * </p>
	 *
	 * @return the instance of analyzer correponding to the given name.
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
	 * <p>
	 * Returns the analyzer that operates in flow sensitive and allocation-site insensitive modes.
	 * </p>
	 *
	 * @return the instance of analyzer correponding to the given name.
	 */
	public static OFAnalyzer getFSOIAnalyzer() {
		return new OFAnalyzer(new FlowSensitiveIndexManager(), new IndexManager(),
			new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fs.ExprSwitch(null, new LHSConnector()),
			new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fs.RHSExprSwitch(null, new RHSConnector()),
			new edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fi.StmtSwitch(null));
	}

	/**
	 * <p>
	 * Returns the analyzer that operates in flow sensitive and allocation-site sensitive modes.
	 * </p>
	 *
	 * @return the instance of analyzer correponding to the given name.
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
	 * <p>
	 * Returns values associated with the given field associated with the given allocation sites.
	 * </p>
	 *
	 * @param f the field reqarding which information is requested.
	 * @param sites the collection of allocation sites that are of interest when extracting field information.
	 *
	 * @return a collection of values the field <code>f</code> may evaluate when associated with object created at allocation
	 * 		   sites given by <code>sites</code>.
	 */
	public Collection getValues(SootField f, Collection sites) {
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
		return retValues;
	}

	/**
	 * <p>
	 * Checks if the <code>method</code> was analyzed in the <code>context</code> in this analysis.
	 * </p>
	 *
	 * @param method to be checked if it was analyzed.
	 * @param ctxt in which the method was analyzed.
	 *
	 * @return <code>true</code> if <code>method</code> was analyzed; <code>false</code>, otherwise.
	 */
	public boolean wasAnalyzed(SootMethod method, AllocationContext ctxt) {
		return bfa.queryMethodVariant(method, ctxt) != null;
	}
}

/*****
 ChangeLog:

$Log$
Revision 1.1  2003/08/07 06:40:24  venku
Major:
 - Moved the package under indus umbrella.


*****/
