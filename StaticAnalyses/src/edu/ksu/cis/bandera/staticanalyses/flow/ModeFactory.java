package edu.ksu.cis.bandera.staticanalyses.flow;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

//ModeFactory.java

/**
 * <p>An implementation of <i>Abstract Factory</i> pattern given in Gang of Four book.  It "creates" various compoments
 * required to setup and run the analysis.  Other components of the framework use this class to obtain components when
 * assembling the analysis and the flow graph.
 *
 * <p>Created: Sun Jan 27 16:31:18 2002</p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */

public class ModeFactory {

	/**
	 * <p>An instance of <code>Logger</code> used for logging purposes.</p>
	 *
	 */
	private static final Logger logger = LogManager.getLogger(ModeFactory.class);

	/**
	 * <p>The prototype of index managers that manage indices related to AST nodes.</p>
	 *
	 */
	private final Prototype astIndexManagerPrototype;

	/**
	 * <p>The prototype of index managers that manager indices related to arrays.</p>
	 *
	 */
	private final Prototype arrayIndexManagerPrototype;

	/**
	 * <p>The prototype of class managers that manage class related primitive information and processing.  Processing of
	 * <clinit> would be an example of such information.</p>
	 *
	 */
	private final Prototype classManagerPrototype;

	/**
	 * <p>The prototype of index managers that manage indices related to instance field variables.</p>
	 *
	 */
	private final Prototype instanceFieldIndexManagerPrototype;

	/**
	 * <p>The prototype of LHS expression visitor to be used in the analysis.</p>
	 *
	 */
	private final Prototype lhsExprPrototype;

	/**
	 * <p>The prototype of index managers that manage indices related to methods.</p>
	 *
	 */
	private final Prototype methodIndexManagerPrototype;

	/**
	 * <p>The prototype of the flow graph node to be used in constructing the flow graph during the analysis.</p>
	 *
	 */
	private final Prototype nodePrototype;

	/**
	 * <p>The prototype of RHS expression visitor to be used in the analysis.</p>
	 *
	 */
	private final Prototype rhsExprPrototype;

	/**
	 * <p>A prototype of index managers to manage indices related to static field variables.</p>
	 *
	 */
	private final Prototype staticFieldIndexManagerPrototype;

	/**
	 * <p>The prototype of statement visitor to be used in the analysis.</p>
	 *
	 */
	private final Prototype stmtPrototype;

	/**
	 * <p>Creates a new <code>ModeFactory</code> instance.  None of the parameters can be <code>null</code>.</p>
	 *
	 * @param astIMPrototype the prototype to generate AST node related index manager objects.
	 * @param arrayIMPrototype the prototype to generate arrays related index manager objects.
	 * @param instanceFieldIMPrototype the prototype to generate instance field variables related index manager objects.
	 * @param staticFieldIMPrototype the prototype to generate static variables related index manager objects.
	 * @param methodIMPrototype the prototype to generate method related index manager objects.
	 * @param nodePrototype the prototype to generate flow graph nodes to be used in constructing the flow graph.
	 * @param stmtPrototype the prototype to generate statement vistor to be used in constructing the flow graph.
	 * @param lhsExprPrototype the prototype to generate LHS expression vistor to be used in constructing the flow graph.
	 * @param rhsExprPrototype the prototype to generate RHS expression vistor to be used in constructing the flow graph.
	 * @param classManagerPrototype the prototype to generate managers to manage class related primitive information and
	 * processing.
	 */
	public ModeFactory (Prototype astIMPrototype, Prototype arrayIMPrototype, Prototype instanceFieldIMPrototype,
						Prototype staticFieldIMPrototype, Prototype methodIMPrototype, Prototype nodePrototype,
						Prototype stmtPrototype, Prototype lhsExprPrototype, Prototype rhsExprPrototype,
						Prototype classManagerPrototype) {
		astIndexManagerPrototype = astIMPrototype;
		arrayIndexManagerPrototype  = arrayIMPrototype;
		instanceFieldIndexManagerPrototype  = instanceFieldIMPrototype;
		staticFieldIndexManagerPrototype  = staticFieldIMPrototype;
		methodIndexManagerPrototype  = methodIMPrototype;
		this.classManagerPrototype = classManagerPrototype;
		this.nodePrototype = nodePrototype;
		this.stmtPrototype = stmtPrototype;
		this.lhsExprPrototype = lhsExprPrototype;
		this.rhsExprPrototype = rhsExprPrototype;
	}

