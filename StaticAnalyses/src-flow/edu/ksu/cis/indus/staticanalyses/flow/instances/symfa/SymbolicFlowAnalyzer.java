
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.symfa;

import soot.Scene;

import edu.ksu.cis.indus.staticanalyses.flow.AbstractAnalyzer;
import edu.ksu.cis.indus.staticanalyses.flow.ClassManager;
import edu.ksu.cis.indus.staticanalyses.flow.ModeFactory;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.LHSConnector;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAFGNode;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.RHSConnector;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fi.StmtSwitch;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fs.ExprSwitch;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fs.RHSExprSwitch;
import edu.ksu.cis.indus.staticanalyses.flow.modes.insensitive.IndexManager;
import edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.flow.FlowSensitiveIndexManager;
import edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.symbolic.SymbolSensitiveIndexManager;
import edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.symbolic.SymbolicContext;


/**
 * This class uses BFA framework to perform intra-procedural symbolic analysis.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class SymbolicFlowAnalyzer
  extends AbstractAnalyzer {
	/**
	 * Creates a new SymbolicFlowAnalyzer object.
	 */
	public SymbolicFlowAnalyzer() {
		super(new SymbolicContext());

		ModeFactory mf = new ModeFactory();
		mf.setASTIndexManagerPrototype(new FlowSensitiveIndexManager());
		mf.setArrayIndexManagerPrototype(new SymbolSensitiveIndexManager());
		mf.setInstanceFieldIndexManagerPrototype(new SymbolSensitiveIndexManager());
		mf.setStaticFieldIndexManagerPrototype(new IndexManager());
		mf.setMethodIndexManagerPrototype(new IndexManager());
		mf.setNodePrototype(new OFAFGNode(null));
		mf.setStmtVisitorPrototype(new StmtSwitch(null));
		mf.setLHSExprVisitorPrototype(new ExprSwitch(null, new LHSConnector()));
		mf.setRHSExprVisitorPrototype(new RHSExprSwitch(null, new RHSConnector()));
		mf.setClassManagerPrototype(new ClassManager(null));
		setModeFactory(mf);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param scm DOCUMENT ME!
	 */
	public void analyzer(Scene scm) {
		analyze(scm, scm.getClasses());
	}
}

/*****
 ChangeLog:

$Log$

*****/
