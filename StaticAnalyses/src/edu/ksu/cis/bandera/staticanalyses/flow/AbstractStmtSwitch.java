package edu.ksu.cis.bandera.bfa;

import ca.mcgill.sable.soot.jimple.Stmt;

import org.apache.log4j.Category;


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

	private static final Category cat = Category.getInstance(AbstractStmtSwitch.class.getName());

	protected final MethodVariant method;

	protected final AbstractExprSwitch rexpr;

	protected final AbstractExprSwitch lexpr;

	protected final Context context;

	protected Stmt stmt;

	protected AbstractStmtSwitch (MethodVariant m){
		method = m;
		context = m.context;
		lexpr = m.bfa.getLHSExpr(this);
		rexpr = m.bfa.getRHSExpr(this);
	}

	public void defaultCase(Object o) {
		cat.info(o + " is not handled.");
	}

	public Stmt getStmt() {
		return stmt;
	}

	protected void process(Stmt stmt) {
		this.stmt = stmt;
		stmt.apply(this);
	}

	public Object prototype() {
		throw new UnsupportedOperationException("Parameterless clone() is not supported.");
	}

}// AbstractStmtSwitch
