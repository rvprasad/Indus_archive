package edu.ksu.cis.bandera.bfa;

import org.apache.log4j.Category;

/**
 * ModeFactory.java
 *
 *
 * Created: Sun Jan 27 16:31:18 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public class ModeFactory {

	private static final Category cat = Category.getInstance(ModeFactory.class.getName());

	private final Prototype astIndexManager;

	private final Prototype arrayIndexManager;

	private final Prototype classManager;

	private final Prototype instanceFieldIndexManager;

	private final Prototype lexpr;

	private final Prototype methodIndexManager;

	private final Prototype node;

	private final Prototype rexpr;

	private final Prototype staticFieldIndexManager;

	private final Prototype stmt;

	public ModeFactory (Prototype astIM, Prototype arrayIM, Prototype instanceFieldIM, Prototype staticFieldIM,
						Prototype methodIM, Prototype node, Prototype stmt, Prototype lexpr, Prototype rexpr,
						Prototype classManager) {
		astIndexManager = astIM;
		arrayIndexManager = arrayIM;
		instanceFieldIndexManager = instanceFieldIM;
		staticFieldIndexManager = staticFieldIM;
		methodIndexManager = methodIM;
		this.classManager = classManager;
		this.node = node;
		this.stmt = stmt;
		this.lexpr = lexpr;
		this.rexpr = rexpr;
	}

	public final AbstractIndexManager getArrayIndexManager() {
		return (AbstractIndexManager)arrayIndexManager.prototype();
	}

	public final AbstractIndexManager getASTIndexManager() {
		return (AbstractIndexManager)astIndexManager.prototype();
	}

	public final ClassManager getClassManager(Object o) {
		return (ClassManager)classManager.prototype(o);
	}

	public final AbstractFGNode getFGNode(Object o) {
		return (AbstractFGNode)node.prototype(o);
	}

	public final AbstractIndexManager getInstanceFieldIndexManager() {
		return (AbstractIndexManager)instanceFieldIndexManager.prototype();
	}

	public final AbstractExprSwitch getLHSExpr(AbstractStmtSwitch s) {
		return (AbstractExprSwitch)lexpr.prototype(s);
	}

	public final AbstractIndexManager getMethodIndexManager() {
		return (AbstractIndexManager)methodIndexManager.prototype();
	}

	public final AbstractExprSwitch getRHSExpr(AbstractStmtSwitch s) {
		return (AbstractExprSwitch)rexpr.prototype(s);
	}

	public final AbstractIndexManager getStaticFieldIndexManager() {
		return (AbstractIndexManager)staticFieldIndexManager.prototype();
	}

	public final AbstractStmtSwitch getStmt(MethodVariant m) {
		return (AbstractStmtSwitch)stmt.prototype(m);
	}

}// ModeFactory
