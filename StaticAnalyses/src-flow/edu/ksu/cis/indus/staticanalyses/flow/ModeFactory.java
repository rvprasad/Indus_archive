
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

package edu.ksu.cis.indus.staticanalyses.flow;

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
	 * The prototype of index managers that manager indices related to arrays.
	 */
	private IPrototype arrayIndexManagerPrototype;

	/**
	 * The prototype of index managers that manage indices related to AST nodes.
	 */
	private IPrototype astIndexManagerPrototype;

	/**
	 * The prototype of class managers that manage class related primitive information and processing.  Processing of
	 * &lt;clinit&gt; would be an example of such information.
	 */
	private IPrototype classManagerPrototype;

	/**
	 * The prototype of index managers that manage indices related to instance field variables.
	 */
	private IPrototype instanceFieldIndexManagerPrototype;

	/**
	 * The prototype of LHS expression visitor to be used in the analysis.
	 */
	private IPrototype lhsExprPrototype;

	/**
	 * The prototype of index managers that manage indices related to methods.
	 */
	private IPrototype methodIndexManagerPrototype;

	/**
	 * The prototype of the flow graph node to be used in constructing the flow graph during the analysis.
	 */
	private IPrototype nodePrototype;

	/**
	 * The prototype of RHS expression visitor to be used in the analysis.
	 */
	private IPrototype rhsExprPrototype;

	/**
	 * A prototype of index managers to manage indices related to static field variables.
	 */
	private IPrototype staticFieldIndexManagerPrototype;

	/**
	 * The prototype of statement visitor to be used in the analysis.
	 */
	private IPrototype stmtPrototype;

	/**
	 * Returns an index manager to manage indices related to AST nodes.
	 *
	 * @return an index manager related to AST nodes.
	 */
	public final AbstractIndexManager getASTIndexManager() {
		return (AbstractIndexManager) astIndexManagerPrototype.getClone();
	}

	/**
	 * Returns an index manager to manage indices related arrays.
	 *
	 * @return an index manager related to arrays.
	 */
	public final AbstractIndexManager getArrayIndexManager() {
		return (AbstractIndexManager) arrayIndexManagerPrototype.getClone();
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param arrayIndexManagerPrototype the prototype to generate arrays related index manager objects.
	 */
	public void setArrayIndexManagerPrototype(IPrototype arrayIndexManagerPrototype) {
		this.arrayIndexManagerPrototype = arrayIndexManagerPrototype;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param astIndexManagerPrototype the prototype to generate AST node related index manager objects.
	 */
	public void setASTIndexManagerPrototype(IPrototype astIndexManagerPrototype) {
		this.astIndexManagerPrototype = astIndexManagerPrototype;
	}

	/**
	 * Returns an object that manages class related primitive information and processing.  Processing of &lt;clinit&gt; would
	 * be an example of such information.
	 *
	 * @param o parameter to be used to create manager.
	 *
	 * @return a <code>ClassManager</code> object parameterized by <code>o</code>.
	 */
	public final ClassManager getClassManager(Object o) {
		return (ClassManager) classManagerPrototype.getClone(o);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param classManagerPrototype the prototype to generate managers to manage class related primitive information and
	 * 		  processing.
	 */
	public void setClassManagerPrototype(IPrototype classManagerPrototype) {
		this.classManagerPrototype = classManagerPrototype;
	}

	/**
	 * Returns an flow graph node object that can be plugged into the flow graph.
	 *
	 * @param o parameter to be used to create the flow graph node.
	 *
	 * @return a flow graph node parameterized by <code>o</code>.
	 */
	public final AbstractFGNode getFGNode(Object o) {
		return (AbstractFGNode) nodePrototype.getClone(o);
	}

	/**
	 * Returns an index manager to manage indices related to instance field variables.
	 *
	 * @return an index manager related to instance field variables.
	 */
	public final AbstractIndexManager getInstanceFieldIndexManager() {
		return (AbstractIndexManager) instanceFieldIndexManagerPrototype.getClone();
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param instanceFieldIndexManagerPrototype the prototype to generate instance field variables related index manager
	 * 		  objects.
	 */
	public void setInstanceFieldIndexManagerPrototype(IPrototype instanceFieldIndexManagerPrototype) {
		this.instanceFieldIndexManagerPrototype = instanceFieldIndexManagerPrototype;
	}

	/**
	 * Returns a LHS expression visitor parameterized by <code>s</code>.
	 *
	 * @param s parameter to be used to create the expression visitor.
	 *
	 * @return a LHS expression visitor parameterizec by <code>s</code>.
	 */
	public final AbstractExprSwitch getLHSExprVisitor(AbstractStmtSwitch s) {
		return (AbstractExprSwitch) lhsExprPrototype.getClone(s);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param lhsExprPrototype the prototype to generate LHS expression vistor to be used in constructing the flow graph.
	 */
	public void setLHSExprVisitorPrototype(IPrototype lhsExprPrototype) {
		this.lhsExprPrototype = lhsExprPrototype;
	}

	/**
	 * Returns an index manager to manage indices related to methods.
	 *
	 * @return an index manager related to methods.
	 */
	public final AbstractIndexManager getMethodIndexManager() {
		return (AbstractIndexManager) methodIndexManagerPrototype.getClone();
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param methodIndexManagerPrototype the prototype to generate method related index manager objects.
	 */
	public final void setMethodIndexManagerPrototype(IPrototype methodIndexManagerPrototype) {
		this.methodIndexManagerPrototype = methodIndexManagerPrototype;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param nodePrototype the prototype to generate flow graph nodes to be used in constructing the flow graph.
	 */
	public final void setNodePrototype(IPrototype nodePrototype) {
		this.nodePrototype = nodePrototype;
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
	public final AbstractExprSwitch getRHSExprVisitor(AbstractStmtSwitch s) {
		return (AbstractExprSwitch) rhsExprPrototype.getClone(s);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param rhsExprPrototype the prototype to generate RHS expression vistor to be used in constructing the flow graph.
	 */
	public final void setRHSExprVisitorPrototype(IPrototype rhsExprPrototype) {
		this.rhsExprPrototype = rhsExprPrototype;
	}

	/**
	 * Returns an index manager to manage indices related to instance field variables.
	 *
	 * @return an index manager related to static field variables.
	 */
	public final AbstractIndexManager getStaticFieldIndexManager() {
		return (AbstractIndexManager) staticFieldIndexManagerPrototype.getClone();
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param staticFieldIndexManagerPrototype the prototype to generate static variables related index manager objects.
	 */
	public final void setStaticFieldIndexManagerPrototype(IPrototype staticFieldIndexManagerPrototype) {
		this.staticFieldIndexManagerPrototype = staticFieldIndexManagerPrototype;
	}

	/**
	 * Returns a statement visitor parameterized by <code>m</code>.
	 *
	 * @param m parameter to be used to create the statement visitor.
	 *
	 * @return a LHS expression visitor parameterizec by <code>m</code>.
	 */
	public final AbstractStmtSwitch getStmtVisitor(MethodVariant m) {
		return (AbstractStmtSwitch) stmtPrototype.getClone(m);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param stmtPrototype the prototype to generate statement vistor to be used in constructing the flow graph.
	 */
	public final void setStmtVisitorPrototype(IPrototype stmtPrototype) {
		this.stmtPrototype = stmtPrototype;
	}
}

/*****
 ChangeLog:

$Log$

*****/
