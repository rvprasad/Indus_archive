
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

package edu.ksu.cis.bandera.staticanalyses.flow;

import ca.mcgill.sable.soot.jimple.Stmt;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


//AbstractStmtSwitch.java

/**
 * <p>
 * The statement visitor class.  This class provides the default implementation for all the statements that need to be dealt
 * at Jimple level in Bandera framework.  The class is tagged as <code>abstract</code> to force the users to extend the
 * class as required.  It extends <code>AbstractJimpleStmtSwitch</code>.
 * </p>
 * 
 * <p>
 * Created: Sun Jan 27 13:28:32 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public abstract class AbstractStmtSwitch
  extends ca.mcgill.sable.soot.jimple.AbstractStmtSwitch
  implements Prototype {
	/**
	 * <p>
	 * An instance of <code>Logger</code> used for logging purpose.
	 * </p>
	 */
	private static final Logger logger = LogManager.getLogger(AbstractStmtSwitch.class.getName());

	/**
	 * <p>
	 * The LHS expression visitor used to this object to process LHS expressions.
	 * </p>
	 */
	protected final AbstractExprSwitch lexpr;

	/**
	 * <p>
	 * The RHS expression visitor used to this object to process RHS expressions.
	 * </p>
	 */
	protected final AbstractExprSwitch rexpr;

	/**
	 * <p>
	 * The context in which this object should process statements.  It is possible for this object to alter the context, but
	 * it should restore it back to it's initial state before returning from it's methods.
	 * </p>
	 */
	protected final Context context;

	/**
	 * <p>
	 * The method variant in which this visitor is used.
	 * </p>
	 */
	protected final MethodVariant method;

	/**
	 * <p>
	 * The current statement this visitor is visiting.
	 * </p>
	 */
	protected Stmt stmt;

	/**
	 * <p>
	 * Creates a new <code>AbstractStmtSwitch</code> instance.
	 * </p>
	 *
	 * @param m the method variant in which this visitor is used.
	 */
	protected AbstractStmtSwitch(MethodVariant m) {
		method = m;

		if(m == null) {
			context = null;
			lexpr = rexpr = null;
		} // end of if (m == null)
		else {
			context = m.context;
			lexpr = m.bfa.getLHSExpr(this);
			rexpr = m.bfa.getRHSExpr(this);
		}

		// end of if (m == null) else
	}

	/**
	 * <p>
	 * Returns the current statement being visited.
	 * </p>
	 *
	 * @return the current statement being visited.
	 */
	public Stmt getStmt() {
		return stmt;
	}

	/**
	 * <p>
	 * Handles situations when alien statement types are visited, i.e., there are no instructions available on how to handle
	 * a particular statement type.
	 * </p>
	 *
	 * @param o the statement to be visited.
	 */
	public void defaultCase(Object o) {
		logger.debug(o + " is not handled.");
	}

	/**
	 * <p>
	 * This method is not supproted.
	 * </p>
	 *
	 * @return (This method will raise an exception.)
	 *
	 * @throws UnsupportedOperationException as the operation is not supported.
	 */
	public Object prototype() {
		throw new UnsupportedOperationException("prototype() is not supported.");
	}

	/**
	 * <p>
	 * This method is not supproted.
	 * </p>
	 *
	 * @param o is ignored.
	 *
	 * @return (This method will raise an exception.)
	 *
	 * @throws UnsupportedOperationException as the operation is not supported.
	 */
	public Object prototype(Object o) {
		throw new UnsupportedOperationException("prototype(Object) is not supported.");
	}

	/**
	 * <p>
	 * Process the given statement.  The usual implementation would be visit the expressions in the statement.
	 * </p>
	 *
	 * @param stmt the statement being visited or to be processed.
	 */
	protected void process(Stmt stmt) {
		this.stmt = stmt;
		logger.debug(">>>>> Processing: " + stmt);
		stmt.apply(this);
		logger.debug("<<<<< Processing: " + stmt);
	}
}

/*****
 ChangeLog:

$Log$

*****/
