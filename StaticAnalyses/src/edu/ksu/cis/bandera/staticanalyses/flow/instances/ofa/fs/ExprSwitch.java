
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

import ca.mcgill.sable.soot.jimple.ArrayRef;
import ca.mcgill.sable.soot.jimple.DefinitionStmt;
import ca.mcgill.sable.soot.jimple.InstanceFieldRef;
import ca.mcgill.sable.soot.jimple.Local;
import ca.mcgill.sable.soot.jimple.ValueBox;

import ca.mcgill.sable.util.Iterator;

import edu.ksu.cis.bandera.staticanalyses.flow.AbstractStmtSwitch;
import edu.ksu.cis.bandera.staticanalyses.flow.FGNode;
import edu.ksu.cis.bandera.staticanalyses.flow.FGNodeConnector;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * 
 * The expression visitor used in flow sensitive mode of object flow analysis.
 * 
 * Created: Sun Jan 27 14:29:14 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class ExprSwitch
  extends edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.fi.ExprSwitch {
	/**
	 * 
	 * An instance of <code>Logger</code> used for logging purpose.
	 * 
	 */
	private static final Logger logger = LogManager.getLogger(ExprSwitch.class);

	/**
	 * 
	 * Creates a new <code>ExprSwitch</code> instance.
	 * 
	 *
	 * @param stmt the statement visitor which uses this instance of expression visitor.
	 * @param connector the connector to be used to connect the ast and non-ast nodes.
	 */
	public ExprSwitch(AbstractStmtSwitch stmt, FGNodeConnector connector) {
		super(stmt, connector);
	}

	/**
	 * 
	 * Handles the array reference expressions.  This calls <code>postProcessBase</code> to finish up processing.
	 * 
	 *
	 * @param e the array ref expression to be processed.
	 */
	public void caseArrayRef(ArrayRef e) {
		super.caseArrayRef(e);
		postProcessBase(e.getBaseBox());
	}

	/**
	 * 
	 * Handles the instance field reference expressions.  This calls <code>postProcessBase</code> to finish up processing.
	 * 
	 *
	 * @param e the instance field ref expression to be processed.
	 */
	public void caseInstanceFieldRef(InstanceFieldRef e) {
		super.caseInstanceFieldRef(e);
		postProcessBase(e.getBaseBox());
	}

	/**
	 * 
	 * Connects the flow graph nodes corresponding to definition of the primary to the use of the primary at the reference
	 * site.  This method assumes that the primary in a access expression is a local variable.  The idea is that once the
	 * nodes have been set up for the primary and the identifier, the nodes corresponding to the primary is connected
	 * according to the mode of operation to instigate flow of values into fields and array components according to the
	 * mode.
	 * 
	 *
	 * @param e the reference program point to be processed.
	 */
	public void postProcessBase(ValueBox e) {
		Local l = (Local)e.getValue();
		ValueBox backup = context.setProgramPoint(e);
		FGNode localNode = method.getASTNode(l);

		for(Iterator i = method.getDefsOfAt(l, stmt.getStmt()).iterator(); i.hasNext();) {
			DefinitionStmt defStmt = (DefinitionStmt)i.next();
			context.setProgramPoint(defStmt.getLeftOpBox());

			FGNode defNode = method.getASTNode(defStmt.getLeftOp());
			logger.debug("Local Def:" + defStmt.getLeftOp() + "\n" + defNode + context);
			defNode.addSucc(localNode);
		}
		 // end of for (Iterator i = defs.getDefsOfAt(e, stmt.stmt).iterator(); i.hasNext();)
		context.setProgramPoint(backup);
	}

	/**
	 * 
	 * Process the expression at the given program point.
	 * 
	 *
	 * @param vb the program point encapsulating the expression to be processed.
	 */
	public void process(ValueBox vb) {
		ValueBox temp = context.setProgramPoint(vb);
		super.process(vb);
		context.setProgramPoint(temp);
	}

	/**
	 * 
	 * Returns a new instance of this class.
	 * 
	 *
	 * @param o the statement visitor which shall use the created visitor instance.  This is of type
	 * 		  <code>AbstractStmtSwitch</code>.
	 *
	 * @return the new visitor instance.
	 */
	public Object prototype(Object o) {
		return new ExprSwitch((AbstractStmtSwitch)o, connector);
	}
}

/*****
 ChangeLog:

$Log$

*****/