	/**
	 * <p>Returns an index manager to manage indices related arrays.</p>
	 *
	 * @return an index manager related to arrays.
	 */
	public final AbstractIndexManager getArrayIndexManager() {
		return (AbstractIndexManager)arrayIndexManagerPrototype.prototype();
	}

	/**
	 * <p>Returns an index manager to manage indices related to AST nodes.</p>
	 *
	 * @return an index manager related to AST nodes.
	 */
	public final AbstractIndexManager getASTIndexManager() {
		return (AbstractIndexManager)astIndexManagerPrototype.prototype();
	}

	/**
	 * <p>Returns an object that manages class related primitive information and processing.  Processing of <clinit> would be
	 * an example of such information.
	 *
	 * @param o parameter to be used to create manager.
	 * @return a <code>ClassManager</code> object parameterized by <code>o</code>.
	 */
	public final ClassManager getClassManager(Object o) {
		return (ClassManager)classManagerPrototype.prototype(o);
	}

	/**
	 * <p>Returns an flow graph node object that can be plugged into the flow graph.</p>
	 *
	 * @param o parameter to be used to create the flow graph node.
	 * @return a flow graph node parameterized by <code>o</code>.
	 */
	public final AbstractFGNode getFGNode(Object o) {
		return (AbstractFGNode)nodePrototype.prototype(o);
	}

	/**
	 * <p>Returns an index manager to manage indices related to instance field variables.</p>
	 *
	 * @return an index manager related to instance field variables.
	 */
	public final AbstractIndexManager getInstanceFieldIndexManager() {
		return (AbstractIndexManager)instanceFieldIndexManagerPrototype.prototype();
	}

	/**
	 * <p>Returns a LHS expression visitor parameterized by <code>s</code>.</p>
	 *
	 * @param s parameter to be used to create the expression visitor.
	 * @return a LHS expression visitor parameterizec by <code>s</code>.
	 */
	public final AbstractExprSwitch getLHSExpr(AbstractStmtSwitch s) {
		return (AbstractExprSwitch)lhsExprPrototype.prototype(s);
	}

	/**
	 * <p>Returns an index manager to manage indices related to methods.</p>
	 *
	 * @return an index manager related to methods.
	 */
	public final AbstractIndexManager getMethodIndexManager() {
		return (AbstractIndexManager)methodIndexManagerPrototype.prototype();
	}

	/**
	 * <p>Returns a LHS expression visitor parameterized by <code>s</code>.</p>
	 *
	 * @param s parameter to be used to create the expression visitor.
	 * @return a LHS expression visitor parameterizec by <code>s</code>.
	 */
	public final AbstractExprSwitch getRHSExpr(AbstractStmtSwitch s) {
		return (AbstractExprSwitch)rhsExprPrototype.prototype(s);
	}

	/**
	 * <p>Returns an index manager to manage indices related to instance field variables.</p>
	 *
	 * @return an index manager related to static field variables.
	 */
	public final AbstractIndexManager getStaticFieldIndexManager() {
		return (AbstractIndexManager)staticFieldIndexManagerPrototype.prototype();
	}

	/**
	 * <p>Returns a statement visitor parameterized by <code>m</code>.</p>
	 *
	 * @param m parameter to be used to create the statement visitor.
	 * @return a LHS expression visitor parameterizec by <code>m</code>.
	 */
	public final AbstractStmtSwitch getStmt(MethodVariant m) {
		return (AbstractStmtSwitch)stmtPrototype.prototype(m);
	}

}// ModeFactory
