
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

package edu.ksu.cis.indus.staticanalyses.flow;

import soot.jimple.Stmt;

import edu.ksu.cis.indus.interfaces.IPrototype;
import edu.ksu.cis.indus.staticanalyses.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * The statement visitor class.  This class provides the default implementation for all the statements that need to be dealt
 * at Jimple level in Bandera framework.  The class is tagged as <code>abstract</code> to force the users to extend the
 * class as required.  It extends <code>AbstractJimpleStmtSwitch</code>.
 * 
 * <p>
 * Created: Sun Jan 27 13:28:32 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public abstract class AbstractStmtSwitch
  extends soot.jimple.AbstractStmtSwitch
  implements IPrototype {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(AbstractStmtSwitch.class);

	/**
	 * The LHS expression visitor used to this object to process LHS expressions.
	 *
	 * @invariant lexpr != null
	 */
	protected final AbstractExprSwitch lexpr;

	/**
	 * The RHS expression visitor used to this object to process RHS expressions.
	 *
	 * @invariant rexpr != null
	 */
	protected final AbstractExprSwitch rexpr;

	/**
	 * The context in which this object should process statements.  It is possible for this object to alter the context, but
	 * it should restore it back to it's initial state before returning from it's methods.
	 *
	 * @invariant context != null
	 */
	protected final Context context;

	/**
	 * The method variant in which this visitor is used.
	 *
	 * @invariant method != null
	 */
	protected final MethodVariant method;

	/**
	 * The current statement this visitor is visiting.
	 */
	protected Stmt currentStmt;

	/**
	 * Creates a new <code>AbstractStmtSwitch</code> instance.
	 *
	 * @param m the method variant in which this visitor is used.
	 *
	 * @pre m != null
	 */
	protected AbstractStmtSwitch(final MethodVariant m) {
		method = m;

		context = m._context;
		lexpr = m._bfa.getLHSExpr(this);
		rexpr = m._bfa.getRHSExpr(this);
	}

	/**
	 * This method is not supproted. To be implemented by subclasses.
	 *
	 * @return (This method will raise an exception.)
	 *
	 * @throws UnsupportedOperationException as the operation is not supported.
	 */
	public Object getClone() {
		throw new UnsupportedOperationException("prototype() is not supported.");
	}

	/**
	 * This method is not supproted. To be implemented by subclasses.
	 *
	 * @param o is ignored.
	 *
	 * @return (This method will raise an exception.)
	 *
	 * @throws UnsupportedOperationException as the operation is not supported.
	 */
	public Object getClone(final Object o) {
		throw new UnsupportedOperationException("prototype(Object) is not supported.");
	}

	/**
	 * Returns the current statement being visited.
	 *
	 * @return the current statement being visited.
	 */
	public Stmt getCurrentStmt() {
		return currentStmt;
	}

	/**
	 * Handles situations when alien statement types are visited, i.e., there are no instructions available on how to handle
	 * a particular statement type.
	 *
	 * @param o the statement to be visited.
	 *
	 * @pre o != null
	 */
	public void defaultCase(final Object o) {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info(o + " is not handled.");
		}
	}

	/**
	 * Process the given statement.  The usual implementation would be visit the expressions in the statement.
	 *
	 * @param stmtToProcess the statement being visited or to be processed.
	 *
	 * @pre stmtToProcess != null
	 */
	protected void process(final Stmt stmtToProcess) {
		this.currentStmt = stmtToProcess;
		currentStmt.apply(this);
	}
}

/*
   ChangeLog:
   
   $Log$
   
   Revision 1.2  2003/08/12 18:40:11  venku
   Ripple effect of moving IPrototype to Indus.
   
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
    
   Revision 0.10  2003/05/22 22:18:31  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
