package edu.ksu.cis.bandera.bfa;


import ca.mcgill.sable.soot.jimple.Stmt;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


//AbstractStmtSwitch.java
/**
 * <p>The statement visitor class.  This class provides the default implementation for all the statements that need to be
 * dealt at Jimple level in Bandera framework.  The class is tagged as <code>abstract</code> to force the users to
 * extend the class as required.  It extends <code>AbstractJimpleStmtSwitch</code>.</p>
 *
 * <p>Created: Sun Jan 27 13:28:32 2002</p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */

public abstract class AbstractStmtSwitch extends ca.mcgill.sable.soot.jimple.AbstractStmtSwitch implements Prototype {

	/**
	 * <p>An instance of <code>Logger</code> used for logging purpose.</p>
	 *
	 */
	private static final Logger logger = LogManager.getLogger(AbstractStmtSwitch.class.getName());

	/**
	 * <p>The method variant in which this visitor is used.</p>
	 *
	 */
	protected final MethodVariant method;

	/**
	 * <p>The RHS expression visitor used to this object to process RHS expressions.</p>
	 *
	 */
	protected final AbstractExprSwitch rexpr;

	/**
	 * <p>The LHS expression visitor used to this object to process LHS expressions.</p>
	 *
	 */
	protected final AbstractExprSwitch lexpr;

	/**
	 * <p>The context in which this object should process statements.  It is possible for this object to alter the context,
	 * but it should restore it back to it's initial state before returning from it's methods.</p>
	 *
	 */
	protected final Context context;

	/**
	 * <p>The current statement this visitor is visiting.</p>
	 *
	 */
	protected Stmt stmt;

	/**
	 * <p>Creates a new <code>AbstractStmtSwitch</code> instance.</p>
	 *
	 * @param m the method variant in which this visitor is used.
	 */
	protected AbstractStmtSwitch (MethodVariant m){
		method = m;
		if (m == null) {
			context = null;
			lexpr = rexpr = null;
		} // end of if (m == null)
		else {
			context = m.context;
			lexpr = m.bfa.getLHSExpr(this);
			rexpr = m.bfa.getRHSExpr(this);
		} // end of if (m == null) else

	}

	/**
	 * <p>Handles situations when alien statement types are visited, i.e., there are no instructions available on how to
	 * handle a particular statement type.
	 *
	 * @param o the statement to be visited.
	 */
	public void defaultCase(Object o) {
		logger.debug(o + " is not handled.");
	}

	/**
	 * <p>Returns the current statement being visited.</p>
	 *
	 * @return the current statement being visited.
	 */
	public Stmt getStmt() {
		return stmt;
	}

	/**
	 * <p>Process the given statement.  The usual implementation would be visit the expressions in the statement.</p>
	 *
	 * @param stmt the statement being visited or to be processed.
	 */
	protected void process(Stmt stmt) {
		this.stmt = stmt;
		logger.debug(">>>>> Processing: " + stmt);
		stmt.apply(this);
		logger.debug("<<<<< Processing: " + stmt);
	}

	/**
	 * <p>This method is not supproted.</p>
	 *
	 * @throws <code>UnsupportedOperationException</code> if the operation is not supported.
	 */
	public Object prototype() {
		throw new UnsupportedOperationException("prototype() is not supported.");
	}

	/**
	 * <p>This method is not supproted.</p>
	 *
	 * @throws <code>UnsupportedOperationException</code> if the operation is not supported.
	 */
	public Object prototype(Object o) {
		throw new UnsupportedOperationException("prototype(Object) is not supported.");
	}

}// AbstractStmtSwitch
