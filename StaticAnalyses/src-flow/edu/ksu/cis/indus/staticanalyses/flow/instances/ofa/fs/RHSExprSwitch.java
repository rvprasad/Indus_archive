
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (C) 2003, 2004, 2005
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://indus.projects.cis.ksu.edu/).
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
 *                http://indus.projects.cis.ksu.edu/
 */

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fs;

import soot.Local;
import soot.ValueBox;

import soot.jimple.DefinitionStmt;

import edu.ksu.cis.indus.staticanalyses.flow.AbstractStmtSwitch;
import edu.ksu.cis.indus.staticanalyses.flow.IFGNode;
import edu.ksu.cis.indus.staticanalyses.flow.IFGNodeConnector;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.List;


/**
 * This is the RHS expression visitor.  It provides implementation to handle locals on RHS in flow sensitive mode.
 * 
 * <p>
 * Created: Sun Jan 27 14:29:14 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class RHSExprSwitch
  extends ExprSwitch {
	/**
	 * An  instance of <code>Logger</code> used for logging purpose.
	 */
	private static final Logger LOGGER = LogManager.getLogger(RHSExprSwitch.class);

	/**
	 * Creates a new <code>RHSExprSwitch</code> instance.
	 *
	 * @param stmtSwitch the statement visitor which uses this instance of expression visitor.
	 * @param nodeConnector the connector to be used to connect the ast and non-ast nodes.
	 *
	 * @pre stmtSwitch != null and nodeConnector != null
	 */
	public RHSExprSwitch(final AbstractStmtSwitch stmtSwitch, final IFGNodeConnector nodeConnector) {
		super(stmtSwitch, nodeConnector);
	}

	/**
	 * Returns a new instance of this class.
	 *
	 * @param o the statement visitor which shall use the created visitor instance.  This is of type
	 * 		  <code>AbstractStmtSwitch</code>.
	 *
	 * @return the new visitor instance.
	 *
	 * @pre o != null
	 * @post result.oclIsKindOf(AbstractExprSwitch)
	 */
	public Object getClone(final Object o) {
		return new RHSExprSwitch((AbstractStmtSwitch) o, connector);
	}

	/**
	 * Handles the expression containing local variables on rhs.  It connects the flow graph nodes corresponding to def sites
	 * of the locals to the nodes corresponding to the  use sites of the locals.
	 *
	 * @param e the local to be visited.
	 *
	 * @pre e != null
	 */
	public void caseLocal(final Local e) {
		IFGNode ast = method.getASTNode(e);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Local:" + e + "\n" + ast);
		}

		List l = method.getDefsOfAt(e, stmt.getStmt());

		if (l != null) {
			ValueBox temp = context.getProgramPoint();

			for (Iterator i = l.iterator(); i.hasNext();) {
				DefinitionStmt defStmt = (DefinitionStmt) i.next();
				context.setProgramPoint(defStmt.getLeftOpBox());

				IFGNode defNode = method.getASTNode(defStmt.getLeftOp());

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Local Def:" + defStmt.getLeftOp() + "\n" + defNode + context);
				}
				defNode.addSucc(ast);
			}
			context.setProgramPoint(temp);
		}
		setResult(ast);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
   Revision 1.5  2003/05/22 22:18:32  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
