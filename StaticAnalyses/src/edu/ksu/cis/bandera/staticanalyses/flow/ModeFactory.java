
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


/**
 * <p>
 * An implementation of <i>Abstract Factory</i> pattern given in Gang of Four book.  It "creates" various compoments required
 * to setup and run the analysis.  Other components of the framework use this class to obtain components when assembling the
 * analysis and the flow graph.
 * </p>
 *
 * <p>
 * Created: Sun Jan 27 16:31:18 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class ModeFactory {
	/**
	 * <p>
	 * The prototype of index managers that manager indices related to arrays.
	 * </p>
	 */
	private final IPrototype arrayIndexManagerPrototype;

	/**
	 * <p>
	 * The prototype of index managers that manage indices related to AST nodes.
	 * </p>
	 */
	private final IPrototype astIndexManagerPrototype;

	/**
	 * <p>
	 * The prototype of class managers that manage class related primitive information and processing.  Processing of
	 * &lt;clinit&gt; would be an example of such information.
	 * </p>
	 */
	private final IPrototype classManagerPrototype;

	/**
	 * <p>
	 * The prototype of index managers that manage indices related to instance field variables.
	 * </p>
	 */
	private final IPrototype instanceFieldIndexManagerPrototype;

	/**
	 * <p>
	 * The prototype of LHS expression visitor to be used in the analysis.
	 * </p>
	 */
	private final IPrototype lhsExprPrototype;

	/**
	 * <p>
	 * The prototype of index managers that manage indices related to methods.
	 * </p>
	 */
	private final IPrototype methodIndexManagerPrototype;

	/**
	 * <p>
	 * The prototype of the flow graph node to be used in constructing the flow graph during the analysis.
	 * </p>
	 */
	private final IPrototype nodePrototype;

	/**
	 * <p>
	 * The prototype of RHS expression visitor to be used in the analysis.
	 * </p>
	 */
	private final IPrototype rhsExprPrototype;

	/**
	 * <p>
	 * A prototype of index managers to manage indices related to static field variables.
	 * </p>
	 */
	private final IPrototype staticFieldIndexManagerPrototype;

	/**
	 * <p>
	 * The prototype of statement visitor to be used in the analysis.
	 * </p>
	 */
	private final IPrototype stmtPrototype;

	/**
	 * <p>
	 * Creates a new <code>ModeFactory</code> instance.  None of the parameters can be <code>null</code>.
	 * </p>
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
	 *           processing.
	 */
	public ModeFactory(IPrototype astIMPrototype, IPrototype arrayIMPrototype, IPrototype instanceFieldIMPrototype,
		IPrototype staticFieldIMPrototype, IPrototype methodIMPrototype, IPrototype nodePrototype, IPrototype stmtPrototype,
		IPrototype lhsExprPrototype, IPrototype rhsExprPrototype, IPrototype classManagerPrototype) {
		astIndexManagerPrototype = astIMPrototype;
		arrayIndexManagerPrototype = arrayIMPrototype;
		instanceFieldIndexManagerPrototype = instanceFieldIMPrototype;
		staticFieldIndexManagerPrototype = staticFieldIMPrototype;
		methodIndexManagerPrototype = methodIMPrototype;
		this.classManagerPrototype = classManagerPrototype;
		this.nodePrototype = nodePrototype;
		this.stmtPrototype = stmtPrototype;
		this.lhsExprPrototype = lhsExprPrototype;
		this.rhsExprPrototype = rhsExprPrototype;
	}

	/**
	 * <p>
	 * Returns an index manager to manage indices related to AST nodes.
	 * </p>
	 *
	 * @return an index manager related to AST nodes.
	 */
	public final AbstractIndexManager getASTIndexManager() {
		return (AbstractIndexManager) astIndexManagerPrototype.prototype();
	}

	/**
	 * <p>
	 * Returns an index manager to manage indices related arrays.
	 * </p>
	 *
	 * @return an index manager related to arrays.
	 */
	public final AbstractIndexManager getArrayIndexManager() {
		return (AbstractIndexManager) arrayIndexManagerPrototype.prototype();
	}

	/**
	 * <p>
	 * Returns an object that manages class related primitive information and processing.  Processing of &lt;clinit&gt; would
	 * be an example of such information.
	 * </p>
	 *
	 * @param o parameter to be used to create manager.
	 *
	 * @return a <code>ClassManager</code> object parameterized by <code>o</code>.
	 */
	public final ClassManager getClassManager(Object o) {
		return (ClassManager) classManagerPrototype.prototype(o);
	}

	/**
	 * <p>
	 * Returns an flow graph node object that can be plugged into the flow graph.
	 * </p>
	 *
	 * @param o parameter to be used to create the flow graph node.
	 *
	 * @return a flow graph node parameterized by <code>o</code>.
	 */
	public final AbstractFGNode getFGNode(Object o) {
		return (AbstractFGNode) nodePrototype.prototype(o);
	}

	/**
	 * <p>
	 * Returns an index manager to manage indices related to instance field variables.
	 * </p>
	 *
	 * @return an index manager related to instance field variables.
	 */
	public final AbstractIndexManager getInstanceFieldIndexManager() {
		return (AbstractIndexManager) instanceFieldIndexManagerPrototype.prototype();
	}

	/**
	 * <p>
	 * Returns a LHS expression visitor parameterized by <code>s</code>.
	 * </p>
	 *
	 * @param s parameter to be used to create the expression visitor.
	 *
	 * @return a LHS expression visitor parameterizec by <code>s</code>.
	 */
	public final AbstractExprSwitch getLHSExpr(AbstractStmtSwitch s) {
		return (AbstractExprSwitch) lhsExprPrototype.prototype(s);
	}

	/**
	 * <p>
	 * Returns an index manager to manage indices related to methods.
	 * </p>
	 *
	 * @return an index manager related to methods.
	 */
	public final AbstractIndexManager getMethodIndexManager() {
		return (AbstractIndexManager) methodIndexManagerPrototype.prototype();
	}

	/**
	 * <p>
	 * Returns a LHS expression visitor parameterized by <code>s</code>.
	 * </p>
	 *
	 * @param s parameter to be used to create the expression visitor.
	 *
	 * @return a LHS expression visitor parameterizec by <code>s</code>.
	 */
	public final AbstractExprSwitch getRHSExpr(AbstractStmtSwitch s) {
		return (AbstractExprSwitch) rhsExprPrototype.prototype(s);
	}

	/**
	 * <p>
	 * Returns an index manager to manage indices related to instance field variables.
	 * </p>
	 *
	 * @return an index manager related to static field variables.
	 */
	public final AbstractIndexManager getStaticFieldIndexManager() {
		return (AbstractIndexManager) staticFieldIndexManagerPrototype.prototype();
	}

	/**
	 * <p>
	 * Returns a statement visitor parameterized by <code>m</code>.
	 * </p>
	 *
	 * @param m parameter to be used to create the statement visitor.
	 *
	 * @return a LHS expression visitor parameterizec by <code>m</code>.
	 */
	public final AbstractStmtSwitch getStmt(MethodVariant m) {
		return (AbstractStmtSwitch) stmtPrototype.prototype(m);
	}
}

/*****
 ChangeLog:

$Log$

*****/
