
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2002, 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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

package edu.ksu.cis.indus.staticanalyses.flow;

import edu.ksu.cis.indus.interfaces.IPrototype;

import edu.ksu.cis.indus.processing.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.jimple.Stmt;


/**
 * The statement visitor class.  This class provides the default implementation for all the statements that need to be dealt
 * at Jimple level in Bandera framework.  The class is tagged as <code>abstract</code> to force the users to extend the
 * class as required.  It extends <code>AbstractJimpleStmtSwitch</code>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public abstract class AbstractStmtSwitch
  extends soot.jimple.AbstractStmtSwitch
  implements IPrototype,
	  IStmtSwitch {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(AbstractStmtSwitch.class);

	/** 
	 * The context in which this object should process statements.  It is possible for this object to alter the context, but
	 * it should restore it back to it's initial state before returning from it's methods.
	 */
	protected final Context context;

	/** 
	 * The LHS expression visitor used to this object to process LHS expressions.
	 */
	protected final IExprSwitch lexpr;

	/** 
	 * The RHS expression visitor used to this object to process RHS expressions.
	 */
	protected final IExprSwitch rexpr;

	/** 
	 * The method variant in which this visitor is used.
	 */
	protected final IMethodVariant method;

	/**
	 * Creates a new <code>AbstractStmtSwitch</code> instance.  In non-prototype mode, all of the fields (declared  in this
	 * class) will be non-null after returning from the constructor.
	 *
	 * @param m the method variant in which this visitor is used.
	 */
	protected AbstractStmtSwitch(final IMethodVariant m) {
		method = m;

		if (m != null) {
			context = m.getContext();
			lexpr = m.getFA().getLHSExpr(this);
			rexpr = m.getFA().getRHSExpr(this);
		} else {
			context = null;
			lexpr = null;
			rexpr = null;
		}
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
	 * Handles situations when alien statement types are visited, i.e., there are no instructions available on how to handle
	 * a particular statement type.
	 *
	 * @param o the statement to be visited.
	 *
	 * @pre o != null
	 */
	public void defaultCase(final Object o) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(o + " is not handled.");
		}
	}

	/**
	 * Process the given statement.  The usual implementation would be visit the expressions in the statement.
	 *
	 * @param stmtToProcess the statement being visited or to be processed.
	 *
	 * @pre stmtToProcess != null
	 */
	public void process(final Stmt stmtToProcess) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: processing " + stmtToProcess);
		}

		final Stmt _temp = context.setStmt(stmtToProcess);
		stmtToProcess.apply(this);
		context.setStmt(_temp);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: processed " + stmtToProcess);
		}
	}
}

// End of File
