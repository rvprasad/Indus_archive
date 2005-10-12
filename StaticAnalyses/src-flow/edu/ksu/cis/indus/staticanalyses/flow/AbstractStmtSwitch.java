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

import edu.ksu.cis.indus.processing.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.jimple.Stmt;

/**
 * The statement visitor class. This class provides the default implementation for all the statements that need to be dealt at
 * Jimple level in Bandera framework. The class is tagged as <code>abstract</code> to force the users to extend the class as
 * required. It extends <code>AbstractJimpleStmtSwitch</code>.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 * @param <S> DOCUMENT ME!
 * @param <N> DOCUMENT ME!
 * @param <LE> DOCUMENT ME!
 * @param <RE> DOCUMENT ME!
 */
public abstract class AbstractStmtSwitch<S extends AbstractStmtSwitch<S, N, LE, RE>, N extends IFGNode<N, ?>, LE extends IExprSwitch<LE, N>, RE extends IExprSwitch<RE, N>>
		extends soot.jimple.AbstractStmtSwitch
		implements IStmtSwitch<S> {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractStmtSwitch.class);

	/**
	 * The context in which this object should process statements. It is possible for this object to alter the context, but it
	 * should restore it back to it's initial state before returning from it's methods.
	 */
	protected final Context context;

	/**
	 * The LHS expression visitor used to this object to process LHS expressions.
	 */
	protected final LE lexpr;

	/**
	 * The method variant in which this visitor is used.
	 */
	protected final IMethodVariant<N, ?, ?, S> method;

	/**
	 * The RHS expression visitor used to this object to process RHS expressions.
	 */
	protected final RE rexpr;

	/**
	 * Creates a new <code>AbstractStmtSwitch</code> instance. In non-prototype mode, all of the fields (declared in this
	 * class) will be non-null after returning from the constructor.
	 * 
	 * @param m the method variant in which this visitor is used.
	 */
	protected AbstractStmtSwitch(final IMethodVariant<N, LE, RE, S> m) {
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
	 * Handles situations when alien statement types are visited, i.e., there are no instructions available on how to handle a
	 * particular statement type.
	 * 
	 * @param o the statement to be visited.
	 * @pre o != null
	 */
	@Override public void defaultCase(final Object o) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(o + " is not handled.");
		}
	}

	/**
	 * This method is not supproted. To be implemented by subclasses.
	 * 
	 * @param o is ignored.
	 * @return (This method will raise an exception.)
	 * @throws UnsupportedOperationException as the operation is not supported.
	 */
	public S getClone(@SuppressWarnings("unused") final Object... o) {
		throw new UnsupportedOperationException("prototype(Object) is not supported.");
	}

	/**
	 * Process the given statement. The usual implementation would be visit the expressions in the statement.
	 * 
	 * @param stmtToProcess the statement being visited or to be processed.
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
