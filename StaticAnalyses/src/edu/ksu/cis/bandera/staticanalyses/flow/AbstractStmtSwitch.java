package edu.ksu.cis.bandera.bfa;


import ca.mcgill.sable.soot.jimple.Stmt;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


/**
 * AbstractStmtSwitch.java
 *
 *
 * Created: Sun Jan 27 13:28:32 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public abstract class AbstractStmtSwitch
	extends ca.mcgill.sable.soot.jimple.AbstractStmtSwitch implements Prototype {

	private static final Logger logger = LogManager.getLogger(AbstractStmtSwitch.class.getName());

	protected final MethodVariant method;

	protected final AbstractExprSwitch rexpr;

	protected final AbstractExprSwitch lexpr;

	protected final Context context;

	protected Stmt stmt;

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

	public void defaultCase(Object o) {
		logger.debug(o + " is not handled.");
	}

	public Stmt getStmt() {
		return stmt;
	}

	protected void process(Stmt stmt) {
		this.stmt = stmt;
		logger.debug(">>>>> Processing: " + stmt);
		stmt.apply(this);
		logger.debug("<<<<< Processing: " + stmt);
	}

	public Object prototype() {
		throw new UnsupportedOperationException("Parameterless clone() is not supported.");
	}

}// AbstractStmtSwitch
