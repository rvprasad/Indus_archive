
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

package edu.ksu.cis.bandera.staticanalyses.flow.instances.symfa;

import ca.mcgill.sable.soot.SootClassManager;

import edu.ksu.cis.bandera.staticanalyses.flow.AbstractAnalyzer;
import edu.ksu.cis.bandera.staticanalyses.flow.ModeFactory;
import edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.LHSConnector;
import edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.OFAFGNode;
import edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.RHSConnector;
import edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.fi.StmtSwitch;
import edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.fs.ExprSwitch;
import edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.fs.RHSExprSwitch;
import edu.ksu.cis.bandera.staticanalyses.flow.modes.insensitive.IndexManager;
import edu.ksu.cis.bandera.staticanalyses.flow.modes.sensitive.flow.FlowSensitiveIndexManager;
import edu.ksu.cis.bandera.staticanalyses.flow.modes.sensitive.symbolic.SymbolSensitiveIndexManager;
import edu.ksu.cis.bandera.staticanalyses.flow.modes.sensitive.symbolic.SymbolicContext;
import edu.ksu.cis.bandera.staticanalyses.support.Util;


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
	 *
	 * @param name of this analyzer.
	 */
	public SymbolicFlowAnalyzer(String name) {
		super(name,
			new ModeFactory(new FlowSensitiveIndexManager(), // AST indexmanager 
				new SymbolSensitiveIndexManager(), // Array indexmanager
				new SymbolSensitiveIndexManager(), // instance field indexmanager 
				new IndexManager(), // static field indexmanager
				new IndexManager(), // method indexmanager
				new OFAFGNode(null), // node prototype
				new StmtSwitch(null), // statement walker prototype
				new ExprSwitch(null, new LHSConnector()), // lhs expression walker prototype 
				new RHSExprSwitch(null, new RHSConnector()), // rhs expression walker prototype 
				null // classmanager
		), new SymbolicContext());
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param scm DOCUMENT ME!
	 */
	public void analyzer(SootClassManager scm) {
		analyze(scm, Util.convert("java.util.ArrayList", scm.getClasses()));
	}
}

/*****
 ChangeLog:

$Log$

*****/
