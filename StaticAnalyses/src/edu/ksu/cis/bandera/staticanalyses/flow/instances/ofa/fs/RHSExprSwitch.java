
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

package edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.fs;

import ca.mcgill.sable.soot.jimple.DefinitionStmt;
import ca.mcgill.sable.soot.jimple.Local;
import ca.mcgill.sable.soot.jimple.ValueBox;

import ca.mcgill.sable.util.Iterator;
import ca.mcgill.sable.util.List;

import edu.ksu.cis.bandera.staticanalyses.flow.AbstractStmtSwitch;
import edu.ksu.cis.bandera.staticanalyses.flow.FGNode;
import edu.ksu.cis.bandera.staticanalyses.flow.FGNodeConnector;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


/**
 * This is the RHS expression visitor.  It provides implementation to handle locals on RHS in flow sensitive mode. Created:
 * Sun Jan 27 14:29:14 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class RHSExprSwitch
  extends ExprSwitch {
	/**
	 * An  instance of <code>Logger</code> used for logging purpose.
	 */
	private static final Logger logger = LogManager.getLogger(RHSExprSwitch.class);

	/**
	 * Creates a new <code>RHSExprSwitch</code> instance.
	 *
	 * @param stmt the statement visitor which uses this instance of expression visitor.
	 * @param connector the connector to be used to connect the ast and non-ast nodes.
	 */
	public RHSExprSwitch(AbstractStmtSwitch stmt, FGNodeConnector connector) {
		super(stmt, connector);
	}

	/**
	 * Handles the expression containing local variables on rhs.  It connects the flow graph nodes corresponding to def sites
	 * of the locals to the nodes corresponding to the  use sites of the locals.
	 *
	 * @param e the local to be visited.
	 */
	public void caseLocal(Local e) {
		FGNode ast = method.getASTNode(e);
		logger.debug("Local:" + e + "\n" + ast);

		List l = method.getDefsOfAt(e, stmt.getStmt());

		if(l != null) {
			ValueBox temp = context.getProgramPoint();

			for(Iterator i = l.iterator(); i.hasNext();) {
				DefinitionStmt defStmt = (DefinitionStmt) i.next();
				context.setProgramPoint(defStmt.getLeftOpBox());

				FGNode defNode = method.getASTNode(defStmt.getLeftOp());
				logger.debug("Local Def:" + defStmt.getLeftOp() + "\n" + defNode + context);
				defNode.addSucc(ast);
			}

			// end of for (Iterator i = defs.getDefsOfAt(e, stmt.stmt).iterator(); i.hasNext();)
			context.setProgramPoint(temp);
		}

		// end of if (l != null)
		setResult(ast);
	}

	/**
	 * Returns a new instance of this class.
	 *
	 * @param o the statement visitor which shall use the created visitor instance.  This is of type
	 * 		  <code>AbstractStmtSwitch</code>.
	 *
	 * @return the new visitor instance.
	 */
	public Object prototype(Object o) {
		return new RHSExprSwitch((AbstractStmtSwitch) o, connector);
	}
}

/*****
 ChangeLog:

$Log$

*****/
